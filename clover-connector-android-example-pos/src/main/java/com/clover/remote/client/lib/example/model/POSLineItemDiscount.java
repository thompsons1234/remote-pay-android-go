package com.clover.remote.client.lib.example.model;

public class POSLineItemDiscount extends POSDiscount
{
    public POSLineItemDiscount(long fixedDiscountAmount, String name)
    {
        setAmountOff(fixedDiscountAmount);
        this.name = name;
    }
    public POSLineItemDiscount(float percentageOff, String name)
    {
        setPercentageOff(percentageOff);
        this.name = name;
    }

    public long getValue(POSItem item)
    {
        if(getAmountOff() > 0)
        {
            return getAmountOff();
        }
        else
        {
            return (int)(item.getPrice() * getPercentageOff());
        }
    }

    public String getName() {
        return name;
    }
}
