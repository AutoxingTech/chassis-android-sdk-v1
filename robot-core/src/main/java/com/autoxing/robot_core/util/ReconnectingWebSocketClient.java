package com.autoxing.robot_core.util;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;
import java.util.Timer;

public abstract class ReconnectingWebSocketClient extends WebSocketClient {

    private boolean debug = true;
    private Integer reconnectInterval = 1000;
    private Integer maxReconnectInterval = 30000;
    private Double reconnectDecay = 1.1;
    private Integer reconnectAttempts = 0;
    private Integer maxReconnectAttempts = 5000;
    private Boolean forcedClose = false;

    private Timer reconnectTimer;
    private volatile Boolean isReconnecting = false;
    private ReschedulableTimerTask reconnectTimerTask;

    public ReconnectingWebSocketClient(URI serverUri, Draft protocolDraft) {
        super(serverUri, protocolDraft,null,0);
    }

    public ReconnectingWebSocketClient(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders,
                                       int connectTimeout) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
    }

    public void enableForcedClose(boolean enabled) { forcedClose = enabled; }

    @Override
    public void onClose(int arg0, String arg1, boolean arg2) {
        if (forcedClose) {
            close(arg0, arg1, arg2);
        } else {
            if (!isReconnecting) {
                restartReconnectionTimer();
            }
            isReconnecting = true;
        }
    }

    @Override
    public void onError(Exception exception) {
        error(exception);
    }

    @Override
    public void onMessage(String message) {
        message(message);
    }

    @Override
    public void onOpen(ServerHandshake arg0) {
        open(arg0);
    }

    private void restartReconnectionTimer() {
        cancelReconnectionTimer();

        reconnectTimer = new Timer("reconnectTimer");
        reconnectTimerTask = new ReschedulableTimerTask() {

            @Override
            public void run() {
                if (reconnectAttempts >= maxReconnectAttempts) {
                    cancelReconnectionTimer();
                    if (debug) {
                        System.out.println("To achieve the maximum number of retries:" + maxReconnectAttempts + ", has stopped retrying!!!!");
                    }
                }

                reconnectAttempts++;
                try {
                    Boolean isOpen = reconnectBlocking();
                    if (isOpen) {
                        if (debug) {
                            System.out.println("The connection was successful and the number of retries was:" + reconnectAttempts);
                        }

                        cancelReconnectionTimer();
                        reconnectAttempts = 0;
                        isReconnecting = false;
                    } else {
                        if (debug) {
                            System.out.println("Connection failed, the number of retries is:" + reconnectAttempts);
                        }

                        double timeoutD = reconnectInterval * Math.pow(reconnectDecay, reconnectAttempts);
                        int timeout = Integer.parseInt(new java.text.DecimalFormat("0").format(timeoutD));
                        timeout = timeout > maxReconnectInterval ? maxReconnectInterval : timeout;
                        System.out.println(timeout);
                        reconnectTimerTask.reSchedule2(timeout);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        reconnectTimerTask.schedule(reconnectTimer, reconnectInterval);
    }

    private void cancelReconnectionTimer() {
        if (reconnectTimer != null) {
            reconnectTimer.cancel();
            reconnectTimer = null;
        }

        if (reconnectTimerTask != null) {
            reconnectTimerTask.cancel();
            reconnectTimerTask = null;
        }
    }

    public abstract void open(ServerHandshake handshakedata);
    public abstract void message(String message);
    public abstract void close(int code, String reason, boolean remote);
    public abstract void error(Exception ex);
}
