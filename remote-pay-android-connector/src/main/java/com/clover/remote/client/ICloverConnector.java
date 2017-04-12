/*
 * Copyright (C) 2016 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clover.remote.client;

public interface ICloverConnector extends IPaymentConnector, IDeviceConnector, IPrintConnector {

  /**
   * Initialize the CloverConnector's connection. Must be called before calling any other method other than to add or remove listeners
   */
  void initializeConnection();

  /**
   * add an ICloverConnectorListener to receive callbacks
   * @param listener
   */
  public void addCloverConnectorListener(ICloverConnectorListener listener);

  /**
   * remove an ICloverConnectorListener from receiving callbacks
   * @param listener
   */
  public void removeCloverConnectorListener(ICloverConnectorListener listener);

  /**
   *  return the Merchant object for the Merchant configured for the Clover Mini
   **/

  /**
   * will dispose of the underlying connection to the device
   */
  void dispose();

}
