package com.clover.remote.client.transport;

public interface CloverTransportObserver
    {
        /// <summary>
        /// Device is there but not yet ready for use
        /// </summary>
        void onDeviceConnected(CloverTransport transport);

        /// <summary>
        /// Device is there and ready for use
        /// </summary>
        void onDeviceReady(CloverTransport transport);

        /// <summary>
        /// Device is not there anymore
        /// </summary>
        /// <param name="transport"></param>
        void onDeviceDisconnected(CloverTransport transport);

        void onMessage(String message);
    }