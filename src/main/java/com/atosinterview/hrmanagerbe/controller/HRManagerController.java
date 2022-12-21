package com.atosinterview.hrmanagerbe.controller;

import com.atosinterview.hrmanagerbe.exception.CreatingRoleException;
import com.atosinterview.hrmanagerbe.exception.ErrorRetrievingDataException;
import com.atosinterview.hrmanagerbe.service.HRManagerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/hrmanager")
@RestController
public class HRManagerController {

    private HRManagerService hrManagerService ;

    Logger logger = LogManager.getLogger(HRManagerController.class);


    public HRManagerController(HRManagerService hrManagerService) {
        this.hrManagerService = hrManagerService;
    }

    //Get full list of roles
    @GetMapping("/roles")
    public ResponseEntity<List<Map<String, Object>>> getRoles(){

        try {
            return new ResponseEntity<>(hrManagerService.findRolesWithPermissionsAndIncludedRoles(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving the roles from db");
            throw new ErrorRetrievingDataException(e.getMessage());
        }
    }

    //endpoint for adding role - parametrized , can accept 1-or multiple objects as parameter
    @PostMapping("/add-role")
    public ResponseEntity<Boolean> addCompleteRole(@RequestBody Map<String, Object> payload){

        try {

            return new ResponseEntity<>(hrManagerService.addRole(payload), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error adding new role");
            throw new CreatingRoleException(e.getMessage());
        }
    }

    @PostMapping("/roles/completeView")
    public ResponseEntity insertInCompleteView(@RequestBody String roleName){

        try {
            hrManagerService.insertInCompleteView(roleName);
            return  new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error inserting in complete view");
            throw new ErrorRetrievingDataException(e.getMessage());
        }
    }
}
