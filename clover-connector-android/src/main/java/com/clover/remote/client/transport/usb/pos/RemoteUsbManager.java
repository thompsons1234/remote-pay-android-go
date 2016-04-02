package com.clover.remote.client.transport.usb.pos;

import android.content.Context;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import com.clover.common.analytics.ALog;
import android.util.Pair;
import com.clover.remote.client.transport.RemoteStringConduit;
import com.clover.remote.client.transport.usb.UsbCloverManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;


public class RemoteUsbManager extends UsbCloverManager<Void> implements RemoteStringConduit {

  private final String TAG = getClass().getSimpleName();

  private static final boolean VERBOSE = true;

  private static final Charset UTF_8 = Charset.forName("UTF-8");

  private static final int REMOTE_STRING_MAGIC_START_TOKEN = 0xcc771122;
  private static final int REMOTE_STRING_LENGTH_MAX = 4 * 1024 * 1024;
  private static final int REMOTE_STRING_HEADER_BYTE_COUNT = 4 + 4; // 2 ints

  // Defined by AOA
  private static final int MAX_PACKET_BYTES = 16384;
  // Size of a short
  private static final int PACKET_HEADER_SIZE = 2;

  public RemoteUsbManager(Context context) {
    super(context);
  }

  @Override
  protected int getReadSize() {
    return MAX_PACKET_BYTES;
  }

  @Override
  protected int getMaxWriteDataSize() {
    return MAX_PACKET_BYTES - PACKET_HEADER_SIZE;
  }

  public static boolean isUsbDeviceAttached(Context context) {
    UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    return findDevice(usbManager, VENDOR_PRODUCT_IDS) != null;
  }

  // See init.maplecutter.usb.rc in platform for more info
  public static final Pair<Integer, Integer>[] VENDOR_PRODUCT_IDS = new Pair[] {
      // FIXME: No longer need these two?
      Pair.create(0x18d1, 0x2d00), // google accessory usb device
      Pair.create(0x18d1, 0x2d01), // google adb,accessory usb device

      Pair.create(0x28f3, 0x2002), // leafcutter accessory usb device
      Pair.create(0x28f3, 0x3002), // maplecutter accessory usb device
      Pair.create(0x28f3, 0x2004), // leafcutter adb,accessory usb device
      Pair.create(0x28f3, 0x3004), // maplecutter adb,accessory usb device
  };

  @Override
  protected Pair<Integer, Integer>[] getVendorProductIds() {
    return VENDOR_PRODUCT_IDS;
  }

  @Override
  protected boolean isInterfaceMatch(UsbInterface usbInterface) {
    if (VERBOSE) {
      ALog.d(this, "Checking interface match: %s", usbInterface);
    }

    // Specified to avoid using ADB interface
    if (usbInterface.getInterfaceClass() == 0xff
        && usbInterface.getInterfaceSubclass() == 0xff
        && usbInterface.getInterfaceProtocol() == 0) {
      return true;
    }

    return false;
  }

  @Override
  protected int getReadTimeOut() {
    return -1; // No timeout, wait forever
  }

  @Override
  protected byte[] wrapWritePacket(ByteBuffer outDataBuffer, int numBytesToTransfer) {
    ByteBuffer writePacketBuffer = ByteBuffer.allocate(numBytesToTransfer + PACKET_HEADER_SIZE);
    writePacketBuffer.putShort((short) numBytesToTransfer);
    outDataBuffer.get(writePacketBuffer.array(), PACKET_HEADER_SIZE, numBytesToTransfer);
    return writePacketBuffer.array();
  }

  @Override
  protected byte[] unwrapReadPacket(ByteBuffer inDataBuffer) {
    short inputSize = inDataBuffer.getShort();
    if (inputSize <= 0) {
      ALog.w(this, "Error, packet too small: %d bytes", inputSize);
      return null;
    }

    if (VERBOSE) {
      ALog.v(this, "Input packet size: %d bytes", inputSize);
    }

    byte[] inputData = new byte[inputSize];
    inDataBuffer.get(inputData);
    return inputData;
  }

  @Override
  protected byte[] processOutputData(byte[] outputData, Void aVoid) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);

    try {
      dos.writeInt(REMOTE_STRING_MAGIC_START_TOKEN);
      dos.writeInt(outputData.length);
      dos.write(outputData);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    byte[] processedOutput = baos.toByteArray();
    if (VERBOSE) {
      ALog.v(this, "Processed output: %s bytes", processedOutput.length);
    }

    return processedOutput;
  }

  private int mCurrentIncomingStringLength;

  @Override
  protected InputResult processInputData(byte[] inputData, ByteArrayOutputStream outputStream, Void params) {
    try {
      if (inputData == null) {
        throw new IOException("Read error");
      }

      int numBytesRead = inputData.length;

      if (numBytesRead == 0) {
        throw new IOException("Read zero bytes");
      }

      DataInputStream dis = new DataInputStream(new ByteArrayInputStream(inputData));

      if (outputStream.size() == 0) {
        // Start packet
        final int startInt = dis.readInt();
        if (startInt != REMOTE_STRING_MAGIC_START_TOKEN) {
//          mCounters.increment("pos.error.processinputdata.starttoken");
          throw new IOException("Unexpected start token: 0x" + Integer.toHexString(startInt));
        }

        final int totalStringLength = dis.readInt();
        if (totalStringLength <= 0 || totalStringLength > REMOTE_STRING_LENGTH_MAX) {
//          mCounters.increment("pos.error.processinputdata.length");
          throw new IOException("Illegal string length: " + totalStringLength + " bytes");
        }

        mCurrentIncomingStringLength = totalStringLength;

        outputStream.write(inputData, REMOTE_STRING_HEADER_BYTE_COUNT, numBytesRead - REMOTE_STRING_HEADER_BYTE_COUNT);
      } else {
        // Continuation packet
        outputStream.write(inputData, 0, numBytesRead);
      }

      int remainingBytes = mCurrentIncomingStringLength - outputStream.size();
      if (remainingBytes > 0) {
        return InputResult.CONTINUE;
      } else {
        mCurrentIncomingStringLength = 0;
        return InputResult.COMPLETE;
      }
    } catch (Exception e) {
      ALog.w(this, e, "Unable to process USB input data");
//      mCounters.increment("pos.error.processinputdata." + e.getClass().getSimpleName().toLowerCase());
      mCurrentIncomingStringLength = 0;
      return InputResult.ERROR;
    }
  }

  public void sendString(String string) throws IOException, InterruptedException {
    if (VERBOSE) {
      ALog.v(this, "Sending: %s", string);
    }

    byte[] stringBytes = string.getBytes(UTF_8);

    final int stringByteLength = stringBytes.length;
    if (stringByteLength <= 0 || stringByteLength > REMOTE_STRING_LENGTH_MAX) {
      if (stringByteLength <= 0) {
//        mCounters.increment("pos.error.sendstring.bytelength.zero");
      } else {
//        mCounters.increment("pos.error.sendstring.bytelength.max");
      }
      throw new IllegalArgumentException("String byte length " + stringByteLength + " bytes outside limits");
    }

    int numWrittenBytes = write(stringBytes, null);
    if (numWrittenBytes <= 0) {
      throw new IOException("USB bulk data write failed");
    }
  }

  public String receiveString() throws IOException, InterruptedException {
    byte[] stringBytes = read(null);
    if (stringBytes == null) {
      throw new IOException("USB bulk data read failed");
    }

    String string = new String(stringBytes, UTF_8);
    if (VERBOSE) {
      ALog.v(this, "Received: %s", string);
    }

    return string;
  }

}
