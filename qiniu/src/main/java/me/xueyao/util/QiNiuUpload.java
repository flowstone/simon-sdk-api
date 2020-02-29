package me.xueyao.util;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import me.xueyao.config.QiNiuConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 七牛云上传工具类
 * @author Simon.Xue
 * @date 2019-12-12 23:39
 **/
@Component
public class QiNiuUpload {

    @Autowired
    private QiNiuConfig qiNiuConfig;

    /**
     * 表单上传文件
     * @param uploadBytes 文件内容
     * @param fileName  文件名称
     * @return 上传成功 true  上传失败 false
     * @throws IOException
     */
    public Boolean uploadImg(byte[] uploadBytes, String fileName) throws IOException {
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region0());
        UploadManager uploadManager = new UploadManager(cfg);

        Auth auth = Auth.create(qiNiuConfig.getAccessKey(), qiNiuConfig.getSecretKey());
        String uploadToken = auth.uploadToken(qiNiuConfig.getBucket());

        Response response = uploadManager.put(uploadBytes, fileName, uploadToken);

        if (null == response) {
            return Boolean.FALSE;
        }

        if (response.statusCode == HttpStatus.OK.value()) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    /**
     * 上传URL文件
     * @param imgUrl  文件路径
     * @param fileName 文件名称
     * @return upload success true
     */
    public Boolean uploadImgUrl(String imgUrl, String fileName) throws QiniuException {
        //构造一个带指定 Region 对象的配置类
        UploadManager uploadManager = new UploadManager(new Configuration(Region.region0()));
        Auth auth = Auth.create(qiNiuConfig.getAccessKey(), qiNiuConfig.getSecretKey());
        String uploadToken = auth.uploadToken(qiNiuConfig.getBucket());
        Response response = uploadManager.put(imgUrl, fileName, uploadToken);
        if (null == response) {
            return false;
        }
        if (response.statusCode == HttpStatus.OK.value()) {
            return true;
        }
        return false;
    }
}
