package com.m2r.botrading.admin.util;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

public class TaskMeasure {
	
    private final static Logger LOG = Logger.getLogger(TaskMeasure.class.getSimpleName());

	public static void measure(String task, ExecuteTask execution) {
		Instant begin = Instant.now();
		execution.execute();
		Instant end = Instant.now();
		Duration duration = Duration.between(begin, end);
		LOG.info(String.format("Task %s executed at %d milliseconds", task, duration.toMillis()));
	}
	
	@FunctionalInterface
	public interface ExecuteTask {
		void execute();
	}

}
