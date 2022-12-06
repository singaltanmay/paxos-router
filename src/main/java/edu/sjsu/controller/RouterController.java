/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu.controller;

import edu.sjsu.Application;
import edu.sjsu.Application.PAXOS_ROLES;
import edu.sjsu.entity.PaxosMessage;
import edu.sjsu.entity.PaxosMessage.PAXOS_MESSAGE_TYPE;
import edu.sjsu.entity.Register;
import edu.sjsu.entity.RoleDescriptor;
import edu.sjsu.service.RouterService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("router")
@RestController
public class RouterController {

  private static final Logger LOGGER = LogManager.getLogger(RouterController.class);
  private final RouterService routerService;
  Map<Application.PAXOS_ROLES, List<RoleDescriptor>> roleRegistry = new HashMap<>();
  Map<String, Pair<String, Integer>> latencies = new HashMap<>();
  Map<String, RoleDescriptor> uuidRegistry = new HashMap<>();

  @Autowired
  public RouterController(RouterService routerService) {
    this.routerService = routerService;
  }

  @PostMapping("register")

  public ResponseEntity<String> register(HttpServletRequest request, @RequestBody Register register) {
    LOGGER.info("Incoming registration " + register);
    // Add registering entity to role list in roles map
    Application.PAXOS_ROLES role = register.getRole();
    final String uuid = register.getUuid();
    // UUID is invalid or already registered
    if (uuid == null || uuid.isBlank() || uuidRegistry.containsKey(uuid)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    // Add to roleList
    List<RoleDescriptor> roleList = roleRegistry.getOrDefault(role, new LinkedList<>());
    final RoleDescriptor roleDescriptor = new RoleDescriptor(role, request.getRemoteAddr(), uuid);
    roleList.add(roleDescriptor);
    roleRegistry.put(role, roleList);

    // Add to uuidRegistry
    uuidRegistry.put(uuid, roleDescriptor);

    return new ResponseEntity<>(uuid, HttpStatus.OK);
  }

  @PostMapping("message")
  public ResponseEntity<Void> incoming(@RequestBody PaxosMessage message) throws InterruptedException {
    // Add latency if configured
    final String messageSource = message.getMessageSource();
    final String messageDestination = message.getMessageDestination();
    if (latencies.containsKey(messageSource)) {
      final Pair<String, Integer> latencyPair = latencies.get(messageSource);
      final boolean isLatencyConfigured = latencyPair.getFirst().equals(messageDestination);
      final long millis = 1000L * (isLatencyConfigured ? latencyPair.getSecond() : 0);
      LOGGER.info("Sleeping for " + millis + " as per latency configured between " + messageSource + " and " + messageDestination);
      Thread.sleep(millis);
    }
    final PAXOS_MESSAGE_TYPE messageType = message.getMessageType();
    message.setPaxosRoleCounts(roleRegistry);
    LOGGER.info("Incoming " + messageType + " message " + message);
    switch (messageType) {
      case PROPOSAL -> {
        // A proposal needs to be broadcast to all acceptors
        LOGGER.info("Broadcasting PROPOSE message to all ACCEPTORS");
        final List<RoleDescriptor> acceptors = roleRegistry.get(PAXOS_ROLES.ACCEPTOR);
        routerService.broadcast(acceptors, message);
      }
      case PROMISE -> { // send promise to proposer
        LOGGER.info("Sending PROMISE message to original PROPOSER");
        String destination = messageDestination;
        final RoleDescriptor descriptor = uuidRegistry.get(destination);
        routerService.sendToDestination(message, descriptor);
      }
      case ACCEPT_REQUEST -> {
        LOGGER.info("Broadcasting ACCEPT_REQUEST message to all ACCEPTORS");
        final List<RoleDescriptor> acceptors = roleRegistry.get(PAXOS_ROLES.ACCEPTOR);
        routerService.broadcast(acceptors, message);
      }
      case ACCEPT -> {
        LOGGER.info("Sending ACCEPT message to original PROPOSER");
        String destination = messageDestination;
        final RoleDescriptor descriptor = uuidRegistry.get(destination);
        routerService.sendToDestination(message, descriptor);
        LOGGER.info("Broadcasting ACCEPT message to all LEARNERS");
        final List<RoleDescriptor> acceptors = roleRegistry.get(PAXOS_ROLES.LEARNER);
        routerService.broadcast(acceptors, message);
      }
    }
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @GetMapping("debug/uuidregistry")
  public ResponseEntity<Map<String, RoleDescriptor>> getUUIDRegistry() {
    return new ResponseEntity<>(uuidRegistry, HttpStatus.OK);
  }

  @PostMapping("latency")
  public ResponseEntity<Void> addLatency(String source, String destination, int latency) {
    if (!uuidRegistry.containsKey(source) || !uuidRegistry.containsKey(destination) || latency < 0) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    latencies.put(source, Pair.of(destination, latency));
    return ResponseEntity.ok().build();
  }

  @GetMapping("learned")
  public ResponseEntity<List<String>> getLearnedValues() throws InterruptedException {
    final List<RoleDescriptor> learners = roleRegistry.get(PAXOS_ROLES.LEARNER);
    Optional<List<String>> values = routerService.getLearnedValues(learners);
    return ResponseEntity.of(values);
  }

  @PostMapping("proposal/initiate")
  public ResponseEntity<Void> initiateProposal(String value, String proposerID) {
    final RoleDescriptor proposerDescriptor = uuidRegistry.get(proposerID);
    if (proposerDescriptor == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    routerService.initiateProposal(value, proposerDescriptor);
    return ResponseEntity.ok().build();
  }

}
