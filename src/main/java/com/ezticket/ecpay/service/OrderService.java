package com.ezticket.ecpay.service;

import com.ezticket.core.service.EmailService;
import com.ezticket.core.util.UrlProvider;
import com.ezticket.web.activity.pojo.Seats;
import com.ezticket.web.activity.pojo.Session;
import com.ezticket.web.activity.pojo.Tdetails;
import com.ezticket.web.activity.pojo.Torder;
import com.ezticket.web.activity.repository.SeatsRepository;
import com.ezticket.web.activity.repository.SessionRepository;
import com.ezticket.web.activity.repository.TdetailsRepository;
import com.ezticket.web.activity.repository.TorderRepository;
import com.ezticket.web.activity.service.CollectCrudService;
import com.ezticket.web.product.pojo.Pdetails;
import com.ezticket.web.product.pojo.Porder;
import com.ezticket.web.product.pojo.Product;
import com.ezticket.web.product.repository.PdetailsRepository;
import com.ezticket.web.product.repository.PorderRepository;
import com.ezticket.web.product.repository.ProductDAO;
import com.ezticket.web.users.pojo.Member;
import com.ezticket.web.users.repository.MemberRepository;
import ecpay.payment.integration.AllInOne;
import ecpay.payment.integration.domain.AioCheckOutALL;
import ecpay.payment.integration.domain.QueryTradeInfoObj;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private PorderRepository porderRepository;
    @Autowired
    private PdetailsRepository pdetailsRepository;
    @Autowired
    private ProductDAO productRepository;

    //	Add by Shawn on 04/17
    @Autowired
    private TorderRepository torderRepository;

    @Autowired
    private TdetailsRepository tdetailsRepository;

    @Autowired
    private SeatsRepository seatsRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CollectCrudService collectCrudService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UrlProvider urlProvider;


    public String ecpayCheckout(Integer porderno) {
        // 綠界的方法裡面都有註解，可以點進去看
        // 建立AllInOne物件
        AllInOne all = new AllInOne("");
        // 取得訂單
        Porder porder = porderRepository.getReferenceById(porderno);
        AioCheckOutALL obj = new AioCheckOutALL();
        // 綠界規定須20碼,放入訂單編號15碼+訂單編號5碼
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 15);
        obj.setMerchantTradeNo(uuid + porderno);
        // 取得當前時間，放入時間
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String Now = now.format(formatter);
        obj.setMerchantTradeDate(Now);
        // 放入結帳金額
        obj.setTotalAmount(porder.getPchecktotal().toString());
        // 交易敘述
        obj.setTradeDesc("ezTicket 售票平台");
        // 商品明細 # 可以換行
        // 將品項一個一個加入字串中
        String itemList = "";
        List<Pdetails> pdetailsList = pdetailsRepository.findByPorderno(porderno);
        for (int i = 0; i < pdetailsList.size(); i++) {
            Pdetails pdetails = pdetailsList.get(i);
            Product product = productRepository.getByPrimaryKey(pdetails.getPdetailsNo().getProductno());
            itemList += "#" + (i + 1) + ". " + product.getPname() + " x " + pdetails.getPorderqty();
        }
        // 塞入商品明細
        obj.setItemName(itemList);

        // 設定接收回傳值的Https + Controller路徑
        obj.setReturnURL(urlProvider.getReturnURL() + "/ecpay/return");
        // 設定商品明細路徑及返回商店路徑
        // 利用自身IP取得路徑返回商店
        String confirmedUrl = "/front-product-order_confirmed.html?id=" + porderno;
        String detailUrl = "/front-product-order_detail.html?id=" + porderno;
        obj.setClientBackURL(urlProvider.getLocalURL() + confirmedUrl);
        obj.setItemURL(urlProvider.getLocalURL() + detailUrl);
        // 是否需要額外的付款資訊
        obj.setNeedExtraPaidInfo("N");
        // 會回傳一個form表單
        String form = all.aioCheckOut(obj, null);

        return form;
    }

    // 綠界訂單查詢 需用綠界訂單編號
    public String checkorder(String merchantTradeNo) {
        QueryTradeInfoObj queryTradeInfoObj = new QueryTradeInfoObj();
        queryTradeInfoObj.setMerchantID("2000132");
        queryTradeInfoObj.setMerchantTradeNo(merchantTradeNo);
        queryTradeInfoObj.setTimeStamp(String.valueOf(Instant.now().getEpochSecond()));
        AllInOne allInOne = new AllInOne("");
        return allInOne.queryTradeInfo(queryTradeInfoObj);
    }

    //	=========================================== 節目訂單 ==================================================
    public String ecpayTCheckout(Integer torderNo) {
        // 建立AllInOne物件
        AllInOne all = new AllInOne("");
        // 取得訂單
        Torder torder = torderRepository.getReferenceById(torderNo);
        AioCheckOutALL obj = new AioCheckOutALL();
        // 綠界規定須20碼,放入訂單編號15碼+訂單編號5碼
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 15);
        obj.setMerchantTradeNo(uuid + torderNo);
        // 取得當前時間，放入時間
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String Now = now.format(formatter);
        obj.setMerchantTradeDate(Now);
        // 放入結帳金額
        obj.setTotalAmount(torder.getTcheckTotal().toString());
        // 交易敘述
        obj.setTradeDesc("ezTicket 售票平台");
        // 將票券資訊濃縮為一個字串
        String itemList = "";

        List<Tdetails> tdetailsList = tdetailsRepository.findByTorderno(torderNo);

        Tdetails tdetails = tdetailsList.get(0);

        Session session = sessionRepository.getById(tdetails.getSessionNo());

        if (tdetails.getSeatNo() != null) {
            Seats seats = seatsRepository.getById(tdetails.getSeatNo());
            itemList = session.getActivity().getAName() + " - " + session.getSessionsTime() + " - " + seats.getBlockName() + " 區 * " + tdetailsList.size() + " 張, ";
        } else {
            itemList = session.getActivity().getAName() + " - " + session.getSessionsTime() + " - 無區分 * " + tdetails.getTqty() + " 張, ";
        }

        // 塞入票券明細
        obj.setItemName(itemList);

        // 設定接收回傳值的Https + Controller路徑
        obj.setReturnURL(urlProvider.getReturnURL() + "/ecpay/Treturn");
        String confirmedUrl = "/front-activity-orderconfirm.html?id=" + torderNo;
        obj.setClientBackURL(urlProvider.getLocalURL() + confirmedUrl);

        // 是否需要額外的付款資訊 ngrok http 8080
        obj.setNeedExtraPaidInfo("N");

        // 回傳 form 表單
        String form = all.aioCheckOut(obj, null);

        return form;
    }

    // 每五分鐘檢查超過十分鐘未付款的訂單，將座位釋放且將訂單改為已結案
    @Scheduled(fixedRate = 300000)
    public void checkTorders() {
        List<Torder> unPaidOrders = torderRepository.findAllByUnpaidAndBeforeTenMins();

        for (Torder torder : unPaidOrders) {
            String merchantTradeNo = "ezTicket0000000" + torder.getTorderNo();

            QueryTradeInfoObj queryTradeInfoObj = new QueryTradeInfoObj();
            queryTradeInfoObj.setMerchantID("2000132");
            queryTradeInfoObj.setMerchantTradeNo(merchantTradeNo);
            queryTradeInfoObj.setTimeStamp(String.valueOf(Instant.now().getEpochSecond()));
            AllInOne allInOne = new AllInOne("");

            String tradeInfo = allInOne.queryTradeInfo(queryTradeInfoObj);
            if (tradeInfo.contains("TradeStatus=1&")) {

                String strArr[] = tradeInfo.split("&");

                Timestamp tsp = null;
                for (String s : strArr) {
                    if (s.contains("PaymentDate") && s.length() > 12) {
                        tsp = Timestamp.valueOf(s.substring(12).trim().replaceAll("/", "-"));
                    }
                }

                torder.setTpayDate(tsp);
                torder.setTpaymentStatus(1);
                torder.setTprocessStatus(1);
                torderRepository.save(torder);

                // 票券付款成功，寄信
                Member member = memberRepository.getReferenceById(torder.getMemberNo());
                emailService.sendTOrderMail(member.getMname(), member.getMemail(), torder.getTorderNo().toString(), String.valueOf(1));

                // 票券 QR Code 產生應於此處 - 2 (Melody)
                collectCrudService.insertCollect(torder);

            } else {
                torder.setTpaymentStatus(3);
                torder.setTprocessStatus(3);
                torderRepository.save(torder);

                // 票券付款失敗，寄信
                Member member = memberRepository.getReferenceById(torder.getMemberNo());
                emailService.sendTOrderMail(member.getMname(), member.getMemail(), torder.getTorderNo().toString(), String.valueOf(3));

                List<Tdetails> toBeModiDetails = tdetailsRepository.findByTorderno(torder.getTorderNo());
                for (Tdetails tdetail : toBeModiDetails) {
                    if (tdetail.getSeatNo() != null) {
                        // 若訂單失敗且節目屬於有座位的，則釋放座位，將座位改成可售出
                        seatsRepository.updateStatus(1, tdetail.getSeatNo());
                    } else {
                        // 若訂單失敗且節目屬於有無座位的，則已售出票券數量減少
                        sessionRepository.updateStandingQtyById(-tdetail.getTqty(), tdetail.getSessionNo());
                    }
                }
            }
        }
    }
}
