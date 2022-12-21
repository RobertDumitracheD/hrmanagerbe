package com.atosinterview.hrmanagerbe.service;


import com.atosinterview.hrmanagerbe.persistance.HRIncludedRole;
import com.atosinterview.hrmanagerbe.persistance.HRManagerRepository;
import com.atosinterview.hrmanagerbe.persistance.HRPermission;
import com.atosinterview.hrmanagerbe.persistance.HRRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HRManagerService {
    private static final String INCLUDED_ROLE_FLAG = "includedRoleName";
    private static final String ROLE_FLAG = "roleName";
    private HRManagerRepository hrManagerRepository;

    public HRManagerService(HRManagerRepository hrManagerRepository) {
        this.hrManagerRepository = hrManagerRepository;
    }

    public List<Map<String, Object>> findRolesWithPermissionsAndIncludedRoles() {
        return hrManagerRepository.findRolesWithPermissionsAndIncludedRoles();

    }

    public void insertInCompleteView(String roleName) {
        hrManagerRepository.insertInCompleteView( roleName);
    }

    public boolean addRole(Map<String, Object> payload) {
        return validateRole(payload);
    }


    private boolean validateRole(Map<String, Object> payload) {
        boolean validation = false;
        ObjectMapper mapper = new ObjectMapper();
        HRRole hrRole = null;
        HRPermission hrPermission = null;
        HRIncludedRole hrIncludedRole = null;
        //parsing the payload to our objects
        for (String s : payload.keySet()) {
            if (s.equals("hrrole")) {
                hrRole = mapper.convertValue(payload.get(s), HRRole.class);
            }
            if (s.equals("hrpermission")) {
                hrPermission = mapper.convertValue(payload.get(s), HRPermission.class);
            }
            if (s.equals("hrincludedrole")) {
                hrIncludedRole = mapper.convertValue(payload.get(s), HRIncludedRole.class);
            }
        }

        if (hrRole != null  && hrManagerRepository.findRoleByName(hrRole.getRoleName()).isEmpty()){
            hrManagerRepository.addRole(hrRole);

            if (hrPermission != null && hrManagerRepository.findPermissionByRoleName(hrRole.getRoleName(),hrPermission.getPermissionName(), ROLE_FLAG).isEmpty()) {
                //add permission to existing role
                hrManagerRepository.addPermission(hrPermission, hrManagerRepository.getRoleID(hrRole.getRoleName()),
                        hrRole.getRoleName(), ROLE_FLAG, hrPermission.isAddPermissionFlagIncludedRole());
            }
            if (hrIncludedRole != null && hrManagerRepository.findIncludedMainRoleByName(hrRole.getRoleName(), hrIncludedRole.getIncludedRoleName()).isEmpty()) {
                //add includedRole to existing role
                hrManagerRepository.addIncludedRole(hrIncludedRole, hrManagerRepository.getRoleID(hrRole.getRoleName()), hrRole.getRoleName());
                if (hrPermission != null && hrManagerRepository.findPermissionByRoleName(hrIncludedRole.getIncludedRoleName(), hrPermission.getPermissionName(),INCLUDED_ROLE_FLAG).isEmpty()
                        && !hrPermission.isAddPermissionFlagIncludedRole()) {
                    hrManagerRepository.addPermission(hrPermission, hrManagerRepository.getIncludedRoleID(hrIncludedRole.getIncludedRoleName()),
                            hrIncludedRole.getIncludedRoleName(), INCLUDED_ROLE_FLAG, !hrPermission.isAddPermissionFlagIncludedRole());
                }
            }
            validation = true;

        }else if (hrRole != null && !hrManagerRepository.findRoleByName(hrRole.getRoleName()).isEmpty()) {

            if (hrPermission != null && hrManagerRepository.findPermissionByRoleName(hrRole.getRoleName(), hrPermission.getPermissionName(), ROLE_FLAG).isEmpty() ) {
                //add permission to existing role
                hrManagerRepository.addPermission(hrPermission, hrManagerRepository.getRoleID(hrRole.getRoleName()), hrRole.getRoleName(),
                        ROLE_FLAG, hrPermission.isAddPermissionFlagIncludedRole());
            }
            if (hrIncludedRole != null && hrManagerRepository.findIncludedMainRoleByName(hrRole.getRoleName(), hrIncludedRole.getIncludedRoleName()).isEmpty()) {
                //add includedRole to existing role
                hrManagerRepository.addIncludedRole(hrIncludedRole, hrManagerRepository.getRoleID(hrRole.getRoleName()), hrRole.getRoleName());
                if (hrPermission != null && hrManagerRepository.findPermissionByRoleName(hrIncludedRole.getIncludedRoleName(), hrPermission.getPermissionName(), INCLUDED_ROLE_FLAG).isEmpty()
                        && !hrPermission.isAddPermissionFlagIncludedRole()) {
                    //if I want to have different permission from main role to second role i can add && hrManagerRepository.findPermissionByRoleName(hrRole.getRoleName(), ROLE_FLAG).isEmpty()
                    //but every role should have it's permission
                    hrManagerRepository.addPermission(hrPermission, hrManagerRepository.getIncludedRoleID(hrIncludedRole.getIncludedRoleName()),
                            hrIncludedRole.getIncludedRoleName(), INCLUDED_ROLE_FLAG, hrPermission.isAddPermissionFlagIncludedRole());
                }
            }
            validation = true;


        } else if (hrRole == null) {
            //add included role without any main role with it's permission
            if (hrIncludedRole != null && hrManagerRepository.findIncludedRoleByName(hrIncludedRole.getIncludedRoleName()).isEmpty()) {
                hrManagerRepository.addIncludedRoleWithoutMainRole(hrIncludedRole);
                if (hrPermission != null && hrManagerRepository.findPermissionByRoleName(hrIncludedRole.getIncludedRoleName(), hrPermission.getPermissionName(), INCLUDED_ROLE_FLAG).isEmpty()) {
                    hrManagerRepository.addPermission(hrPermission, hrManagerRepository.getIncludedRoleID(hrIncludedRole.getIncludedRoleName()), hrIncludedRole.getIncludedRoleName(), INCLUDED_ROLE_FLAG, hrPermission.isAddPermissionFlagIncludedRole());
                }
//                validation = true;
//            } else if (hrIncludedRole != null && !hrManagerRepository.findIncludedRoleByName(hrIncludedRole.getIncludedRoleName()).isEmpty()) {
//                if (hrPermission != null && hrManagerRepository.findPermissionByRoleName(hrIncludedRole.getIncludedRoleName(), INCLUDED_ROLE_FLAG).isEmpty()) {
//                    hrManagerRepository.updatePermission(hrPermission.getPermissionName(), hrIncludedRole.getIncludedRoleName(), INCLUDED_ROLE_FLAG);
//                }
                validation = true;
            }


        }
        return validation;
    }
}