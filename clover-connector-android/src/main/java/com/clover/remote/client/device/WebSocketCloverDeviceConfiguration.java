package com.clover.remote.client.device;

import com.clover.remote.client.transport.CloverTransport;
import com.clover.remote.client.transport.WebSocketCloverTransport;

import java.net.URI;

/**
 * Created by blakewilliams on 12/15/15.
 */
public class WebSocketCloverDeviceConfiguration implements CloverDeviceConfiguration {
    private URI uri = null;

    public WebSocketCloverDeviceConfiguration(URI endpoint) {
        uri = endpoint;
    }

    @Override
    public String getCloverDeviceTypeName() {
        return DefaultCloverDevice.class.getCanonicalName();
    }

    @Override
    public String getMessagePackageName() {
        return "com.clover.remote.protocol.lan";
    }

    @Override
    public String getName() {
        return "Clover WebSocket Connector";
    }

    @Override
    public CloverTransport getCloverTransport() {
        return new WebSocketCloverTransport(uri);
    }
}
