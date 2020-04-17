package com.example.socialmediaapp.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAux-y-Cc:APA91bFXuXd6jvnnZ2ZC3kGL4tEfkug7ruuxI1HrDimSboYCAL0ZrdxZnCD0y949pW6Xf15n28iDe3H7GesRtmqvOlh60XNLGVkgaCYcYjYeC3Gmg2UXJtzo5GK3ws9FTRh6FQqVp5r5"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
