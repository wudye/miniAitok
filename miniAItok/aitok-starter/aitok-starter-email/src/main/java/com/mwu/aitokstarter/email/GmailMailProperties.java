package com.mwu.aitokstarter.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gmail.mail")
public class GmailMailProperties {
    /**
     * 是否启用自动装配
     */
    private boolean enabled = true;

    private String username;
    private String password;
    private String host = "smtp.gmail.com";
    private int port = 587;
    private String protocol = "smtp";
    private boolean auth = true;
    private boolean starttlsEnable = true;
    private String defaultFrom;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    public boolean isAuth() { return auth; }
    public void setAuth(boolean auth) { this.auth = auth; }

    public boolean isStarttlsEnable() { return starttlsEnable; }
    public void setStarttlsEnable(boolean starttlsEnable) { this.starttlsEnable = starttlsEnable; }

    public String getDefaultFrom() { return defaultFrom; }
    public void setDefaultFrom(String defaultFrom) { this.defaultFrom = defaultFrom; }

    }