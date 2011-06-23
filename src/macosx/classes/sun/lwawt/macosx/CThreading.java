package sun.lwawt.macosx;

import java.awt.EventQueue;


public class CThreading {
    static String APPKIT_THREAD_NAME = "AppKit Thread";
    
    static boolean isEventQueue() {
        return EventQueue.isDispatchThread();
    }
    
    static boolean isAppKit() {
        return APPKIT_THREAD_NAME.equals(Thread.currentThread().getName());
    }
    
    static boolean assertEventQueue() {
        final boolean isEventQueue = isEventQueue();
        assert isEventQueue : "Threading violation: not EventQueue thread";
        return isEventQueue;
    }
    
    static boolean assertNotEventQueue() {
        final boolean isNotEventQueue = isEventQueue();
        assert isNotEventQueue : "Threading violation: EventQueue thread";
        return isNotEventQueue;
    }
    
    static boolean assertAppKit() {
        final boolean isAppKitThread = isAppKit();
        assert isAppKitThread : "Threading violation: not AppKit thread";
        return isAppKitThread;
    }
    
    static boolean assertNotAppKit() {
        final boolean isNotAppKitThread = !isAppKit();
        assert isNotAppKitThread : "Threading violation: AppKit thread";
        return isNotAppKitThread;
    }
}
