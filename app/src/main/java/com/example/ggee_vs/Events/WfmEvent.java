package com.example.ggee_vs.Events;

import com.example.ggee_vs.Requests.WfmRequest;

public class WfmEvent {

    public WfmRequest wReq;
    public int type = 0;

    public WfmEvent(WfmRequest wReq, int type){
        this.wReq = wReq;
        this.type = type;
    }
}
