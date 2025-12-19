package com.mwu.aitok.service.security;

public class CorUrlConstant {

    public   final  static String[]  API_URLS_ALLOWED = {
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/**",
            "/prometheus/**",
            "/elasticsearch/**",
            "/grafana/**",
            "/kafka/**",
            "/api/account/info",
    };

    public final static  String[] API_URLS_ALLOWED_ADMIN = {
            "/api/group/page",
            "/api/user/page",

    };

    public final static String[] API_URLS_ALLOWED_USER = {
            "/api/account/register",
            "/api/account/login",
            "/api/account/refresh-token",

    };



    public final static String[] FOR_OAUT2 = {

            "/login/oauth2/code/**",
            "/oauth2/authorization/**"
    };

}
