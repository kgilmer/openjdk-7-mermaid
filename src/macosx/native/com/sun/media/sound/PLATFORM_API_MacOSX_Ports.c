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

//#define USE_ERROR
//#define USE_TRACE

#include "Ports.h"
#include <CoreAudio/CoreAudio.h>
#include <IOKit/audio/IOAudioTypes.h>

#if USE_PORTS == TRUE

/*
 TODO

 Handle devices with >2 channels. Check isInput=1 as well as =0.
 Test that this works properly with device plug/unplug.
 Compare control names to other platforms.
 */

typedef struct {
    struct PortMixer *mixer;

    AudioObjectID control;
    AudioClassID class; // kAudioVolumeControlClassID etc.
    char *jcontrolType; // CONTROL_TYPE_VOLUME etc.

    int channel; // master = 0, channels = 1 2 ...
} PortControl;

typedef struct {
    int numDevices; // = # port mixers
    AudioDeviceID *devices;
} PortsContext;

typedef struct {
    AudioDeviceID deviceID;

    // = # of ports on the mixer
    // cached here in case the values can change
    int numInputStreams;
    int numOutputStreams;
    // streams[0..numInputStreams-1] contains inputs,
    // streams[numInputStreams..numInputStreams+numOutputStreams-1] contains outputs
    AudioStreamID *streams;

    int numDeviceControls;
    PortControl *deviceControls;

    /*
     TODO these
     */
    /*
    int *numStreamControls;
    PortControl **streamControls;
    */
} PortMixer;

static PortsContext ports_ctx;

INT32 PORT_GetPortMixerCount() {
    UInt32 size;

    TRACE0("> PORT_GetPortMixerCount\n");

    AudioHardwareGetPropertyInfo(kAudioHardwarePropertyDevices, &size, NULL);
    ports_ctx.numDevices = size / sizeof(AudioDeviceID);

    if (ports_ctx.numDevices) {
        ports_ctx.devices = calloc(ports_ctx.numDevices, sizeof(AudioDeviceID));
        size = ports_ctx.numDevices * sizeof(AudioDeviceID);
        AudioHardwareGetProperty(kAudioHardwarePropertyDevices, &size, ports_ctx.devices);
        ports_ctx.numDevices = size / sizeof(AudioDeviceID); // in case it changed
    }

    TRACE1("< PORT_GetPortMixerCount = %d\n", ports_ctx.numDevices);

    return ports_ctx.numDevices;
}

INT32 PORT_GetPortMixerDescription(INT32 mixerIndex, PortMixerDescription* description) {
    TRACE0("> PORT_GetPortMixerDescription\n");

    AudioDeviceID deviceID = ports_ctx.devices[mixerIndex];
    CFStringRef name = NULL, vendor = NULL;
    OSStatus err = noErr;
    UInt32 size;

    size = sizeof(name);
    err = AudioDeviceGetProperty(deviceID, 0, 0, kAudioObjectPropertyName, &size, &name);
    if (err) goto exit;

    size = sizeof(vendor);
    err = AudioDeviceGetProperty(deviceID, 0, 0, kAudioObjectPropertyManufacturer, &size, &vendor);
    if (err) goto exit;

    CFStringGetCString(name, description->name, PORT_STRING_LENGTH, kCFStringEncodingUTF8);
    CFStringGetCString(vendor, description->vendor, PORT_STRING_LENGTH, kCFStringEncodingUTF8);

    /*
     We can't fill out the 'version' and 'description' fields.
     */

exit:
    if (name)   CFRelease(name);
    if (vendor) CFRelease(vendor);

    TRACE0("< PORT_GetPortMixerDescription\n");

    return err ? FALSE : TRUE;
}

void* PORT_Open(INT32 mixerIndex) {
    TRACE1("> PORT_Open %d\n", mixerIndex);
    PortMixer *mixer = calloc(1, sizeof(PortMixer));
    mixer->deviceID = ports_ctx.devices[mixerIndex];

    UInt32 size;

    AudioDeviceGetPropertyInfo(mixer->deviceID, 0, 1, kAudioDevicePropertyStreams, &size, NULL);
    mixer->numInputStreams  = size / sizeof(AudioStreamID);

    AudioDeviceGetPropertyInfo(mixer->deviceID, 0, 0, kAudioDevicePropertyStreams, &size, NULL);
    mixer->numOutputStreams = size / sizeof(AudioStreamID);

    if (mixer->numInputStreams || mixer->numOutputStreams) {
        mixer->streams = calloc(mixer->numInputStreams + mixer->numOutputStreams, sizeof(AudioStreamID));

        size = mixer->numInputStreams * sizeof(AudioStreamID);
        AudioDeviceGetProperty(mixer->deviceID, 0, 1, kAudioDevicePropertyStreams, &size, mixer->streams);

        size = mixer->numOutputStreams * sizeof(AudioStreamID);
        AudioDeviceGetProperty(mixer->deviceID, 0, 0, kAudioDevicePropertyStreams, &size, mixer->streams + mixer->numInputStreams);
    }

    TRACE1("< PORT_Open %p\n", mixer);
    return mixer;
}

void PORT_Close(void* id) {
    TRACE1("> PORT_Close %p\n", id);

    if (id) {
        PortMixer *mixer = id;
        free(mixer->streams);
        free(mixer);
    }

    TRACE0("< PORT_Close\n");
}

INT32 PORT_GetPortCount(void* id) {
    TRACE0("> PORT_GetPortCount\n");
    PortMixer *mixer = id;
    int numStreams = mixer->numInputStreams + mixer->numOutputStreams;
    TRACE1("< PORT_GetPortCount = %d\n", numStreams);
    return numStreams;
}

INT32 PORT_GetPortType(void* id, INT32 portIndex) {
    TRACE0("> PORT_GetPortType\n");
    PortMixer *mixer = id;

    AudioStreamID streamID = mixer->streams[portIndex];
    UInt32 direction;
    UInt32 terminalType;
    OSStatus err;
    int size;
    int ret;

    size = sizeof(terminalType);
    err = AudioStreamGetProperty(streamID, 0, kAudioStreamPropertyTerminalType, &size, &terminalType);
    if (err) goto exit;

    size = sizeof(direction);
    err = AudioStreamGetProperty(streamID, 0, kAudioStreamPropertyDirection, &size, &direction);
    if (err) goto exit;

    /*
     Note that kAudioStreamPropertyTerminalType actually returns values from
     IOAudioTypes.h, not the defined kAudioStreamTerminalType*.
     */

    if (direction) {
        // input
        switch (terminalType) {
            case EXTERNAL_LINE_CONNECTOR:
                ret = PORT_SRC_LINE_IN;
                break;

            case INPUT_MICROPHONE:
                ret = PORT_SRC_MICROPHONE;
                break;

            case EXTERNAL_SPDIF_INTERFACE:
                ret = PORT_SRC_UNKNOWN;
                break;

            default:
                TRACE1("unknown input terminal type %#x\n", terminalType);
#ifdef USE_TRACE
                AudioObjectShow(mixer->deviceID);
                AudioObjectShow(streamID);
#endif
                ret = PORT_SRC_UNKNOWN;
        }
    } else {
        // output
        switch (terminalType) {
            case EXTERNAL_LINE_CONNECTOR:
                ret = PORT_DST_LINE_OUT;
                break;

            case OUTPUT_SPEAKER:
                ret = PORT_DST_SPEAKER;
                break;

            case OUTPUT_HEADPHONES:
                ret = PORT_DST_HEADPHONE;
                break;

            case EXTERNAL_SPDIF_INTERFACE:
                ret = PORT_DST_UNKNOWN;
                break;

            default:
                TRACE1("unknown output terminal type %#x\n", terminalType);
#ifdef USE_TRACE
                AudioObjectShow(mixer->deviceID);
                AudioObjectShow(streamID);
#endif
                ret = PORT_DST_UNKNOWN;
        }
    }

    TRACE1("< PORT_GetPortType = %d\n", ret);
    return ret;
exit:
    ERROR1("< PORT_GetPortType error %d\n", err);
    return -1;
}

INT32 PORT_GetPortName(void* id, INT32 portIndex, char* name, INT32 len) {
    TRACE0("> PORT_GetPortName\n");

    PortMixer *mixer = id;
    AudioStreamID streamID = mixer->streams[portIndex];

    CFStringRef cfname = NULL;
    OSStatus err = noErr;
    UInt32 size;

    size = sizeof(cfname);
    err = AudioStreamGetProperty(streamID, 0, kAudioObjectPropertyName, &size, &cfname);
    if (err && err != kAudioHardwareUnknownPropertyError) goto exit;

    if (!cfname) {
        // use the device's name if the stream has no name (usually the case)
        size = sizeof(cfname);
        err = AudioDeviceGetProperty(mixer->deviceID, 0, 0, kAudioObjectPropertyName, &size, &cfname);
    }

    if (cfname) {
        CFStringGetCString(cfname, name, len, kCFStringEncodingUTF8);
    }

exit:
    if (cfname) CFRelease(cfname);

    TRACE0("< PORT_GetPortName\n");

    return FALSE;
}

static void *CreateVolumeControl(PortControlCreator *creator, PortControl *control)
{
    Float32 min = 0, max = 1, precision;
    UInt32 size;
    control->jcontrolType = CONTROL_TYPE_VOLUME;

    const AudioObjectPropertyAddress rangeAddress = {kAudioLevelControlPropertyDecibelRange, kAudioObjectPropertyScopeGlobal, kAudioObjectPropertyElementMaster};
    AudioValueRange range;

    size = sizeof(range);
    AudioObjectGetPropertyData(control->control, &rangeAddress, 0, NULL, &size, &range);
    precision = 1. / (range.mMaximum - range.mMinimum);

    return creator->newFloatControl(creator, control, CONTROL_TYPE_VOLUME, min, max, precision, "dB");
}

static void *CreateMuteControl(PortControlCreator *creator, PortControl *control)
{
    control->jcontrolType = CONTROL_TYPE_MUTE;
    return creator->newBooleanControl(creator, control, CONTROL_TYPE_MUTE);
}

void PORT_GetControls(void* id, INT32 portIndex, PortControlCreator* creator) {
    TRACE0("> PORT_GetControls\n");
    PortMixer *mixer = id;
    AudioStreamID streamID = mixer->streams[portIndex];

    const AudioObjectPropertyAddress ownedAddress = { kAudioObjectPropertyOwnedObjects, kAudioObjectPropertyScopeGlobal, kAudioObjectPropertyElementMaster };
    UInt32 size;
    OSStatus err;
    int i;

    // numDeviceControls / numStreamControls are overestimated
    // because we don't actually filter by if the owned objects are controls

    err = AudioObjectGetPropertyDataSize(mixer->deviceID, &ownedAddress, 0, NULL, &size);
    if (err) goto exit;

    mixer->numDeviceControls = size / sizeof(AudioObjectID);

    err = AudioObjectGetPropertyDataSize(streamID, &ownedAddress, 0, NULL, &size);
    if (err) goto exit;

    TRACE2("%d controls on device, %d on stream\n", mixer->numDeviceControls, size / sizeof(AudioObjectID));

    if (mixer->numDeviceControls) {
        AudioObjectID *controlIDs = calloc(mixer->numDeviceControls, sizeof(AudioObjectID));
        mixer->deviceControls     = calloc(mixer->numDeviceControls, sizeof(PortMixer));

        size = mixer->numDeviceControls * sizeof(AudioObjectID);
        err = AudioObjectGetPropertyData(mixer->deviceID, &ownedAddress, 0, NULL, &size, controlIDs);
        for (i = 0; i < mixer->numDeviceControls; i++)
            mixer->deviceControls[i].control = controlIDs[i];
    }

    /*
     TODO
     Add Balance for stereo devices
     Always add a Master Gain control for master volume?
     */

    int numVolumeControls = 0, numMuteControls = 0; // not counting the master
    int hasChannelVolume  = 0, hasChannelMute  = 0;
    PortControl *masterVolume = NULL, *masterMute = NULL;
    PortControl *noiseReduction = NULL;

    // setup all the device control structs, count volume and mute devices
    for (i = 0; i < mixer->numDeviceControls; i++) {
        const AudioObjectPropertyAddress controlAddress = {kAudioObjectPropertyClass, kAudioObjectPropertyScopeGlobal, kAudioObjectPropertyElementMaster};
        const AudioObjectPropertyAddress elementAddress = {kAudioControlPropertyElement, kAudioObjectPropertyScopeGlobal, kAudioObjectPropertyElementMaster};

        PortControl *control = &mixer->deviceControls[i];
        size = sizeof(control->class);
        AudioObjectGetPropertyData(control->control, &controlAddress, 0, NULL, &size, &control->class);
        control->mixer = mixer;

        size = sizeof(control->channel);
        err = AudioObjectGetPropertyData(control->control, &elementAddress, 0, NULL, &size, &control->channel);

        if (err) continue; // not a control

        switch (control->class) {
            case kAudioVolumeControlClassID:
                if (control->channel == 0)
                    masterVolume = control;
                else {
                    numVolumeControls++;
                    hasChannelVolume = 1;
                }
                break;

            case kAudioMuteControlClassID:
                if (control->channel == 0)
                    masterMute = control;
                else {
                    numMuteControls++;
                    hasChannelMute = 1;
                }
                break;

            case 'nzca':
                noiseReduction = control;
                break;
        }
    }

    AudioObjectShow(mixer->deviceID);
    TRACE4("volume: channel %d master %d, mute: channel %d master %d\n", hasChannelVolume, masterVolume != NULL, hasChannelMute, masterMute != NULL);

    if (masterVolume) {
        void *jControl = CreateVolumeControl(creator, masterVolume);
        creator->addControl(creator, jControl);
    }

    if (masterMute) {
        void *jControl = CreateMuteControl(creator, masterMute);
        creator->addControl(creator, jControl);
    }

    if (noiseReduction) {
        noiseReduction->jcontrolType = "Noise Reduction";
        void *jControl = creator->newBooleanControl(creator, noiseReduction, "Noise Reduction");
        creator->addControl(creator, jControl);
    }

    if (numVolumeControls) {
        void **jControls = calloc(numVolumeControls, sizeof(void*));
        int j = 0;
        for (i = 0; i < mixer->numDeviceControls && j < numVolumeControls; i++) {
            PortControl *control = &mixer->deviceControls[i];

            if (control->class != kAudioVolumeControlClassID || control->channel == 0)
                continue;

            jControls[j++] = CreateVolumeControl(creator, control);
        }

        void *compoundControl = creator->newCompoundControl(creator, "Volume", jControls, numVolumeControls);
        creator->addControl(creator, compoundControl);
        free(jControls);
    }

    if (numMuteControls) {
        void **jControls = calloc(numMuteControls, sizeof(void*));
        int j = 0;
        for (i = 0; i < mixer->numDeviceControls && j < numMuteControls; i++) {
            PortControl *control = &mixer->deviceControls[i];

            if (control->class != kAudioMuteControlClassID || control->channel == 0)
                continue;

            jControls[j++] = CreateMuteControl(creator, control);
        }

        void *compoundControl = creator->newCompoundControl(creator, "Mute", jControls, numMuteControls);
        creator->addControl(creator, compoundControl);
        free(jControls);
    }

exit:
    if (err) {
        ERROR1("PORT_GetControls err %.4s\n", err);
    }
    free(mixer->deviceControls);

    TRACE0("< PORT_GetControls\n");
}

INT32 PORT_GetIntValue(void* controlIDV) {
    TRACE0("> PORT_GetIntValue\n");

    return FALSE;
}

void PORT_SetIntValue(void* controlIDV, INT32 value) {
    TRACE1("> PORT_SetIntValue = %d\n", value);

}

float PORT_GetFloatValue(void* controlIDV) {
    TRACE0("> PORT_GetFloatValue\n");

    return 0;
}

void PORT_SetFloatValue(void* controlIDV, float value) {
    TRACE1("> PORT_SetFloatValue = %f\n", value);

}

#endif // USE_PORTS
