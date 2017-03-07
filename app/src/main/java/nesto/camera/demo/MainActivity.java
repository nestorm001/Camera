package nesto.camera.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import nesto.camera.CameraView;

public class MainActivity extends AppCompatActivity {

    private CameraView mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewGroup holder = (ViewGroup) findViewById(R.id.holder);
        mPreview = new CameraView(this);
        holder.addView(mPreview);
    }

    @Override protected void onResume() {
        super.onResume();
        mPreview.openCamera();
    }

    @Override protected void onPause() {
        super.onPause();
        mPreview.stopCamera();
    }
}
