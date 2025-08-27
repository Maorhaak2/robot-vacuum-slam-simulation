package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TerminatedBroadcast implements Broadcast {
    private final String msName; 

    public TerminatedBroadcast(String msName) {
        this.msName = msName;
    }

    public String getMsName() {
        return msName;
    }
}