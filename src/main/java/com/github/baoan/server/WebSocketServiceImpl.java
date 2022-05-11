package com.github.baoan.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.baoan.WebSocketMsgEndpoint;
import com.github.baoan.data.SessionEvent;
import com.github.baoan.data.SubscriptionMsg;
import com.github.baoan.data.WebSocketSessionRef;
import com.github.baoan.service.LocalSubscriptionService;
import com.github.baoan.service.WebSocketService;
import com.github.baoan.subscription.*;
import com.github.baoan.util.ServiceCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.*;

import static com.github.baoan.data.WebSocketConstant.*;

/**
 * 处理web socket 消息以及订阅内容
 * @author wang xiao
 * @date 2022/5/11
 */
public class WebSocketServiceImpl implements WebSocketService {


    private final ConcurrentMap<String, WebSocketSessionRef> sessions = new ConcurrentHashMap<>();

    private final Logger logger = LoggerFactory.getLogger(WebSocketServiceImpl.class);

    private final ConcurrentMap<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private WebSocketMsgEndpoint msgEndpoint;

    private ExecutorService executor;

    private LocalSubscriptionService localSubscriptionService;


    @PostConstruct
    public void initExecutor() {
        executor = new ThreadPoolExecutor(20,100,60,TimeUnit.SECONDS,new SynchronousQueue<>(),new ThreadPoolExecutor.AbortPolicy());

        ScheduledExecutorService pingExecutor = Executors.newSingleThreadScheduledExecutor();
        pingExecutor.scheduleWithFixedDelay(this::sendPing, PING_TIMEOUT / NUMBER_OF_PING_ATTEMPTS, PING_TIMEOUT / NUMBER_OF_PING_ATTEMPTS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handleWebSocketSessionEvent(WebSocketSessionRef sessionRef, SessionEvent sessionEvent) {
        String sessionId = sessionRef.getSessionId();
        switch (sessionEvent.getEventType()) {
            case ESTABLISHED:
                sessions.put(sessionId, sessionRef);
                break;
            case CLOSED:
                sessions.remove(sessionId);
                localSubscriptionService.cancelAllSessionSubscriptions(sessionId);
                processSessionClose(sessionRef);
                break;
            default:
                break;
        }
    }

    @Override
    public void handleWebSocketMsg(WebSocketSessionRef sessionRef, String msg) {
        try {
            SubscriptionCmdWrapper cmdsWrapper = JSON_MAPPER.readValue(msg, SubscriptionCmdWrapper.class);
            if (cmdsWrapper != null){
                for (AbstractSubscriptionCmd abstractSubscriptionCmd :cmdsWrapper.getSubs()){
                    if (checkSubscription(sessionRef,abstractSubscriptionCmd)){
                        handleSubscriptionCmd(sessionRef, abstractSubscriptionCmd);
                    }
                }
            }
        }catch (JsonProcessingException e){
            // FIXME -1 代表失败
            sendWsMsg(sessionRef.getSessionId(), new DefaultSubscriptionDataUpdate(-1, "FAILED_TO_PARSE_WS_COMMAND"));
        }
    }



    @Override
    public void close(String sessionId, CloseStatus status) {
        WebSocketSessionRef sessionRef = sessions.get(sessionId);
        if (sessionRef != null) {
            try {
                msgEndpoint.close(sessionRef, status);
            } catch (IOException e) {
                // don`t know do some things
            }
        }
    }

    @Override
    public void sendWsMsg(String sessionId, SubscriptionDataUpdate update) {
        doSendWsMsg(sessionId, update);
    }

    private void doSendWsMsg(String sessionId, SubscriptionDataUpdate update) {
        WebSocketSessionRef sessionRef = sessions.get(sessionId);
        try {
            String msg = JSON_MAPPER.writeValueAsString(update);
            executor.submit(() -> {
                try {
                    msgEndpoint.send(sessionRef, msg);
                } catch (IOException e) {
                    logger.warn("Failed to send web socket msg reply,session:{},data:{},error:{}", sessionRef.getSessionId(), update, e);
                }
            });
        } catch (JsonProcessingException e) {
            logger.warn("Failed to encode socket msg reply,session:{},data:{},error:{}", sessionRef.getSessionId(), update, e);
        }
    }


    private void processSessionClose(WebSocketSessionRef sessionRef) {
        String sessionId =  sessionRef.getSessionId();
        sessionSubscriptions.remove(sessionId);

    }
    private boolean checkSubscription(WebSocketSessionRef sessionRef, SubscriptionCmd cmd) {
        String subId =  sessionRef.getSessionId() + cmd.getSubId() ;
        try {
            Set<String> sessionSubs = sessionSubscriptions.computeIfAbsent(sessionRef.getSessionId(), id -> ConcurrentHashMap.newKeySet());
            synchronized (sessionSubs) {
                if (cmd.isUnSub()) {
                    sessionSubs.remove(subId);
                } else if (sessionSubs.size() < MAX_SUB_OF_SESSION) {
                    sessionSubs.add(subId);
                } else {
                    msgEndpoint.close(sessionRef, CloseStatus.POLICY_VIOLATION.withReason("Max subscriptions limit reached!"));
                    return false;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }


    private void handleSubscriptionCmd(WebSocketSessionRef sessionRef, AbstractSubscriptionCmd cmd) {
        String sessionId = sessionRef.getSessionId();
        if (validateSessionRef(sessionRef, cmd, sessionId)) {
            if (cmd.isUnSub()) {
                unsubscribe( cmd, sessionId);
            } else {

                ServiceCallback<Object> callback = new ServiceCallback<>() {
                    @Override
                    public void onSuccess(Object result) {
                        if (null!= result){
                            sendWsMsg(sessionId, new DefaultSubscriptionDataUpdate(cmd.getSubId(), result));
                        }
                        SubscriptionMsg sub = SubscriptionMsg.SubscriptionMsgBuilder.builder()
                                .sessionId(sessionId)
                                .subId(cmd.getSubId())
                                .entityId(cmd.getEntityId())
                                .consumer(WebSocketServiceImpl.this::doSendWsMsg)
                                .build();
                        localSubscriptionService.addSubscription(sub);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        sendWsMsg(sessionId, new DefaultSubscriptionDataUpdate(-1, "FAILED_TO_SUBSCRIPTION"));
                    }
                };
                validate(sessionRef,cmd,callback);
            }
        }
    }



    private boolean validateSessionRef(WebSocketSessionRef sessionRef, SubscriptionCmd cmd, String sessionId) {
        return validateSessionMetadata(sessionRef, cmd.getSubId(), sessionId);
    }

    private boolean validateSessionMetadata(WebSocketSessionRef sessionRef, int cmdId, String sessionId) {
        WebSocketSessionRef targetSessionRef = sessions.get(sessionId);
        if (targetSessionRef == null) {
            SubscriptionDataUpdate update = new DefaultSubscriptionDataUpdate(cmdId, "SESSION_DATA_NOT_FOUND");
            sendWsMsg(sessionRef.getSessionId(), update);
            return false;
        } else {
            return true;
        }
    }

    private void unsubscribe( SubscriptionCmd cmd, String sessionId) {
        localSubscriptionService.cancelSubscription(sessionId, cmd.getSubId());
    }

    /**
     * 这里做校验逻辑和第一次查询逻辑
     * @param webSocketSessionRef webSocketSessionRef
     * @param cmd cmd
     * @param callback callback
     * @param <T> T
     */
    private <T> void validate(WebSocketSessionRef webSocketSessionRef,AbstractSubscriptionCmd cmd,ServiceCallback<T> callback){
        // fixme 校验权限
        if (cmd.isFirstQuery()){
            // fixme 查询数据并回调
        }
        callback.onSuccess(null);

    }


    private void sendPing() {
        long currentTime = System.currentTimeMillis();
        sessions.values().forEach(sessionRef ->
                executor.submit(() -> {
                    try {
                        msgEndpoint.sendPing(sessionRef,currentTime);
                    } catch (IOException e) {
                        logger.warn(" {} Failed to send ping: {}",sessionRef.getSessionId(), e);
                    }
                }));
    }

    @Autowired
    public void setMsgEndpoint(WebSocketMsgEndpoint msgEndpoint) {
        this.msgEndpoint = msgEndpoint;
    }

    @Autowired
    public void setLocalSubscriptionService(LocalSubscriptionService localSubscriptionService) {
        this.localSubscriptionService = localSubscriptionService;
    }
}
