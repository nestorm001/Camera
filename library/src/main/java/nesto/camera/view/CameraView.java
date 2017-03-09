package nesto.camera.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created on 2017/3/8.
 * By nesto
 */

public class CameraView extends RelativeLayout {
    private CameraPreview cameraPreview;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        cameraPreview = new CameraPreview(context);
        this.addView(cameraPreview);
    }
}
