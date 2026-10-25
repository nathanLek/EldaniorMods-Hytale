package com.eldanior.system.skills.runes.spells;

/** Sorts de rune branches sur le moteur commun. Une ligne par sort (rune_tool.py new l'ajoute). */
public final class RuneSpells {

    private RuneSpells() {}

    public static void registerAll() {
        RuneSpellRuntime.register(new Bourrasque());
        RuneSpellRuntime.register(new FujinWrath());          // Courroux de Fujin (divin, Oeil du Cyclone)
        RuneSpellRuntime.register(new PuitsGravitationnel());
        RuneSpellRuntime.register(new AutoriteGravitationnelle());
        RuneSpellRuntime.register(new Telekinesie());
        RuneSpellRuntime.register(new Clonage());
        RuneSpellRuntime.register(new Doppelganger());
        RuneSpellRuntime.register(new Gungnir.Lancer());      // Gungnir (artefact divin) : lancer guide qui revient
        RuneSpellRuntime.register(new Gungnir.OeilOdin());    // Gungnir : signature, Oeil d'Odin
        // <rune-tool:register>
    }
}
