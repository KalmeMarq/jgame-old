package me.kalmemarq.jgame.client;

import me.kalmemarq.jgame.common.OptionArg;
import me.kalmemarq.jgame.common.OptionArgParser;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        OptionArgParser optionArgParser = new OptionArgParser();
        OptionArg<File> gameDirArg = optionArgParser.add("gameDir", File.class).defaultsTo(new File(""));
        optionArgParser.parseArgs(args);

        File gameDir = gameDirArg.value();

        if (!gameDir.exists() && !gameDir.mkdirs()) {
            throw new RuntimeException("Could not create game directory! " + gameDir.getAbsolutePath());
        }

        Client client = new Client(gameDirArg.value());
        client.run();
        client.destroy();
    }
}