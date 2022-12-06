package com.example.ggee_vs.UploadService;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.ggee_vs.UploadService.ClsGlobal.getDataColumn;
import static com.example.ggee_vs.UploadService.ClsGlobal.isDownloadsDocument;
import static com.example.ggee_vs.UploadService.ClsGlobal.isExternalStorageDocument;
import static com.example.ggee_vs.UploadService.ClsGlobal.isGooglePhotosUri;
import static com.example.ggee_vs.UploadService.ClsGlobal.isMediaDocument;

public class FileUpload {
    private static final String LOG_TAG = "FileUpload";
    private static final String API_URL_BASE = "http://10.10.11.235:8080/api/";
    private Context mContext;

    public FileUpload(Context ctx){
        mContext = ctx;
    }

    private void mulipleFileUploadFile(Uri[] fileUri) {
        OkHttpClient okHttpClient = new OkHttpClient();
        OkHttpClient clientWith10sTimeout = okHttpClient.newBuilder()
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL_BASE)
                .addConverterFactory(GsonConverterFactory.create()) //new MultiPartConverter()
                .client(clientWith10sTimeout)
                .build();

        WebAPIService service = retrofit.create(WebAPIService.class); //here is the interface which you have created for the call service
        Map<String, RequestBody> maps = new HashMap<>();

        if (fileUri!=null && fileUri.length>0) {
            for (int i = 0; i < fileUri.length; i++) {
                String filePath = getRealPathFromUri(fileUri[i]);
                File file1 = new File(filePath);

                if (filePath != null && filePath.length() > 0) {
                    if (file1.exists()) {
                        okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"), file1);
                        String filename = "imagePath" + i; //key for upload file like : imagePath0
                        maps.put(filename + "\"; filename=\"" + file1.getName(), requestFile);
                    }
                }
            }
        }

        String descriptionString = " string request";//
        //hear is the your json request
        Call<String> call = service.postFile(maps, descriptionString);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call,
                                   Response<String> response) {
                Log.i(LOG_TAG, "success");
                Log.d("body==>", response.body().toString() + "");
                Log.d("isSuccessful==>", response.isSuccessful() + "");
                Log.d("message==>", response.message() + "");
                Log.d("raw==>", response.raw().toString() + "");
                Log.d("raw().networkResponse()", response.raw().networkResponse().toString() + "");
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(LOG_TAG, t.getMessage());
            }
        });
    }


    public void singleFileUpload(String filePath) {
        OkHttpClient okHttpClient = new OkHttpClient();
        OkHttpClient clientWith10sTimeout = okHttpClient.newBuilder()
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL_BASE)
                .addConverterFactory(GsonConverterFactory.create()) //new MultiPartConverter()
                .client(clientWith10sTimeout)
                .build();

        WebAPIService service = retrofit.create(WebAPIService.class); //here is the interface which you have created for the call service
        Map<String, RequestBody> maps = new HashMap<>();

        File file1 = new File(filePath);
        if (filePath != null && filePath.length() > 0) {
            if (file1.exists()) {
                okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"), file1);
                String filename = "imagePath"; //key for upload file like : imagePath0
                maps.put("file", requestFile);
            }
        }


        String descriptionString = " string request";//
        //hear is the your json request
        Call<String> call = service.postFile(maps, descriptionString);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call,
                                   Response<String> response) {
                Log.i(LOG_TAG, "success");
                Log.d("body==>", response.body().toString() + "");
                Log.d("isSuccessful==>", response.isSuccessful() + "");
                Log.d("message==>", response.message() + "");
                Log.d("raw==>", response.raw().toString() + "");
                Log.d("raw().networkResponse()", response.raw().networkResponse().toString() + "");
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(LOG_TAG, t.getMessage());
            }
        });
    }

    public String getRealPathFromUri(final Uri uri) { // function for file path from uri,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(mContext, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(mContext, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(mContext, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(mContext, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
}
