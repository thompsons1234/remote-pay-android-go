package com.clover.remote.protocol.message;

import com.clover.remote.order.DisplayOrder;
import com.clover.remote.order.action.AddDiscountAction;
import com.clover.remote.order.action.AddLineItemAction;
import com.clover.remote.order.action.OrderActionResponse;
import com.clover.remote.order.action.RemoveDiscountAction;
import com.clover.remote.order.action.RemoveLineItemAction;
import com.clover.remote.order.operation.DiscountsAddedOperation;
import com.clover.remote.order.operation.DiscountsDeletedOperation;
import com.clover.remote.order.operation.LineItemsAddedOperation;
import com.clover.remote.order.operation.LineItemsDeletedOperation;
import com.clover.remote.protocol.ByteArrayToBase64TypeAdapter;
import com.clover.remote.protocol.CloverSdkDeserializer;
import com.clover.remote.protocol.CloverSdkSerializer;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.Refund;
import com.clover.sdk.v3.payments.ServiceChargeAmount;
import com.clover.sdk.v3.payments.TaxableAmountRate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class Message {
  private static final String TAG = Message.class.getSimpleName();

  private static final Gson GSON;

  static {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(Refund.class, new CloverSdkSerializer<Refund>());
    builder.registerTypeAdapter(Payment.class, new CloverSdkSerializer<Payment>());
    builder.registerTypeAdapter(Payment.class, new CloverSdkDeserializer<Payment>(Payment.class));
    builder.registerTypeAdapter(Order.class, new CloverSdkSerializer<Order>());
    builder.registerTypeAdapter(Order.class, new CloverSdkDeserializer<Order>(Order.class));
    builder.registerTypeAdapter(Credit.class, new CloverSdkSerializer<Credit>());
    builder.registerTypeAdapter(Credit.class, new CloverSdkDeserializer<Credit>(Credit.class));
    builder.registerTypeAdapter(TaxableAmountRate.class, new CloverSdkSerializer<TaxableAmountRate>());
    builder.registerTypeAdapter(TaxableAmountRate.class, new CloverSdkDeserializer<TaxableAmountRate>(TaxableAmountRate.class));
    builder.registerTypeAdapter(ServiceChargeAmount.class, new CloverSdkSerializer<ServiceChargeAmount>());
    builder.registerTypeAdapter(ServiceChargeAmount.class, new CloverSdkDeserializer<ServiceChargeAmount>(ServiceChargeAmount.class));
    builder.registerTypeAdapter(DisplayOrder.class, new CloverSdkSerializer<DisplayOrder>());
    builder.registerTypeAdapter(DisplayOrder.class, new CloverSdkDeserializer<DisplayOrder>(DisplayOrder.class));
    builder.registerTypeAdapter(LineItemsAddedOperation.class, new CloverSdkSerializer<LineItemsAddedOperation>());
    builder.registerTypeAdapter(LineItemsAddedOperation.class, new CloverSdkDeserializer<LineItemsAddedOperation>(LineItemsAddedOperation.class));
    builder.registerTypeAdapter(LineItemsDeletedOperation.class, new CloverSdkSerializer<LineItemsDeletedOperation>());
    builder.registerTypeAdapter(LineItemsDeletedOperation.class, new CloverSdkDeserializer<LineItemsDeletedOperation>(LineItemsDeletedOperation.class));
    builder.registerTypeAdapter(DiscountsAddedOperation.class, new CloverSdkSerializer<DiscountsAddedOperation>());
    builder.registerTypeAdapter(DiscountsAddedOperation.class, new CloverSdkDeserializer<DiscountsAddedOperation>(DiscountsAddedOperation.class));
    builder.registerTypeAdapter(DiscountsDeletedOperation.class, new CloverSdkSerializer<DiscountsDeletedOperation>());
    builder.registerTypeAdapter(DiscountsDeletedOperation.class, new CloverSdkDeserializer<DiscountsDeletedOperation>(DiscountsDeletedOperation.class));
    builder.registerTypeAdapter(AddLineItemAction.class, new CloverSdkSerializer<AddLineItemAction>());
    builder.registerTypeAdapter(AddLineItemAction.class, new CloverSdkDeserializer<AddLineItemAction>(AddLineItemAction.class));
    builder.registerTypeAdapter(RemoveLineItemAction.class, new CloverSdkSerializer<RemoveLineItemAction>());
    builder.registerTypeAdapter(RemoveLineItemAction.class, new CloverSdkDeserializer<RemoveLineItemAction>(RemoveLineItemAction.class));
    builder.registerTypeAdapter(AddDiscountAction.class, new CloverSdkSerializer<AddDiscountAction>());
    builder.registerTypeAdapter(AddDiscountAction.class, new CloverSdkDeserializer<AddDiscountAction>(AddDiscountAction.class));
    builder.registerTypeAdapter(RemoveDiscountAction.class, new CloverSdkSerializer<RemoveDiscountAction>());
    builder.registerTypeAdapter(RemoveDiscountAction.class, new CloverSdkDeserializer<RemoveDiscountAction>(RemoveDiscountAction.class));
    builder.registerTypeAdapter(OrderActionResponse.class, new CloverSdkSerializer<OrderActionResponse>());
    builder.registerTypeAdapter(OrderActionResponse.class, new CloverSdkDeserializer<OrderActionResponse>(OrderActionResponse.class));

    builder.registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter());

    GSON = builder.create();
  }

  private static final JsonParser PARSER = new JsonParser();

  public final Method method;
  public final int version = 1;

  protected Message(Method method) {
    this.method = method;
  }

  public String toJsonString() {
    return GSON.toJson(this, this.getClass());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + toJsonString();
  }

  public static Message fromJsonString(String m) {
    JsonElement je = PARSER.parse(m);
    JsonObject jo = je.getAsJsonObject();
    Method method = Method.valueOf(jo.get("method").getAsString());
    Class<? extends Message> cls = method.cls;
    return GSON.fromJson(jo, cls);
  }
}
