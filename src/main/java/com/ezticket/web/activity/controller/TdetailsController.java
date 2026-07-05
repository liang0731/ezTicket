package com.ezticket.web.activity.controller;


import com.ezticket.web.activity.dto.TdetailsDto;
import com.ezticket.web.activity.pojo.Tdetails;
import com.ezticket.web.activity.service.TdetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Tdetails")
public class TdetailsController {

    @Autowired
    private TdetailsService adetailsService;

    @GetMapping("/findAll")
    public List<TdetailsDto> findAll(){
        return  adetailsService.findAll();
    }

    // Add by Shawn on 04/19
    @GetMapping("/getById")
    public List<Tdetails> getDetailsByOrderNo(@RequestParam Integer torderNo) {
        return adetailsService.getByOrderNo(torderNo);
    }
}
