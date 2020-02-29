package me.xueyao.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.xueyao.config.WxConfig;
import me.xueyao.dto.R;
import me.xueyao.dto.WxAuthDto;
import me.xueyao.vo.Code2SessionVo;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon.Xue
 * @date 2019-10-22 21:46
 **/
@RestController
@RequestMapping("/wxMp")
@Slf4j
public class WxMpController {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    WxConfig wxConfig;

    /**
     * 登陆接口  获取openId
     * @param code
     * @return
     */
    @GetMapping("/getOpenId")
    public R login(@RequestParam("code") String code) {
        Boolean existByAppId = getExistByAppId();
        if (!existByAppId) {
            log.info("appId不存在，appId = {}", wxConfig.getAppId());
        }
        Code2SessionVo code2SessionVo = code2Session(code);
        if (null != code2SessionVo.getErrcode()) {
            return R.ofParamsError(code2SessionVo.getErrmsg());
        }
        code2SessionVo.setSessionKey("");
        return R.ofSuccess("登录凭证校验成功", code2SessionVo);
    }


    @GetMapping("/accessToken")
    public R getAccessToken () {
        String accessToken  = accessToken();
        if (StringUtils.isEmpty(accessToken)) {
            return R.ofParamsError("获取accessToken失败");
        } else {
            return R.ofSuccess(accessToken);
        }
    }

    /**
     * 生成二维码
     * @param uid
     * @return
     * @throws Exception
     */
    @GetMapping("/getWxCodeUid")
    public R getWxACodeUid(@RequestParam String uid)  throws Exception {
        Map<String, String> params = new HashMap<>(16);
        params.put("scene",uid);
        params.put("path","pages/menu/menu?uid="+uid);
        return R.ofSuccess(inputStreamBase64 (params));
    }


    /**
     * 图片转成base64
     * @param params
     * @return
     */
    private String inputStreamBase64 (Map<String, String> params)  throws Exception {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost("https://api.weixin.qq.com/wxa/getwxacode?access_token="+accessToken());
        httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");
        String body = JSON.toJSONString(params);
        StringEntity entity;
        entity = new StringEntity(body);
        entity.setContentType("image/png");
        httpPost.setEntity(entity);
        HttpResponse response;
        response = httpClient.execute(httpPost);
        InputStream inputStream = response.getEntity().getContent();
        return getBase64FromInputStream(inputStream);
    }


    private static String getBase64FromInputStream(InputStream in) {
        // 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        byte[] data = null;
        // 读取图片字节数组
        try {
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[100];
            int rc = 0;
            while ((rc = in.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            data = swapStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new String(org.apache.commons.codec.binary.Base64.encodeBase64(data));
    }


    private Code2SessionVo code2Session(String code) {
        Code2SessionVo code2SessionVo = JSON.toJavaObject(wxJsonObject(code), Code2SessionVo.class);
        return code2SessionVo;
    }

    private JSONObject wxJsonObject(String code){
        Map<String, String> params = new HashMap<>(16);
        params.put("appId", wxConfig.getAppId());
        params.put("secret", wxConfig.getSecret());
        params.put("jsCode", code);
        params.put("grantType", "authorization_code");
        String body = restTemplate.getForEntity(wxConfig.getCode2session(), String.class, params).getBody();
        JSONObject jsonObject = JSONObject.parseObject(body);
        return jsonObject;
    }

    private Boolean getExistByAppId() {
        JSONObject jsonObject = getToken();
        String errCode = jsonObject.getString("errcode");
        if (StringUtils.isEmpty(errCode)) {
            return true;
        } else {
            return false;
        }
    }


    private String accessToken() {
        JSONObject jsonObject = getToken();
        String errCode = jsonObject.getString("errcode");
        if (StringUtils.isEmpty(errCode)) {
            return jsonObject.getString("access_token");
        } else {
            return null;
        }
    }

    private JSONObject getToken() {
        Map<String, Object> map = new HashMap<>(16);
        map.put("appId", wxConfig.getAppId());
        map.put("appSecret", wxConfig.getSecret());
        ResponseEntity<String> forEntity = restTemplate.getForEntity(wxConfig.getClientCredential(), String.class, map);
        String body = forEntity.getBody();
        JSONObject jsonObject = JSONObject.parseObject(body);
        return jsonObject;
    }

    /**
     * 获得授权用户信息
     * @param encrypted
     * @param iv
     * @param code
     * @return
     */
    @GetMapping("/login/userinfo")
    public R<WxAuthDto> userInfo(@RequestParam("encrypted") String encrypted, @RequestParam("iv") String iv,
                                 @RequestParam("code") String code) {
        WxAuthDto wxAuthDto = new WxAuthDto();
        String sessionKey = getSessionKey(code);
        JSONObject userInfo = this.getUserInfo(encrypted, sessionKey, iv);
        log.info("微信授权用户信息参数:{}",userInfo);
        String country = userInfo.getString("country");
        String gender = userInfo.getString("gender");
        String province = userInfo.getString("province");
        String city = userInfo.getString("city");
        String avatarUrl = userInfo.getString("avatarUrl");
        String openId = userInfo.getString("openId");
        String nickName = userInfo.getString("nickName");
        String language = userInfo.getString("language");
        wxAuthDto.setCity(city);
        wxAuthDto.setCountry(country);
        wxAuthDto.setSex(Integer.valueOf(gender));
        wxAuthDto.setProvince(province);
        wxAuthDto.setHeadimg(avatarUrl);
        wxAuthDto.setNickname(nickName);
        wxAuthDto.setAppid(wxConfig.getAppId());
        return R.ofSuccess(wxAuthDto);
    }


    /**
     * 获得授权用户的手机号
     * @param encrypted
     * @param iv
     * @param code
     * @return
     */
    @GetMapping("/auth/phone")
    public R wxAuthPhone(@RequestParam("encrypted") String encrypted, @RequestParam("iv") String iv,
                         @RequestParam("code") String code){
        encrypted = encrypted.replace(" ","+");
        iv = iv.replace(" ","+");
        code = code.replace(" ","+");
        String sessionKey = getSessionKey(code);
        JSONObject userInfo = this.getUserInfo(encrypted, sessionKey, iv);
        return R.ofSuccess(userInfo.getString("phoneNumber"));
    }

    public String getSessionKey(String code) {
        JSONObject jsonObject = wxJsonObject(code);
        String session_key = (String) jsonObject.get("session_key");
        return session_key;
    }


    /**
     * 获取信息
     */
    public JSONObject getUserInfo(String encryptedData,String sessionkey,String iv){
        // 被加密的数据
        byte[] dataByte = Base64.decode(encryptedData);
        // 加密秘钥
        byte[] keyByte = Base64.decode(sessionkey);
        // 偏移量
        byte[] ivByte = Base64.decode(iv);
        try {
            // 如果密钥不足16位，那么就补足.  这个if 中的内容很重要
            int base = 16;
            if (keyByte.length % base != 0) {
                int groups = keyByte.length / base + (keyByte.length % base != 0 ? 1 : 0);
                byte[] temp = new byte[groups * base];
                Arrays.fill(temp, (byte) 0);
                System.arraycopy(keyByte, 0, temp, 0, keyByte.length);
                keyByte = temp;
            }
            // 初始化
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding","BC");
            SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
            parameters.init(new IvParameterSpec(ivByte));
            // 初始化
            cipher.init(Cipher.DECRYPT_MODE, spec, parameters);
            byte[] resultByte = cipher.doFinal(dataByte);
            if (null != resultByte && resultByte.length > 0) {
                String result = new String(resultByte, "UTF-8");
                return JSONObject.parseObject(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
