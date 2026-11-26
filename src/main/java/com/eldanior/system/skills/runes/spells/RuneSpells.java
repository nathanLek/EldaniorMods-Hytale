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
        RuneSpellRuntime.register(new Excalibur.JugementAvalon());   // Excalibur (artefact divin) : signature, Jugement d'Avalon
        RuneSpellRuntime.register(new Ryukiri.TrancheMontagne());    // Ryukiri (artefact divin des Runkandel) : signature, Tranche-Montagne
        RuneSpellRuntime.register(new Ryukiri.TrancheMontagneEveil()); // Ryukiri : charge du Tranche-Montagne
        RuneSpellRuntime.register(new Tenryumaru.Degaine());          // Tenryumaru (relique du Clan Luo) : la lame passe au clair
        RuneSpellRuntime.register(new Tenryumaru.Rengaine());          // Tenryumaru : clic droit bref, la lame retourne au fourreau
        RuneSpellRuntime.register(new Tenryumaru.FlashCeleste());     // Tenryumaru : attaque maintenue, Flash Celeste
        RuneSpellRuntime.register(new Tenryumaru.DixSerments());
        RuneSpellRuntime.register(new Tenryumaru.LuoIai());           // katanas du Clan Luo : attaque maintenue (iai)
        RuneSpellRuntime.register(new Tenryumaru.LuoIaigiri());       // katanas du Clan Luo : signature (Iaigiri)
        RuneSpellRuntime.register(new LuoPassives.Pas());             // combo Luo : pas en avant du 4e coup
        RuneSpellRuntime.register(new LuoPassives.FinCombo());        // fin de combo (Aonami, Shirasagi)
        RuneSpellRuntime.register(new LuoPassives.DegaineMouvement()); // au degaine (Hien, Tsukikage)      // Tenryumaru : signature, les Dix Serments (dix clones)
        Tenryumaru.AirBlade.all().forEach(RuneSpellRuntime::register); // Tenryumaru : lames d'or de chaque coup d'epee
        // Armes divines : une signature par famille d'arme (ArmesDivines)
        RuneSpellRuntime.register(new ArmesDivines.OeilDeRa());
        RuneSpellRuntime.register(new ArmesDivines.PhiltreDeCirce());
        RuneSpellRuntime.register(new ArmesDivines.RegardDeMeduse());
        RuneSpellRuntime.register(new ArmesDivines.BrecheDeRoland());
        RuneSpellRuntime.register(new ArmesDivines.HiverDeSkadi());
        RuneSpellRuntime.register(new ArmesDivines.ChargeDuMinotaure());
        RuneSpellRuntime.register(new ArmesDivines.MoissonDesAmes());
        RuneSpellRuntime.register(new ArmesDivines.ColereDeThor());
        RuneSpellRuntime.register(new ArmesDivines.EcraseurDeMilliers());
        RuneSpellRuntime.register(new ArmesDivines.PluieLunaire());
        RuneSpellRuntime.register(new ArmesDivines.BatonCeleste());
        RuneSpellRuntime.register(new ArmesDivines.CoupDHeracles());
        RuneSpellRuntime.register(new ArmesDivines.ChaudronDuDagda());
        RuneSpellRuntime.register(new ArmesDivines.VoileDeLOmbre());
        RuneSpellRuntime.register(new ArmesDivines.CroissantDuDragon());
        RuneSpellRuntime.register(new ArmesDivines.MjollnirLancer());
        RuneSpellRuntime.register(new ArmesDivines.GuandaoLancer());
        RuneSpellRuntime.register(new ArmesDivines.RuyiExtension());
        RuneSpellRuntime.register(new ArmesDivines.FlecheDuSoleil());
        RuneSpellRuntime.register(new LamesJumelles.CroixJumelle());   // Lames Jumelles (doubles katanas) : attaque maintenue, croix d'energie
        RuneSpellRuntime.register(new LamesJumelles.EclipseJumelle()); // Lames Jumelles : signature, croix geante qui explose
        RuneSpellRuntime.register(new AiglesDuDesert.Balle("AiglesBalleDroite", 1, false));   // Aigles du Desert : tir du pistolet droit
        RuneSpellRuntime.register(new AiglesDuDesert.Balle("AiglesBalleGauche", -1, false));  // Aigles du Desert : tir du pistolet gauche
        RuneSpellRuntime.register(new AiglesDuDesert.Balle("AiglesRafaleDroite", 1, true));   // Aigles du Desert : signature, rafale
        RuneSpellRuntime.register(new AiglesDuDesert.Balle("AiglesRafaleGauche", -1, true));
        RuneSpellRuntime.register(new AiglesDuDesert.Jugement());                              // Aigles du Desert : signature, balle de jugement
        RuneSpellRuntime.register(new AiglesDuDesert.DoubleTir());                             // Aigles du Desert : 4e temps de la cadence
        RuneSpellRuntime.register(new AiglesDuDesert.Perforante());                            // Aigles du Desert : attaque maintenue
        RuneSpellRuntime.register(new AiglesDuDesert.BoutPortant());                           // Aigles du Desert : fin du gun-kata
        RuneSpellRuntime.register(Mousquet.Tir.normal());                                      // Mousquet : balle (lancee par Eldanior_MousquetTir)
        RuneSpellRuntime.register(Mousquet.Tir.concentration());                               // Mousquet : signature Concentration
        RuneSpellRuntime.register(new Mousquet.SarsTir());                                     // SARS (Black Bess transformee)
        RuneSpellRuntime.register(new Mousquet.Obus());                                        // Canon de char (Blue Tiger transforme)
        RuneSpellRuntime.register(new Mousquet.Visee());
        RuneSpellRuntime.register(new Mousquet.Rechargement());                               // SARS : chargeur vide au clic                                       // visee : entites lointaines visibles
        RuneSpellRuntime.register(new ArmeTransfo.Veille());                                   // fin des transformations (5 min)
        RuneSpellRuntime.register(new ArmeTransfo.Baionnette());                               // baionnette fixee le temps de l'estoc
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
        RuneSpellRuntime.register(new MarqueFuneste());
        RuneSpellRuntime.register(new PuitsDAmes());
        RuneSpellRuntime.register(new ReleveDesMorts());
        RuneSpellRuntime.register(new AmesErrantes());
        RuneSpellRuntime.register(new Phylactere());
        RuneSpellRuntime.register(new DomaineDeLaMort());
        RuneSpellRuntime.register(new FormeSpectrale());
        RuneSpellRuntime.register(new ArmeeDesTombes());
        RuneSpellRuntime.register(new MiroirTrompeur());
        RuneSpellRuntime.register(new GalerieDesGlaces());
        RuneSpellRuntime.register(new Hallucination());
        RuneSpellRuntime.register(new ChainesNoires());
        RuneSpellRuntime.register(new PacteDeSang());
        RuneSpellRuntime.register(new Marionnette());
        RuneSpellRuntime.register(new Ruee());
        RuneSpellRuntime.register(new VoileDeFumee());
        RuneSpellRuntime.register(new PluieDeFleches());
        RuneSpellRuntime.register(new LancerDePieces());
        RuneSpellRuntime.register(new ProjectionAstrale());
        RuneSpellRuntime.register(new CeintureAsteroides());
        RuneSpellRuntime.register(new Prescience());
        RuneSpellRuntime.register(new RayonCeleste());
        RuneSpellRuntime.register(new Distorsion());
        RuneSpellRuntime.register(new RetourEnArriere());
        RuneSpellRuntime.register(new OeilOmniscient());
        RuneSpellRuntime.register(new Sentence());
        RuneSpellRuntime.register(new BulleTemporelle());
        RuneSpellRuntime.register(new ArretDuTemps());
        RuneSpellRuntime.register(new Surregime());
        RuneSpellRuntime.register(new EchosDuPasse());
        RuneSpellRuntime.register(new Meteore());
        RuneSpellRuntime.register(new RayonArcanique());
        RuneSpellRuntime.register(new AvatarOrage());
        RuneSpellRuntime.register(new Eternite());
        RuneSpellRuntime.register(new SalveArcanique());
        RuneSpellRuntime.register(new NuageOrage());
        RuneSpellRuntime.register(new Reflexion());
        RuneSpellRuntime.register(new Decret());
        RuneSpellRuntime.register(new Meditation());
        RuneSpellRuntime.register(new SanctuaireMineur());
        RuneSpellRuntime.register(new SavoirAccelere());
        RuneSpellRuntime.register(new Enseignement());
        RuneSpellRuntime.register(new Miracle());
        RuneSpellRuntime.register(new PilierAscendant());
        RuneSpellRuntime.register(new Inversion());
        RuneSpellRuntime.register(new Rempart());
        RuneSpellRuntime.register(new SablesMouvants());
        RuneSpellRuntime.register(new NuageToxique());
        RuneSpellRuntime.register(new PierrePhilosophale());
        RuneSpellRuntime.register(new Petrification());
        RuneSpellRuntime.register(new Racines());
        RuneSpellRuntime.register(new Bosquet());
        RuneSpellRuntime.register(new EspritForet());
        RuneSpellRuntime.register(new TempeteFeuilles());
        RuneSpellRuntime.register(new LentilleGravitationnelle());
        RuneSpellRuntime.register(new CouronneAutorite());
        RuneSpellRuntime.register(new LoiArcanique());
        RuneSpellRuntime.register(new PilierDuMonde());
        RuneSpellRuntime.register(new Bannissement());
        RuneSpellRuntime.register(new Effacement());
        RuneSpellRuntime.register(new FissureVide());
        RuneSpellRuntime.register(new SilenceNeant());
        RuneSpellRuntime.register(new EruptionPrimordiale());
        RuneSpellRuntime.register(new SculpteurDeGlace());
        RuneSpellRuntime.register(new OndeOriginelle());
        RuneSpellRuntime.register(new Resurrection());
        RuneSpellRuntime.register(new GolemsCristal());
        RuneSpellRuntime.register(new Faconnage());
        RuneSpellRuntime.register(new ColossePierre());
        RuneSpellRuntime.register(new MatiereVive());
        RuneSpellRuntime.register(new Reecriture());
        RuneSpellRuntime.register(new MareeGravitationnelle());
        RuneSpellRuntime.register(new PluieCometes());
        RuneSpellRuntime.register(new NaineBlanche());
        RuneSpellRuntime.register(new Supernova());
        RuneSpellRuntime.register(new PlanArchitecte());
        RuneSpellRuntime.register(new Labyrinthe());
        RuneSpellRuntime.register(new PorteMondes());
        RuneSpellRuntime.register(new Presage());
        RuneSpellRuntime.register(new Revelation());
        RuneSpellRuntime.register(new Prophetie());
        RuneSpellRuntime.register(new KiEpee());
        RuneSpellRuntime.register(new MeditationMartiale());
        RuneSpellRuntime.register(new PaumeKi());
        RuneSpellRuntime.register(new FrappeMeridiens());
        RuneSpellRuntime.register(new GardeFleuve());
        RuneSpellRuntime.register(new CroissantKi());
        RuneSpellRuntime.register(new DomaineEpee());
        RuneSpellRuntime.register(new FleurPrunier());
        RuneSpellRuntime.register(new TransformationHwakyung());
        RuneSpellRuntime.register(new UniteCielTerre());
        RuneSpellRuntime.register(new LuoManuels.Apprendre());                 // manuels secrets du Clan Luo
        RuneSpellRuntime.register(new MagicManuals.Apprendre());               // manuels de magie (2026-11-24)
        RuneSpellRuntime.register(new com.eldanior.system.luo.VoieMartialePage.Parchemin());   // Rouleau de la Voie Martiale
        RuneSpellRuntime.register(new LuoPassives.Rafale());
        RuneSpellRuntime.register(new Percee());                                // Percee des katanas : garde + clic (2026-11-11)
        RuneSpellRuntime.register(new PerceePosture("LuoPerceePosture", true));      // posture du maintien du Bokken (2026-11-12)
        RuneSpellRuntime.register(new PerceePosture("LuoPerceePostureFourreau", false));
        RuneSpellRuntime.register(new LuoPassives.Endurance());                 // 5 % d'endurance par coup du Bokken (2026-11-11)
        KiEpee.Coup.all().forEach(RuneSpellRuntime::register);                  // kata Luo : lame de Ki inclinee comme le coup (Saint et +)
        RuneSpellRuntime.register(new KiEpee.Charge());
        RuneSpellRuntime.register(new SenseiDuel());                            // duel d'entrainement du Sensei (2026-11-10)                         // coup charge : lame de Ki (Maitre et +)                          // kata Luo : lame de Ki a chaque coup (Saint et +)                   // Bokken : rafale du combo
        RuneSpellRuntime.register(new RenforcementCorps());
        RuneSpellRuntime.register(new PasLeger());
        RuneSpellRuntime.register(new SensMartial());
        RuneSpellRuntime.register(new ProjectionEpeeAura());
        RuneSpellRuntime.register(new VolEpeeKi());
        RuneSpellRuntime.register(new IncarnationAme());
        RuneSpellRuntime.register(new DomaineDivin());
        RuneSpellRuntime.register(new ExecutionCeleste());
        RuneSpellRuntime.register(new TransformationArme());
        RuneSpellRuntime.register(new ArkiesDomini());
        RuneSpellRuntime.register(new SansVisage());
        RuneSpellRuntime.register(new SentenceSilencieuse());
        RuneSpellRuntime.register(new BouleDeFeu());
        // <rune-tool:register>
    }
}
