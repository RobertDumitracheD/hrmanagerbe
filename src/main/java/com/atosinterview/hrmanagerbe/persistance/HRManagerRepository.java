package com.atosinterview.hrmanagerbe.persistance;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.sources.tables.records.IncludedrolesRecord;
import org.jooq.sources.tables.records.PermissionsRecord;
import org.jooq.sources.tables.records.RolesRecord;
import org.jooq.sources.tables.records.RolescompleteviewRecord;
import org.simpleflatmapper.jdbc.JdbcMapper;
import org.simpleflatmapper.jdbc.JdbcMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.sources.tables.Includedroles.INCLUDEDROLES;
import static org.jooq.sources.tables.Permissions.PERMISSIONS;
import static org.jooq.sources.tables.Roles.ROLES;
import static org.jooq.sources.tables.Rolescompleteview.ROLESCOMPLETEVIEW;


@Repository
public class HRManagerRepository {


    private final DSLContext jooq;

    //     JDBC mappers are JdbcMapperFactory thread-safe
    private final JdbcMapper<HRRole> jdbcMapper;


    //roleId -pk from role table, permissionsId pk from permissions
    @Autowired
    public HRManagerRepository(DSLContext dslContext) {

        this.jdbcMapper = JdbcMapperFactory
                .newInstance()
                .addKeys("roleID", "permissionsID")
                .newMapper(HRRole.class);
        this.jooq = dslContext;

    }

    @Transactional
    public List<Map<String, Object>> findRolesWithPermissionsAndIncludedRoles() {

        return jooq.selectDistinct(ROLESCOMPLETEVIEW.ROLENAME,
                ROLESCOMPLETEVIEW.ROLEDESCRIPTION,
                ROLESCOMPLETEVIEW.ROLEPERMISSION,
                ROLESCOMPLETEVIEW.INCLUDEDROLENAME,
                ROLESCOMPLETEVIEW.INCLUDEDROLEPERMISSION
                ).from(ROLESCOMPLETEVIEW).fetchMaps();
    }

    public  void insertInCompleteView(String roleName){
        StringBuilder roleDescription = extractRoleDescription(roleName);

        StringBuilder permissionName = extractPermissionName(roleName);

        StringBuilder includedRoleName = extractIncludedRoles(roleName);

        StringBuilder includedRolePermission = extractIncludedRolePermission(includedRoleName);

        if(findCompleteViewByRole(roleName).isEmpty()){
            jooq.insertInto(ROLESCOMPLETEVIEW)
                    .set(ROLESCOMPLETEVIEW.ROLENAME, roleName)
                    .set(ROLESCOMPLETEVIEW.ROLEDESCRIPTION, roleDescription.toString())
                    .set(ROLESCOMPLETEVIEW.ROLEPERMISSION, permissionName.toString())
                    .set(ROLESCOMPLETEVIEW.INCLUDEDROLENAME, includedRoleName.toString())
                    .set(ROLESCOMPLETEVIEW.INCLUDEDROLEPERMISSION, includedRolePermission.toString())
                    .execute();

        } else if(!findCompleteViewByRole(roleName).isEmpty()){
            jooq.update(ROLESCOMPLETEVIEW)
                    .set(ROLESCOMPLETEVIEW.ROLEPERMISSION, permissionName.toString())
                    .set(ROLESCOMPLETEVIEW.INCLUDEDROLENAME, includedRoleName.toString())
                    .set(ROLESCOMPLETEVIEW.INCLUDEDROLEPERMISSION, includedRolePermission.toString())
                    .execute();

        }

    }

    private Result<RolescompleteviewRecord> findCompleteViewByRole(String roleName) {
        return jooq.selectFrom(ROLESCOMPLETEVIEW).where(ROLESCOMPLETEVIEW.ROLENAME.eq(roleName)).fetch();
    }

    private StringBuilder extractRoleDescription(String roleName) {
        StringBuilder roleDescription = new StringBuilder(jooq.selectDistinct(ROLES.ROLEDESCRIPTION).from(ROLES)
                .where(ROLES.ROLENAME.eq(roleName)).fetchMaps().stream().map(stringObjectMap -> stringObjectMap.get("roleDescription")).collect(Collectors.toList()).toString());
        if (roleDescription.length() > 3) {
            roleDescription.deleteCharAt(0);
            roleDescription.deleteCharAt(roleDescription.length() - 1);
        }
        return roleDescription;
    }

    private StringBuilder extractPermissionName(String roleName) {
        StringBuilder permissionName = new StringBuilder(jooq.selectDistinct(PERMISSIONS.PERMISSIONNAME).from(PERMISSIONS)
                .where(PERMISSIONS.ROLENAME.eq(roleName)).fetchMaps().stream().map(stringObjectMap -> stringObjectMap.get("permissionName"))
                .collect(Collectors.toList()).toString());
        if (permissionName.length() > 3) {
            permissionName.deleteCharAt(0);
            permissionName.deleteCharAt(permissionName.length() - 1);
        }
        return permissionName;
    }

    private StringBuilder extractIncludedRoles(String roleName) {
        StringBuilder includedRoleName = new StringBuilder(jooq.selectDistinct(INCLUDEDROLES.INCLUDEDROLENAME).from(INCLUDEDROLES)
                .where(INCLUDEDROLES.ROLENAME.eq(roleName)).fetchMaps().stream().map(stringObjectMap -> stringObjectMap.get("includedRoleName")).collect(Collectors.toList()).toString());
        if (includedRoleName.length() > 3) {
            includedRoleName.deleteCharAt(0);
            includedRoleName.deleteCharAt(includedRoleName.length() - 1);
        }
        return includedRoleName;
    }

    private StringBuilder extractIncludedRolePermission(StringBuilder includedRoleName) {
        StringBuilder includedRolePermission = new StringBuilder();
            for (String s : includedRoleName.toString().split(",")) {
                List<Object> permissionName1 = jooq.selectDistinct(PERMISSIONS.PERMISSIONNAME).from(PERMISSIONS)
                        .where(PERMISSIONS.INCLUDEDROLENAME.eq(s)).fetchMaps().stream().map(stringObjectMap -> stringObjectMap.get("permissionName")).collect(Collectors.toList());
                for (Object o : permissionName1) {
                    includedRolePermission.append(o.toString() + ",");
                }
            }
            if(includedRolePermission.length()>3) {
                includedRolePermission.deleteCharAt(includedRolePermission.length() - 1).toString();
            }

        return includedRolePermission;
    }


    //find ifExists role
    @Transactional
    public Result<RolesRecord> findRoleByName(String roleName) {

        Result<RolesRecord> result = jooq.selectFrom(ROLES).where(ROLES.ROLENAME.eq(roleName)).fetch();
        return result;
    }

    //find ifExists included-role
    @Transactional
    public Result<IncludedrolesRecord> findIncludedRoleByName(String roleName) {

        Result<IncludedrolesRecord> result = jooq.selectFrom(INCLUDEDROLES).where(INCLUDEDROLES.INCLUDEDROLENAME.eq(roleName)).fetch();
        return result;
    }

    //get roleID for main role
    @Transactional
    public int getRoleID(String roleName) {

        int roleID = (int) jooq.select(ROLES.ROLEID).from(ROLES).where(ROLES.ROLENAME.eq(roleName)).fetchMaps().stream()
                .filter(stringObjectMap -> stringObjectMap.containsKey("roleID")).findFirst().get().get("roleID");


        return roleID;
    }

    //get roleID for included-role
    @Transactional
    public int getIncludedRoleID(String includedRoleName) {

        int roleID = (int) jooq.select(INCLUDEDROLES.INCLUDEDROLEID).from(INCLUDEDROLES).where(INCLUDEDROLES.INCLUDEDROLENAME
                        .eq(includedRoleName)).fetchMaps().stream()
                .filter(stringObjectMap -> stringObjectMap.containsKey("includedRoleID"))
                .findFirst().get().get("includedRoleID");


        return roleID;
    }

    //find ifExists permission for querry role
    @Transactional
    public Result<PermissionsRecord> findPermissionByRoleName(String roleName, String permissionName, String hrRoleFlag) {
        Result<PermissionsRecord> result = null;
        if (hrRoleFlag.contains("roleName")) {
            result = jooq.selectFrom(PERMISSIONS).where(PERMISSIONS.ROLENAME.eq(roleName).and(PERMISSIONS.PERMISSIONNAME.eq(permissionName))).fetch();
        } else if (hrRoleFlag.contains("includedRoleName")) {
            result = jooq.selectFrom(PERMISSIONS).where(PERMISSIONS.INCLUDEDROLENAME.eq(roleName).and(PERMISSIONS.PERMISSIONNAME.eq(permissionName))).fetch();
        }

        return result;
    }

    //find ifExists included-role for querry main-role
    @Transactional
    public Result<IncludedrolesRecord> findIncludedMainRoleByName(String roleName, String includedRoleName) {
        Result<IncludedrolesRecord> result = jooq.selectFrom(INCLUDEDROLES).where(INCLUDEDROLES.ROLENAME.eq(roleName)
                .and(INCLUDEDROLES.INCLUDEDROLENAME.eq(includedRoleName))).fetch();
        return result;
    }

    @Transactional
    public void addRole(HRRole newHRRole) {
        jooq.insertInto(ROLES)
                .set(ROLES.ROLENAME, newHRRole.getRoleName())
                .set(ROLES.ROLEDESCRIPTION, newHRRole.getRoleDescription())
                .execute();
    }

    @Transactional
    public boolean addPermission(HRPermission newHRPermission, int roleID, String roleName, String hrRoleFlag, boolean addPermissionFlagIncludedRole) {
        if (hrRoleFlag.contains("roleName")) {
            jooq.insertInto(PERMISSIONS)
                    .set(PERMISSIONS.PERMISSIONNAME, newHRPermission.getPermissionName())
                    .set(PERMISSIONS.PERMISSIONDESCRIPTION, newHRPermission.getPermissionDescription())
                    .set(PERMISSIONS.ROLENAME, roleName)
                    .set(PERMISSIONS.ROLEID, roleID)
                    .execute();
        } else if (hrRoleFlag.contains("includedRoleName") && addPermissionFlagIncludedRole) {
            jooq.insertInto(PERMISSIONS)
                    .set(PERMISSIONS.PERMISSIONNAME, newHRPermission.getPermissionName())
                    .set(PERMISSIONS.PERMISSIONDESCRIPTION, newHRPermission.getPermissionDescription())
                    .set(PERMISSIONS.INCLUDEDROLENAME, roleName)
                    .set(PERMISSIONS.INCLUDEDROLEID, roleID)
                    .execute();
        }
        return addPermissionFlagIncludedRole;
    }

    @Transactional
    public void addIncludedRole(HRIncludedRole newHrIncludedRole, int roleID, String roleName) {

        jooq.insertInto(INCLUDEDROLES)
                .set(INCLUDEDROLES.INCLUDEDROLENAME, newHrIncludedRole.getIncludedRoleName())
                .set(INCLUDEDROLES.INCLUDEDROLEDESCRIPTION, newHrIncludedRole.getIncludedRoleDescription())
                .set(INCLUDEDROLES.ROLEID, roleID)
                .set(INCLUDEDROLES.ROLENAME, roleName)
                .execute();
    }

    @Transactional
    public void addIncludedRoleWithoutMainRole(HRIncludedRole hrIncludedRole) {
        jooq.insertInto(INCLUDEDROLES)
                .set(INCLUDEDROLES.INCLUDEDROLENAME, hrIncludedRole.getIncludedRoleName())
                .set(INCLUDEDROLES.INCLUDEDROLEDESCRIPTION, hrIncludedRole.getIncludedRoleDescription())
                .execute();

    }
//
//    private String extractColumn(){
//
//
//
//    }


    public void updatePermission(String permissionName, String roleName, String hrRoleFlag) {
        String permissionConcat;
        if (hrRoleFlag.contains("roleName")) {

//            permissionConcat = jooq.select(PERMISSIONS.PERMISSIONNAME)
//                    .from(PERMISSIONS)
//                    .where(PERMISSIONS.ROLENAME.eq(roleName))
//                    .fetchMaps().stream()
//                    .filter(stringObjectMap -> stringObjectMap.containsKey("permissionName")).findFirst().get().get("permissionName").toString();

            jooq.update(PERMISSIONS)
                    .set(PERMISSIONS.ROLENAME, roleName)
                    .where(PERMISSIONS.PERMISSIONNAME.eq(permissionName))
                    .execute();

        } else {

//            permissionConcat = jooq.select(PERMISSIONS.PERMISSIONNAME)
//                    .from(PERMISSIONS)
//                    .where(PERMISSIONS.INCLUDEDROLENAME.eq(roleName))
//                    .fetchMaps().stream()
//                    .filter(stringObjectMap -> stringObjectMap.containsKey("permissionName")).findFirst().get().get("permissionName").toString();

            jooq.update(PERMISSIONS)
                    .set(PERMISSIONS.INCLUDEDROLENAME, roleName)
                    .where(PERMISSIONS.PERMISSIONNAME.eq(permissionName))
                    .execute();
        }
    }

    public void alterIncludedRole(String includedRole, String roleName) {
        jooq.update(INCLUDEDROLES)
                .set(INCLUDEDROLES.ROLENAME, roleName)
                .where(INCLUDEDROLES.INCLUDEDROLENAME.eq(includedRole))
                .execute();
    }


//    private List<HRRole> transformResultIntoRoles(Results query) {
//
//        try (ResultSet rs = (ResultSet) query.stream()) {
//            return jdbcMapper.stream(rs).collect(Collectors.toList());
//        } catch (SQLException e) {
//            throw new DataQueryException("Cannot transform query", e);
//        }
//
//    }


}
