/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu.entity;

import edu.sjsu.Application;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
public class Register {

  @Getter
  Application.PAXOS_ROLES role;

  @Getter
  String uuid;

  public Register(Application.PAXOS_ROLES role, String uuid) {
    this.role = role;
    this.uuid = uuid;
  }

}
