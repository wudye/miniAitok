package com.mwu.aitiokcoomon.core.exception;

import com.mwu.aitok.model.common.enums.HttpCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;

@Getter
public class CustomException extends RuntimeException {

    private HttpCodeEnum httpCodeEnum;

    public CustomException(HttpCodeEnum httpCodeEnum) {
        super(httpCodeEnum.getMsg());
        this.httpCodeEnum = httpCodeEnum;
    }
    public CustomException(HttpCodeEnum httpCodeEnum, String message) {
        super(message);
        this.httpCodeEnum = httpCodeEnum;
    }
    public CustomException() {
        super();
    }


}
