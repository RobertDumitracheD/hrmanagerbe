package com.atosinterview.hrmanagerbe.persistance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HRPermission {

    private String permissionName;
    private String permissionDescription;
    private Integer roleID;
    private String roleName;
    private Integer includedRoleID;
    private String includedRoleName;
    private boolean addPermissionFlagIncludedRole;

}
