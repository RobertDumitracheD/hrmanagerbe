package com.atosinterview.hrmanagerbe.persistance;




import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HRRole {

    private Integer roleID;
    private String roleName;
    private String roleDescription;

    private List<HRPermission> permissions;

}
