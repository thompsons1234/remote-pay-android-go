package com.clover.remote.client.device;

import com.clover.remote.client.transport.CloverTransport;

public interface CloverDeviceConfiguration
    {
        String getCloverDeviceTypeName();
        String getMessagePackageName();
        String getName();
        CloverTransport getCloverTransport();
    }