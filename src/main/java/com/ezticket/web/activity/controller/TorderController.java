package com.ezticket.web.activity.controller;

import com.ezticket.ecpay.service.OrderService;
import com.ezticket.web.activity.dto.AddTorderDTO;
import com.ezticket.web.activity.dto.TorderDto;
import com.ezticket.web.activity.pojo.Torder;
import com.ezticket.web.activity.service.TorderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("/Torder")
public class TorderController {
    @Autowired
    private TorderService torderService;
    @Autowired
    private OrderService orderService;

    @GetMapping("/findAll")
    public List<TorderDto> findAll() {

        return torderService.findAll();
    }

    @GetMapping("/findById")
    public Optional<Torder> findById(@RequestParam("torderNo") Integer torderNo) {
        return torderService.findById(torderNo);
    }

    // Add by Shawn on 04/17
    @PostMapping("/addTorder")
    @ResponseBody
    public String addTOrder(@RequestBody AddTorderDTO addTorderDTO) {
//        torderService.addTOrder(addTorderDTO);
//        return "";
        return orderService.ecpayTCheckout(torderService.addTOrder(addTorderDTO).getTorderNo());
    }

    // Add by Shawn on 04/19
    @GetMapping("/getById")
    public Torder getById(@RequestParam Integer torderNo) {
        return torderService.getById(torderNo);
    }

    @PostMapping("/deleteTorder")
    public void deleteTorder(@RequestParam Integer torderNo) {
        torderService.deleteTorder(torderNo);
    }

}
