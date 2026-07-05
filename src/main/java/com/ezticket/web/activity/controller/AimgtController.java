package com.ezticket.web.activity.controller;

import com.ezticket.web.activity.dto.AimgtDto;
import com.ezticket.web.activity.pojo.Aimgt;
import com.ezticket.web.activity.service.AimgtService;
import jakarta.servlet.http.Part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/Aimgt")
public class AimgtController {
    @Autowired
    private AimgtService aimgtService;

    @GetMapping("/findAll")
    public List<AimgtDto> findAll(){

        return aimgtService.findAll();
    }

//    @GetMapping("/findAllByActivityNo/{activityNo}")
//    public ResponseEntity<byte[]> getImage(@PathVariable Integer activityNo) {
//        List<AimgtDto> aimgtDtos = aimgtService.findAllByActivityNo(activityNo);
//        if (!aimgtDtos.isEmpty()) {
//            AimgtDto aimgtDto = aimgtDtos.get(0);
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.IMAGE_JPEG);
//            headers.setContentLength(aimgtDto.getAimg().length);
//            return new ResponseEntity<>(aimgtDto.getAimg(), headers, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
    @GetMapping("/findAllByActivityNo")
    public List<Aimgt> findAllByActivityNo(@RequestParam Integer activityNo) {
        return aimgtService.findAllByActivityNo(activityNo);
    }


    @PostMapping("/saveAimgt")
    public ResponseEntity<String> saveAimgt(@RequestParam("activityno") Integer activityNo,
                                            @RequestParam("images") MultipartFile[] images,
                                            @RequestParam(value = "aimgmain", required = false) Integer mainImageIndex) throws IOException {

        // Instantiate a list of Aimgt objects
        List<Aimgt> aimgts = new ArrayList<>();

        // Convert mainImageIndex to an integer if it's not null or empty
        Integer mainIndex = null;
        if (mainImageIndex != null) {
            mainIndex = mainImageIndex;
        }

        // Loop through each image and create a new Aimgt object with the activityNo and image bytes
        for (int i = 0; i < images.length; i++) {
            byte[] imageBytes = images[i].getBytes();
            Aimgt aimgt = new Aimgt();
            aimgt.setActivityNo(activityNo);
            aimgt.setAimg(imageBytes);
            int j=0;
            if (i==j) {
                aimgt.setAimgMain(1);
            }else{
                aimgt.setAimgMain(0);
            }
            aimgts.add(aimgt);
        }

        // Save the list of Aimgt objects to the database using the service method
        aimgtService.saveAimgt(aimgts);

        return ResponseEntity.ok("Images uploaded successfully.");
    }


    @PostMapping("/updateAimgt")
    public ResponseEntity<String> updateAimgt(@RequestParam Integer aimgNo,
                                              @RequestParam(required = false) Part image) throws IOException {

        Aimgt aimgt = aimgtService.findById(aimgNo);
        if (image != null) {
            byte[] imageBytes = image.getInputStream().readAllBytes();
            aimgt.setAimg(imageBytes);
            aimgtService.save(aimgt);
        }
        if (aimgt != null) {
            return ResponseEntity.ok("Aimgt updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update Aimgt");
        }
    }


    @PostMapping("/updateNewAimgt")
    public ResponseEntity<String> updateNewAimgt(@RequestParam("activityno") Integer activityNo,
                                            @RequestParam("images") MultipartFile[] images,
                                            @RequestParam(value = "aimgmain", required = false) Integer mainImageIndex) throws IOException {

        // Instantiate a list of Aimgt objects
        List<Aimgt> aimgts = new ArrayList<>();

        // Convert mainImageIndex to an integer if it's not null or empty
        Integer mainIndex = null;
        if (mainImageIndex != null) {
            mainIndex = mainImageIndex;
        }

        // Loop through each image and create a new Aimgt object with the activityNo and image bytes
        for (int i = 0; i < images.length; i++) {
            byte[] imageBytes = images[i].getBytes();
            Aimgt aimgt = new Aimgt();
            aimgt.setActivityNo(activityNo);
            aimgt.setAimg(imageBytes);

            aimgt.setAimgMain(0);

            aimgts.add(aimgt);
        }

        // Save the list of Aimgt objects to the database using the service method
        aimgtService.saveAimgt(aimgts);

        return ResponseEntity.ok("Images uploaded successfully.");
    }



    @DeleteMapping("/deleteAimgt")
    public void deleteAimgt(@RequestParam("aimgNo") Integer aimgNo) {
        aimgtService.deleteAimgt(aimgNo);
    }
    }





