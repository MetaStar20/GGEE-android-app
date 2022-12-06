package com.example.ggee_vs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.ggee_vs.CaptureService.CaptureLightService;
import com.example.ggee_vs.Events.WfmEvent;
import com.example.ggee_vs.Requests.RequestType;
import com.example.ggee_vs.Requests.WfmRequest;
import com.example.ggee_vs.HttpServer.MyHTTPD;
import com.example.ggee_vs.UploadService.RequestUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.FutureTask;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class MainActivity extends Activity {
    private final static String TAG = "MainActivity";
    private final static boolean isBackgroundRunning = false;
    private static final int IMAGE_COUNT = 1;
    private TextView tvGrab;
    private TextView tvIpAddress;
    private TextView tvStatus;
    private Intent front_translucent;
    private static MyHTTPD server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_card);
        initObject();
        setListener();
    }

    private void initObject() {
        tvGrab = (TextView) findViewById(R.id.textViewTapToGrabPic);
        tvIpAddress = (TextView)findViewById(R.id.txtIpAddress);
        tvStatus = (TextView)findViewById(R.id.tvStatus);
        try {
            server = new MyHTTPD(this);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setListener(){
        tvGrab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Touch...");
                myTakePicture();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
            // Test
            myTakePicture();
            return true;
        }
        return super.onKeyDown(keycode, event);
    }

    // capture image from camera activity
    private void myTakePicture(){
        Camera camera = null;
        try {
            camera = Camera.open();
            camera.lock();
//            SurfaceView dummy = (SurfaceView) findViewById(R.id.surfaceView); //new SurfaceView(this);
//            camera.setPreviewDisplay(dummy.getHolder());
            camera.setDisplayOrientation(90);
            camera.startPreview();
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(final byte[] originalData, Camera camera) {
                    ((TextView) findViewById(R.id.textViewTapToGrabPic)).setText(R.string.pic_grabbed);
                    if(savePic(originalData)) {
                        Log.d(TAG, "Pic saved");
                    }
                    else
                        Log.d(TAG,"Pic save FAILED");
                    camera.release();
                }
            });
        } catch (Exception e) {
            Log.d(TAG,e.getMessage());
            Log.d(TAG, Log.getStackTraceString(e));
            ((TextView)findViewById(R.id.textViewTapToGrabPic)).setText(R.string.pic_grab_fail);
            if (camera != null) {
                camera.release();
            }
        }
    }

    private boolean savePic(byte[] data){
        if (data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data.length);
            String myPath = "/storage/sdcard0/DCIM/Camera";
            if(bitmap!=null){
                /*
                String urlNewPic = MediaStore.Images.Media.insertImage(getContentResolver(),
                        bitmap, "test" + System.currentTimeMillis(), "Test");
                if(urlNewPic != null) {
                    Log.d("SAVE_PIC", "path: " + urlNewPic);
                    return true;
                }*/
                File file=new File(myPath);
                if(!file.isDirectory()){
                    file.mkdir();
                }
                file = new File(myPath,"test_" + System.currentTimeMillis() + ".png");
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    // Make it visible from PC
                    MediaScannerConnection.scanFile(this, new String[]{myPath},new String[]{"image/png"}, null);
                    // upload file...
                    uploadFile(file);
                    return true;
                }
                catch(IOException e){
                    Log.d(TAG, Log.getStackTraceString(e));
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void startGrabbing(int id_request, int id_method, int resolution, int intervals, int extent) {

        /* set detail parameter to open camera. */
        int count = extent*1000/intervals;

        front_translucent = new Intent(getApplication().getApplicationContext(), CaptureLightService.class);
        front_translucent.putExtra("Front_Request", true);
        front_translucent.putExtra("count", count);
        front_translucent.putExtra("intervals", intervals);
        front_translucent.putExtra("id_request", id_request);
        front_translucent.putExtra("id_method", id_method);
        front_translucent.putExtra("resolution",resolution);

        getApplication().getApplicationContext().startService(front_translucent);
    }

    private boolean stopGrabbing() {
        return stopService(front_translucent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWfmEvent(WfmEvent wfmEvent){
        if(wfmEvent.type == RequestType.Start){
            Toast.makeText(this, "Start to grabbing images...", Toast.LENGTH_SHORT).show();
            tvStatus.setText("Grabbing images...");
            // set detail information to start grabbing.
            WfmRequest wReq = wfmEvent.wReq;
            startGrabbing(wReq.getId_request(), wReq.getId_method(),
                          wReq.getResolution(), wReq.getIntervals(), wReq.getExtent());
        } else if (wfmEvent.type == RequestType.Stop){
            Toast.makeText(this, "Stop to grabbing images...", Toast.LENGTH_SHORT).show();
            if(stopGrabbing()) {
                // send stop requests to WFM.
                HashMap<String,String> params = new HashMap<String,String>();
                params.put("stopped","1");
                params.put("id_request",String.valueOf(wfmEvent.wReq.getId_request()));
                RequestUtils.sendPostRequests(Config.STOP_URL,params);
                tvStatus.setText("Binding command from Laptop...");
            }
            else {
                HashMap<String,String> params = new HashMap<String,String>();
                params.put("stopped","0");
                params.put("id_request",String.valueOf(wfmEvent.wReq.getId_request()));
                RequestUtils.sendPostRequests(Config.STOP_URL,params);
                tvStatus.setText("Fail to stop Grabbing images");
            }
        }
    }

    public boolean uploadFile(File file){
        Log.d(TAG,"uploadFile..." + file.getName());
        if (file.exists()){
            OkHttpClient okhttp = new OkHttpClient();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                     .addFormDataPart("file", file.getName(),RequestBody.create(MediaType.parse("multipart/form-data"), file))
                     .addFormDataPart("filename", file.getName())
                     .build();
            FutureTask<Boolean> task = new FutureTask<>(()-> {
                try
                {
                    ResponseBody responseBody = okhttp.newCall(new Request.Builder().post(body).url(Config.UPLOAD_URL).build()).execute().body();
                    if(responseBody != null)
                        return Boolean.parseBoolean(responseBody.string());
                    return false;
                }catch (IOException e){
                    return false;
                }
            });
            try{
                new Thread(task).start();
                return task.get();
            }catch (Exception e){
                return false;
            }
        }else {
            Toast.makeText(MainActivity.this,"Select File", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}
