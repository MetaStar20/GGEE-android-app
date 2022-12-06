package com.example.ggee_vs.Requests;

import android.content.Context;

import com.example.ggee_vs.Preferences.PKey;
import com.example.ggee_vs.Preferences.Prefs;

import java.util.ArrayList;
import java.util.List;

public class GgeeRequest extends Request {
    private int id_request;
    private int id_method;
    private List<String> params = new ArrayList<>();
    private Context mContext;

    public GgeeRequest(){
        GgeeRequest();
    }

    public GgeeRequest(Context ctx){
        GgeeRequest();
        mContext = ctx;
    }

    public GgeeRequest(int id_request, int id_method, List<String> params){
        this.id_request = id_request; this.id_method = id_method; this.params = params;
    }

    private void GgeeRequest(){
        id_request = 0; id_method = 1000;
    }

    public int getId_request() { return this.id_request; }
    public void setId_request(int id_request){ this.id_request = id_request; }

    public int getId_method() { return  this.id_method; }
    public void setId_method(int id_method){ this.id_method = id_method; }

    @Override
    public void save() {
        Prefs.putIntPref(mContext, PKey.ID_REQUEST,id_request);
        Prefs.putIntPref(mContext, PKey.ID_METHOD,id_method);
    }
}
