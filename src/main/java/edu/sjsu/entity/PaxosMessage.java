/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
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

  public PaxosMessage(long id, PAXOS_MESSAGE_TYPE messageType, String value, String messageDestination) {
    this.id = id;
    this.messageType = messageType;
    this.value = value;
    this.messageDestination = messageDestination;
  }

  public PaxosMessage(long id, PAXOS_MESSAGE_TYPE messageType, String value) {
    this.id = id;
    this.messageType = messageType;
    this.value = value;
  }

  public enum PAXOS_MESSAGE_TYPE {PROMISE, PROPOSAL}

  public PaxosMessage(PAXOS_MESSAGE_TYPE messageType, String value) {
    this.messageType = messageType;
    this.value = value;
  }
}
