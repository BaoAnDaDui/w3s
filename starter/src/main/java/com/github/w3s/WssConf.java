package com.github.w3s;

/**
 * @author wang xiao
 * date 2022/10/24
 */
public  class WssConf {


    private String wsUrlPrefix;

    private int sendTimeOut;

    private int maxSubOfSession;

    private int pingTimeout;

    private int numberOfPingAttempts;

    public String getWsUrlPrefix() {
        return wsUrlPrefix;
    }

    public void setWsUrlPrefix(String wsUrlPrefix) {
        this.wsUrlPrefix = wsUrlPrefix;
    }

    public int getSendTimeOut() {
        return sendTimeOut;
    }

    public void setSendTimeOut(int sendTimeOut) {
        this.sendTimeOut = sendTimeOut;
    }

    public int getMaxSubOfSession() {
        return maxSubOfSession;
    }

    public void setMaxSubOfSession(int maxSubOfSession) {
        this.maxSubOfSession = maxSubOfSession;
    }

    public int getPingTimeout() {
        return pingTimeout;
    }

    public void setPingTimeout(int pingTimeout) {
        this.pingTimeout = pingTimeout;
    }

    public int getNumberOfPingAttempts() {
        return numberOfPingAttempts;
    }

    public void setNumberOfPingAttempts(int numberOfPingAttempts) {
        this.numberOfPingAttempts = numberOfPingAttempts;
    }
}
