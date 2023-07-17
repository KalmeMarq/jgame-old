package me.kalmemarq.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptionArgParser {
    private final List<OptionArg<?>> args = new ArrayList<>();

    public <T> OptionArg<T> add(String name, Class<T> clazz) {
        return this.add(name, name, clazz);
    }

    public <T> OptionArg<T> add(String name, String alias, Class<T> clazz) {
        OptionArg<T> option = new OptionArg<T>(clazz, name, alias);
        this.args.add(option);
        return option;
    }

    public void parseArgs(String[] argv) {
        Map<String, List<String>> params = new HashMap<>();

        List<String> options = null;
        for (int i = 0; i < argv.length; i++) {
            final String a = argv[i];

            if (a.charAt(0) == '-' || (a.charAt(0) == '-' && a.charAt(1) == '-')) {
                boolean isD = a.charAt(1) == '-';
                if ((isD && a.length() < 3) || a.length() < 2) {
                    System.err.println("Error at argument " + a);
                    return;
                }

                options = new ArrayList<>();

                if (a.contains("=")) {
                    String n = a.substring(isD ? 2 : 1, a.indexOf("="));
                    params.put(n, options);
                } else {
                    params.put(a.substring(isD ? 2 : 1), options);
                }
            } else if (options != null) {
                options.add(a);
            } else {
                System.err.println("Illegal parameter usage");
                return;
            }
        }

        for (OptionArg<?> op : this.args) {
            List<String> l = params.get(op.name);

            if (l == null)  {
                l = params.get(op.alias);
            }

            if (l != null) {
                op.parseValues(l);
                continue;
            } else {
                op.onNotFound();
            }

            if (op.required)  {
                throw new RuntimeException("Missing '" + op.name + "' option argument");
            }
        }
    }
}
