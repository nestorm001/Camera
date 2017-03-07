package nesto.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.schedulers.Schedulers;

/**
 * Created on 2017/2/20.
 * By nesto
 */

@SuppressWarnings("deprecation")
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Camera camera;
    private boolean surfaceCreated;
    private boolean cameraReleased;

    public CameraView(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override public void surfaceCreated(SurfaceHolder holder) {
        Log.d("wtf", "surfaceCreated");
        Log.d("wtf", holder.toString() + " " + mHolder.toString());
        surfaceCreated = true;
        try {
            if (camera != null && !cameraReleased) {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("wtf", "surfaceChanged");
    }

    @Override public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("wtf", "surfaceDestroyed");
        surfaceCreated = false;
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            startPreview();
        } catch (Exception e) {
            Log.d("wtf", "Error starting camera preview: " + e.getMessage());
        }
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
                        try {
                            startPreview();
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw Exceptions.propagate(e);
                        }
                    }
                }, Throwable::printStackTrace);
    }

    private void startPreview() throws IOException {
        if (cameraReleased) return;

        camera.setPreviewDisplay(mHolder);
        camera.startPreview();
    }
}
