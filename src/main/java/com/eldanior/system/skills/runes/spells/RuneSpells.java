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
        RuneSpellRuntime.register(new OndeRepulsion());
        RuneSpellRuntime.register(new Flammeche());
        RuneSpellRuntime.register(new MurDeFlammes());
        RuneSpellRuntime.register(new MarqueInfernale());
        RuneSpellRuntime.register(new AuraDeBraise());
        RuneSpellRuntime.register(new EclatDeGivre());
        RuneSpellRuntime.register(new LanceDeCristal());
        RuneSpellRuntime.register(new PrisonDeGlace());
        RuneSpellRuntime.register(new SentierDHiver());
        RuneSpellRuntime.register(new Decharge());
        RuneSpellRuntime.register(new ChaineDEclairs());
        RuneSpellRuntime.register(new OrbeStatique());
        RuneSpellRuntime.register(new Blizzard());
        RuneSpellRuntime.register(new DomaineElementaire());
        RuneSpellRuntime.register(new TremblementTectonique());
        RuneSpellRuntime.register(new DanseDesVents());
        RuneSpellRuntime.register(new HaloProtecteur());
        RuneSpellRuntime.register(new ArmeBenie());
        RuneSpellRuntime.register(new GlyphesFoudroyants());
        RuneSpellRuntime.register(new EgideRoyale());
        RuneSpellRuntime.register(new SoinMineur());
        RuneSpellRuntime.register(new FontaineDeVie());
        RuneSpellRuntime.register(new LanceDeLumiere());
        RuneSpellRuntime.register(new LienDAme());
        RuneSpellRuntime.register(new EspritFollet());
        RuneSpellRuntime.register(new GolemDePierre());
        RuneSpellRuntime.register(new SentinelleDeGivre());
        RuneSpellRuntime.register(new MeuteSpectrale());
        // <rune-tool:register>
    }
}
