package me.kalmemarq.jgame.client.render;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import me.kalmemarq.jgame.client.resource.PreparationResourceReloader;
import me.kalmemarq.jgame.client.resource.ResourceManager;
import me.kalmemarq.jgame.common.StringHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Font extends PreparationResourceReloader<Map<Integer, Font.Glyph>> {
    private Map<Integer, Glyph> glyphMap = new HashMap<>();

    public void drawString(String text, int x, int y, int colour) {
        if (text == null || text.length() == 0) return;
        char[] chrs = text.toCharArray();
        int xx = x;
        Renderer.setCurrentShader(ShaderManager::getPositionTextureColorShader);
        Renderer.setShaderTexture(0, "font.png");
        Renderer.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        int r = (colour >> 16) & 0xFF;
        int g = (colour >> 8) & 0xFF;
        int b = colour & 0xFF;
        int a = (colour >> 24) & 0xFF;
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBufferBuilder();
        
        builder.begin(VertexFormat.POSITION_TEXTURE_COLOR);

        for (int i = 0; i < chrs.length; ++i) {
            if (chrs[i] == ' ') {
                xx += 8;
                continue;
            }

            Glyph glyph = this.glyphMap.get((int) chrs[i]);
            if (glyph != null) {
                builder.vertex(xx, y, 0).texture(glyph.u0, glyph.v0).colour(r, g, b, a).next();
                builder.vertex(xx, y + 8, 0).texture(glyph.u0, glyph.v1).colour(r, g, b, a).next();
                builder.vertex(xx + 8, y + 8, 0).texture(glyph.u1, glyph.v1).colour(r, g, b, a).next();
                builder.vertex(xx + 8, y, 0).texture(glyph.u1, glyph.v0).colour(r, g, b, a).next();
                xx += glyph.advance;
            }
        }
        
        tessellator.draw();
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

    @Override
    protected Map<Integer, Glyph> prepare(ResourceManager resourceManager) {
        ObjectMapper mapper = new ObjectMapper();
        Map<Integer, Glyph> map = new HashMap<>();

        try {
            JsonNode fontObj = mapper.readTree(StringHelper.readString(resourceManager.getResource("font.json").getAsInputStream()));
            int advance = fontObj.has("advance") ? fontObj.get("advance").intValue() : 8;

            if (fontObj.isObject()) {
                ArrayNode chars = (ArrayNode) fontObj.get("chars");

                for (int i = 0; i < chars.size(); ++i) {
                    char[] a = chars.get(i).textValue().toCharArray();
                    int rowV = i * 8;

                    for (int j = 0; j < a.length; ++j) {
                        int colU = j * 8;
                        if (a[j] == '\u0000') continue;
                        map.put((int) a[j], new Glyph((colU) / 256.0f, (rowV) / 256.0f, (colU + 8) / 256.0f, (rowV + 8) / 256.0f, advance));
                    }
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    protected void apply(Map<Integer, Glyph> prepared, ResourceManager resourceManager) {
        this.glyphMap = prepared;
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
