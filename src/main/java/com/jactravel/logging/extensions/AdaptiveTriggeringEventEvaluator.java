package com.jactravel.logging.extensions;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;

public class AdaptiveTriggeringEventEvaluator implements TriggeringEventEvaluator {

    public AdaptiveTriggeringEventEvaluator() {
    }

    public boolean isTriggeringEvent(LoggingEvent event) {
        return event.getLevel().equals(Level.WARN) || event.getLevel().equals(Level.ERROR) || event.getLevel().equals(Level.FATAL);
    }
}
