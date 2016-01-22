package com.clover.remote.client.lib.example.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by blakewilliams on 12/22/15.
 */
public class ObservableList<T> extends ArrayList<T> {
    List<ObservableListListener<T>> observers = new ArrayList<ObservableListListener<T>>();

    public void addObserver(ObservableListListener<T> listener) {
        observers.add(listener);
    }

    public void removeObserver(ObservableListListener<T> listener) {
        observers.add(listener);
    }

    private void notifyItemAdded(T obj, int index) {
        for(ObservableListListener listener : observers) {
            listener.itemAdded(obj, index);
        }
    }

    private void notifyItemsAdded(Collection<T> obj) {
        for(ObservableListListener listener : observers) {
            listener.itemsAdded(obj);
        }
    }

    private void notifyItemRemoved(T obj, int index) {
        for(ObservableListListener listener : observers) {
            listener.itemRemoved(obj, index);
        }
    }

    private void notifyItemsRemoved(Collection<T> obj) {
        for(ObservableListListener listener : observers) {
            listener.itemsRemoved(obj);
        }
    }

    private void notifyListCleared() {
        for(ObservableListListener listener : observers) {
            listener.listCleared();
        }
    }


    @Override
    public void clear() {
        super.clear();
        notifyListCleared();
    }

    @Override
    public boolean add(T object) {
        boolean result = super.add(object);
        notifyItemAdded(object, size()-1);
        return result;
    }

    @Override
    public void add(int index, T object) {
        super.add(index, object);
        notifyItemAdded(object, index);
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        boolean result = super.addAll(collection);
        if(result) {
            notifyItemsAdded((Collection<T>) collection);
        }
        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> collection) {
        boolean result = super.addAll(index, collection);
        if(result) {
            notifyItemsAdded((Collection<T>) collection);
        }
        return result;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        List<T> removed = new ArrayList<T>();
        for(int i=fromIndex; i<=toIndex; i++) {
            removed.add(get(i));
        }
        super.removeRange(fromIndex, toIndex);
        notifyItemsRemoved(removed);
    }

    @Override
    public boolean remove(Object object) {
        int idx = indexOf(object);
        boolean result = super.remove(object);
        if(result) {
            notifyItemRemoved((T)object, idx);
        }
        return result;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean result = super.removeAll(collection);
        if(result) {
            notifyItemsRemoved((Collection<T>) collection);
        }
        return result;
    }

    @Override
    public T remove(int index) {
        T result = super.remove(index);
        notifyItemRemoved(result, index);
        return result;
    }
}
