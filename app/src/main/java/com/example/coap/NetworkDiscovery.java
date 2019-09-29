package com.example.coap;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public abstract class NetworkDiscovery {

    Map<Integer, NeighborRecord> neighborRecords = new ConcurrentSkipListMap<>();
    INeighborChangedListener neighborChangedListener;

    public void setNeighborChangedListener(INeighborChangedListener listener) {
        this.neighborChangedListener = listener;
    }

    protected void notifyNeighborChanged(Collection<NeighborRecord> values) {
        if (neighborChangedListener != null) {
            neighborChangedListener.onNeighborChanged(values);
        }
    }

    public interface INeighborChangedListener {

        void onNeighborChanged(Collection<NeighborRecord> values);
    }
}
