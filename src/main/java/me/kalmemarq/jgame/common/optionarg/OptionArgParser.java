package me.kalmemarq.jgame.common.optionarg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptionArgParser {
    private final List<OptionArg<?>> options = new ArrayList<>();

    public <T> OptionArg<T> add(String name, Class<T> clazz) {
        return this.add(name, name, clazz);
    }

    public <T> OptionArg<T> add(String name, String alias, Class<T> clazz) {
        OptionArg<T> option = new OptionArg<T>(clazz, name, alias);
        this.options.add(option);
        return option;
    }

    public void parseArgs(String[] argv) {
        Map<String, List<String>> params = new HashMap<>();

        List<String> values = null;
        
        for (int i = 0; i < argv.length; ++i) {
            if (argv[i].charAt(0) == '-' && argv[i].charAt(1) == '-' && argv[i].length() >= 3) {
                values = new ArrayList<>();
                String name = argv[i].substring(2);
                
                if (name.contains("=")) {
                    String value = name.substring(name.indexOf("=") + 1);
                    values.add(value);
                    name = name.substring(0, name.indexOf("="));
                }
                params.put(name, values);
            } else if (argv[i].charAt(0) == '-' && argv[i].length() >= 2) {
                values = new ArrayList<>();
                String name = argv[i].substring(1);

                if (name.contains("=")) {
                    String value = name.substring(name.indexOf("=") + 1);
                    values.add(value);
                    name = name.substring(0, name.indexOf("="));
                }

                params.put(name, values);
            } else if (values != null) {
                values.add(argv[i]);
            } else {
                System.err.println("Illegal parameter usage");
                return;
            }
        }
        
        this.parseOptions(params);
    }
    
    private void parseOptions(Map<String, List<String>> params) {
        for (OptionArg<?> option : this.options) {
            List<String> values = params.get(option.name);

            if (values == null)  {
                values = params.get(option.alias);
            }

            if (values != null) {
                option.parseValues(values);
                continue;
            } else {
                option.onNotFound();
            }

            if (option.required)  {
                throw new RuntimeException("Missing '" + option.name + "' option option");
            }
        }
    }
}
