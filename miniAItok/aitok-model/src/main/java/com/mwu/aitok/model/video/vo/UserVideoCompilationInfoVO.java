package com.mwu.aitok.model.video.vo;

import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.plaf.PanelUI;

/**
 * web页面
 * 大屏播放视频合集tab
 *
 * @AUTHOR: mwu
 * @DATE: 2024/5/15
 **/
@Data
@NoArgsConstructor
public class UserVideoCompilationInfoVO  {
    private Long playCount;
    private Long favoriteCount;
    private Long videoCount;
    private Boolean weatherFollow;

    private UserVideoCompilation userVideoCompilation;

    public UserVideoCompilationInfoVO(UserVideoCompilation userVideoCompilation) {
        this.userVideoCompilation = userVideoCompilation;
    }
    public static UserVideoCompilationInfoVO build(UserVideoCompilation userVideoCompilation) {
        return new UserVideoCompilationInfoVO(userVideoCompilation);
    }
}
