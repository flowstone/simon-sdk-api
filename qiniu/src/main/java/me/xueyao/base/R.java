package me.xueyao.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Simon.Xue
 * @date 2019-12-13 00:56
 **/
@Data
@NoArgsConstructor
public class R implements Serializable {
    private Integer code;
    private String msg;
    private Object data;

    public R(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public R(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }


    public static R ofSuccess(String msg) {
        return new R(RStatus.SUCCESS.code, msg);
    }

    public static R ofSuccess(String msg, Object data) {
        return new R(RStatus.SUCCESS.code, msg, data);
    }

    public static R ofParamError(String msg) {
        return new R(RStatus.BAD_PARAM.code, msg);
    }
}
