package nesto.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

/**
 * Created on 2017/2/20.
 * By nesto
 */

@SuppressWarnings("deprecation") public final class CameraHelper {
    private CameraHelper() {}

    public static boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static Camera getFrontCamera() {
        return getCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    public static Camera getBackCamera() {
        return getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    private static Camera getCamera(int facing) {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == facing) return Camera.open(i);
        }
        return null;
    }

    public static void enableFlashLight(Camera camera, boolean enable) {
        try {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(enable ? Camera.Parameters.FLASH_MODE_ON
                    : Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
