package com.yilin.yixueblog;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "全局统一返回结果")
public class Result<T> {

    @ApiModelProperty(value = "返回码")
    private Integer code;

    @ApiModelProperty(value = "返回消息")
    private String message;

    @ApiModelProperty(value = "返回数据")
    private T data;

    private  String token;

    public Result(){}

    //成功
    public static<T> Result<T> succeed(){
        Result<T> result = new Result<>();
        result.setData(null);
        result.setCode(20000);
        result.setMessage("成功");
        result.setToken("admin-token");
        return result;
    }

    //失败
    public static<T> Result<T> err(){
        Result<T> result = new Result<>();
        result.setData(null);
        result.setCode(20001);
        result.setMessage("失败");
        return result;
    }

    public Result<T> data(T data){
        this.setData(data);
        return this;
    }
    public Result<T> message(String msg){
        this.setMessage(msg);
        return this;
    }

    public Result<T> code(Integer code){
        this.setCode(code);
        return this;
    }
}