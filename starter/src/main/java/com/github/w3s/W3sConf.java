package com.github.w3s;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author wang xiao
 * date 2022/10/24
 */
@Configuration

@ConfigurationProperties(prefix = "w3s")
public class W3sConf {


    private String wsUrlPrefix;

    private Integer sendTimeOut = 5000;

    private Integer maxSubOfSession = 10;

    private Integer pingTimeout = 30000;

    private Integer numberOfPingAttempts = 20;

    public String getWsUrlPrefix() {
        return wsUrlPrefix;
    }

    public void setWsUrlPrefix(String wsUrlPrefix) {
        this.wsUrlPrefix = wsUrlPrefix;
    }

    public Integer getSendTimeOut() {
        return sendTimeOut;
    }

    public void setSendTimeOut(Integer sendTimeOut) {
        this.sendTimeOut = sendTimeOut;
    }

    public Integer getMaxSubOfSession() {
        return maxSubOfSession;
    }

    public void setMaxSubOfSession(Integer maxSubOfSession) {
        this.maxSubOfSession = maxSubOfSession;
    }

    public Integer getPingTimeout() {
        return pingTimeout;
    }

    public void setPingTimeout(Integer pingTimeout) {
        this.pingTimeout = pingTimeout;
    }

    public Integer getNumberOfPingAttempts() {
        return numberOfPingAttempts;
    }

    public void setNumberOfPingAttempts(Integer numberOfPingAttempts) {
        this.numberOfPingAttempts = numberOfPingAttempts;
    }
}
