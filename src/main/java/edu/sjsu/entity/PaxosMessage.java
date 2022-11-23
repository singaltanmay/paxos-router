/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu.entity;

import edu.sjsu.Application.PAXOS_ROLES;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
public class PaxosMessage {

  @Getter
  @Setter
  long id;

  @Getter
  @Setter
  PAXOS_MESSAGE_TYPE messageType;

  @Getter
  @Setter
  String value;

  @Getter
  String messageSource;

  @Getter
  String messageDestination;

  @Getter
  @Setter
  int numProposers;

  @Getter
  @Setter
  int numAcceptors;

  @Getter
  @Setter
  int numLearners;

  @Getter
  @Setter
  boolean ignore;

  public void setPaxosRoleCounts(Map<PAXOS_ROLES, List<RoleDescriptor>> roleRegistry) {
    final List<RoleDescriptor> proposersList = roleRegistry.get(PAXOS_ROLES.PROPOSER);
    this.numProposers = proposersList == null ? 0 : proposersList.size();
    final List<RoleDescriptor> acceptorsList = roleRegistry.get(PAXOS_ROLES.ACCEPTOR);
    this.numAcceptors = acceptorsList == null ? 0 : acceptorsList.size();
    final List<RoleDescriptor> learnersList = roleRegistry.get(PAXOS_ROLES.LEARNER);
    this.numLearners = learnersList == null ? 0 : learnersList.size();
  }

  public enum PAXOS_MESSAGE_TYPE {PROMISE, ACCEPT_REQUEST, ACCEPT, PROPOSAL}
}
