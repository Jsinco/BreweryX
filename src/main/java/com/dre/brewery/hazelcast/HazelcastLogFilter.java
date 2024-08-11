package com.dre.brewery.hazelcast;

import com.dre.brewery.BreweryPlugin;
import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.Member;
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

    private static final String[] filteredMsgs = new String[]{
            "o    o     o     o---o", // big ass hazelcast banner
            "Integrity Checker is disabled.",
            "The Jet engine is disabled.",
            "Enable DEBUG/FINE log level",
            "Diagnostics disabled.",
            "CP Subsystem is not",
            "Copyright (c)"
    };

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

        for (String filteredMsg : filteredMsgs) {
            if (message.contains(filteredMsg)) {
                return Result.DENY;
            }
        }

        if (message.contains("Members")) {
            if (BreweryPlugin.getHazelcastManager() == null) {
                return Result.DENY;
            }

            Cluster cluster = BreweryPlugin.getHazelcast().getCluster();
            plugin.log("&d[Hazelcast] Total member count&7:&d " + cluster.getMembers().size());
            for (Member member : cluster.getMembers()) {
                plugin.log("&d[Hazelcast] Member &e" + member.getAddress() + " &d- &6" + member.getUuid() + (member.localMember() ? " &a<-- this cluster" : ""));
            }
            return Result.DENY;
        } else if (message.contains("[5.1.1]")) {
            message = message.substring(message.indexOf("[5.1.1]") + 8);
        }



        switch (level.name()) {
            case "INFO" -> plugin.log("&d[Hazelcast] " + message);
            case "WARN" -> plugin.log("&d[Hazelcast] &eWARNING: " + message);
            case "ERROR" -> plugin.errorLog(message, throwable);
        }


       return Result.DENY;
    }

}
