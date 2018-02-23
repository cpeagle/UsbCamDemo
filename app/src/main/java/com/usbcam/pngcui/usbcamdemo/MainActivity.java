package com.usbcam.pngcui.usbcamdemo;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity extends Activity implements View.OnClickListener, SurfaceHolder.Callback {

    private static final String TAG = "Pngcui";
    private Button camReleaseBtn,camCam1Btn, camCam2Btn,camStwichBtn;
    private Button jniReleaseBtn,jniCam1Btn, jniCam2Btn,jniStwichBtn;

    private SurfaceView mPreviewSurface;

    private SurfaceHolder mHolder;

    private Camera mCamera;

    private Jni jni;

    private boolean flag = true;

    // 0表示后置，1表示前置
    private int mCameraPosition = 0;
    private int mJniCameraPosition = 0;

    static final int IMG_WIDTH=640;
    static final int IMG_HEIGHT=480;

    // The following variables are used to draw camera images.
    private int winWidth=0;
    private int winHeight=0;
    private Rect rect;
    private int dw, dh;
    private float rate;
    private Bitmap bmp=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initHolder();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initHolder() {
        mHolder = mPreviewSurface.getHolder();
        mHolder.addCallback(this);

        jni = new Jni();
    }

    private void initViews() {
        camReleaseBtn = (Button) findViewById(R.id.cam_release_btn);
        camReleaseBtn.setOnClickListener(this);
        camCam1Btn = (Button) findViewById(R.id.cam_cam1_btn);
        camCam1Btn.setOnClickListener(this);
        camCam2Btn = (Button) findViewById(R.id.cam_cam2_btn);
        camCam2Btn.setOnClickListener(this);
        camStwichBtn = (Button) findViewById(R.id.cam_switch_btn);
        camStwichBtn.setOnClickListener(this);

        jniReleaseBtn = (Button) findViewById(R.id.jni_release_btn);
        jniReleaseBtn.setOnClickListener(this);
        jniCam1Btn = (Button) findViewById(R.id.jni_cam1_btn);
        jniCam1Btn.setOnClickListener(this);
        jniCam2Btn = (Button) findViewById(R.id.jni_cam2_btn);
        jniCam2Btn.setOnClickListener(this);
        jniStwichBtn = (Button) findViewById(R.id.jni_switch_btn);
        jniStwichBtn.setOnClickListener(this);

        mPreviewSurface = (SurfaceView) findViewById(R.id.preview_suf);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cam_cam1_btn:
                openCamera1();
                break;
            case R.id.cam_cam2_btn:
                openCamera2();
                break;
            case R.id.cam_switch_btn:
                switchCamera();
                break;
            case R.id.cam_release_btn:
                releaseCamera();
                break;

            case R.id.jni_cam1_btn:
                startMyCamera(0);
                break;
            case R.id.jni_cam2_btn:
                startMyCamera(1);
                break;
            case R.id.jni_switch_btn:
                try {
                    JniSwitchCamera();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.jni_release_btn:
                flag = false;
                jni.stopCamera();
                break;
            default:
                break;
        }
    }

    private void JniSwitchCamera() throws InterruptedException {
        flag = false;
        jni.stopCamera();
        Thread.sleep(500);
        startMyCamera(mJniCameraPosition%2);
        mJniCameraPosition = mJniCameraPosition+1;
    }

    private void startMyCamera(int i) {
        flag = true;
        int ret = jni.prepareCamera(i);
        Log.i(TAG,"open cam"+i+" : "+ret);
        if(bmp==null){
            bmp = Bitmap.createBitmap(IMG_WIDTH, IMG_HEIGHT, Bitmap.Config.ARGB_8888);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"run...");
                while(flag) {
                    if(winWidth==0){
                        winWidth=mPreviewSurface.getWidth();
                        winHeight=mPreviewSurface.getHeight();
                        Log.i(TAG,"width = "+winWidth+"height = "+winHeight);
                        if(winWidth*3/4<=winHeight){
                            dw = 0;
                            dh = (winHeight-winWidth*3/4)/2;
                            rate = ((float)winWidth)/IMG_WIDTH;
                            rect = new Rect(dw,dh,dw+winWidth-1,dh+winWidth*3/4-1);
                        }else{
                            dw = (winWidth-winHeight*4/3)/2;
                            dh = 0;
                            rate = ((float)winHeight)/IMG_HEIGHT;
                            rect = new Rect(dw,dh,dw+winHeight*4/3 -1,dh+winHeight-1);
                        }
                    }
                    jni.processCamera();
                    jni.pixeltobmp(bmp);
//test
                    Canvas canvas = mHolder.lockCanvas();
                    if (canvas != null)
                    {
                        // draw camera bmp on canvas
                        canvas.drawBitmap(bmp,null,rect,null);

                        mHolder.unlockCanvasAndPost(canvas);
                    }
                }
                Log.i(TAG,"break..."+flag);
            }
        }).start();
    }

    private void openCamera1() {
        Log.i(TAG,"open cam:cam1");
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数
        Log.i(TAG,"cameraNum = "+cameraCount);
        releaseCamera();
        // 打开当前选中的摄像头
        mCamera = Camera.open(0);
        // 通过surfaceview显示取景画面
        setStartPreview(mCamera, mHolder);
    }
//test for git
    private void openCamera2() {
        Log.i(TAG,"open cam:cam2");
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数
        Log.i(TAG,"cameraNum = "+cameraCount);

        releaseCamera();
        // 打开当前选中的摄像头
        mCamera = Camera.open(1);
        // 通过surfaceview显示取景画面
        setStartPreview(mCamera, mHolder);
    }

    private void switchCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数
        Log.i(TAG,"cameraNum = "+cameraCount);
        int post = (mCameraPosition)%cameraCount;
        Log.i(TAG,"cameraPostision = "+post);
        releaseCamera();
        // 打开当前选中的摄像头
        mCamera = Camera.open(post);

        // 通过surfaceview显示取景画面
        setStartPreview(mCamera, mHolder);
        mCameraPosition = mCameraPosition + 1;

        Camera.getCameraInfo(post,cameraInfo);
        if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
            Log.i(TAG,"it is back facing!!!!!!!!!!");
        else
            Log.i(TAG,"it is front facing!!!!!!!!!!");
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();// 停掉原来摄像头的预览
            mCamera.release();
            mCamera = null;
        }
    }

    // 检测是否有可用摄像头
    private boolean checkCameraHardware() {
        if (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    // 开启预览
    private void setStartPreview(Camera camera, SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {

        }
    }


    private void takePicture() {
        // 拍照,设置相关参数
        //Camera.Parameters params = mCamera.getParameters();
        //params.setPictureFormat(ImageFormat.JPEG);
        //params.setPreviewSize(800, 400);
        // 自动对焦
        //params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        //mCamera.setParameters(params);
        Log.i(TAG,"takePicture");
        mCamera.takePicture(null, null, picture);
        setStartPreview(mCamera, mHolder);
    }

    /**
     * 创建png图片回调数据对象
     */
    Camera.PictureCallback picture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) return;
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }
        }
    };

    private static File getOutputMediaFile(int type) {

        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".png");
        } else {
            return null;
        }
        return mediaFile;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mHolder = mPreviewSurface.getHolder();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        if (mHolder.getSurface() == null)
            return;

        try {
            mCamera.stopPreview();
        } catch (Exception e) {

        }

        // setStartPreview(mCamera, mHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        releaseCamera();
        mPreviewSurface = null;
    }


    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }
}
