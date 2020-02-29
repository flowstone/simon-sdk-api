package me.xueyao.util;

import lombok.extern.slf4j.Slf4j;
import me.xueyao.config.WxConfig;
import me.xueyao.dto.RefundDto;
import me.xueyao.dto.TransfersDto;
import me.xueyao.dto.UnifiedOrderDto;
import me.xueyao.enums.PayEnum;
import me.xueyao.enums.StatusEnums;
import me.xueyao.enums.TradeTypeEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * 微信支付api
 *
 * @author qxw
 * 2018年3月1日
 */
@Slf4j
@Component
public class WxPayApi {

    @Autowired
    private WxConfig wxConfig;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ClientCustomSSL clientCustomSSL;

    /**
     * 统一下单返回预支付id
     *
     * @param
     * @param orderDto 统一下单对象
     * @return prepayId  预支付id
     * @throws Exception
     */
    public String unifiedOrder(UnifiedOrderDto orderDto) throws Exception {
        Map<String, String> map = new HashMap<>(16);
        map.put("appid", wxConfig.getAppId());
        //商户号
        map.put("mch_id", wxConfig.getMchId());
        //PC网页或公众号内支付可以传"WEB" 门店号
        map.put("device_info", orderDto.getDeviceInfo());
        //随机字符串长度要求在32位以内
        map.put("nonce_str", WxUtil.generateNonceStr());
        //商品简单描述
        map.put("body", orderDto.getBody());
        //商户订单号
        map.put("out_trade_no", orderDto.getOutTradeNo());
        //订单总金额，单位为分
        map.put("total_fee", orderDto.getTotalFee().multiply(new BigDecimal("100")).intValue() + "");
        //APP和网页支付提交用户端ip Native支付填调用微信支付API的机器IP
        map.put("spbill_create_ip", orderDto.getSpbillCreateIp());
        //订单回调地址
        map.put("notify_url", wxConfig.getNotifyUrl());
        //交易类型   JSAPI 公众号支付  NATIVE 扫码支付  APP APP支付
        map.put("trade_type", TradeTypeEnums.JSAPI.getCode());
        map.put("openid", orderDto.getOpenId());

        if (wxConfig.getUnifiedOrderUrl().contains("sandboxnew")) {
            map.put("sign", WxUtil.generateSignature(map, wxConfig.getSandboxKey()));
        } else {
            map.put("sign", WxUtil.generateSignature(map, wxConfig.getKey()));

        }

        String dataXML = WxUtil.mapToXml(map);
        String resultXMlStr = restTemplate.postForEntity(wxConfig.getUnifiedOrderUrl(), dataXML, String.class)
                .getBody();
        log.info("统一下单返回结果-----------------   " + resultXMlStr);
        try {
            Map<String, String> result = WxUtil.xmlToMap(resultXMlStr);
            String prepayId = result.get("prepay_id");
            if (StringUtils.isEmpty(prepayId)) {
                return null;
            } else {
                return prepayId;
            }
        } catch (Exception e) {
            throw new Exception("微信服务器超时");
        }



    }


    /**
     * 查询订单
     * @return
     * @throws Exception
     */
    public String queryOrder(String dealCode) throws Exception {
        Map<String, String> map = new HashMap<>(16);
        map.put("appid", wxConfig.getAppId());
        map.put("mch_id", wxConfig.getMchId());
        map.put("out_trade_no", dealCode);
        map.put("nonce_str", WxUtil.generateNonceStr());
        if (wxConfig.getUnifiedOrderUrl().contains("sandboxnew")) {
            map.put("sign", WxUtil.generateSignature(map, wxConfig.getSandboxKey()));
        } else {
            map.put("sign", WxUtil.generateSignature(map, wxConfig.getKey()));

        }
        String dataXml = WxUtil.mapToXml(map);
        String body = restTemplate.postForEntity(wxConfig.getOrderQuery(), dataXml, String.class)
                .getBody();
        return body;

    }


    /**
     * 获得仿真系统key
     * @return
     */
    public String getSignKey() throws Exception {
        Map<String, String> map = new HashMap<>(16);
        map.put("mch_id", wxConfig.getMchId());
        map.put("nonce_str", WxUtil.generateNonceStr());
        map.put("sign", WxUtil.generateSignature(map, wxConfig.getKey()));
        String dataXml = WxUtil.mapToXml(map);
        String body = restTemplate.postForEntity(wxConfig.getGetSignKey(), dataXml, String.class).getBody();
        Map<String, String> resultMap = WxUtil.xmlToMap(body);
        String returnCode = resultMap.get("return_code");
        if (StatusEnums.ReturnCode.SUCCESS.name().equals(returnCode)) {
            return resultMap.get("sandbox_signkey");
        } else {
            return null;
        }
    }


    /**
     * 根据预支付id 生成包含所有必须参数的map对象 返回给前端JsSDK使用
     *
     * @param prepayId
     * @return
     * @throws Exception
     */
    public Map<String, String> getClientPrepayMap(String prepayId) throws Exception {
        Map<String, String> map = new HashMap<>(16);
        map.put("appId", wxConfig.getAppId());
        map.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        map.put("nonceStr", WxUtil.generateNonceStr());
        map.put("package", "prepay_id=" + prepayId);
        map.put("signType", "MD5");
        String sign = WxUtil.generateSignature(map, wxConfig.getKey());
        map.put("paySign", sign);
        return map;
    }


    /**
     * 微信支付回调结果参数解析  接收通知成功必须通知微信成功接收通知
     *
     * @param request
     * @return
     * @throws Exception
     */
    public static String payCallBack(HttpServletRequest request) throws Exception {
        // 读取参数
        InputStream inputStream;
        StringBuffer sb = new StringBuffer();
        inputStream = request.getInputStream();
        String s;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        while ((s = in.readLine()) != null) {
            sb.append(s);
        }
        in.close();
        inputStream.close();

        return sb.toString();

    }



    /**
     * 微信退款
     * @param refundDto
     * @return
     * @throws Exception
     */
    public Boolean refundOrder(RefundDto refundDto) throws Exception {
        Map<String, String> map = new HashMap<>(16);
        map.put("appid", wxConfig.getAppId());
        map.put("mch_id", wxConfig.getMchId());
        map.put("nonce_str", WxUtil.generateNonceStr());
        map.put("out_refund_no", refundDto.getOutRefundNo());
        map.put("out_trade_no", refundDto.getDealCode());
        map.put("total_fee", refundDto.getTotalFee().multiply(new BigDecimal(100)).intValue()+"");
        map.put("refund_fee", refundDto.getRefundFee().multiply(new BigDecimal(100)).intValue()+"");
        map.put("sign", WxUtil.generateSignature(map, wxConfig.getKey()));
        String xml = WxUtil.mapToXml(map);
        String resultXMlStr = clientCustomSSL.wxRefund(wxConfig.getRefundUrl(), xml);
        log.info("退款订单返回结果---------" + resultXMlStr);
        Map<String, String> xmlToMap = WxUtil.xmlToMap(resultXMlStr);
        String returnCode = xmlToMap.get(PayEnum.WxEnum.RETURN_CODE.getMsg());
        String resultCode = xmlToMap.get(PayEnum.WxEnum.RESULT_CODE.getMsg());
        if (PayEnum.WxEnum.SUCCESS.getMsg().equals(returnCode)
                && PayEnum.WxEnum.SUCCESS.getMsg().equals(resultCode)) {
            return true;
        }
        return false;
    }

    /**
     * 企业付款到零钱
     * @param transfersDto
     * @return
     * @throws Exception
     */
    public synchronized String transfers(TransfersDto transfersDto) throws Exception {
        Map<String, String> map = new HashMap<>(16);
        map.put("mch_appid", wxConfig.getMchAppid());
        map.put("mchid", wxConfig.getMchId());
        map.put("nonce_str", WxUtil.generateNonceStr());
        map.put("partner_trade_no", transfersDto.getDealCode());
        map.put("openid", transfersDto.getOpenId());
        map.put("check_name", "FORCE_CHECK");
        map.put("re_user_name", transfersDto.getRealName());
        map.put("amount", transfersDto.getAmount().multiply(new BigDecimal(100)).intValue()+"");
        map.put("desc", transfersDto.getDesc());
        map.put("spbill_create_ip", transfersDto.getSpbillCreateIp());
        map.put("sign", WxUtil.generateSignature(map, wxConfig.getKey()));
        String xml = WxUtil.mapToXml(map);
        String resultXMlStr = clientCustomSSL.wxRefund(wxConfig.getTransfers(), xml);

        return resultXMlStr;
    }


}
