package com.eldanior.system.quest.dialogue;

/**
 * Une page de dialogue PNJ.
 * Chaque page a un texte narratif et optionnellement des conditions pour continuer.
 */
public class DialoguePage {

    private final String speakerName;   // Nom du PNJ qui parle
    private final String text;          // Texte narratif
    private final String imageHint;     // Description visuelle (optionnel)

    public DialoguePage(String speakerName, String text) {
        this(speakerName, text, null);
    }

    public DialoguePage(String speakerName, String text, String imageHint) {
        this.speakerName = speakerName;
        this.text = text;
        this.imageHint = imageHint;
    }

    public String getSpeakerName() { return speakerName; }
    public String getText() { return text; }
    public String getImageHint() { return imageHint; }
}
