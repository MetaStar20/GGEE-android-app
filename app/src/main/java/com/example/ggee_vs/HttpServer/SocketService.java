package com.example.ggee_vs.HttpServer;

import android.content.Context;
import android.util.Log;
import java.net.URI;
import java.net.URISyntaxException;

import tech.gusavila92.websocketclient.WebSocketClient;

public class SocketService {

    private Context mContext;
    public SocketService(Context ctx){
        mContext = ctx;
    }

    private WebSocketClient webSocketClient;
    private void createWebSocketClient() {
        URI uri;
        try {
            // Connect to local host
            uri = new URI("ws://10.10.11.235:8080/websocket");
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session is starting");
                webSocketClient.send("Hello World!");
            }
            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                final String message = s;
                /*
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{

                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });*/
            }
            @Override
            public void onBinaryReceived(byte[] data) {
            }
            @Override
            public void onPingReceived(byte[] data) {
            }
            @Override
            public void onPongReceived(byte[] data) {
            }
            @Override
            public void onException(Exception e) {
                System.out.println(e.getMessage());
            }
            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
                System.out.println("onCloseReceived");
            }
        };
        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    public void sendMessage(String message) {
        Log.i("WebSocket", "Button was clicked");
        // Send button id string to WebSocket Server
        webSocketClient.send(message);
    }

}
