/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.HashMap;
import java.util.Map;

public class RetrofitClient {

    private static final Map<String, Retrofit> retrofitMap = new HashMap<>();

    public static Retrofit getRetrofitInstance(String host) {
        Retrofit retrofit = retrofitMap.get(host);
        if (retrofit == null) {
            String PAXOS_ROLE_APPLICATION_PORT = "8080";
            retrofit = new retrofit2.Retrofit.Builder().baseUrl("http://" + host + ":" + PAXOS_ROLE_APPLICATION_PORT).addConverterFactory(GsonConverterFactory.create()).build();
            retrofitMap.put(host, retrofit);
        }
        return retrofit;
    }


}
