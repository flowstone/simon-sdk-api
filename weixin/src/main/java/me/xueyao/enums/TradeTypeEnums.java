package me.xueyao.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Simon.Xue
 * @date 2019-10-23 18:08
 **/
@Getter
@AllArgsConstructor
public enum TradeTypeEnums {
    JSAPI("JSAPI", "公众号支付"),
    NATIVE("NATIVE", "扫码支付"),
    APP("APP", "APP支付");

    String code;
    String msg;

}

