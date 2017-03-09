package nesto.camera.demo;

import android.os.Bundle;
import android.view.View;

import nesto.camera.util.CameraHelper;
import nesto.camera.view.CameraPreview;
import nesto.camera.callback.PreviewAutoFullScreenListener;

public class MainActivity extends BaseActivity {

    private CameraPreview cameraPreview;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraPreview = (CameraPreview) findViewById(R.id.holder);
        cameraPreview.setRealScreenSize(CameraHelper.getRealScreenSize(this));
        cameraPreview.setOnPreviewSizeChangeListener(
                new PreviewAutoFullScreenListener(this, cameraPreview));
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
        // TODO
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        cameraPreview.release();
    }
}
