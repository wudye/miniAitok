package com.mwu.aitiokcoomon.core.exception;

import com.mwu.aitok.model.common.enums.HttpCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException {

    private HttpCodeEnum httpCodeEnum;
}
