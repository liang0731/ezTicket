package com.ezticket.web.product.controller;

import com.ezticket.ecpay.service.FonPayService;
import com.ezticket.ecpay.service.OrderService;
import com.ezticket.web.product.dto.AddPorderDTO;
import com.ezticket.web.product.dto.PorderDTO;
import com.ezticket.web.product.dto.PorderDetailsDTO;
import com.ezticket.web.product.dto.PorderStatusDTO;
import com.ezticket.web.product.service.PorderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/porder")
public class PorderController {

    @Autowired private PorderService porderService;
    @Autowired private OrderService orderService;

    @Autowired private FonPayService fonPayService;

    @GetMapping("/list")
    public List<PorderDTO> getAllPorderlist(){
        return porderService.getAllPorder();
    }

    @GetMapping("/getporderbyid")
    public PorderDTO getDeliveryByID(@RequestParam Integer id){
        return porderService.getPorderByID(id);
    }
    @GetMapping("/GetPorderDetailsByID")
    public PorderDetailsDTO getDetailByID(@RequestParam Integer id){
        return porderService.getPorderDetailsByID(id);
    }
    @GetMapping("/getordersbyid")
    public List<PorderDTO> getPordersByID(@RequestParam Integer id){
        return porderService.getPordersByID(id);
    }
    @PostMapping("/updatestatusbyid")
    @ResponseBody
    public boolean updateStatusByID(@RequestBody PorderStatusDTO ps){
        porderService.updateByID(ps.getPorderno(), ps.getPprocessstatus());
        return true;
    }

    @PostMapping("/add")
    @ResponseBody
    public String addPorder(@RequestBody AddPorderDTO addPorderDTO) throws IOException {
        String paytype = addPorderDTO.getPaymenttype();
        if (paytype.equalsIgnoreCase("Ecpay")){
            return orderService.ecpayCheckout(porderService.addPorder(addPorderDTO).getPorderno());
        } else {
            return fonPayService.fonpayCheckout(porderService.addPorder(addPorderDTO).getPorderno(),paytype);
        }
    }
}
