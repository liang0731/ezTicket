package com.ezticket.web.users.controller;

import com.ezticket.web.users.dto.RoleDTO;
import com.ezticket.web.users.pojo.Role;
import com.ezticket.web.users.pojo.Roleauthority;
import com.ezticket.web.users.repository.RoleRepository;
import com.ezticket.web.users.repository.RoleauthorityRepository;
import com.ezticket.web.users.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RoleauthorityRepository roleauthorityRepository;



    @GetMapping("/gr")
    public List<Role> getAllRole(){ return roleService.getAllRole(); };

    @GetMapping("/garf")
    public List<RoleDTO> getAllRolesWithFuncs() { return  roleService.getAllRolesWithFuncs(); }

    @PostMapping("/addNewRole")
    public Role createRole(@RequestBody Role newrole){
       newrole = roleRepository.save(newrole);
        //一創角色就把基礎功能加進去
        for (int i = 1;i <= 2 ;i++){
            Roleauthority roleauthority = new Roleauthority(newrole.getRoleno(),i);//後台首頁,個人資料編輯
            roleauthorityRepository.save(roleauthority);
        }
        System.out.println("insertThisRole: "+ newrole.toString());
        return newrole;
    }

    @PostMapping("/{roleno}/roleauth")
    public ResponseEntity<Role> toggleRoleStatus(@PathVariable("roleno") Integer roleno){
        Role updateRoleStatus = roleService.updateRoleStatus(roleno);
        System.out.println("Update Status roleno: "+roleno);
        return  ResponseEntity.ok(updateRoleStatus);
    }

}
