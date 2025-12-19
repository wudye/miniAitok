package com.mwu.aitok.service.video.aop;

import com.mwu.aitok.service.video.creator.controller.GetUserId;
import com.mwu.aitok.service.video.annotation.VideoRepeatSubmit;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.aitiokcoomon.core.utils.spring.SpElUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class VideoRepeatSubmitAspect {

    @Resource
    private RedisService redisService;

    /*
@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @PostMapping("/upload")
    @VideoRepeatSubmit(
        key = "#videoDTO.title",  // 使用视频标题作为业务标识
        interval = 30,            // 30秒内不能重复
        timeunit = TimeUnit.SECONDS,
        message = "视频正在上传中，请勿重复提交"
    )
    public ResponseEntity<String> uploadVideo(@RequestBody VideoDTO videoDTO) {
        // 业务逻辑
        return ResponseEntity.ok("上传成功");
    }
}
根据注解定义，当前的默认配置是：

interval = 5000 毫秒（5秒）
timeunit = TimeUnit.MILLISECONDS （毫秒）
message = "您的操作太快了，请稍候再试"

    */
    @Before("@annotation(videoRepeatSubmit)")
    public void doBefore(JoinPoint point, VideoRepeatSubmit videoRepeatSubmit) {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        String prefixKey = SpElUtils.getMethodKey(method);
        String key = SpElUtils.parseSpEl(method, point.getArgs(), videoRepeatSubmit.key());

        String redisKey = prefixKey + "_" + key + "_" + GetUserId.getUserId();
        if (redisService.hasKey(redisKey)) {
            throw new RuntimeException(videoRepeatSubmit.message());
        }
        redisService.setCacheObject(redisKey, 1, videoRepeatSubmit.interval(), videoRepeatSubmit.timeunit());

    }

    @After("@annotation(videoRepeatSubmit)")
    public void doAfter(JoinPoint point, VideoRepeatSubmit videoRepeatSubmit) throws Throwable {
        try {
            // 记录成功日志
            log.info("方法执行完成: {}",
                    ((MethodSignature) point.getSignature()).getMethod().getName());
        } catch (Exception e) {
            log.warn("After通知执行失败", e);
        }
    }


}