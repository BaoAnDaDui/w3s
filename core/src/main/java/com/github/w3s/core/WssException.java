package com.github.w3s.core;

/**
 * @author wang xiao
 * date 2022/10/24
 */
public class WssException extends RuntimeException{

    public WssException(String message, Throwable cause) {
        super(message, cause);
    }

    public WssException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
