package org.terracotta.loaders.csvloader;

import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ResourceLoader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@ComponentScan(
		basePackages = {"org.terracotta.loaders"},
		excludeFilters = {
				@ComponentScan.Filter(pattern = {".*Mock.*"}, type = FilterType.REGEX),
				@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)})
@PropertySource("classpath:application.properties")
public class AppConfig {
	static final Logger log = LoggerFactory.getLogger(AppConfig.class);

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public ExecutorService executorService() {
		return Executors.newCachedThreadPool();
	}

	@Bean
	public MetricRegistry metricRegistry() {
		return new MetricRegistry();
	}

	@Autowired
	ResourceLoader resourceLoader;

	@Bean
	public FactoryBean<net.sf.ehcache.CacheManager> cacheManager() {
		EhCacheManagerFactoryBean bean = new EhCacheManagerFactoryBean();
		bean.setConfigLocation(resourceLoader.getResource("classpath:ehcache.xml"));
		return bean;
	}
}