package com.clover.remote.client.transport;

import java.util.ArrayList;
import java.util.List;

public abstract class CloverTransport
    {
        protected List<CloverTransportObserver> observers = new ArrayList<CloverTransportObserver>();
        boolean ready = false;

        protected void onDeviceConnected()
        {
            for(CloverTransportObserver obs : observers) {
                obs.onDeviceConnected(this);
            }

        }

        protected void onDeviceReady()
        {
            ready = true;
            for(CloverTransportObserver obs : observers) {
                obs.onDeviceReady(this);
            }
        }

        protected void onDeviceDisconnected()
        {
            ready = false;
            for(CloverTransportObserver obs : observers) {
                obs.onDeviceDisconnected(this);
            }
        }

        /// <summary>
        /// Should be called by subclasses when a message is received.
        /// </summary>
        /// <param name="message"></param>
        protected void onMessage(String message)
        {
            for(CloverTransportObserver obs : observers) {
                obs.onMessage(message);
            }
        }

        public void Subscribe(CloverTransportObserver observer)
        {
            CloverTransport me = this;
            if (ready)
            {
                for(CloverTransportObserver obs : observers) {
                    obs.onDeviceReady(this);
                }
            }
            observers.add(observer);
        }

        public abstract void dispose();

        public void Unsubscribe(CloverTransportObserver observer)
        {
            observers.remove(observer);
        }

        public void clearListeners() {
            observers.clear();
        }



        // Implement this to send info
        public abstract int sendMessage(String message);
    }

