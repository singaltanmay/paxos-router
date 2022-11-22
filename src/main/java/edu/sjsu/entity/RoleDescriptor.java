/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu.entity;

import edu.sjsu.Application;
import edu.sjsu.Application.PAXOS_ROLES;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class RoleDescriptor {

  @Getter
  @Setter
  Application.PAXOS_ROLES role;

  @Getter
  @Setter
  String uri;

  @Getter
  @Setter
  String uuid;

  public RoleDescriptor(PAXOS_ROLES role, String uri, String uuid) {
    this.role = role;
    this.uri = uri;
    this.uuid = uuid;
  }
}
