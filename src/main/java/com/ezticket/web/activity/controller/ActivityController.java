package com.ezticket.web.activity.controller;

import com.ezticket.web.activity.dto.ActivityDto;
import com.ezticket.web.activity.pojo.Activity;
import com.ezticket.web.activity.service.ActivityService;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.servlet.http.Part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/Activity")

//ggggggg
public class ActivityController {
    @Autowired
    private ActivityService activityService;

    @GetMapping("/findAll")
    public List<ActivityDto> findAll(){

        return activityService.findAllByOrderByActivityNoDesc();
    }
    @GetMapping("/findByaName")
    public Optional<ActivityDto> findByAname(String aName){
        return activityService.findByaName(aName);
    }
    @GetMapping("/findByactivityNo")
    public Optional<Activity> findByactivityNo(Integer activityNo){
        return activityService.findByactivityNo(activityNo);
    }

    // Add by Shawn on 4/3
    @GetMapping("/afterSell")
    public List<Activity> getAllActAfterSell(){
        return activityService.getAllActAfterSell();
    }

    @GetMapping("/beforeSell")
    public List<Activity> getAllActBeforeSell(){
        return activityService.getAllActBeforeSell();
    }

    @GetMapping("/selling")
    public List<Activity> getAllActSelling(){
        return activityService.getAllActSelling();
    }

    @GetMapping("/findByActNo")
    public Optional<Activity> getActById(@RequestParam Integer actNo){
        return activityService.getActById(actNo);
    }

    @GetMapping("/ByActName")
    public List<Activity> getActByBlurActName(@RequestParam String actName) {
        System.out.println(actName);
        return activityService.getActByBlurActName(actName);
    };

    @GetMapping("/findAllByActivityNo/{activityNo}")
    public ResponseEntity<byte[]> getImage(@PathVariable Integer activityNo) {
        List<ActivityDto> activityDtos = activityService.findAllByActivityNo(activityNo);
        if (!activityDtos.isEmpty()) {
            ActivityDto activityDto = activityDtos.get(0);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(activityDto.getASeatsImg().length);
            return new ResponseEntity<>(activityDto.getASeatsImg(), headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/saveActivity")
    public Activity saveActivity(
            @RequestParam("aname") String aName,
            @RequestParam("aclass") Integer aClassNo,
            @RequestParam("aperformer") String performer,
            @RequestParam("host") Integer hostNo,
            @RequestParam("adiscrip") String aDiscrip,
            @RequestParam("anote") String aNote,
            @RequestParam("aticketremind") String aTicketRemind,
            @RequestParam("aplace") String aPlace,
            @RequestParam("aplaceaddress") String aPlaceAdress,
            @RequestParam("asdate") String aSDateString,
            @RequestParam("aedate") String aEDateString,
            @RequestParam("wetherseat") Integer wetherSeat,
            @RequestParam("aseatimg") Part image
    ) throws IOException, ParseException {
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd kk:mm");
        Date aSDate = format.parse(aSDateString);
        Date aEDate = format.parse(aEDateString);
        Activity activity = new Activity();
        activity.setAName(aName);
        activity.setAClassNo(aClassNo);
        activity.setPerformer(performer);
        activity.setHostNo(hostNo);
        activity.setADiscrip(aDiscrip);
        activity.setANote(aNote);
        activity.setATicketRemind(aTicketRemind);
        activity.setAPlace(aPlace);
        activity.setAPlaceAdress(aPlaceAdress);
        activity.setASDate(aSDate);
        activity.setAEDate(aEDate);
        activity.setWetherSeat(wetherSeat);
        activity.setAStatus(0);

        // Handle the uploaded image
        if (image != null) {
            byte[] imageBytes = image.getInputStream().readAllBytes();
            activity.setASeatsImg(imageBytes);
        }

       return activityService.saveActivity(activity);

    }
    @PostMapping(value = "/updateActivity/{activityNo}")
    public Activity updateActivity(
            @PathVariable Integer activityNo,
            @RequestParam(value = "aseatimg", required = false) Part aSeatImg,
            @RequestParam(value = "aname") String aName,
            @RequestParam(value = "aclass") Integer aClassNo,
            @RequestParam(value = "aperformer") String performer,
            @RequestParam(value = "host") Integer hostNo,
            @RequestParam(value = "adiscrip") String aDiscrip,
            @RequestParam(value = "anote", required = false) String aNote,
            @RequestParam(value = "aticketremind", required = false) String aTicketRemind,
            @RequestParam(value = "aplace") String aPlace,
            @RequestParam(value = "aplaceaddress") String aPlaceAddress,
            @RequestParam(value = "asdate") String aSDateStr,
            @RequestParam(value = "aedate") String aEDateStr,
            @RequestParam(value = "wetherseat") Integer wetherSeat) throws IOException {

        Activity activity = activityService.findActivityByNo(activityNo);



            if (aSeatImg != null) {
                byte[] imageBytes = aSeatImg.getInputStream().readAllBytes();
                activity.setASeatsImg(imageBytes);
            }
        activity.setAName(aName);
        activity.setAClassNo(aClassNo);
        activity.setPerformer(performer);
        activity.setHostNo(hostNo);
        activity.setADiscrip(aDiscrip);
        activity.setANote(aNote);
        activity.setATicketRemind(aTicketRemind);
        activity.setAPlace(aPlace);
        activity.setAPlaceAdress(aPlaceAddress);
        activity.setWetherSeat(wetherSeat);

        try {
            activity.setASDate(new SimpleDateFormat("yyyy/MM/dd kk:mm").parse(aSDateStr));
        } catch (ParseException e) {
        }

        try {
            activity.setAEDate(new SimpleDateFormat("yyyy/MM/dd kk:mm").parse(aEDateStr));
        } catch (ParseException e) {
        }


        return activityService.saveActivity(activity);
    }
    @PostMapping("/deleteActivity")
    public boolean deleteActivity(@RequestParam("activityNo") Integer activityNo, @RequestParam("aStatus") Integer aStatus) {
        activityService.deleteActivity(activityNo, aStatus);
    return true;
    }

    @PostMapping("/findByaClassNo")
    public List <Activity> findByaClassNo(@RequestParam Integer aclassNo){
        return activityService.findByaClassNo(aclassNo);
    }
}
