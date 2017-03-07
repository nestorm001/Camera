package nesto.camera;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback {

    private SurfaceHolder holder;
    private Camera camera;
    private boolean surfaceCreated;
    private boolean cameraReleased;

    private boolean useFrontCamera = false;
    private boolean useFlashLight = false;
    private int cameraRotation;
    private OnFocusListener onFocusListener;

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
                            useFlashLight = !useFlashLight;
                            CameraHelper.enableFlashLight(camera, useFlashLight);
                            Log.d("wtf", "flash mode: " + camera.getParameters().getFlashMode());
                            break;
                        default:
                            break;
                    }
                }, (throwable) -> {
                    throwable.printStackTrace();
                    initOnChangeListener();
                });
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
                    camera.autoFocus(this);
                    cameraReleased = false;
                    if (surfaceCreated) {
                        Log.d("wtf", "setPreviewDisplay");
                        startPreview();
                    }
                }, Throwable::printStackTrace);
    }

    public synchronized void stopCamera() {
        if (camera == null || cameraReleased) return;

        try {
            camera.stopPreview();
            camera.release();
            cameraReleased = true;
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

    private void stopPreview() {
        if (camera == null || cameraReleased) return;
        try {
            camera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* methods about switch camera facing ====== start */
    public void switchCamera() {
        if (onChangeListener == null) return;
        onChangeListener.switchCamera();
    }

    public CameraView useFrontCamera(boolean useFrontCamera) {
        this.useFrontCamera = useFrontCamera;
        return this;
    }

    public boolean useFrontCamera() {
        return useFrontCamera;
    }
    /* methods about switch camera facing ====== end */

    /* methods about switch flash light mode ====== start */
    public boolean useFlashLight() {
        return useFlashLight;
    }

    public CameraView useFlashLight(boolean useFlashLight) {
        this.useFlashLight = useFlashLight;
        return this;
    }

    // return flash mode changed or not
    public boolean switchFlashMode() {
        if (camera == null || cameraReleased) return false;
        if (useFrontCamera) return false;
        if (onChangeListener == null) return false;
        onChangeListener.switchFlashMode();
        return true;
    }
    /* methods about switch flash light mode ====== end */

    /* methods about focus ====== start */
    @Override public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN://单点按下
                focusOnTouch(event);
                return true;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void focusOnTouch(MotionEvent event) {
        try {
            CameraHelper.setAutoFocus(camera);
            if (onFocusListener != null) {
                onFocusListener.onStartFocus(event.getX(), event.getY());
            }

            Camera.Parameters parameters = camera.getParameters();
            Rect focusRect = FocusHelper.tapEventToFocusArea(event, useFrontCamera,
                    cameraRotation, this, 1f);
            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<>();
                focusAreas.add(new Camera.Area(focusRect, 1000));
                parameters.setFocusAreas(focusAreas);
            }

            Rect meteringRect = FocusHelper.tapEventToFocusArea(event, useFrontCamera,
                    cameraRotation, this, 1.5f);
            if (parameters.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> meteringAreas = new ArrayList<>();
                meteringAreas.add(new Camera.Area(meteringRect, 1000));
                parameters.setMeteringAreas(meteringAreas);
            }

            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void onAutoFocus(boolean success, Camera camera) {
        if (onFocusListener == null) return;
        Log.d("wtf", "onAutoFocus");
        onFocusListener.onEndFocus();
    }
    /* methods about focus ====== start */

    public void release() {
        if (onChangeSubscription != null && !onChangeSubscription.isUnsubscribed()) {
            onChangeSubscription.unsubscribe();
        }
    }
}
