package com.clover.remote.client.clovergo;

import com.clover.remote.client.ICloverConnectorListener;
import com.clover.remote.client.messages.CardApplicationIdentifier;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.firstdata.clovergo.domain.model.ReaderInfo;

import java.util.List;

/**
 * Created by Akhani, Avdhesh on 5/19/17.
 */

public interface ICloverGoConnectorListener extends ICloverConnectorListener {


    /**
     * Called when the Clover Go Bluetooth device is Discovered to connect
     */
    public void onDeviceDiscovered(ReaderInfo readerInfo);

    /**
     * Called when the Clover device is disconnected
     */
    public void onDeviceDisconnected(ReaderInfo readerInfo);

    /**
     * Chip cards have application identifiers which negotiates with the card reader on what application identifier to use to send card data back to reader to process transaction.
     * <p>
     * In case card has multiple application identifiers and reader is not able to negotiate explicit consent from customer is needed to proceed.
     * Please return one of the application identifiers from the list to proceed or null to cancel transaction
     *
     * @param applicationIdentifierList - application identifier from the card.
     * @return selected application identifier
     */
    void onAidMatch(List<CardApplicationIdentifier> applicationIdentifierList, AidSelection aidSelection);

    /**
     * on AidSelection return selected Application Identifier
     */
    interface AidSelection{
        void selectApplicationIdentifier(CardApplicationIdentifier selectedCardApplicationIdentifier);
    }

    void onCloverGoDeviceActivity(CloverDeviceEvent deviceEvent );

    void onGetMerchantInfo();

    void onGetMerchantInfoResponse(boolean isSuccess);

    void onSignatureNeeded();

}
