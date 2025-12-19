package com.mwu.aitok.model.video.vo;

import lombok.Data;

import java.util.List;

/**
 * web端
 * 视频大屏播放，侧边栏合集tab
 * 视频合集视频列表
 *
 * @AUTHOR: mwu
 * @DATE: 2024/5/15
 **/
@Data
public class UserVideoCompilationVideoPageVO {
    private UserVideoCompilationInfoVO userVideoCompilationInfoVO;
    private List<VideoVO> videoVOList;
}
