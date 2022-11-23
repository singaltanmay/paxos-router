/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.AttachedNetwork;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.NetworkSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("containers")
public class ContainerController {

  private static final Logger LOGGER = LogManager.getLogger(ContainerController.class);

  @Nullable
  private static String getContainerName(Container container) {
    final ImmutableList<String> containerNamesList = container.names();
    return containerNamesList != null && containerNamesList.size() > 0 ? containerNamesList.get(0).substring(1) : null;
  }

  @PostMapping("run")
  public void createContainer(@RequestParam(name = "role") String role, @RequestParam int count) {
    try {
      final DockerClient docker = DefaultDockerClient.fromEnv().build();

      // docker.pull("mysql:8.0.2");
      count = containers(role, count);

      while (count != 0) {
        final ContainerConfig containerConfig = ContainerConfig.builder().image("mysql:8.0.2").env("MYSQL_ROOT_PASSWORD=p$ssw0rd", "MYSQL_DATABASE=my_app_db").exposedPorts("3306").build();
        final ContainerCreation container = docker.createContainer(containerConfig, role + "-" + System.currentTimeMillis());
        docker.startContainer(container.id());
        docker.connectToNetwork(container.id(), "a31eb9eed1607753b3984ce8e556d7e95f9bf480cc2b07bdd61660ca59e6e1cf");
        count--;
      }
    } catch (DockerCertificateException | InterruptedException | DockerException e) {
      LOGGER.debug("Failed to create container:\n" + e.getMessage());
    }
  }

  @GetMapping("list")
  public List<ContainerEntity> runningContainers() {
    final DockerClient docker;
    try {
      docker = DefaultDockerClient.fromEnv().build();
      List<ContainerEntity> result = new ArrayList<>();
      List<Container> listContainers = null;
      listContainers = docker.listContainers();
      for (int i = 0; i < listContainers.size(); i++) {
        final com.spotify.docker.client.messages.Container container = listContainers.get(i);
        final String containerId = container.id();
        final String containerName = getContainerName(container);
        final NetworkSettings networkSettings = container.networkSettings();
        final AtomicReference<String> containerIpv4Addr = new AtomicReference<>();
        if (networkSettings != null) {
          final ImmutableMap<String, AttachedNetwork> networks = networkSettings.networks();
          if (networks != null) {
            networks.entrySet().stream().findFirst().ifPresent(it -> containerIpv4Addr.set(it.getValue().ipAddress()));
          }
        }
        final String containerPort = container.portsAsString();
        ContainerEntity containerDTO = new ContainerEntity(containerId, containerName, containerIpv4Addr.get(), containerPort);
        result.add(containerDTO);
      }
      return result;
    } catch (Exception e) {
      LOGGER.debug("Encountered error: " + e.getMessage());
    }
    return Collections.emptyList();
  }

  public int containers(String role, int count) throws DockerException, InterruptedException, DockerCertificateException {
    final DockerClient docker = DefaultDockerClient.fromEnv().build();
    List<String> ids = new ArrayList<>();
    for (com.spotify.docker.client.messages.Container container : docker.listContainers()) {
      final String containerName = getContainerName(container);
      if (containerName != null && !containerName.isBlank() && containerName.startsWith(role)) {
        count--;
        ids.add(container.id());
      }
    }
    while (count < 0) {
      stopContainer(ids.get(ids.size() - 1));
      ids.remove(ids.size() - 1);
      count++;
    }
    return count;
  }

  @PostMapping("stop/{containerId}")
  public void stopContainer(@PathVariable String containerId) throws DockerCertificateException, DockerException, InterruptedException {
    final DockerClient docker = DefaultDockerClient.fromEnv().build();
    docker.killContainer(containerId);
    docker.removeContainer(containerId);
  }

  public record ContainerEntity(String id, String name, String ipv4, String port) {

  }

}
