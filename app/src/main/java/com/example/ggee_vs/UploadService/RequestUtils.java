package com.example.ggee_vs.UploadService;

import android.util.Log;

import com.example.ggee_vs.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class RequestUtils {
    public static boolean uploadFile(String url, File file, int id_request){
        if (file.exists()){
            OkHttpClient okhttp = new OkHttpClient();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(),RequestBody.create(MediaType.parse("multipart/form-data"), file))
                    .addFormDataPart("filename", file.getName())
                    .addFormDataPart("id_request", String.valueOf(id_request))
                    .build();
            FutureTask<Boolean> task = new FutureTask<>(()-> {
                try
                {
                    ResponseBody responseBody = okhttp.newCall(new Request.Builder().post(body).url(url).build()).execute().body();
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
            Log.d("Upload","uploading error");
        }
        return false;
    }

    public static boolean sendPostRequests(String url, HashMap<String,String> params) {
        OkHttpClient okhttp = new OkHttpClient();
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for(Map.Entry<String,String> map: params.entrySet()) {
            bodyBuilder.addFormDataPart(map.getKey(),map.getValue());
        }


        RequestBody body = bodyBuilder.build();
        FutureTask<Boolean> task = new FutureTask<>(()-> {
            try
            {
                ResponseBody responseBody = okhttp.newCall(new Request.Builder().post(body).url(url).build()).execute().body();
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
    }


}
