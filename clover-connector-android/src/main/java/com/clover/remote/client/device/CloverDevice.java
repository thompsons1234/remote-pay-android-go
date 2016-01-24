package com.clover.remote.client.device;

import android.graphics.Bitmap;
import com.clover.common2.payments.PayIntent;
import com.clover.remote.client.CloverDeviceObserver;
import com.clover.remote.client.transport.CloverTransport;
import com.clover.remote.client.transport.CloverTransportObserver;
import com.clover.remote.order.DisplayOrder;
import com.clover.remote.terminal.KeyPress;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Payment;

import java.util.ArrayList;
import java.util.List;

public abstract class CloverDevice
    {
        protected List<CloverDeviceObserver> deviceObservers = new ArrayList<CloverDeviceObserver>();

        protected CloverTransport transport;
        protected String packageName;

        public CloverDevice(String packageName, CloverTransport transport)
        {
            this.transport = transport;
            this.packageName = packageName;
        }

        /// <summary>
        /// Adds a observer for transport events to the member transport object to notify
        /// </summary>
        /// <param name="observer"></param>
        /*public void Subscribe(CloverTransportObserver observer)
        {
            this.transport.Subscribe(observer);
        }*/

        public void Subscribe(CloverDeviceObserver observer)
        {
            deviceObservers.add(observer);
        }

        public void Unsubscribe(CloverDeviceObserver observer)
        {
            deviceObservers.remove(observer);
        }

        public abstract void doDiscoveryRequest();
        /// <summary>
        /// 
        /// </summary>
        /// <param name="payIntent"></param>
        /// <param name="order">can be null.  If it is, an order will implicitly be created on the other end</param>
        public abstract void doTxStart(PayIntent payIntent, Order order);
        public abstract void doKeyPress(KeyPress keyPress);
        public abstract void doVoidPayment(Payment payment, VoidReason reason);
        public abstract void doOrderUpdate(DisplayOrder order, Object orderOperation); //OrderDeletedOperation, LineItemsDeletedOperation, LineItemsAddedOperation, DiscountsDeletedOperation, DiscountsAddedOperation,
        public abstract void doSignatureVerified(Payment payment, boolean verified);
        public abstract void doTerminalMessage(String text);
        public abstract void doPaymentRefund(String orderId, String paymentId, long amount); // manual refunds are handled via doTxStart
        public abstract void doTipAdjustAuth(String orderId, String paymentId, long amount);
        //void doBreak();
        public abstract void doPrintText(List<String> textLines);
        public abstract void doShowWelcomeScreen();
        public abstract void doShowReceiptScreen();
        public abstract void doShowThankYouScreen();
        public abstract void doOpenCashDrawer(String reason);
        public abstract void doPrintImage(Bitmap bitmap);

        public abstract void dispose();

        public abstract void doCloseout();

        public abstract void doCaptureCard(int cardEntryMethods);
    }