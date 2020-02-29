package me.xueyao.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Simon.Xue
 * @date 2019-10-28 00:04
 **/
public interface StatusEnums {
    @AllArgsConstructor
    @Getter
    enum ReturnCode {
        SUCCESS,FAIL;
    }
    @AllArgsConstructor
    @Getter
    enum TradeState {
        SUCCESS("SUCCESS", "支付成功"),
        REFUND("REFUND", "转入退款"),
        NOTPAY("NOTPAY", "未支付"),
        CLOSED("CLOSED", "已关闭"),
        REVOKED("REVOKED", "已撤销（刷卡支付）"),
        USERPAYING("USERPAYING", "用户支付中"),
        PAYERROR("PAYERROR", "支付失败");
        String code;
        String msg;
    }
}
