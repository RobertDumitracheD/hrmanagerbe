package com.atosinterview.hrmanagerbe.persistance;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HRIncludedRole {

    private String includedRoleID;
    private String includedRoleName;
    private String includedRoleDescription;
    private Integer roleID;
    private String roleName;

}
