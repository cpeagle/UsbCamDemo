package com.usbcam.pngcui.usbcamdemo;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2017/8/8.
 */

public class Jni {

    static {
        System.loadLibrary("UsbCam");
    }

    // JNI functions
    public native int prepareCamera(int videoid);
    public native int prepareCameraWithBase(int videoid, int camerabase);
    public native void processCamera();
    public native void stopCamera();
    public native void pixeltobmp(Bitmap bitmap);

}
