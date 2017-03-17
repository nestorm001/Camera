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

    private boolean isScreenPortrait;
    private CameraPreview cameraPreview;
    private int viewWidth;
    private int viewHeight;

    public PreviewAutoFullScreenListener(Activity activity, CameraPreview cameraPreview) {
        Point point = CameraHelper.getRealScreenSize(activity);
        isScreenPortrait = point.x < point.y;
        viewWidth = point.x;
        viewHeight = point.y;
        this.cameraPreview = cameraPreview;
    }

    public PreviewAutoFullScreenListener(Activity activity, CameraPreview cameraPreview,
                                         int width, int height) {
        this(activity, cameraPreview);
        viewWidth = width;
        viewHeight = height;
    }

    @Override public void onPreviewSizeChange(int width, int height) {
        ViewGroup.LayoutParams params = cameraPreview.getLayoutParams();
        if (isScreenPortrait) {
            int temp = height;
            //noinspection SuspiciousNameCombination
            height = width;
            width = temp;
        }
        float heightRatio = 1.f * viewHeight / height;
        float widthRatio = 1.f * viewWidth / width;

        if (heightRatio > widthRatio) {
            params.height = viewHeight;
            params.width = params.height * width / height;
        } else {
            params.width = viewWidth;
            params.height = params.width * height / width;
        }

        Print.log("view width " + params.width + " height " + params.height);
        cameraPreview.requestLayout();
    }
}
