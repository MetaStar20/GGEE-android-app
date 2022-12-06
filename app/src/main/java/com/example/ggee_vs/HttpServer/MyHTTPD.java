package com.example.ggee_vs.HttpServer;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import com.example.ggee_vs.Config;
import com.example.ggee_vs.Events.WfmEvent;
import com.example.ggee_vs.Requests.RequestType;
import com.example.ggee_vs.Requests.WfmRequest;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/* How to start this server
    server = new MyHTTPD();
    server.start();
    server.stop();
*/
public class MyHTTPD extends NanoHTTPD {

    private static final String TAG = "MyHTTPD";
    private Context context;

    public MyHTTPD(Context ctx) throws IOException {
        super(Config.MyHttpServerPort);
        context = ctx;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Log.d(TAG, "serve: "+session.getUri());
        String uri = session.getUri();
        Map<String, String> files = new HashMap<String, String>();

        if (session.getMethod() == Method.POST){
            if (uri.equals("/start")) {
                try {
                    session.parseBody(files);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // get the POST body
                String postBody = session.getQueryParameterString();
                // get  the POST request's parameters and save it.
                WfmRequest wfm = new WfmRequest(context);
                wfm.setId_request(Integer.valueOf(session.getParms().get("id_request")));
                wfm.setId_method(Integer.valueOf(session.getParms().get("id_method")));
                wfm.setResolution(Integer.valueOf(session.getParms().get("resolution")));
                wfm.setIntervals(Integer.valueOf(session.getParms().get("intervals")));
                wfm.setExtent(Integer.valueOf(session.getParms().get("extent")));
                wfm.save();
                Log.d(TAG,"save wfm request");
                // send event to MainActivity to start grabbing image.
                EventBus.getDefault().post(new WfmEvent(wfm,RequestType.Start));
                // return response.
                Response response = newFixedLengthResponse("success");
                response.addHeader("Access-Control-Allow-Origin", "*");
                return response;
            }

            if (uri.equals("/stop")) {
                try {
                    session.parseBody(files);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // get the POST body
                String postBody = session.getQueryParameterString();
                // get  the POST request's parameters and save it.
                WfmRequest wfm = new WfmRequest(context);
                wfm.setId_request(Integer.valueOf(session.getParms().get("id_request")));
                wfm.setId_method(Integer.valueOf(session.getParms().get("id_method")));
                wfm.save();
                Log.d(TAG,"save wfm request");
                // send event to MainActivity to start grabbing image.
                EventBus.getDefault().post(new WfmEvent(wfm, RequestType.Stop));
                // return response.
                Response response = newFixedLengthResponse("success");
                response.addHeader("Access-Control-Allow-Origin", "*");
                return response;
            }
        }
        return  null;
    }

    private void initIPAddress() {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        Log.d(TAG,"Server running at: " + ip + ":" + Config.MyHttpServerPort);
        Log.d(TAG, "onCreate: " + ip);
    }

}
