package com.mwu.aitok.service.video.controller.temp_app;


import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.video.dto.CategoryVideoPageDTO;
import com.mwu.aitok.model.video.vo.app.AppVideoCategoryVo;
import com.mwu.aitok.service.video.service.IVideoCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * (VideoCategory)表控制层
 *
 * @author mwu
 * @since 2023-10-30 19:41:13
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app/category")
public class AppVideoCategoryController {

    /*
    题就出在这个 WebMvcConfig.java 文件里。我们来分析一下它的代码：1.configurePathMatch 方法：
    这个方法是用来配置 Spring MVC 的路径匹配规则的。你的代码在这里调用了三次 configurePathMatch，
    分别针对 adminApi, webApi, 和 appApi。2.configurePathMatch (私有方法)： 这个方法是核心。它调用了
     configurer.addPathPrefix(...)。•addPathPrefix(api.getPrefix(), ...)：这个方法的作用就是给满足特定条件的
     Controller 添加一个统一的 URL 前缀。•clazz.isAnnotationPresent(RestController.class)
     ：条件之一是这个类必须有 @RestController 注解。•antPathMatcher.match(api.getController(), clazz.getPackage().getName())：
     第二个条件是，这个 Controller 的包名必须匹配 api.getController() 中定义的模式。3.WebProperties 类： Web
     MvcConfig 从 WebProperties 类中获取前缀和包名模式。这个 WebProperties 类上一定有 @ConfigurationProperties(pre
     fix = "aitok.web") 这样的注解，意味着它的配置项都来自 application.yml 中 aitok.web 下面的内容。结论：
     你的 AppVideoCategoryController 的包名是 com.mwu.aitok.service.video.controller.app。
     这个包名一定匹配了 WebProperties 中 appApi 的 controller 模式。同时，appApi 的 prefix 被设置成了
      /app-api。因此，Spring 自动给你的 AppVideoCategoryController 下的所有接口都加上了
      /app-api 这个前缀。所以，你以为的路径： GET /api/v1/app/category/test实际上在服务器端的真实路径是
      ： GET /app-api/api/v1/app/category/test
     */

    private final IVideoCategoryService videoCategoryService;
    @GetMapping("/test")
    public String test() {
        return "app test";
    }
    /**
     * 获取所有可用视频父分类
     */
    @GetMapping("/parent")
    public R<List<AppVideoCategoryVo>> getNormalParentCategory() {
        return R.ok(videoCategoryService.getNormalParentCategory());
    }

    /**
     * 获取所有可用视频父分类
     */
    @GetMapping("/children/{id}")
    public R<List<AppVideoCategoryVo>> getNormalParentCategory(@PathVariable("id") Long id) {
        return R.ok(videoCategoryService.getNormalChildrenCategory(id));
    }

    /**
     * 分页分类视频
     */
    @PostMapping("/videoPage")
    public PageData<?> getVideoByCategoryId(@Validated @RequestBody CategoryVideoPageDTO pageDTO) {
        return videoCategoryService.getVideoPageByCategoryId(pageDTO);
    }



}

