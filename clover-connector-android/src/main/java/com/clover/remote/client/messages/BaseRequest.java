package com.clover.remote.client.messages;

import java.util.UUID;

/**
 * Created by blakewilliams on 12/15/15.
 */
public class BaseRequest {
    protected BaseRequest()
    {
    }
    private UUID _RequestMessageUUID;
    /// <summary>
    /// The UUID used to correlate a request and response message.
    /// </summary>
    public UUID getRequestMessageUUID()
    {
        if (_RequestMessageUUID == null)
        {
            _RequestMessageUUID = UUID.randomUUID();
        }
        return _RequestMessageUUID;
    }

    public void setRequestMessageUUID(UUID uuid)
    {
        if (_RequestMessageUUID == null)
        {
            _RequestMessageUUID = uuid;
        }
        else
        {
            throw new IllegalArgumentException("Request Message UUID is already set!");
        }

    }
}
