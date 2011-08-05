/*
 * Copyright (c) 2002, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

//#define USE_ERROR
//#define USE_TRACE

#include <AudioUnit/AudioUnit.h>
#include <CoreServices/CoreServices.h>
#include <CoreAudio/CoreAudio.h>
#include <libkern/OSAtomic.h>
#include <algorithm>

#include <CABitOperations.h>
#include "CARingBuffer.h"

extern "C" {
#include "DirectAudio.h"
#include "PLATFORM_API_MacOSX_Utils.h"
}

#if USE_DAUDIO == TRUE

/*
 TODO

 Add the default input/output as the first device.
 Support devices with multiple streams.
 */

typedef struct OSXAudioDevice {
    AudioUnit    unit;
    CARingBuffer buffer;
    AudioStreamBasicDescription asbd;
    SInt64       lastSampleTimeRead;
    int          bufferSizeInFrames;

    OSXAudioDevice() : unit(NULL), asbd(), lastSampleTimeRead(0), bufferSizeInFrames(0) {}
} OSXAudioDevice;

INT32 DAUDIO_GetDirectAudioDeviceCount() {
    TRACE0("> DAUDIO_GetDirectAudioDeviceCount\n");
    int count = GetAudioDeviceCount();
    TRACE1("< DAUDIO_GetDirectAudioDeviceCount = %d\n", count);

    return count;
}

INT32 DAUDIO_GetDirectAudioDeviceDescription(INT32 mixerIndex, DirectAudioDeviceDescription* daudioDescription) {
    TRACE0("> DAUDIO_GetDirectAudioDeviceDescription\n");
    AudioDeviceDescription description;

    description.strLen = DAUDIO_STRING_LENGTH;
    description.name   = daudioDescription->name;
    description.vendor = daudioDescription->vendor;
    description.description = daudioDescription->description;

    /*
     We can't fill out the version field.
     */

    int err = GetAudioDeviceDescription(mixerIndex, &description);

    daudioDescription->deviceID = description.deviceID;
    daudioDescription->maxSimulLines = description.numInputStreams + description.numOutputStreams;

    TRACE1("< DAUDIO_GetDirectAudioDeviceDescription = %d lines\n", (int)daudioDescription->maxSimulLines);

    return err == noErr;
}

/*
 Philosophical differences here -

 Sample rate -
 Audio Output Unit has a current sample rate, but will accept any sample rate.
 The device has a list of nominal sample rates, but we don't want to get into changing those.

 Integer format -
 Core Audio uses floats. Audio Output Unit will accept anything.
 The best Direct Audio can do is 16-bit signed.
 */

void DAUDIO_GetFormats(INT32 mixerIndex, INT32 deviceID, int isSource, void* creator) {
	TRACE3("> DAUDIO_GetFormats %d %d isSource=%d\n", (int)mixerIndex, (int)deviceID, isSource);
    AudioDeviceDescription description = {0};
    OSStatus err = noErr;
    Float64 sampleRate;
    int numStreams;
    UInt32 size;

    GetAudioDeviceDescription(mixerIndex, &description);

    int channels[] = {1, 2, description.numChannels};
    int i;

    numStreams = isSource ? description.numOutputStreams : description.numInputStreams;

    if (numStreams == 0)
        goto exit;

    /*
     For now register 16-bit for any sample rate.
     Come back to this if applications need something different.
     */

    for (i = 0; i < ((description.numChannels > 2) ? 3 : 2); i++) {
        DAUDIO_AddAudioFormat(creator,
                              16, /* 16-bit */
                              -1,  /* frame size (auto) */
                              channels[i],  /* channels */
                              -1, /* sample rate (any) */
                              DAUDIO_PCM, /* only accept PCM */
                              1,  /* signed (for 16-bit) */
                              BYTE_ORDER == BIG_ENDIAN);
    }

exit:
    if (err) {
        ERROR1("DAUDIO_GetFormats err %.4s\n", &err);
    }
}

static AudioUnit CreateOutputUnit(AudioDeviceID deviceID)
{
    OSStatus err = noErr;
    AudioUnit unit;
    UInt32 size;

	ComponentDescription desc;
	desc.componentType         = kAudioUnitType_Output;
	desc.componentSubType      = kAudioUnitSubType_HALOutput;
	desc.componentManufacturer = kAudioUnitManufacturer_Apple;
	desc.componentFlags        = 0;
	desc.componentFlagsMask    = 0;

    Component comp = FindNextComponent(NULL, &desc);
	err = OpenAComponent(comp, &unit);

    if (err) {
        ERROR1("OpenComponent err %d\n", err);
        goto exit;
    }

    err = AudioUnitSetProperty(unit, kAudioOutputUnitProperty_CurrentDevice, kAudioUnitScope_Global,
                         0, &deviceID, sizeof(deviceID));
    if (err) {
        ERROR1("SetProperty err %d\n", err);
        goto exit;
    }

    return unit;

exit:

    if (unit)
        CloseComponent(unit);

    return NULL;
}

static void ClearAudioBufferList(AudioBufferList *list, int start, int end, int sampleSize)
{
    for (int channel = 0; channel < list->mNumberBuffers; channel++)
        memset((char*)list->mBuffers[channel].mData + start*sampleSize, 0, (end - start)*sampleSize);
}

OSStatus AudioOutputCallback(void                           *inRefCon,
                             AudioUnitRenderActionFlags 	*ioActionFlags,
                             const AudioTimeStamp           *inTimeStamp,
                             UInt32 						inBusNumber,
                             UInt32 						inNumberFrames,
                             AudioBufferList                *ioData)
{
    OSXAudioDevice *device = (OSXAudioDevice*)inRefCon;
    CARingBufferError err;

    SInt64 readTime = device->lastSampleTimeRead;
    SInt64 startTime, endTime;
    int underran = 0;
    err = device->buffer.GetTimeBounds(startTime, endTime);

    if (err) goto exit;
    if (readTime >= endTime) goto exit;
    if (readTime + inNumberFrames >= endTime) {
        ClearAudioBufferList(ioData, endTime - readTime, inNumberFrames, device->asbd.mBytesPerFrame);
        inNumberFrames = endTime - readTime;
        underran = 1;
    }

    err = device->buffer.Fetch(ioData, inNumberFrames, readTime);

    if (err) goto exit;

    if (underran) {
        TRACE4("< Underrun, only fetched %d frames (%lld %lld %lld)\n", inNumberFrames, startTime, readTime, endTime);
    }

    OSAtomicCompareAndSwap64Barrier(readTime, readTime + inNumberFrames, &device->lastSampleTimeRead);

    return noErr;

exit:
    ClearAudioBufferList(ioData, 0, inNumberFrames, device->asbd.mBytesPerFrame);
    return noErr;
}

void* DAUDIO_Open(INT32 mixerIndex, INT32 deviceID, int isSource,
                  int encoding, float sampleRate, int sampleSizeInBits,
                  int frameSize, int channels,
                  int isSigned, int isBigEndian, int bufferSizeInBytes)
{
	TRACE0("> DAUDIO_Open\n");

    if (!isSource) {
        /* Not yet implemented */
        return NULL;
    }

    AudioUnitScope scope = isSource ? kAudioUnitScope_Input : kAudioUnitScope_Output;
    OSXAudioDevice *device = new OSXAudioDevice;
    int bufferSizeInFrames = bufferSizeInBytes / frameSize;
    OSStatus err = noErr;

    device->unit = CreateOutputUnit(deviceID);

    if (!device->unit)
        goto exit;

    FillOutASBDForLPCM(device->asbd, sampleRate, channels, sampleSizeInBits, sampleSizeInBits, 0, isBigEndian);

    err = AudioUnitSetProperty(device->unit, kAudioUnitProperty_StreamFormat, scope, 0, &device->asbd, sizeof(device->asbd));
    if (err) goto exit;

    AURenderCallbackStruct output;
    output.inputProc       = AudioOutputCallback;
    output.inputProcRefCon = device;

    err = AudioUnitSetProperty(device->unit, kAudioUnitProperty_SetRenderCallback, kAudioUnitScope_Global, 0, &output, sizeof(output));
    if (err) goto exit;

    err = AudioUnitInitialize(device->unit);
    if (err) goto exit;

    device->buffer.Allocate(channels, frameSize, bufferSizeInFrames);
    device->bufferSizeInFrames = NextPowerOfTwo(bufferSizeInFrames);

    return device;

exit:
    if (err) {
        ERROR1("DAUDIO_Open err %d\n", err);
    }

    delete device;
    return NULL;
}

int DAUDIO_Start(void* id, int isSource) {
    TRACE0("> DAUDIO_Start\n");
    OSXAudioDevice *device = (OSXAudioDevice*)id;
    OSStatus err;

    err = AudioOutputUnitStart(device->unit);

    return err == noErr ? TRUE : FALSE;
}

int DAUDIO_Stop(void* id, int isSource) {
    TRACE0("> DAUDIO_Stop\n");
    OSXAudioDevice *device = (OSXAudioDevice*)id;
    OSStatus err;

    err = AudioOutputUnitStop(device->unit);

    return err == noErr ? TRUE : FALSE;
}

void DAUDIO_Close(void* id, int isSource) {
    TRACE0("> DAUDIO_Close\n");
    OSXAudioDevice *device = (OSXAudioDevice*)id;

    device->buffer.Deallocate();
    CloseComponent(device->unit);
    delete device;
}

static SInt64 GetLastSampleTime(CARingBuffer *buffer)
{
    SInt64 startTime, endTime;
    OSStatus err = buffer->GetTimeBounds(startTime, endTime);

    return endTime;
}

int DAUDIO_Write(void* id, char* data, int byteSize) {
    OSXAudioDevice *device = (OSXAudioDevice*)id;
    AudioBufferList bufferList;

    bufferList.mNumberBuffers = 1;
    bufferList.mBuffers[0].mNumberChannels= device->asbd.mChannelsPerFrame;
    bufferList.mBuffers[0].mDataByteSize  = byteSize;
    bufferList.mBuffers[0].mData          = data;

    SInt64 firstAvailableSampleTime, lastWrittenSampleTime, lastReadSampleTime, newLastSampleTime;
    device->buffer.GetTimeBounds(firstAvailableSampleTime, lastWrittenSampleTime);
    lastReadSampleTime = device->lastSampleTimeRead;

    int numAvailableFrames = byteSize / device->asbd.mBytesPerFrame;
    int numUsedFrames      = lastWrittenSampleTime - firstAvailableSampleTime;
    int numFreeFrames      = (lastReadSampleTime - firstAvailableSampleTime) /* already-written samples */
                           + (device->bufferSizeInFrames - numUsedFrames);   /* space at the end of the buffer */
    int numFrames          = std::min(numAvailableFrames, numFreeFrames);

    if (numFrames == 0)
        return 0;

    TRACE4("> DAUDIO_Write %d frames (%d %d %d)\n", numFrames,
                                                    numFreeFrames,
                                                    numAvailableFrames,
                                                    lastWrittenSampleTime - lastReadSampleTime);

    int err = device->buffer.Store(&bufferList, numFrames, lastWrittenSampleTime);

    if (err) {
        TRACE1("Store err %d\n", err);
    }

    return err == kCARingBufferError_OK ? numFrames * device->asbd.mBytesPerFrame /* bytes written */
                                        : -1;                                     /* error */
}

int DAUDIO_Read(void* id, char* data, int byteSize) {
    TRACE0("> DAUDIO_Read\n");

    return FALSE;
}

int DAUDIO_GetBufferSize(void* id, int isSource) {
    TRACE0("> DAUDIO_GetBufferSize\n");
    OSXAudioDevice *device = (OSXAudioDevice*)id;
    int bufferSizeInBytes = device->bufferSizeInFrames * device->asbd.mBytesPerFrame;

    TRACE1("< %d\n", bufferSizeInBytes);
    return bufferSizeInBytes;
}

int DAUDIO_StillDraining(void* id, int isSource) {
    TRACE0("> DAUDIO_StillDraining\n");
    OSXAudioDevice *device = (OSXAudioDevice*)id;
    SInt64 startTime, endTime;
    int draining;

    device->buffer.GetTimeBounds(startTime, endTime);

    draining = device->lastSampleTimeRead < endTime;

    TRACE1("< %d\n", draining);
    return draining;
}

int DAUDIO_Flush(void* id, int isSource) {
    TRACE0("> DAUDIO_Flush\n");
    OSXAudioDevice *device = (OSXAudioDevice*)id;
    SInt64 startTime, endTime;
    CARingBufferError err;

    err = device->buffer.GetTimeBounds(startTime, endTime);
    if (err)
        return FALSE;

    device->buffer.Store(NULL, 0, startTime);

    return 0;
}

int DAUDIO_GetAvailable(void* id, int isSource) {
    TRACE0("> DAUDIO_GetAvailable\n");

    return 0;
}

INT64 DAUDIO_GetBytePosition(void* id, int isSource, INT64 javaBytePos) {
    TRACE0("> DAUDIO_GetBytePosition\n");
    OSXAudioDevice *device = (OSXAudioDevice*)id;
    INT64 position;

    position = (device->lastSampleTimeRead * device->asbd.mBytesPerFrame) - javaBytePos;

    TRACE1("< %lld\n", position);
    return position;
}

void DAUDIO_SetBytePosition(void* id, int isSource, INT64 javaBytePos) {
    TRACE0("> DAUDIO_SetBytePosition\n");
}

int DAUDIO_RequiresServicing(void* id, int isSource) {
    TRACE0("> DAUDIO_RequiresServicing\n");
    return FALSE;
}

void DAUDIO_Service(void* id, int isSource) {
    // Nothing to do
}

#endif
