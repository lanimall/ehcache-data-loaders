package org.terracotta.loaders.csvloader.engine.consumers;

import com.codahale.metrics.Timer;
import com.lmax.disruptor.WorkHandler;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.loaders.csvloader.engine.CsvRowEvent;
import org.terracotta.loaders.csvloader.utils.TimerRegistry;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Fabien Sanglier
 *
 */
public final class CsvRowHandler implements WorkHandler<CsvRowEvent> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Ehcache cache;
    private final TimerRegistry registry;
    private String[] rowHeaders = null;
    private boolean validateRowDataAgainstHeaders = true;

    public CsvRowHandler(final Ehcache cache, final TimerRegistry registry) {
        this.cache = cache;
        this.registry = registry;
    }

    public String[] getRowHeaders() {
        return rowHeaders;
    }

    public void setRowHeaders(String[] rowHeaders) {
        this.rowHeaders = rowHeaders;
    }

    public boolean isValidateRowDataAgainstHeaders() {
        return validateRowDataAgainstHeaders;
    }

    public void setValidateRowDataAgainstHeaders(boolean validateRowDataAgainstHeaders) {
        this.validateRowDataAgainstHeaders = validateRowDataAgainstHeaders;
    }

    @Override
    public void onEvent(final CsvRowEvent valueEvent) throws Exception {
        Timer.Context ctx;
        Object cachedValue = null;
        final String[] rawRow = valueEvent.getRowValues();
        if (null != rawRow) {
            if (null != rowHeaders) {
                if (validateRowDataAgainstHeaders && null != rowHeaders && rawRow.length != rowHeaders.length){
                    log.warn("The row length does not match the header length. Not adding this entry");
                    return;
                }

                Pair<String, String>[] rowColumnData = new Pair[rawRow.length];
                for (int i = 0; i < rawRow.length; i++) {
                    rowColumnData[i] = new ImmutablePair<String, String>(rowHeaders[i], rawRow[i]);
                }
                cachedValue = rowColumnData;
            } else {
                cachedValue = rawRow;
            }

            ctx = registry.getGetRequestPerSecond().time();
            cache.put(new Element(valueEvent.getRowIndex(), cachedValue));
            ctx.stop();
        }
    }
}