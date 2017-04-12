package com.clover.remote.client;

import com.clover.remote.InputOption;
import com.clover.remote.order.DisplayOrder;

public interface IDeviceConnector {
  /**
   * Show a message on the Clover Mini screen
   *
   * @param message -
   **/
  void showMessage(String message);

  /**
   * Return the device to the Welcome Screen
   **/
  void showWelcomeScreen();

  /**
   * Show the thank you screen on the device
   **/
  void showThankYouScreen();

  /**
   * display the payment receipt screen for the orderId/paymentId combination.
   *
   * @param paymentId
   * @param orderId
   */
  void displayPaymentReceiptOptions(String orderId, String paymentId);

  /**
   * Will trigger cash drawer to open that is connected to Clover Mini
   **/
  void openCashDrawer(String reason);

  /**
   * Show the DisplayOrder on the device. Replaces the existing DisplayOrder on the device.
   *
   * @param order -
   **/
  void showDisplayOrder(DisplayOrder order);

  /**
   * Remove the DisplayOrder from the device.
   *
   * @param order -
   **/
  void removeDisplayOrder(DisplayOrder order);

  /**
   * Used to invoke user options on the mini such as "OK", "CANCEL", "DONE", etc.
   *
   * @param io
   */
  void invokeInputOption(InputOption io);

  /**
   * Used to reset the device if it gets in an invalid state from POS perspective.
   * This could cause a missed transaction or other missed information, so it
   * needs to be used cautiously as a last resort
   */
  void resetDevice();

  /**
   * used to cancel the current user action on the device.
   */
  void cancel();

  /**
   * start an custom activity on the device
   */
  void startCustomActivity(CustomActivityRequest request);

}
