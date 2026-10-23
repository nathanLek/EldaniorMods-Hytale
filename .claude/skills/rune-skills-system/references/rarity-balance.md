# Raretés et équilibrage

## Table de base (source : `scripts/runes/rune_rules.json`)
| Rareté | Qualité Hytale | Mana | Recharge | Niveau d'objet |
|---|---|---:|---:|---:|
| Common | Common | 25 | 15 s | 10 |
| Uncommon | Uncommon | 60 | 30 s | 25 |
| Rare | Rare | 120 | 60 s | 40 |
| Epic | Epic | 300 | 120 s | 60 |
| Unique | Unique (mod) | 600 | 180 s | 75 |
| Legendaire | Legendary | 1 200 | 300 s | 90 |
| Divin | Divin (mod) | 3 000 | 500 s | 100 |

Exception (coût ou recharge différents) : seulement si la mécanique le justifie (ex. sort à charges multiples, sort qui se
réactive), avec `overrides: { mana, cooldown, reason }` dans la fiche. `rune_tool.py check` signale toute exception non justifiée.
Les sorts divins actuels sont en valeurs de test (2000 mana / 5 s), listés dans `rune_rules.json > exceptions`.

## Ce que mesure la rareté (pas seulement les dégâts)
Importance, complexité, portée, durée, nombre de cibles, impact sur le terrain, mise en scène. Un soin, un contrôle ou une mobilité
peut être Legendaire avec peu de dégâts.

## Budget de puissance
Noter la fiche (`design.balance`) de 0 à 3 sur chaque axe :
dégâts/soins, portée+rayon, durée, mobilité, contrôle, défense, nombre de cibles, fréquence, terrain, et difficulté d'utilisation
(qui **réduit** le total).

| Rareté | Total visé (somme − difficulté) | Axes à 3 autorisés |
|---|---|---|
| Common | 4–7 | aucun |
| Uncommon | 6–9 | 1 |
| Rare | 8–12 | 1 |
| Epic | 11–15 | 2 |
| Unique | 13–18 | 3 |
| Legendaire | 16–21 | 4 |
| Divin | 19–26 | libre, mais limites explicites (durée, zone, contre-jeu) |

## Fourchettes indicatives (à ajuster après tests)
| Rareté | Dégâts mono-cible | Dégâts de zone (par cible) | Rayon / portée | Durée persistante | Contrôle dur max | Terrain |
|---|---|---|---|---|---|---|
| Common | 40–80 | 25–50 | ≤ 8 | — | 0,5 s | aucun |
| Uncommon | 80–150 | 50–100 | ≤ 12 | ≤ 4 s | 1 s | cosmétique |
| Rare | 150–300 | 100–200 | ≤ 16 | ≤ 6 s | 1,5 s | petit, restauré (≤ 50 blocs) |
| Epic | 300–500 | 200–350 | ≤ 20 | ≤ 10 s | 2 s | moyen (≤ 300 blocs) |
| Unique | 500–800 | 300–500 | ≤ 25 | ≤ 15 s | 3 s | domaine (≤ 1 500 blocs) |
| Legendaire | 800–1 200 | 400–700 | ≤ 30 | ≤ 25 s | 4 s | grand (≤ 5 000 blocs) |
| Divin | 1 000+ | 500–1 200 | 35 (`DIVINE_ZONE_RADIUS`) et plus | 30–60 s | zone de contrôle | massif, toujours restauré |

Repères du projet : Bourrasque (Common) 40 dégâts / cône 8 ; wind strike des tornades de Fujin 350 ; impact du Bifrost 900 ;
lame de Fujin 700. Les monstres ont des PV virtuels élevés (logs `HP virtuels`), à prendre en compte pour l'impression de puissance.

## Garde-fous anti-abus
- Pas de contrôle dur permanent en chaîne : recharge > durée du contrôle × 5.
- Les sorts de zone épargnent groupe, guilde et famille (`enemiesAround`).
- Les dégâts de terrain ne touchent jamais les zones protégées (`isProtectedArea`) et sont restaurés.
- Mobilité : pas de téléportation dans un bloc ; protection de chute obligatoire si le sort projette le lanceur.
- Pas de soin > 50 % des PV max en Common/Uncommon/Rare.

## Quantités cibles par type (objectifs, pas obligations)
Common 8–12 · Uncommon 6–8 · Rare 5–7 · Epic 4–5 · Unique 3–4 · Legendaire 2–3 · Divin 1–2. Suivi dans `library.md`.

**Aucune nouvelle compétence Divine** (décision de l'utilisateur, 2026-10-21) : les 14 sorts divins existants suffisent ; ne pas en proposer d'autres.
