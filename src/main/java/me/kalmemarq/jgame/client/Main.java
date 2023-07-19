package me.kalmemarq.jgame.client;

import me.kalmemarq.jgame.common.OptionArg;
import me.kalmemarq.jgame.common.OptionArgParser;

import java.io.File;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        OptionArgParser optionArgParser = new OptionArgParser();
        OptionArg<File> gameDirArg = optionArgParser.add("gameDir", File.class).defaultsTo(new File(""));

        Client client = new Client(gameDirArg.value());
        client.run();
        client.destroy();
    }
}