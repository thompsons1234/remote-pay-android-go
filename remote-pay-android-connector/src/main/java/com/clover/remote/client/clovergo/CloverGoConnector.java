package com.clover.remote.client.clovergo;

import android.graphics.Bitmap;

import com.clover.remote.Challenge;
import com.clover.remote.InputOption;
import com.clover.remote.client.DefaultCloverConnector;
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.CapturePreAuthRequest;
import com.clover.remote.client.messages.CloseoutRequest;
import com.clover.remote.client.messages.CustomActivityRequest;
import com.clover.remote.client.messages.ManualRefundRequest;
import com.clover.remote.client.messages.PreAuthRequest;
import com.clover.remote.client.messages.ReadCardDataRequest;
import com.clover.remote.client.messages.RefundPaymentRequest;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.client.messages.TipAdjustAuthRequest;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentRequest;
import com.clover.remote.order.DisplayOrder;
import com.firstdata.clovergo.domain.model.ReaderInfo;

import java.util.List;

/**
 * Created by Akhani, Avdhesh on 4/18/17.
 */

//TODO: Add POS support
//TODO: Void Payment - order.setID() on null Order Object
//TODO:  Auth Transaction -  (As Per Pivotal)If the merchant is not configured for Auth transactions on the payment gateway then the transaction should throw an exception. Right now if not configured, we do transaction as Sale.
//TODO: Pre Auth -  If the merchant is not configured for PreAuth on the payment gateway then the transaction should throw an exception.
public class CloverGoConnector extends DefaultCloverGoConnector {

    private static String TAG = "CloverGO";

    private CloverGoDeviceConfiguration mCloverGoConfiguration;
    private static TransactionModule transactionModule;


    public CloverGoConnector(CloverGoDeviceConfiguration mCloverGoConfiguration) {
        this.mCloverGoConfiguration = mCloverGoConfiguration;
        if (transactionModule == null)
            transactionModule = new TransactionModule(broadcaster,mCloverGoConfiguration);
    }

    @Override
    public void initializeConnection() {
        transactionModule.initializeConnection(mCloverGoConfiguration.getReaderType());
    }

    @Override
    public void connectToDevice(ReaderInfo readerInfo) {
        transactionModule.connectToDevice(readerInfo);
    }

    @Override
    public void disconnectDevice() {
        transactionModule.disconnectDevice(mCloverGoConfiguration.getReaderType());
    }

    @Override
    public void stopDeviceScan() {
        transactionModule.stopDeviceScan();
    }

    @Override
    public void dispose() {
        broadcaster.clear();
    }

    @Override
    public void sale(SaleRequest saleRequest) {
        transactionModule.sale(saleRequest,mCloverGoConfiguration.getReaderType(),mCloverGoConfiguration.isAllowDuplicate());
    }

    @Override
    public void acceptSignature(VerifySignatureRequest request) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");
    }

    @Override
    public void rejectSignature(VerifySignatureRequest request) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");

    }

    @Override
    public void acceptPayment(com.clover.sdk.v3.payments.Payment payment) {
        transactionModule.acceptPayment(payment);
    }

    @Override
    public void rejectPayment(com.clover.sdk.v3.payments.Payment payment, Challenge challenge) {
        transactionModule.rejectPayment(payment,challenge);
    }

    @Override
    public void auth(AuthRequest authRequest) {
        transactionModule.auth(authRequest, mCloverGoConfiguration.getReaderType(),mCloverGoConfiguration.isAllowDuplicate());
    }

    @Override
    public void preAuth(PreAuthRequest preAuthRequest) {
        transactionModule.preAuth(preAuthRequest, mCloverGoConfiguration.getReaderType(),mCloverGoConfiguration.isAllowDuplicate());
    }

    @Override
    public void tipAdjustAuth(final TipAdjustAuthRequest authTipAdjustRequest) {
        transactionModule.tipAdjustAuth(authTipAdjustRequest, mCloverGoConfiguration.getReaderType());
    }

    @Override
    public void capturePreAuth(final CapturePreAuthRequest capturePreAuthRequest) {
        transactionModule.capturePreAuth(capturePreAuthRequest);
    }

    @Override
    public void voidPayment(final VoidPaymentRequest voidPaymentRequest) {
        transactionModule.voidPayment(voidPaymentRequest, mCloverGoConfiguration.getReaderType());
    }

    @Override
    public void refundPayment(final RefundPaymentRequest refundPaymentRequest) {
        transactionModule.refundPayment(refundPaymentRequest);
    }

    @Override
    public void manualRefund(ManualRefundRequest request) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");

    }

    @Override
    public void vaultCard(Integer cardEntryMethods) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");

    }

    @Override
    public void retrievePendingPayments() throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");

    }

    @Override
    public void readCardData(ReadCardDataRequest request) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");
    }

    @Override
    public void closeout(CloseoutRequest closeoutRequest){
        transactionModule.closeout(closeoutRequest);
    }

    @Override
    public void captureSignature(String paymentId, int[][] xy) {
        transactionModule.captureSignature(paymentId,xy);
    }

    @Override
    public void cancel() {
        transactionModule.cancel(mCloverGoConfiguration.getReaderType());
    }

    @Override
    public void resetDevice() {
        //TODO:
    }

    @Override
    public void startCustomActivity(CustomActivityRequest request) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");
    }

    @Override
    public void showMessage(String message) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");

    }

    @Override
    public void showWelcomeScreen() throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");

    }

    @Override
    public void showThankYouScreen() throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");

    }

    @Override
    public void displayPaymentReceiptOptions(String orderId, String paymentId) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");

    }

    @Override
    public void openCashDrawer(String reason) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");

    }

    @Override
    public void showDisplayOrder(DisplayOrder order) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");

    }

    @Override
    public void removeDisplayOrder(DisplayOrder order) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");

    }

    @Override
    public void invokeInputOption(final InputOption io) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");
    }

    @Override
    public void printText(List<String> messages) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");

    }

    @Override
    public void printImage(Bitmap image) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");
    }

    @Override
    public void printImageFromURL(String url) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Operation Not supported for cloverGo");
    }
}
