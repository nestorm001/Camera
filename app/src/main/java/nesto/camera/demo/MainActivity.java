package nesto.camera.demo;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import nesto.camera.callback.PreviewAutoFullScreenListener;
import nesto.camera.util.CameraHelper;
import nesto.camera.view.CameraPreview;

public class MainActivity extends BaseActivity {

    private CameraPreview cameraPreview;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraPreview = (CameraPreview) findViewById(R.id.holder);
        cameraPreview.setRealScreenSize(CameraHelper.getRealScreenSize(this));
        cameraPreview.setOnPreviewSizeChangeListener(
                new PreviewAutoFullScreenListener(this, cameraPreview));
        cameraPreview.setOnPictureTakeListener((inputStream, pictureRotation) -> {
            BitmapFactory.Options oBounds = new BitmapFactory.Options();
            oBounds.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, oBounds);
            int width = oBounds.outWidth;
            int height = oBounds.outHeight;
            Log.d("wtf", "photo width " + width + " height " + height);
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
}
