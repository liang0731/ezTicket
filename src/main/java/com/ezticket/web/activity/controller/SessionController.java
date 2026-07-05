package com.ezticket.web.activity.controller;

import com.ezticket.web.activity.dto.AimgtDto;
import com.ezticket.web.activity.dto.SessionDto;
import com.ezticket.web.activity.dto.TorderDto;
import com.ezticket.web.activity.pojo.Activity;
import com.ezticket.web.activity.pojo.Seats;
import com.ezticket.web.activity.pojo.Session;
import com.ezticket.web.activity.service.SessionService;
import com.google.gson.Gson;
import jakarta.servlet.http.Part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/Session")
public class SessionController {
    @Autowired
    private SessionService sessionService;
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    @GetMapping("/findAll")
    public List<SessionDto> findAll() {

        return sessionService.findAll();
    }

    @GetMapping("/findByactivityNo")
    public List<SessionDto> findByactivityNo(@RequestParam Integer activityNo) {

        return sessionService.findByactivityNo(activityNo);
    }

    //    Add by Shawn on 4/3
    @GetMapping("/ActSessions")
    public List<SessionDto> getAllSessionByActNo(@RequestParam Integer actNo) {
        return sessionService.getAllSessionByActNo(actNo);
    }

    //    Add by Shawn on 04/08
    @GetMapping("/updateTicketQTY")
    public Integer updateTicketQTY(@RequestParam Integer ticketChange, @RequestParam Integer sessionNo) {
        return sessionService.updateTicketQTY(ticketChange, sessionNo);
    }

    //    Add by Shawn on 04/12
    @GetMapping("/findById")
    public Optional<SessionDto> findById(@RequestParam Integer sessionNo) {

        return sessionService.findById(sessionNo);
    }
    @PostMapping("/saveSession")
    public Session saveSession(@RequestBody Session session) throws ParseException {

        return  sessionService.saveSession(session);
    }
    @PostMapping("/updateSession")
    public Session updateSession(@RequestBody Session session) {
        return sessionService.updateSession(session);
    }

    @PostMapping("deleteSession")
    public boolean deleteSessionTwo( @RequestParam Integer sessionNo){

        sessionService.deleteSessionTwo(sessionNo);
        return true;
    }

    // 當使用者進到選頁面時，將顯示每個區域的剩餘可售票券數
    @GetMapping("/getBlockToSellQty")
    public Map<Integer, Integer> getToSellTQty(@RequestParam Integer activityNo, @RequestParam Integer sessionNo){
        return sessionService.getToSellTQty(activityNo, sessionNo);
    }


}


