package org.newrelic.nrjmx;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.cli.HelpFormatter;
import org.newrelic.nrjmx.JMXFetcher.ConnectionError;

public class Application {
    private static final Logger logger = Logger.getLogger("nrjmx");

    private static void setupLogging(boolean verbose) {
        logger.setUseParentHandlers(false);
        Handler consoleHandler = new ConsoleHandler();
        logger.addHandler(consoleHandler);

        consoleHandler.setFormatter(new SimpleFormatter());

        if (verbose) {
            logger.setLevel(Level.FINE);
            consoleHandler.setLevel(Level.FINE);
        } else {
            logger.setLevel(Level.INFO);
            consoleHandler.setLevel(Level.INFO);
        }
    }

    public static void printHelp() {
        new HelpFormatter().printHelp("nrjmx", Arguments.options());
    }

    public static void main(String[] args) {
        Arguments cliArgs = null;
        try {
            cliArgs = Arguments.from(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            printHelp();
            System.exit(1);
        }

        if (cliArgs.isHelp()) {
            printHelp();
            System.exit(0);
        }

        setupLogging(cliArgs.isVerbose());

        // TODO: move all the code below to a testable class
        JMXFetcher fetcher = null;
        try {
            fetcher = new JMXFetcher(
                cliArgs.getHostname(), cliArgs.getPort(), cliArgs.getUriPath(),
                cliArgs.getUsername(), cliArgs.getPassword(),
                cliArgs.getKeyStore(), cliArgs.getKeyStorePassword(),
                cliArgs.getTrustStore(), cliArgs.getTrustStorePassword(),
                cliArgs.getIsRemoteJMX()
            );
            fetcher.processMBeans();
        } catch (ConnectionError e) {
            logger.severe(e.getMessage());
            logger.log(Level.FINE, e.getMessage(), e);
            System.exit(1);
        } catch (Exception e) {
            if (cliArgs.isDebugMode()) {
                e.printStackTrace();
            } else {
                System.out.println(e.getClass().getCanonicalName());
                logger.severe(e.getClass().getCanonicalName() + ": " + e.getMessage());
                logger.log(Level.FINE, e.getMessage(), e);
            }
            System.exit(1);
        }
    }
}
