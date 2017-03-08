package nesto.camera;

import android.app.Activity;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;

/**
 * Created on 2017/3/8.
 * By nesto
 */

public class PreviewAutoFullScreenListener implements OnPreviewSizeChangeListener {

    private boolean isPortrait;
    private CameraPreview cameraPreview;
    private int viewWidth = CameraHelper.SCREEN_WIDTH;
    private int viewHeight = CameraHelper.SCREEN_HEIGHT;

    public PreviewAutoFullScreenListener(Activity activity, CameraPreview cameraPreview) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        isPortrait = point.x < point.y;
        this.cameraPreview = cameraPreview;
    }

    public PreviewAutoFullScreenListener(Activity activity, CameraPreview cameraPreview,
                                         int smaller, int bigger) {
        this(activity, cameraPreview);
        viewWidth = smaller;
        viewHeight = bigger;
    }

    @Override public void onPreviewSizeChange(int width, int height) {
        ViewGroup.LayoutParams params = cameraPreview.getLayoutParams();
        int bigger = Math.max(width, height);
        int smaller = Math.min(width, height);
        float biggerRatio = 1.f * viewHeight / bigger;
        float smallerRatio = 1.f * viewWidth / smaller;

        int realWidth;
        int realHeight;
        if (biggerRatio > smallerRatio) {
            realHeight = viewHeight;
            realWidth = realHeight * smaller / bigger;
        } else {
            realWidth = viewWidth;
            realHeight = realWidth * bigger / smaller;
        }

        params.width = isPortrait ? realWidth : realHeight;
        params.height = isPortrait ? realHeight : realWidth;

        cameraPreview.setLayoutParams(params);
    }
}
