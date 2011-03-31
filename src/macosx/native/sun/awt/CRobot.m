/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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

// Directly reading the framebuffer via CGDisplayAddressForPosition()
// is discouraged, and may not work well for all pointer sizes on some machines.
// CoreGraphics recommends using a more abstract yet faster mechanisms to
// read framebuffer content.  The use of OpenGL to read back pixels is
// generally a good idea.

#import <Carbon/Carbon.h>
#import <JavaNativeFoundation/JavaNativeFoundation.h>
#import <ApplicationServices/ApplicationServices.h>
#import <OpenGL/OpenGL.h>
#import <OpenGL/gl.h>
#import <OpenGL/glu.h>

#import "LWCToolkit.h"

#import "sun_lwawt_macosx_CRobot.h"

// Theoretically, Quarts works with up to 32 buttons.
#define k_JAVA_ROBOT_NUMBER_KNOWN_BUTTONS    3
#define k_JAVA_ROBOT_WHEEL_COUNT 1

#if defined(_LITTLE_ENDIAN__)
#define PIXEL_DATA_TYPE GL_UNSIGNED_INT_8_8_8_8
#else
#define PIXEL_DATA_TYPE GL_UNSIGNED_INT_8_8_8_8_REV
#endif

#if !defined(kCGBitmapByteOrder32Host)
#define kCGBitmapByteOrder32Host 0
#endif

// Data struct for the screen grabs via CoreGraphics
struct screenDataProviderData 
{
    unsigned char*  base_addr;      /* READ ONLY */
    size_t          numRows;        /* READ ONLY */
    size_t          bytesPerRow;    /* READ ONLY */
    unsigned char*  max;            /* READ ONLY */
    
    size_t          row;            /* current row */
    unsigned char*  offset;         /* current raw pointer */
};
typedef struct screenDataProviderData screenDataProviderData;

static inline CGKeyCode GetCGKeyCode(jint javaKeyCode);

static void SwizzleBitmap(void* data, int rowBytes, int height);
static void cleanContext(CGLContextObj glContextObj);
static CGLContextObj InitContext(CGDirectDisplayID display);


static void
CreateJavaException(JNIEnv* env, CGError err)
{
    // Throw a java exception indicating what is wrong.
    NSString* s = [NSString stringWithFormat:@"Robot: CGError: %d", err];
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/awt/AWTException"),
                     [s UTF8String]);
}

/*
 * Class:     sun_lwawt_macosx_CRobot
 * Method:    initRobot
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CRobot_initRobot
    (JNIEnv *env, jobject peer)
{
    // Set things up to let our app act like a synthetic keyboard and mouse.
    // Always set all states, in case Apple ever changes default behaviors.
    static int setupDone = 0;
    if (!setupDone) {
        setupDone = 1;
        // Don't block local events after posting ours
        CGSetLocalEventsSuppressionInterval(0.0);
        
        // Let our event's modifier key state blend with local hardware events
        CGEnableEventStateCombining(TRUE);
        
        // Don't let our events block local hardware events
        CGSetLocalEventsFilterDuringSupressionState(kCGEventFilterMaskPermitAllEvents,
                                                    kCGEventSupressionStateSupressionInterval);
        CGSetLocalEventsFilterDuringSupressionState(kCGEventFilterMaskPermitAllEvents,
                                                    kCGEventSupressionStateRemoteMouseDrag);
    }
}    
/*
 * Class:     sun_lwawt_macosx_CRobot
 * Method:    mouseEvent
 * Signature: (Lapple/awt/CGraphicsDevice;IIIIIZ)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CRobot_mouseEvent
    (JNIEnv *env, jobject peer,
     jint screenIndex, jint mouseLastX, jint mouseLastY,
     jint mouse1DesiredState, jint mouse2DesiredState,
     jint mouse3DesiredState, jboolean mouseMoveAction)
{
    JNF_COCOA_ENTER(env);

    // This is the native method called when Robot mouse events occur.
    // The CRobot tracks the mouse position, and which button was
    // pressed. If the mouse position is unknown it is obtained from
    // CGEvents. The peer also tracks the mouse button desired state,
    // the appropriate key modifier state, and whether the mouse action
    // is simply a mouse move with no mouse button state changes.

    CGError err = kCGErrorSuccess;
    
    CGDirectDisplayID displayID =
        FindCGDirectDisplayIDForScreenIndex(screenIndex);
    CGRect globalDeviceBounds = CGDisplayBounds(displayID);

    // Set unknown mouse location, if needed.
    if ((mouseLastX == sun_lwawt_macosx_CRobot_MOUSE_LOCATION_UNKNOWN) ||
        (mouseLastY == sun_lwawt_macosx_CRobot_MOUSE_LOCATION_UNKNOWN))
    {
//TODO: use Cocoa API instead.
        HIPoint globalPos;
        HIGetMousePosition(kHICoordSpaceScreenPixel, NULL, &globalPos);

        // Normalize the coords within this display device, as
        // per Robot rules.        
        if (globalPos.x < CGRectGetMinX(globalDeviceBounds)) {
            globalPos.x = CGRectGetMinX(globalDeviceBounds);
        }
        else if (globalPos.x > CGRectGetMaxX(globalDeviceBounds)) {
            globalPos.x = CGRectGetMaxX(globalDeviceBounds);
        }
        
        if (globalPos.y < CGRectGetMinY(globalDeviceBounds)) {
            globalPos.y = CGRectGetMinY(globalDeviceBounds);
        }
        else if (globalPos.y > CGRectGetMaxY(globalDeviceBounds)) {
            globalPos.y = CGRectGetMaxY(globalDeviceBounds);
        }

        mouseLastX = (jint)globalPos.x;
        mouseLastY = (jint)globalPos.y;
    }

//volatile, otherwise it warns that variable might be clobbered by 'longjmp' or 'vfork'
    volatile CGPoint point;
//Translate the device relative point into a valid global CGPoint.
    point.x = mouseLastX + globalDeviceBounds.origin.x;
    point.y = mouseLastY + globalDeviceBounds.origin.y;

    BOOL button1, button2, button3;

    UInt32 bstate = GetCurrentButtonState();
    button1 = ((bstate & (1 << 0)) != 0); // left
    button2 = ((bstate & (1 << 1)) != 0); // right
    button3 = ((bstate & (1 << 2)) != 0); // other

    // When moving, the buttons aren't changed from their current state.
    if (mouseMoveAction == JNI_FALSE) {
        // Some of the buttons are changing state.

        // Left
        if (mouse1DesiredState != sun_lwawt_macosx_CRobot_BUTTON_STATE_UNKNOWN) {
            button1 = (mouse1DesiredState == sun_lwawt_macosx_CRobot_BUTTON_STATE_DOWN);
        }

        // Other
        if (mouse2DesiredState != sun_lwawt_macosx_CRobot_BUTTON_STATE_UNKNOWN) {
            button2 = (mouse2DesiredState == sun_lwawt_macosx_CRobot_BUTTON_STATE_DOWN);
        }

        // Right
        if (mouse3DesiredState != sun_lwawt_macosx_CRobot_BUTTON_STATE_UNKNOWN) {
            button3 = (mouse3DesiredState == sun_lwawt_macosx_CRobot_BUTTON_STATE_DOWN);
        }
    }

    // CGPostMouseEvent takes left, right, other.
    err = CGPostMouseEvent(point, true, k_JAVA_ROBOT_NUMBER_KNOWN_BUTTONS,
                           button1, button2, button3);
    if (err != kCGErrorSuccess) {
        CreateJavaException(env, err);
    }

    JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CRobot
 * Method:    mouseWheel
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CRobot_mouseWheel
    (JNIEnv *env, jobject peer, jint wheelAmt)
{
    CGError err = CGPostScrollWheelEvent(k_JAVA_ROBOT_WHEEL_COUNT, wheelAmt);
    if (err != kCGErrorSuccess) {
        CreateJavaException(env, err);
    }
}

/*
 * Class:     sun_lwawt_macosx_CRobot
 * Method:    keyEvent
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CRobot_keyEvent
    (JNIEnv *env, jobject peer, jint javaKeyCode, jboolean keyPressed)
{
    CGKeyCode keyCode = GetCGKeyCode(javaKeyCode);
    
    CGError err = CGPostKeyboardEvent(0, keyCode, keyPressed);
    if (err != kCGErrorSuccess) {
        CreateJavaException(env, err);
    }
}

/*
 * Class:     sun_lwawt_macosx_CRobot
 * Method:    getScreenPixels
 * Signature: (Lsun/awt/CGraphicsDevice;IIII[I)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CRobot_nativeGetScreenPixels
(JNIEnv *env, jobject peer,
 jint screenIndex, jint x, jint y, jint width, jint height, jintArray pixels)
{
    JNF_COCOA_ENTER(env);
  
    // The array of pixels has been set up to hold enough jint's for the pixels.
    // Also notice that the rect (bounds) and pixels (int array) passed from
    // the Java layer are in terms of points, not pixels.  We need to take
    // care of the HiDPI scenarios.
  
    jint screenX = x;
    jint screenY = y;
    jint screenWidth = width;
    jint screenHeight = height;
	
    jint actualX = 0;
    jint actualY = 0;
    jint actualWidth = 0;
    jint actualHeight = 0;
    jint* actualPixelData = NULL;
	
    CGDirectDisplayID displayID =
	FindCGDirectDisplayIDForScreenIndex(screenIndex);
    
    CGLContextObj glContextObj = InitContext(displayID);
    if (glContextObj == NULL) {
        return;
    }
	
    jint displayHeight = (jint) CGDisplayPixelsHigh(displayID);
	
    glReadBuffer(GL_FRONT);
	
    // Get a handle on the native int array.
    void* jPixelData = (*env)->GetPrimitiveArrayCritical(env, pixels, 0);
	
    // Finish all OpenGL commands.
    glFinish();
	
    // Read framebuffer into our bitmap.
    // Calculates the window coordinates of the lower left corner corresponding
    // to the Java rectangle.
	
    glReadPixels(screenX, displayHeight - screenY - screenHeight,
                 screenWidth, screenHeight,
                 GL_BGRA,
                 PIXEL_DATA_TYPE,
                 jPixelData);
	
    // Invert the pixels for Java's sake.
    SwizzleBitmap(jPixelData, screenWidth * sizeof(jint), screenHeight);
	
    cleanContext(glContextObj);
	
    // Get our pixels
    (*env)->ReleasePrimitiveArrayCritical(env, pixels, jPixelData, 0);
	
    JNF_COCOA_EXIT(env);
}

/****************************************************
 * Helper methods
 ****************************************************/

// Called from getScreenPixels JNI impl.
static void
SwizzleBitmap(void* data, int rowBytes, int height)
{
    int top, bottom;
    void* buffer;
    void* topP;
    void* bottomP;
    void* base;

    top = 0;
    bottom = height - 1;
    base = data;
    buffer = malloc(rowBytes);

    while (top < bottom) {
        topP = (void *)((top * rowBytes) + (intptr_t)base);
        bottomP = (void *)((bottom * rowBytes) + (intptr_t)base);

        /*
         * Save and swap scanlines.
         *
         * This code does a simple in-place exchange with a temp buffer.
         * If you need to reformat the pixels, replace the first two bcopy()
         * calls with your own custom pixel reformatter.
         */
        bcopy( topP, buffer, rowBytes );
        bcopy( bottomP, topP, rowBytes );
        bcopy( buffer, bottomP, rowBytes );

        ++top;
        --bottom;
    }
    free( buffer );
}

// Called from getScreenPixels JNI impl.
static CGLContextObj
InitContext(CGDirectDisplayID display)
{
    CGLContextObj glContextObj;
    CGLPixelFormatObj pixelFormatObj;
    GLint numPixelFormats;
    
    CGLPixelFormatAttribute attribs[] = {
        kCGLPFAFullScreen,
        kCGLPFADisplayMask,
        CGDisplayIDToOpenGLDisplayMask(display),
        0
    };
    
    CGLChoosePixelFormat(attribs, &pixelFormatObj, &numPixelFormats);
    if (pixelFormatObj == NULL) {
        printf("No full screen pixel format\n");
        return 0;
    }

    CGLCreateContext(pixelFormatObj, NULL, &glContextObj);
    CGLDestroyPixelFormat(pixelFormatObj);
    
    if (glContextObj == NULL) {
        printf("Unable to create Full Screen context\n");
        return 0;
    }
    
    CGLSetCurrentContext(glContextObj);
    CGLSetFullScreen(glContextObj);
    
    return glContextObj;
}

// Called from getScreenPixels JNI impl.
static
void cleanContext(CGLContextObj glContextObj)
{
    CGLSetCurrentContext(0);
    CGLClearDrawable(glContextObj);  // disassociate from full screen
    CGLDestroyContext(glContextObj); // and destroy the context
}

// NOTE: Don't modify this table directly. It is machine generated. See below.
static const unsigned char javaToMacKeyCode[] = {
    127,    //     0     0 VK_UNDEFINED                      No_Equivalent
    127,    //     1   0x1 Not_Used                      
    127,    //     2   0x2 Not_Used                      
    127,    //     3   0x3 VK_CANCEL                         No_Equivalent
    127,    //     4   0x4 Not_Used                      
    127,    //     5   0x5 Not_Used                      
    127,    //     6   0x6 Not_Used                      
    127,    //     7   0x7 Not_Used                      
     51,    //     8   0x8 VK_BACK_SPACE                 
     48,    //     9   0x9 VK_TAB                        
     36,    //    10   0xa VK_ENTER                      
    127,    //    11   0xb Not_Used                      
     71,    //    12   0xc VK_CLEAR                      
    127,    //    13   0xd Not_Used                      
    127,    //    14   0xe Not_Used                      
    127,    //    15   0xf Not_Used                      
     56,    //    16  0x10 VK_SHIFT                      
     59,    //    17  0x11 VK_CONTROL                    
     58,    //    18  0x12 VK_ALT                        
    113,    //    19  0x13 VK_PAUSE                      
     57,    //    20  0x14 VK_CAPS_LOCK                  
    127,    //    21  0x15 VK_KANA                           No_Equivalent
    127,    //    22  0x16 Not_Used                      
    127,    //    23  0x17 Not_Used                      
    127,    //    24  0x18 VK_FINAL                          No_Equivalent
    127,    //    25  0x19 VK_KANJI                          No_Equivalent
    127,    //    26  0x1a Not_Used                      
     53,    //    27  0x1b VK_ESCAPE                     
    127,    //    28  0x1c VK_CONVERT                        No_Equivalent
    127,    //    29  0x1d VK_NONCONVERT                     No_Equivalent
    127,    //    30  0x1e VK_ACCEPT                         No_Equivalent
    127,    //    31  0x1f VK_MODECHANGE                     No_Equivalent
     49,    //    32  0x20 VK_SPACE                      
    116,    //    33  0x21 VK_PAGE_UP                    
    121,    //    34  0x22 VK_PAGE_DOWN                  
    119,    //    35  0x23 VK_END                        
    115,    //    36  0x24 VK_HOME                       
    123,    //    37  0x25 VK_LEFT                       
    126,    //    38  0x26 VK_UP                         
    124,    //    39  0x27 VK_RIGHT                      
    125,    //    40  0x28 VK_DOWN                       
    127,    //    41  0x29 Not_Used                      
    127,    //    42  0x2a Not_Used                      
    127,    //    43  0x2b Not_Used                      
     43,    //    44  0x2c VK_COMMA                      
     27,    //    45  0x2d VK_MINUS                      
     47,    //    46  0x2e VK_PERIOD                     
     44,    //    47  0x2f VK_SLASH                      
     29,    //    48  0x30 VK_0                          
     18,    //    49  0x31 VK_1                          
     19,    //    50  0x32 VK_2                          
     20,    //    51  0x33 VK_3                          
     21,    //    52  0x34 VK_4                          
     23,    //    53  0x35 VK_5                          
     22,    //    54  0x36 VK_6                          
     26,    //    55  0x37 VK_7                          
     28,    //    56  0x38 VK_8                          
     25,    //    57  0x39 VK_9                          
    127,    //    58  0x3a Not_Used                      
     41,    //    59  0x3b VK_SEMICOLON                  
    127,    //    60  0x3c Not_Used                      
     24,    //    61  0x3d VK_EQUALS                     
    127,    //    62  0x3e Not_Used                      
    127,    //    63  0x3f Not_Used                      
    127,    //    64  0x40 Not_Used                      
      0,    //    65  0x41 VK_A                          
     11,    //    66  0x42 VK_B                          
      8,    //    67  0x43 VK_C                          
      2,    //    68  0x44 VK_D                          
     14,    //    69  0x45 VK_E                          
      3,    //    70  0x46 VK_F                          
      5,    //    71  0x47 VK_G                          
      4,    //    72  0x48 VK_H                          
     34,    //    73  0x49 VK_I                          
     38,    //    74  0x4a VK_J                          
     40,    //    75  0x4b VK_K                          
     37,    //    76  0x4c VK_L                          
     46,    //    77  0x4d VK_M                          
     45,    //    78  0x4e VK_N                          
     31,    //    79  0x4f VK_O                          
     35,    //    80  0x50 VK_P                          
     12,    //    81  0x51 VK_Q                          
     15,    //    82  0x52 VK_R                          
      1,    //    83  0x53 VK_S                          
     17,    //    84  0x54 VK_T                          
     32,    //    85  0x55 VK_U                          
      9,    //    86  0x56 VK_V                          
     13,    //    87  0x57 VK_W                          
      7,    //    88  0x58 VK_X                          
     16,    //    89  0x59 VK_Y                          
      6,    //    90  0x5a VK_Z                          
     33,    //    91  0x5b VK_OPEN_BRACKET               
     42,    //    92  0x5c VK_BACK_SLASH                 
     30,    //    93  0x5d VK_CLOSE_BRACKET              
    127,    //    94  0x5e Not_Used                      
    127,    //    95  0x5f Not_Used                      
     82,    //    96  0x60 VK_NUMPAD0                    
     83,    //    97  0x61 VK_NUMPAD1                    
     84,    //    98  0x62 VK_NUMPAD2                    
     85,    //    99  0x63 VK_NUMPAD3                    
     86,    //   100  0x64 VK_NUMPAD4                    
     87,    //   101  0x65 VK_NUMPAD5                    
     88,    //   102  0x66 VK_NUMPAD6                    
     89,    //   103  0x67 VK_NUMPAD7                    
     91,    //   104  0x68 VK_NUMPAD8                    
     92,    //   105  0x69 VK_NUMPAD9                    
     67,    //   106  0x6a VK_MULTIPLY                   
     69,    //   107  0x6b VK_ADD                        
    127,    //   108  0x6c VK_SEPARATER                      No_Equivalent
     78,    //   109  0x6d VK_SUBTRACT                   
     65,    //   110  0x6e VK_DECIMAL                    
     75,    //   111  0x6f VK_DIVIDE                     
    122,    //   112  0x70 VK_F1                         
    120,    //   113  0x71 VK_F2                         
     99,    //   114  0x72 VK_F3                         
    118,    //   115  0x73 VK_F4                         
     96,    //   116  0x74 VK_F5                         
     97,    //   117  0x75 VK_F6                         
     98,    //   118  0x76 VK_F7                         
    100,    //   119  0x77 VK_F8                         
    101,    //   120  0x78 VK_F9                         
    109,    //   121  0x79 VK_F10                        
    103,    //   122  0x7a VK_F11                        
    111,    //   123  0x7b VK_F12                        
    127,    //   124  0x7c Not_Used                      
    127,    //   125  0x7d Not_Used                      
    127,    //   126  0x7e Not_Used                      
    117,    //   127  0x7f VK_DELETE                     
    127,    //   128  0x80 VK_DEAD_GRAVE                     No_Equivalent
    127,    //   129  0x81 VK_DEAD_ACUTE                     No_Equivalent
    127,    //   130  0x82 VK_DEAD_CIRCUMFLEX                No_Equivalent
    127,    //   131  0x83 VK_DEAD_TILDE                     No_Equivalent
    127,    //   132  0x84 VK_DEAD_MACRON                    No_Equivalent
    127,    //   133  0x85 VK_DEAD_BREVE                     No_Equivalent
    127,    //   134  0x86 VK_DEAD_ABOVEDOT                  No_Equivalent
    127,    //   135  0x87 VK_DEAD_DIAERESIS                 No_Equivalent
    127,    //   136  0x88 VK_DEAD_ABOVERING                 No_Equivalent
    127,    //   137  0x89 VK_DEAD_DOUBLEACUTE               No_Equivalent
    127,    //   138  0x8a VK_DEAD_CARON                     No_Equivalent
    127,    //   139  0x8b VK_DEAD_CEDILLA                   No_Equivalent
    127,    //   140  0x8c VK_DEAD_OGONEK                    No_Equivalent
    127,    //   141  0x8d VK_DEAD_IOTA                      No_Equivalent
    127,    //   142  0x8e VK_DEAD_VOICED_SOUND              No_Equivalent
    127,    //   143  0x8f VK_DEAD_SEMIVOICED_SOUND          No_Equivalent
    127,    //   144  0x90 VK_NUM_LOCK                       No_Equivalent
    107,    //   145  0x91 VK_SCROLL_LOCK                
    127,    //   146  0x92 Not_Used                      
    127,    //   147  0x93 Not_Used                      
    127,    //   148  0x94 Not_Used                      
    127,    //   149  0x95 Not_Used                      
    127,    //   150  0x96 VK_AMPERSAND                      No_Equivalent
    127,    //   151  0x97 VK_ASTERISK                       No_Equivalent
    127,    //   152  0x98 VK_QUOTEDBL                       No_Equivalent
    127,    //   153  0x99 VK_LESS                           No_Equivalent
    105,    //   154  0x9a VK_PRINTSCREEN                
    127,    //   155  0x9b VK_INSERT                         No_Equivalent
    114,    //   156  0x9c VK_HELP                       
     55,    //   157  0x9d VK_META                       
    127,    //   158  0x9e Not_Used                      
    127,    //   159  0x9f Not_Used                      
    127,    //   160  0xa0 VK_GREATER                        No_Equivalent
    127,    //   161  0xa1 VK_BRACELEFT                      No_Equivalent
    127,    //   162  0xa2 VK_BRACERIGHT                     No_Equivalent
    127,    //   163  0xa3 Not_Used                      
    127,    //   164  0xa4 Not_Used                      
    127,    //   165  0xa5 Not_Used                      
    127,    //   166  0xa6 Not_Used                      
    127,    //   167  0xa7 Not_Used                      
    127,    //   168  0xa8 Not_Used                      
    127,    //   169  0xa9 Not_Used                      
    127,    //   170  0xaa Not_Used                      
    127,    //   171  0xab Not_Used                      
    127,    //   172  0xac Not_Used                      
    127,    //   173  0xad Not_Used                      
    127,    //   174  0xae Not_Used                      
    127,    //   175  0xaf Not_Used                      
    127,    //   176  0xb0 Not_Used                      
    127,    //   177  0xb1 Not_Used                      
    127,    //   178  0xb2 Not_Used                      
    127,    //   179  0xb3 Not_Used                      
    127,    //   180  0xb4 Not_Used                      
    127,    //   181  0xb5 Not_Used                      
    127,    //   182  0xb6 Not_Used                      
    127,    //   183  0xb7 Not_Used                      
    127,    //   184  0xb8 Not_Used                      
    127,    //   185  0xb9 Not_Used                      
    127,    //   186  0xba Not_Used                      
    127,    //   187  0xbb Not_Used                      
    127,    //   188  0xbc Not_Used                      
    127,    //   189  0xbd Not_Used                      
    127,    //   190  0xbe Not_Used                      
    127,    //   191  0xbf Not_Used                      
     50,    //   192  0xc0 VK_BACK_QUOTE                 
    127,    //   193  0xc1 Not_Used                      
    127,    //   194  0xc2 Not_Used                      
    127,    //   195  0xc3 Not_Used                      
    127,    //   196  0xc4 Not_Used                      
    127,    //   197  0xc5 Not_Used                      
    127,    //   198  0xc6 Not_Used                      
    127,    //   199  0xc7 Not_Used                      
    127,    //   200  0xc8 Not_Used                      
    127,    //   201  0xc9 Not_Used                      
    127,    //   202  0xca Not_Used                      
    127,    //   203  0xcb Not_Used                      
    127,    //   204  0xcc Not_Used                      
    127,    //   205  0xcd Not_Used                      
    127,    //   206  0xce Not_Used                      
    127,    //   207  0xcf Not_Used                      
    127,    //   208  0xd0 Not_Used                      
    127,    //   209  0xd1 Not_Used                      
    127,    //   210  0xd2 Not_Used                      
    127,    //   211  0xd3 Not_Used                      
    127,    //   212  0xd4 Not_Used                      
    127,    //   213  0xd5 Not_Used                      
    127,    //   214  0xd6 Not_Used                      
    127,    //   215  0xd7 Not_Used                      
    127,    //   216  0xd8 Not_Used                      
    127,    //   217  0xd9 Not_Used                      
    127,    //   218  0xda Not_Used                      
    127,    //   219  0xdb Not_Used                      
    127,    //   220  0xdc Not_Used                      
    127,    //   221  0xdd Not_Used                      
     39     //   222  0xde VK_QUOTE                      
};

// NOTE: All values above 222 don't have an equivalent on MacOSX.
static inline CGKeyCode GetCGKeyCode(jint javaKeyCode)
{
    if (javaKeyCode > 222) {
        return 127;
    } else {
        return javaToMacKeyCode[javaKeyCode];
    }
}
