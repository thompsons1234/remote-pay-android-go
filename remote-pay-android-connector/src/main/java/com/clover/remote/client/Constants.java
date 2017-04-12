package com.clover.remote.client;

/**
 * Created by ddmoore on 4/11/17.
 */
public class Constants {
  private static final int KIOSK_CARD_ENTRY_METHODS = 1 << 15;
  public static final int CARD_ENTRY_METHOD_MANUAL = 0b1000 | 0b1000_00000000 | KIOSK_CARD_ENTRY_METHODS; // 34824
  public static final int CARD_ENTRY_METHOD_NFC_CONTACTLESS = 0b0100 | 0b0100_00000000 | KIOSK_CARD_ENTRY_METHODS; // 33796
  public static final int CARD_ENTRY_METHOD_ICC_CONTACT = 0b0010 | 0b0010_00000000 | KIOSK_CARD_ENTRY_METHODS; // 33282
  public static final int CARD_ENTRY_METHOD_MAG_STRIPE = 0b0001 | 0b0001_00000000 | KIOSK_CARD_ENTRY_METHODS; // 33026
}
