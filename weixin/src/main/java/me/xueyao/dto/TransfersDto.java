package me.xueyao.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Simon.Xue
 * @date 2020-02-10 22:20
 **/
@Data
@Accessors(chain = true)
public class TransfersDto implements Serializable {
    /**
     * 商品订单号
     */
    @NotEmpty(message = "订单编号不能为空")
    private String dealCode;
    /**
     * 用户openId
     */
    @NotEmpty(message = "用户openId不能为空")
    private String openId;
    /**
     * 收款用户姓名
     */
    @NotEmpty(message = "收款用户姓名不能为空")
    private String realName;

    /**
     * 金额
     */
    @NotNull(message = "提现金额不能为空")
    private BigDecimal amount;

    /**
     * 企业付款备注
     */
    @NotNull(message = "企业付款备注不能为空")
    private String desc;

    /**
     * IP地址
     */
    private String spbillCreateIp;
}
