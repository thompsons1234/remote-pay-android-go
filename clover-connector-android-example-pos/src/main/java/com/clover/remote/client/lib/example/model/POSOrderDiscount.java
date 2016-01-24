package com.clover.remote.client.lib.example.model;

public class POSOrderDiscount extends POSDiscount
    {
        public POSOrderDiscount(long fixedDiscountAmount, String name)
        {
            setAmountOff(fixedDiscountAmount);
            this.name = name;
        }
        public POSOrderDiscount(float percentageOff, String name)
        {
            setPercentageOff(percentageOff);
            this.name = name;
        }

        public long Value(POSOrder order)
        {
            if (getAmountOff() > 0)
            {
                return getAmountOff();
            }
            else
            {
                return (int)(order.getPreTaxSubTotal() * getPercentageOff());
            }
        }

    }
