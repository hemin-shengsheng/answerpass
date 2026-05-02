package com.ayan.answerpass.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ayan.answerpass.model.entity.Post;
import java.util.Date;
import java.util.List;

/**
 * 帖子数据库操作
 *
 * @author <a href="#">程序员沈笙</a>
 * @from <a href="https://#.icu">编程导航知识星球</a>
 */
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 查询帖子列表（包括已被删除的数据）
     */
    List<Post> listPostWithDelete(Date minUpdateTime);

}




