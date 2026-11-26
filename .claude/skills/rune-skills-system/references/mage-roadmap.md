# Feuille de route : runes des classes mages (validée par l'utilisateur le 2026-10-26)

Règle : une rune par classe, de la rareté de la classe ; 3 runes (Epic + Unique + Legendaire) par classe Legendaire.
Classes Divines : aucune nouvelle rune Divine, leur assigner les runes Divines existantes.
Restriction (`classes` de la fiche) : toujours `mage` — toute rune de mage s'apprend par n'importe quel mage (décision utilisateur 2026-10-26) ; la classe associée est la « rune de prédilection » (texte + `runes(...)`). Ajouter aussi la rune au champ `runes` de la classe dans `scripts/classes/classes.json` puis relancer `python3 scripts/classes/class_catalog.py` (sorts des aventuriers PNJ).
Durées : respecter `rarity-balance.md` (Rare ≤ 6 s, Epic ≤ 10 s, Unique ≤ 15 s, Legendaire ≤ 25 s) même si la liste dit plus.
Icône : un emblème dédié par rune (`rune_icons.py` + champ `emblem`), prévisualisé avant livraison.
Statut : ☐ à faire · ✔ créée (compilée + check) · ★ existante assignée.

Lot 1 (Base + Pyromancien) lot 2 (Cryomancien), lot 3 (Élémentaliste) lot 4 (Maître Élémentaire) lot 5 (Enchanteur) lot 6 (Guérisseur), lot 7 (Invocateur) lot 8 (Nécromancien) lot 9 (Liche) lot 10 (Illusionniste) et lot 11 (Sorcier) faits le 2026-10-26 ; lots 12 (Mystique), 13 (Oracle), 14 (Chronoturge), 15 (Magus), 16 (Archimage), 17 (Sage), 18 (Thaumaturge) et 19 (Alchimiste) faits le 2026-10-28 ; lots 20 (Druide), 21 (Archonte) et 22 (Mage du Vide) faits le 2026-10-29, lots 23 (Primordial), 24 (Démiurge), 25 (Démiurge Suprême), 26 (Créateur Cosmique), 27 (Architecte du Monde) et 28 (Prophète) le 2026-11-01 ; emblèmes dédiés pour les 105.
Faisceaux (chaînes, fils) : `SpellFx.spawnBeam` + `SpellFx.beam` (replacé chaque tick).
Crochets du lanceur touché : `Instance.onOwnerHurt` (réduire/annuler/riposter) et `preventOwnerDeath` (DivineDamageSystem → RuneSpellRuntime.onOwnerHurt).
Catégorie créative : `Eldanior_Runes.<Type>` (générée par rune_tool.py).
Invocations de créatures : `SummonPack` + rôles `Eldanior_Summon_*` (gabarit vanilla `Template_Summoned_Ally`), exclus des systèmes de monstres par `RuneSpellRuntime.isSummonRole`.

## Base
- ✔ Mage (C) — Onde de répulsion `Rune_OndeRepulsion` · Spatial — repousse tout à 6 blocs

## Pyromancien (Feu)
- ✔ Pyromancien (C) — Flammèche · Feu — projectile rapide, brûlure 5 s `Rune_Flammeche`
- ✔ Maître des Flammes (R) — Mur de flammes · Feu — mur 10 blocs, 6 s `Rune_MurDeFlammes`
- ✔ Infernal Mage (R) — Marque infernale `Rune_MarqueInfernale` (6 s) · Feu — la cible marquée explose à sa mort et propage la brûlure
- ✔ Brasier Vivant (R) — Aura de braise · Feu — aura 4 blocs 6 s `Rune_AuraDeBraise`, sol brûlé derrière soi

## Cryomancien (Glace)
- ✔ Cryomancien (C) — Éclat de givre · Glace — projectile ralentissant `Rune_EclatDeGivre`
- ✔ Maître du Givre (R) — Lance de cristal · Glace — perce en ligne, ×2 sur gelés `Rune_LanceDeCristal`
- ✔ Glacial Mage (R) — Prison de glace · Glace — cage de blocs de glace 1,5 s puis éclat `Rune_PrisonDeGlace`
- ✔ Seigneur du Froid (R) — Sentier d'hiver · Glace — neige sous les pas 6 s, gel après 2 s dessus `Rune_SentierDHiver`

## Élémentaliste (Foudre)
- ✔ Élémentaliste (C) — Décharge · Foudre — éclair court, étourdit 0,5 s `Rune_Decharge`
- ✔ Élémentaliste Maître (R) — Chaîne d'éclairs · Foudre — 5 cibles `Rune_ChaineDEclairs`
- ✔ Catalyseur Arcane (R) — Orbe statique · Foudre — orbe lent 6 s qui électrise à 4 blocs puis explose `Rune_OrbeStatique`
- ⚠ `Elementaliste.java` cite `seigneur_des_elements` (id inexistant)

## Maître Élémentaire
- ✔ Maître Élémentaire (E) — Blizzard · Glace — zone rayon 8, 10 s, ralentit puis gèle 2 s, neige `Rune_Blizzard`
- ✔ Seigneur des Éléments (U) — Domaine élémentaire · Feu — domaine 15 blocs 15 s, feu → glace → foudre toutes les 5 s `Rune_DomaineElementaire`
- ✔ Élémentaire Suprême (U) — Tremblement tectonique · Terre — 4 séismes en chaîne sur 20 blocs, fissures `Rune_TremblementTectonique`
- ✔ Force Naturelle (U) — Danse des vents · Air — 15 s, clic droit = bond + rafale `Rune_DanseDesVents`

## Enchanteur
- ✔ Enchanteur (C) — Halo protecteur · Lumière — -30 % dégâts 8 s soi/allié `Rune_HaloProtecteur`
- ✔ Grand Enchanteur (R) — Arme bénie · Lumière — arme du lanceur 10 s : éclat sacré + soin à chaque coup `Rune_ArmeBenie`
- ✔ Tisseur de Runes (R) — Glyphes foudroyants · Foudre — 3 glyphes-pièges 6 s, détonation finale `Rune_GlyphesFoudroyants`
- ✔ Enchanteur Royal (R) — Égide royale · Lumière — dôme 6 blocs 6 s : projectiles détruits, ennemis repoussés, alliés -40 % `Rune_EgideRoyale`

## Guérisseur
- ✔ Guérisseur (C) — Soin mineur · Lumière — 15 % + 5 × 1 % `Rune_SoinMineur`
- ✔ Grand Guérisseur (R) — Fontaine de vie · Lumière — source 6 s, 5 blocs, purification + 5 %/s `Rune_FontaineDeVie`
- ✔ Prêtre Arcane (R) — Lance de lumière · Lumière — rayon perçant, ×2 morts-vivants, soigne les alliés traversés `Rune_LanceDeLumiere`
- ✔ Gardien des Âmes (R) — Lien d'âme · Lumière — 8 s : transfert de PV vers le plus blessé, allié -20 % `Rune_LienDAme`

## Invocateur
- ✔ Invocateur (C) — Esprit follet · Lumière — esprit qui tire 5 traits `Rune_EspritFollet`
- ✔ Invocateur Suprême (R) — Golem de pierre · Terre — golem PNJ 600 PV, provocation, 20 s `Rune_GolemDePierre`
- ✔ Conjurateur Maître (R) — Sentinelle de givre · Glace — tourelle de vrais blocs de glace 12 s `Rune_SentinelleDeGivre`
- ✔ Maître d'Invocation (R) — Meute spectrale · Ténèbres — 3 loups PNJ 150 PV, 15 s `Rune_MeuteSpectrale`

## Nécromancien
- ✔ Nécromancien (C) — Marque funeste · Ténèbres — vos coups +15 % sur la cible 8 s `Rune_MarqueFuneste`
- ✔ Nécromancien Noir (R) — Puits d'âmes · Ténèbres — zone 6 s, drain → soin 50 % `Rune_PuitsDAmes`
- ✔ Seigneur des Morts (R) — Relève des morts · Ténèbres — morts en 15 s → squelettes alliés 15 s (4 max) `Rune_ReleveDesMorts`
- ✔ Invocateur d'Âmes (R) — Âmes errantes · Ténèbres — 4 âmes 12 s, frappent qui approche à 6 blocs `Rune_AmesErrantes`

## Liche
- ✔ Liche (E) — Phylactère · Ténèbres — coup mortel annulé, renaissance sur le phylactère (60 s, 30 % PV) `Rune_Phylactere`
- ✔ Arche-Liche (U) — Domaine de la mort · Ténèbres — 15 blocs 15 s, drain + soin à chaque mort `Rune_DomaineDeLaMort`
- ✔ Liche Éternelle (U) — Forme spectrale · Ténèbres — 10 s : immunité physique, -50 % le reste, pas de chute `Rune_FormeSpectrale`
- ✔ Seigneur Non-Mort (U) — Armée des tombes · Ténèbres — 8 squelettes 20 s `Rune_ArmeeDesTombes`

## Illusionniste
- ✔ Illusionniste (R) — Miroir trompeur · Lumière — 2 leurres qui attirent les monstres + invisibilité 3 s `Rune_MiroirTrompeur`
- ✔ Grand Illusionniste (E) — Galerie des glaces · Lumière — 10 reflets répètent vos coups 10 s puis éclatent `Rune_GalerieDesGlaces`
- ★ Maître du Mirage (E) — Rune_Clonage (ouverte aux mages le 2026-10-26)
- ✔ Tisseur d'Illusions (E) — Hallucination · Ténèbres — brume 8 s, les monstres s'entre-attaquent `Rune_Hallucination`

## Sorcier
- ✔ Sorcier (R) — Chaînes noires · Ténèbres — 3 ennemis liés 6 s, vos coups partagés 50 % `Rune_ChainesNoires`
- ✔ Sorcier Suprême (E) — Pacte de sang · Ténèbres — −20 % PV → +50 % dégâts et vol de vie 15 % pendant 10 s `Rune_PacteDeSang`
- ★ Mage Noir (E) — Rune_Doppelganger (ouverte aux mages le 2026-10-26)
- ✔ Envoûteur Maître (E) — Marionnette · Ténèbres — contrôle d'un monstre 10 s `Rune_Marionnette`

## Mystique
- ✔ Mystique (R) — Projection astrale · Espace — esprit guidé par le regard 6 s, clic ou fin = téléportation + onde 120 `Rune_ProjectionAstrale`
- ✔ Mystique Supérieur (E) — Ceinture d'astéroïdes · Spatial — 8 blocs du sol en orbite 10 s, clic = tir 60 + éclats `Rune_CeintureAsteroides`
- ✔ Voyant Arcane (E) — Prescience · Temps — 10 s : 3 coups esquivés (pas de côté, attaquant figé 1 s), riposte +50 % `Rune_Prescience`
- ✔ Devin Cosmique (E) — Rayon céleste · Lumière — pilier du ciel qui suit le curseur 6 s, 14/0,25 s, soigne les alliés `Rune_RayonCeleste`

## Oracle
- ✔ Oracle (E) — Distorsion · Temps — zone r 8, 10 s : ennemis ×0,4 vitesse, projectiles ennemis figés `Rune_Distorsion`
- ✔ Oracle Absolu (U) — Retour en arrière · Temps — ancre : 5 s plus tard (ou coup mortel) retour position + PV, coups rejoués 50 % `Rune_RetourEnArriere`
- ✔ Visionnaire Suprême (U) — Œil omniscient · Espace — 15 s, clic droit = échange avec l'ennemi visé (40 blocs), 150 + étourdi 1,5 s, 4 fois `Rune_OeilOmniscient`
- ✔ Prophète du Destin (U) — Sentence · Temps — 3 condamnés 8 s, puis rejouent vos dégâts cumulés (100–800) `Rune_Sentence`

## Chronoturge
- ✔ Chronoturge (E) — Bulle temporelle · Temps — dôme r 7, 10 s : alliés ×1,4 + 1 % mana/s, ennemis ×0,5 `Rune_BulleTemporelle`
- ✔ Maître du Temps (U) — Arrêt du temps · Temps — ennemis à 12 blocs figés 3 s, coups mis en réserve puis ×1,25 à la reprise `Rune_ArretDuTemps`
- ✔ Chrono-Seigneur (U) — Surrégime · Temps — 15 s : vitesse ×1,8, coups répétés à 40 %, mana +3 %/s (recharges : prédites par le client, non modifiables) `Rune_Surregime`
- ✔ Tisseur Temporel (U) — Échos du passé · Temps — 12 s : 3 rémanences suivent vos pas à 1/2/3 s et rejouent vos coups à 35 % `Rune_EchosDuPasse`

## Magus
- ✔ Magus (E) — Météore · Feu — alerte 1,2 s, météore 400/250 (r 2/5) + brûlure, cratère de lave restauré 60 s `Rune_Meteore`
- ✔ Magus Suprême (U) — Rayon arcanique · Lumière — rayon canalisé 5 s qui suit le regard, 30 blocs, 30/0,25 s `Rune_RayonArcanique`
- ✔ Archi-Magus (U) — Avatar de l'orage · Foudre — 15 s : chaque coup = éclair 60 + étourdi 0,3 s, saut 40 `Rune_AvatarOrage`
- ✔ Magus Éternel (U) — Éternité · Temps — 10 s sans passer sous 1 PV, +20 % PV à la fin si sauvé `Rune_Eternite`

## Archimage
- ✔ Archimage (R) — Salve arcanique · Lumière — 5 orbes à tête chercheuse répartis, 45 chacun `Rune_SalveArcanique`
- ✔ Grand Archimage (E) — Nuage d'orage · Foudre — nuage r 7, 10 s : éclair / 0,5 s sur un ennemi au hasard, 70 + étourdi 0,5 s `Rune_NuageOrage`
- ✔ Archimage Suprême (E) — Réflexion · Lumière — dôme mobile 5 blocs 8 s : projectiles renvoyés sur le tireur (80), 30 % des coups renvoyés `Rune_Reflexion`
- ✔ Archimage Royal (E) — Décret · Spatial — r 8 : plaqués 2 s (40) puis éjectés (150) `Rune_Decret`

## Sage
- ✔ Sage (R) — Méditation · Lumière — 4 s immobile : +5 % PV et +8 % mana/s, onde purifiante si menée à terme `Rune_Meditation`
- ✔ Grand Sage (E) — Sanctuaire mineur · Lumière — dôme 6 blocs 8 s : alliés −40 %, immunisés aux contrôles, +1 % PV/s `Rune_SanctuaireMineur`
- ✔ Érudit Arcane (E) — Savoir accéléré · Temps — 10 s : mana +5 %/s, +20 % dégâts élémentaires (recharges : non modifiables) `Rune_SavoirAccelere`
- ✔ Mentorant Suprême (E) — Enseignement · Lumière — alliés à 15 blocs : +30 % dégâts 10 s, +10 % mana `Rune_Enseignement`

## Thaumaturge
- ✔ Thaumaturge (R) — Miracle · Lumière — soin 40 %, le débordement explose (jusqu'à 180, ×2 morts-vivants) `Rune_Miracle`
- ✔ Grand Thaumaturge (E) — Pilier ascendant · Terre — colonne de basalte 6 blocs sous le lanceur et ses alliés, 5 s, effondrement 200 + étourdi 1 s `Rune_PilierAscendant`
- ✔ Maître du Prodige (E) — Inversion · Spatial — r 7, 6 s : 3 cycles lévitation 1,6 s puis plaquage 80 `Rune_Inversion`
- ✔ Thaumaturge Royal (E) — Rempart · Terre — muraille r 5 × 3 de haut, 10 s, ennemis repoussés dehors `Rune_Rempart`

## Alchimiste
- ✔ Alchimiste (R) — Sables mouvants · Terre — sable r 4, 6 s : ×0,3, 10/0,5 s, englué 1 s après 2 s `Rune_SablesMouvants`
- ✔ Grand Alchimiste (E) — Nuage toxique · Ténèbres — nuage r 6, 10 s, dérive vers le curseur, 14 poison/0,5 s `Rune_NuageToxique`
- ✔ Philosophe Mage (E) — Pierre philosophale · Lumière — alliés à 8 blocs, 10 s : +3 % PV/s, immunité poison, purification /2 s `Rune_PierrePhilosophale`
- ✔ Transmutateur Divin (E) — Pétrification · Terre — 3 ennemis statues 2 s, vos coups ×2, brisure 120 `Rune_Petrification`

## Druide
- ✔ Druide (R) — Racines · Terre — racines souterraines vers 3 ennemis : 90, immobilisés 1,5 s (épines) puis entravés 1,6 s `Rune_Racines`
- ✔ Arche Druide (E) — Bosquet · Terre — r 7, 10 s : 3 chênes (alliés +3 % PV/s), haie de ronces, ennemis 12/0,5 s + ralentis `Rune_Bosquet`
- ✔ Gardien de la Nature (E) — Esprit de la forêt · Terre — esprit-arbre (Hedera) 800 PV 25 s, racines /3 s sur 2 ennemis proches du lanceur `Rune_EspritForet`
- ✔ Druide Primordial (E) — Tempête de feuilles · Air — tornade 8 s qui suit le viseur : aspire, 16/0,5 s, éjection 60 `Rune_TempeteFeuilles`

## Archonte
- ✔ Archonte (U) — Lentille gravitationnelle · Spatial — r 8, 12 s : ennemis en orbite, vos coups +40 %, projectiles ennemis capturés, effondrement 200 + 30/projectile `Rune_LentilleGravitationnelle`
- ✔ Archonte Suprême (U) — Couronne d'autorité · Spatial — répulsion 6 blocs 15 s (40/s), projectiles détruits, 3 décrets au clic (r 12, 150) `Rune_CouronneAutorite`
- ✔ Seigneur Arcanique (U) — Loi arcanique · Espace — domaine r 15, 12 s sans sortie : renvoi au point opposé (60 + étourdi), verdict 60/fuite `Rune_LoiArcanique`
- ✔ Pilier des Mondes (U) — Pilier du monde · Terre — pilier 18 blocs (250 + projection), 3 ondes sismiques r 20 (120 + étourdi 1 s), effondrement 150 `Rune_PilierDuMonde`

## Mage du Vide
- ✔ Mage du Vide (U) — Bannissement · Espace — 5 ennemis disparaissent 6 s puis retombent (250) `Rune_Bannissement`
- ✔ Mage Void Absolu (U) — Effacement · Ténèbres — rayon 30 blocs : 25 % des PV max (150-2 000), effacés sous 10 % `Rune_Effacement`
- ✔ Void Primordial (U) — Fissure du vide · Espace — fissure 12 s : ennemis recrachés au hasard (80), bond de 12 blocs pour le lanceur `Rune_FissureVide`
- ✔ Néant Mystique (U) — Silence du néant · Ténèbres — r 10, 10 s : monstres n'attaquent plus, runes ennemies dissipées, 25/s, lanceur invisible `Rune_SilenceNeant`

## Primordial
- ✔ Primordial (U) — Éruption primordiale · Feu — 6 geysers sous les ennemis (180 + projection), mares de lave 12 s `Rune_EruptionPrimordiale`
- ✔ Primordial Ancien (U) — Sculpteur de glace · Glace — 15 s : murs (clic gauche) et rampes (clic droit), 6 max, éclatement 100 `Rune_SculpteurDeGlace`
- ✔ Force Originelle (U) — Onde originelle · Spatial — onde 25 blocs : 150, lévitation 1,5 s, plaquage 200, blocs arrachés `Rune_OndeOriginelle`
- ✔ Genèse Mage (U) — Résurrection · Lumière — 15 s : premier coup mortel des alliés annulé, relevés à 50 % + onde 150 `Rune_Resurrection`

## Classes Legendaires (Epic / Unique / Legendaire)
- ✔ Démiurge — Golems de cristal · Terre (E) `Rune_GolemsCristal` / Façonnage · Terre (U) `Rune_Faconnage` / Colosse de pierre · Terre (L, 20 s, taille x2) `Rune_ColossePierre`
- ✔ Démiurge Suprême — Matière vive · Spatial (E) `Rune_MatiereVive` / Réécriture · Espace (U) `Rune_Reecriture` / Marée gravitationnelle · Spatial (L) `Rune_MareeGravitationnelle`
- ✔ Créateur Cosmique — Pluie de comètes · Feu (E) `Rune_PluieCometes` / Naine blanche · Lumière (U) `Rune_NaineBlanche` / Supernova · Feu (L) `Rune_Supernova`
- ✔ Architecte du Monde — Plan d'architecte · Terre (E) `Rune_PlanArchitecte` / Labyrinthe · Espace (U) `Rune_Labyrinthe` / Porte des mondes · Espace (L, 25 s, ancres = Cercles d'Ancrage posés et nommés, choix si plusieurs, sinon spawn — refonte 2026-11-04) `Rune_PorteMondes`
- ✔ Prophète — Présage · Temps (E) `Rune_Presage` / Révélation · Lumière (U) `Rune_Revelation` / Prophétie · Temps (L) `Rune_Prophetie`
- ☐ Prophète Absolu — Verbe tonnant · Foudre / Décret absolu · Temps / Jugement céleste · Lumière
- ☐ Oracle Divin — Œil divin · Lumière / Fin et commencement · Temps / Supercellule · Foudre
- ☐ Voix de l'Éternel — Murmure · Ténèbres / Chant éternel · Lumière / Écho de l'éternité · Temps

## Classes Divines (runes Divines existantes à assigner, pas de nouvelle)
Avatar Arcanique, Demi-Arcane, Dieu des Arcanes, Demi-Mage.
