package me.kalmemarq.jgame.common.logger;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// [HH:mm:ss:SS] [thread name/level name] (logger name) message
public class Logger {
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("HH:mm:ss:SSS");
    private static final PrintStream PRINT_STREAM = new PrintStream(System.out);
    private static final Date DATE = new Date();

    private final String name;

    private Logger(String name) {
        this.name = name;
    }

    public static Logger getLogger() {
        return new Logger(STACK_WALKER.getCallerClass().getSimpleName());
    }

    public static Logger getLogger(@NotNull Class<?> clazz) {
        return new Logger(clazz.getSimpleName());
    }

    public static Logger getLogger(String name) {
        return new Logger(name);
    }

    private String formatMessage(String message, Object... args) {
        StringBuilder sb = new StringBuilder();
        int cursor = 0;
        int argIdx = 0;
        while (cursor < message.length()) {
            if (argIdx == args.length) {
                sb.append(message.substring(cursor));
                break;
            }

            if (message.charAt(cursor) == '{') {
                if (message.charAt(cursor + 1) == '}') {
                    cursor += 2;
                    sb.append(args[argIdx]);
                    ++argIdx;
                    continue;
                } else if (message.charAt(cursor + 2) == '}') {
                    char ch = message.charAt(cursor + 1);

                    if (ch >= '0' && ch <= '9') {
                        int idx = message.charAt(cursor + 1) - '0';
                        if (idx >= 0 && idx < args.length) {
                            sb.append(args[idx]);
                            cursor += 3;
                        }
                        continue;
                    }
                }
            }

            sb.append(message.charAt(cursor));

            ++cursor;
        }
        return sb.toString();
    }

    public void info(Object message) {
        this.info(String.valueOf(message));
    }

    public void info(String message) {
        DATE.setTime(System.currentTimeMillis());
        String out = Ansi.BLUE + "[" + DATE_FORMATTER.format(DATE) + "] " + Ansi.GREEN + "[" + Thread.currentThread().getName() + "/" + "Info] " + Ansi.CYAN + "(" + this.name + ") " + Ansi.RESET + message + "\n";
        PRINT_STREAM.print(out);
    }

    public void info(String message, Object... args) {
        this.info(this.formatMessage(message, args));
    }

    public void warn(Object message) {
        this.warn(String.valueOf(message));
    }

    public void warn(String message) {
        DATE.setTime(System.currentTimeMillis());
        String out = Ansi.BLUE + "[" + DATE_FORMATTER.format(DATE) + "] " + Ansi.YELLOW + "[" + Thread.currentThread().getName() + "/" + "Warn] " + Ansi.CYAN + "(" + this.name + ") " + Ansi.RESET + message + "\n";
        PRINT_STREAM.print(out);
    }

    public void warn(String message, Object... args) {
        this.warn(this.formatMessage(message, args));
    }

    public void error(Object message) {
        this.error(String.valueOf(message));
    }

    public void error(String message) {
        DATE.setTime(System.currentTimeMillis());
        String out = Ansi.BLUE + "[" + DATE_FORMATTER.format(DATE) + "] " + Ansi.RED + "[" + Thread.currentThread().getName() + "/" + "Error] " + Ansi.CYAN + "(" + this.name + ") " + Ansi.RED + message + Ansi.RESET + "\n";
        PRINT_STREAM.print(out);
    }

    public void error(String message, Object... args) {
        this.error(this.formatMessage(message, args));
    }

    public void fatal(Object message) {
        this.fatal(String.valueOf(message));
    }

    public void fatal(String message) {
        DATE.setTime(System.currentTimeMillis());
        String out = Ansi.BLUE + "[" + DATE_FORMATTER.format(DATE) + "] " + Ansi.RED + "[" + Thread.currentThread().getName() + "/Fatal] " + Ansi.CYAN + "(" + this.name + ") " + Ansi.RED + message + Ansi.RESET + "\n";
        PRINT_STREAM.print(out);
    }

    public void fatal(String message, Object... args) {
        this.fatal(this.formatMessage(message, args));
    }

    public void debug(Object message) {
        this.debug(String.valueOf(message));
    }

    public void debug(String message) {
        DATE.setTime(System.currentTimeMillis());
        String out = Ansi.BLUE + "[" + DATE_FORMATTER.format(DATE) + "] " + Ansi.GREEN + "[" + Thread.currentThread().getName() + "/Debug] " + Ansi.CYAN + "(" + this.name + ") " + Ansi.RESET + message + "\n";
        PRINT_STREAM.print(out);
    }

    public void debug(String message, Object... args) {
        this.debug(this.formatMessage(message, args));
    }

    private static class Ansi {
        public static final String RESET = "\033[0m";
        public static final String RED = "\033[0;31m";
        public static final String GREEN = "\033[0;32m";
        public static final String YELLOW = "\033[0;33m";
        public static final String BLUE = "\033[0;34m";
        public static final String CYAN = "\033[0;36m";
    }
}
