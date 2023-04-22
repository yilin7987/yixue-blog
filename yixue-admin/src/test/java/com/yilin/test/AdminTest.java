package com.yilin.test;

import com.yilin.yixueblog.controller.AdminController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class AdminTest {
    public static void main(String[] args) throws InterruptedException, IOException {
        // 在Java中创建一个ProcessBuilder对象，指定python命令和脚本路径
        ProcessBuilder processBuilder = new ProcessBuilder("C:\\Users\\yilin\\Desktop\\python\\blog\\venv\\Scripts\\python.exe", "C:\\Users\\yilin\\Desktop\\python\\blog\\similarityBlog.py");
        // 设置参数列表，可以是任意类型和数量
        List<String> arguments = new ArrayList<>();
        arguments.add("1595059714108973058");
        // 将参数列表添加到ProcessBuilder对象中
        processBuilder.command().addAll(arguments);
        // 启动进程并获取Process对象
        Process process = processBuilder.start();
        // 从进程的输入流中读取输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        //错误流
        BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line); // 这里打印的就是Python脚本的返回值
        }
        // 等待进程结束并获取退出码
        reader.close();
        int exitCode = process.waitFor();
        System.out.println("Exit code: " + exitCode);
        String err = null;
        while ((err = error.readLine()) != null) {
            System.out.println(err);
        }
        error.close();

    }

}
