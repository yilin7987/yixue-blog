package com.yilin.yixueblog.utils;




/**
 * 文件操作工具类
 */
public class FileUtils {
    /**
     * 获取后缀名
     * @param fileName
     * @return
     */
    public static String getPicExpandedName(String fileName) {
        String ext = "";
        if (StringUtils.isNotBlank(fileName) &&
                StringUtils.contains(fileName, ".")) {
            ext = StringUtils.substring(fileName, fileName.lastIndexOf(".") + 1);
        }
        ext = ext.toLowerCase();
        if (ext == null || ext.length() < 1) {
            ext = "jpg";
        }

        return ext;
    }
}
