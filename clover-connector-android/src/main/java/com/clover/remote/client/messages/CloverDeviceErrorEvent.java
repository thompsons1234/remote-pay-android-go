package com.clover.remote.client.messages;

public class CloverDeviceErrorEvent
{
    private int code;
    private String message;

    public CloverDeviceErrorEvent()
    {

    }
    public CloverDeviceErrorEvent(int devCode, String msg)
    {
        code = devCode;
        message = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}