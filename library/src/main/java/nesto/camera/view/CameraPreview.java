package nesto.camera.view;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nesto.camera.callback.CameraOrientationListener;
import nesto.camera.callback.OnChangeFinishListener;
import nesto.camera.callback.OnChangeListener;
import nesto.camera.callback.OnFocusListener;
import nesto.camera.callback.OnPictureTakeListener;
import nesto.camera.callback.OnPreviewSizeChangeListener;
import nesto.camera.util.CameraHelper;
import nesto.camera.util.FocusHelper;
import nesto.camera.util.Print;

import static android.hardware.Camera.Parameters.FLASH_MODE_ON;
import static android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;
import static nesto.camera.callback.OnChangeListener.OnChangeEvent.SWITCH_CAMERA;
import static nesto.camera.callback.OnChangeListener.OnChangeEvent.SWITCH_FLASH_MODE;

/**
 * Created on 2017/2/20.
 * By nesto
 */

@SuppressWarnings({"deprecation", "unused"})
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback,
        Camera.AutoFocusCallback,
        Camera.PictureCallback {

    private SurfaceHolder holder;
    private Camera camera;
    private Integer cameraId;
    private boolean surfaceCreated;
    private boolean cameraReleased;
    private boolean pictureTakenFinished = true;

    private boolean useFrontCamera = false;
    private Boolean useFlashLight = false;
    private int cameraRotation;
    private OnFocusListener onFocusListener;
    private OnPictureTakeListener onPictureTakeListener;
    private OnPreviewSizeChangeListener onPreviewSizeChangeListener;
    private OnChangeFinishListener onChangeFinishListener;
    private CameraOrientationListener orientationListener;

    private OnChangeListener onChangeListener;
    private Disposable onChangeSubscription;

    private Context context;
    private Point desiredSize;

    public CameraPreview(Context context) {
        this(context, null);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        holder = getHolder();
        holder.addCallback(this);

        initOnChangeListener();

        orientationListener = new CameraOrientationListener(context.getApplicationContext());
    }

    private void initOnChangeListener() {
        onChangeSubscription = Observable.create(
                (ObservableOnSubscribe<OnChangeListener.OnChangeEvent>) observableEmitter ->
                        onChangeListener = new OnChangeListener() {
                            @Override public void switchCamera() {
                                observableEmitter.onNext(SWITCH_CAMERA);
                            }

                            @Override public void switchFlashMode() {
                                observableEmitter.onNext(SWITCH_FLASH_MODE);
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
                            CameraHelper.enableFlashLight(camera, !useFlashLight);
                            cameraModeChangeFinish();
                            break;
                        default:
                            break;
                    }
                }, (throwable) -> {
                    Print.error(throwable);
                    initOnChangeListener();
                });
    }

    private void cameraModeChangeFinish() {
        String flashMode = camera.getParameters().getFlashMode();
        if (flashMode == null) {
            useFlashLight = null;
        } else {
            useFlashLight = flashMode.equals(FLASH_MODE_ON);
        }
        if (onChangeFinishListener != null) {
            onChangeFinishListener.onChangeFinish(useFrontCamera, useFlashLight);
        }
    }

    @Override public void surfaceCreated(SurfaceHolder holder) {
        Print.log("surfaceCreated");
        surfaceCreated = true;
        startPreview();
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Print.log("surfaceChanged");
        surfaceCreated = true;
    }

    @Override public void surfaceDestroyed(SurfaceHolder holder) {
        Print.log("surfaceDestroyed");
        surfaceCreated = false;
    }

    public synchronized void openCamera() {
        Observable.create((ObservableOnSubscribe<Camera>)
                subscriber -> {
                    subscriber.onNext(getCamera());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(camera -> {
                    Print.log("open camera");
                    this.camera = camera;
                    initCameraConfig();
                    cameraReleased = false;
                    if (surfaceCreated) {
                        Print.log("setPreviewDisplay");
                        startPreview();
                    }
                    cameraModeChangeFinish();
                }, Print::error);
    }

    private synchronized Camera getCamera() {
        cameraId = useFrontCamera
                ? CameraHelper.getFrontCameraId()
                : CameraHelper.getBackCameraId();
        cameraRotation = CameraHelper.cameraDisplayOrientation(context, cameraId);
        Print.log("cameraRotation " + cameraRotation);
        this.camera = CameraHelper.getCamera(cameraId);
        camera.setDisplayOrientation(cameraRotation);
        if (orientationListener != null) orientationListener.enable();
        return camera;
    }

    private void initCameraConfig() {
        CameraHelper.setFocusMode(camera, FOCUS_MODE_AUTO);
        try {
            camera.autoFocus(this);
        } catch (Exception e) {
            Print.error(e);
        }

        Camera.Parameters parameters = camera.getParameters();
        Camera.Size previewSize = CameraHelper.getBestPreviewSize(parameters,
                desiredSize == null ? CameraHelper.SCREEN_WIDTH : desiredSize.x,
                desiredSize == null ? CameraHelper.SCREEN_HEIGHT : desiredSize.y);
        Print.log("previewSize width " + previewSize.width + " height " + previewSize.height);
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        if (onPreviewSizeChangeListener != null) {
            onPreviewSizeChangeListener
                    .onPreviewSizeChange(previewSize.width, previewSize.height);
        }

        Camera.Size pictureSize = CameraHelper.getBestPictureSize(parameters,
                previewSize.width, previewSize.height);
        Print.log("pictureSize width " + pictureSize.width + " height " + pictureSize.height);
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        camera.setParameters(parameters);
    }

    public synchronized void stopCamera() {
        if (camera == null || cameraReleased) return;

        try {
            camera.stopPreview();
            camera.release();
            cameraReleased = true;
            if (orientationListener != null) orientationListener.disable();
        } catch (Exception e) {
            Print.error(e);
        }
    }

    private void startPreview() {
        if (camera == null || cameraReleased) return;
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Print.error(e);
        }
    }

    private void stopPreview() {
        if (camera == null || cameraReleased) return;
        try {
            camera.stopPreview();
        } catch (Exception e) {
            Print.error(e);
        }
    }

    /* methods about switch camera facing ====== start */
    public void switchCamera() {
        if (onChangeListener == null) return;
        onChangeListener.switchCamera();
    }

    public CameraPreview useFrontCamera(boolean useFrontCamera) {
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

    public CameraPreview useFlashLight(boolean useFlashLight) {
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
            if (onFocusListener != null) {
                onFocusListener.onStartFocus(event.getX(), event.getY());
            }

            CameraHelper.setFocusMode(camera, Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            Camera.Parameters parameters = camera.getParameters();
            Rect focusRect = FocusHelper.tapEventToFocusArea(event, useFrontCamera,
                    cameraRotation, this, 1f);
            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<>();
                focusAreas.add(new Camera.Area(focusRect, 1000));
                parameters.setFocusAreas(focusAreas);
            }

            Print.log("focus area " + focusRect.toString());

            Rect meteringRect = FocusHelper.tapEventToFocusArea(event, useFrontCamera,
                    cameraRotation, this, 1.5f);
            if (parameters.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> meteringAreas = new ArrayList<>();
                meteringAreas.add(new Camera.Area(meteringRect, 1000));
                parameters.setMeteringAreas(meteringAreas);
            }

            camera.setParameters(parameters);
        } catch (Exception e) {
            Print.error(e);
        }
    }

    @Override public void onAutoFocus(boolean success, Camera camera) {
        Print.log("onAutoFocus");
        if (onFocusListener == null) return;
        onFocusListener.onEndFocus();
    }
    /* methods about focus ====== start */

    /* methods about take picture ====== start */
    public void tackPicture() {
        if (camera != null && pictureTakenFinished) {
            try {
                camera.takePicture(null, null, this);
                pictureTakenFinished = false;
            } catch (Exception e) {
                Print.error(e);
                pictureTakenFinished = true;
            }
        }
    }

    public int getPictureAngle() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        int rotation;
        int degrees = orientationListener.getRememberedOrientation();
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - degrees + 360) % 360;
        } else {
            rotation = (info.orientation + degrees) % 360;
        }
        return rotation;
    }

    @Override public void onPictureTaken(byte[] data, Camera camera) {
        if (orientationListener != null && onPictureTakeListener != null) {
            orientationListener.rememberOrientation();
            onPictureTakeListener.onPictureTaken(data, getPictureAngle());
        }
        pictureTakenFinished = true;
        startPreview();
    }
    /* methods about take picture ====== end */

    public void setOnPictureTakeListener(OnPictureTakeListener onPictureTakeListener) {
        this.onPictureTakeListener = onPictureTakeListener;
    }

    public void setOnPreviewSizeChangeListener(OnPreviewSizeChangeListener listener) {
        onPreviewSizeChangeListener = listener;
    }

    public void setDesiredSize(Point desiredSize) {
        this.desiredSize = desiredSize;
    }

    public void setOnChangeFinishListener(OnChangeFinishListener onChangeFinishListener) {
        this.onChangeFinishListener = onChangeFinishListener;
    }

    public void release() {
        if (onChangeSubscription != null && !onChangeSubscription.isDisposed()) {
            onChangeSubscription.dispose();
        }
    }
}
