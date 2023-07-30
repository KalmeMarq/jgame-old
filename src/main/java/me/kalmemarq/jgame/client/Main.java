package me.kalmemarq.jgame.client;

import me.kalmemarq.jgame.common.optionarg.OptionArg;
import me.kalmemarq.jgame.common.optionarg.OptionArgParser;
import me.kalmemarq.jgame.common.logger.Logger;
import me.kalmemarq.jgame.common.logger.LoggerPrintStream;
import org.lwjgl.system.Configuration;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        OptionArgParser optionArgParser = new OptionArgParser();
        OptionArg<Boolean> debugLWJGLArg = optionArgParser.add("debugLWJGL", Boolean.class);
        OptionArg<File> gameDirArg = optionArgParser.add("gameDir", File.class).defaultsTo(new File(""));
        optionArgParser.parseArgs(args);

        File gameDir = gameDirArg.value();

        if (!gameDir.exists() && !gameDir.mkdirs()) {
            throw new RuntimeException("Could not create game directory! " + gameDir.getAbsolutePath());
        }
        
        if (debugLWJGLArg.value()) {
            Configuration.DEBUG.set(true);
            Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
            Configuration.DEBUG_STACK.set(true);
            Logger.setLogLevel(Logger.LogLevel.DEBUG);
        }

        Client client = new Client(gameDirArg.value());
        System.setOut(new LoggerPrintStream("SystemOut", System.out, false, debugLWJGLArg.value()));
        System.setErr(new LoggerPrintStream("SystemError", System.err, true, debugLWJGLArg.value()));
        client.run();
        client.destroy();
    }
}