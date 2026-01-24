package com.eldanior.system.utils;

import com.hypixel.hytale.protocol.MaybeBool;
import com.hypixel.hytale.server.core.Message;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList; // Ajout
import java.util.Deque;
import java.util.HashMap;
import java.util.List; // Ajout
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TinyMsg {
    private static final Pattern TAG_PATTERN = Pattern.compile("<(/?)([a-zA-Z0-9_]+)(?::([^>]+))?>");
    private static final Map<String, Color> NAMED_COLORS = new HashMap<>();

    public static Message parse(String text) {
        if (text == null) return Message.raw("");
        if (text.isEmpty()) return Message.raw("");

        // On utilise une liste pour stocker les morceaux à plat (plus propre pour les Titres)
        List<Message> components = new ArrayList<>();

        Deque<StyleState> stateStack = new ArrayDeque<>();
        stateStack.push(new StyleState());

        Matcher matcher = TAG_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            // 1. Ajouter le texte AVANT la balise
            if (matcher.start() > lastEnd) {
                String content = text.substring(lastEnd, matcher.start());
                assert stateStack.peek() != null;
                components.add(createStyledMessage(content, stateStack.peek()));
            }

            // 2. Traiter la balise
            boolean isClosing = matcher.group(1).equals("/");
            String tagName = matcher.group(2).toLowerCase();
            String tagArg = matcher.group(3);

            if (isClosing) {
                if (stateStack.size() > 1) stateStack.pop();
            } else {
                StyleState currentState = stateStack.peek();
                StyleState newState = currentState;

                if (tagName.equals("color") && tagArg != null) {
                    Color color = parseColor(tagArg);
                    if (color != null) newState = currentState.withColor(color);
                } else if (tagName.equals("b")) {
                    newState = currentState.withBold(true);
                } else if (tagName.equals("i")) {
                    newState = currentState.withItalic(true);
                } else if (tagName.equals("u")) {
                    newState = currentState.withUnderlined(true);
                } else if (tagName.equals("reset")) {
                    newState = new StyleState();
                }
                stateStack.push(newState);
            }
            lastEnd = matcher.end();
        }

        // 3. Ajouter le reste du texte APRÈS la dernière balise
        if (lastEnd < text.length()) {
            String content = text.substring(lastEnd);
            assert stateStack.peek() != null;
            components.add(createStyledMessage(content, stateStack.peek()));
        }

        // 4. Construction finale optimisée
        if (components.isEmpty()) return Message.raw("");
        if (components.size() == 1) return components.getFirst(); // Si un seul morceau, on le renvoie direct

        // Si plusieurs morceaux, on les joint proprement
        return Message.join(components.toArray(new Message[0]));
    }

    private static Message createStyledMessage(String content, StyleState state) {
        Message msg = Message.raw(content);
        if (state.color != null) msg = msg.color(state.color);
        if (state.bold) msg.getFormattedMessage().bold = MaybeBool.True;
        if (state.italic) msg.getFormattedMessage().italic = MaybeBool.True;
        if (state.underlined) msg.getFormattedMessage().underlined = MaybeBool.True;
        return msg;
    }

    private static Color parseColor(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) return null;
        Color namedColor = NAMED_COLORS.get(colorStr.toLowerCase());
        if (namedColor != null) return namedColor;
        if (colorStr.startsWith("#")) {
            try {
                String hex = colorStr.substring(1);
                if (hex.length() == 6) {
                    return new Color(
                            Integer.parseInt(hex.substring(0, 2), 16),
                            Integer.parseInt(hex.substring(2, 4), 16),
                            Integer.parseInt(hex.substring(4, 6), 16)
                    );
                }
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    static {
        NAMED_COLORS.put("black", new Color(0, 0, 0));
        NAMED_COLORS.put("dark_blue", new Color(0, 0, 170));
        NAMED_COLORS.put("dark_green", new Color(0, 170, 0));
        NAMED_COLORS.put("dark_aqua", new Color(0, 170, 170));
        NAMED_COLORS.put("dark_red", new Color(170, 0, 0));
        NAMED_COLORS.put("dark_purple", new Color(170, 0, 170));
        NAMED_COLORS.put("gold", new Color(255, 170, 0));
        NAMED_COLORS.put("gray", new Color(170, 170, 170));
        NAMED_COLORS.put("dark_gray", new Color(85, 85, 85));
        NAMED_COLORS.put("blue", new Color(85, 85, 255));
        NAMED_COLORS.put("green", new Color(85, 255, 85));
        NAMED_COLORS.put("aqua", new Color(85, 255, 255));
        NAMED_COLORS.put("red", new Color(255, 85, 85));
        NAMED_COLORS.put("light_purple", new Color(255, 85, 255));
        NAMED_COLORS.put("yellow", new Color(255, 255, 85));
        NAMED_COLORS.put("white", new Color(255, 255, 255));
        NAMED_COLORS.put("lime", new Color(85, 255, 85));
    }

    private static class StyleState {
        final Color color;
        final boolean bold;
        final boolean italic;
        final boolean underlined;
        final boolean mono;

        StyleState() { this(null, false, false, false, false); }
        StyleState(Color color, boolean bold, boolean italic, boolean underlined, boolean mono) {
            this.color = color;
            this.bold = bold;
            this.italic = italic;
            this.underlined = underlined;
            this.mono = mono;
        }

        StyleState withColor(Color color) { return new StyleState(color, this.bold, this.italic, this.underlined, this.mono); }
        StyleState withBold(boolean bold) { return new StyleState(this.color, bold, this.italic, this.underlined, this.mono); }
        StyleState withItalic(boolean italic) { return new StyleState(this.color, this.bold, italic, this.underlined, this.mono); }
        StyleState withUnderlined(boolean underlined) { return new StyleState(this.color, this.bold, this.italic, underlined, this.mono); }
    }
}