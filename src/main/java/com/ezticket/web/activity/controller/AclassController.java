package com.ezticket.web.activity.controller;

import com.ezticket.web.activity.dto.AclassDto;
import com.ezticket.web.activity.pojo.Aclass;
import com.ezticket.web.activity.service.AclassService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Aclass")
public class AclassController {
    @Autowired
    private AclassService aclassService;
    @GetMapping("/findAll")
    public List<AclassDto> findAll(){
        return aclassService.findAll();
    }
    @PostMapping("/saveAclass")
    public boolean saveAclass(@RequestBody Aclass jsonData) {

        aclassService.saveAclass(jsonData);
        return true;
    }
}
