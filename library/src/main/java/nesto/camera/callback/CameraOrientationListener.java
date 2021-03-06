package nesto.camera.callback;

import android.content.Context;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;

public class CameraOrientationListener extends OrientationEventListener {

    private int currentNormalizedOrientation;
    private int rememberedNormalizedOrientation;

    public CameraOrientationListener(Context context) {
        super(context, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override public void onOrientationChanged(int orientation) {
        if (orientation != ORIENTATION_UNKNOWN) {
            currentNormalizedOrientation = normalize(orientation);
        }
    }

    public void rememberOrientation() {
        rememberedNormalizedOrientation = currentNormalizedOrientation;
    }

    public int getRememberedOrientation() {
        return rememberedNormalizedOrientation;
    }

    private int normalize(int degrees) {
        if (degrees > 315 || degrees <= 45) return 0;
        if (degrees > 45 && degrees <= 135) return 90;
        if (degrees > 135 && degrees <= 225) return 180;
        if (degrees > 225 && degrees <= 315) return 270;
        throw new RuntimeException("You should never get here");
    }
}
