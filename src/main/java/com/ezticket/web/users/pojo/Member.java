package com.ezticket.web.users.pojo;

import com.ezticket.core.pojo.Core;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "MEMBER")
public class Member extends Core {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBERNO")
    private Integer memberno;
    @Column(name = "MIMG")
    private byte[] mimg;
    @Column(name = "MEMAIL")
    private String memail;
    @Column(name = "MPASSWORD", length = 72)
    private String mpassword;
    @Column(name = "MNAME")
    private String mname;
    @Column(name = "BIRTH")
    private Date birth;
    @Column(name = "GENDER")
    private Integer gender;
    @Column(name = "MCELL")
    private String mcell;
    @Column(name = "MPHONE")
    private String mphone;
    @Column(name = "ADDRESS")
    private String address;
    @Column(name = "COMRECIPIENT")
    private String comrecipient;
    @Column(name = "COMREPHONE")
    private String comrephone;
    @Column(name = "COMREADDRESS")
    private String comreaddress;
    @Column(name = "MEMBERSTATUS")
    private Integer memberstatus;

//    @OneToMany
//    @JoinColumn(name = "torderno",
//            referencedColumnName = "MEMBERNO")
//    private List<Torder> torder;
}
