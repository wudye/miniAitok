package com.mwu.aitok.service.video.domain;

import lombok.Data;
import ws.schild.jave.info.MultimediaInfo;

/**
 * 选中的代码 import ws.schild.jave.info.MultimediaInfo; 是一个导入语句，用于引入外部库 ws.schild.jave 中的 MultimediaInfo 类。这个类通常与多媒体处理相关，提供视频或音频文件的元信息，例如格式、时长、比特率、分辨率等。
 *
 * @AUTHOR: roydon
 * @DATE: 2024/2/4
 **/
@Data
public class MediaVideoInfo {

    private String format = null;
    private long duration = -1L;
    private String decoder;
    private int bitRate = -1;
    private float frameRate = -1.0F;
    private Integer width;
    private Integer height;

    public MediaVideoInfo(MultimediaInfo multimediaInfo) {
        this.format = multimediaInfo.getFormat();
        this.duration = multimediaInfo.getDuration();
        if(multimediaInfo.getVideo()!= null){
            this.decoder = multimediaInfo.getVideo().getDecoder();
            this.bitRate = multimediaInfo.getVideo().getBitRate();
            this.frameRate = multimediaInfo.getVideo().getFrameRate();
        }
        if (multimediaInfo.getVideo().getSize() != null) {
            this.width = multimediaInfo.getVideo().getSize().getWidth();
            this.height = multimediaInfo.getVideo().getSize().getHeight();
        }
    }

}
