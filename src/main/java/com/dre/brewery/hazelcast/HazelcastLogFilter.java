package com.dre.brewery.hazelcast;

import com.dre.brewery.BreweryPlugin;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public class HazelcastLogFilter extends AbstractFilter {

    private static final String PACKAGE = "com.hazelcast"; //"com.dre.brewery.dependencies.hazelcast";
    private static final BreweryPlugin plugin = BreweryPlugin.getInstance();

    public void registerFilter() {
        Logger logger = (Logger) LogManager.getRootLogger();
        logger.addFilter(this);
    }

    @Override
    public Result filter(LogEvent event) {
        if (event == null) {
            return Result.NEUTRAL;
        }

        return validateMessage(event.getLoggerName(), event.getLevel(), event.getMessage().getFormattedMessage(), event.getThrown());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return validateMessage(logger.getName(), level, msg.getFormattedMessage(), t);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return validateMessage(logger.getName(), level, msg, null);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return validateMessage(logger.getName(), level, msg.toString(), t);
    }


    private Result validateMessage(String loggerName, Level level, String message, Throwable throwable) {
        if (message == null || !loggerName.startsWith(PACKAGE)) {
            return Result.NEUTRAL;
        }

        boolean shouldBeOmitted = false; // Implement

        switch (level.name()) {
            case "INFO" -> {
                shouldBeOmitted = true;
                plugin.log("&5[Hazelcast] &f" + message);
            }
            case "WARN" -> {
                shouldBeOmitted = true;
                plugin.warningLog(message);
            }
            case "ERROR" -> {
                shouldBeOmitted = true;
                plugin.errorLog(message, throwable);
            }
        }


       return shouldBeOmitted ? Result.DENY : Result.NEUTRAL;
    }

}
