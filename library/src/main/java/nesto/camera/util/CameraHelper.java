package nesto.camera.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.List;

/**
 * Created on 2017/2/20.
 * By nesto
 */

@SuppressWarnings("deprecation") public final class CameraHelper {

    public static final int SCREEN_WIDTH = Resources.getSystem().getDisplayMetrics().widthPixels;
    public static final int SCREEN_HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;

    private CameraHelper() {}

    public static Point getRealScreenSize(Activity activity) {
        Point result = new Point();
        Display display = activity.getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(result);
        } else {
            display.getSize(result);
        }
        return result;
    }

    public static boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static Integer getFrontCameraId() {
        return getCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    public static Integer getBackCameraId() {
        return getCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    private static Integer getCameraId(int facing) {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == facing) return i;
        }
        return null;
    }

    public static Camera getCamera(Integer id) {
        if (id == null) return null;
        return Camera.open(id);
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

    public static void setFocusMode(@NonNull Camera camera, String focusMode) {
        Camera.Parameters parameters = camera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(focusMode)) {
            parameters.setFocusMode(focusMode);
            camera.setParameters(parameters);
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.setParameters(parameters);
        }
    }

    public static int cameraDisplayOrientation(Context context, int cameraId) {
        int cameraAngle;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = manager.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraAngle = (info.orientation + degrees) % 360;
            cameraAngle = (360 - cameraAngle) % 360; // compensate the mirror
        } else { // back-facing
            cameraAngle = (info.orientation - degrees + 360) % 360;
        }
        return cameraAngle;
    }

    public static Camera.Size getBestPreviewSize(Camera.Parameters parameters,
                                                 int width, int height) {
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        return getBestSize(sizes, width, height);
    }

    public static Camera.Size getBestPictureSize(Camera.Parameters parameters) {
        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        return getBestSize(sizes, null, null);
    }

    private static Camera.Size getBestSize(List<Camera.Size> sizes,
                                           Integer width, Integer height) {
        Camera.Size bestSize = null;
        boolean hasRatio = (height != null && width != null);
        // get real best size
        // find size fit both size and ratio
        if (hasRatio) {
            int gcd = gcd(width, height);
            int bigger = Math.max(width, height) / gcd;
            int smaller = Math.min(width, height) / gcd;
            for (Camera.Size currentSize : sizes) {
                boolean isDesiredRatio = (currentSize.width / bigger) ==
                        (currentSize.height / smaller);
                boolean isBetterSize = (bestSize == null || currentSize.width > bestSize.width);
                if (isDesiredRatio && isBetterSize) bestSize = currentSize;
            }
        }

        // find maximum size
        if (bestSize == null) {
            for (Camera.Size currentSize : sizes) {
                boolean isBetterSize = (bestSize == null || currentSize.width > bestSize.width);
                if (isBetterSize) bestSize = currentSize;
            }
        }

        // find minimum size
        if (bestSize == null) {
            for (Camera.Size currentSize : sizes) {
                boolean isBetterSize = (bestSize == null || currentSize.width < bestSize.width);
                if (isBetterSize) bestSize = currentSize;
            }
        }
        return bestSize;
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            int c = b;
            b = a % b;
            a = c;
        }
        return a;
    }
}
