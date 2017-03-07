package nesto.camera;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created on 2017/3/7.
 * By nesto
 */

public class FocusHelper {
    private FocusHelper() {}

    public static Rect tapEventToFocusArea(MotionEvent event, boolean mirror,
                                           int displayOrientation, View view, float coefficient) {
        final float focusAreaSize = 200;
        float tapX = event.getX();
        float tapY = event.getY();
        Matrix matrix = new Matrix();
        RectF rect = new RectF(tapX - focusAreaSize * coefficient / 2,
                tapY - focusAreaSize * coefficient / 2,
                tapX + focusAreaSize * coefficient / 2,
                tapY + focusAreaSize * coefficient / 2);

        // Need mirror for front camera.  
        matrix.setScale(mirror ? -1 : 1, 1);

        // UI coordinates range from (0, 0) to (width, height).  
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).  
        matrix.postScale(2000f / view.getWidth(), 2000f / view.getHeight());
        matrix.postTranslate(-1000f, -1000f);

        // This is the value for android.hardware.Camera.setDisplayOrientation.  
        matrix.postRotate(-displayOrientation);

        matrix.mapRect(rect);
        return new Rect(clamp((int) rect.left, -1000, 1000),
                clamp((int) rect.top, -1000, 1000),
                clamp((int) rect.right, -1000, 1000),
                clamp((int) rect.bottom, -1000, 1000));
    }

    private static int clamp(int x, int min, int max) {
        return Math.min(min, Math.min(x, max));
    }
}
