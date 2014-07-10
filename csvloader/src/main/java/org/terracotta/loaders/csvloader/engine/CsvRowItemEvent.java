package org.terracotta.loaders.csvloader.engine;

import com.lmax.disruptor.EventFactory;

/**
 * @author Fabien Sanglier
 * 
 */
public final class CsvRowItemEvent
{
    private long rowIndex;
    private int columnIndex;
    private String value;

    public long getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(long rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValues(long rowIndex, int columnIndex, String value){
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.value = value;
    }

    public final static EventFactory<CsvRowItemEvent> EVENT_FACTORY = new EventFactory<CsvRowItemEvent>()
    {
        public CsvRowItemEvent newInstance()
        {
            return new CsvRowItemEvent();
        }
    };
}
