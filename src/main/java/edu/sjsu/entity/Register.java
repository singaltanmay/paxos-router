/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu.entity;

import edu.sjsu.Application;
import lombok.Getter;
import lombok.ToString;

@ToString
public class Register {

    @Getter
    Application.PAXOS_ROLES role;

    public Register(Application.PAXOS_ROLES role) {
        this.role = role;
    }

    public Register() {
        role = Application.PAXOS_ROLES.PROPOSER;
    }
}
