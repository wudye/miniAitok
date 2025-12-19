package com.mwu.aitok.model.video.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mwu.aitok.model.video.domain.Video;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 功能：
 * 作者：mwu
 * 日期：2023/10/29 19:55
 */
@Data
public class VideoPageDto  {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long videoId;


    /**
     * 用户id
     */
    private Long userId;

    @Size(min = 1, max = 100, message = "标题需在100字符以内")
    private String videoTitle;

    @Size(min = 1, max = 200, message = "描述需在200字符以内")
    private String videoDesc;
    /**
     * 视频封面地址
     */
    private String coverImage;
    /**
     * 视频地址
     */
    private String videoUrl;
    private Long viewNum;
    private Long likeNum;
    private Long favoritesNum;
    /**
     * 发布类型（0视频，1图文）
     */
    private String publishType;
    /**
     * 展示类型（0全部可见1好友可见2自己可见）
     */
    private String showType;
    /**
     * 定位功能0关闭1开启
     */
    private String positionFlag;
    /**
     * 审核状态(0:待审核1:审核成功2:审核失败)
     */
    private String auditsStatus;
    /**
     * 视频详情
     */
    private String videoInfo;
    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private String delFlag;
    /**
     * 创建者
     */
    private String createBy;
    /**
     * 创建时间
     */

    private LocalDateTime createTime;
    /**
     * 更新者
     */
    private String updateBy;
    /**
     * 更新时间
     */

    private LocalDateTime updateTime;
}

