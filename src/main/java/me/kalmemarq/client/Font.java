package me.kalmemarq.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import me.kalmemarq.common.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Font {
    private final Map<Integer, Glyph> glyphMap = new HashMap<>();

    public void load() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode fontObj = mapper.readTree(Util.readString(Client.class.getResourceAsStream("/font.json")));
            int advance = fontObj.has("advance") ? fontObj.get("advance").intValue() : 8;

            if (fontObj.isObject()) {
                ArrayNode chars = (ArrayNode) fontObj.get("chars");

                for (int i = 0; i < chars.size(); ++i) {
                    char[] a = chars.get(i).textValue().toCharArray();
                    int rowV = i * 8;

                    for (int j = 0; j < a.length; ++j) {
                        int colU = j * 8;
                        if (a[j] == '\u0000') continue;
                        this.glyphMap.put((int) a[j], new Glyph((colU) / 256.0f, (rowV) / 256.0f, (colU + 8) / 256.0f, (rowV + 8) / 256.0f, advance));
                    }
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void drawText(String text, int x, int y, int color) {
        if (text == null) return;
        char[] chrs = text.toCharArray();
        int xx = x;

        Renderer.color((color >> 16 & 0xFF) / 255.0f, (color >> 8 & 0xFF) / 255.0f, (color & 0xFF) / 255.0f, (color >> 24 & 0xFF) / 255.0f);

        Renderer.begin(Renderer.PrimitiveType.QUADS);
        for (int i = 0; i < chrs.length; ++i) {
            if (chrs[i] == ' ') {
                xx += 8;
                continue;
            }

            Glyph glyph = this.glyphMap.get((int) chrs[i]);
            if (glyph != null) {
                Renderer.texCoord(glyph.u0, glyph.v0);
                Renderer.vertex(xx, y, 0);
                Renderer.texCoord(glyph.u0, glyph.v1);
                Renderer.vertex(xx, y + 8, 0);
                Renderer.texCoord(glyph.u1, glyph.v1);
                Renderer.vertex(xx + 8, y + 8, 0);
                Renderer.texCoord(glyph.u1, glyph.v0);
                Renderer.vertex(xx + 8, y, 0);
                xx += glyph.advance;
            }
        }
        Renderer.end();

        Renderer.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public List<String> breakTextIntoLines(String text, int maxwidth) {
        List<String> list = new ArrayList<>();

        String[] str = text.split(" ");
        String line = str[0];

        int i = 1;
        while (true) {
            if (i >= str.length) {
                list.add(line);
                break;
            }

            if (this.textWidth(line + " " + str[i]) > maxwidth) {
                list.add(line);
                line = str[i];
            } else {
                line += " " + str[i];
            }
            ++i;
        }

        return list;
    }

    public int textWidth(String text) {
        return text.length() * 8;
    }

    static class Glyph {
        public float u0;
        public float v0;
        public float u1;
        public float v1;
        public int advance;

        public Glyph(float u0, float v0, float u1, float v1, int advance) {
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
            this.advance = advance;
        }
    }
}
