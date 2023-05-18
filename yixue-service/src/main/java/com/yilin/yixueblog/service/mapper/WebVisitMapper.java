package com.yilin.yixueblog.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yilin.yixueblog.model.entity.WebVisit;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


public interface WebVisitMapper extends BaseMapper<WebVisit> {


    /**
     * 获取IP数目
     *
     * @param startTime
     * @param endTime
     * @return
     */
    @Select("SELECT COUNT(ip) FROM (SELECT ip FROM tb_web_visit WHERE create_time >= #{startTime} AND create_time <= #{endTime} GROUP BY ip) AS tmp")
    Integer getIpCount(@Param("startTime") String startTime, @Param("endTime") String endTime);

}
