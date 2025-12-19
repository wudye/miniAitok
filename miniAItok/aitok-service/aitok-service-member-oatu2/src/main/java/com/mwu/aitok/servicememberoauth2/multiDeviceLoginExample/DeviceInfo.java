package com.mwu.aitok.servicememberoauth2.multiDeviceLoginExample;


import java.io.Serializable;

public class DeviceInfo implements Serializable {
    private String deviceId;
    private String deviceType; // e.g. "PC", "MOBILE", "OTHER"
    private String userAgent;
    private String ip;

    public DeviceInfo() {}

    public DeviceInfo(String deviceId, String deviceType, String userAgent, String ip) {
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.userAgent = userAgent;
        this.ip = ip;
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
}
