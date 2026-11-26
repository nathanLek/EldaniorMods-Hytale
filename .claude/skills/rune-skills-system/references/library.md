# Bibliothèque des runes (à tenir à jour après chaque création)

Fiches : `scripts/runes/specs/<Id>.json` (runes créées avec le skill). Les runes plus anciennes n'ont pas de fiche ;
leur description est dans leur item JSON.

## Runes existantes
| Rune | Type | Rareté | Mécanique signature | Moteur |
|---|---|---|---|---|
| Rune_Clonage | Lumière | Epic | 5 copies indiscernables du lanceur (nom, arme, 50 % PV, sans teinte), 20 s : clic droit = un clone libre part combattre puis reste autonome contre les monstres proches | `RuneSpell` `Clonage` |
| Rune_Doppelganger | Ténèbres | Epic | double exact à droite, déplacements en miroir (gauche/droite inversées), regarde ce que vise le curseur, agit seul quand le lanceur est immobile, même arme, rejoue attaques et capacités, intouchable, 30 s | `RuneSpell` `Doppelganger` |
| Rune_AutoriteGravitationnelle | Spatial | Legendaire | zone de 20 blocs autour du lanceur, 30 s : un Puits Gravitationnel sous chaque ennemi (10 max) | `RuneSpell` `AutoriteGravitationnelle` |
| Rune_Telekinesie | Spatial | Epic | main spectrale, 20 s : un clic saisit la sphère de 5 blocs au curseur, puis gauche = écraser, droit = projeter | `RuneSpell` `Telekinesie` |
| Rune_PuitsGravitationnel | Spatial | Unique | gravité sous l'ennemi visé : cratère 7×2 en demi-dalles + fissures, zone = cratère, pluie noir-violet 50 blocs, cloue 10 s (recharge test 5 s) | `RuneSpell` `PuitsGravitationnel` |
| Rune_OndeRepulsion | Spatial | Common | onde radiale 6 blocs autour du lanceur, 30 dégâts, projection (classe Mage) | `RuneSpell` `OndeRepulsion` |
| Rune_Flammeche | Feu | Common | boule de feu rapide (35 b/s, 30 blocs) déplacée par le code, 50 dégâts + Elda_Brulure (lignée Pyromancien) | `RuneSpell` `Flammeche` |
| Rune_Fireball | Feu | Rare | grosse sphère (Fireball ×2,2, 20 b/s, 35 blocs) qui explose au contact ou en fin de course : 180 r 2 / 110 r 4 + projection + Elda_Brulure, terrain intact (tous les Mages, Dragon ; refaite le 2026-11-24) | `RuneSpell` `BouleDeFeu` |
| Rune_MurDeFlammes | Feu | Rare | mur de feu 10 blocs en travers du regard, 6 s, 10 dégâts / 0,5 s au contact + brûlure (Maître des Flammes) | `RuneSpell` `MurDeFlammes` |
| Rune_MarqueInfernale | Feu | Rare | marque 6 s ; à la mort la cible explose (120, rayon 5) et la marque rebondit 3 fois ; sinon détone à la fin (Infernal Mage) | `RuneSpell` `MarqueInfernale` |
| Rune_AuraDeBraise | Feu | Rare | aura 4 blocs 6 s, 15 dégâts/s + brûlure, immunité feu, sol calciné restauré 1 min (Brasier Vivant) | `RuneSpell` `AuraDeBraise` |
| Rune_EclatDeGivre | Glace | Common | projectile de givre (40 b/s, 30 blocs), 45 dégâts + Elda_Givre (Cryomancien) | `RuneSpell` `EclatDeGivre` |
| Rune_LanceDeCristal | Glace | Rare | lance perçante 30 blocs, 120 dégâts, ×2 sur cible gelée (Elda_Gel/Niflheim_Gel/Freeze) (Maître du Givre) | `RuneSpell` `LanceDeCristal` |
| Rune_PrisonDeGlace | Glace | Rare | cage de Rock_Ice (cases vides) + gel 1,5 s, puis éclat 100 + 40 rayon 3 (Glacial Mage) | `RuneSpell` `PrisonDeGlace` |
| Rune_SentierDHiver | Glace | Rare | 6 s de neige sous les pas ; ennemis sur la traînée ralentis, gelés 1,5 s après 2 s (Seigneur du Froid) | `RuneSpell` `SentierDHiver` |
| Rune_Decharge | Foudre | Common | éclair instantané sur l'ennemi visé (18 blocs), 55 dégâts + étourdissement 0,5 s (Élémentaliste) | `RuneSpell` `Decharge` |
| Rune_ChaineDEclairs | Foudre | Rare | éclair qui saute de cible en cible (8 blocs, 5 cibles), 140 −15 %/saut, étourdissement 0,3 s (Élémentaliste Maître) | `RuneSpell` `ChaineDEclairs` |
| Rune_OrbeStatique | Foudre | Rare | orbe de particules qui avance 6 s (5 b/s) en suivant le relief, 12 dégâts/0,5 s rayon 4, explosion 90 + étourdissement (Catalyseur Arcane) | `RuneSpell` `OrbeStatique` |
| Rune_Blizzard | Glace | Epic | tempête de neige 10 s, rayon 8 au point visé : 8 dégâts/0,5 s + ralenti, gel 2 s après 3 s, neige au sol (Maître Élémentaire) | `RuneSpell` `Blizzard` |
| Rune_DomaineElementaire | Feu | Unique | domaine 15 blocs, 15 s, règle qui change toutes les 5 s : feu, glace (gel général en fin de phase), foudre (4 éclairs/s) (Seigneur des Éléments) | `RuneSpell` `DomaineElementaire` |
| Rune_TremblementTectonique | Terre | Unique | 4 séismes en ligne (tous les 5 blocs, 0,6 s), fissures de basalte, 130 dégâts + projection + étourdissement 1,5 s (Élémentaire Suprême) | `RuneSpell` `TremblementTectonique` |
| Rune_DanseDesVents | Air | Unique | 15 s : vitesse +50 %, pas de chute ; clic droit = bond + rafale 45 dégâts rayon 5 (8 max) (Force Naturelle) | `RuneSpell` `DanseDesVents` |
| Rune_HaloProtecteur | Lumière | Common | allié visé (ou soi) : -30 % de dégâts 8 s (Physical/Elemental/Projectile) + 5 % de soin (Enchanteur) | `RuneSpell` `HaloProtecteur` |
| Rune_ArmeBenie | Lumière | Rare | 10 s : chaque coup du lanceur → +25 dégâts sacrés (markEcho) et 2 % de soin, 4/s max (Grand Enchanteur) | `RuneSpell` `ArmeBenie` |
| Rune_GlyphesFoudroyants | Foudre | Rare | 3 glyphes-pièges en triangle 6 s : foudre 110 + étourdissement sur qui marche dessus, détonation des restants (Tisseur de Runes) | `RuneSpell` `GlyphesFoudroyants` |
| Rune_EgideRoyale | Lumière | Rare | dôme fixe 6 blocs 6 s : projectiles entrants (composant Projectile) détruits, ennemis repoussés, alliés -40 % (Enchanteur Royal) | `RuneSpell` `EgideRoyale` |
| Rune_SoinMineur | Lumière | Common | soin 15 % sur l'allié visé (ou soi) puis 1 %/s pendant 5 s (Guérisseur) | `RuneSpell` `SoinMineur` |
| Rune_FontaineDeVie | Lumière | Rare | source 6 s au point visé, rayon 5 : purifie les malus à l'apparition puis 5 % PV/s aux alliés (Grand Guérisseur) | `RuneSpell` `FontaineDeVie` |
| Rune_LanceDeLumiere | Lumière | Rare | rayon instantané 30 blocs : 110 aux ennemis (×2 morts-vivants, nom du rôle), +10 % PV aux alliés traversés (Prêtre Arcane) | `RuneSpell` `LanceDeLumiere` |
| Rune_LienDAme | Lumière | Rare | lien 8 s avec un allié : transfert de PV (EntityStatMap) vers le plus blessé, allié -20 % dégâts (Gardien des Âmes) | `RuneSpell` `LienDAme` |
| Rune_EspritFollet | Lumière | Common | esprit en particules à l'épaule, 5 traits (16) sur le monstre le plus proche, 1 / 1,2 s (Invocateur) | `RuneSpell` `EspritFollet` |
| Rune_GolemDePierre | Terre | Rare | golem PNJ (`Eldanior_Summon_Golem`, 600 PV) 20 s : provoque les monstres à 8 blocs, frappe (Invocateur Suprême) | `RuneSpell` `GolemDePierre` + `SummonPack` |
| Rune_SentinelleDeGivre | Glace | Rare | tourelle Rock_Ice 3 blocs + pointe au point visé, 12 s : éclat 22 + ralentissement / s (Conjurateur Maître) | `RuneSpell` `SentinelleDeGivre` |
| Rune_MeuteSpectrale | Ténèbres | Rare | 3 loups PNJ (`Eldanior_Summon_Loup`, 150 PV, teinte spectrale) 15 s, cibles réparties (Maître d'Invocation) | `RuneSpell` `MeuteSpectrale` + `SummonPack` |
| Rune_MarqueFuneste | Ténèbres | Common | cible maudite 8 s : les coups du lanceur sur elle +15 % (markEcho) (Nécromancien) | `RuneSpell` `MarqueFuneste` |
| Rune_PuitsDAmes | Ténèbres | Rare | puits 6 s rayon 5 au point visé : 10 Poison / 0,5 s, 50 % rendus au lanceur (`SpellFx.heal`) (Nécromancien Noir) | `RuneSpell` `PuitsDAmes` |
| Rune_ReleveDesMorts | Ténèbres | Rare | 15 s : monstres morts à 20 blocs → squelettes PNJ (`Eldanior_Summon_Squelette`, 120 PV) 15 s, 4 max (Seigneur des Morts) | `RuneSpell` `ReleveDesMorts` + `SummonPack` |
| Rune_AmesErrantes | Ténèbres | Rare | 4 âmes en orbite 12 s, l'une fond (guidée) sur tout ennemi à 6 blocs : 50 + 3 % PV (Invocateur d'Âmes) | `RuneSpell` `AmesErrantes` |
| Rune_Phylactere | Ténèbres | Epic | cristal 60 s aux pieds ; coup mortel annulé (`preventOwnerDeath`) → renaissance dessus à 30 % PV + 1,5 s invulnérable (Liche) | `RuneSpell` `Phylactere` |
| Rune_DomaineDeLaMort | Ténèbres | Unique | domaine fixe 15 blocs 15 s : 20 Poison/s + ralenti, chaque mort dedans → +10 % PV (Arche-Liche) | `RuneSpell` `DomaineDeLaMort` |
| Rune_FormeSpectrale | Ténèbres | Unique | 10 s : dégâts physiques annulés, autres ×0,5 (`onOwnerHurt`), vitesse +30 %, toucher glacial 2 blocs (Liche Éternelle) | `RuneSpell` `FormeSpectrale` |
| Rune_ArmeeDesTombes | Ténèbres | Unique | 8 squelettes PNJ (150 PV) en cercle, 20 s, cibles réparties (Seigneur Non-Mort) | `RuneSpell` `ArmeeDesTombes` + `SummonPack` |
| Rune_MiroirTrompeur | Lumière | Rare | 2 reflets PNJ (60 PV) s'enfuient 6 s et attirent les monstres (hostile + LockedTarget), lanceur invisible 3 s (Illusionniste) | `RuneSpell` `MiroirTrompeur` + `SummonPack` |
| Rune_GalerieDesGlaces | Lumière | Epic | 10 reflets PNJ (1 PV) figés autour de la cible 10 s : chaque coup du lanceur répété 15 %/reflet (markEcho), éclat final 40 × reflets + éblouissement (Grand Illusionniste) | `RuneSpell` `GalerieDesGlaces` + `SummonPack` |
| Rune_Hallucination | Ténèbres | Epic | brume 8 s rayon 10 : monstres appariés au hasard, forcés à se battre (NpcAttitudes), seul = terreur (Tisseur d'Illusions) | `RuneSpell` `Hallucination` |
| Rune_ChainesNoires | Ténèbres | Rare | cible + 2 voisins enchaînés 6 s (faisceaux Nyx_Chain), ramenés au centre, coups du lanceur partagés 50 % (Sorcier) | `RuneSpell` `ChainesNoires` |
| Rune_PacteDeSang | Ténèbres | Epic | −20 % PV : 10 s de coups +50 % (markEcho) et vol de vie 15 % (Sorcier Suprême) | `RuneSpell` `PacteDeSang` |
| Rune_Marionnette | Ténèbres | Epic | monstre visé contrôlé 10 s : ignore le lanceur (attitude Ignore), attaque le monstre le plus proche, fil d'ombre (Envoûteur Maître) | `RuneSpell` `Marionnette` |
| Rune_ProjectionAstrale | Espace | Rare | esprit astral (Astral_Esprit) guidé par le regard 6 s (10 b/s, 30 blocs, arrêté par les murs) ; clic ou fin = téléportation sur lui (case libre) + onde 120 rayon 3 (Mystique) | `RuneSpell` `ProjectionAstrale` |
| Rune_CeintureAsteroides | Spatial | Epic | 8 blocs de surface arrachés (rayon 2-5) en orbite 10 s ; clic = le bloc le plus en avant part vers le curseur (40 b/s) : 60 + éclats 30 rayon 2 ; sol restauré 60 s (Mystique Supérieur) | `RuneSpell` `CeintureAsteroides` |
| Rune_Prescience | Temps | Epic | 10 s : 3 coups d'un attaquant annulés (`onOwnerHurt`), pas de côté 2 blocs, Chronos_Fige 1 s sur l'attaquant, riposte +50 % (markEcho) (Voyant Arcane) | `RuneSpell` `Prescience` |
| Rune_RayonCeleste | Lumière | Epic | pilier Bifrost_Pillar qui glisse vers le curseur (7 b/s, 30 blocs) 6 s, rayon 2,5 : 14 / 0,25 s (×2 morts-vivants), alliés +1 % / 0,5 s (Devin Cosmique) | `RuneSpell` `RayonCeleste` |
| Rune_Distorsion | Temps | Epic | zone r 8 au point visé 10 s : ennemis Elda_Distorsion (vitesse ×0,4, réappliqué 0,5 s), projectiles volant vers le lanceur figés (hold) puis dissipés (Oracle) | `RuneSpell` `Distorsion` |
| Rune_RetourEnArriere | Temps | Unique | ancre (position + PV) ; à 5 s ou sur coup mortel (`preventOwnerDeath`) : retour, PV max(actuels, ancre), coups portés rejoués à 50 % (markEcho), 1 s invulnérable (Oracle Absolu) | `RuneSpell` `RetourEnArriere` |
| Rune_OeilOmniscient | Espace | Unique | œil 15 s ; clic droit = échange de places avec l'ennemi visé (40 blocs) : 150 + Gaia_Etourdi 1,5 s (appliqué au tick suivant, après la téléportation), 4 échanges (Visionnaire Suprême) | `RuneSpell` `OeilOmniscient` |
| Rune_Sentence | Temps | Unique | cible + 2 ennemis à 6 blocs condamnés 8 s ; vos dégâts sur eux cumulés (onOwnerHit) puis rejoués à l'échéance, bornés 100–800 (Prophète du Destin) | `RuneSpell` `Sentence` |
| Rune_BulleTemporelle | Temps | Epic | dôme fixe r 7, 10 s : alliés (lanceur compris) Elda_BulleHate ×1,4 + 1 % mana/s, ennemis Elda_BulleLenteur ×0,5 (Chronoturge) | `RuneSpell` `BulleTemporelle` |
| Rune_ArretDuTemps | Temps | Unique | ennemis à 12 blocs (12 max) figés 3 s (lockInPlace + Chronos_Fige) ; tous les coups reçus mis en réserve (`ArretDuTemps.absorb` dans DivineDamageSystem), rendus ×1,25 à la reprise (Maître du Temps) | `RuneSpell` `ArretDuTemps` |
| Rune_Surregime | Temps | Unique | 15 s : Elda_Surregime ×1,8, chaque coup répété à 40 % 0,3 s après (markEcho, 4/s), mana +3 %/s (Chrono-Seigneur) | `RuneSpell` `Surregime` |
| Rune_EchosDuPasse | Temps | Unique | 12 s : 3 rémanences habillées (Chronos_Ghost + Elda_Echo) rejouent vos poses à 1/2/3 s de retard et vos coups à 35 % (Tisseur Temporel) | `RuneSpell` `EchosDuPasse` |
| Rune_Meteore | Feu | Epic | alerte Muspelheim_Impact_Zone 1,2 s, météore Muspelheim_Meteor (setModelScale 5) en biais : 400 r 2 / 250 r 5 + Elda_Brulure, cratère (lave au fond, Soil_Dirt_Burnt autour) restauré 60 s (Magus) | `RuneSpell` `Meteore` |
| Rune_RayonArcanique | Lumière | Unique | faisceau Basic canalisé 5 s qui suit le regard (30 blocs, arrêté par le solide) : 30 / 0,25 s aux ennemis traversés, lanceur Elda_Canalisation ×0,5 (Magus Suprême) | `RuneSpell` `RayonArcanique` |
| Rune_AvatarOrage | Foudre | Unique | 15 s : chaque coup → éclair Taranis_Bolt 60 + Taranis_Foudroye 0,3 s, saut 40 sur le voisin à 6 blocs, 3/s (markEcho), ×1,15 vitesse (Archi-Magus) | `RuneSpell` `AvatarOrage` |
| Rune_Eternite | Temps | Unique | 10 s : coup mortel annulé et PV fixés à 1 (`preventOwnerDeath`), +20 % PV max à la fin si sauvé (Magus Éternel) | `RuneSpell` `Eternite` |
| Rune_SalveArcanique | Lumière | Rare | 5 orbes Bifrost_Divine (particules déplacées par le code) en éventail puis à tête chercheuse (18 b/s), répartis sur les ennemis devant (25 blocs) : 45 chacun (Archimage) | `RuneSpell` `SalveArcanique` |
| Rune_NuageOrage | Foudre | Epic | nuage Taranis_Storm_Clouds_Low (échelle 12) à 12 blocs, 10 s : éclair / 0,5 s sur un ennemi au hasard (r 7), 70 + Taranis_Foudroye 0,5 s (Grand Archimage) | `RuneSpell` `NuageOrage` |
| Rune_Reflexion | Lumière | Epic | dôme 5 blocs qui suit le lanceur 8 s : projectiles volant vers lui détruits et renvoyés en trait sur le tireur (80), coups reçus renvoyés à 30 % (Archimage Suprême) | `RuneSpell` `Reflexion` |
| Rune_Decret | Spatial | Epic | r 8 au point visé : plaqués au sol (poussée −18, lockInPlace + Elda_Decret 2 s, 40) puis éjectés haut/extérieur (150) (Archimage Royal) | `RuneSpell` `Decret` |
| Rune_Meditation | Lumière | Rare | 4 s sans bouger ni être frappé : +5 % PV et +8 % mana/s ; à terme, onde 6 blocs : alliés cureAllDebuffs + 10 %, ennemis 60 + recul (Sage) | `RuneSpell` `Meditation` |
| Rune_SanctuaireMineur | Lumière | Epic | dôme fixe 6 blocs 8 s : Elda_Sanctuaire (DamageResistance 40 %) à l'entrée, cureAllDebuffs + unlock / 0,5 s, +1 % PV/s (Grand Sage) | `RuneSpell` `SanctuaireMineur` |
| Rune_SavoirAccelere | Temps | Epic | 10 s : mana +5 %/s, Elda_SavoirAccelere (OutgoingDamage Elemental +20 %) (Érudit Arcane) | `RuneSpell` `SavoirAccelere` |
| Rune_Enseignement | Lumière | Epic | instantané : alliés à 15 blocs (lanceur compris) Elda_Enseignement (OutgoingDamage +30 % 10 s) + 10 % mana (Mentorant Suprême) | `RuneSpell` `Enseignement` |
| Rune_Miracle | Lumière | Rare | instantané : soin 40 % PV max sur l'allié visé (ou soi) ; la part au-delà des PV max explose r 4 (180 × débordement/40 %, ×2 morts-vivants) (Thaumaturge) | `RuneSpell` `Miracle` |
| Rune_PilierAscendant | Terre | Epic | colonne 3×3 de Rock_Basalt (6 couches / 150 ms, passagers téléportés puis couche posée au tick suivant), 5 s, effondrement : 200 r 5 + Gaia_Etourdi 1 s (Grand Thaumaturge) | `RuneSpell` `PilierAscendant` |
| Rune_Inversion | Spatial | Epic | r 7 au point visé, 3 cycles de 2 s : lévitation à 4 blocs (vitesse verticale imposée) puis plaquage −25 / 80 (Maître du Prodige) | `RuneSpell` `Inversion` |
| Rune_Rempart | Terre | Epic | ennemis à 5,5 blocs repoussés (30), anneau de Rock_Stone_Brick r 5 × 3 (sol local par colonne), 10 s, restauré (Thaumaturge Royal) | `RuneSpell` `Rempart` |
| Rune_SablesMouvants | Terre | Rare | surface en Soil_Sand r 4 (restaurée 6 s) : Elda_Enlisement ×0,3, 10 Earth / 0,5 s, lockInPlace 1 s après 2 s dedans (Alchimiste) | `RuneSpell` `SablesMouvants` |
| Rune_NuageToxique | Ténèbres | Epic | nuage Nuage_Toxique (particule du mod) r 6, 10 s, dérive 2 b/s vers le curseur : 14 Poison / 0,5 s, teinte Elda_Toxique renouvelée avant expiration (Grand Alchimiste) | `RuneSpell` `NuageToxique` |
| Rune_PierrePhilosophale | Lumière | Epic | alliés à 8 blocs (à l'incantation), 10 s : +3 % PV/s, Elda_PierrePhilosophale (DamageResistance Poison 100 %), cureAllDebuffs / 2 s (Philosophe Mage) | `RuneSpell` `PierrePhilosophale` |
| Rune_Petrification | Terre | Epic | cible + 2 voisins (3 blocs) : lockInPlace + Elda_Petrifie (Stoneskin) 2 s, vos coups +100 % (markEcho), brisure 120 (Transmutateur Divin) | `RuneSpell` `Petrification` |
| Rune_Racines | Terre | Rare | racines souterraines (22 b/s, suivent la cible) vers l'ennemi vise + 2 voisins (6 blocs) : 90, Elda_Racines (modele Root vanilla) + lockInPlace 1,5 s, epines 15 / 0,5 s, puis Yggdrasil_Ronces 1,6 s (Druide) | `RuneSpell` `Racines` |
| Rune_Bosquet | Terre | Epic | bosquet r 7 au point vise, 10 s : 3 chenes de blocs qui poussent couche par couche + haie de Plant_Bramble_Moss_Twisted (sur Soil_*) ; allies +1,5 % PV / 0,5 s, ennemis 12 / 0,5 s + Yggdrasil_Ronces ; restaure (Arche Druide) | `RuneSpell` `Bosquet` |
| Rune_EspritForet | Terre | Epic | esprit-arbre PNJ (`Eldanior_Summon_Esprit_Foret`, modele Hedera recolore vert, 800 PV, griffes de Hedera) 25 s ; / 3 s : racines sous les 2 ennemis les plus proches du lanceur (12 blocs), 80 + Elda_Racines 1 s (Gardien de la Nature) | `RuneSpell` `EspritForet` + `SummonPack` |
| Rune_TempeteFeuilles | Air | Epic | tornade (modele Tempete_Feuilles = tornade vanilla x3 + particule de feuilles du mod) 8 s, suit le viseur au sol a 5 b/s : aspiration en spirale (5 blocs), 16 Slashing / 0,5 s (3,5 blocs), ejection finale 60 Wind (Druide Primordial) | `RuneSpell` `TempeteFeuilles` |
| Rune_LentilleGravitationnelle | Spatial | Unique | lentille r 8 au point vise, 12 s (Chaos_BlackHole + Chaos_Accretion) : ennemis devies en orbite (poussee tangentielle / 0,25 s), vos coups +40 % (markEcho), projectiles ennemis (tireur deduit de la trajectoire inverse) captures en orbite (12) ; effondrement 200 + 30 / projectile (Archonte) | `RuneSpell` `LentilleGravitationnelle` |
| Rune_CouronneAutorite | Spatial | Unique | 15 s, couronne Apollo_Laurel sur la tete : ennemis a 6 blocs repousses (40 / s max), projectiles entrants detruits ; 3 clics = decret r 12 : 150 + ejection (Archonte Supreme) | `RuneSpell` `CouronneAutorite` |
| Rune_LoiArcanique | Espace | Unique | domaine fixe r 15, 12 s : ennemi inscrit qui franchit la frontiere teleporte au point oppose (r 13, case libre) ; 4 premiers renvois : 60 + Gaia_Etourdi 0,5 s (tick suivant) ; verdict 60 / renvoi (240 max) (Seigneur Arcanique) | `RuneSpell` `LoiArcanique` |
| Rune_PilierDuMonde | Terre | Unique | pilier Rock_Basalt r 2 x 18 (anneaux de marbre), 250 + projection a la sortie, 3 ondes sismiques (15 b/s, r 20) : 120 + Gaia_Etourdi 1 s, effondrement 150 r 5 a 12 s, restaure (Pilier des Mondes) | `RuneSpell` `PilierDuMonde` |
| Rune_Bannissement | Espace | Unique | cible + 4 voisins (8 blocs) : teleportes 60 blocs au-dessus de leur place, Elda_Banni (modele dissous) + lockInPlace 6 s (joueurs replaces / 0,25 s), puis rendus 8 blocs au-dessus de leur place : 250 ; lanceur mort : retour sans degats (Mage du Vide) | `RuneSpell` `Bannissement` |
| Rune_Effacement | Tenebres | Unique | rayon instantane 30 blocs (incantation 1 s) : 25 % des PV max virtuels (MobVirtualHPSystem) bornes 150-2 000 a chaque ennemi aligne ; sous 10 % estimes : PV restants effaces en plus (Mage Void Absolu) | `RuneSpell` `Effacement` |
| Rune_FissureVide | Espace | Unique | fissure 12 blocs en travers du regard, 12 s (3 Chaos_Rift) : ennemi qui la touche recrache au hasard (8-16 blocs, 10 au-dessus d'une case libre), 80, 3 fois max ; le lanceur y entre = bond de 12 blocs vers son regard (Void Primordial) | `RuneSpell` `FissureVide` |
| Rune_SilenceNeant | Tenebres | Unique | domaine r 10, 10 s : monstres IGNORE envers les joueurs + LockedTarget efface, joueurs ennemis Elda_Silence + runes dissipees (`RuneSpellRuntime.silence`), 25 / s ; lanceur invisible tant qu'il y reste (Neant Mystique) | `RuneSpell` `SilenceNeant` |
| Rune_EruptionPrimordiale | Feu | Unique | 12 s, r 10 au point vise : 6 geysers (alerte Muspelheim_Impact_Zone 0,8 s) sous les ennemis : 180 + projection + brulure, mares 3x3 de Rock_Volcanic_Cracked_Lava (15 / 0,5 s), restaurees (Primordial) | `RuneSpell` `EruptionPrimordiale` |
| Rune_SculpteurDeGlace | Glace | Unique | 15 s : clic gauche = mur Rock_Ice 4x3, clic droit = rampe 4 marches (6 max, axes cardinaux) ; 7e clic ou fin : eclatement 100 + Elda_Givre r 3 par structure (Primordial Ancien) | `RuneSpell` `SculpteurDeGlace` |
| Rune_OndeOriginelle | Spatial | Unique | onde depuis le lanceur, 12 b/s jusqu'a 25 blocs : 150, levitation 1,5 s puis plaquage 200 ; blocs de surface arraches (entites-blocs en parabole, 90 max), restaures (Force Originelle) | `RuneSpell` `OndeOriginelle` |
| Rune_Resurrection | Lumiere | Unique | 15 s : lanceur + allies a 15 blocs benis (`RuneSpellRuntime.ward`) ; premier coup mortel annule : 50 % PV, purification, 2 s invulnerable, onde sacree 150 r 5 (x2 morts-vivants) (Genese Mage) | `RuneSpell` `Resurrection` |
| Rune_GolemsCristal | Terre | Epic | 3 golems de cristal PNJ (Eldanior_Summon_Golem_Feu / _Givre / _Foudre, 250 PV) 15 s : / 1,5 s leur cible prend 25 + brulure / givre / foudroiement ; eclatement 120 r 3 a leur chute ou a la fin (Demiurge) | `RuneSpell` `GolemsCristal` + `SummonPack` |
| Rune_Faconnage | Terre | Unique | domaine r 12, 12 s : / 0,5 s pic de basalte sous un ennemi (90 + projection), pose 0,2 s apres, reste (24 max) ; allies Elda_Faconnage (-25 %) ; effondrement 60, restaure (Demiurge) | `RuneSpell` `Faconnage` |
| Rune_ColossePierre | Terre | Legendaire | 20 s : modele du joueur recopie x2 (`ColossePierre.scaled`), Elda_Colosse (-35 %, x0,8) ; 150 + recul a la transformation, crateres 80 r 2 a chaque coup (3/s, cases videes, restaurees), pas 25 r 3 / s ; garde-fou `restoreIfStuck` (Demiurge) | `RuneSpell` `ColossePierre` |
| Rune_MatiereVive | Spatial | Epic | 6 blocs de surface arraches (r 3 au point vise) prennent vie 10 s : bonds paraboliques vers l'ennemi le plus proche (15 blocs), 60 + recul, 3 impacts chacun puis poussiere ; sol restaure (Demiurge Supreme) | `RuneSpell` `MatiereVive` |
| Rune_Reecriture | Espace | Unique | domaine r 12, 12 s : / 2 s derangement des places des ennemis (2 a 12, teleportation), 60 + Gaia_Etourdi 0,5 s au tick suivant (Demiurge Supreme) | `RuneSpell` `Reecriture` |
| Rune_MareeGravitationnelle | Spatial | Legendaire | 20 s, r 25 autour du point d'incantation, 5 cycles : basse = levitation + debris arraches qui montent ; haute = plaquage 60, debris qui retombent, cuvette conique (4 couches) ; allies sans chute ; restaure (Demiurge Supreme) | `RuneSpell` `MareeGravitationnelle` |
| Rune_PluieCometes | Feu | Epic | 10 s, r 8 au point vise : 16 cometes (Muspelheim_Meteor x1,5, 1 / 0,6 s, en biais) sur des ennemis au hasard : 70 + brulure r 2,5 (Createur Cosmique) | `RuneSpell` `PluieCometes` |
| Rune_NaineBlanche | Lumiere | Unique | etoile 12 s a 4 blocs au-dessus du point vise (Apollo_Sun reduit) : attraction 2,5 b/s, 20 / 0,5 s (x2 morts-vivants), allies +1 % PV / 0,5 s (r 10) ; effondrement 250 r 8 (Createur Cosmique) | `RuneSpell` `NaineBlanche` |
| Rune_Supernova | Feu | Legendaire | charge 3 s (Elda_Canalisation, degats subis x0,5, attraction + 15 / 0,5 s a 15 blocs), explosion 600 + brulure r 8, onde 8 -> 30 blocs (250), sol calcine r 10 restaure 60 s (Createur Cosmique) | `RuneSpell` `Supernova` |
| Rune_PlanArchitecte | Terre | Epic | trace de 2 blocs devant soi au point vise (20) : plan 1 s puis mur Rock_Stone_Brick 3 de haut colonne par colonne, ennemis ecartes (60) ; ecroulement a 11 s : 120 a 2 blocs ; restaure (Architecte du Monde) | `RuneSpell` `PlanArchitecte` |
| Rune_Labyrinthe | Espace | Unique | anneaux de murs r 4/7/10 (2 de haut, 2 breches chacun, cases occupees sautees) 10 s ; / 2 s chaque ennemi teleporte sur une case libre d'une autre allee (jamais sur un mur) : 40 + etourdi 0,5 s (Architecte du Monde) | `RuneSpell` `Labyrinthe` |
| Rune_PorteMondes | Espace | Legendaire | porte Gilgamesh_Portal x2,4 25 s devant soi : lanceur et allies joueurs teleportes a une ancre (bloc Cercle d'Ancrage pose et nomme, sauvegarde, meme monde ; fenetre de choix si plusieurs ; 3 ancres si le sort est connu, 1 sinon) sinon au point d'apparition du monde ; chute protegee 5 s (Architecte du Monde) | `RuneSpell` `PorteMondes` + package `anchors` |
| Rune_Presage | Temps | Epic | 3 lectures (0,5 / 3,5 / 6,5 s) : sceau dore la ou chacun des 6 ennemis proches (15 blocs) sera dans 1,5 s (deplacement observe x3) ; frappe 120 + Chronos_Fige 1 s a 2 blocs (Prophete) | `RuneSpell` `Presage` |
| Rune_Revelation | Lumiere | Unique | onde r 20 : 100 (x2 morts-vivants), invisibles devoiles, monstres eblouis 2 s ; Elda_Revele + vulnerabilite x1,3 toutes sources 12 s (`RuneSpellRuntime.expose`) (Prophete) | `RuneSpell` `Revelation` |
| Rune_Prophetie | Temps | Legendaire | 15 s, lanceur + allies a 15 blocs : Elda_Prophetie (+30 %), 3 coups esquives chacun (`ward`), riposte 80 + Chronos_Fige 0,5 s sur l'attaquant (Prophete) | `RuneSpell` `Prophetie` |
| Rune_Ruee | Combat | Common | charge 12 blocs (poussee entretenue du lanceur), s'arrete sur le premier ennemi : 50 + recul, protection de chute (rune propre du Guerrier) | `RuneSpell` `Ruee` |
| Rune_VoileDeFumee | Ténèbres | Common | bombe fumigene (nuage Voile_Fumee r ~5) : invisible 3 s (modele dissous, Elda_InvisTotal), monstres a 8 blocs perdent la trace (attitude IGNORE + LockedTarget efface), premier coup +50 % (markEcho) (rune propre de l'Assassin) | `RuneSpell` `VoileDeFumee` |
| Rune_PluieDeFleches | Combat | Common | maintenir pour bander l'arc (cast.hold, 0,6 s), relacher : 10 fleches (Arrow_Iron deplacees par le code) retombent en 1 s sur la zone visee (r 4, 20 blocs) : 18 chacune, 3 max par ennemi (rune propre de l'Archer) | `RuneSpell` `PluieDeFleches` |
| Rune_LancerDePieces | Combat | Common | pieces d'or en cone 6 blocs : 45 + recul ; +5 or par ennemi touche tue dans les 1,5 s (ecriture differee de PlayerLevelData) (rune propre du Marchand) | `RuneSpell` `LancerDePieces` |
| Rune_TransformationArme | Combat | Legendaire (200 mana, 600 s, exception : classe Mousquetaire) | mousquet legendaire en main (garde `Eldanior_ArmeLegendaireTenue` avant le cout, champ `gate` de la fiche) : 5 min sous une autre forme (Black Bess -> SARS, Green Colt -> Colts de cavalerie, Blue Tiger -> canon de char), puis retour au mousquet ; aucun effet visuel sur le personnage (rune de classe du Mousquetaire) | `RuneSpell` `TransformationArme` -> `ArmeTransfo.transform` |
| Rune_KiEpee | Combat | Rare (bascule, 0 mana / 1 s) | Ki de l'Epee de la voie martiale Luo : clic = active / coupe, 3-8 mana/s (coupe a sec) ; armes a lame +8 a +50 %, Ki qui traverse l'armure, eclat au 4e coup, lames de Ki (Tenryumaru.kiBlade) des le palier 4, echo et parade des projectiles (6), fleur de Ki (7), coupe de zone (8) ; palier = VoieMartiale.tier | `RuneSpell` `KiEpee` |
| Rune_MeditationMartiale | Combat | Rare (bascule, 0 mana / 1 s) | posture en tailleur (Luo_Gestes/Meditation), immobile et vulnerable, coupee par un coup ou un second clic ; +2 a +12 vie/mana/endurance par 10 s, +1 a +5 XP par 15 s selon le palier ; coupe le Ki | `RuneSpell` `MeditationMartiale` |
| Rune_PaumeKi | Combat | Common | paume sur le premier ennemi a 3 blocs : 70 + gros recul + Stun 0,5 s, sans arme (Apprenti en Arts Martiaux) | `RuneSpell` `PaumeKi` |
| Rune_FrappeMeridiens | Combat | Uncommon | 3 x 40 sur l'ennemi vise (4 blocs), puis meridiens scelles 4 s : ralenti + expose x1,15 (Disciple Martial) | `RuneSpell` `FrappeMeridiens` |
| Rune_GardeFleuve | Combat | Rare | garde 1,5 s : premier coup recu annule et renvoye (250 + recul, 8 blocs) (Expert Martial) | `RuneSpell` `GardeFleuve` |
| Rune_CroissantKi | Combat | Rare | lame de Ki x2,4 couleur du palier, 20 blocs a 30 b/s, traverse les ennemis : 220 chacun (Maitre Martial) | `RuneSpell` `CroissantKi` |
| Rune_DomaineEpee | Combat | Epic | domaine 8 blocs qui suit le lanceur 8 s : ennemi qui bouge (> 0,3 bloc / 0,25 s) tranche 60 (2/s max) ; ennemis dedans -20 % de degats sur le lanceur (Grand Maitre Martial) | `RuneSpell` `DomaineEpee` |
| Rune_FleurPrunier | Combat | Unique | 40 petales en 2 s dans un cone 10 blocs / 60 deg : 25 chacun sur un ennemi au hasard ; 5 petales = floraison 300 r 3 (Saint Martial) | `RuneSpell` `FleurPrunier` |
| Rune_TransformationHwakyung | Combat | Legendaire | 20 s : +35 % degats, -30 % subis, malus purges / 0,25 s, onde de Ki 40 % r 3 a chaque coup (Maitre Supreme) | `RuneSpell` `TransformationHwakyung` |
| Rune_UniteCielTerre | Combat | Legendaire | 10 s intouchable ; vent 90 / 0,6 s + ralenti sur les ennemis a 25 blocs (16 max) ; coupe finale 700 (Dieu Martial) | `RuneSpell` `UniteCielTerre` |
| Rune_RenforcementCorps | Luo | Uncommon (bascule) | voie martiale, Disciple : -10 a -30 % de degats subis, bouclier de Ki (palier 5+, absorbe un coup / 8 s), 2-6 mana/s | `RuneSpell` `RenforcementCorps` |
| Rune_PasLeger | Luo | Rare (25 mana / 5 s) | voie martiale, Expert : ruee 8 m, pas fantome intouchable (4), bond aerien (5), teleportation 14/20/26 m (6-8, coupe 50/80 au 7+), vitesse x1,15-1,35 4 s | `RuneSpell` `PasLeger` |
| Rune_SensMartial | Luo | Rare (bascule) | voie martiale, Maitre : marques vues du seul lanceur (SpellFx.fxFor) 20-36 m, puissance (5+), revele les invisibles (6+), coups dans le dos /2 ; doubles devant les murs via `WallGhosts` (sans etiquette depuis le 2026-11-17) | `RuneSpell` `SensMartial` |
| Rune_ArkiesDomini | Tenebres | Legendaire (bascule) | classe Observateur : 1,5 % mana max/s, caracteristiques x2 (statMultiplier), domaine 100 m (silhouettes 40, doubles 12 via WallGhosts, invisibles reveles), ecoute (arrivees, combats, morts), carte de l'entite visee dans un HUD avec modele 3D (ItemPreviewComponent + portraits Eldanior_Visage_*) | `RuneSpell` `ArkiesDomini` |
| Rune_SansVisage | Tenebres | Legendaire (fenetre) | classe Observateur : deguisement (joueur du monde, PNJ, animal, monstre, skin aleatoire), nom d'emprunt, fausse classe ; 300 mana par visage ; tombe a la mort / deconnexion / Sentence | `RuneSpell` `SansVisage` + `SansVisagePage` |
| Rune_SentenceSilencieuse | Tenebres | Legendaire | classe Observateur, ultime : cible figee, invisibilite, teleportation dans le dos, execution sous 40 % (joueurs 30 %, boss jamais), sinon 35 % / 25 % / 15 % de la vie max ; 1200 mana / 300 s preleves seulement avec une cible | `RuneSpell` `SentenceSilencieuse` |
| Rune_ProjectionEpeeAura | Luo | Epic | voie martiale, Grand Maitre : 1/3/6/12 epees Ki_Epee en orbite 20 s, frappent seules (45-80) | `RuneSpell` `ProjectionEpeeAura` |
| Rune_VolEpeeKi | Luo | Unique (bascule) | voie martiale, Saint : FlyMode.Forced vitesse 9/11/13, epee Ki_Epee_Vol sous les pieds, 8/6/4 mana/s | `RuneSpell` `VolEpeeKi` |
| Rune_IncarnationAme | Luo | Legendaire | voie martiale, Maitre Supreme : 1 double (2 au Dieu Martial) PNJ copie (CloneFx), corps Hwakyung_Corps, 60 % vie, autonome 25/30 s | `RuneSpell` `IncarnationAme` |
| Rune_DomaineDivin | Luo | Legendaire | voie martiale, Dieu Martial : 15 m, 12 s, ennemis ralentis + 50/s plaques au sol, lanceur -30 % et +3 % vie/s | `RuneSpell` `DomaineDivin` |
(2026-11-10 : les 8 anciennes runes de palier, Paume de Ki a Unite du Ciel et de la Terre, sont des techniques de clan : luo.LuoClans.TECHNIQUES, classes "clan".)
| Rune_Bourrasque | Air | Common | souffle en cône 8 blocs, repousse (40 dégâts) | `RuneSpell` `Bourrasque` |
| Rune_WindStrike (vanilla) | Air | Rare | frappe de vent | JSON vanilla |
| Rune_ChargedShot (vanilla) | Glace | Rare | tir chargé d'eau | JSON vanilla |
| Rune_GroundSlam (vanilla) | Terre | Rare | frappe au sol, zone | JSON vanilla |
| Rune_Enrage (vanilla) | Combat | Rare | buff de rage | JSON vanilla |
| Rune_ChainHook (vanilla) | Combat | Rare | grappin qui attire | JSON vanilla |
| Rune_PoisonImbue (vanilla) | Ténèbres | Rare | arme empoisonnée | JSON vanilla |
| Rune_AuraBlade | Combat | Rare | lame d'aura | JSON |
| Rune_Aura | — | Legendary (spécial) | aura de dignité, donnée par le titre | JSON + `RuneAuraSystem` |
| Rune_AvatarDragon | Dragonic | Legendaire | forme draconique | `DragonAvatar` |
| Rune_CourrouxMuspelheim | Feu | Divin | pluie de feu, terre brûlée | `MuspelheimWrath` |
| Rune_PorteNiflheim | Glace | Divin | porte du froid, gel de zone | `NiflheimGate` |
| Rune_ReveilGaia | Terre | Divin | plaques tectoniques, crocs, séismes | `GaiaAwakening` |
| Rune_EveilYggdrasil | Terre (nature) | Divin | arbre-monde, racines, fontaine de soin | `YggdrasilAwakening` |
| Rune_CourrouxFujin | Air | Divin | Œil du Cyclone : le lanceur plane, mur de 100 blocs, nuages, aspire entités, sol et arbres entiers sur 35 blocs, lames-projectiles qui tranchent | `RuneSpell` `FujinWrath` |
| Rune_SanctuaireTaranis | Foudre | Divin | sanctuaire de foudre | `TaranisSanctuary` |
| Rune_VoileNyx | Ténèbres | Divin | Trône du Néant : dôme, chaînes, bannissement, ombres | `NyxVeil` |
| Rune_SanctuaireApollon | Lumière | Divin | temple solaire, soins, feu sacré | `ApolloSanctuary` |
| Rune_SablierChronos | Temps | Divin | sablier, figer, rémanences | `ChronosHourglass` |
| Rune_PontBifrost | Espace | Divin | pont arc-en-ciel, téléportation 600 blocs, pilier de lumière | `BifrostBridge` |
| Rune_AbimeChaos | Spatial | Divin | trou noir qui grossit et aspire tout | `ChaosAbyss` |
| Rune_AvenementBahamut | Dragonic | Divin | avènement du dragon-roi | `BahamutAdvent` |
| Rune_GilgameshBabylon | Combat | Divin | porte de Babylone, pluie d'armes | `GilgameshBabylon` |

Ajouter un type : `rune_rules.json > types` (palette, mots-clés, teinte), `rune_icons.py > EMBLEMS` (embleme), puis ce fichier et `categories.md`.

## Couverture (existant / objectif)
| Type | Common 8–12 | Uncommon 6–8 | Rare 5–7 | Epic 4–5 | Unique 3–4 | Legendaire 2–3 | Divin 1–2 |
|---|---|---|---|---|---|---|---|
| Feu | 1 | 0 | 4 | 2 | 2 | 1 | 1 |
| Glace | 1 | 0 | 5 | 1 | 1 | 0 | 1 |
| Terre | 0 | 0 | 4 | 7 ✔ | 3 ✔ | 1 | 2 ✔ |
| Air | 1 | 0 | 1 | 1 | 1 | 0 | 1 |
| Foudre | 1 | 0 | 3 | 1 | 1 | 0 | 1 |
| Ténèbres | 2 | 0 | 6 | 6 | 5 | 0 | 1 |
| Lumière | 3 | 0 | 9 | 7 | 4 ✔ | 0 | 1 |
| Temps | 0 | 0 | 0 | 5 ✔ | 6 | 1 | 1 |
| Espace | 0 | 0 | 1 | 0 | 6 ✔ | 1 | 1 |
| Combat | 3 | 0 | 3 | 0 | 0 | 0 | 1 |
| Spatial | 1 | 0 | 0 | 5 ✔ | 4 ✔ | 2 ✔ | 1 |
| Dragonic | 0 | 0 | 0 | 0 | 0 | 1 | 1 |

Feuille de route des classes mages : `mage-roadmap.md`.

Priorités suggérées : Common et Uncommon de chaque type (socle de progression), puis Epic/Unique pour Temps, Espace, Spatial, Lumière.
