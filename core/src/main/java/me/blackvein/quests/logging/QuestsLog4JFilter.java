package me.blackvein.quests.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public class QuestsLog4JFilter extends AbstractFilter {

    private static final long serialVersionUID = -5594073755007974514L;

    /**
     * Validates a message and returns the {@link Result} value
     * depending on whether the message contains undesirable data.
     *
     * @param message The message to filter for
     *
     * @return Result of DENY or NUETRAL
     */
    private static Result validateMessage(String message) {
        if (message == null) {
            return Result.NEUTRAL;
        }
        // Hikari tries to nag author about unavoidable logger usage
        if (message.contains("[PikaMug]")) {
            return Result.DENY;
        }
        // Paper outputs errors when dependencies for module events are missing
        return message.startsWith("Plugin Quests") ? Result.DENY : Result.NEUTRAL;
    }

    @Override
    public Result filter(LogEvent event) {
        if (event == null) {
            return Result.NEUTRAL;
        }

        return validateMessage(event.getMessage().getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return validateMessage(msg.getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return validateMessage(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        if (msg == null) {
            return Result.NEUTRAL;
        }

        return validateMessage(msg.toString());
    }
}
