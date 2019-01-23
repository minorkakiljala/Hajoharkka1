package fi.utu.fractalexplorer.renderers;

import fi.utu.fractalexplorer.util.Viewport;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * You need to implement this!
 */
public interface MandelbrotDynamicParallelRenderer extends MandelbrotRenderer {
    default void drawSet(Viewport vp) {
        class DynamicThread implements Runnable {

            int tx;
            int ty;
            int width;
            int heigth;
            Viewport vp;

            public DynamicThread(int tx, int ty, int width, int heigth, Viewport vp) {
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

        ForkJoinPool f = new ForkJoinPool(process);



        for (int i = 0; i < process; i++) {
            for (int j = 0; j < process; j++) {
                int w = renderWidth() / process;
                int x = j * w;
                int h = renderHeight() / process;
                int y = i * h;


            f.execute(new DynamicThread(x, y, w, h, vp));


        }
    }
    f.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!f.awaitTermination(60, TimeUnit.SECONDS)) {
                f.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!f.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            f.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }



}
}
