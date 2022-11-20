/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu.service;

import edu.sjsu.api.ProposerApi;
import edu.sjsu.api.RetrofitClient;
import edu.sjsu.entity.PaxosMessage;
import edu.sjsu.entity.RoleDescriptor;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.util.List;

@Service
public class RouterService {
    public void broadcastToProposers(List<RoleDescriptor> proposers, PaxosMessage message) {
        for (RoleDescriptor proposer : proposers) {
            String uri = proposer.getUri();
            Retrofit retrofit = RetrofitClient.getRetrofitInstance(uri);
            ProposerApi api = retrofit.create(ProposerApi.class);
            Call<Void> call = api.sendMessage(message);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    System.out.println("Sent message " + message + " with code : " + response.code());
                }

                @Override
                public void onFailure(Call<Void> call, Throwable throwable) {
                    System.err.println("Failed to send message " + message);
                }
            });
        }
    }
}
