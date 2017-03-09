package nesto.camera.callback;

/**
 * Created on 2017/3/7.
 * By nesto
 */

public interface OnFocusListener {
    void onStartFocus(float x, float y);

    void onEndFocus();
}
