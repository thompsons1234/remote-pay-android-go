package com.clover.remote.client.lib.example.model;

import java.io.Serializable;

public class POSDiscount
    {
        public String name;
        private long _amountOff = 0;
        private float _percentageOff = 0.0f;

        public POSDiscount()
        {
            name = "";
        }
        public POSDiscount(String name, float percentOff)
        {
            this.name = name;
            _percentageOff = percentOff;
        }
        public POSDiscount(String name, long amountOff)
        {
            this.name = name;
            amountOff = amountOff;
        }

        public long getAmountOff() {
            return _amountOff;
        }

        public void setAmountOff(long value) {
                _percentageOff = 0.0f;
                _amountOff = value;
        }

        public float getPercentageOff() {
            return _percentageOff;
        }
        public void setPercentageOff(float value) {
            _amountOff = 0;
            _percentageOff = value;
        }

        public String getName() {
            return name;
        }

        protected long appliedTo(long sub)
        {
            if(getAmountOff() == 0)
            {
                sub = (long)Math.round(sub - (sub * getPercentageOff()));
            }
            else
            {
                sub -= getAmountOff();
            }
            return Math.max(sub, 0);
        }

        public long getValue(long sub)
        {
            long value = _amountOff;
            if (getAmountOff() == 0)
            {
                value = (long)Math.round(sub * getPercentageOff());
            }

            return value;
        }
    }
