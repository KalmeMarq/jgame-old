package me.kalmemarq.jgame.client.main;

import me.kalmemarq.jgame.client.Client;
import me.kalmemarq.jgame.common.optionarg.OptionArg;
import me.kalmemarq.jgame.common.optionarg.OptionArgParser;
import me.kalmemarq.jgame.common.logger.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.Configuration;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        OptionArgParser optionArgParser = new OptionArgParser();
        OptionArg<Boolean> debugLWJGLArg = optionArgParser.add("debugLWJGL", Boolean.class);
        OptionArg<Boolean> debugImGuiArg = optionArgParser.add("debugImGui", Boolean.class).defaultsTo(false);
        OptionArg<File> gameDirArg = optionArgParser.add("gameDir", File.class).defaultsTo(new File(""));
        OptionArg<String> logArg = optionArgParser.add("log", String.class);
        optionArgParser.parseArgs(args);

        logArg.ifHas(Logger::setLogLevel);
        
        File gameDir = gameDirArg.value();

        if (!gameDir.exists() && !gameDir.mkdirs()) {
            throw new RuntimeException("Could not create game directory! " + gameDir.getAbsolutePath());
        }

        if (debugLWJGLArg.value()) {
            Configuration.DEBUG.set(true);
            Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
            Configuration.DEBUG_STACK.set(true);
            Configuration.DEBUG_LOADER.set(true);
            Configuration.DEBUG_STREAM.set(true);
            Logger.setLogLevel(Logger.LogLevel.DEBUG);
        }
        
        Client client = new Client(gameDir.toPath(), debugImGuiArg.value(), debugLWJGLArg.value());
//        System.setOut(new LoggerPrintStream("SystemOut", System.out, false, debugLWJGLArg.value()));
//        System.setErr(new LoggerPrintStream("SystemError", System.err, true, debugLWJGLArg.value()));
        client.run();
        client.destroy();
    }
}