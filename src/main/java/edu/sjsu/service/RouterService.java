/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu.service;

import edu.sjsu.api.Api;
import edu.sjsu.api.RetrofitClient;
import edu.sjsu.entity.PaxosMessage;
import edu.sjsu.entity.RoleDescriptor;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import okhttp3.ResponseBody;
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
    if (learners != null && !learners.isEmpty()) {
      for (final RoleDescriptor learner : learners) {
        final Retrofit retrofit = RetrofitClient.getRetrofitInstance(learner.getUri());
        final Api api = retrofit.create(Api.class);
        final Call<ResponseBody> call = api.getLearnerValue();

        try {
          final Response<ResponseBody> execute = call.execute();
          if (execute.isSuccessful()) {
            if (execute.body() != null) {
//              final String string = execute.body().string();
//              final String[] split = string.split("\"");
//              if (split.length > 4) {
//                values.add(split[3]);
//              }
              final String string = execute.body().string();
              if (!string.isBlank()) {
                values.add(string);
              }
            }
          } else {
            LOGGER.error("Failed to get learned value from " + learner.getUuid());
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return Optional.of(values);
  }

  public void initiateProposal(String value, RoleDescriptor proposer) {

    final Retrofit retrofit = RetrofitClient.getRetrofitInstance(proposer.getUri());
    final Api api = retrofit.create(Api.class);
    final Call<Void> call = api.initiateProposal(value);
    call.enqueue(new Callback<Void>() {
      @Override
      public void onResponse(Call<Void> call, Response<Void> response) {
        LOGGER.info("Successfully initiated proposal for value " + value);
      }

      @Override
      public void onFailure(Call<Void> call, Throwable throwable) {
        LOGGER.error("Failed to initiate proposal for value " + value);
      }
    });

  }
}
