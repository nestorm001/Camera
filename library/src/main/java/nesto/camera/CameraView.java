package nesto.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static nesto.camera.OnChangeListener.OnChangeEvent.SWITCH_CAMERA;
import static nesto.camera.OnChangeListener.OnChangeEvent.SWITCH_FLASH_MODE;

/**
 * Created on 2017/2/20.
 * By nesto
 */

@SuppressWarnings({"deprecation", "unused"})
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private Camera camera;
    private boolean surfaceCreated;
    private boolean cameraReleased;

    private boolean useFrontCamera = false;

    private OnChangeListener onChangeListener;
    private Subscription onChangeSubscription;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

        holder = getHolder();
        holder.addCallback(this);

        initOnChangeListener();
    }

    private void initOnChangeListener() {
        onChangeSubscription = Observable.create(
                (Observable.OnSubscribe<OnChangeListener.OnChangeEvent>) subscriber ->
                        onChangeListener = new OnChangeListener() {
                            @Override public void switchCamera() {
                                subscriber.onNext(SWITCH_CAMERA);
                            }

                            @Override public void switchFlashMode() {
                                subscriber.onNext(SWITCH_FLASH_MODE);
                            }
                        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(onChangeEvent -> {
                    switch (onChangeEvent) {
                        case SWITCH_CAMERA:
                            useFrontCamera = !useFrontCamera;
                            stopCamera();
                            openCamera();
                            break;
                        case SWITCH_FLASH_MODE:
                            break;
                        default:
                            break;
                    }
                }, Throwable::printStackTrace);
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
    }

    public synchronized void stopCamera() {
        if (camera == null || cameraReleased) return;

        camera.stopPreview();
        camera.release();
        cameraReleased = true;
    }

    public synchronized void openCamera() {
        Observable.create((Observable.OnSubscribe<Camera>) subscriber
                -> subscriber.onNext(useFrontCamera
                ? CameraHelper.getFrontCamera()
                : CameraHelper.getBackCamera()))
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

    public void switchCamera() {
        onChangeListener.switchCamera();
    }

    public CameraView useFrontCamera(boolean useFrontCamera) {
        this.useFrontCamera = useFrontCamera;
        return this;
    }

    public boolean useFrontCamera() {
        return useFrontCamera;
    }

    public void release() {
        if (onChangeSubscription != null && !onChangeSubscription.isUnsubscribed()) {
            onChangeSubscription.unsubscribe();
        }
    }
}
