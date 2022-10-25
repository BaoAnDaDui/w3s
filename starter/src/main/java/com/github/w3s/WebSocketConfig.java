package com.github.w3s;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;
import java.util.Objects;

/**
 * @author wang xiao
 * date 2022/10/25
 */
@Configuration
@EnableWebSocket
@Import( value = {
        com.github.w3s.W3sConf.class,
        com.github.w3s.DefaultLocalSubscriptionManager.class,
        com.github.w3s.WebSocketServiceImpl.class
})
public class WebSocketConfig implements WebSocketConfigurer {

    private W3sConf w3sConf;


    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(32768);
        container.setMaxBinaryMessageBufferSize(32768);
        return container;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
       registry.addHandler(w3sWebSocketHandler(), w3sConf.getWsUrlPrefix()+"/**").setAllowedOriginPatterns("*")
               .addInterceptors(new HttpSessionHandshakeInterceptor(), new HandshakeInterceptor() {

                   @Override
                   public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                                  Map<String, Object> attributes) {
                       String connectNeededTokenKey = w3sConf.getConnectNeededTokenKey();
                       if (Objects.nonNull(connectNeededTokenKey) && !connectNeededTokenKey.isEmpty()){
                           return Objects.nonNull(request.getHeaders().get(connectNeededTokenKey));
                       }else {
                           return true;
                       }
                   }

                   @Override
                   public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                              Exception exception) {
                       //Do nothing
                   }
               });
    }


    @Bean
    public W3sWebSocketHandler w3sWebSocketHandler() {
        return new W3sWebSocketHandler();
    }

    @Autowired
    public void setW3sConf(W3sConf w3sConf) {
        this.w3sConf = w3sConf;
    }
}
