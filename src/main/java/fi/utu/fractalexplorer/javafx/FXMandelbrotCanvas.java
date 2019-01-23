package fi.utu.fractalexplorer.javafx;

import fi.utu.fractalexplorer.MandelbrotCanvas;
import fi.utu.fractalexplorer.renderers.RendererType;
import fi.utu.fractalexplorer.util.Viewport;
import javafx.scene.canvas.Canvas;

/**
 * You need to consider if this needs to be changed in order to support all rendering modes.
 */
public class FXMandelbrotCanvas extends MandelbrotCanvas {
    public final Canvas peer = new Canvas();
    private double oldX, oldY;
    private boolean pressed;
    protected FXPixelRenderer backendBuffer;

    public FXMandelbrotCanvas(int maxIterations, RendererType rendererType) {
        this.maxIterations = maxIterations;
        this.rendererType = rendererType;
        initControls();
    }

    protected void setViewport(Viewport vp){
        viewPort = vp;
    }

    void initControls() {
        peer.setOnScroll(e -> {
            if (e.getDeltaY() > 0)
                setViewport(viewPort.zoom(0.95));
            if (e.getDeltaY() < 0)
                setViewport(viewPort.zoom(1.05));

            if (e.getDeltaX() > 0)
                setViewport(viewPort.rotate(Math.PI / 4));
            if (e.getDeltaX() < 0)
                setViewport(viewPort.rotate(-Math.PI / 4));
        });

        peer.setOnMousePressed(e -> pressed = true);
        peer.setOnMouseReleased(e -> pressed = false);
        peer.setOnMouseDragged(e -> {
            oldX = e.getX();
            oldY = e.getY();
        });
    }

    @Override protected FXPixelRenderer generateBackend(int w, int h, int vectorSize) {
        backendBuffer = new FXPixelRenderer(w, h, maxIterations, vectorSize);
        return backendBuffer;
    }

    int getWidth() {
        return (int)peer.getWidth();
    }

    int getHeight() {
        return (int)peer.getHeight();
    }

    protected void calculateDrawParameters(){
        int w2 = getWidth(), h2 = getHeight();
        if (w2 > 0 && h2 > 0) {
            if (renderer == null || renderer.renderWidth() != w2 || renderer.renderHeight() != h2) {
                buildRenderer(w2, h2);
            }
            if (pressed) setViewport(viewPort.translate(oldX / getWidth() - 0.5, oldY / getHeight() - 0.5));
        }
    }

    @Override public void redraw() {
        calculateDrawParameters();
        if (renderer != null) {
            renderer.drawSet(viewPort);
            backendBuffer.draw(peer.getGraphicsContext2D());
        }
    }
}