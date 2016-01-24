package com.clover.remote.client.messages;

public class VoidPaymentRequest extends BaseRequest
    {
        private String paymentId;
        private String voidReason; // {USER_CANCEL}

        private String employeeId;//optional TODO: Revisit
        private String orderId; //optional TODO: Revisit

        public String getPaymentId() {
            return paymentId;
        }

        public void setPaymentId(String paymentId) {
            this.paymentId = paymentId;
        }

        public String getVoidReason() {
            return voidReason;
        }

        public void setVoidReason(String voidReason) {
            this.voidReason = voidReason;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
    }


