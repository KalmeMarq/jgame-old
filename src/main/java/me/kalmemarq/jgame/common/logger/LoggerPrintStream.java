package me.kalmemarq.jgame.common.logger;

import java.io.OutputStream;
import java.io.PrintStream;

public class LoggerPrintStream extends PrintStream {
    private final Logger logger;
    private final boolean error;
    private final boolean debug;

    public LoggerPrintStream(final String name, OutputStream out, boolean error, boolean debug) {
        super(out);
        this.logger = Logger.getLogger(name);
        this.error = error;
        this.debug = debug;
    }

    @Override
    public void print(String s) {
        this.log(s);
    }

    @Override
    public void println(boolean x) {
        this.log(String.valueOf(x));
    }

    @Override
    public void println(String x) {
        this.log(x);
    }

    @Override
    public void println(Object x) {
        this.log(String.valueOf(x));
    }

    private void log(String x) {
        if (this.error) {
            if (this.debug) {
                StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                StackTraceElement stackTraceElement = stackTraceElements[Math.min(3, stackTraceElements.length)];
                this.logger.error("({}:{}): {}",  stackTraceElement.getFileName(), stackTraceElement.getLineNumber(), x);
            } else {
                this.logger.error("{}", x);
            }
        } else {
            if (this.debug) {
                StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                StackTraceElement stackTraceElement = stackTraceElements[Math.min(3, stackTraceElements.length)];
                this.logger.info("({}:{}): {}",  stackTraceElement.getFileName(), stackTraceElement.getLineNumber(), x);
            } else {
                this.logger.info("{}", x);
            }
        }
    }
}