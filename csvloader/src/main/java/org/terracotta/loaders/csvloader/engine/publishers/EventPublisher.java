package org.terracotta.loaders.csvloader.engine.publishers;

import com.lmax.disruptor.RingBuffer;

/**
 * Created by FabienSanglier on 7/10/14.
 */
public interface EventPublisher<T> {
    //publish all data to the multi-threaded engine
    void publishAll(RingBuffer<T> ringBuffer);
}