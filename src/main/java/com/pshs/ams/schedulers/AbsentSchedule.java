package com.pshs.ams.schedulers;

import org.apache.logging.log4j.Logger;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AbsentSchedule {

	@Inject
	Logger logger;

	@Scheduled(cron = "0 0 0 * * ?")
	public void checkAbsent() {
		logger.info("Checking absent students");
		
	}
}
