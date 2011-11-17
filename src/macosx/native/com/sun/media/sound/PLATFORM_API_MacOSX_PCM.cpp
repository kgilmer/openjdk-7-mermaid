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

#define USE_ERROR
#define USE_TRACE
#define USE_VERBOSE_TRACE

#include <AudioUnit/AudioUnit.h>
#include <CoreServices/CoreServices.h>
#include <pthread.h>
/*
#if !defined(__COREAUDIO_USE_FLAT_INCLUDES__)
#include <CoreAudio/CoreAudioTypes.h>
#else
#include <CoreAudioTypes.h>
#endif
*/

extern "C" {
#include "DirectAudio.h"
#include "PLATFORM_API_MacOSX_Utils.h"
}

#if USE_DAUDIO == TRUE

// =======================================
// MixerProvider functions implementation

INT32 DAUDIO_GetDirectAudioDeviceCount() {
    int count = 1 + GetAudioDeviceCount();
    TRACE1("DAUDIO_GetDirectAudioDeviceCount returns %d\n", count);
    return count;
}

INT32 DAUDIO_GetDirectAudioDeviceDescription(INT32 mixerIndex, DirectAudioDeviceDescription *desc) {
    AudioDeviceDescription description;
    description.strLen = DAUDIO_STRING_LENGTH;
    description.name   = desc->name;
    description.vendor = desc->vendor;
    description.description = desc->description;

    // We can't fill out the version field.

    int err = GetAudioDeviceDescription(mixerIndex - 1, &description);

    desc->deviceID = description.deviceID;
    // TODO: (FIXIT) maxSimulLines = ???
    desc->maxSimulLines = description.numInputStreams + description.numOutputStreams;

    TRACE2("DAUDIO_GetDirectAudioDeviceDescription (mixerIndex = %d), returns %d lines\n", (int)mixerIndex, (int)desc->maxSimulLines);

    return err == noErr ? TRUE : FALSE;
}

/*
Philosophical differences here -

Sample rate -
Audio Output Unit has a current sample rate, but will accept any sample rate for output.
For input, the sample rate of the unit must be the current hardware sample rate.
It is possible for the user to change this, but there is no way to notify Java of it
happening, so this is not handled.

Integer format -
Core Audio uses floats. Audio Output Unit will accept anything.
The best Direct Audio can do is 16-bit signed.
*/

void DAUDIO_GetFormats(INT32 mixerIndex, INT32 deviceID, int isSource, void* creator) {
    TRACE3("DAUDIO_GetFormats mixer=%d deviceID=%#x isSource=%d\n", (int)mixerIndex, (int)deviceID, isSource);

    OSStatus err = noErr;
    AudioDeviceDescription description;
    description.strLen = 0; // don't fill name/vendor/description

    GetAudioDeviceDescription(mixerIndex - 1, &description);

    Float64 sampleRate;
    int numStreams, numChannels;
    UInt32 size;

    numChannels = isSource ? description.numOutputChannels : description.numInputChannels;

    int channels[] = {1, 2, numChannels};
    int i;

    numStreams = isSource ? description.numOutputStreams : description.numInputStreams;

    if (numStreams == 0) {
        goto exit;
    }

    /*
    For output, register 16-bit for any sample rate with either 1ch/2ch.
    For input, we must use the real sample rate.
    */

    int maxChannelIndex;

    if (!isSource && numChannels == 1)
        maxChannelIndex = 0;
    else if (numChannels <= 2)
        maxChannelIndex = 1;
    else
        maxChannelIndex = 2;

    sampleRate = isSource ? -1 : description.inputSampleRate;

    for (i = 0; i <= maxChannelIndex; i++) {
        DAUDIO_AddAudioFormat(creator,
            16,             // 16-bit
            -1,             // frame size (auto)
            channels[i],    // channels
            sampleRate,     // sample rate (any for output)
            DAUDIO_PCM,     // only accept PCM
            1,              // signed (for 16-bit)
            BYTE_ORDER == BIG_ENDIAN);
    }

exit:
    if (err) {
        ERROR1("DAUDIO_GetFormats err %d\n", (int)err);
    }
}


// =======================================
// Source/Target DataLine functions implementation

// ====
/* 1writer-1reader ring buffer class with flush() support */
class RingBuffer {
public:
    RingBuffer() : pBuffer(NULL), nBufferSize(0) {
        pthread_mutex_init(&lockMutex, NULL); 
    }
    ~RingBuffer() {
        Deallocate();
        pthread_mutex_destroy(&lockMutex);
    }

    // extraBytes: number of additionally allocated bytes to prevent data
    // overlapping when almost whole buffer is filled
    // (required only if Write() can override the buffer)
    bool Allocate(int requestedBufferSize, int extraBytes) {
        int fullBufferSize = requestedBufferSize + extraBytes;
        int powerOfTwo = 1;
        while (powerOfTwo < fullBufferSize) {
            powerOfTwo <<= 1;
        }
        pBuffer = (Byte*)malloc(powerOfTwo);
        if (pBuffer == NULL) {
            ERROR0("RingBuffer::Allocate: OUT OF MEMORY\n");
            return false;
        }

        nBufferSize = requestedBufferSize;
        nAllocatedBytes = powerOfTwo;
        nPosMask = powerOfTwo - 1;
        nWritePos = 0;
        nReadPos = 0;
        nFlushPos = -1;

        TRACE2("RingBuffer::Allocate: OK, bufferSize=%d, allocated:%d\n", nBufferSize, nAllocatedBytes);
        return true;
    }

    void Deallocate() {
        if (pBuffer) {
            free(pBuffer);
            pBuffer = NULL;
            nBufferSize = 0;
        }
    }

    inline int GetBufferSize() {
        return nBufferSize;
    }

    inline int GetAllocatedSize() {
        return nAllocatedBytes;
    }

    // gets number of bytes available for reading
    int GetValidByteCount() {
        lock();
        INT64 result = nWritePos - (nFlushPos >= 0 ? nFlushPos : nReadPos);
        unlock();
        return result > (INT64)nBufferSize ? nBufferSize : (int)result;
    }

    int Write(void *srcBuffer, int len, bool preventOverflow) {
        lock();
        TRACE2("RingBuffer::Write (%d bytes, preventOverflow=%d)\n", len, preventOverflow ? 1 : 0);
        TRACE2("  writePos = %lld (%d)", (long long)nWritePos, Pos2Offset(nWritePos));
        TRACE2("  readPos=%lld (%d)", (long long)nReadPos, Pos2Offset(nReadPos));
        TRACE2("  flushPos=%lld (%d)\n", (long long)nFlushPos, Pos2Offset(nFlushPos));

        INT64 writePos = nWritePos;
        if (preventOverflow) {
            INT64 avail_read = writePos - (nFlushPos >= 0 ? nFlushPos : nReadPos);
            if (avail_read >= (INT64)nBufferSize) {
                // no space
                TRACE0("  preventOverlow: OVERFLOW => len = 0;\n");
                len = 0;
            } else {
                int avail_write = nBufferSize - (int)avail_read;
                if (len > avail_write) {
                    TRACE2("  preventOverlow: desrease len: %d => %d\n", len, avail_write);
                    len = avail_write;
                }
            }
        }
        unlock();

        if (len > 0) {

            write((Byte *)srcBuffer, Pos2Offset(writePos), len);

            lock();
            TRACE4("--RingBuffer::Write writePos: %lld (%d) => %lld, (%d)\n",
                (long long)nWritePos, Pos2Offset(nWritePos), (long long)nWritePos + len, Pos2Offset(nWritePos + len));
            nWritePos += len;
            unlock();
        }
        return len;
    }

    int Read(void *dstBuffer, int len) {
        lock();
        TRACE1("RingBuffer::Read (%d bytes)\n", len);
        TRACE2("  writePos = %lld (%d)", (long long)nWritePos, Pos2Offset(nWritePos));
        TRACE2("  readPos=%lld (%d)", (long long)nReadPos, Pos2Offset(nReadPos));
        TRACE2("  flushPos=%lld (%d)\n", (long long)nFlushPos, Pos2Offset(nFlushPos));

        applyFlush();
        INT64 avail_read = nWritePos - nReadPos;
        // check for overflow
        if (avail_read > (INT64)nBufferSize) {
            nReadPos = nWritePos - nBufferSize;
            avail_read = nBufferSize;
            TRACE0("  OVERFLOW\n");
        }
        INT64 readPos = nReadPos;
        unlock();

        if (len > (int)avail_read) {
            TRACE2("  RingBuffer::Read - don't have enough data, len: %d => %d\n", len, (int)avail_read);
            len = (int)avail_read;
        }

        if (len > 0) {

            read((Byte *)dstBuffer, Pos2Offset(readPos), len);

            lock();
            if (applyFlush()) {
                // just got flush(), results became obsolete
                TRACE0("--RingBuffer::Read, got Flush, return 0\n");
                len = 0;
            } else {
                TRACE4("--RingBuffer::Read readPos: %lld (%d) => %lld (%d)\n",
                    (long long)nReadPos, Pos2Offset(nReadPos), (long long)nReadPos + len, Pos2Offset(nReadPos + len));
                nReadPos += len;
            }
            unlock();
        } else {
            // underrun!
        }
        return len;
    }

    // returns number of the flushed bytes
    int Flush() {
        lock();
        INT64 flushedBytes = nWritePos - (nFlushPos >= 0 ? nFlushPos : nReadPos);
        nFlushPos = nWritePos;
        unlock();
        return flushedBytes > (INT64)nBufferSize ? nBufferSize : (int)flushedBytes;
    }

private:
    Byte *pBuffer;
    int nBufferSize;
    int nAllocatedBytes;
    INT64 nPosMask;

    pthread_mutex_t lockMutex;

    volatile INT64 nWritePos;
    volatile INT64 nReadPos;
    // Flush() sets nFlushPos value to nWritePos;
    // next Read() sets nReadPos to nFlushPos and resests nFlushPos to -1
    volatile INT64 nFlushPos;

    inline void lock() {
        pthread_mutex_lock(&lockMutex);
    }
    inline void unlock() {
        pthread_mutex_unlock(&lockMutex);
    }

    inline bool applyFlush() {
        if (nFlushPos >= 0) {
            nReadPos = nFlushPos;
            nFlushPos = -1;
            return true;
        }
        return false;
    }

    inline int Pos2Offset(INT64 pos) {
        return (int)(pos & nPosMask);
    }

    void write(Byte *srcBuffer, int dstOffset, int len) {
        int dstEndOffset = dstOffset + len;

        int lenAfterWrap = dstEndOffset - nAllocatedBytes;
        if (lenAfterWrap > 0) {
            // dest.buffer does wrap
            len = nAllocatedBytes - dstOffset;
            memcpy(pBuffer+dstOffset, srcBuffer, len);
            memcpy(pBuffer, srcBuffer+len, lenAfterWrap);
        } else {
            // dest.buffer does not wrap
            memcpy(pBuffer+dstOffset, srcBuffer, len);
        }
    }

    void read(Byte *dstBuffer, int srcOffset, int len) {
        int srcEndOffset = srcOffset + len;

        int lenAfterWrap = srcEndOffset - nAllocatedBytes;
        if (lenAfterWrap > 0) {
            // need to unwrap data
            len = nAllocatedBytes - srcOffset;
            memcpy(dstBuffer, pBuffer+srcOffset, len);
            memcpy(dstBuffer+len, pBuffer, lenAfterWrap);
        } else {
            // source buffer is not wrapped
            memcpy(dstBuffer, pBuffer+srcOffset, len);
        }
    }
};


struct CA_DirectAudioDevice {
    AudioUnit   audioUnit;
    RingBuffer  ringBuffer;
    AudioStreamBasicDescription asbd;

    // only for target lines
    UInt32      inputBufferSizeInBytes;

    CA_DirectAudioDevice() : audioUnit(NULL), asbd() {
    }

    ~CA_DirectAudioDevice() {
        if (audioUnit) {
            CloseComponent(audioUnit);
        }
    }
};

static AudioUnit CreateOutputUnit(AudioDeviceID deviceID, int isSource)
{
    OSStatus err = noErr;
    AudioUnit unit;
    UInt32 size;

    ComponentDescription desc;
    desc.componentType         = kAudioUnitType_Output;
    desc.componentSubType      = (deviceID == 0 && isSource)
                                    ? kAudioUnitSubType_DefaultOutput
                                    : kAudioUnitSubType_HALOutput;
    desc.componentManufacturer = kAudioUnitManufacturer_Apple;
    desc.componentFlags        = 0;
    desc.componentFlagsMask    = 0;

    Component comp = FindNextComponent(NULL, &desc);
    err = OpenAComponent(comp, &unit);

    if (err) {
        ERROR1("OpenComponent err %d\n", (int)err);
        goto exit;
    }

    if (!isSource) {
        int enableIO = 0;
        err = AudioUnitSetProperty(unit, kAudioOutputUnitProperty_EnableIO, kAudioUnitScope_Output,
                                    0, &enableIO, sizeof(enableIO));
        if (err) {
            ERROR1("SetProperty 1 err %d\n", (int)err);
        }
        enableIO = 1;
        err = AudioUnitSetProperty(unit, kAudioOutputUnitProperty_EnableIO, kAudioUnitScope_Input,
                                    1, &enableIO, sizeof(enableIO));
        if (err) {
            ERROR1("SetProperty 2 err %d\n", (int)err);
        }
    }

    if (deviceID || !isSource) {
        UInt32 inputDeviceID;

        /* There is no "default input unit", so get the current input device. */
        GetAudioObjectProperty(kAudioObjectSystemObject, kAudioObjectPropertyScopeGlobal,
                                    kAudioHardwarePropertyDefaultInputDevice,
                                    sizeof(inputDeviceID), &inputDeviceID, 1);

        err = AudioUnitSetProperty(unit, kAudioOutputUnitProperty_CurrentDevice, kAudioUnitScope_Global,
                                    0, deviceID ? &deviceID : &inputDeviceID, sizeof(deviceID));
        if (err) {
            ERROR1("SetProperty 3 err %d\n", (int)err);
            goto exit;
        }
    }

    return unit;

exit:
    if (unit)
        CloseComponent(unit);
    return NULL;
}

static OSStatus OutputCallback(void                         *inRefCon,
                               AudioUnitRenderActionFlags   *ioActionFlags,
                               const AudioTimeStamp         *inTimeStamp,
                               UInt32                       inBusNumber,
                               UInt32                       inNumberFrames,
                               AudioBufferList              *ioData)
{
    CA_DirectAudioDevice *device = (CA_DirectAudioDevice*)inRefCon;

    int nchannels = ioData->mNumberBuffers; // should be always == 1 (interleaved channels)
    AudioBuffer *audioBuffer = ioData->mBuffers;

    TRACE3(">>OutputCallback: busNum=%d, requested %d frames (%d bytes)\n",
        (int)inBusNumber, (int)inNumberFrames, (int)(inNumberFrames * device->asbd.mBytesPerFrame));
    TRACE3("  abl: %d buffers, buffer[0].channels=%d, buffer.size=%d\n",
        nchannels, (int)audioBuffer->mNumberChannels, (int)audioBuffer->mDataByteSize);

    int bytesToRead = inNumberFrames * device->asbd.mBytesPerFrame;
    if (bytesToRead > (int)audioBuffer->mDataByteSize) {
        TRACE0("--OutputCallback: !!! audioBuffer IS TOO SMALL!!!\n");
        bytesToRead = audioBuffer->mDataByteSize / device->asbd.mBytesPerFrame * device->asbd.mBytesPerFrame;
    }
    int bytesRead = device->ringBuffer.Read(audioBuffer->mData, bytesToRead);
    if (bytesRead < bytesToRead) {
        // no enough data (underrun)
        TRACE2("--OutputCallback: !!! UNDERRUN (read %d bytes of %d)!!!\n", bytesRead, bytesToRead);
        // silence the rest
        memset((Byte*)audioBuffer->mData + bytesRead, 0, bytesToRead-bytesRead);
        bytesRead = bytesToRead;
    }

    audioBuffer->mDataByteSize = (UInt32)bytesRead;
    // SAFETY: set mDataByteSize for all other AudioBuffer in the AudioBufferList to zero
    while (--nchannels > 0) {
        audioBuffer++;
        audioBuffer->mDataByteSize = 0;
    }
    TRACE1("<<OutputCallback (returns %d)\n", bytesRead);

    return noErr;
}

static OSStatus InputCallback(void                          *inRefCon,
                              AudioUnitRenderActionFlags    *ioActionFlags,
                              const AudioTimeStamp          *inTimeStamp,
                              UInt32                        inBusNumber,
                              UInt32                        inNumberFrames,
                              AudioBufferList               *ioData)
{
    CA_DirectAudioDevice *device = (CA_DirectAudioDevice*)inRefCon;

    TRACE4(">>InputCallback: busNum=%d, timeStamp=%lld, %d frames (%d bytes)\n",
        (int)inBusNumber, (long long)inTimeStamp->mSampleTime, (int)inNumberFrames, (int)(inNumberFrames * device->asbd.mBytesPerFrame));

    AudioBufferList abl;    // by default it contains 1 AudioBuffer
    abl.mNumberBuffers = 1;
    abl.mBuffers[0].mNumberChannels = device->asbd.mChannelsPerFrame;
    abl.mBuffers[0].mDataByteSize   = device->inputBufferSizeInBytes;   // assume this is == (inNumberFrames * device->asbd.mBytesPerFrame)
    abl.mBuffers[0].mData           = NULL;     // request for the audioUnit's buffer

    // TODO: is it possible device->inputBufferSizeInBytes != inNumberFrames * device->asbd.mBytesPerFrame??

    OSStatus err = AudioUnitRender(device->audioUnit, ioActionFlags, inTimeStamp, inBusNumber, inNumberFrames, &abl);
    if (err) {
        ERROR1("InputCallback: ERROR(%d) AudioUnitRender\n", (int)err);
    } else {
        int bytesWritten = device->ringBuffer.Write(abl.mBuffers[0].mData, (int)abl.mBuffers[0].mDataByteSize, false);
        TRACE2("<<OutputCallback (saved %d bytes of %d)\n", bytesWritten, (int)abl.mBuffers[0].mDataByteSize);
    }

    return noErr;
}

void* DAUDIO_Open(INT32 mixerIndex, INT32 deviceID, int isSource,
                  int encoding, float sampleRate, int sampleSizeInBits,
                  int frameSize, int channels,
                  int isSigned, int isBigEndian, int bufferSizeInBytes)
{
    TRACE2(">>DAUDIO_Open: mixerIndex=%d deviceID=%#x\n", (int)mixerIndex, (unsigned int)deviceID);

    CA_DirectAudioDevice *device = new CA_DirectAudioDevice;

    AudioUnitScope scope = isSource ? kAudioUnitScope_Input : kAudioUnitScope_Output;
    int element = isSource ? 0 : 1;
    OSStatus err = noErr;
    int extraBufferBytes = 0;

#ifdef USE_TRACE
    if (deviceID) AudioObjectShow(deviceID);
#endif

    device->audioUnit = CreateOutputUnit(deviceID, isSource);

    if (!device->audioUnit) {
        goto exit;
    }

    FillOutASBDForLPCM(device->asbd, sampleRate, channels, sampleSizeInBits, sampleSizeInBits, 0, isBigEndian);

    err = AudioUnitSetProperty(device->audioUnit, kAudioUnitProperty_StreamFormat, scope, element, &device->asbd, sizeof(device->asbd));
    if (err) {
        ERROR1("<<DAUDIO_Open ERROR(%d): set StreamFormat\n", (int)err);
        goto exit;
    }

    AURenderCallbackStruct output;
    output.inputProc       = isSource ? OutputCallback : InputCallback;
    output.inputProcRefCon = device;

    err = AudioUnitSetProperty(device->audioUnit,
                                isSource
                                    ? (AudioUnitPropertyID)kAudioUnitProperty_SetRenderCallback
                                    : (AudioUnitPropertyID)kAudioOutputUnitProperty_SetInputCallback,
                                kAudioUnitScope_Global, 0, &output, sizeof(output));
    if (err) {
        ERROR1("<<DAUDIO_Open ERROR(%d): set RenderCallback\n", (int)err);
        goto exit;
    }

    err = AudioUnitInitialize(device->audioUnit);
    if (err) {
        ERROR1("<<DAUDIO_Open ERROR(%d): UnitInitialize\n", (int)err);
        goto exit;
    }

    if (!isSource) {
        // for target lines we need extra bytes in the buffer
        // to prevent collisions when AudioInputCallback overrides data on overflow
        UInt32 size;
        OSStatus err;

        size = sizeof(device->inputBufferSizeInBytes);
        err  = AudioUnitGetProperty(device->audioUnit, kAudioDevicePropertyBufferFrameSize, kAudioUnitScope_Global,
                                    0, &device->inputBufferSizeInBytes, &size);
        if (err) {
            ERROR1("<<DAUDIO_Open ERROR(%d): (TargetDataLine)GetBufferSize\n", (int)err);
            goto exit;
        }
        device->inputBufferSizeInBytes *= device->asbd.mBytesPerFrame;  // convert frames by bytes
        extraBufferBytes = (int)device->inputBufferSizeInBytes;
    }

    if (!device->ringBuffer.Allocate(bufferSizeInBytes, extraBufferBytes)) {
        ERROR0("DAUDIO_Open: Ring buffer allocation error\n");
        goto exit;
    }

    TRACE0("<<DAUDIO_Open: OK\n");
    return device;

exit:
    delete device;
    return NULL;
}

int DAUDIO_Start(void* id, int isSource) {
    CA_DirectAudioDevice *device = (CA_DirectAudioDevice*)id;
    TRACE0("DAUDIO_Start\n");

    OSStatus err = AudioOutputUnitStart(device->audioUnit);

    return err == noErr ? TRUE : FALSE;
}

int DAUDIO_Stop(void* id, int isSource) {
    CA_DirectAudioDevice *device = (CA_DirectAudioDevice*)id;
    TRACE0("DAUDIO_Stop\n");

    OSStatus err = AudioOutputUnitStop(device->audioUnit);

    return err == noErr ? TRUE : FALSE;
}

void DAUDIO_Close(void* id, int isSource) {
    CA_DirectAudioDevice *device = (CA_DirectAudioDevice*)id;
    TRACE0("DAUDIO_Close\n");

    delete device;
}

int DAUDIO_Write(void* id, char* data, int byteSize) {
    CA_DirectAudioDevice *device = (CA_DirectAudioDevice*)id;
    TRACE1(">>DAUDIO_Write: %d bytes to write\n", byteSize);

    int result = device->ringBuffer.Write(data, byteSize, true);

    TRACE1("<<DAUDIO_Write: %d bytes written\n", result);
    return result;
}

int DAUDIO_Read(void* id, char* data, int byteSize) {
    CA_DirectAudioDevice *device = (CA_DirectAudioDevice*)id;
    TRACE1(">>DAUDIO_Read: %d bytes to read\n", byteSize);

    int result = device->ringBuffer.Read(data, byteSize);

    TRACE1("<<DAUDIO_Read: %d bytes has been read\n", result);
    return result;
}

int DAUDIO_GetBufferSize(void* id, int isSource) {
    CA_DirectAudioDevice *device = (CA_DirectAudioDevice*)id;

    int bufferSizeInBytes = device->ringBuffer.GetBufferSize();

    TRACE1("DAUDIO_GetBufferSize returns %d\n", bufferSizeInBytes);
    return bufferSizeInBytes;
}

int DAUDIO_StillDraining(void* id, int isSource) {
    CA_DirectAudioDevice *device = (CA_DirectAudioDevice*)id;

    int draining = device->ringBuffer.GetValidByteCount() > 0 ? TRUE : FALSE;

    TRACE1("DAUDIO_StillDraining returns %d\n", draining);
    return draining;
}

int DAUDIO_Flush(void* id, int isSource) {
    CA_DirectAudioDevice *device = (CA_DirectAudioDevice*)id;
    TRACE0("DAUDIO_Flush\n");

    device->ringBuffer.Flush();

    return TRUE;
}

int DAUDIO_GetAvailable(void* id, int isSource) {
    CA_DirectAudioDevice *device = (CA_DirectAudioDevice*)id;

    int bytesInBuffer = device->ringBuffer.GetValidByteCount();
    if (isSource) {
        return device->ringBuffer.GetBufferSize() - bytesInBuffer;
    } else {
        return bytesInBuffer;
    }
}

INT64 DAUDIO_GetBytePosition(void* id, int isSource, INT64 javaBytePos) {
    CA_DirectAudioDevice *device = (CA_DirectAudioDevice*)id;
    INT64 position;

    if (isSource) {
        position = javaBytePos - device->ringBuffer.GetValidByteCount();
    } else {
        position = javaBytePos + device->ringBuffer.GetValidByteCount();
    }

    TRACE2("DAUDIO_GetBytePosition returns %lld (javaBytePos = %lld)\n", (long long)position, (long long)javaBytePos);
    return position;
}

void DAUDIO_SetBytePosition(void* id, int isSource, INT64 javaBytePos) {
    // no need javaBytePos (it's available in DAUDIO_GetBytePosition)
}

int DAUDIO_RequiresServicing(void* id, int isSource) {
    return FALSE;
}

void DAUDIO_Service(void* id, int isSource) {
    // unreachable
}

#endif  // USE_DAUDIO == TRUE
