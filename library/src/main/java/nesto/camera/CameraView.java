package nesto.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on 2017/2/20.
 * By nesto
 */

@SuppressWarnings("deprecation")
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private Camera camera;
    private boolean surfaceCreated;
    private boolean cameraReleased;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

        holder = getHolder();
        holder.addCallback(this);
    }

    @Override public void surfaceCreated(SurfaceHolder holder) {
        Log.d("wtf", "surfaceCreated");
        surfaceCreated = true;
        startPreview();

    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("wtf", "surfaceChanged");
    }

    @Override public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("wtf", "surfaceDestroyed");
        surfaceCreated = false;
        if (holder.getSurface() == null) return;

        // stop preview before making changes
        stopPreview();

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // TODO do something

        // start preview with new settings
        startPreview();
    }

    public synchronized void stopCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            cameraReleased = true;
        }
    }

    public synchronized void openCamera() {
        Observable.create((Observable.OnSubscribe<Camera>) subscriber
                -> subscriber.onNext(CameraHelper.getCameraInstance()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(camera -> {
                    Log.d("wtf", "open camera");
                    this.camera = camera;
                    cameraReleased = false;
                    if (surfaceCreated) {
                        Log.d("wtf", "setPreviewDisplay");
                        startPreview();
                    }
                }, Throwable::printStackTrace);
    }

    private void stopPreview() {
        if (camera == null || cameraReleased) return;
        try {
            camera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        if (camera == null || cameraReleased) return;
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
