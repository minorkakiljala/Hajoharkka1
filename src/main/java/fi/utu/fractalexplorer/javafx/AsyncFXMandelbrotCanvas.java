package fi.utu.fractalexplorer.javafx;

import java.util.concurrent.LinkedBlockingQueue;
import fi.utu.fractalexplorer.renderers.RendererType;
import fi.utu.fractalexplorer.util.Viewport;
import javafx.scene.image.WritableImage;

public class AsyncFXMandelbrotCanvas extends FXMandelbrotCanvas {
    private final LinkedBlockingQueue<WritableImage> imageQueue = new LinkedBlockingQueue<WritableImage>(3);

    public AsyncFXMandelbrotCanvas(int maxIterations, RendererType rendererType) {
        super(maxIterations, rendererType);
    }

    private volatile boolean rendering = false;

    private boolean async = false;

    private volatile boolean needToDraw = true;

    private Object drawLock = new Object();

    @Override protected FXPixelRenderer generateBackend(int w, int h, int vectorSize) {
        synchronized (drawLock) {
            backendBuffer = new AsyncFXPixelRenderer(w, h, maxIterations, vectorSize, this.imageQueue);
        }
        return backendBuffer;
    }

    @Override
    protected void buildRenderer(int w, int h){
        synchronized (drawLock){
            super.buildRenderer(w, h);
        }
        needToDraw = true;
    }

    @Override
    protected void setViewport(Viewport vp){
        synchronized (drawLock){
            viewPort = vp;
        }
        needToDraw = true;
    }

    @Override
    public void setRendererType(RendererType r){
        async = r == RendererType.ThreadedWorkQueue;
        super.setRendererType(r);
    }

    private void renderToQueue(){
        if (renderer != null) {
            rendering = true;
            synchronized (drawLock) {
                FXPixelRenderer r = backendBuffer;
                renderer.drawSet(viewPort);
                r.draw(null);
            }
            rendering = false;
            needToDraw = false;
        }
    }

    private void prepareDraw(){
        calculateDrawParameters();
        if(!needToDraw) return;
        if(rendering){
            System.out.println("Frame skipped");
            return;
        }
        if(async)
            new Thread(this::renderToQueue).start();
        else
            renderToQueue();
    }

    @ Override public void redraw(){
        prepareDraw();
        WritableImage buffer = imageQueue.poll();
        if (buffer != null) {
            peer.getGraphicsContext2D().drawImage(buffer, 0.0, 0.0);
        }
    }
}
