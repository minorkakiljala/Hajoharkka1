package fi.utu.fractalexplorer.renderers;

import fi.utu.fractalexplorer.util.Viewport;


/**
 * You need to implement this!
 */
public interface MandelbrotStaticParallelRenderer extends MandelbrotRenderer {
    default void drawSet(Viewport vp) {


        class StaticThread implements Runnable {

            int tx;
            int ty;
            int width;
            int heigth;
            Viewport vp;

            public StaticThread(int tx, int ty, int width, int heigth, Viewport vp) {
                this.tx = tx;
                this.ty = ty;
                this.width = width;
                this.heigth = heigth;
                this.vp = vp;

            }


            public void run() {   //säikeen päästessä suoritukseen toteuttaa run metodin
                try{
                    MandelbrotRenderer mr = RendererFactory.createRenderer(RendererType.Vector, renderWidth(), renderHeight(), getMaxIterations(), pixelRenderer());
                    mr.drawTile(tx, ty, width, heigth, vp);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
        int process = Runtime.getRuntime().availableProcessors();
        Thread p[] = new Thread[process];
        int w = renderWidth();
        int h = renderHeight() / process;

        for (int i = 0; i < process; i++) {
            int y = i * h;
            p[i] = new Thread(new StaticThread(0, y, w, h, vp));
            new Thread(p[i]).start();
        }
        try {
            for (int i = 0; i < process; i++) {
                p[i].join();
            }
        } catch (Exception e) {

        }
    }
}
