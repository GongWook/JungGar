package com.example.junggar;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RetrofitService {

    // @GET( EndPoint-μμμμΉ(URI) )
    @GET("posts/{post}")
    Call<PostResult> getPosts(@Path("post") String post);

}
