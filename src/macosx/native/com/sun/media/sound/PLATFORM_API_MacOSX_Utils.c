/*
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
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

//#define USE_TRACE
//#define USE_ERROR

#include "PLATFORM_API_MacOSX_Utils.h"
#include "Ports.h"

typedef struct OSXAudioDevice {
    AudioDeviceID deviceID;
    int numInputStreams;
    int numOutputStreams;

    /* May actually be different for different streams on a device */
    int numChannels;
} OSXAudioDevice;

typedef struct {
    int numDevices;
    OSXAudioDevice *devices;
} AudioDeviceContext;

static AudioDeviceContext deviceCtx;

static int GetDeviceChannelCount(AudioDeviceID deviceID)
{
    int streamID;
    OSStatus err;
    UInt32 size;

    err = GetAudioObjectPropertySize(deviceID, kAudioDevicePropertyScopeOutput, kAudioDevicePropertyStreams, &size);
    if (!err && size) {
        GetAudioObjectProperty(deviceID, kAudioDevicePropertyScopeOutput, kAudioDevicePropertyStreams,
                               sizeof(streamID), &streamID, 1);
    } else {
        err = GetAudioObjectPropertySize(deviceID, kAudioDevicePropertyScopeInput, kAudioDevicePropertyStreams, &size);
        if (!err && size) {
            GetAudioObjectProperty(deviceID, kAudioDevicePropertyScopeInput, kAudioDevicePropertyStreams,
                                   sizeof(streamID), &streamID, 1);
        } else {
            ERROR1("GetDeviceChannelCount error %.4s\n", &err);
            return 0;
        }
    }

    AudioStreamBasicDescription asbd = {0};

    err = GetAudioObjectProperty(streamID, kAudioObjectPropertyScopeGlobal, kAudioStreamPropertyVirtualFormat,
                                 sizeof(asbd), &asbd, 1);
    if (err)
        return 0;
    TRACE3("by AudioStream: samplerate %f channels %d bits %d\n", asbd.mSampleRate, (int)asbd.mChannelsPerFrame, (int)asbd.mBitsPerChannel);

    return asbd.mChannelsPerFrame;
}

int GetAudioDeviceCount()
{
    const AudioObjectPropertyAddress devicesAddress = {kAudioHardwarePropertyDevices, kAudioObjectPropertyScopeGlobal, kAudioObjectPropertyElementMaster};
    UInt32 size;
    int i;

    if (!deviceCtx.numDevices) {
        AudioObjectGetPropertyDataSize(kAudioObjectSystemObject, &devicesAddress, 0, NULL, &size);
        deviceCtx.numDevices = size / sizeof(AudioDeviceID);

        if (deviceCtx.numDevices) {
            AudioDeviceID deviceIDs[deviceCtx.numDevices];
            deviceCtx.devices = calloc(deviceCtx.numDevices, sizeof(OSXAudioDevice));

            size = deviceCtx.numDevices * sizeof(AudioDeviceID);
            AudioObjectGetPropertyData(kAudioObjectSystemObject, &devicesAddress, 0, NULL, &size, deviceIDs);
            deviceCtx.numDevices = size / sizeof(AudioDeviceID); // in case it changed

            for (i = 0; i < deviceCtx.numDevices; i++) {
                OSXAudioDevice *device = &deviceCtx.devices[i];

                device->deviceID = deviceIDs[i];

                GetAudioObjectPropertySize(device->deviceID, kAudioDevicePropertyScopeInput, kAudioDevicePropertyStreams,
                                           &size);
                device->numInputStreams  = size / sizeof(AudioStreamID);

                GetAudioObjectPropertySize(device->deviceID, kAudioDevicePropertyScopeOutput, kAudioDevicePropertyStreams,
                                           &size);
                device->numOutputStreams = size / sizeof(AudioStreamID);

                device->numChannels = GetDeviceChannelCount(device->deviceID);
            }
        }
    }

    return deviceCtx.numDevices;
}

int GetAudioDeviceDescription(int index, AudioDeviceDescription *description)
{
    OSXAudioDevice *device = &deviceCtx.devices[index];
    CFStringRef name = NULL, vendor = NULL;
    OSStatus err = noErr;
    UInt32 size;

    description->deviceID         = device->deviceID;
    description->numInputStreams  = device->numInputStreams;
    description->numOutputStreams = device->numOutputStreams;
    description->numChannels      = device->numChannels;

    if (description->name) {
        err = GetAudioObjectProperty(device->deviceID, kAudioObjectPropertyScopeGlobal, kAudioObjectPropertyName,
                                     sizeof(name), &name, 1);
        if (err) goto exit;

        CFStringGetCString(name, description->name, description->strLen, kCFStringEncodingUTF8);
        if (description->description)
            CFStringGetCString(name, description->description, description->strLen, kCFStringEncodingUTF8);
    }

    if (description->vendor) {
        err = GetAudioObjectProperty(device->deviceID, kAudioObjectPropertyScopeGlobal, kAudioObjectPropertyManufacturer,
                                     sizeof(vendor), &vendor, 1);
        if (err) goto exit;

        CFStringGetCString(vendor, description->vendor, description->strLen, kCFStringEncodingUTF8);
    }

exit:
    if (err) {
        ERROR1("GetAudioDeviceDescription error %.4s\n", &err);
    }
    if (name)   CFRelease(name);
    if (vendor) CFRelease(vendor);

    return err ? FALSE : TRUE;
}

OSStatus GetAudioObjectProperty(AudioObjectID object, AudioObjectPropertyScope scope, AudioObjectPropertySelector property, UInt32 size, void *data, int checkSize)
{
    const AudioObjectPropertyAddress address = {property, scope, kAudioObjectPropertyElementMaster};
    UInt32 oldSize = size;
    OSStatus err;

    err = AudioObjectGetPropertyData(object, &address, 0, NULL, &size, data);

    if (!err && checkSize && size != oldSize)
        return kAudioHardwareBadPropertySizeError;
    return err;
}

OSStatus GetAudioObjectPropertySize(AudioObjectID object, AudioObjectPropertyScope scope, AudioObjectPropertySelector property, UInt32 *size)
{
    const AudioObjectPropertyAddress address = {property, scope, kAudioObjectPropertyElementMaster};
    OSStatus err;

    err = AudioObjectGetPropertyDataSize(object, &address, 0, NULL, size);

    return err;
}
