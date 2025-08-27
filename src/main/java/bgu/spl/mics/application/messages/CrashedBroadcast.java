package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast {
    private final String msName; 
    private final String errorDescription; 

    public CrashedBroadcast(String msName, String errorDescription) {
        this.msName = msName;
        this.errorDescription = errorDescription;
    }

    public String getMsName() {
        return msName;
    }

    public String getErrorMsg(){
        return errorDescription;
    }
}