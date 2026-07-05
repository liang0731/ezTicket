package com.ezticket.web.users.pojo;

import com.ezticket.core.pojo.Core;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BACKUSER")
public class Backuser extends Core {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BANO")
    private Integer bano;

    @Column(name = "BAIMG")
    private byte[] baimg;
    @Column(name = "BAACCOUNT")
    private String baaccount;
    @NotBlank(message = "密碼不得為空")
    @Pattern(regexp = "^(?:(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,12}|\\$2[aby]\\$.{56})$", message = "密碼需由英文及數字組成，並且至少包含一個大寫字母、一個小寫字母，長度為8~12個字元。")
    @Column(name = "BAPASSWORD", length = 72)
    private String bapassword;
    @NotBlank(message = "名字不可為空")
    @Column(name = "BANAME")
    private String baname;
    @NotBlank(message = "信箱不可為空")
    @Email(message = "請輸入正確的電子信箱格式")
    @Column(name = "BAEMAIL")
    private String baemail;
    @NotBlank(message = "手機不可為空")
    @Pattern(regexp = "^09\\d{8}$", message = "手機必須為09開頭的10位數字")
    @Column(name = "BACELL")
    private String bacell;
    @Column(name = "BAROLENO")
    private Integer baroleno;
    @Column(name = "BASTATUS")
    private Integer bastatus;
}
