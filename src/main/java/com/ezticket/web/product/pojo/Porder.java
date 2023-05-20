package com.ezticket.web.product.pojo;


import com.ezticket.web.users.pojo.Member;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data // Lombok: Gene getter/setter by @getter/@setter
@AllArgsConstructor // Lombok: Auto gene args constructor
@NoArgsConstructor // Lombok: read above
@Entity
public class Porder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer porderno;
    private Integer memberno;
    private Integer ptotal;
    private Integer pdiscounttotal;
    private Integer pcoupontotal;
    private Integer pchecktotal;
    private Integer pcouponno;
    private String recipient;
    private String rephone;
    private String readdress;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JoinColumn(insertable = false,updatable = false)
    private LocalDateTime porderdate;
    @JoinColumn(insertable = false,updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ppaydate;
    @JoinColumn(insertable = false,updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pshipdate;
    @JoinColumn(insertable = false,updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime parrivedate;
    @JoinColumn(insertable = false,updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pclosedate;
    @JoinColumn(insertable = false,updatable = false)
    private Integer ppaymentstatus;
    @JoinColumn(insertable = false,updatable = false)
    private Integer pprocessstatus;
    private String logisticsid;
    private String paymenttransactionid;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(referencedColumnName = "porderno", name = "porderno"),
            name = "pdetails",
            inverseJoinColumns = @JoinColumn(name = "productno", referencedColumnName = "productno")
    )
    private List<Product> products;
    @OneToMany(mappedBy = "pdetailsNo.porderno")
    private List<Pdetails> pdetails;
    @ManyToOne
    @JoinColumn(name = "memberno", insertable = false, updatable = false)
    private Member member;

}
