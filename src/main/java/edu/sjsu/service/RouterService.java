/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu.service;

import edu.sjsu.api.Api;
import edu.sjsu.api.RetrofitClient;
import edu.sjsu.entity.PaxosMessage;
import edu.sjsu.entity.RoleDescriptor;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@Service
public class RouterService {

  private static final Logger LOGGER = LogManager.getLogger(RouterService.class);

  private void sendMessageToUri(PaxosMessage message, String uri) {
    Retrofit retrofit = RetrofitClient.getRetrofitInstance(uri);
    Api api = retrofit.create(Api.class);
    Call<Void> call = api.sendMessage(message);
    call.enqueue(new Callback<>() {
      @Override
      public void onResponse(Call<Void> call, Response<Void> response) {
        LOGGER.info("Sent message " + message + " with code : " + response.code());
      }

      @Override
      public void onFailure(Call<Void> call, Throwable throwable) {
        LOGGER.error("Failed to send message " + message);
      }
    });
  }

  public void broadcast(List<RoleDescriptor> nodes, PaxosMessage message) {
    if (nodes == null) {
      return;
    }
    for (RoleDescriptor node : nodes) {
      String uri = node.getUri();
      sendMessageToUri(message, uri);
    }
  }

  public void sendToDestination(PaxosMessage message, RoleDescriptor destination) {
    if (destination == null) {
      return;
    }
    final String uri = destination.getUri();
    sendMessageToUri(message, uri);
  }

  public Optional<List<String>> getLearnedValues(List<RoleDescriptor> learners) throws InterruptedException {
    final List<String> values = new LinkedList<>();
    final int[] callsQueued = {0};
    if (learners != null && !learners.isEmpty()) {
      for (final RoleDescriptor learner : learners) {
        final Retrofit retrofit = RetrofitClient.getRetrofitInstance(learner.getUri());
        final Api api = retrofit.create(Api.class);
        final Call<String> call = api.getLearnerValue();
        callsQueued[0]++;
        call.enqueue(new Callback<>() {
          @Override
          public void onResponse(Call<String> call, Response<String> response) {
            values.add(response.body());
            callsQueued[0]--;
          }

          @Override
          public void onFailure(Call<String> call, Throwable throwable) {
            LOGGER.error("Failed to get learned value from " + learner.getUuid());
            callsQueued[0]--;
          }
        });
      }
    }
    while (callsQueued[0] > 0) {
      Thread.sleep(500);
    }
    return Optional.of(values);
  }
}
