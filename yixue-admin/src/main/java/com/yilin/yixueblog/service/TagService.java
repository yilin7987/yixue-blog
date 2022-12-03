package com.yilin.yixueblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.vo.TagVO;

import java.util.List;

/**
 * <p>
 * 标签表 服务类
 * </p>
 * @author yilin
 * @since 2022-11-19
 */
public interface TagService extends IService<Tag> {

    /**
     * 新增博客标签
     * @param tagVO
     */
    String addTag(TagVO tagVO);

    /**
     * 获取博客标签列表
     * @param tagVO
     * @return
     */
    IPage<Tag> getPageList(TagVO tagVO);

    /**
     * 编辑博客标签
     * @param tagVO
     */
    String editTag(TagVO tagVO);

    /**
     * 删除标签
     * @param tagUidList
     * @return
     */
    String deleteTag(List<String> tagUidList);

}
