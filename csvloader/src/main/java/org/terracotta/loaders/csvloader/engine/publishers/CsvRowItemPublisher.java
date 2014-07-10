package org.terracotta.loaders.csvloader.engine.publishers;

import au.com.bytecode.opencsv.CSVReader;
import com.lmax.disruptor.RingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.loaders.csvloader.engine.CsvRowItemEvent;

import java.io.IOException;

/**
 * Created by FabienSanglier on 7/10/14.
 */
public class CsvRowItemPublisher implements EventPublisher<CsvRowItemEvent> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final CSVReader reader;
    private long rowIndex;
    private String[] rowHeaders = null;
    private boolean validateRowDataAgainstHeaders = true;

    public CsvRowItemPublisher(final CSVReader reader) {
        this(reader, 1);
    }

    public CsvRowItemPublisher(final CSVReader reader, final long rowIndex) {
        this.reader = reader;
        this.rowIndex = rowIndex;
    }

    public boolean isValidateRowDataAgainstHeaders() {
        return validateRowDataAgainstHeaders;
    }

    public void setValidateRowDataAgainstHeaders(boolean validateRowDataAgainstHeaders) {
        this.validateRowDataAgainstHeaders = validateRowDataAgainstHeaders;
    }

    public String[] getRowHeaders() {
        return rowHeaders;
    }

    public void setRowHeaders(String[] rowHeaders) {
        this.rowHeaders = rowHeaders;
    }

    @Override
    public void publishAll(RingBuffer<CsvRowItemEvent> ringBuffer) {
        String [] nextLine;
        if(null != reader) {
            try {
                while ((nextLine = reader.readNext()) != null) {
                    if (validateRowDataAgainstHeaders && null != rowHeaders && nextLine.length != rowHeaders.length){
                        log.warn("The row length does not match the header length. Not adding this line");
                        return;
                    }

                    for(int colIndex = 0; colIndex < nextLine.length; colIndex++) {
                        final long sequence = ringBuffer.next();
                        try {
                            ringBuffer.get(sequence).setValues(rowIndex, colIndex, nextLine[colIndex]);
                        } finally {
                            ringBuffer.publish(sequence);
                        }
                    }

                    rowIndex++;
                }
            } catch (IOException e) {
                log.error("Unexpected error", e);
            }
        }
    }
}
