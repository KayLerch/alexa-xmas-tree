package io.klerch.alexa.xmastree.skill.model;

import io.klerch.alexa.state.model.AlexaScope;
import io.klerch.alexa.state.model.AlexaStateIgnore;
import io.klerch.alexa.state.model.AlexaStateModel;
import io.klerch.alexa.state.model.AlexaStateSave;
import io.klerch.alexa.tellask.schema.annotation.AlexaSlotSave;
import io.klerch.alexa.xmastree.skill.utils.ColorUtil;

import java.awt.*;

@AlexaStateSave(Scope = AlexaScope.APPLICATION)
public class TreeState extends AlexaStateModel {
    public enum MODE {
        ON, OFF, SHOW, STOP, COLOR
    }

    private Integer r;
    private Integer g;
    private Integer b;
    private String mode;
    @AlexaStateIgnore
    private Color rgb;
    @AlexaSlotSave(slotName = "color")
    private String color;
    @AlexaStateIgnore
    private final ColorUtil colorUtil;

    public TreeState() {
        this("de-DE");
    }

    public TreeState(final String locale) {
        this(locale, "de-DE".equals(locale) ? "weiss" : "white");
    }

    public TreeState(final String locale, final MODE mode) {
        this(locale);
        setState(mode);
    }

    public TreeState(final String locale, final String color) {
        this.colorUtil = new ColorUtil(locale);
        this.rgb = colorUtil.getColor(color).orElse(null);
        if (this.rgb != null) {
            this.r = rgb.getRed();
            this.b = rgb.getBlue();
            this.g = rgb.getGreen();
            this.color = color;
        }
        setState(MODE.COLOR);
    }

    public Color getRgb() {
        if (rgb == null) {
            rgb = new Color(r, g, b);
        }
        return rgb;
    }

    public String getColor() {
        return color;
    }

    public void setState(final MODE mode) {
        this.mode = mode.toString();
    }
}
