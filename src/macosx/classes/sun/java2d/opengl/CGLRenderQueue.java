package sun.java2d.opengl;

import sun.java2d.SurfaceData;
import sun.java2d.opengl.CGLSurfaceData.CGLWindowSurfaceData;
import sun.java2d.pipe.RenderBuffer;

import java.util.List;
import java.util.ArrayList;

import sun.misc.Unsafe;

public class CGLRenderQueue extends OGLRenderQueue
{    
    private static CGLSurfaceData currentSurfaceData = null;
    
    public static void setCurrentSurfaceData(SurfaceData surfaceData) {
        currentSurfaceData = (CGLSurfaceData)surfaceData;
    }

    @Override
    protected void flushBuffer(long buf, int limit) {
        super.flushBuffer(buf, limit);                

        if (currentSurfaceData != null) {
            currentSurfaceData.setNeedsDisplay();
        }
    }

    @Override
    protected void invokeTask(Runnable task) {        
        super.invokeTask(task);
        
        if (currentSurfaceData != null) {
            currentSurfaceData.setNeedsDisplay();
        }
    }
}
