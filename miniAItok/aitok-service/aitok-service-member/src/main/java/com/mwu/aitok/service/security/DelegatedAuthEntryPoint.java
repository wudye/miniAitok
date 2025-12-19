package com.mwu.aitok.service.security;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;


@Component
public class DelegatedAuthEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver handlerExceptionResolver;

    public DelegatedAuthEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)  {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        //  response.getWriter().write("{\"error\":\"未认证\",\"message\":\"" + authException.getMessage() + "\"}");
        handlerExceptionResolver.resolveException(request, response, null, authException);

    }
}


/*
这段代码实现了一个自定义的 AuthenticationEntryPoint，用于处理未认证用户的请求。它将异常处理委托给 Spring MVC 的 HandlerExceptionResolver，这样可以统一返回自定义的错误响应（如 JSON）。
示例：

假设你有如下异常处理器：
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthException(AuthenticationException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "未认证", "message", ex.getMessage()));
    }
}
当用户访问需要认证的接口但未登录时，Spring Security 会调用 DelegatedAuthEntryPoint 的 commence 方法：
@Override
public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
    handlerExceptionResolver.resolveException(request, response, null, authException);
}

当用户未登录访问受保护接口时，DelegatedAuthEntryPoint 会将异常交给该处理器，最终前端收到如下 JSON 响应：
{
  "error": "未认证",
  "message": "Full authentication is required to access this resource"
}
这样就实现了统一的认证异常处理，前后端分离项目可以方便地解析和展示错误信息。
 */