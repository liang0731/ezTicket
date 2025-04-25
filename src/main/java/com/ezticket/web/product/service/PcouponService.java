package com.ezticket.web.product.service;

import com.ezticket.web.product.dto.AddPcouponDTO;
import com.ezticket.web.product.dto.PcouponDTO;
import com.ezticket.web.product.pojo.Pcoupon;
import com.ezticket.web.product.pojo.Pfitcoupon;
import com.ezticket.web.product.pojo.PfitcouponPK;
import com.ezticket.web.product.repository.PcouponRepository;
import com.ezticket.web.product.repository.PfitcouponRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PcouponService {
    @Autowired
    private PcouponRepository pcouponRepository;
    @Autowired
    private PfitcouponRepository pfitcouponRepository;
    @Autowired
    private ModelMapper modelMapper;


    public PcouponDTO getPcouponByID(Integer id) {
        Pcoupon pcoupon = pcouponRepository.getReferenceById(id);
        return EntityToDTO(pcoupon);
    }
    public List<PcouponDTO> getPcouponsByName(String pcouponname) {
        return pcouponRepository.findByPcouponnameContaining(pcouponname)
                .stream()
                .map(this::EntityToDTO)
                .collect(Collectors.toList());
    }
    public List<PcouponDTO> getPcouponsByID(Integer id) {
        return pcouponRepository.findById(id)
                .stream()
                .map(this::EntityToDTO)
                .collect(Collectors.toList());
    }
    public PcouponDTO EntityToDTO(Pcoupon pcoupon){
//        pcoupon.getPcouponholdings().forEach(holding -> holding.setPcoupon(null));
        return modelMapper.map(pcoupon, PcouponDTO.class);
    }
    public List<PcouponDTO> getAllPcoupon(){
        return pcouponRepository.findAll()
                .stream()
                .map(this::EntityToDTO)
                .collect(Collectors.toList());
    }
    public boolean updateByID(Integer id, byte processStatus) {
        Pcoupon pcoupon = pcouponRepository.getReferenceById(id);
        pcoupon.setPcouponstatus(processStatus);
        pcouponRepository.save(pcoupon);
        checkPouconStatus();
        return true;
    }

    @Transactional
    public ResponseEntity<?> addPcouponError(AddPcouponDTO couponBody, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        } else {
            // 執行新增操作
            Pcoupon pcoupon = new Pcoupon();
            pcoupon.setPcouponholdings(null);
            pcoupon.setPcouponname(couponBody.getPcouponname());
            pcoupon.setPdiscount(couponBody.getPdiscount());
            pcoupon.setPreachprice(couponBody.getPreachprice());
            System.out.println(couponBody.getPcoupnsdate());
            System.out.println(couponBody.getPcoupnedate());
            pcoupon.setPcoupnsdate(couponBody.getPcoupnsdate());
            pcoupon.setPcoupnedate(couponBody.getPcoupnedate());
            Pcoupon savedPcoupon = pcouponRepository.save(pcoupon);
            Pfitcoupon pfitcoupon = new Pfitcoupon();
            PfitcouponPK pfitcouponPK = new PfitcouponPK();
            pfitcouponPK.setPcouponno(savedPcoupon.getPcouponno());
            pfitcouponPK.setProductno(couponBody.getProductno());
            pfitcoupon.setPfitcouponNo(pfitcouponPK);
            pfitcouponRepository.save(pfitcoupon);
            checkPouconStatus();
            return ResponseEntity.ok().build();
        }
    }



    @Transactional
    public ResponseEntity<?> editPcouponError(AddPcouponDTO couponBody, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        } else {
            Pcoupon pcoupon = pcouponRepository.getReferenceById(couponBody.getPcouponno());
            pcoupon.setPcouponname(couponBody.getPcouponname());
            pcoupon.setPdiscount(couponBody.getPdiscount());
            pcoupon.setPreachprice(couponBody.getPreachprice());
            pcoupon.setPcoupnsdate(couponBody.getPcoupnsdate());
            pcoupon.setPcoupnedate(couponBody.getPcoupnedate());
            pcouponRepository.save(pcoupon);

            // 搞不懂，為啥不能用更新的只能刪除重新新增
            // 先刪除目前的 pfitcoupon 記錄
            Pfitcoupon pfitcoupon = pfitcouponRepository.findByPcouponno(couponBody.getPcouponno());
            if (pfitcoupon != null) {
                pfitcouponRepository.deleteById(pfitcoupon.getPfitcouponNo());
            }
            // 新增一筆符合修改後條件的 pfitcoupon 記錄
            PfitcouponPK PfitcouponPK = new PfitcouponPK(couponBody.getPcouponno(), couponBody.getProductno());
            Pfitcoupon Pfitcoupon = new Pfitcoupon();
            Pfitcoupon.setPfitcouponNo(PfitcouponPK);

            pfitcouponRepository.save(Pfitcoupon);
            checkPouconStatus();
            return ResponseEntity.ok().build();
        }
    }


    // 這是一個 Spring 框架的定時任務設定，表示每小時的整點觸發一次，其中各個欄位的意義如下：
    // 第一個 * 代表秒數，表示不限定秒數。
    // 第二個 0 代表分鐘數，表示每小時的 0 分鐘觸發。
    // 第三個 * 代表小時數，表示每小時都要觸發。
    // 第四個 * 代表天數，表示不限定天數。
    // 第五個 * 代表月份，表示不限定月份。
    // 第六個 * 代表星期幾，表示不限定星期幾。

    // 每小時檢查Coupon使用狀態
    @Scheduled(cron = "0 0 * * * *")
    public void checkPouconStatus() {
        LocalDateTime today = LocalDateTime.now(ZoneId.of("Asia/Taipei"));
        List<Pcoupon> pcoupons = pcouponRepository.findAll();

        for (Pcoupon pcoupon : pcoupons) {
            //  2為手動關閉 需手動開啟
            if (pcoupon.getPcouponstatus() != 2){
                LocalDateTime start = pcoupon.getPcoupnsdate();
                LocalDateTime end = pcoupon.getPcoupnedate();
                byte status = 0;
                if (today.isEqual(start) || today.isEqual(end)) {
                    status = 1;
                } else if (today.isAfter(start) && today.isBefore(end)) {
                    status = 1;
                }
                pcoupon.setPcouponstatus(status);
                pcouponRepository.save(pcoupon);
            }
        }
    }


}
