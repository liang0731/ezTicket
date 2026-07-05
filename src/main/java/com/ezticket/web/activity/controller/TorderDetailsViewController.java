package com.ezticket.web.activity.controller;

import com.ezticket.web.activity.pojo.TorderDetailsView;
import com.ezticket.web.activity.pojo.TorderView;
import com.ezticket.web.activity.service.TorderDetailsViewService;
import com.ezticket.web.activity.service.TorderViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/TorderDetailsView")
public class TorderDetailsViewController {

    @Autowired
    private TorderDetailsViewService torderDetailsViewService;

    @GetMapping("/findAllBytorderNo")
    public List<TorderDetailsView> findAllBytorderNo(@RequestParam Integer torderNo) {

        return torderDetailsViewService.findAllBytorderNo(torderNo);
    }

    @GetMapping("/findBymemberNo")
    public List<TorderDetailsView> findBymemberNo(@RequestParam Integer memberNo) {

        return torderDetailsViewService.findBymemberNo(memberNo);
    }
}





