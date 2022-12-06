package com.example.ggee_vs.Requests;

import android.content.Context;

import com.example.ggee_vs.Preferences.PKey;
import com.example.ggee_vs.Preferences.Prefs;

import java.util.ArrayList;
import java.util.List;

public class WfmRequest extends Request {

    private int id_request;
    private int id_method;
    private int resolution;
    private int intervals;
    private int extent;
    private Context mContext;

    public WfmRequest(){
        WfmRequest();
    }

    public WfmRequest(Context ctx){
        mContext = ctx;
        WfmRequest();
    }

    public WfmRequest(int id_request, int id_method,int resolution, int intervals, int extent){
        this.id_request = id_request; this.id_method = id_method; this.resolution = resolution;
        this.intervals = intervals; this.extent = extent;
    }


    private void WfmRequest(){
        id_request = 0; id_method = 1000; resolution = 0; intervals = 1000; extent = 60;
    }


    public int getId_request() { return this.id_request; }
    public void setId_request(int id_request){ this.id_request = id_request; }

    public int getId_method() { return  this.id_method; }
    public void setId_method(int id_method){ this.id_method = id_method; }

    public int getResolution() { return  this.resolution; }
    public void setResolution(int resolution){ this.resolution = resolution; }

    public int getIntervals() { return  this.intervals; }
    public void setIntervals(int intervals){ this.intervals = intervals; }

    public int getExtent() { return  this.extent; }
    public void setExtent(int extent){ this.extent = extent; }

    @Override
    public void save() {
        Prefs.putIntPref(mContext, PKey.ID_REQUEST,id_request);
        Prefs.putIntPref(mContext, PKey.ID_METHOD,id_method);
        Prefs.putIntPref(mContext, PKey.RESOLUTIOIN,resolution);
        Prefs.putIntPref(mContext, PKey.INTERVALS,intervals);
        Prefs.putIntPref(mContext, PKey.EXTENT,extent);
    }
}
