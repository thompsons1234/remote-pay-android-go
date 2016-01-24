package com.clover.remote.terminal;

import android.os.Parcel;
import android.os.Parcelable;

public enum UiState implements Parcelable {
  // payment flow
  START,
  FAILED,
  FATAL,
  TRY_AGAIN,
  INPUT_ERROR,
  PIN_BYPASS_CONFIRM,
  CANCELED,
  TIMED_OUT,
  DECLINED,
  VOIDED,
  CONFIGURING,
  PROCESSING,
  REMOVE_CARD,
  PROCESSING_GO_ONLINE,
  PROCESSING_CREDIT,
  PROCESSING_SWIPE,
  SELECT_APPLICATION,
  PIN_PAD,
  MANUAL_CARD_NUMBER,
  MANUAL_CARD_CVV,
  MANUAL_CARD_CVV_UNREADABLE,
  MANUAL_CARD_EXPIRATION,
  SELECT_ACCOUNT,
  CASHBACK_CONFIRM,
  CASHBACK_SELECT,
  CONTACTLESS_TAP_REQUIRED,
  VOICE_REFERRAL_RESULT,
  CONFIRM_PARTIAL_AUTH,
  PACKET_EXCEPTION,
  CONFIRM_DUPLICATE_CHECK,
  FORCE_ACCEPTANCE,

  // verify CVM flow
  VERIFY_SIGNATURE_ON_PAPER,
  VERIFY_SIGNATURE_ON_PAPER_CONFIRM_VOID,
  VERIFY_SIGNATURE_ON_SCREEN,
  VERIFY_SIGNATURE_ON_SCREEN_CONFIRM_VOID,
  ADD_SIGNATURE,
  SIGNATURE_ON_SCREEN_FALLBACK,
  RETURN_TO_MERCHANT,
  SIGNATURE_REJECT,
  ADD_SIGNATURE_CANCEL_CONFIRM,

  // add tip flow
  ADD_TIP,

  // receipt options flow
  RECEIPT_OPTIONS,

  // tender handling flow
  HANDLE_TENDER,
  // for DCPOS language selection
  SELECT_LANGUAGE,
  // for DCPOS final approved screen
  APPROVED,
  ;

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(final Parcel dest, final int flags) {
    dest.writeString(name());
  }

  public static final Creator<UiState> CREATOR = new Creator<UiState>() {
    @Override
    public UiState createFromParcel(final Parcel source) {
      return UiState.valueOf(source.readString());
    }

    @Override
    public UiState[] newArray(final int size) {
      return new UiState[size];
    }
  };

  public enum UiDirection implements Parcelable {
    ENTER,
    EXIT;

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
      dest.writeString(name());
    }

    public static final Creator<UiDirection> CREATOR = new Creator<UiDirection>() {
      @Override
      public UiDirection createFromParcel(final Parcel source) {
        return UiDirection.valueOf(source.readString());
      }

      @Override
      public UiDirection[] newArray(final int size) {
        return new UiDirection[size];
      }
    };
  }
}
