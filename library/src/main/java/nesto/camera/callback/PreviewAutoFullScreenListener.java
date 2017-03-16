package nesto.camera.callback;

import android.app.Activity;
import android.graphics.Point;
import android.view.ViewGroup;

import nesto.camera.util.CameraHelper;
import nesto.camera.util.Print;
import nesto.camera.view.CameraPreview;

/**
 * Created on 2017/3/8.
 * By nesto
 */

public class PreviewAutoFullScreenListener implements OnPreviewSizeChangeListener {

    private boolean isPortrait;
    private CameraPreview cameraPreview;
    private int viewSmallerSide;
    private int viewBiggerSide;

    public PreviewAutoFullScreenListener(Activity activity, CameraPreview cameraPreview) {
        Point point = CameraHelper.getRealScreenSize(activity);
        isPortrait = point.x < point.y;
        viewSmallerSide = Math.min(point.x, point.y);
        viewBiggerSide = Math.max(point.x, point.y);
        this.cameraPreview = cameraPreview;
    }

    public PreviewAutoFullScreenListener(Activity activity, CameraPreview cameraPreview,
                                         int smaller, int bigger) {
        this(activity, cameraPreview);
        viewSmallerSide = Math.min(smaller, bigger);
        viewBiggerSide = Math.max(smaller, bigger);
    }

    @Override public void onPreviewSizeChange(int width, int height) {
        ViewGroup.LayoutParams params = cameraPreview.getLayoutParams();
        int bigger = Math.max(width, height);
        int smaller = Math.min(width, height);
        float biggerRatio = 1.f * viewBiggerSide / bigger;
        float smallerRatio = 1.f * viewSmallerSide / smaller;

        int realWidth;
        int realHeight;
        if (biggerRatio > smallerRatio) {
            realHeight = viewBiggerSide;
            realWidth = realHeight * smaller / bigger;
        } else {
            realWidth = viewSmallerSide;
            realHeight = realWidth * bigger / smaller;
        }

        params.width = isPortrait ? realWidth : realHeight;
        params.height = isPortrait ? realHeight : realWidth;

        Print.log("view width " + params.width + " height " + params.height);
        cameraPreview.requestLayout();
    }
}
