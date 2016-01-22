package com.clover.remote.client.messages;

/// <summary>
///
/// </summary>
public class CaptureAuthRequest extends BaseRequest
{
    public String paymentID;
    public long amount;
    public long tipAmount;
}
