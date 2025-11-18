package com.mwu.aitok.model.search.dubbo;

import lombok.Data;

import java.io.Serializable;

//TODO: replace with grpc generated class
/**
 * VideoBehaveData
 *
 *
 * @AUTHOR: mwu
 * @DATE: 2024/5/19
 **/
@Data
public class VideoBehaveData implements Serializable {
    private static final long serialVersionUID = 112321L;

    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long favoriteCount;
}
