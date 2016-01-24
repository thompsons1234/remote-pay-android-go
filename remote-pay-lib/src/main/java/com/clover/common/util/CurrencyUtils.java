package com.clover.common.util;

import android.content.Context;
import android.widget.Button;
import com.clover.sdk.v3.inventory.Item;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;


public class CurrencyUtils {

  private CurrencyUtils() { }

  private static ThreadLocal<DecimalFormat> DECIMAL_FORMAT = new ThreadLocal<DecimalFormat>() {
    @Override
    protected DecimalFormat initialValue() {
      return (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.getDefault());
    }
  };

  private static DecimalFormat getCurrencyFormatInstance(Currency currency) {
    DecimalFormat format = DECIMAL_FORMAT.get();
    if (format.getCurrency() != currency) {
      format.setCurrency(currency);
    }
    return format;
  }

  private static final Decimal HUNDRED_CENTS = new Decimal("100");

  // Convert an amount string to a long (i.e. 1.50 -> 150)
  public static Long amountStringToLong(String amountStr, Currency currency) {
    try {
      NumberFormat format = NumberFormat.getInstance();
      Number number = format.parse(amountStr);
      return new Decimal(number.doubleValue()).multiply(new Decimal(Math.pow(10, currency.getDefaultFractionDigits()))).setScale(0, RoundingMode.HALF_UP).longValue();
    } catch (ParseException e) {
      throw new NumberFormatException(amountStr);
    }
  }

  @Deprecated
  public static boolean currencyPositionInFront() {
    return currencyPositionInFront(Currency.getInstance(Locale.getDefault()));
  }

  /* Returns true if currency symbol is before number, false if after */
  public static boolean currencyPositionInFront(Currency currency) {
    NumberFormat format = getCurrencyFormatInstance(currency);
    String str = format.format(5);
    if (str.charAt(0) == '5') {
      return false;
    } else {
      return true;
    }
  }

  public static String currencySymbol(Currency currency) {
    return currency.getSymbol();
  }

  public static double longToDecimal(double num, Currency currency) {
    return num / Math.pow(10, currency.getDefaultFractionDigits());
  }

  // Take a long and convert it to an amount string (i.e 150 -> $1.50)
  public static String longToAmountString(Currency currency, long amt) {
    DecimalFormat decimalFormat = getCurrencyFormatInstance(currency);
    return longToAmountString(currency, amt, decimalFormat);
  }

  // Take a long and convert it to an amount string (i.e 150 -> $1.50)
  public static String longToAmountString(Currency currency, long amt, DecimalFormat decimalFormat) {
    return decimalFormat.format(longToDecimal(amt, currency));
  }

  // Convert a positive value into a negative one
  public static Long negativeValue(Long value) {
    return -1 * value;
  }

  // Convert a possibly negative-formatted amount into a negative Long (i.e. ($1.50) -> -150)
  public static Long possibleNegativeAmountStringToLong(Currency currency, String value) {
    boolean isNegative = value.length() > 2 && value.substring(0, 1).equals("(") && value.substring(value.length() - 1).equals(")");
    if (isNegative) {
      value = value.replace("(", "");
      value = value.replace(")", "");
    }
    Long v = amountStringToLong(stripDollarAndComma(currency, value), currency);
    if (isNegative) {
      v = negativeValue(v);
    }

    return v;
  }

  public static String centsToDollarString(Currency currency, String amount, boolean noFormatting) {
    DecimalFormat format = getCurrencyFormatInstance(currency);
    return centsToDollarString(currency, format, amount, noFormatting);
  }

  public static String centsToDollarString(Currency currency, DecimalFormat decimalFormat, String amount, boolean noFormatting) {
    long value = Long.valueOf(amount);
    String newAmt = CurrencyUtils.longToAmountString(currency, value, decimalFormat);
    if (noFormatting) {
      DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
      newAmt = newAmt.replace(currencySymbol(currency), "");
      newAmt = newAmt.replace(String.valueOf(symbols.getGroupingSeparator()), "");
      newAmt = newAmt.replace("\u00A0", "");
    }
    return newAmt;
  }

  /* format string from edittext (e.g. 0.00) to properly-formatted dollar amount. can also be used for custom keyboard button presses.
   * str: initial string, usually edittext.getText().toString()
   * zeroZeroButton: if a custom keyboard is used, pass in 00 button to disable it at the right times. otherwise null.
   * toAdd: if a custom keyboard is used, pass in the text of the button being pressed (e.g. button1.getText().toString()) so it can be added. otherwise null.
   * noFormatting: true if you want to exclude $ and , from the final string. false if you want them included.
   */
  public static String formatAmountForEditText(Currency currency, String str, Button zeroZeroButton, boolean isDelete, String toAdd, boolean noFormatting) {
    String a = CurrencyUtils.stripAmountStr(currency, str);
    try {
      if (isDelete) {
        if (a != null && !a.equals("")) {
          a = a.substring(0, a.length() - 1);
          long amt = Long.valueOf(a);
          if (amt < 100.0 && zeroZeroButton != null) {
            zeroZeroButton.setEnabled(true);
          }
          String newAmt = CurrencyUtils.longToAmountString(currency, amt);
          if (noFormatting) {
            DecimalFormat format = getCurrencyFormatInstance(currency);
            DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
            newAmt = newAmt.replace(currencySymbol(currency), "");
            newAmt = newAmt.replace(String.valueOf(symbols.getGroupingSeparator()), "");
            newAmt = newAmt.replace("\u00A0", "");
          }
          return newAmt;
        }
      } else {
        if (a != null) {
          if (toAdd != null) {
            a = a + toAdd;
          }
          final Long val = Long.valueOf(a);
          if (val <= 9999999) {
            if (val >= 100.0 && zeroZeroButton != null) {
              zeroZeroButton.setEnabled(false);
            }
            String newAmt = CurrencyUtils.centsToDollarString(currency, a, noFormatting);
            return newAmt;
          } else {
            if (toAdd == null) {
              //over 9999999; ignore last number entered
              return CurrencyUtils.centsToDollarString(currency, a.substring(0, a.length() - 1), noFormatting);
            }
            return str;
          }
        }
      }
    } catch (NumberFormatException e) {
      return "";
    }
    return "";
  }

  //$1,500.00 -> 150000
  public static String stripAmountStr(Currency currency, String amount) {
    if (amount != null) {
      amount = amount.replace(currencySymbol(currency), "");
      amount = amount.replace(".", "");
      amount = amount.replace(",", "");
      amount = amount.replace("\u00A0", "");
      amount = amount.trim();
    }

    return amount;
  }

  //$1,500.00 -> 1500.00
  @Deprecated
  public static String stripDollarAndComma(Currency currency, Long amt) {
    return stripCurrencyAndGrouping(currency, amt);
  }

  public static String stripCurrencyAndGrouping(Currency currency, Long amt) {
    String amount = longToAmountString(currency, amt);
    if (amount != null) {
      DecimalFormat format = getCurrencyFormatInstance(currency);
      DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
      amount = amount.replace(currencySymbol(currency), "");
      amount = amount.replace(String.valueOf(symbols.getGroupingSeparator()), "");
      amount = amount.replace("\u00A0", "");
      amount = amount.trim();
    }

    return amount;
  }

  //$1,500.00 -> 1500.00
  @Deprecated
  public static String stripDollarAndComma(Currency currency, String amount) {
    return stripCurrencyAndGrouping(currency, amount);
  }

    public static String stripCurrencyAndGrouping(Currency currency, String amount) {
    if (amount != null) {
      DecimalFormat format = getCurrencyFormatInstance(currency);
      DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
      amount = amount.replace(currencySymbol(currency), "");
      amount = amount.replace(String.valueOf(symbols.getGroupingSeparator()), "");
      amount = amount.replace("\u00A0", "");
      amount = amount.trim();
    }

    return amount;
  }

  public static String priceAndTypeString(Context context, Item item, Currency currency) {
    switch (item.getPriceType()) {
      case FIXED:
        return longToAmountString(currency, item.getPrice());
      case VARIABLE:
        return "Variable";
      case PER_UNIT:
        return "($5.00)/(gram)";
      default:
        throw new RuntimeException();
    }
  }


  /**
   * @param currency
   * @return list of common bills (common coins and/or banknotes)
   */
  public static long[] getCurrencyCommonBills(Currency currency) {
    if ("USD".equals(currency.getCurrencyCode())) {
      return commonUSBills;
    } else if ("EUR".equals(currency.getCurrencyCode())) {
      return commonEURBills;
    } else if ("GBP".equals(currency.getCurrencyCode())) {
      return commonGBPBills;
    } else if ("PLN".equals(currency. getCurrencyCode())) {
      return commonPLNBills;
    }

    return commonAmounts;
  }

  /**
   * get the cash suggestions based on the common bills
   *
   * @param currency current amount currency
   * @param amount cash received
   * @return sorted cash amounts list (in natural order)
   */
  public static ArrayList<Long> getCashSuggestions(Currency currency, long amount) {
    TreeSet<Long> suggestions = new TreeSet<Long>();
    suggestions.add(amount);

    long[] commonBills = getCurrencyCommonBills(currency);

    for (int i = 0; i < commonBills.length; i++) {
      long currentBill = commonBills[i];
      boolean divided = (amount >= currentBill) && (amount % currentBill == 0);
      if (divided) {
        if (currentBill > amount) {
          suggestions.add(currentBill);
        }
      } else {
        long suggestedAmount = ((amount + currentBill) / currentBill) * currentBill;
        suggestions.add(suggestedAmount);
      }
    }

    return new ArrayList<Long>(suggestions);
  }

  public static List<Long> getCashSuggestions(Currency currency, long amount, int maxSuggestionsNumber) {
    TreeSet<Long> suggestions = new TreeSet<Long>(getCashSuggestions(currency, amount));
    ArrayList<Long> list = new ArrayList<Long>(suggestions);
    if (list.size() == maxSuggestionsNumber) {
      return list;

    } else if (list.size() > maxSuggestionsNumber) {
      return list.subList(0, maxSuggestionsNumber);

    } else {
      //just fill the list with some other commonAmounts
      for (int i = 0; (i < commonAmounts.length - 1 && suggestions.size() < maxSuggestionsNumber); i++) {
        long additionalAmount = getClosestCashAmount(currency, amount, i + 1);
        if (additionalAmount > 0) {
          suggestions.add(additionalAmount);
        }
      }
      return new ArrayList<Long>(suggestions);
    }
  }

  public static final long[] commonGBPBills = {100, 500, 1000, 2000, 5000, 10000};

  /** 1, 2, 5 are actually coins */
  public static final long[] commonPLNBills = {100, 200, 500, 1000, 2000, 5000, 10000, 20000};

  /** 1 and 2 are actually coins **/
  public static final long[] commonEURBills = {100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000};

  public static final long[] commonUSBills = {100, 500, 1000, 2000, 5000, 10000};

  public static final long[] commonAmounts = {100, 500, 1000, 1500, 2000, 2500, 3000, 4000, 5000, 6000, 7000, 8000, 10000, 12000, 15000, 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000, 100000, 200000};

  private static long getClosestCashAmount(Currency currency, long amount, int offset) {
    if (offset == 0) {
      return amount;
    }

    int currentCommonAmount = -1;

    double fractionDigits = Math.pow(10, currency.getDefaultFractionDigits());
    boolean isEvenDollar = (amount % fractionDigits) == 0;

    //find closest common amount
    for (int i = 0; i < commonAmounts.length; i++) {
      if (commonAmounts[i] > amount) {
        currentCommonAmount = i;
        break;
      }
    }

    long closestAmount = -1; // indicator not found

    for (int i = 1; i <= offset; i++) {
      //if amount is 3.43, make button2 4.00
      if (i == 1 && !isEvenDollar) {
        closestAmount = (long) (Math.ceil((double) amount / fractionDigits) * fractionDigits);
        if (currentCommonAmount > -1 && currentCommonAmount < commonAmounts.length && closestAmount == commonAmounts[currentCommonAmount]) {
          currentCommonAmount++;
        }
      } else {
        // next common amount
        if (currentCommonAmount < commonAmounts.length && currentCommonAmount > -1) {
          closestAmount = commonAmounts[currentCommonAmount];
          currentCommonAmount++;
        } else {
          closestAmount = -1;
        }
      }
      if (offset == i) {
        return closestAmount;
      }
    }
    return -1;
  }

  /**
   * Immutable signed decimal numbers with a limited range and precision. The goal of the Decimal class is to provide the
   * same basic functionality as BigDecimal except for the unlimited range and precision and with much higher performance.
   * Overflow and NaN behavior is undefined.
   */
  static class Decimal extends Number implements Comparable<Decimal> {
    public static final int DEFAULT_SCALE = 7;
    public static final int MAX_SCALE = 9;
    static final int[] MULTIPLIERS = new int[MAX_SCALE+1];

    public static final double MIN_FRACTION;

    static {
      for (int i = 0; i < MULTIPLIERS.length; i++) {
        MULTIPLIERS[i] = pow(10, i);
      }

      MIN_FRACTION = 1.0 / MULTIPLIERS[MAX_SCALE];
    }

    public static final Decimal ZERO = new Decimal(0, 0);
    public static final Decimal ONE = new Decimal(1, 0);

    protected final double value;
    protected final byte scale;

    /**
     * Construct a new Decimal with the specified value and scale.
     */
    public Decimal(double dbl, int scale) {
      value = roundDouble(dbl, MULTIPLIERS[scale]);
      this.scale = (byte) scale;
    }

    /**
     * Construct a new Decimal with the DEFAULT_SCALE. Prefer explicitly setting scale with {@link #Decimal(double, int)}.
     * @deprecated
     */
    public Decimal(double dbl) {
      this(dbl, DEFAULT_SCALE);
    }

    /**
     * Construct a new Decimal that is a copy of the given Decimal.
     */
    public Decimal(Decimal dval) {
      value = dval.value;
      scale = dval.scale;
    }

    /**
     * Construct a new Decimal from the given Decimal with the same value but a different scale.
     */
    public Decimal(Decimal dval, int newScale) {
      if (newScale < dval.scale) {
        value = roundDouble(dval.value, MULTIPLIERS[newScale]);
      } else {
        value = dval.value;
      }
      scale = (byte) newScale;
    }

    /**
     * Construct a new Decimal from the given ObjectInput.
     */
    public Decimal(ObjectInput in) throws IOException {
      value = in.readDouble();
      scale = in.readByte();
    }

    /**
     * Construct a new Decimal with the given long using the DEFAULT scale. It does not divide the long like the
     * {@link #Decimal(long, int)} constructor!
     * @deprecated
     */
    public Decimal(long lval) {
      this((double) lval, DEFAULT_SCALE);
    }

    /**
     * Construct a new Decimal with the value set to (lval / 10^scale) and the specified scale.
     */
    public Decimal(long lval, int scale) {
      this(((double) lval) / MULTIPLIERS[scale], scale);
    }

    /**
     * Construct a new Decimal from the given string. The scale is determined by the number of places after the decimal
     * point. The following regex describes acceptable input: [+-]?(([0-9]*\.[0-9]+)|[0-9]+). This function will not
     * accept a more precise value than can be stored accurately.
     */
    public Decimal(String str) {
      final int start;
      final boolean negative;

      final char firstChar = str.charAt(0);
      if (firstChar == '-') {
        negative = true;
        start = 1;
      } else if (firstChar == '+') {
        negative = false;
        start = 1;
      } else {
        negative = false;
        start = 0;
      }

      long res = 0;
      int precision = 0;

      for (int i = start, length = str.length(); i < length; i++) {
        final char c = str.charAt(i);
        if (c >= '0' && c <= '9') {
          res = res * 10 + (c - '0');
        } else if (c == '.') {
          if (precision > 0) {
            throw new IllegalArgumentException("Input String contains too many decimal points: " + str);
          }
          precision = length - i - 1;
        } else {
          throw new IllegalArgumentException();
        }
      }

      if (precision > MAX_SCALE) {
        throw new IllegalArgumentException("Input String has more precision than can be represented accurately: " + str);
      }

      double v = (double)res / MULTIPLIERS[precision];
      value = negative ? -v : v;
      scale = (byte)precision;
    }

    public Decimal add(Decimal dval) {
      Decimal result = new Decimal(value + dval.value, Math.max(scale, dval.scale));
      return result;
    }

    public Decimal subtract(Decimal dval) {
      Decimal result = new Decimal(value - dval.value, Math.max(scale, dval.scale));
      return result;
    }

    public Decimal divide(Decimal dval) {
      Decimal result = new Decimal(value / dval.value, Math.max(scale, dval.scale));
      return result;
    }

    public Decimal divide(long lval) {
      Decimal result = new Decimal(value / (double) lval, scale);
      return result;
    }

    public Decimal divide(Decimal dval, int newScale, RoundingMode newRoundingMode) {
      if (newRoundingMode != RoundingMode.HALF_UP) {
        throw new IllegalArgumentException();
      }
      return new Decimal(value / dval.value, newScale);
    }

    public Decimal multiply(Decimal dval) {
      Decimal result = new Decimal(value * dval.value, Math.max(scale, dval.scale));
      return result;
    }

    public Decimal multiply(BigDecimal bdval) {
      Decimal result = new Decimal(value * bdval.doubleValue(), Math.max(scale, bdval.scale()));
      return result;
    }

    public Decimal multiply(long lval) {
      Decimal result = new Decimal(value * (double) lval, scale);
      return result;
    }

    public Decimal multiply(Decimal dval, int newScale, RoundingMode newRoundingMode) {
      if (newRoundingMode != RoundingMode.HALF_UP) {
        throw new IllegalArgumentException();
      }
      Decimal result = new Decimal(value * dval.value, newScale);
      return result;
    }

    public Decimal setScale(int newScale, RoundingMode newRoundingMode) {
      if (newRoundingMode != RoundingMode.HALF_UP) {
        throw new IllegalArgumentException("RoundingMode.HALF_UP is the only accepted rounding mode");
      }

      if (scale == newScale) {
        return this;
      }

      return new Decimal(this, newScale);
    }

    @Override
    public double doubleValue() {
      return value;
    }

    @Override
    public float floatValue() {
      return (float) value;
    }

    @Override
    public long longValue() {
      return (long) value;
    }

    @Override
    public int intValue() {
      return (int) value;
    }

    protected static double roundDouble(double value, int multiplier) {
      // 2 is a magical number that ensures negative numbers go over the threshold required by Math.floor
      double multiplied = Double.longBitsToDouble(Double.doubleToLongBits(value * multiplier) + 2l);
      return Math.floor(multiplied + 0.5d) / multiplier;
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (o != null && o instanceof Decimal) {
        Decimal d = (Decimal) o;
        return value == d.value;
      }
      return false;
    }

    @Override
    public int hashCode() {
      long bits = Double.doubleToLongBits(value);
      return (int) (bits ^ (bits >>> 32));
    }

    @Override
    public int compareTo(Decimal o) {
      if (equals(o)) {
        return 0;
      } else if (value > o.value) {
        return 1;
      } else {
        return -1;
      }
    }

    @Override
    public String toString() {
      return fasterFormat(value, scale);
    }

    /**
     * Construct a new Decimal by first converting the double to a String with the maximum scale allowed and then parsing
     * that String.
     */
    public static Decimal valueOf(double dval) {
      DecimalFormat df = new DecimalFormat("#");
      df.setMaximumFractionDigits(MAX_SCALE);
      return new Decimal(df.format(dval));
    }

    protected static String fasterFormat(double val, int precision) {
      StringBuilder sb = new StringBuilder();
      if (val < 0) {
        sb.append('-');
        val = -val;
      }
      final int exp = MULTIPLIERS[precision];
      final long lval = (long)(val * exp + 0.5);
      sb.append(lval / exp);
      if (precision > 0) {
        sb.append('.');
        final long fval = lval % exp;
        for (int p = precision - 1; p > 0 && fval < MULTIPLIERS[p]; p--) {
          sb.append('0');
        }
        sb.append(fval);
      }
      return sb.toString();
    }

    protected static int pow(int a, int b) {
      if (b == 0) {
        return 1;
      }
      int result = a;
      while (--b > 0) {
        result *= a;
      }
      return result;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
      out.writeDouble(value);
      out.writeByte(scale);
    }
  }
}
