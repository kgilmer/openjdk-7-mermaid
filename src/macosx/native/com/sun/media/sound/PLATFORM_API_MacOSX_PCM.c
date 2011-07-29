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
#include "DirectAudio.h"
#include "PLATFORM_API_MacOSX_Utils.h"

#if USE_DAUDIO == TRUE

/*
 TODO

 Add the default input/output as the first device.
 Support devices with multiple streams.
 */

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
	TRACE3("> DAUDIO_GetFormats %d %d %d\n", (int)mixerIndex, (int)deviceID, isSource);
    AudioDeviceDescription description = {0};
    OSStatus err = noErr;
    Float64 sampleRate;
    int numStreams;
    UInt32 size;

    GetAudioDeviceDescription(mixerIndex, &description);

    numStreams = isSource ? description.numInputStreams : description.numOutputStreams;

    if (numStreams == 0)
        goto exit;

    /*
     For now register 16-bit for any sample rate.
     Come back to this if applications need something different.
     */

    DAUDIO_AddAudioFormat(creator,
                          16, /* 16-bit */
                          0,  /* frame size (auto) */
                          description.numChannels,  /* channels */
                          -1, /* sample rate (any) */
                          DAUDIO_PCM, /* only accept PCM */
                          1,  /* signed (for 16-bit) */
                          BYTE_ORDER == BIG_ENDIAN);

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
	desc.componentSubType      = kAudioUnitSubType_GenericOutput;
	desc.componentManufacturer = kAudioUnitManufacturer_Apple;
	desc.componentFlags        = 0;
	desc.componentFlagsMask    = 0;

    Component comp = FindNextComponent(NULL, &desc);
	err = OpenAComponent(comp, &unit);

    if (err)
        goto exit;

    AudioUnitSetProperty(unit, kAudioOutputUnitProperty_CurrentDevice, kAudioUnitScope_Global,
                         0, &deviceID, sizeof(deviceID));
    return unit;

exit:

    if (unit)
        CloseComponent(unit);

    return NULL;
}

void* DAUDIO_Open(INT32 mixerIndex, INT32 deviceID, int isSource,
                  int encoding, float sampleRate, int sampleSizeInBits,
                  int frameSize, int channels,
                  int isSigned, int isBigEndian, int bufferSizeInBytes) {
	TRACE0("> DAUDIO_Open\n");

    return NULL;
}

int DAUDIO_Start(void* id, int isSource) {
    TRACE0("> DAUDIO_Start\n");
    return FALSE;
}

int DAUDIO_Stop(void* id, int isSource) {
    TRACE0("> DAUDIO_Stop\n");
    return FALSE;
}

void DAUDIO_Close(void* id, int isSource) {
    TRACE0("> DAUDIO_Close\n");
}

int DAUDIO_Write(void* id, char* data, int byteSize) {
    TRACE0("> DAUDIO_Write\n");

    return FALSE;
}

int DAUDIO_Read(void* id, char* data, int byteSize) {
    TRACE0("> DAUDIO_Read\n");

    return FALSE;
}

int DAUDIO_GetBufferSize(void* id, int isSource) {
    TRACE0("> DAUDIO_GetBufferSize\n");

    return 0;
}

int DAUDIO_StillDraining(void* id, int isSource) {
    TRACE0("> DAUDIO_StillDraining\n");

    return FALSE;
}

int DAUDIO_Flush(void* id, int isSource) {
    TRACE0("> DAUDIO_Flush\n");

    return FALSE;
}

int DAUDIO_GetAvailable(void* id, int isSource) {
    TRACE0("> DAUDIO_GetAvailable\n");

    return 0;
}

INT64 DAUDIO_GetBytePosition(void* id, int isSource, INT64 javaBytePos) {
    TRACE0("> DAUDIO_GetBytePosition\n");

    return 0;
}

void DAUDIO_SetBytePosition(void* id, int isSource, INT64 javaBytePos) {
    TRACE0("> DAUDIO_SetBytePosition\n");

}

int DAUDIO_RequiresServicing(void* id, int isSource) {
    TRACE0("> DAUDIO_RequiresServicing\n");

    return FALSE;
}

void DAUDIO_Service(void* id, int isSource) {
    // nothing to do
}

#endif