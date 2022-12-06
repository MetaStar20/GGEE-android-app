package com.example.ggee_vs.CaptureService;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.ggee_vs.Config;
import com.example.ggee_vs.R;
import com.example.ggee_vs.UploadService.FileUpload;
import com.example.ggee_vs.UploadService.RequestUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class CaptureLightService extends Service implements
        SurfaceHolder.Callback {

    // Camera variables
    // a surface holder
    // a variable to control the camera
    private Camera mCamera;
    // the camera parameters
    private Camera.Parameters parameters;
    private Camera.Size pictureSize;

    //Take image AsyncTask
    TakeImageAsync mTakenImgAsync;

    //upload function.
    FileUpload mUpload;
    /* grabbing information */
    public Intent cameraIntent;
    private String FLASH_MODE;
    private int QUALITY_MODE = 0; // Compression rate
    private int IMAGE_COUNT = 0; // calculate from intervals and extent in MainActivity.
    private int resolution = 0; // width, height
    private int intervals = 1000;
    private int id_request = 0;
    private int id_method = 0;

    private boolean isFrontCamRequest = false;
    private SurfaceView sv;
    private SurfaceHolder sHolder;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private Bitmap bmp;
    private FileOutputStream fo;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private int imgWidth = 0, imgHeight = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate() {
        super.onCreate();

    }

    private Camera openFrontFacingCameraGingerbread() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e("Camera",
                            "Camera failed to open: " + e.getLocalizedMessage());
                    /*
                     * Toast.makeText(getApplicationContext(),
                     * "Front Camera failed to open", Toast.LENGTH_LONG)
                     * .show();
                     */
                }
            }
        }
        return cam;
    }

    private void setPictureResolution() {
        if(resolution < 1) resolution = 1;
        if (imgWidth == 0 | imgHeight == 0) {
            pictureSize = getBiggesttPictureSize(parameters);
            if (pictureSize != null){
                imgWidth = pictureSize.width/resolution;
                imgHeight = pictureSize.height/resolution;
                parameters.setPictureSize(imgWidth, imgHeight);
            }
        } else {
            // if (pictureSize != null)
            parameters.setPictureSize(imgWidth, imgHeight);
        }
    }

    private Camera.Size getBiggesttPictureSize(Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result = size;
            } else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                if (newArea > resultArea) {
                    result = size;
                }
            }
        }

        return (result);
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** Check if this device has front camera */
    private boolean checkFrontCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FRONT)) {
            // this device has front camera
            return true;
        } else {
            // no front camera on this device
            return false;
        }
    }

    Handler handler = new Handler();

    private class TakeImageAsync extends AsyncTask<Intent, Void, Void> {

        @Override
        protected Void doInBackground(Intent... params) {
            try {
                getParamsFromIntent(params[0]);
                for (int i = 0 ; i < IMAGE_COUNT; i++){
                    if(isCancelled()) break;
                    takeImage(params[0]);
                    Thread.currentThread();
                    Thread.sleep(intervals);
                }
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }


    private void getParamsFromIntent(Intent intent){
        Bundle extras = intent.getExtras();
        if (extras != null) {
            FLASH_MODE = extras.getString("FLASH","auto");
            isFrontCamRequest = extras.getBoolean("Front_Request",false);
            resolution = extras.getInt("resolution",0);
            IMAGE_COUNT  =  extras.getInt("count",10);
            intervals    = extras.getInt("intervals",1000);
            id_request   = extras.getInt("id_request",0);
            id_method    = extras.getInt("id_method",1000);
        }
    }

     private synchronized void takeImage(Intent intent) {

        if (mCamera == null)
             mCamera = getCameraInstance();

        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(sv.getHolder());
                parameters = mCamera.getParameters();
                if (FLASH_MODE == null || FLASH_MODE.isEmpty()) {
                    FLASH_MODE = "auto";
                }
                parameters.setFlashMode(FLASH_MODE);
                // set biggest picture
                setPictureResolution();
                // log quality and image format
                Log.d("Qaulity", parameters.getJpegQuality() + "");
                Log.d("Format", parameters.getPictureFormat() + "");

                // set camera parameters
                mCamera.setParameters(parameters);
                mCamera.startPreview();
                mCamera.takePicture(null, null, mCall);
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Camera is unavailable !",
                                Toast.LENGTH_LONG).show();
                    }
                });

            }
            // return 4;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e("TAG", "CmaraHeadService()::takePicture", e);
        }
        // Get a surface
        /*
         * sHolder = sv.getHolder(); // tells Android that this surface
         * will have its data constantly // replaced if
         * (Build.VERSION.SDK_INT < 11)
         *
         * sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
         */
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // sv = new SurfaceView(getApplicationContext());
        cameraIntent = intent;
        Log.d("ImageTakin", "StartCommand()");
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.width = 1;
        params.height = 1;
        params.x = 0;
        params.y = 0;
        sv = new SurfaceView(getApplicationContext());

        windowManager.addView(sv, params);
        sHolder = sv.getHolder();
        sHolder.addCallback(this);

        // tells Android that this surface will have its data constantly
        // replaced
        if (Build.VERSION.SDK_INT < 11)
            sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        return START_NOT_STICKY;
    }

    private File getTestFileFromDrawable(){
        try
        {
            File f=new File("TestImg");
            @SuppressLint("ResourceType") InputStream inputStream = getResources().openRawResource(R.drawable.background);
            OutputStream out=new FileOutputStream(f);
            byte buf[]=new byte[1024];
            int len;
            while((len=inputStream.read(buf))>0)
                out.write(buf,0,len);
            out.close();
            inputStream.close();
            return f;
        }
        catch (IOException e){}
        return  null;
    }

    Camera.PictureCallback mCall = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // decode the data obtained by the camera into a Bitmap
            Log.d("ImageTakin", "Done");
            if (bmp != null)
                bmp.recycle();
            System.gc();
            bmp = decodeBitmap(data);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            if (bmp != null && QUALITY_MODE == 0)
                bmp.compress(Bitmap.CompressFormat.JPEG, 70, bytes);
            else if (bmp != null && QUALITY_MODE != 0)
                bmp.compress(Bitmap.CompressFormat.JPEG, QUALITY_MODE, bytes);
//            File imagesFolder = new File(Environment.getExternalStorageDirectory(), "MYGALLERY");
            File imagesFolder = new File(Config.SAVE_IMG_PATH);
            if (!imagesFolder.exists())
                imagesFolder.mkdirs(); // <----
            File image = new File(imagesFolder, System.currentTimeMillis()
                    + ".jpg");

            // write the bytes in file
            try {
                fo = new FileOutputStream(image);
            } catch (FileNotFoundException e) {
                Log.e("TAG", "FileNotFoundException", e);
                // TODO Auto-generated catch block
            }
            try {
                fo.write(bytes.toByteArray());
            } catch (IOException e) {
                Log.e("TAG", "fo.write::PictureTaken", e);
                // TODO Auto-generated catch block
            }

            // remember close de FileOutput
            try {
                fo.close();
                if (Build.VERSION.SDK_INT < 19)
                    sendBroadcast(new Intent(
                            Intent.ACTION_MEDIA_MOUNTED,
                            Uri.parse("file://"
                                    + Environment.getExternalStorageDirectory())));
                else {
                    MediaScannerConnection
                            .scanFile(
                                    getApplicationContext(),
                                    new String[] { image.toString() },
                                    null,
                                    new MediaScannerConnection.OnScanCompletedListener() {
                                        public void onScanCompleted(
                                                String path, Uri uri) {
                                            Log.i("ExternalStorage", "Scanned "
                                                    + path + ":");
                                            Log.i("ExternalStorage", "-> uri="
                                                    + uri);
                                        }
                                    });
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            /*
             * Toast.makeText(getApplicationContext(),
             * "Your Picture has been taken !", Toast.LENGTH_LONG).show();
             */

            RequestUtils.uploadFile(Config.UPLOAD_URL,image,id_request);

            Log.d("Camera", "Image Taken ! .. upload image");

            if (bmp != null) {
                bmp.recycle();
                bmp = null;
                System.gc();
            }
//            if (mCamera != null) {
//                mCamera.stopPreview();
//                mCamera.release();
//                mCamera = null;
//            }
//
//            mCamera = null;
//            stopSelf();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void onDestroy() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (sv != null)
            windowManager.removeView(sv);

        // stop TakenImgAsync Task

        mTakenImgAsync.cancel(true);

        Log.d("CaptureLightService","stop service...");
        super.onDestroy();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (cameraIntent != null)
            mTakenImgAsync =  new TakeImageAsync();
            mTakenImgAsync.execute(cameraIntent);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public static Bitmap decodeBitmap(byte[] data) {

        Bitmap bitmap = null;
        BitmapFactory.Options bfOptions = new BitmapFactory.Options();
        bfOptions.inDither = false; // Disable Dithering mode
        bfOptions.inPurgeable = true; // Tell to gc that whether it needs free
        // memory, the Bitmap can be cleared
        bfOptions.inInputShareable = true; // Which kind of reference will be
        // used to recover the Bitmap data
        // after being clear, when it will
        // be used in the future
        bfOptions.inTempStorage = new byte[32 * 1024];

        if (data != null)
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                    bfOptions);

        return bitmap;
    }

}