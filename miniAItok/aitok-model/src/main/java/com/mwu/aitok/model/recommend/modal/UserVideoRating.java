package com.mwu.aitok.model.recommend.modal;

import lombok.Data;

import java.util.List;

/**
 * UserVideoRating
 *
 * @AUTHOR: mwu
 * @DATE: 2024/4/27
 **/
@Data
public class UserVideoRating {
    private Long userId;
    private List<VideoScore> videoScoreList;
}
