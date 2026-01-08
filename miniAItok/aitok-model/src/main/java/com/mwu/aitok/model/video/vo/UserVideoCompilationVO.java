package com.mwu.aitok.model.video.vo;

import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import lombok.*;

/**
 * UserVideoCompilationVO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/11/27
 **/
@Getter
@Setter
@NoArgsConstructor
public class UserVideoCompilationVO {
    private Long compilationId;
    private Long userId;
    private String title;
    private String description;
    private String coverImage;

    // 播放量
    private Long viewCount;
    // 获赞量
    private Long likeCount;
    // 被收藏数
    private Long favoriteCount;
    // 视频数
    private Long videoCount;

    private UserVideoCompilation userVideoCompilation;

    public UserVideoCompilationVO(UserVideoCompilation userVideoCompilation) {
        this.userVideoCompilation = userVideoCompilation;
    }

    public static UserVideoCompilationVO build(UserVideoCompilation userVideoCompilation) {
        return new UserVideoCompilationVO(userVideoCompilation);
    }


}
