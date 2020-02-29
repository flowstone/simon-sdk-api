package me.xueyao.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Simon.Xue
 * @date 2019-12-03 11:01
 **/
public interface PayEnum {
    @AllArgsConstructor
    @Getter
    enum WxEnum{
        SUCCESS("SUCCESS"),
        FAIL("FAIL"),
        RETURN_CODE("return_code"),
        RESULT_CODE("result_code");
        String msg;
    }
}
