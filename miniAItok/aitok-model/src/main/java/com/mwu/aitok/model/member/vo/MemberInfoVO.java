package com.mwu.aitok.model.member.vo;



import com.fasterxml.jackson.annotation.JsonFormat;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.member.domain.MemberInfo;
import com.mwu.aitok.model.member.vo.app.AppMemberInfoVO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * MemberInfoVO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/11/12
 * 用户详情返回体
 **/
@NoArgsConstructor
@Data
public class MemberInfoVO  {

    private Long infoId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 个人页面背景图片
     */
    private String backImage;
    /**
     * 个人描述
     */
    private String description;
    /**
     * 生日
     */
    private LocalDate birthday;
    /**
     * 省
     */
    private String province;
    /**
     * 市
     */
    private String city;
    /**
     * 区
     */
    private String region;
    /**
     * 邮编
     */
    private String adcode;
    /**
     * 学校
     */
    private String campus;
    /**
     * 喜欢视频向外展示状态：0展示1隐藏
     */
    private String likeShowStatus;

    /**
     * 收藏视频向外展示状态：0展示1隐藏
     */
    private String favoriteShowStatus;


}
