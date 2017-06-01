package com.jactravel.logging.extensions.demo;

import org.apache.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Log4jAppenderExtensionApplication implements CommandLineRunner {

	private Logger LOG = Logger.getLogger(Log4jAppenderExtensionApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(Log4jAppenderExtensionApplication.class, args);
	}


	@Override
	public void run(String... strings) throws Exception {
		LOG.trace("This is a trace message");
		LOG.debug("This is a debug message");
		LOG.info("This is an information message");
		LOG.warn("This is a warn message");
		LOG.error("This is an error message");
		LOG.fatal("This is a fatal message");
	}
}
