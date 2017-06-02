package com.clover.remote.client.clovergo;

/**
 * Created by Akhani, Avdhesh on 4/18/17.
 */

import android.content.Context;

import com.clover.remote.client.device.CloverDeviceConfiguration;
import com.clover.remote.client.transport.CloverTransport;
import com.firstdata.clovergo.domain.model.ReaderInfo;

/**
 * Builder for building CloverGo instance
 */
public class CloverGoDeviceConfiguration implements CloverDeviceConfiguration {


    private final String apiKey;
    private final String secret;
    private final Context context;
    private final String accessToken;
    private final ENV env;
    private final String appId;
    private long scanPeriod = 30000;
    private boolean allowDuplicate = false;
    private boolean autoConnect = false;
    private ReaderInfo.ReaderType readerType = ReaderInfo.ReaderType.RP450;

    private CloverGoDeviceConfiguration(Context context, String accessToken, ENV env, String apiKey, String secret, String appId) {
        this.context = context;
        this.accessToken = accessToken;
        this.env = env;
        this.apiKey = apiKey;
        this.secret = secret;
        this.appId = appId;
    }

    @Override
    public String getCloverDeviceTypeName() {
        return "Clover Go";
    }

    @Override
    public String getMessagePackageName() {
        return "com.clover.remote.client.clovergo";
    }

    @Override
    public String getName() {
        return "Clover Go Device Configuration";
    }

    @Override
    public CloverTransport getCloverTransport() {
        // TODO: do we need this still?
        return null;
    }

    @Override
    public String getApplicationId() {
        return appId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSecret() {
        return secret;
    }

    public Context getContext() {
        return context;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public ENV getEnv() {
        return env;
    }

    public long getScanPeriod() {
        return scanPeriod;
    }

    public boolean isAllowDuplicate() {
        return allowDuplicate;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public ReaderInfo.ReaderType getReaderType() {
        return readerType;
    }


    public static class Builder{

        CloverGoDeviceConfiguration mCLoCloverGoConfiguration;

        /**
         *
         * Build CloverGo Object with AccessToken(mandatory parameter to initialize CloverGo Object)
         *
         *
         * @param accessToken access token received from the OAuth request
         * @param apiKey API Key
         * @param secret API Secret
         * @return CloverGoBuilder instance
         */
        public Builder(Context context, String accessToken, ENV env, String apiKey, String secret, String appId) {
            mCLoCloverGoConfiguration = new CloverGoDeviceConfiguration(context,accessToken,env,apiKey,secret,appId);
        }


        /**
         * This method is used to set Allow Duplicate Transaction or not. This is optional field. By default, It is not allowed.
         * @param allowDuplicate Boolean field whether to allow duplicates or not
         * @return CloverGoBuilder instance
         */
        public Builder overrideDuplicateTransaction(boolean allowDuplicate){
            mCLoCloverGoConfiguration.allowDuplicate = allowDuplicate;
            return this;
        }

        /**
         *This method is used to set scan timeout to scan for RP450 readers. This is optional field. By Default, it scans for 30 seconds.
         * @param timeOut Scan period in milliseconds to scan for RP450 readers.
         * @return CloverGoBuilder instance
         */
        public Builder scanTimeOut(long timeOut){
            mCLoCloverGoConfiguration.scanPeriod = timeOut;
            return this;
        }



        /**
         * This method is used to set to allow auto connect to last RP450 connected reader when App come in foreground. This is optional parameter. By Default, it is disabled.
         * @param autoConnect Boolean field to enable auto connect to last connected RP450
         * @return CloverGoBuilder instance
         */
        public Builder allowAutoConnect(boolean autoConnect){
            mCLoCloverGoConfiguration.autoConnect = autoConnect;
            //if (autoConnect)
            //   cloverGoConnector.registerActivityCallbacks();
            return this;
        }

        public Builder deviceType(ReaderInfo.ReaderType readerType){
            mCLoCloverGoConfiguration.readerType = readerType;
            //if (autoConnect)
            //   cloverGoConnector.registerActivityCallbacks();
            return this;
        }


        /**
         * Initializes the CloverGo object
         *
         * @return CloverGo instance
         */
        public CloverGoDeviceConfiguration build(){
            return mCLoCloverGoConfiguration;
        }


    }

    public static enum ENV {
        LIVE,DEMO,SANDBOX,SANDBOX_DEV
    }
}