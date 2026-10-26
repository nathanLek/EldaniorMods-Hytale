# Feuille de route : runes des classes mages (validée par l'utilisateur le 2026-10-26)

Règle : une rune par classe, de la rareté de la classe ; 3 runes (Epic + Unique + Legendaire) par classe Legendaire.
Classes Divines : aucune nouvelle rune Divine, leur assigner les runes Divines existantes.
Restriction (`classes` de la fiche) : toujours `mage` — toute rune de mage s'apprend par n'importe quel mage (décision utilisateur 2026-10-26) ; la classe associée est la « rune de prédilection » (texte + `runes(...)`). Ajouter aussi la rune au `runes(...)` de la classe (sorts des aventuriers PNJ).
Durées : respecter `rarity-balance.md` (Rare ≤ 6 s, Epic ≤ 10 s, Unique ≤ 15 s, Legendaire ≤ 25 s) même si la liste dit plus.
Icône : un emblème dédié par rune (`rune_icons.py` + champ `emblem`), prévisualisé avant livraison.
Statut : ☐ à faire · ✔ créée (compilée + check) · ★ existante assignée.

Lot 1 (Base + Pyromancien) lot 2 (Cryomancien), lot 3 (Élémentaliste) lot 4 (Maître Élémentaire) lot 5 (Enchanteur) lot 6 (Guérisseur) et lot 7 (Invocateur) faits le 2026-10-26 ; emblèmes dédiés pour les 28.
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
- ☐ Nécromancien (C) — Marque funeste · Ténèbres — +15 % dégâts subis 8 s
- ☐ Nécromancien Noir (R) — Puits d'âmes · Ténèbres — zone de drain de vie
- ☐ Seigneur des Morts (R) — Relève des morts · Ténèbres — ennemis tués en 20 s → squelettes alliés
- ☐ Invocateur d'Âmes (R) — Âmes errantes · Ténèbres — 4 âmes en orbite contre-attaquent

## Liche
- ☐ Liche (E) — Phylactère · Ténèbres — renaissance sur le phylactère (60 s, 30 % PV)
- ☐ Arche-Liche (U) — Domaine de la mort · Ténèbres — anti-soin + drain 15 blocs
- ☐ Liche Éternelle (U) — Forme spectrale · Ténèbres — intangible + vol plané 15 s
- ☐ Seigneur Non-Mort (U) — Armée des tombes · Ténèbres — 8 squelettes 30 s

## Illusionniste
- ☐ Illusionniste (R) — Miroir trompeur · Lumière — 2 leurres + invisibilité 3 s
- ☐ Grand Illusionniste (E) — Galerie des glaces · Lumière — prison de miroirs qui renvoient les coups
- ★ Maître du Mirage (E) — Rune_Clonage
- ☐ Tisseur d'Illusions (E) — Hallucination · Ténèbres — confusion, les ennemis s'entre-attaquent

## Sorcier
- ☐ Sorcier (R) — Chaînes noires · Ténèbres — 3 ennemis liés 6 s, dégâts partagés
- ☐ Sorcier Suprême (E) — Pacte de sang · Ténèbres — −20 % PV → +50 % dégâts et vol de vie 15 s
- ★ Mage Noir (E) — Rune_Doppelganger
- ☐ Envoûteur Maître (E) — Marionnette · Ténèbres — contrôle d'un monstre 15 s

## Mystique
- ☐ Mystique (R) — Projection astrale · Espace — esprit libre 8 s puis téléportation sur lui
- ☐ Mystique Supérieur (E) — Ceinture d'astéroïdes · Spatial
- ☐ Voyant Arcane (E) — Prescience · Temps — prochaine attaque esquivée
- ☐ Devin Cosmique (E) — Rayon céleste · Lumière — rayon du ciel qui suit le viseur 6 s

## Oracle
- ☐ Oracle (E) — Distorsion · Temps — zone 12 s : −60 % vitesse, projectiles figés
- ☐ Oracle Absolu (U) — Retour en arrière · Temps — position + PV d'il y a 5 s
- ☐ Visionnaire Suprême (U) — Œil omniscient · Espace — œil 20 s, échange de place avec un ennemi vu
- ☐ Prophète du Destin (U) — Sentence · Temps — dégâts accumulés ×2 après 8 s

## Chronoturge
- ☐ Chronoturge (E) — Bulle temporelle · Temps — dôme : alliés accélérés, ennemis ralentis
- ☐ Maître du Temps (U) — Arrêt du temps · Temps — tout figé 4 s, dégâts appliqués à la reprise
- ☐ Chrono-Seigneur (U) — Surrégime · Temps — vitesse ×2 et recharges réduites 15 s
- ☐ Tisseur Temporel (U) — Échos du passé · Temps — 3 rémanences rejouent 6 s d'attaques

## Magus
- ☐ Magus (E) — Météore · Feu — gros météore au curseur, cratère restauré
- ☐ Magus Suprême (U) — Rayon arcanique · Lumière — rayon canalisé 30 blocs
- ☐ Archi-Magus (U) — Avatar de l'orage · Foudre — chaque coup lance un éclair
- ☐ Magus Éternel (U) — Éternité · Temps — impossible de passer sous 1 PV pendant 10 s

## Archimage
- ☐ Archimage (R) — Salve arcanique · Lumière — 5 projectiles à tête chercheuse
- ☐ Grand Archimage (E) — Nuage d'orage · Foudre — nuage 12 s, foudre aléatoire
- ☐ Archimage Suprême (E) — Réflexion · Lumière — dôme qui renvoie les projectiles
- ☐ Archimage Royal (E) — Décret · Spatial — plaqués au sol 4 s puis éjectés

## Sage
- ☐ Sage (R) — Méditation · Lumière — 4 s immobile : mana + PV, onde purifiante
- ☐ Grand Sage (E) — Sanctuaire mineur · Lumière
- ☐ Érudit Arcane (E) — Savoir accéléré · Temps — recharges −50 % 20 s
- ☐ Mentorant Suprême (E) — Enseignement · Lumière — alliés 15 blocs +30 % dégâts

## Thaumaturge
- ☐ Thaumaturge (R) — Miracle · Lumière — gros soin ; PV pleins → explosion sacrée
- ☐ Grand Thaumaturge (E) — Pilier ascendant · Terre
- ☐ Maître du Prodige (E) — Inversion · Spatial — gravité inversée 6 s
- ☐ Thaumaturge Royal (E) — Rempart · Terre — mur circulaire protecteur

## Alchimiste
- ☐ Alchimiste (R) — Sables mouvants · Terre — sol piège 8 s
- ☐ Grand Alchimiste (E) — Nuage toxique · Ténèbres — poison persistant 12 s
- ☐ Philosophe Mage (E) — Pierre philosophale · Lumière — régénération + immunité poison
- ☐ Transmutateur Divin (E) — Pétrification · Terre — pétrifie 3 s, ×2 dégâts

## Druide
- ☐ Druide (R) — Racines · Terre — 3 ennemis immobilisés 3 s
- ☐ Arche Druide (E) — Bosquet · Terre — arbres qui soignent, ronces qui blessent
- ☐ Gardien de la Nature (E) — Esprit de la forêt · Terre — arbre-gardien 25 s
- ☐ Druide Primordial (E) — Tempête de feuilles · Air — tornade tranchante qui suit le viseur

## Archonte
- ☐ Archonte (U) — Lentille gravitationnelle · Spatial
- ☐ Archonte Suprême (U) — Couronne d'autorité · Spatial — répulsion permanente 6 blocs 20 s
- ☐ Seigneur Arcanique (U) — Loi arcanique · Espace — domaine sans sortie
- ☐ Pilier des Mondes (U) — Pilier du monde · Terre — colonne géante + onde sismique

## Mage du Vide
- ☐ Mage du Vide (U) — Bannissement · Espace — 5 ennemis disparaissent 6 s puis retombent
- ☐ Mage Void Absolu (U) — Effacement · Ténèbres — rayon en % des PV max
- ☐ Void Primordial (U) — Fissure du vide · Espace — téléportation aléatoire au contact
- ☐ Néant Mystique (U) — Silence du néant · Ténèbres — capacités interdites + invisibilité

## Primordial
- ☐ Primordial (U) — Éruption primordiale · Feu — lave et geysers
- ☐ Primordial Ancien (U) — Sculpteur de glace · Glace — murs et rampes à volonté 20 s
- ☐ Force Originelle (U) — Onde originelle · Spatial — onde 25 blocs, soulève ennemis et blocs
- ☐ Genèse Mage (U) — Résurrection · Lumière

## Classes Legendaires (Epic / Unique / Legendaire)
- ☐ Démiurge — Golems de cristal · Terre / Façonnage · Terre / Colosse de pierre · Terre
- ☐ Démiurge Suprême — Matière vive · Spatial / Réécriture · Espace / Marée gravitationnelle · Spatial
- ☐ Créateur Cosmique — Pluie de comètes · Feu / Naine blanche · Lumière / Supernova · Feu
- ☐ Architecte du Monde — Plan d'architecte · Terre / Labyrinthe · Espace / Porte des mondes · Espace
- ☐ Prophète — Présage · Temps / Révélation · Lumière / Prophétie · Temps
- ☐ Prophète Absolu — Verbe tonnant · Foudre / Décret absolu · Temps / Jugement céleste · Lumière
- ☐ Oracle Divin — Œil divin · Lumière / Fin et commencement · Temps / Supercellule · Foudre
- ☐ Voix de l'Éternel — Murmure · Ténèbres / Chant éternel · Lumière / Écho de l'éternité · Temps

## Classes Divines (runes Divines existantes à assigner, pas de nouvelle)
Avatar Arcanique, Demi-Arcane, Dieu des Arcanes, Demi-Mage.
