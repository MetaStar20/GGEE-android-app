package com.example.ggee_vs.UploadService;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface WebAPIService {
    @Multipart
    @POST("upload")
    Call<String> postFile(@PartMap Map<String, RequestBody> Files, @Part("json") String description);
}