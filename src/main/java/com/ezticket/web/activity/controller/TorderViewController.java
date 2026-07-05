package com.ezticket.web.activity.controller;

import com.ezticket.web.activity.pojo.TorderView;
import com.ezticket.web.activity.service.TorderViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/TorderView")
public class TorderViewController {

    @Autowired
    private TorderViewService torderViewService;

    @GetMapping("/findBymemberNo")
    public List<TorderView> findBymemberNo(@RequestParam Integer memberNo) {

        return torderViewService.findBymemberNo(memberNo);
    }


}





