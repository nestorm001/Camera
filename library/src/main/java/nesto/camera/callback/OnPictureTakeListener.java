package nesto.camera.callback;

import java.io.InputStream;

/**
 * Created on 2017/3/8.
 * By nesto
 */

public interface OnPictureTakeListener {
    void onPictureTaken(InputStream inputStream, int pictureRotation);
}
