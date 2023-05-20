package com.ezticket.web.product.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data // Lombok: Gene getter/setter by @getter/@setter
public class AddPorderDTO {
    private Integer memberno;
    private Integer ptotal;
    private Integer pdiscounttotal;
    private Integer pcoupontotal;
    private Integer pchecktotal;
    private Integer pcouponno;
    @NotNull(message = "請填入收件人")
    private String recipient;
    @NotNull(message = "請填入收件人手機")
    private String rephone;
    @NotNull(message = "請填入地址")
    @Size(min=15, message="地址長度不足")
    private String readdress;
    private List<OrderProductDTO> orderProducts;
    private String paymenttype;
}
