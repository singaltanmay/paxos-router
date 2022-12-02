/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu.api;

import edu.sjsu.entity.PaxosMessage;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface Api {

    @POST("message")
    Call<Void> sendMessage(@Body PaxosMessage message);

    @GET("value")
    Call<String> getLearnerValue();

}
