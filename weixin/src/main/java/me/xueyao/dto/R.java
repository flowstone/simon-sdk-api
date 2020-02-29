package me.xueyao.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.function.Consumer;

/**
 * 请求响应对象封装
 * Created by Rocky.Jiang on 2019-06-04 15:15.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class R<T> implements Serializable {

    /**
     * 请求成功
     */
    public static final int CODE_SUCCESS = 200;

    /**
     * 系统异常
     */
    public static final int CODE_SYS_ERROR = 500;

    /**
     * 参数错误
     */
    public static final int CODE_PARAMS_ERROR = 400;

    /**
     * 超时错误，如：登陆超时、授权超时等；
     */
    public static final int CODE_TIME_OUT = 401;

    /**
     * 用户不存在错误，必要情况跳转登陆页面；
     */
    public static final int CODE_NULL_USER = 402;

    /**
     * 用户权限不匹配，无操作权限；
     */
    public static final int CODE_AUTHORITY_ERROR = 403;
    /**
     * 系统异常 提示信息
     */
    public static final String MESSAGE_SYS_ERROR = "系统异常";

    /**
     * 操作成功提示信息
     */
    public static final String MESSAGE_SUCCESS = "操作成功";


    public int code = CODE_SUCCESS;

    public String message = MESSAGE_SUCCESS;

    public T data;

    public R(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public R<T> code(int code) {
        this.code = code;
        return this;
    }

    public R<T> message(String message) {
        this.message = message;
        return this;
    }

    public R<T> data(T data) {
        this.data = data;
        return this;
    }


    @JsonProperty("success")
    public boolean getSuccess() {
        return this.code == CODE_SUCCESS;
    }

    /**
     * 用于安全的拆包
     *
     * @param failedConsumer 异常时，消费方法
     * @return 当 success 为true 返回正确的对象data；
     * 当success为false时，
     * 1、result.open(null) 返回null对象，不抛出异常；
     * 2、result.open(p->{throw new BusinessException(result.getMessage(),this);}); 抛出BusinessException
     */
    @JsonIgnore
    public T open(Consumer<R<T>> failedConsumer) {
        if (this.getSuccess()) {
            return this.getData();
        } else if (failedConsumer != null) {
            failedConsumer.accept(this);
        }
        return null;
    }

    public static <T> R<T> ofSuccess(String message, T data) {
        R<T> r = new R<T>();
        r.setCode(CODE_SUCCESS);
        r.setData(data);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> ofError(String message, T data) {
        R<T> r = new R<T>();
        r.setCode(CODE_PARAMS_ERROR);
        r.setData(data);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> ofSuccess(T data) {
        return ofSuccess(MESSAGE_SUCCESS, data);
    }

    public static <T> R<T> ofError(T data) {
        return ofError(MESSAGE_SYS_ERROR, data);
    }

    @NotNull
    public static <T> R<T> ofParamsError(int code, String message) {
        return new R(code, message);
    }

    @NotNull
    public static <T> R<T> ofParamsError(String message) {
        return new R(CODE_PARAMS_ERROR, message);
    }

    @NotNull
    public static <T> R<T> ofSystemError(String message) {
        return new R(CODE_SYS_ERROR, message);
    }
}
