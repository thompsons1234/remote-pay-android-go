package com.clover.remote.client.lib.example.model;

import java.io.Serializable;

public class POSItem
    {
        private boolean tippable;
        private boolean taxable;
        private String id;
        private long price;
        private String name;

        public POSItem(String id, String name, long price) {
            this(id, name, price, true, true);
        }

        public POSItem(String id, String name, long price, boolean taxable, boolean tippable)
        {
            this.id = id;
            this.name = name;
            this.price = price;
            this.taxable = taxable;
            this.tippable = tippable; //
        }

        public String getName() {
            return name;
        }
        public long getPrice() {
            return price;
        }

        public boolean isTippable() {
            return tippable;
        }

        public boolean isTaxable() {
            return taxable;
        }

        public String getId() {
            return id;
        }
    }
