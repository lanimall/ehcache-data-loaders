package org.terracotta.loaders.csvloader;

import com.lexicalscope.jewel.cli.CliFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainApp {
	static final Logger log = LoggerFactory.getLogger(MainApp.class);

	public static void main(String[] args) throws Exception {
        LauncherOptions po = CliFactory.parseArguments(LauncherOptions.class, args);

		log.info("Loading Spring ApplicationContext...");

		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(AppConfig.class);
		ctx.refresh();

		log.info("Loaded Spring ApplicationContext.");

        //now do something based on args...
        CommandController controller = (CommandController) ctx.getBean("CommandController");

        controller.getTimerRegistry().startReporting();
        controller.run(po);

        //wait extra time to make sure the reporter has time to print the last iteration
        Thread.sleep((controller.getTimerRegistry().getInterval() + 1) * 1000);

        controller.getTimerRegistry().stopReporting();

        System.exit(0);
	}
}