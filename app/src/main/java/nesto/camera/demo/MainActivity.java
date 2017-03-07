package nesto.camera.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import nesto.camera.CameraView;

public class MainActivity extends AppCompatActivity {

    private CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = (CameraView) findViewById(R.id.holder);
    }

    @Override protected void onResume() {
        super.onResume();
        cameraView.openCamera();
    }

    @Override protected void onPause() {
        super.onPause();
        cameraView.stopCamera();
    }
}
