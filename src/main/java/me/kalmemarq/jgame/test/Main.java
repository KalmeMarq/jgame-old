package me.kalmemarq.jgame.test;

import me.kalmemarq.jgame.common.optionarg.OptionArg;
import me.kalmemarq.jgame.common.optionarg.OptionArgParser;

public class Main {
    public static void main(String[] args) {
        String[] argv = { "--width=200"};

        OptionArgParser optionArgParser = new OptionArgParser();
        OptionArg<Integer> widthArg = optionArgParser.add("width", Integer.class);
        optionArgParser.parseArgs(argv);
        
        System.out.println(widthArg.has());
        System.out.println(widthArg.value());
        System.out.println(widthArg.defaultValue());
    }
}
