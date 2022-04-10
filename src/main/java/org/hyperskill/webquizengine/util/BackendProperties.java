package org.hyperskill.webquizengine.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties("backend.maxima")
public class BackendProperties {

    @NotBlank
    private String ip;
    @NotBlank
    private String healthcheckUrl;
    @NotBlank
    private int port;
    @NotBlank
    private int timeout;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getHealthcheckUrl() {
        return healthcheckUrl;
    }

    public void setHealthcheckUrl(String healthcheckUrl) {
        this.healthcheckUrl = healthcheckUrl;
    }
}