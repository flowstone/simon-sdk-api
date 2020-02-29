package me.xueyao.controller;

import lombok.extern.slf4j.Slf4j;
import me.xueyao.config.WxConfig;
import me.xueyao.util.WxPayApi;
import me.xueyao.util.WxUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon.Xue
 * @date 2019-10-23 13:55
 **/
@RestController
@RequestMapping("/wxCallback")
@Slf4j
public class WxCallbackController {

    @Autowired
    private WxConfig wxConfig;


    /**
     * 微信回调方法
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/callback")
    public String callback(HttpServletRequest request) throws Exception {
        String callBackStr = WxPayApi.payCallBack(request);
        log.info("微信回调函数返回，{}", callBackStr);
        boolean isSignatureValid = WxUtil.isSignatureValid(callBackStr, wxConfig.getKey());
        Map<String, String> resultMap = new HashMap<>(16);
        if (!isSignatureValid) {
            resultMap.put("return_code", "FAIL");
            resultMap.put("return_msg", "校验签名失败");
            String resultXml = WxUtil.mapToXml(resultMap);
            log.warn("校验签名失败 -> {}", resultXml);
            return resultXml;
        }
        Map<String, String> map = WxUtil.xmlToMap(callBackStr);
        /**
         * 处理微信回调------------
         */
        resultMap.put("return_code", "SUCCESS");
        resultMap.put("return_msg", "OK");
        String resultXml = WxUtil.mapToXml(resultMap);
        log.info("商户处理后返回给微信的参数 -> {}", resultXml);
        return resultXml;
    }

}
