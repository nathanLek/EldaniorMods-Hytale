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
| Feu | 0 | 0 | 1 | 0 | 0 | 0 | 1 |
| Glace | 0 | 0 | 1 | 0 | 0 | 0 | 1 |
| Terre | 0 | 0 | 1 | 0 | 0 | 0 | 2 ✔ |
| Air | 1 | 0 | 1 | 0 | 0 | 0 | 1 |
| Foudre | 0 | 0 | 0 | 0 | 0 | 0 | 1 |
| Ténèbres | 0 | 0 | 1 | 1 | 0 | 0 | 1 |
| Lumière | 0 | 0 | 0 | 1 | 0 | 0 | 1 |
| Temps | 0 | 0 | 0 | 0 | 0 | 0 | 1 |
| Espace | 0 | 0 | 0 | 0 | 0 | 0 | 1 |
| Combat | 0 | 0 | 3 | 0 | 0 | 0 | 1 |
| Spatial | 0 | 0 | 0 | 1 | 1 | 1 | 1 |
| Dragonic | 0 | 0 | 0 | 0 | 0 | 1 | 1 |

Priorités suggérées : Common et Uncommon de chaque type (socle de progression), puis Epic/Unique pour Temps, Espace, Spatial, Lumière.
