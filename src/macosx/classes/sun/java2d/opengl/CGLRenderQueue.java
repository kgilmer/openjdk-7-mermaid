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
        synchronized (this) {
            if (currentSurfaceData != null) {
                RenderBuffer renderBuffer = RenderBuffer.allocate(limit);
                Unsafe unsafe = Unsafe.getUnsafe();
                unsafe.copyMemory(buf, renderBuffer.getAddress(), limit);                                            
                
                currentSurfaceData.setNeedsDisplay(renderBuffer);
            } else {
                super.flushBuffer(buf, limit);                
            }
        }
    }

    @Override
    protected void invokeTask(Runnable task) {
        if (currentSurfaceData != null) {
            currentSurfaceData.setNeedsDisplay(task);
        } else {
            super.invokeTask(task);            
        }
    }

    void drawLayer(RenderBuffer renderBuffer) {        
        super.flushBuffer(renderBuffer.getAddress(), renderBuffer.capacity());
    }

    void invokeTaskNow(Runnable task) {
        super.invokeTask(task);
    }
}
