package com.ezticket.web.product.service;

import com.ezticket.core.service.EmailService;
import com.ezticket.ecpay.service.OrderService;
import com.ezticket.web.product.dto.AddPorderDTO;
import com.ezticket.web.product.dto.OrderProductDTO;
import com.ezticket.web.product.dto.PorderDTO;
import com.ezticket.web.product.dto.PorderDetailsDTO;
import com.ezticket.web.product.pojo.*;
import com.ezticket.web.product.repository.*;
import com.ezticket.web.users.pojo.Member;
import com.ezticket.web.users.repository.MemberRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PorderService {

    @Autowired
    private ProductDAO dao;
    @Autowired
    private PorderRepository porderRepository;
    @Autowired
    private PdetailsRepository pdetailsRepository;
    @Autowired
    private PcouponholdingRepository pcouponholdingRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private OrderService ecpayorderService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EmailService emailService;
    @Autowired
    private Top10DAO top10Dao;


    // GetPordersByID
    public List<PorderDTO> getPordersByID(Integer id) {
        return porderRepository.findByID(id)
                .stream()
                .map(this::EntityToDTO)
                .collect(Collectors.toList());
    }

    public PorderDTO getPorderByID(Integer id) {
        Porder porder = porderRepository.getReferenceById(id);
        return EntityToDTO(porder);
    }

    // GetPorderDetailsByID
    public PorderDetailsDTO getPorderDetailsByID(Integer id) {
        Porder porder = porderRepository.findByPorderno(id);
        return EntityToDetailDTO(porder);
    }

    public PorderDetailsDTO EntityToDetailDTO(Porder porder) {
        PorderDetailsDTO porderDetailsDTO = modelMapper.map(porder, PorderDetailsDTO.class);
        porderDetailsDTO.setProducts(porder.getProducts());
        return porderDetailsDTO;
    }

    // UpdatePorderByID
    @Transactional
    public PorderDTO updateByID(Integer id, Integer processStatus) {
        Porder porder = porderRepository.getReferenceById(id);
        // 付款狀態 Ppaymentstatus
        // 0 未付款 1 已付款 2 已退款 3 付款失敗
        // 訂單狀態 Pprocessstatus
        // 0 未處理 1 已出貨 2 已結案 3 已取消 4 取消申請
        Integer paymentStatus = porder.getPpaymentstatus();

        switch (paymentStatus) {
            case 0 -> {
                // 如果訂單未付款，只能取消訂單，不能執行其他操作
                if (processStatus == 4) {
                    processStatus = 3;
                } else if (processStatus != 3 && processStatus != 4) {
                    throw new IllegalStateException("未付款的訂單只能取消");
                }
            }
            case 1 -> {
                // 如果訂單已付款
                if (processStatus == 3) {
                    // 如果要取消訂單，則付款狀態改為退款
                    porder.setPpaymentstatus(2);
                    porder.setPprocessstatus(3);
                } else if (processStatus == 4) {
                    // 如果要將訂單標記為“取消申請”狀態
                    porder.setPprocessstatus(4);
                } else if (processStatus == 1) {
                    // 如果訂單付款，則可以出貨
                    porder.setPprocessstatus(2);
                } else if (processStatus == 2) {
                    // 如果訂單已出貨，則可以結案
                    porder.setPprocessstatus(2);
                } else {
                    // 其他狀態都不允許
                    throw new IllegalStateException("訂單已付款，不能執行此操作");
                }
            }
            default ->
                // 其他付款狀態都不允許
                    throw new IllegalStateException("未知的訂單付款狀態");
        }

        switch (processStatus) {
            case 1:
                porder.setPshipdate(LocalDateTime.now());
                break;
            case 2:
                porder.setParrivedate(LocalDateTime.now());
                porder.setPclosedate(LocalDateTime.now());
                break;
            case 3:
                porder.setPclosedate(LocalDateTime.now());
                List<Pdetails> pdetails = pdetailsRepository.findByPorderno(porder.getPorderno());
                for (Pdetails pdetail : pdetails) {
                    Product product = dao.getByPrimaryKey(pdetail.getPdetailsNo().getProductno());
                    product.setPqty(product.getPqty() + pdetail.getPorderqty());
                    pdetailsRepository.save(pdetail);
                    dao.update(product);
                }
                break;
        }
        porder.setPprocessstatus(processStatus);
        Porder updatedPorder = porderRepository.save(porder);
        return EntityToDTO(updatedPorder);
    }

    public PorderDTO EntityToDTO(Porder porder) {
        return modelMapper.map(porder, PorderDTO.class);
    }

    public List<PorderDTO> getAllPorder() {
        return porderRepository.findAll()
                .stream()
                .map(this::EntityToDTO)
                .collect(Collectors.toList());
    }

    // AddPorder
    @Transactional
    public PorderDTO addPorder(AddPorderDTO addPorderDTO) {
        Porder porder = new Porder();
        porder.setMemberno(addPorderDTO.getMemberno());
        porder.setPtotal(addPorderDTO.getPtotal());
        porder.setPdiscounttotal(addPorderDTO.getPdiscounttotal());
        porder.setPcoupontotal(addPorderDTO.getPcoupontotal());
        porder.setPchecktotal(addPorderDTO.getPchecktotal());
        porder.setPcouponno(addPorderDTO.getPcouponno());
        porder.setRecipient(addPorderDTO.getRecipient());
        porder.setRephone(addPorderDTO.getRephone());
        porder.setReaddress(addPorderDTO.getReaddress());
        porder.setPorderdate(LocalDateTime.now());
        porder.setPpaymentstatus(0);
        porder.setPprocessstatus(0);
        if (addPorderDTO.getPcouponno() != null){
            PcouponholdingPK pcouponholdingPK = new PcouponholdingPK(addPorderDTO.getPcouponno(),addPorderDTO.getMemberno());
            Pcouponholding pcouponholding = pcouponholdingRepository.getReferenceById(pcouponholdingPK);
            pcouponholding.setPcouponstatus((byte) 1);
        }
        Porder porderno = porderRepository.save(porder);
        Member member = memberRepository.getReferenceById(porderno.getMemberno());
        emailService.sendOrderMail(member.getMname(),member.getMemail(),porder.getPorderno().toString(), String.valueOf(1));
        List<OrderProductDTO> orderProducts = addPorderDTO.getOrderProducts();
        for (OrderProductDTO orderProduct : orderProducts) {
            Pdetails pdetails = new Pdetails();
            PdetailsPK pdetailsPK = new PdetailsPK();
            pdetailsPK.setPorderno(porderno.getPorderno());
            pdetailsPK.setProductno(orderProduct.getProductno());
            pdetails.setPdetailsNo(pdetailsPK);
            pdetails.setPorderqty(orderProduct.getQuantity());
            pdetails.setPprice(orderProduct.getPprice());
            pdetails.setPcommentstatus(0);
            pdetailsRepository.save(pdetails);
            Product product = dao.getByPrimaryKey(orderProduct.getProductno());
            if (product.getPqty() < orderProduct.getQuantity()) {
                throw new RuntimeException("庫存不足");
            }
            product.setPqty(product.getPqty() - orderProduct.getQuantity());
            dao.update(product);
            top10Dao.addKeyValue(String.valueOf(product.getProductno()), orderProduct.getQuantity());

        }
        return EntityToDTO(porder);
    }

    // 每五分鐘掃描一次
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void checkOrderPayStatus() {
        List<Porder> porders = porderRepository.findAll();
        for (Porder porder : porders) {
            // 取得訂單成立時間
            LocalDateTime start = porder.getPorderdate();
            // 設定成立的11分後
            LocalDateTime end = start.plusMinutes(11);
            //  當付款狀態為0 未付款且已超過11分鐘
            if (porder.getPpaymentstatus() == 0 && LocalDateTime.now().isAfter(end)) {
                // 在這裡執行超過11分鐘的動作
                // 設定付款失敗
                // 0 未付款 1 已付款 2 已退款 3 付款失敗
                porder.setPpaymentstatus(3);
                // 0 未處理 1 已出貨 2 已結案 3 已取消 4 取消確認
                porder.setPprocessstatus(3);
                porder.setPclosedate(LocalDateTime.now());
                porderRepository.save(porder);
                // 取得訂單明細
                List<Pdetails> pdetailsList = pdetailsRepository.findByPorderno(porder.getPorderno());
                for (Pdetails pdetails : pdetailsList){
                    // 取得商品
                    Product product = dao.getByPrimaryKey(pdetails.getPdetailsNo().getProductno());
                    // 返回庫存
                    product.setPqty(product.getPqty() + pdetails.getPorderqty());
                    dao.update(product);
                }
                // 寄送取消通知信
                // status 1 成立 2 付款 3 取消
                Member member = memberRepository.getReferenceById(porder.getMemberno());
                emailService.sendOrderMail(member.getMname(),member.getMemail(),porder.getPorderno().toString(), String.valueOf(3));
            }
        }
    }

}
