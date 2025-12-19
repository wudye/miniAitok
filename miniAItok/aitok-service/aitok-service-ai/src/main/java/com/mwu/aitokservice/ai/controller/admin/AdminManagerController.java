package com.mwu.aitokservice.ai.controller.admin;


import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.ai.AiManagerUserInfo;
import com.mwu.aitokservice.ai.service.IManagerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端管理员接口
 *
 * @AUTHOR: roydon
 * @DATE: 2025/5/30
 **/
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/manager")
public class AdminManagerController {

    private final IManagerService managerService;

//    @PrePermission("manager:login")
    @PostMapping("/login")
    public R<String> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(managerService.login(dto));
    }

    @GetMapping("/userInfo")
    public R<AiManagerUserInfo> userInfo( ) {
        return R.ok(managerService.userInfo());
    }

    public record LoginDTO(@NotBlank(message = "用户名不能为空") String username,
                           @NotBlank(message = "密码不能为空") String password) {
    }
}
