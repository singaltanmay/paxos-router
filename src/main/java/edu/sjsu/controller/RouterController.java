/*
 * SJSU CS 218 FALL 2022 TEAM 5
 */

package edu.sjsu.controller;

import edu.sjsu.Application;
import edu.sjsu.entity.PaxosMessage;
import edu.sjsu.entity.Register;
import edu.sjsu.entity.RoleDescriptor;
import edu.sjsu.service.RouterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RequestMapping("router")
@RestController
public class RouterController {
    private final RouterService routerService;
    Map<Application.PAXOS_ROLES, List<RoleDescriptor>> roleRegistry = new HashMap<>();

    @Autowired
    public RouterController(RouterService routerService) {
        this.routerService = routerService;
    }

    @PostMapping("register")

    public ResponseEntity<Void> register(HttpServletRequest request, @RequestBody Register register) {
        System.out.println("Incoming registration " + register);
        // Add registering entity to role list in roles map
        Application.PAXOS_ROLES role = register.getRole();
        List<RoleDescriptor> roleList = roleRegistry.getOrDefault(role, new LinkedList<>());
        roleList.add(new RoleDescriptor(role, request.getRemoteAddr()));
        roleRegistry.put(role, roleList);
        System.out.println(roleList);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("message")
    public ResponseEntity<Void> incoming(@RequestBody PaxosMessage message) {
        System.out.println("Incoming message " + message);
        List<RoleDescriptor> proposers = roleRegistry.get(Application.PAXOS_ROLES.PROPOSER);
        routerService.broadcastToProposers(proposers, message);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
