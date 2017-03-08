package nesto.camera;

import android.app.Activity;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;

/**
 * Created on 2017/3/8.
 * By nesto
 */

public class PreviewAutoFullScreenListener implements OnPreviewSizeChangeListener {

    private boolean isPortrait;
    private CameraPreview cameraPreview;
    private int viewWidth;
    private int viewHeight;

    public PreviewAutoFullScreenListener(Activity activity, CameraPreview cameraPreview) {
        Point point = CameraHelper.getRealScreenSize(activity);
        isPortrait = point.x < point.y;
        viewWidth = Math.min(point.x, point.y);
        viewHeight = Math.max(point.x, point.y);
        this.cameraPreview = cameraPreview;

        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
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
