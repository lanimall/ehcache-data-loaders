package org.terracotta.loaders.csvloader.engine;

import com.lmax.disruptor.EventFactory;

/**
 * @author Fabien Sanglier
 * 
 */
public final class CsvRowEvent
{
    private long rowIndex;
    private String[] rowValues;

    public long getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(long rowIndex) {
        this.rowIndex = rowIndex;
    }

    public String[] getRowValues() {
        return rowValues;
    }

    public void setRowValues(String[] rowValues) {
        this.rowValues = rowValues;
    }

    public void setValues(long rowIndex, String[] rowValues){
        this.rowIndex = rowIndex;
        this.rowValues = rowValues;
    }

    public final static EventFactory<CsvRowEvent> EVENT_FACTORY = new EventFactory<CsvRowEvent>()
    {
        public CsvRowEvent newInstance()
        {
            return new CsvRowEvent();
        }
    };
}
