/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  public enum PAXOS_ROLES {PROPOSER, ACCEPTOR, LEARNER}

}