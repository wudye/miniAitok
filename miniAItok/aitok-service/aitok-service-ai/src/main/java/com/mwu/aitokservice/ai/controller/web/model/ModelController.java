package com.mwu.aitokservice.ai.controller.web.model;


import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.ai.vo.model.ModelVO;
import com.mwu.aitokservice.ai.service.IChatModelService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ModelController
 *
 * @AUTHOR: roydon
 * @DATE: 2025/6/7
 **/
@RestController
@RequestMapping("v1/model")
public class ModelController {

    @Resource
    private IChatModelService chatModelService;

    @GetMapping("/list")
    public R<List<ModelVO>> getModelPage(@RequestParam(value = "type", required = false) String type,
                                         @RequestParam(value = "platform", required = false) String platform) {
        return R.ok(chatModelService.getModelList(type, platform));
    }

}
