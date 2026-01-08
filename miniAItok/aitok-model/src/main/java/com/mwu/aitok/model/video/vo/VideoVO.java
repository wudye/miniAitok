package com.mwu.aitok.model.video.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.model.video.domain.VideoPosition;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * VideoVO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/10/31
 **/
@NoArgsConstructor
@Data
public class VideoVO  {

    private Long commentNum;
    private String userNickName;
    private String userAvatar;
    // 是否点赞
    private boolean weatherLike;
    // 是否收藏
    private boolean weatherFavorite;
    // 是否关注
    private boolean weatherFollow;
    // 标签数组
    private String[] tags;
    // 图片集合
    private String[] imageList;
    private Long positionId;
    /**
     * 视频id
     */
    private String videoId;
    /**
     * 经度
     */
    private Double longitude;
    /**
     * 纬度
     */
    private Double latitude;
    /**
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 城市code
     */
    private String cityCode;
    /**
     * 区
     */
    private String district;
    /**
     * 街道
     */
    private String township;
    /**
     * 邮编
     */
    private String adcode;
    /**
     * 地址
     */
    private String address;
    /**
     * 状态标志（0：启用、1：禁用）
     */
    private String status;
    // 视频所在视频合集
    @JsonIgnore
    private UserVideoCompilationInfoVO userVideoCompilationInfoVO;

    // 热力值
    private Double hotScore;

    private Long id;


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
