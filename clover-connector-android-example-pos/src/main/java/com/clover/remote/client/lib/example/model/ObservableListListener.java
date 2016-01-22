package com.clover.remote.client.lib.example.model;

import java.util.Collection;

/**
 * Created by blakewilliams on 12/22/15.
 */
public interface ObservableListListener<T> {
    public void itemAdded(T item, int index);
    public void itemRemoved(T item, int index);
    public void itemsAdded(Collection<T> items);
    public void itemsRemoved(Collection<T> items);
    public void listCleared();
}
