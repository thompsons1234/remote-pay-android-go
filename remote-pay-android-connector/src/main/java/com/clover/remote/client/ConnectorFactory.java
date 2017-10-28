package com.clover.remote.client;

import com.clover.remote.client.clovergo.CloverGoConnector;
import com.clover.remote.client.clovergo.CloverGoDeviceConfiguration;
import com.firstdata.clovergo.domain.model.ReaderInfo;

import java.util.HashMap;

public class ConnectorFactory {

    // To Support RP350 and RP450 reader connected at the same time
    private static HashMap<ReaderInfo.ReaderType, CloverGoConnector> cloverGoConnectorMap = new HashMap<>();

    //TODO: Merge Create connector method and Initialize SDK without Reader Type
    public static ICloverConnector createCloverConnector(CloverDeviceConfiguration configuration) {

        if (configuration instanceof CloverGoDeviceConfiguration) {

          ReaderInfo.ReaderType readerType = ((CloverGoDeviceConfiguration) configuration).getReaderType();

            if (cloverGoConnectorMap.get(readerType) == null) {
                CloverGoConnector cloverGoConnector = new CloverGoConnector((CloverGoDeviceConfiguration) configuration);
                cloverGoConnectorMap.put(readerType, cloverGoConnector);

                return cloverGoConnector;

            } else {
                return cloverGoConnectorMap.get(readerType);
            }

        } else {
            return new CloverConnector(configuration);
        }
    }
}