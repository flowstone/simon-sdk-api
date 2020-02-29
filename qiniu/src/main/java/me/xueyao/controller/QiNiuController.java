package me.xueyao.controller;

import com.qiniu.common.QiniuException;
import me.xueyao.base.R;
import me.xueyao.util.QiNiuUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * @author Simon.Xue
 * @date 2019-12-13 00:07
 **/
@RestController
@RequestMapping("/qiniu")
public class QiNiuController {

    @Autowired
    private QiNiuUpload qiNiuUpload;

    @Value("${qiniu.url}")
    private String httpUrl;

    /**
     * 表单上传文件
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public R uploadImg(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + new Random().nextInt(1000);
        String originalFilename = file.getOriginalFilename();
        String fileSuffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        originalFilename = fileName + "." + fileSuffix;
        String fullPath = httpUrl + fileName + "." + fileSuffix;

        Boolean status = qiNiuUpload.uploadImg(bytes, originalFilename);
        if (status) {
            return R.ofSuccess("上传成功", fullPath);
        }
        return R.ofParamError("上传失败");
    }


    /**
     * 上传URL文件
     * @param httpUrl
     * @return
     * @throws QiniuException
     */
    @GetMapping("/uploadUrl")
    public R uploadImgUrl(@RequestParam("httpUrl") String httpUrl) throws QiniuException {
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + new Random().nextInt(1000);
        String originalFilename = fileName + ".png";
        String fullPath = httpUrl + fileName + ".png";

        Boolean status = qiNiuUpload.uploadImgUrl(httpUrl, originalFilename);
        if (status) {
            return R.ofSuccess("上传成功", fullPath);
        }
        return R.ofParamError("上传失败");
    }
}
