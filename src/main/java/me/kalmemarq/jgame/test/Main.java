package me.kalmemarq.jgame.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kalmemarq.jgame.common.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        JsonReader reader = new JsonReader();
        JsonNode node = reader.read(StringUtils.readString(Main.class.getResourceAsStream("/assets/minicraft/fonts/font.json")));
        System.out.println(node.toPrintableString());
    }
    
    static class JsonReader {
        private String content;
        private int cursor;
    
        public JsonNode read(String value) {
            this.content = value;
            this.cursor = 0;
            return this.readElement();
        }
        
        private char peek() {
            return peek(0);
        }
        
        private char peek(int offset) {
            return this.content.charAt(this.cursor + offset);
        }
        
        private JsonNode readElement() {
            this.skipWhitespace();
            
            if (this.peek() == '{') {
                return this.readObject();
            } else if (this.peek() == '[') {
                return this.readArray();
            } else if (this.peek() >= '0' && this.peek() <= '9') {
                int cur = this.cursor;
                while (this.cursor < this.content.length() && (this.peek() >= '0' && this.peek() <= '9')) {
                    ++this.cursor;
                }
                return new JsonIntNode(Integer.parseInt(this.content.substring(cur, this.cursor)));
            } else if (this.peek() == 't' && this.peek(1) == 'r' && this.peek(2) == 'u' && this.peek(3) == 'e') {
                this.cursor += 4;
                return new JsonBooleanNode(true);
            } else if (this.peek() == 'n' && this.peek(1) == 'u' && this.peek(2) == 'l' && this.peek(3) == 'l') {
                this.cursor += 4;
                return new JsonNullNode();
            } else if (this.peek() == 'f' && this.peek(1) == 'a' && this.peek(2) == 'l' && this.peek(3) == 's' && this.peek(4) == 'e') {
                this.cursor += 5;
                return new JsonBooleanNode(false);
            } else if (this.peek() == '"') {
                ++this.cursor;
                int cur = this.cursor;
                while (this.cursor < this.content.length() && this.peek() != '"') {
                    ++this.cursor;
                }
                ++this.cursor;
                return new JsonStringNode(this.content.substring(cur, this.cursor - 1));
            }
            
            return null;
        }
        
        private boolean expect(char chr) {
            if (this.peek() == chr) {
                ++this.cursor;
                return true;
            }
            return false;
        }
        
        private void skipWhitespace() {
            while (Character.isWhitespace(this.peek()) || this.peek() == '\n'|| this.peek() == '\r') {
                ++this.cursor;
            }
        }
        
        private JsonObjectNode readObject() {
            Map<String, JsonNode> map = new LinkedHashMap<>();
            this.expect('{');
            this.skipWhitespace();
            
            while (this.peek() != '}') {
                this.expect('"');
                String key;
                {
                    int cur = this.cursor;
                    while (this.cursor < this.content.length() && this.peek() != '"') {
                        ++this.cursor;
                    }
                    ++this.cursor;
                    key = this.content.substring(cur, this.cursor - 1);
                }
                this.expect('"');
                this.skipWhitespace();
                this.expect(':');
                this.skipWhitespace();
                map.put(key, this.readElement());

                this.skipWhitespace();

                if (this.expect(',')) {
                    this.skipWhitespace();
                } else if (this.peek() == '}') {
                    break;
                } else {
                    throw new RuntimeException("sss " + this.content.substring(this.cursor, Math.min(this.cursor + 10, this.content.length())));
                }
            }
            
            this.expect('}');
            return new JsonObjectNode(map);
        }

        private JsonArrayNode readArray() {
            List<JsonNode> array = new LinkedList<>();
            this.expect('[');
            this.skipWhitespace();
            
            while (this.peek() != ']') {
                array.add(this.readElement());
                this.skipWhitespace();
                
                if (this.expect(',')) {
                    this.skipWhitespace();
                } else if (this.peek() == ']') {
                    break;
                } else {
                    throw new RuntimeException("sss");
                }
            }
            
            this.expect(']');
            return new JsonArrayNode(array);
        }
    }
    
    static abstract class JsonNode {
        public String toPrintableString() {
            return "";
        }
        
        public boolean isObject() {
            return false;
        }

        public boolean isNumeric() {
            return false;
        }

        public boolean isString() {
            return false;
        }

        public boolean isNull() {
            return false;
        }

        public boolean isArray() {
            return false;
        }

        public boolean isBoolean() {
            return false;
        }
    }
    
    static class JsonObjectNode extends JsonNode {
        private final Map<String, JsonNode> map;
        
        public JsonObjectNode() {
            this.map = new LinkedHashMap<>();
        }
        
        public JsonObjectNode(Map<String, JsonNode> map) {
            this.map = map;
        }
        
        public void put(String key, int value) {
            this.map.put(key, new JsonIntNode(value));
        }

        public void put(String key, JsonNode value) {
            this.map.put(key, value);
        }

        @Override
        public boolean isObject() {
            return true;
        }

        @Override
        public String toPrintableString() {
            StringBuilder builder = new StringBuilder();
            builder.append('{');
            int i = 0;
            for (Map.Entry<String, JsonNode> entry : this.map.entrySet()) {
                builder.append('"').append(entry.getKey()).append('"').append(':').append(entry.getValue().toPrintableString());
                if (i + 1 < this.map.size()) builder.append(',');
                ++i;
            }
            builder.append('}');
            return builder.toString();
        }
    }

    static class JsonArrayNode extends JsonNode {
        private final List<JsonNode> array;
        
        public JsonArrayNode() {
            this.array = new LinkedList<>();
        }

        public JsonArrayNode(List<JsonNode> array) {
            this.array = array;
        }
        
        @Override
        public boolean isArray() {
            return true;
        }

        @Override
        public String toPrintableString() {
            StringBuilder builder = new StringBuilder();
            builder.append('[');
            int i = 0;
            for (JsonNode item : this.array) {
                builder.append(item.toPrintableString());
                if (i + 1 < this.array.size()) builder.append(',');
                ++i;
            }
            builder.append(']');
            return builder.toString();
        }
    }

    static class JsonBooleanNode extends JsonNode {
        private final boolean value;
        
        public JsonBooleanNode(boolean value) {
            this.value = value;
        }
        
        @Override
        public boolean isBoolean() {
            return true;
        }

        @Override
        public String toPrintableString() {
            return this.value ? "true" : "false";
        }
    }

    static abstract class JsonNumericNode extends JsonNode {
        @Override
        public boolean isNumeric() {
            return true;
        }
    }

    static class JsonFloatNode extends JsonNode {
        private final float value;

        public JsonFloatNode(float value) {
            this.value = value;
        }
    }

    static class JsonDoubleNode extends JsonNode {
        private final double value;

        public JsonDoubleNode(double value) {
            this.value = value;
        }
    }
    
    static class JsonIntNode extends JsonNode {
        private final int value;
        
        public JsonIntNode(int value) {
            this.value = value;
        }

        @Override
        public String toPrintableString() {
            return String.valueOf(this.value);
        }
    }

    static class JsonLongNode extends JsonNode {
        private final long value;

        public JsonLongNode(long value) {
            this.value = value;
        }
    }

    static class JsonStringNode extends JsonNode {
        private final String value;
        
        public JsonStringNode(String value) {
            this.value = value;
        }

        @Override
        public boolean isString() {
            return true;
        }

        @Override
        public String toPrintableString() {
            return '"' + this.value + '"';
        }
    }

    static class JsonNullNode extends JsonNode {
        @Override
        public boolean isNull() {
            return true;
        }

        @Override
        public String toPrintableString() {
            return "null";
        }
    }
    
    public static void main2(String[] args) {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/assets/minicraft/languages/en_us.json"), StandardCharsets.UTF_8));
        
//        Path p = Path.of(Main.class.getClassLoader().getResource("assets/minicraft/languages/en_us.json").toURI());
//        System.out.println(p);
//        JsonNode node = JacksonHelper.OBJECT_MAPPER.readTree(Files.newBufferedReader(Path.of("assets", "minicraft", "languages", "en_us.json")));
//        reader.close();
        
//        System.out.println(node.get("test").textValue());
//        System.out.println("á à");
        
    }
    
    public static void main1(String[] args) {
//        String[] argv = { "--width=200"};
//
//        OptionArgParser optionArgParser = new OptionArgParser();
//        OptionArg<Integer> widthArg = optionArgParser.add("width", Integer.class);
//        optionArgParser.parseArgs(argv);
//        
//        System.out.println(widthArg.has());
//        System.out.println(widthArg.value());
//        System.out.println(widthArg.defaultValue());
    }
}
