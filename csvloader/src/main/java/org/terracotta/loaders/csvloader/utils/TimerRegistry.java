package org.terracotta.loaders.csvloader.utils;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
public class TimerRegistry {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${org.terracotta.loaders.csvloader.stats.reporter.console.enabled}")
	boolean consoleReporterEnabled = false;

	@Value(value = "${org.terracotta.loaders.csvloader.stats.reporter.slf4j.enabled}")
	boolean slf4jReporterEnabled = false;

	@Autowired
	MetricRegistry metricRegistry;

	ConsoleReporter consoleReporter;
	Slf4jReporter slf4jReporter;

    @Value(value = "${org.terracotta.loaders.csvloader.stats.reporter.interval}")
    int interval;

    public int getInterval() {
        return interval;
    }

    @PostConstruct
	public void init() {
		consoleReporter = ConsoleReporter.forRegistry(metricRegistry)
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.build();

		slf4jReporter = Slf4jReporter.forRegistry(metricRegistry)
				.outputTo(log)
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.build();
	}

	public void startReporting() {
		log.info("Starting reporters...");
		slf4jReporter.start(interval, TimeUnit.SECONDS);
		consoleReporter.start(interval, TimeUnit.SECONDS);
	}

	public void stopReporting() {
		log.info("Stopping reporters...");
		slf4jReporter.stop();
		consoleReporter.stop();
		init();
	}

	public Timer getGetRequestPerSecond() {
		return metricRegistry.timer("Get-Requests per Second");
	}

	public Timer getInsertRequestPerSecond() {
		return metricRegistry.timer("Insert-Requests per Second");
	}

	public Timer getUpdateRequestPerSecond() {
		return metricRegistry.timer("Update-Requests per Second");
	}

	public Timer getDeleteRequestPerSecond() {
		return metricRegistry.timer("Delete-Requests per Second");
	}
}
