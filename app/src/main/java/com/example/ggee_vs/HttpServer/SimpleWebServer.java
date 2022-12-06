package com.example.ggee_vs.HttpServer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

class SimpleWebServer extends AsyncTask<Void, Void, Void> {
    ServerSocket server = null;
    int PORT = 9090;
    /*
    SimpleWebServer simpleserver = new SimpleWebServer();
    simpleserver.execute();
    */
    @Override
    protected Void doInBackground(Void... params) {
        try {
            server = new ServerSocket(PORT);
            Log.d("webserver", "server listening..");
            Socket socket = null;
            while( (socket = server.accept()) != null ){
                Log.d("webserver", socket.getInetAddress() + " has connected..");
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                String reqLine = in.readLine();
                Log.d("webserver", reqLine);
                StringTokenizer tokenizer = new StringTokenizer(reqLine);
                if(reqLine != null && "GET".equals(tokenizer.nextToken())){
                    String reqURL = tokenizer.nextToken();
                    if("/".equals(reqURL)){
                        out.writeBytes("HTTP/1.1 200 OK \r\n");
                        out.writeBytes("\r\n");
                        out.writeBytes("<h1>Welcome to Simple WebServer</h1>");
                        out.writeBytes("<p>This is Index page</p>");
                        Log.d("webserver", ">> 200 OK");
                    }else{
                        out.writeBytes("HTTP/1.1 400 Not Found \r\n");
                        out.writeBytes("Connection: close\r\n");
                        out.writeBytes("\r\n");
                        out.writeBytes("<h1>404 Not found</h1>");
                        Log.d("webserver", ">> 404 NotFound");
                    }
                }else{
                    out.writeBytes("HTTP/1.1 500 Internal Server Error \r\n");
                    out.writeBytes("Connection: close\r\n");
                    out.writeBytes("\r\n");
                    out.writeBytes("<h1>500 Internal Server Error</h1>");
                    Log.d("webserver", ">>500 Server Error");
                }
                out.flush();
                out.close();
                socket.close();
                Log.d("webserver", "response has finished.");
            }//end of while

        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("webserver", "server is shutting down");
        return null;
    }
}
