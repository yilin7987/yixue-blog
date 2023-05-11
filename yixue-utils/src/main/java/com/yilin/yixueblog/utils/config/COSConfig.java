package com.yilin.yixueblog.utils.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class COSConfig {
    @Value("${COS.secretId}")
    private String secretId;
    @Value("${COS.secretKey}")
    private String secretKey;
    @Value("${COS.region}")
    private String region;
    @Value("${COS.bucketName}")
    private String bucketName;
    @Value("${COS.url}")
    private String url;

    @Bean
    public COSClient cosClient() {
        //初始化用户信息
        COSCredentials cosCredentials = new BasicCOSCredentials(this.secretId, this.secretKey);
        //设置地域
        Region region = new Region(this.region);
        ClientConfig config = new ClientConfig(region);
        //生成COS客户端
        COSClient client = new COSClient(cosCredentials, config);
        return client;
    }
}
