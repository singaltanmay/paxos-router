/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public enum PAXOS_ROLES {PROPOSER, ACCEPTOR, LEARNER}

}