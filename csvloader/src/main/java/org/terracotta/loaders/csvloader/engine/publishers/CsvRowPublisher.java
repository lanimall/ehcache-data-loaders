package org.terracotta.loaders.csvloader.engine.publishers;

import au.com.bytecode.opencsv.CSVReader;
import com.lmax.disruptor.RingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.loaders.csvloader.engine.CsvRowEvent;

import java.io.IOException;

/**
 * Created by FabienSanglier on 7/10/14.
 */
public class CsvRowPublisher implements EventPublisher<CsvRowEvent> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final CSVReader reader;
    private long rowIndex;

    public CsvRowPublisher(final CSVReader reader) {
        this(reader, 0);
    }

    public CsvRowPublisher(final CSVReader reader, final long rowIndex) {
        this.reader = reader;
        this.rowIndex = rowIndex;
    }

    @Override
    public void publishAll(RingBuffer<CsvRowEvent> ringBuffer) {
        String [] nextLine;
        if(null != reader) {
            try {
                while ((nextLine = reader.readNext()) != null) {
                    final long sequence = ringBuffer.next();
                    try {
                        ringBuffer.get(sequence).setValues(rowIndex++, nextLine);
                    } finally {
                        ringBuffer.publish(sequence);
                    }
                }
            } catch (IOException e) {
                log.error("Unexpected error", e);
            }
        }
    }
}
