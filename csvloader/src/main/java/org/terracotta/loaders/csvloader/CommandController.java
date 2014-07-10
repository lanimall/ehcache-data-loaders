package org.terracotta.loaders.csvloader;

import au.com.bytecode.opencsv.CSVReader;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.WorkHandler;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.terracotta.loaders.csvloader.engine.CommandExecutor;
import org.terracotta.loaders.csvloader.engine.CsvRowEvent;
import org.terracotta.loaders.csvloader.engine.CsvRowItemEvent;
import org.terracotta.loaders.csvloader.engine.consumers.CsvRowHandler;
import org.terracotta.loaders.csvloader.engine.consumers.CsvRowItemHandler;
import org.terracotta.loaders.csvloader.engine.publishers.CsvRowItemPublisher;
import org.terracotta.loaders.csvloader.engine.publishers.CsvRowPublisher;
import org.terracotta.loaders.csvloader.engine.publishers.EventPublisher;
import org.terracotta.loaders.csvloader.utils.TimerRegistry;

import java.io.FileReader;
import java.io.IOException;

/**
 * @author Fabien Sanglier
 *
 */

@Component("CommandController")
@PropertySource("classpath:application.properties")
public class CommandController {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	protected TimerRegistry timerRegistry;

	@Autowired
	protected CommandExecutor commandExecutor;

	@Autowired
	protected CacheManager cacheManager;

	@Value("${org.terracotta.loaders.csvloader.engine.workhandlers.size}")
	protected int handlerCount;

    public TimerRegistry getTimerRegistry() {
        return timerRegistry;
    }

    public void run(LauncherOptions po){
		log.info("Running '" + po.toString() + "'");

		//handlers
		WorkHandler[] workHandlers = new WorkHandler[handlerCount];

        Ehcache cache = null;
        if(cacheManager.cacheExists(po.getCacheName())){
            cache = cacheManager.getCache(po.getCacheName());

            if(po.isBulkLoadEnabled()) cache.setNodeBulkLoadEnabled(true);

            if(po.isReplaceAll()){
                cache.removeAll();
            }
        } else {
            throw new IllegalArgumentException("The cache does not exist.");
        }

        CSVReader reader = null;
        String[] headers = null;
        try {
            reader = new CSVReader(new FileReader(po.getFilePath()), po.getCsvDelimiter(),'"','\\',po.getCsvSkipLines(),false,false);
            if(po.isFirstLineHeaderLine()){
                headers = reader.readNext();
            }

            EventPublisher publisher = null;
            EventFactory eventFactory = null;
            switch(po.getDataStorageType()){
                case items:
                    publisher = new CsvRowItemPublisher(reader);
                    eventFactory = CsvRowItemEvent.EVENT_FACTORY;
                    for(int i = 0; i < handlerCount; i++){
                        CsvRowItemHandler handler = new CsvRowItemHandler(cache, timerRegistry);
                        handler.setRowHeaders(headers);

                        workHandlers[i] = handler;
                    }
                    break;
                case rows:
                    publisher = new CsvRowPublisher(reader);
                    eventFactory = CsvRowEvent.EVENT_FACTORY;
                    for(int i = 0; i < handlerCount; i++){
                        CsvRowHandler handler = new CsvRowHandler(cache, timerRegistry);
                        handler.setRowHeaders(headers);

                        workHandlers[i] = handler;
                    }
                    break;
            }

            commandExecutor.process(publisher, workHandlers, eventFactory);
        } catch (IOException e) {
            log.error("Exception occurred with file reading operation", e);
        } finally {
            if(null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Exception occurred with file closing operation", e);
                }
            }
        }
	}
}
