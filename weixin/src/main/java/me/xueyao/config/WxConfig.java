package me.xueyao.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Simon.Xue
 * @date 2019-10-22 21:29
 **/
@Configuration
@Getter
public class WxConfig {

    /**
     * 设置微信小程序的appid
     */
    @Value("${wx.miniApp.appId}")
    private String appId;

    /**
     * 设置微信小程序的Secret
     */
    @Value("${wx.miniApp.secret}")
    private String secret;

    /**
     * 设置微信小程序消息服务器配置的token
     */
    @Value("${wx.miniApp.token}")
    private String token;

    /**
     * 设置微信小程序消息服务器配置的EncodingAESKey
     */
    @Value("${wx.miniApp.aesKey}")
    private String aesKey;

    /**
     * 消息格式，XML或者JSON
     */
    @Value("${wx.miniApp.msgDataFormat}")
    private String msgDataFormat;

    /**
     * 统一下单
     */
    @Value("${wx.pay.unifiedOrderUrl}")
    private String unifiedOrderUrl;

    /**
     * 商户号
     */
    @Value("${wx.pay.mchId}")
    private String mchId;

    /**
     * 商户号密钥
     */
    @Value("${wx.pay.key}")
    private String key;

    /**
     * 支付成功回调地址
     */
    @Value("${wx.pay.notifyUrl}")
    private String notifyUrl;

    /**
     * 获取公共号的access_token,可以用来判断小程序号是否存在
     */
    @Value("${wx.url.clientCredential}")
    private String clientCredential;

    /**
     * 登录凭证校验
     */
    @Value("${wx.url.code2session}")
    private String code2session;

    /**
     * 查询订单
     */
    @Value("${wx.url.orderQuery}")
    private String orderQuery;

    /**
     * 仿真系统获取key
     */
    @Value("${wx.url.getSignKey}")
    private String getSignKey;

    /**
     * 仿真系统key
     */
    @Value("${wx.pay.sandboxKey}")
    private String sandboxKey;

    /**
     * 退款
     */
    @Value("${wx.pay.refundUrl}")
    private String refundUrl;

    /**
     * 企业付款
     */
    @Value("${wx.pay.transfers}")
    private String transfers;

    /**
     * 企业appid
     */
    @Value("${wx.pay.mchAppid}")
    private String mchAppid;

    /**
     * API证书
     */
    @Value("${wx.pay.certPath}")
    private String certPath;

}
