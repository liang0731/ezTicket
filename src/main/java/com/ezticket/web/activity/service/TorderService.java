package com.ezticket.web.activity.service;

import com.ezticket.core.service.EmailService;
import com.ezticket.web.activity.dto.AddTorderDTO;
import com.ezticket.web.activity.dto.OrderTicketDTO;
import com.ezticket.web.activity.dto.TorderDto;
import com.ezticket.web.activity.pojo.Seats;
import com.ezticket.web.activity.pojo.Session;
import com.ezticket.web.activity.pojo.Tdetails;
import com.ezticket.web.activity.pojo.Torder;
import com.ezticket.web.activity.repository.SeatsRepository;
import com.ezticket.web.activity.repository.SessionRepository;
import com.ezticket.web.activity.repository.TdetailsRepository;
import com.ezticket.web.activity.repository.TorderRepository;
import com.ezticket.web.product.dto.AddPorderDTO;
import com.ezticket.web.product.dto.OrderProductDTO;
import com.ezticket.web.product.dto.PorderDTO;
import com.ezticket.web.product.pojo.*;
import com.ezticket.web.users.pojo.Member;
import com.ezticket.web.users.repository.MemberRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TorderService {
    @Autowired
    private TorderRepository torderRepository;
    @Autowired
    private ModelMapper modelMapper;

    //    Add by Shawn on 04/17
    @Autowired
    private TdetailsRepository tdetailsRepository;

    @Autowired
    private SeatsService seatsService;

    @Autowired
    private SessionService sessionService;
    @Autowired
    SeatsRepository seatsRepository;
    @Autowired
    SessionRepository sessionRepository;
    @Autowired
    private CollectCrudService collectCrudService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EmailService emailService;

    public List<TorderDto> findAll() {
        return torderRepository.findAll()
                .stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    public Optional<Torder> findById(Integer torderNo) {
        return torderRepository.findById(torderNo);
    }

    private TorderDto entityToDTO(Torder torder) {
        TorderDto torderDto = new TorderDto();
        torderDto = modelMapper.map(torder, TorderDto.class);
        return torderDto;
    }

    // AddPorder
    @Transactional
    public TorderDto addTOrder(AddTorderDTO addTorderDTO) {
        Torder torder = new Torder();
        torder.setMemberNo(addTorderDTO.getMemberno());
        torder.setTtotal(addTorderDTO.getTtotal());
        torder.setTcheckTotal(addTorderDTO.getTcheckTotal());
        torder.setTorderDate(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("Asia/Taipei"))));
        torder.setTpaymentStatus(0);
        torder.setTprocessStatus(0);

        Torder savedTorder = torderRepository.save(torder);

        Member member = memberRepository.getReferenceById(savedTorder.getMemberNo());
        emailService.sendTOrderMail(member.getMname(), member.getMemail(), savedTorder.getTorderNo().toString(), String.valueOf(0));

        List<OrderTicketDTO> orderTickets = addTorderDTO.getOrderTickets();

        for (int i = 0; i < orderTickets.size(); i++) {
            Tdetails tdetails = new Tdetails();
            tdetails.setTorderNo(savedTorder.getTorderNo());
            tdetails.setSessionNo(orderTickets.get(i).getSessionNo());
            tdetails.setSeatNo(orderTickets.get(i).getSeatNo());
            tdetails.setTqty(orderTickets.get(i).getTQty());
            tdetails.setAcommentStatus(0);
            tdetailsRepository.save(tdetails);

            if (orderTickets.get(i).getSeatNo() != null) {
                // 訂單建立後，如節目屬於有座位的，則先將該座位的編號從 Redis 中移除，並改變 MySQL 座位狀態為已售出
                seatsService.setSessionSeats(2, orderTickets.get(i).getSeatNo(), orderTickets.get(i).getSessionNo());
            } else {
                // 訂單建立後，如節目屬於無座位的，則改變 MySQL 可出售票券數量
                sessionService.updateTicketQTY(orderTickets.get(i).getTQty(), orderTickets.get(i).getSessionNo());
            }
        }

        return entityToDTO(torder);
    }

    public Torder getById(Integer torderNo){
        return torderRepository.getReferenceById(torderNo);
    }

    public Torder updateTorder(Torder torder){
        return torderRepository.save(torder);
    }




    public void deleteTorder(Integer torderNo) {
        // 找到對應的tdetails
        List<Tdetails> tdetails = tdetailsRepository.findAllByTorderNo(torderNo);
        System.out.println("hhhhhhhhhhh");

        for (Tdetails i : tdetails ){
            // 找到對應的session
            Integer sessionNo = i.getSessionNo();
            Session session = sessionRepository.findById(sessionNo).get();
            // 活動開始時間前三天才可退票
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(session.getSessionsTime());
            calendar.add(Calendar.DATE, -3);
            Date deadline = calendar.getTime();
            if (now.before(deadline)) {
                // 若有座位，更新座位狀態
                if (i.getSeatNo() != null) {
                    updateSeatStatus(sessionNo, i.getSeatNo(), 1);
                } else {
                    // 若無座位，更新無座位狀態
                    updateStandingQty(sessionNo, i.getTqty());
                }
                // 更新torder狀態
                Torder torder = torderRepository.findById(torderNo).get();
                torder.setTpaymentStatus(2);
                torder.setTprocessStatus(2);
                torderRepository.save(torder);
                collectCrudService.cancelTicket(torderNo);
            }
        }


    }

    private void updateSeatStatus(Integer sessionNo, Integer seatNo, Integer seatStatus) {
        Seats seat = seatsRepository.findBySessionNoAndSeatNo(sessionNo, seatNo);
        System.out.println("aaaaaaaa");
        seat.setSeatStatus(seatStatus);
        seatsRepository.save(seat);
        Session session = sessionRepository.findById(sessionNo).get();
        session.setSeatsQty(session.getSeatsQty() - 1);
        sessionRepository.save(session);
    }

    private void updateStandingQty(Integer sessionNo, Integer tqty) {
        Session session = sessionRepository.findById(sessionNo).get();
        System.out.println("bbbbbbbbbbbbb");
        session.setStandingQty(session.getStandingQty() - tqty);
        sessionRepository.save(session);
    }


}
