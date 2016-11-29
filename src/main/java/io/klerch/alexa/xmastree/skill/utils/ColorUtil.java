package io.klerch.alexa.xmastree.skill.utils;

import io.klerch.alexa.tellask.util.resource.ResourceUtteranceReader;
import io.klerch.alexa.tellask.util.resource.YamlReader;

import java.awt.*;
import java.util.Optional;

public class ColorUtil {
    private final String locale;
    private final YamlReader yamlReader;

    public ColorUtil(final String locale) {
        this.locale = locale;
        final ResourceUtteranceReader reader = new ResourceUtteranceReader("/out", "/colors.yml");
        this.yamlReader = new YamlReader(reader, locale);
    }

    public Optional<Color> getColor(final String color) {
        return this.yamlReader.getRandomUtterance(color.replace(" ", "_")).map(colorCode -> {
            final String[] colorCodes = colorCode.split("_");
            return new Color(Integer.valueOf(colorCodes[0]), Integer.valueOf(colorCodes[1]), Integer.valueOf(colorCodes[2]));
        });
    }

    public Optional<String> getColorName(final Color rgb) {
        return this.yamlReader.getRandomUtterance(String.format("%1s_%2s_%3s", rgb.getRed(), rgb.getGreen(), rgb.getBlue()))
                .map(color -> color.replace("_", " "));
    }

    public String getLocale() {
        return locale;
    }
}
