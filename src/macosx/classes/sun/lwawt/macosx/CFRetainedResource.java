package sun.lwawt.macosx;

/**
 * Safely holds and disposes of native AppKit resources, using the
 * correct AppKit threading and Objective-C GC semantics.
 */
public class CFRetainedResource {
	private static native void nativeCFRelease(final long ptr, final boolean disposeOnAppKitThread);
	
	final boolean disposeOnAppKitThread;
	protected long ptr;
	
	/**
	 * @param ptr CFRetained native object pointer
	 * @param disposeOnAppKitThread is the object needs to be CFReleased on the main thread
	 */
	protected CFRetainedResource(final long ptr, final boolean disposeOnAppKitThread) {
	    this.disposeOnAppKitThread = disposeOnAppKitThread;
	    this.ptr = ptr;
	}
	
	/**
	 * CFReleases previous resource and assigns new pre-retained resource.
	 * @param ptr CFRetained native object pointer
	 */
	protected void setPtr(final long ptr) {
	    synchronized (this) {
	        if (this.ptr != 0) dispose();
	        this.ptr = ptr;
        }
	}
	
	/**
	 * Manually CFReleases the native resource
	 */
	protected void dispose() {
	    long oldPtr = 0L;
	    synchronized (this) {
	        if (ptr == 0) return;
	        oldPtr = ptr;
	        ptr = 0;
        }
		
	    nativeCFRelease(oldPtr, disposeOnAppKitThread); // perform outside of the synchronized block
	}
	
	@Override
	protected void finalize() throws Throwable {
		dispose();
	}
}