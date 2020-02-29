package me.xueyao.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Simon.Xue
 * @date 2019-12-02 15:02
 **/
@Data
@Accessors(chain = true)
public class RefundDto implements Serializable {
    @NotEmpty(message = "订单编号不能为空")
    private String dealCode;
    /**
     * 订单金额
     */
    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0.00", message = "订单金额不能少于0.00元")
    private BigDecimal totalFee;
    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.00", message = "退款金额不能少于0.00元")
    private BigDecimal refundFee;
    /**
     * 退款原因
     */
    private String refundDesc;

    /**
     * 退款订单号(数据库不要保存，不能和订单编号相同)
     */
    @NotEmpty(message = "订单退款编号不能为空")
    private String outRefundNo;


}
