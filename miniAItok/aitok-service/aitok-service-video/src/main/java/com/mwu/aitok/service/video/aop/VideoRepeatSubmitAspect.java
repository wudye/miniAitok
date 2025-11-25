package com.mwu.aitok.service.video.aop;

import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitok.service.video.annotation.VideoRepeatSubmit;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.aitiokcoomon.core.utils.spring.SpElUtils;
import jakarta.annotation.Resource;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class VideoRepeatSubmitAspect {

    @Resource
    private RedisService redisService;

    /*


    */
    @Before(("@annotation(VideoRepeatSubmit)"))
    public void doBefore(JoinPoint point, VideoRepeatSubmit videoRepeatSubmit) {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        String prefixKey = SpElUtils.getMethodKey(method);
        String key = SpElUtils.parseSpEl(method, point.getArgs(), videoRepeatSubmit.key());

        String redisKey = prefixKey + "_" + key + "_" + UserContext.getUserId();
        if (redisService.hasKey(redisKey)) {
            throw new RuntimeException(videoRepeatSubmit.message());
        }
        redisService.setCacheObject(redisKey, 1, videoRepeatSubmit.interval(), videoRepeatSubmit.timeunit());

    }

    @After("@annotation(videoRepeatSubmit)")
    public void doAfter(JoinPoint point, VideoRepeatSubmit videoRepeatSubmit) throws Throwable {

    }


}