package com.example.imagepostroom;



import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitAPI {
    @Multipart
    @POST("post")
    Call<UploadResponse> uploadData(
            @Part MultipartBody.Part image,
            @Part("name") RequestBody name,
            @Part("age") RequestBody age
    );
}
