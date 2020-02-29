package me.xueyao.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 获取七牛云相关配置
 * @author Simon.Xue
 * @date 2019-12-12 23:30
 **/
@Configuration
@Getter
public class QiNiuConfig {

    @Value("${qiniu.accessKey}")
    private String accessKey;

    @Value("${qiniu.secretKey}")
    private String secretKey;

    @Value(("${qiniu.bucket}"))
    private String bucket;
}
