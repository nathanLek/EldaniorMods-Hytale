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
| Rune_Bourrasque | Air | Common | souffle en cône 8 blocs, repousse (40 dégâts) | `RuneSpell` `Bourrasque` |
| Rune_WindStrike (vanilla) | Air | Rare | frappe de vent | JSON vanilla |
| Rune_Fireball (vanilla) | Feu | Rare | boule de feu, zone | JSON vanilla |
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
| Feu | 1 | 0 | 4 | 0 | 1 | 0 | 1 |
| Glace | 1 | 0 | 5 | 1 | 0 | 0 | 1 |
| Terre | 0 | 0 | 2 | 0 | 1 | 0 | 2 ✔ |
| Air | 1 | 0 | 1 | 0 | 1 | 0 | 1 |
| Foudre | 1 | 0 | 3 | 0 | 0 | 0 | 1 |
| Ténèbres | 0 | 0 | 2 | 1 | 0 | 0 | 1 |
| Lumière | 3 | 0 | 5 | 1 | 0 | 0 | 1 |
| Temps | 0 | 0 | 0 | 0 | 0 | 0 | 1 |
| Espace | 0 | 0 | 0 | 0 | 0 | 0 | 1 |
| Combat | 0 | 0 | 3 | 0 | 0 | 0 | 1 |
| Spatial | 1 | 0 | 0 | 1 | 1 | 1 | 1 |
| Dragonic | 0 | 0 | 0 | 0 | 0 | 1 | 1 |

Feuille de route des classes mages : `mage-roadmap.md`.

Priorités suggérées : Common et Uncommon de chaque type (socle de progression), puis Epic/Unique pour Temps, Espace, Spatial, Lumière.
