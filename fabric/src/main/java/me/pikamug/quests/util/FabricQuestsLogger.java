package me.pikamug.quests.util;

import org.slf4j.LoggerFactory;

import java.util.logging.Logger;

public class FabricQuestsLogger extends Logger {

    private final org.slf4j.Logger logger;

    public FabricQuestsLogger() {
        super("Quests", null);
        logger = LoggerFactory.getLogger("Quests");
    }

    public void info(final String format, final Object... args) {
        if (args.length > 0 && args[args.length - 1] instanceof Throwable t) {
            if (args.length == 1) {
                logger.info(format, t);
            } else {
                final Object[] prefix = new Object[args.length - 1];
                System.arraycopy(args, 0, prefix, 0, prefix.length);
                logger.info(format, prefix);
                return;
            }
            return;
        }
        logger.info(format, args);
    }

    public void warn(final String format, final Object... args) {
        if (args.length > 0 && args[args.length - 1] instanceof Throwable t) {
            if (args.length == 1) {
                logger.warn(format, t);
            } else {
                final Object[] prefix = new Object[args.length - 1];
                System.arraycopy(args, 0, prefix, 0, prefix.length);
                logger.warn(format, prefix);
                return;
            }
            return;
        }
        logger.warn(format, args);
    }

    public void error(final String format, final Object... args) {
        if (args.length > 0 && args[args.length - 1] instanceof Throwable t) {
            if (args.length == 1) {
                logger.error(format, t);
            } else {
                final Object[] prefix = new Object[args.length - 1];
                System.arraycopy(args, 0, prefix, 0, prefix.length);
                logger.error(format, prefix);
                return;
            }
            return;
        }
        logger.error(format, args);
    }

    public void debug(final String format, final Object... args) {
        if (args.length > 0 && args[args.length - 1] instanceof Throwable t) {
            if (args.length == 1) {
                logger.debug(format, t);
            } else {
                final Object[] prefix = new Object[args.length - 1];
                System.arraycopy(args, 0, prefix, 0, prefix.length);
                logger.debug(format, prefix);
                return;
            }
            return;
        }
        logger.debug(format, args);
    }
}