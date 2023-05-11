package com.yilin.yixueblog.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Author Lints
 * @Date  19:31
 * @Description MD5工具类
 * @Since version-1.0
 */
public class MD5Utils {
    //打印日志
//    private static Logger logger = LoggerFactory.getLogger(MD5Utils.class);

    /**
     * MD5加码 生成32位md5码(不可逆的)
     * @param inStr
     * @return String
     */
    public static String string2MD5(String inStr) {
        MessageDigest md5;
        String string = "";
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
//            logger.error("MD5加密实现的错误日志-->>" + e.getMessage(), e);
            return string;
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        //遍历md5Bytes数组中的每个字节，将其转换成一个0~255之间的整数
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            //如果val小于16，说明它只有一位十六进制数，需要在前面补一个0
            if (val < 16) {
                hexValue.append("0");
            }
            //将val转换成十六进制数
            hexValue.append(Integer.toHexString(val));
        }
        string = hexValue.toString();
//        logger.debug("MD5加密的32位密钥的调试日志-->>" + string);
        return string;
    }

    /**
     * 加密解密算法 执行一次加密，两次就是解密
     * @param inStr
     * @return
     * @throws Exception
     */
    public static String convertMD5(String inStr) throws Exception {
        //简单来说，就是对一个字符串进行一种简单的加密或解密操作，使用字符’t’作为密钥
        char[] a = inStr.toCharArray();
        for (int i = 0; i < a.length; i++) {
            a[i] = (char) (a[i] ^ 't');
        }
        String string = new String(a);
//        logger.debug("MD5加密的二次加密的字符串的调试日志-->>" + string);
        return string;
    }

}
