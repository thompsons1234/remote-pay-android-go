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

package com.clover.remote.client.clovergo;

import com.clover.remote.client.ICloverConnector;
import com.firstdata.clovergo.domain.model.ReaderInfo;

public interface ICloverGoConnector extends ICloverConnector {

  /**
   * add an ICloverConnectorListener to receive callbacks
   *
   * @param listener
   */
  public void addCloverGoConnectorListener(ICloverGoConnectorListener listener);

  /**
   * remove an ICloverConnectorListener from receiving callbacks
   *
   * @param listener
   */
  public void removeCloverGoConnectorListener(ICloverGoConnectorListener listener);


  void connectToBluetoothDevice(ReaderInfo readerInfo);

  void disconnectDevice();

  void stopDeviceScan();

  void sendReceipt(String email, String phoneNo, String orderId);

}
