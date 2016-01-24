package com.clover.remote.client.messages;

import java.util.UUID;

public class BaseResponse
{
    public static final String SUCCESS = "SUCCESS";
    public static final String CANCEL = "CANCEL";
    public static final String FAIL = "FAIL";
    public static final String ERROR = "ERROR";

    private UUID requestMessageUUID;
    /*
    the status of the transaction activity.
    */
    private String code;//SUCCESS, CANCEL, ERROR, FAIL - TODO: enum

    protected BaseResponse()
    {

    }
    protected BaseResponse(UUID requestUUID)
    {
        requestMessageUUID = requestUUID;
    }

    protected void setRequestMessageUUID(UUID requestID) {
        if(requestMessageUUID != null) {
            throw new IllegalArgumentException("Request Message UUID is already set");
        }
        requestMessageUUID = requestID;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}