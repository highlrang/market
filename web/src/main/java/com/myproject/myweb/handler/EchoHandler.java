package com.myproject.myweb.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class EchoHandler extends TextWebSocketHandler {

    // 사용자 목록 (전체, 1대1)
    List<WebSocketSession> sessions = new ArrayList<WebSocketSession>();
    Map<String, WebSocketSession> users = new ConcurrentHashMap<String, WebSocketSession>();

    // 클라이언트가 서버로 연결 시
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        String senderId = getUserId(session);
        if(senderId != null){
            log.info(senderId + " 연결!");
            users.put(senderId, session);
        }
    }

    // 클라이언트가 데이터 전송 시
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
        String senderId = getUserId(session);

        String msg = message.getPayload();
        if(msg != null){
            String[] strs = msg.split(",");
            log.info(strs.toString());
            if(strs != null && strs.length == 4){
                String type = strs[0];
                String target = strs[1];
                String content = strs[2];
                String url = strs[3];
                WebSocketSession targetSession = users.get(target);

                if(targetSession != null){
                    TextMessage tmpMsg = new TextMessage("<a target='_blank' href='" + url + ";>[<b>" + type + "</b>]" + content + "</a>");
                    targetSession.sendMessage(tmpMsg);
                }
            }
        }
    }

    // 연결 해제될 때
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
        String senderId = getUserId(session);
        if(senderId != null){
            log.info(senderId + " 연결 종료!");
            users.remove(senderId);
            sessions.remove(session);
        }
    }

    // 에러 발생 시
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception{
        log.info(session.getId() + " 익셉션 발생: " + exception.getMessage());
    }

    private String getUserId(WebSocketSession session){
        Map<String, Object> httpSession = session.getAttributes();
        return (String) httpSession.get("id");
    }
}


