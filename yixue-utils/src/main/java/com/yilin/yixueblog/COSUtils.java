package com.yilin.yixueblog;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.yilin.yixueblog.config.COSConfig;
import com.yilin.yixueblog.entity.Picture;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;



/**
 * 腾讯云对象存储工具类
 */
@Component
public class COSUtils {
    @Autowired
    private COSClient cosClient;

    @Autowired
    private COSConfig cosconfig;

    public String upload(InputStream inputStream, String uid ,String picExpandedName) throws IOException {

        String dateUrl = new DateTime().toString("yyyy/MM/dd");
        String path;
        // jpg/2022/11/21/15415151515515.jpg
        if ("avatar".equals(picExpandedName.split("/")[0])){
            //如果是头像
            String[] split = picExpandedName.split("/");
            path = picExpandedName+"/"+dateUrl + "/" + uid+"."+split[split.length-1];
        }else {
            path = picExpandedName+"/"+dateUrl + "/" + uid+"."+picExpandedName;
        }
        ObjectMetadata objectMetadata = new ObjectMetadata();
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosconfig.getBucketName(), path, inputStream, objectMetadata);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        if (putObjectResult.getETag() != null) {
            //https://ggkt-atguigu-1310644373.cos.ap-beijing.myqcloud.com/01.jpg
            String url = cosconfig.getUrl() + "/" + path;
            return url;
        }
        return "上传失败";
    }
}
