package com.ezticket.web.activity.controller;

import com.ezticket.web.activity.pojo.CollectRedis;
import com.ezticket.web.activity.pojo.Torder;
import com.ezticket.web.activity.repository.CollectRedisRepository;
import com.ezticket.web.activity.repository.TorderRepository;
import com.ezticket.web.activity.service.CollectCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/EditCollect")
public class CollectCrudController {
    @Autowired
    private CollectCrudService collectCrudService;
    @Autowired
    private CollectRedisRepository collectRedisRepository;
    @Autowired
    private TorderRepository torderRepository;

    //    驗票
    //    回傳代碼：-1 無票券；-2 已使用；-3 票券狀態異常；1 驗票成功
    @GetMapping("/checkin/{collectno}/{salt}")
    public String checkin(@PathVariable("collectno") Integer collectno, @PathVariable("salt") String salt) {
        int statusCode = collectCrudService.useTicket(collectno, salt);
        switch (statusCode){
            case 1:
                return "驗票成功";
            case -1:
                return "查無票券";
            case -2:
                return "票券已使用";
            case -3:
                return "票券狀態異常";
            default:
                return "系統忙碌中請稍候再試";
        }
    }

//    顯示票券 QRcode
    @GetMapping("/showQRcode/{collectno}")
    public String showQRcode(@PathVariable("collectno") Integer collecnto){
        return collectCrudService.getQRcode(collecnto);
    }

//    分票
    @GetMapping("/transfer/{collectno}")
    public boolean transferTicket(@PathVariable("collectno") Integer collectno, @RequestParam String memail){
        System.out.println(memail);
        if (memail == null || (memail.trim()).length() == 0) {
            return false;
        }
        return collectCrudService.updateCollect(collectno, memail);
    }


/* **************************************
 *              以下為測試                *
 ************************************** */
//    測試 Redis crud
    @PostMapping("/test/{collectno}")
    public boolean testRedisConnection(@PathVariable("collectno") Integer collectno) {
//        long oneHourLater = System.currentTimeMillis()/1000+60*60;
        CollectRedis cr = new CollectRedis(String.valueOf(collectno), "0", "givemesuccess", "iamsalt", 1728000);
        collectRedisRepository.save(cr);
        System.out.println("新增/修改票券");
        return true;
    }

    @PostMapping("/test2/{collectno}")
    public String inspect(@PathVariable("collectno") Integer collectRedisno) {
        Optional<CollectRedis> optCR = collectRedisRepository.findById(String.valueOf(collectRedisno));
        System.out.println("觀察票券" + collectRedisno);
        return optCR.toString();
    }

    @PostMapping("/test3/{collectno}")
    public String testRedisDelete(@PathVariable("collectno") Integer collectRedisno) {
        collectRedisRepository.deleteById(String.valueOf(collectRedisno));
        System.out.println("已刪除票券" + collectRedisno);
        return "deleted";
    }

    @PostMapping("/test4")
    public String testCancelOrderMySQL() {
        int ordno = 10015;
        collectCrudService.cancelTicket(ordno);
        System.out.println("已刪除票券" + ordno);
        return "Canceled";
    }

    // 測試退票功能
    @PostMapping("/test5/{torderno}")
    public String testCancelOrderBoth(@PathVariable("torderno") Integer torderno) {
        collectCrudService.cancelTicket(torderno);
        System.out.println("已刪除訂單" + torderno);
        return "Canceled";
    }

    //    測試判斷是否能退票
    @PostMapping("/test6/{torderno}")
    public boolean beforeCancel(@PathVariable("torderno") Integer torderno) {
        boolean result = collectCrudService.isCancelable(torderno);
        System.out.print("訂單" + torderno);
        System.out.println(result ? "可取消" : "不可取消");
        return result;
    }

    //    測試驗票
    @GetMapping("/test7/{collectno}/{salt}")
    public boolean checkinTest(@PathVariable("collectno") Integer collectno, @PathVariable("salt") String salt) {
        int result = collectCrudService.useTicket(collectno, salt);
        return result == 1;
    }

    //    測試由訂單新增票券
    @PostMapping("/test8/{torderno}")
    public boolean createTickets(@PathVariable("torderno") Integer torderno) {
        Optional<Torder> optTo = torderRepository.findById(torderno);
        if (optTo.isEmpty()) {
            System.out.println("查無訂單");
            return false;
        } else {
            Torder torder = optTo.get();
            boolean result = collectCrudService.insertCollect(torder);
            if (!result) {
                System.out.println("新增失敗");
                return false;
            }
            return true;
        }
    }

}
