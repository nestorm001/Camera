package nesto.camera.demo;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.ByteArrayInputStream;

import nesto.camera.callback.OnChangeFinishListener;
import nesto.camera.callback.PreviewAutoFullScreenListener;
import nesto.camera.util.CameraHelper;
import nesto.camera.util.Print;
import nesto.camera.view.CameraPreview;

public class MainActivity extends BaseActivity implements OnChangeFinishListener {

    private CameraPreview cameraPreview;
    private Button switchFlash;
    private ViewGroup viewGroup;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Print.debug = true;
        switchFlash = (Button) findViewById(R.id.switchFlash);
        viewGroup = (ViewGroup) findViewById(R.id.preview_holder);
        cameraPreview = (CameraPreview) findViewById(R.id.holder);
        cameraPreview.setOnChangeFinishListener(this);
        cameraPreview.setDesiredSize(CameraHelper.getRealScreenSize(this));
        viewGroup.post(() -> cameraPreview.setOnPreviewSizeChangeListener(
//                new PreviewAutoFullScreenListener(this, cameraPreview)
                new PreviewAutoFullScreenListener(this, cameraPreview,
                        viewGroup.getWidth(), viewGroup.getHeight())
        ));

        cameraPreview.setOnPictureTakeListener((bytes, pictureRotation) -> {
            BitmapFactory.Options oBounds = new BitmapFactory.Options();
            oBounds.inJustDecodeBounds = false;
            BitmapFactory.decodeStream(new ByteArrayInputStream(bytes), null, oBounds);
            Log.d("wtf", "photo width " + oBounds.outWidth + " height " + oBounds.outHeight);
        });
    }

    @Override protected void onResume() {
        super.onResume();
        cameraPreview.openCamera();
    }

    @Override protected void onPause() {
        super.onPause();
        cameraPreview.stopCamera();
    }

    public void switchCamera(View view) {
        cameraPreview.switchCamera();
    }

    public void switchFlashMode(View view) {
        cameraPreview.switchFlashMode();
    }

    public void take(View view) {
        cameraPreview.tackPicture();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        cameraPreview.release();
    }

    @Override public void onChangeFinish(boolean useFrontCamera, Boolean useFlashLight) {
        runOnUiThread(() -> {
            boolean hasFlashLight = (useFlashLight != null);
            switchFlash.setVisibility(hasFlashLight ? View.VISIBLE : View.INVISIBLE);
            if (hasFlashLight) {
                switchFlash.setText(useFlashLight ? "flash on" : "flash off");
            }
            cameraPreview.useFrontCamera(useFrontCamera);
        });
    }
}
