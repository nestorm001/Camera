package nesto.camera.util;

import android.util.Log;

/**
 * Created on 2017/3/16.
 * By nesto
 */

public class Print {
    public static boolean debug = false;

    public static void log(String s) {
        if (debug) {
            Log.d("wtf", s);
        }
    }

    public static void error(Throwable throwable) {
        if (debug) {
            throwable.printStackTrace();
        }
    }
}
