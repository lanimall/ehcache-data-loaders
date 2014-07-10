package org.terracotta.loaders.csvloader.engine;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.terracotta.loaders.csvloader.engine.publishers.EventPublisher;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * @author Fabien Sanglier
 *
 */

@Component
public class CommandExecutor<T> {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ExecutorService executorService;

	@Value("${org.terracotta.loaders.csvloader.engine.ringbuffer.size}")
	int ringBufferSize;

	public CommandExecutor() {
		super();
	}

	public void process(final EventPublisher eventPublisher, final WorkHandler<T>[] workHandlers, final EventFactory<T> eventFactory) throws IOException {
		if(null == workHandlers || workHandlers.length == 0)
			throw new IllegalArgumentException("workhandlers cannot be null");
		log.info("Executing operation with " + workHandlers.length + " WorkHandler(s).");

		// Construct the Disruptor with a SingleProducerSequencer
		Disruptor<T> disruptor = new Disruptor<T>(
                eventFactory,
				ringBufferSize,
				executorService,
				ProducerType.SINGLE, // Single producer
				new BlockingWaitStrategy() //new BusySpinWaitStrategy()
				);

		// Connect the handler
		disruptor.handleEventsWithWorkerPool(workHandlers);
		disruptor.handleExceptionsWith(new ExceptionHandler() {
			@Override
			public void handleOnStartException(Throwable ex) {
				ex.printStackTrace();
			}

			@Override
			public void handleOnShutdownException(Throwable ex) {
				ex.printStackTrace();
			}

			@Override
			public void handleEventException(Throwable ex, long sequence, Object event) {
				log.error("Exception occurred with event = " + event.toString(), ex);
				ex.printStackTrace();
			}
		});

		// Start the Disruptor, starts all threads running
		log.info("Starting Disruptor...");
		disruptor.start();

		// Get the ring buffer from the Disruptor to be used for publishing.
		RingBuffer<T> ringBuffer = disruptor.getRingBuffer();
		long time1 = System.currentTimeMillis();
		try
		{
			log.info("Waiting for all published commands to be processed.");
            eventPublisher.publishAll(ringBuffer);
        } finally {
            disruptor.shutdown();
			log.info(String.format("Done in %d ms", System.currentTimeMillis() - time1));
            log.info("Published all objects to process!");
		}
	}
}