/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu.service;

import edu.sjsu.api.ProposerApi;
import edu.sjsu.api.RetrofitClient;
import edu.sjsu.entity.PaxosMessage;
import edu.sjsu.entity.RoleDescriptor;
import java.util.List;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@Service
public class RouterService {

  private void sendMessageToUri(PaxosMessage message, String uri) {
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

  public void broadcastToProposers(List<RoleDescriptor> proposers, PaxosMessage message) {
    for (RoleDescriptor proposer : proposers) {
      String uri = proposer.getUri();
      sendMessageToUri(message, uri);
    }
  }

  public void sendToProposer(PaxosMessage message, RoleDescriptor destination) {
    final String uri = destination.getUri();
    sendMessageToUri(message, uri);
  }
}
