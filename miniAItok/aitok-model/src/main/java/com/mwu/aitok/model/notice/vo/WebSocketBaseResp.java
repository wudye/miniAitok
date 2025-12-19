package com.mwu.aitok.model.notice.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocketBaseResp
 *
 * @AUTHOR: mwu
 * @DATE: 2024/3/8
 **/
@NoArgsConstructor
@Data
public class WebSocketBaseResp<T> {

    private String type;
    private T msg;

    public static <T> WebSocketBaseResp<T> build(String type, T msg){
        WebSocketBaseResp<T> res = new WebSocketBaseResp<>();
        res.setType(type);
        res.setMsg(msg);
        return res;
    }

}
