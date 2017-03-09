package nesto.camera.callback;

/**
 * Created on 2017/3/7.
 * By nesto
 */

public interface OnChangeListener {

    enum OnChangeEvent {
        SWITCH_CAMERA,
        SWITCH_FLASH_MODE,
    }

    void switchCamera();

    void switchFlashMode();
}
