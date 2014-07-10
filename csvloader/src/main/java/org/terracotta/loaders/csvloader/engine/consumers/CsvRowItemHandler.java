package org.terracotta.loaders.csvloader.engine.consumers;

import com.codahale.metrics.Timer;
import com.lmax.disruptor.WorkHandler;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.loaders.csvloader.engine.CsvRowItemEvent;
import org.terracotta.loaders.csvloader.utils.TimerRegistry;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Fabien Sanglier
 *
 */
public final class CsvRowItemHandler implements WorkHandler<CsvRowItemEvent> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Ehcache cache;
    private final TimerRegistry registry;
    private String[] rowHeaders = null;

    public CsvRowItemHandler(final Ehcache cache, final TimerRegistry registry) {
        this.cache = cache;
        this.registry = registry;
    }

    public String[] getRowHeaders() {
        return rowHeaders;
    }

    public void setRowHeaders(String[] rowHeaders) {
        this.rowHeaders = rowHeaders;
    }

    @Override
    public void onEvent(final CsvRowItemEvent valueEvent) throws Exception {
        Timer.Context ctx;
        Object cachedValue = null;

        final String rawValue = valueEvent.getValue();

        String columnName = null;
        if(null != rowHeaders){
            try {
                columnName = rowHeaders[valueEvent.getColumnIndex()];
            } catch (Exception e) {
                log.error("Could not get the column name based on index", e);
                columnName = null;
            }
        }

        ctx = registry.getGetRequestPerSecond().time();
        cache.put(
                new Element(
                        new ImmutablePair<Long, Integer>(
                                valueEvent.getRowIndex(), //rowIndex
                                valueEvent.getColumnIndex() //columnIndex
                        ),
                        new ImmutablePair<String, String>(
                                columnName, //column name (for info purpose)
                                valueEvent.getValue() //value
                        )
                )
        );
        ctx.stop();
    }
}