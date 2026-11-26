# Économie d'Eldanior — répertoire de tous les objets

> Fichier **généré** par `scripts/economie/gen_economie.py` le 2026-11-25 — ne pas modifier à la main : changer les prix dans `scripts/economie/prix.json`, puis relancer le script.

**5932 objets** (3950 vanilla, 1982 Eldanior) · **4807** ont un prix · **14** sans prix · **1111** hors économie.

## Règles

- Tous les prix sont en **cuivres** : 1 argent = 10 · 1 or = 100 · 1 diamant = 1 000 · 1 zénith = 10 000 · 1 astrale = 100 000 (`CoinWallet`, 1 « or » du compte en banque = 1 cuivre).
- **fixé** : objet sans recette, prix saisi dans `prix.json` → `ressources`.
- **calculé** : objet avec recette = somme des ingrédients × **1.3** (marge de l'artisan) ÷ quantité produite, arrondi à 2 chiffres significatifs. Changer le prix d'une ressource recalcule tout ce qui en dépend.
- **forcé** : prix imposé dans `prix.json` → `forces` (remplace le calcul ; marche aussi pour un objet hors économie qu'on veut vendre).
- `[Type]` dans une recette : n'importe quel objet de ce type de ressource ; il vaut le moins cher d'entre eux (voir la table des types en fin de document).
- Repères : épée en fer ≈ 20 or ; lingot de fer divin ≈ 1 000 000 (10 astrales), la ressource la plus chère.
- Boutiques : rachat d'un objet porté = ¼ du prix ; taxe de commerce = 10 % du prix payé, versée à la zone.
- Colonne **O** : V = vanilla, E = Eldanior. « (jeu) » : prix modifié en jeu avec `/es prix` (fichier `prix_jeu.json` du serveur ; `--fusionner` le recopie dans `prix.json`).

## Sommaire

| Famille | Sans recette (fixé) | Avec recette (calculé) | Total |
|---|---:|---:|---:|
| [Monnaie](#fam-monnaie) | 7 | 0 | 7 |
| [Minerais & métaux](#fam-minerais-metaux) | 55 | 11 | 66 |
| [Bois](#fam-bois) | 224 | 219 | 443 |
| [Pierres & terres](#fam-pierres-terres) | 274 | 584 | 858 |
| [Plantes & cultures](#fam-plantes-cultures) | 465 | 107 | 572 |
| [Ingrédients](#fam-ingredients) | 57 | 38 | 95 |
| [Poissons](#fam-poissons) | 30 | 0 | 30 |
| [Nourriture](#fam-nourriture) | 43 | 21 | 64 |
| [Potions](#fam-potions) | 21 | 44 | 65 |
| [Butins de monstres](#fam-butins-de-monstres) | 285 | 0 | 285 |
| [Armes](#fam-armes) | 218 | 109 | 327 |
| [Armures](#fam-armures) | 159 | 79 | 238 |
| [Outils & utilitaires](#fam-outils-utilitaires) | 22 | 51 | 73 |
| [Runes & compétences](#fam-runes-competences) | 674 | 8 | 682 |
| [Construction](#fam-construction) | 191 | 98 | 289 |
| [Meubles & décoration](#fam-meubles-decoration) | 292 | 280 | 572 |
| [Livres & histoires](#fam-livres-histoires) | 49 | 0 | 49 |
| [Divers](#fam-divers) | 97 | 9 | 106 |
| [Hors économie](#fam-hors-economie) | | | 1111 |
| [À fixer](#fam-a-fixer) | | | 14 |

<a id="fam-a-fixer"></a>
## À fixer

Objets sans prix : ressource absente de `prix.json` (relancer avec `--init` pour une estimation), ou recette dont un ingrédient n'a pas de prix.

| ID | Nom | Raison |
|---|---|---|
| `Artefact_Daggers_Observateur` | Mumei et Kagerō, Lames de l'Observateur | pas dans prix.json |
| `Eldanior_Borne_Ville` | Borne de ville | pas dans prix.json |
| `Eldanior_Manuel_Magie_Speciales_Aura` | Manuel Spécial : Aura | pas dans prix.json |
| `Eldanior_Manuel_Magie_Tenebres_ArkiesDomini` | Manuel des Ténèbres : Arkies Domini | pas dans prix.json |
| `Eldanior_Manuel_Magie_Tenebres_SansVisage` | Manuel des Ténèbres : Sans Visage | pas dans prix.json |
| `Eldanior_Manuel_Magie_Tenebres_SentenceSilencieuse` | Manuel des Ténèbres : Sentence Silencieuse | pas dans prix.json |
| `Eldanior_Portail_Continental` | Pierre de fondation du Portail Continental | pas dans prix.json |
| `Rune_ArkiesDomini` | Rune Arkies Domini | pas dans prix.json |
| `Rune_Aura` | Rune d'Aura | pas dans prix.json |
| `rune_page_arkiesdomini` | Parchemin de Rune : Arkies Domini | pas dans prix.json |
| `rune_page_sansvisage` | Parchemin de Rune : Sans Visage | pas dans prix.json |
| `rune_page_sentencesilencieuse` | Parchemin de Rune : Sentence Silencieuse | pas dans prix.json |
| `Rune_SansVisage` | Rune Sans Visage | pas dans prix.json |
| `Rune_SentenceSilencieuse` | Rune Sentence Silencieuse | pas dans prix.json |

<a id="fam-monnaie"></a>
## Monnaie

### Sans recette — prix fixés (7)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Elda_Astral_Coins` | Pièce Astrale | Légendaire |  | 100 000 | 1 astrale | monnaie | E |
| `Elda_Copper_Coins` | Pièce de Bronze | Commun |  | 1 | 1 cuivre | monnaie | E |
| `Elda_Diamond_Coins` | Pièce de Diamand | Commun |  | 1 000 | 1 diamant | monnaie | E |
| `Elda_Gold_Coins` | Pièce d'Or | Commun |  | 100 | 1 or | monnaie | E |
| `Elda_Imperial_Coins` | Pièce Impériale | Divin |  | 1 000 000 | 1 impériale | monnaie | E |
| `Elda_Silver_Coins` | Pièce d'Argent | Commun |  | 10 | 1 argent | monnaie | E |
| `Elda_Zenith_Coins` | Pièce de Zenith | Légendaire |  | 10 000 | 1 zénith | monnaie | E |

<a id="fam-minerais-metaux"></a>
## Minerais & métaux

### Sans recette — prix fixés (55)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Ingredient_Bar_Bronze` | Lingot de bronze |  |  | 300 | 3 or | fixé | V |
| `Ore_Adamantite` | Minerai d’adamantite |  | 10 | 1 000 | 1 diamant | fixé | V |
| `Ore_Adamantite_Magma` | Minerai d’adamantite : magma |  |  | 1 000 | 1 diamant | fixé | V |
| `Ore_Adamantite_Magma_Cracked` | Magma fissuré |  | 10 | 1 000 | 1 diamant | fixé | V |
| `Ore_Cobalt` | Minerai de cobalt |  | 10 | 600 | 6 or | fixé | V |
| `Ore_Cobalt_Shale` | Minerai de cobalt : schiste |  |  | 600 | 6 or | fixé | V |
| `Ore_Cobalt_Slate` | Minerai de cobalt : ardoise |  |  | 600 | 6 or | fixé | V |
| `Ore_Cobalt_Slate_Cracked` | Ardoise fissurée |  | 10 | 600 | 6 or | fixé | V |
| `Ore_Copper` | Minerai de cuivre |  | 10 | 60 | 6 argent | fixé | V |
| `Ore_Copper_Sandstone` | Minerai de cuivre : grès |  |  | 60 | 6 argent | fixé | V |
| `Ore_Copper_Shale` | Minerai de cuivre : schiste |  |  | 60 | 6 argent | fixé | V |
| `Ore_Copper_Stone` | Minerai de cuivre : roche |  |  | 60 | 6 argent | fixé | V |
| `Ore_Divin` | Minerai Divin | Divin | 800 | 385 000 | 3 astrales 8 zéniths 5 diamants | fixé | E |
| `Ore_Divin_Basalt` | Minerai Divin (Basalte) | Divin | 800 | 385 000 | 3 astrales 8 zéniths 5 diamants | fixé | E |
| `Ore_Divin_Basalt_Cracked` | Roche Fissurée Divine (Basalte) | Divin | 800 | 385 000 | 3 astrales 8 zéniths 5 diamants | fixé | E |
| `Ore_Divin_Magma` | Minerai Divin (Magma Refroidi) | Divin | 800 | 385 000 | 3 astrales 8 zéniths 5 diamants | fixé | E |
| `Ore_Divin_Magma_Cracked` | Roche Fissurée Divine (Magma Refroidi) | Divin | 800 | 385 000 | 3 astrales 8 zéniths 5 diamants | fixé | E |
| `Ore_Divin_Shale` | Minerai Divin (Schiste) | Divin | 800 | 385 000 | 3 astrales 8 zéniths 5 diamants | fixé | E |
| `Ore_Divin_Shale_Cracked` | Roche Fissurée Divine (Schiste) | Divin | 800 | 385 000 | 3 astrales 8 zéniths 5 diamants | fixé | E |
| `Ore_Divin_Slate` | Minerai Divin (Ardoise) | Divin | 800 | 385 000 | 3 astrales 8 zéniths 5 diamants | fixé | E |
| `Ore_Divin_Slate_Cracked` | Roche Fissurée Divine (Ardoise) | Divin | 800 | 385 000 | 3 astrales 8 zéniths 5 diamants | fixé | E |
| `Ore_Divin_Stone` | Minerai Divin (Pierre) | Divin | 800 | 385 000 | 3 astrales 8 zéniths 5 diamants | fixé | E |
| `Ore_Divin_Stone_Cracked` | Roche Fissurée Divine (Pierre) | Divin | 800 | 385 000 | 3 astrales 8 zéniths 5 diamants | fixé | E |
| `Ore_Divin_Volcanic` | Minerai Divin (Roche Volcanique) | Divin | 800 | 385 000 | 3 astrales 8 zéniths 5 diamants | fixé | E |
| `Ore_Divin_Volcanic_Cracked` | Roche Fissurée Divine (Roche Volcanique) | Divin | 800 | 385 000 | 3 astrales 8 zéniths 5 diamants | fixé | E |
| `Ore_Gold` | Minerai d’or |  | 10 | 500 | 5 or | fixé | V |
| `Ore_Gold_Basalt` | Minerai d’or : basalte |  |  | 500 | 5 or | fixé | V |
| `Ore_Gold_Calcite` | Minerai d’or : calcite |  |  | 500 | 5 or | fixé | V |
| `Ore_Gold_Sandstone` | Minerai d’or : grès |  |  | 500 | 5 or | fixé | V |
| `Ore_Gold_Shale` | Minerai d’or : schiste |  |  | 500 | 5 or | fixé | V |
| `Ore_Gold_Stone` | Minerai d’or : roche |  |  | 500 | 5 or | fixé | V |
| `Ore_Gold_Volcanic` | Minerai d’or : roche volcanique |  |  | 500 | 5 or | fixé | V |
| `Ore_Iron` | Minerai de fer |  | 10 | 150 | 1 or 5 argent | fixé | V |
| `Ore_Iron_Basalt` | Minerai de fer : basalte |  |  | 150 | 1 or 5 argent | fixé | V |
| `Ore_Iron_Basalt_Cracked` | Basalte fissuré |  | 10 | 150 | 1 or 5 argent | fixé | V |
| `Ore_Iron_Sandstone` | Minerai de fer : grès |  |  | 150 | 1 or 5 argent | fixé | V |
| `Ore_Iron_Shale` | Minerai de fer : schiste |  |  | 150 | 1 or 5 argent | fixé | V |
| `Ore_Iron_Slate` | Minerai de fer : ardoise |  |  | 150 | 1 or 5 argent | fixé | V |
| `Ore_Iron_Stone` | Minerai de fer : roche |  |  | 150 | 1 or 5 argent | fixé | V |
| `Ore_Iron_Volcanic` | Minerai de fer : roche volcanique |  |  | 150 | 1 or 5 argent | fixé | V |
| `Ore_Mithril` | Minerai de mithril |  | 10 | 1 500 | 1 diamant 5 or | fixé | V |
| `Ore_Mithril_Stone` | Minerai de mithril : roche |  |  | 1 500 | 1 diamant 5 or | fixé | V |
| `Ore_Onyxium` | Minerai d’onyxium |  | 10 | 2 500 | 2 diamants 5 or | fixé | V |
| `Ore_Prisma` | Minerai prismatique |  | 10 | 4 000 | 4 diamants | fixé | V |
| `Ore_Silver` | Minerai d’argent |  | 10 | 300 | 3 or | fixé | V |
| `Ore_Silver_Basalt` | Minerai d’argent : basalte |  |  | 300 | 3 or | fixé | V |
| `Ore_Silver_Sandstone` | Minerai d’argent : grès |  |  | 300 | 3 or | fixé | V |
| `Ore_Silver_Shale` | Minerai d’argent : schiste |  |  | 300 | 3 or | fixé | V |
| `Ore_Silver_Slate` | Minerai d’argent : ardoise |  |  | 300 | 3 or | fixé | V |
| `Ore_Silver_Stone` | Minerai d’argent : roche |  |  | 300 | 3 or | fixé | V |
| `Ore_Silver_Volcanic` | Minerai d’argent : roche volcanique |  |  | 300 | 3 or | fixé | V |
| `Ore_Thorium` | Minerai de thorium |  | 10 | 400 | 4 or | fixé | V |
| `Ore_Thorium_Mud` | Minerai de thorium : boue sèche |  |  | 400 | 4 or | fixé | V |
| `Ore_Thorium_Mud_Cracked` | Boue craquelée |  | 10 | 400 | 4 or | fixé | V |
| `Ore_Thorium_Sandstone` | Minerai de thorium : grès |  |  | 400 | 4 or | fixé | V |

### Avec recette — prix calculés (11)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O  Recette |
|---|---|---|---:|---:|---|---|---|---|
| `Ingredient_Bar_Adamantite` | Lingot d’adamantite |  |  | 1 300 | 1 diamant 3 or | calculé | V | 1× Minerai d’adamantite · Furnace |
| `Ingredient_Bar_Cobalt` | Lingot de cobalt |  |  | 780 | 7 or 8 argent | calculé | V | 1× Minerai de cobalt · Furnace |
| `Ingredient_Bar_Copper` | Lingot de cuivre |  |  | 78 | 7 argent 8 cuivres | calculé | V | 1× Minerai de cuivre · Furnace |
| `Ingredient_Bar_Divin` | Lingot Divin | Divin | 800 | 1 000 000 | 1 impériale | calculé | E | 2× Minerai Divin · Furnace |
| `Ingredient_Bar_Gold` | Lingot d’or |  |  | 650 | 6 or 5 argent | calculé | V | 1× Minerai d’or · Furnace |
| `Ingredient_Bar_Iron` | Lingot de fer |  |  | 200 | 2 or | calculé | V | 1× Minerai de fer · Furnace |
| `Ingredient_Bar_Mithril` | Lingot de mithril |  |  | 2 000 | 2 diamants | calculé | V | 1× Minerai de mithril · Furnace |
| `Ingredient_Bar_Onyxium` | Lingot d’onyxium |  |  | 3 200 | 3 diamants 2 or | calculé | V | 1× Minerai d’onyxium · Furnace |
| `Ingredient_Bar_Prisma` | Lingot prismatique |  |  | 5 200 | 5 diamants 2 or | calculé | V | 1× Minerai prismatique · Furnace |
| `Ingredient_Bar_Silver` | Lingot d’argent |  |  | 390 | 3 or 9 argent | calculé | V | 1× Minerai d’argent · Furnace |
| `Ingredient_Bar_Thorium` | Lingot de thorium |  |  | 520 | 5 or 2 argent | calculé | V | 1× Minerai de thorium · Furnace |

<a id="fam-bois"></a>
## Bois

### Sans recette — prix fixés (224)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Wood_Amber_Branch_Corner` | Branche d’ambre angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Amber_Branch_Long` | Branche d’ambre longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Amber_Branch_Short` | Branche d’ambre courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Amber_Roots` | Racines d’ambre |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Amber_Trunk` | Bûche d’ambre |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Amber_Trunk_Full` | Tronc d’ambre |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Apple_Branch_Corner` | Branche de pommier angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Apple_Branch_Long` | Branche de pommier longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Apple_Branch_Short` | Branche de pommier courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Apple_Roots` | Racines de pommier |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Apple_Trunk` | Bûche de pommier |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Apple_Trunk_Full` | Tronc de pommier |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Ash_Branch_Corner` | Branche de frêne angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Ash_Branch_Long` | Branche de frêne longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Ash_Branch_Short` | Branche de frêne courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Ash_Roots` | Racines de frêne |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Ash_Trunk` | Bûche de frêne |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Ash_Trunk_Full` | Tronc de frêne |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Aspen_Branch_Corner` | Branche de tremble angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Aspen_Branch_Long` | Branche de tremble longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Aspen_Branch_Short` | Branche de tremble courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Aspen_Roots` | Racines de tremble |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Aspen_Trunk` | Bûche de tremble |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Aspen_Trunk_Full` | Tronc de tremble |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Azure_Branch_Corner` | Branche azurée angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Azure_Branch_Long` | Branche azurée longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Azure_Branch_Short` | Branche azurée courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Azure_Roots` | Racines azurées |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Azure_Trunk` | Bûche azurée |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Azure_Trunk_Full` | Tronc azuré |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Bamboo_Branch_Corner` | Branche de bambou angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Bamboo_Branch_Corner_Deco` | Branche de bambou angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Bamboo_Branch_Long` | Branche de bambou longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Bamboo_Branch_Long_Deco` | Branche de bambou longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Bamboo_Branch_Short` | Branche de bambou courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Bamboo_Branch_Short_Deco` | Branche de bambou courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Bamboo_Roots` | Racines de bambou |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Bamboo_Roots_Deco` | Racines de bambou |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Bamboo_Trunk` | Bûche de bambou |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Bamboo_Trunk_Deco` | Bûche de bambou |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Bamboo_Trunk_Full` | Tronc de bambou |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Bamboo_Trunk_Full_Deco` | Tronc de bambou |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Banyan_Branch_Corner` | Branche de figuier des banians angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Banyan_Branch_Long` | Branche de figuier des banians longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Banyan_Branch_Short` | Branche de figuier des banians courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Banyan_Roots` | Racines de figuier des banians |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Banyan_Trunk` | Bûche de figuier des banians |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Banyan_Trunk_Full` | Tronc de figuier des banians |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Beech_Branch_Corner` | Branche de hêtre angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Beech_Branch_Long` | Branche de hêtre longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Beech_Branch_Short` | Branche de hêtre courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Beech_Roots` | Racines de hêtre |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Beech_Trunk` | Bûche de hêtre |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Beech_Trunk_Full` | Tronc de hêtre |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Birch_Branch_Corner` | Branche de bouleau angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Birch_Branch_Long` | Branche de bouleau longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Birch_Branch_Short` | Branche de bouleau courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Birch_Roots` | Racines de bouleau |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Birch_Trunk` | Bûche de bouleau |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Birch_Trunk_Full` | Tronc de bouleau |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Blackwood_Roof_Hollow` | Toit en bois noir : creux |  | 15 | 5 | 5 cuivres | fixé | V |
| `Wood_Bottletree_Branch_Corner` | Branche d’arbre bouteille angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Bottletree_Branch_Long` | Branche d’arbre bouteille longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Bottletree_Branch_Short` | Branche d’arbre bouteille courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Bottletree_Roots` | Racines d’arbre bouteille |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Bottletree_Trunk` | Bûche d’arbre bouteille |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Bottletree_Trunk_Full` | Tronc d’arbre bouteille |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Burnt_Branch_Corner` | Branche d’arbre brûlé angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Burnt_Branch_Long` | Branche d’arbre brûlé longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Burnt_Branch_Short` | Branche d’arbre brûlé courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Burnt_Roots` | Racines d’arbre brûlé |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Burnt_Trunk` | Bûche d’arbre brûlé |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Burnt_Trunk_Full` | Tronc d’arbre brûlé |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Camphor_Branch_Corner` | Branche de camphrier angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Camphor_Branch_Long` | Branche de camphrier longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Camphor_Branch_Short` | Branche de camphrier courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Camphor_Roots` | Racines de camphrier |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Camphor_Trunk` | Bûche de camphrier |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Camphor_Trunk_Full` | Tronc de camphrier |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Cedar_Branch_Corner` | Branche de cèdre angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Cedar_Branch_Long` | Branche de cèdre longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Cedar_Branch_Short` | Branche de cèdre courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Cedar_Roots` | Racines de cèdre |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Cedar_Trunk` | Bûche de cèdre |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Cedar_Trunk_Full` | Tronc de cèdre |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Crystal_Branch_Corner` | Branche d’arbre de cristal angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Crystal_Branch_Long` | Branche d’arbre de cristal longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Crystal_Branch_Short` | Branche d’arbre de cristal courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Crystal_Roots` | Racines d’arbre de cristal |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Crystal_Trunk` | Bûche d’arbre de cristal |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Crystal_Trunk_Full` | Tronc d’arbre de cristal |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Darkwood_Roof_Hollow` | Toit en bois sombre : creux |  | 15 | 5 | 5 cuivres | fixé | V |
| `Wood_Deadwood_Roof_Hollow` | Toit en bois mort : creux |  | 15 | 5 | 5 cuivres | fixé | V |
| `Wood_Dry_Branch_Corner` | Branche d’arbre sec angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Dry_Branch_Long` | Branche d’arbre sec longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Dry_Branch_Short` | Branche d’arbre sec courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Dry_Roots` | Racines d’arbre sec |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Dry_Trunk` | Bûche d’arbre sec |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Dry_Trunk_Full` | Tronc d’arbre sec |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Drywood_Roof_Hollow` | Toit en bois sec : creux |  | 15 | 5 | 5 cuivres | fixé | V |
| `Wood_Fig_Blue_Branch_Corner` | Branche de figuier bleu angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Fig_Blue_Branch_Long` | Branche de figuier bleu longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Fig_Blue_Branch_Short` | Branche de figuier bleu courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Fig_Blue_Roots` | Racines de figuier bleu |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Fig_Blue_Trunk` | Bûche de figuier bleu |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Fig_Blue_Trunk_Full` | Tronc de figuier bleu |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Fir_Branch_Corner` | Branche de sapin angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Fir_Branch_Long` | Branche de sapin longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Fir_Branch_Short` | Branche de sapin courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Fir_Roots` | Racines de sapin |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Fir_Trunk` | Bûche de sapin |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Fir_Trunk_Full` | Tronc de sapin |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Fire_Branch_Corner` | Branche d’arbre de feu angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Fire_Branch_Long` | Branche d’arbre de feu longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Fire_Branch_Short` | Branche d’arbre de feu courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Fire_Roots` | Racines d’arbre de feu |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Fire_Trunk` | Bûche d’arbre de feu |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Fire_Trunk_Full` | Tronc d’arbre de feu |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Gnarled_Roots` | Racines noueuses |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Goldenwood_Roof_Hollow` | Toit en bois doré : creux |  | 15 | 5 | 5 cuivres | fixé | V |
| `Wood_Greenwood_Roof_Hollow` | Toit en bois vert : creux |  | 15 | 5 | 5 cuivres | fixé | V |
| `Wood_Gumboab_Branch_Corner` | Branche d’arbre à gomme angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Gumboab_Branch_Long` | Branche d’arbre à gomme longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Gumboab_Branch_Short` | Branche d’arbre à gomme courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Gumboab_Roots` | Racines d’arbre à gomme |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Gumboab_Trunk` | Bûche d’arbre à gomme |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Gumboab_Trunk_Full` | Tronc d’arbre à gomme |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Hardwood_Roof_Hollow` | Toit en bois dur : creux |  | 15 | 5 | 5 cuivres | fixé | V |
| `Wood_Ice_Branch_Corner` | Branche d’arbre gelé angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Ice_Branch_Long` | Branche d’arbre gelé longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Ice_Branch_Short` | Branche d’arbre gelé courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Ice_Roots` | Racines d’arbre gelé |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Ice_Trunk` | Bûche d’arbre gelé |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Ice_Trunk_Full` | Tronc d’arbre gelé |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Jungle_Branch_Corner` | Branche d’arbre tropical angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Jungle_Branch_Long` | Branche d’arbre tropical longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Jungle_Branch_Short` | Branche d’arbre tropical courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Jungle_Roots` | Racines d’arbre tropical |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Jungle_Trunk` | Bûche d’arbre tropical |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Jungle_Trunk_Full` | Tronc d’arbre tropical |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Lightwood_Roof_Hollow` | Toit en bois clair : creux |  | 15 | 5 | 5 cuivres | fixé | V |
| `Wood_Maple_Branch_Corner` | Branche d’érable angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Maple_Branch_Long` | Branche d’érable longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Maple_Branch_Short` | Branche d’érable courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Maple_Roots` | Racines d’érable |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Maple_Trunk` | Bûche d’érable |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Maple_Trunk_Full` | Tronc d’érable |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Oak_Branch_Corner` | Branche de chêne angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Oak_Branch_Long` | Branche de chêne longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Oak_Branch_Short` | Branche de chêne courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Oak_Roots` | Racines de chêne |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Oak_Trunk` | Bûche de chêne |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Oak_Trunk_Full` | Tronc de chêne |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Palm_Branch_Corner` | Branche de palmier angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Palm_Branch_Long` | Branche de palmier longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Palm_Branch_Short` | Branche de palmier courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Palm_Roots` | Racines de palmier |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Palm_Trunk` | Bûche de palmier |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Palm_Trunk_Full` | Tronc de palmier |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Palo_Branch_Corner` | Branche de palo angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Palo_Branch_Long` | Branche de palo longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Palo_Branch_Short` | Branche de palo courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Palo_Roots` | Racines de palo |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Palo_Trunk` | Bûche de palo |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Palo_Trunk_Full` | Tronc de palo |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Petrified_Branch_Corner` | Branche d’arbre pétrifié angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Petrified_Branch_Long` | Branche d’arbre pétrifié longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Petrified_Branch_Short` | Branche d’arbre pétrifié courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Petrified_Roots` | Racines d’arbre pétrifié |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Petrified_Trunk` | Bûche d’arbre pétrifié |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Petrified_Trunk_Full` | Tronc d’arbre pétrifié |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Poisoned_Branch_Corner` | Branche d’arbre empoisonné angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Poisoned_Branch_Long` | Branche d’arbre empoisonné longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Poisoned_Branch_Short` | Branche d’arbre empoisonné courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Poisoned_Roots` | Racines d’arbre empoisonné |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Poisoned_Trunk` | Bûche d’arbre empoisonné |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Poisoned_Trunk_Full` | Tronc d’arbre empoisonné |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Redwood_Branch_Corner` | Branche de séquoia angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Redwood_Branch_Long` | Branche de séquoia longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Redwood_Branch_Short` | Branche de séquoia courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Redwood_Roof_Hollow` | Toit en bois rouge : creux |  | 15 | 5 | 5 cuivres | fixé | V |
| `Wood_Redwood_Roots` | Racines de séquoia |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Redwood_Trunk` | Bûche de séquoia |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Redwood_Trunk_Full` | Tronc de séquoia |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Sadwillow_Branch_Corner` | Branche de saule pleureur angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Sadwillow_Branch_Long` | Branche de saule pleureur longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Sadwillow_Branch_Short` | Branche de saule pleureur courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Sadwillow_Roots` | Racines de saule pleureur |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Sadwillow_Trunk` | Bûche de saule pleureur |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Sadwillow_Trunk_Full` | Tronc de saule pleureur |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Sallow_Branch_Corner` | Branche de saule cendré angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Sallow_Branch_Long` | Branche de saule cendré longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Sallow_Branch_Short` | Branche de saule cendré courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Sallow_Roots` | Racines de saule cendré |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Sallow_Trunk` | Bûche de saule cendré |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Sallow_Trunk_Full` | Tronc de saule cendré |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Softwood_Roof_Hollow` | Toit en bois tendre : creux |  | 15 | 5 | 5 cuivres | fixé | V |
| `Wood_Spiral_Branch_Corner` | Branche d’arbre spiralé angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Spiral_Branch_Long` | Branche d’arbre spiralé longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Spiral_Branch_Short` | Branche d’arbre spiralé courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Spiral_Roots` | Racines d’arbre spiralé |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Spiral_Trunk` | Bûche d’arbre spiralé |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Spiral_Trunk_Full` | Tronc d’arbre spiralé |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Stormbark_Branch_Corner` | Branche d’écorce des tempêtes angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Stormbark_Branch_Long` | Branche d’écorce des tempêtes longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Stormbark_Branch_Short` | Branche d’écorce des tempêtes courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Stormbark_Roots` | Racines d’écorce des tempêtes |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Stormbark_Trunk` | Bûche d’écorce des tempêtes |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Stormbark_Trunk_Full` | Tronc d’écorce des tempêtes |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Stripped_Deco` | Bûche écorcée |  |  | 5 | 5 cuivres | fixé | V |
| `Wood_Torch_Wall` | Torche en bois |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Tropicalwood_Roof_Hollow` | Toit en bois tropical : creux |  | 15 | 5 | 5 cuivres | fixé | V |
| `Wood_Windwillow_Branch_Corner` | Branche de saule-vent angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Windwillow_Branch_Long` | Branche de saule-vent longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Windwillow_Branch_Short` | Branche de saule-vent courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Windwillow_Roots` | Racines de saule-vent |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Windwillow_Trunk` | Bûche de saule-vent |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Windwillow_Trunk_Full` | Tronc de saule-vent |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Wisteria_Wild_Branch_Corner` | Branche de glycine sauvage angle |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Wisteria_Wild_Branch_Long` | Branche de glycine sauvage longue |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Wisteria_Wild_Branch_Short` | Branche de glycine sauvage courte |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Wisteria_Wild_Roots` | Racines de glycine sauvage |  | 10 | 5 | 5 cuivres | fixé | V |
| `Wood_Wisteria_Wild_Trunk` | Bûche de glycine sauvage |  | 10 | 20 | 2 argent | fixé | V |
| `Wood_Wisteria_Wild_Trunk_Full` | Tronc de glycine sauvage |  | 10 | 20 | 2 argent | fixé | V |

### Avec recette — prix calculés (219)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O  Recette |
|---|---|---|---:|---:|---|---|---|---|
| `Wood_Amber_Trunk_Half` | Dalle en bois d’ambre |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche d’ambre → ×2 · Builders |
| `Wood_Amber_Trunk_Stairs` | Escalier en bois d’ambre |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche d’ambre · Builders |
| `Wood_Apple_Trunk_Half` | Dalle en bois de pommier |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de pommier → ×2 · Builders |
| `Wood_Apple_Trunk_Stairs` | Escalier en bois de pommier |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de pommier · Builders |
| `Wood_Ash_Trunk_Half` | Dalle en bois de frêne |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de frêne → ×2 · Builders |
| `Wood_Ash_Trunk_Stairs` | Escalier en bois de frêne |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de frêne · Builders |
| `Wood_Aspen_Trunk_Half` | Dalle en bois de tremble |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de tremble → ×2 · Builders |
| `Wood_Aspen_Trunk_Stairs` | Escalier en bois de tremble |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de tremble · Builders |
| `Wood_Azure_Trunk_Half` | Dalle en bois azuré |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Wood_Azure] → ×2 · Builders |
| `Wood_Azure_Trunk_Stairs` | Escalier en bois azuré |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Wood_Azure] · Builders |
| `Wood_Bamboo_Trunk_Half` | Dalle en bois de bambou |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de bambou → ×2 · Builders |
| `Wood_Bamboo_Trunk_Half_Deco` | Dalle en bois de bambou |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de bambou → ×2 · Builders |
| `Wood_Bamboo_Trunk_Stairs` | Escalier en bois de bambou |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de bambou · Builders |
| `Wood_Bamboo_Trunk_Stairs_Deco` | Escalier en bois de bambou |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de bambou · Builders |
| `Wood_Banyan_Trunk_Half` | Dalle en bois de figuier des banians |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de figuier des banians → ×2 · Builders |
| `Wood_Banyan_Trunk_Stairs` | Escalier en bois de figuier des banians |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de figuier des banians · Builders |
| `Wood_Beech_Trunk_Half` | Dalle en bois de hêtre |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de hêtre → ×2 · Builders |
| `Wood_Beech_Trunk_Stairs` | Escalier en bois de hêtre |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de hêtre · Builders |
| `Wood_Birch_Trunk_Half` | Dalle en bois de bouleau |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de bouleau → ×2 · Builders |
| `Wood_Birch_Trunk_Stairs` | Escalier en bois de bouleau |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de bouleau · Builders |
| `Wood_Blackwood_Beam` | Poutre en bois noir |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois noir] → ×2 · Builders |
| `Wood_Blackwood_Decorative` | Planches en bois noir décoratives |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois noir] · Builders |
| `Wood_Blackwood_Fence` | Clôture en bois noir |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois noir] → ×2 · Builders |
| `Wood_Blackwood_Fence_Gate` | Portillon en bois noir |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois noir] · Builders |
| `Wood_Blackwood_Ornate` | Planches en bois noir ornées |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois noir] · Builders |
| `Wood_Blackwood_Planks` | Planches en bois noir |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois noir] · Builders |
| `Wood_Blackwood_Planks_Half` | Dalle en bois noir |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois noir] → ×2 · Builders |
| `Wood_Blackwood_Roof` | Toit en bois noir |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois noir] · Builders |
| `Wood_Blackwood_Roof_Flat` | Toit en bois noir : plat |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois noir] · Builders |
| `Wood_Blackwood_Roof_Shallow` | Toit en bois noir : faible pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois noir] · Builders |
| `Wood_Blackwood_Roof_Steep` | Toit en bois noir : forte pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois noir] · Builders |
| `Wood_Blackwood_Stairs` | Escalier en bois noir |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois noir] · Builders |
| `Wood_Bottletree_Trunk_Half` | Dalle en bois d’arbre bouteille |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche d’arbre bouteille → ×2 · Builders |
| `Wood_Bottletree_Trunk_Stairs` | Escalier en bois d’arbre bouteille |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche d’arbre bouteille · Builders |
| `Wood_Burnt_Trunk_Half` | Dalle en bois brûlé |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche d’arbre brûlé → ×2 · Builders |
| `Wood_Burnt_Trunk_Stairs` | Escalier en bois brûlé |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche d’arbre brûlé · Builders |
| `Wood_Camphor_Trunk_Half` | Dalle en bois de camphrier |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de camphrier → ×2 · Builders |
| `Wood_Camphor_Trunk_Stairs` | Escalier en bois de camphrier |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de camphrier · Builders |
| `Wood_Cedar_Trunk_Half` | Dalle en bois de cèdre |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de cèdre → ×2 · Builders |
| `Wood_Cedar_Trunk_Stairs` | Escalier en bois de cèdre |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de cèdre · Builders |
| `Wood_Crystal_Trunk_Half` | Dalle en bois d’arbre de cristal |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Wood_Crystal] → ×2 · Builders |
| `Wood_Crystal_Trunk_Stairs` | Escalier en bois d’arbre de cristal |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Wood_Crystal] · Builders |
| `Wood_Darkwood_Beam` | Poutre en bois sombre |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois sombre] → ×2 · Builders |
| `Wood_Darkwood_Decorative` | Planches en bois sombre décoratives |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois sombre] · Builders |
| `Wood_Darkwood_Fence` | Clôture en bois sombre |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois sombre] → ×2 · Builders |
| `Wood_Darkwood_Fence_Gate` | Portillon en bois sombre |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sombre] · Builders |
| `Wood_Darkwood_Ornate` | Planches en bois sombre ornées |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois sombre] · Builders |
| `Wood_Darkwood_Planks` | Planches en bois sombre |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois sombre] · Builders |
| `Wood_Darkwood_Planks_Half` | Dalle en bois sombre |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois sombre] → ×2 · Builders |
| `Wood_Darkwood_Roof` | Toit en bois sombre |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sombre] · Builders |
| `Wood_Darkwood_Roof_Flat` | Toit en bois sombre : plat |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sombre] · Builders |
| `Wood_Darkwood_Roof_Shallow` | Toit en bois sombre : faible pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sombre] · Builders |
| `Wood_Darkwood_Roof_Steep` | Toit en bois sombre : forte pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sombre] · Builders |
| `Wood_Darkwood_Stairs` | Escalier en bois sombre |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sombre] · Builders |
| `Wood_Deadwood_Beam` | Poutre en bois mort |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois mort] → ×2 · Builders |
| `Wood_Deadwood_Decorative` | Planches en bois mort décoratives |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois mort] · Builders |
| `Wood_Deadwood_Fence` | Clôture en bois mort |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois mort] → ×2 · Builders |
| `Wood_Deadwood_Fence_Gate` | Portillon en bois mort |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois mort] · Builders |
| `Wood_Deadwood_Ornate` | Planches en bois mort ornées |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois mort] · Builders |
| `Wood_Deadwood_Planks` | Planches en bois mort |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois mort] · Builders |
| `Wood_Deadwood_Planks_Half` | Dalle en bois mort |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois mort] → ×2 · Builders |
| `Wood_Deadwood_Roof` | Toit en bois mort |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois mort] · Builders |
| `Wood_Deadwood_Roof_Flat` | Toit en bois mort : plat |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois mort] · Builders |
| `Wood_Deadwood_Roof_Shallow` | Toit en bois mort : faible pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois mort] · Builders |
| `Wood_Deadwood_Roof_Steep` | Toit en bois mort : forte pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois mort] · Builders |
| `Wood_Deadwood_Stairs` | Escalier en bois mort |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois mort] · Builders |
| `Wood_Dry_Trunk_Half` | Dalle en bois sec |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche d’arbre sec → ×2 · Builders |
| `Wood_Dry_Trunk_Stairs` | Escalier en bois sec |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche d’arbre sec · Builders |
| `Wood_Drywood_Beam` | Poutre en bois sec |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois sec] → ×2 · Builders |
| `Wood_Drywood_Decorative` | Planches en bois sec décoratives |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois sec] · Builders |
| `Wood_Drywood_Fence` | Clôture en bois sec |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois sec] → ×2 · Builders |
| `Wood_Drywood_Fence_Gate` | Portillon en bois sec |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sec] · Builders |
| `Wood_Drywood_Ornate` | Planches en bois sec ornées |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois sec] · Builders |
| `Wood_Drywood_Planks` | Planches en bois sec |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois sec] · Builders |
| `Wood_Drywood_Planks_Half` | Dalle en bois sec |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois sec] → ×2 · Builders |
| `Wood_Drywood_Roof` | Toit en bois sec |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sec] · Builders |
| `Wood_Drywood_Roof_Flat` | Toit en bois sec : plat |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sec] · Builders |
| `Wood_Drywood_Roof_Shallow` | Toit en bois sec : faible pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sec] · Builders |
| `Wood_Drywood_Roof_Steep` | Toit en bois sec : forte pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sec] · Builders |
| `Wood_Drywood_Stairs` | Escalier en bois sec |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sec] · Builders |
| `Wood_Fig_Blue_Trunk_Half` | Dalle en bois de figuier bleu |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de figuier bleu → ×2 · Builders |
| `Wood_Fig_Blue_Trunk_Stairs` | Escalier en bois de figuier bleu |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de figuier bleu · Builders |
| `Wood_Fir_Trunk_Half` | Dalle en bois de sapin |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de sapin → ×2 · Builders |
| `Wood_Fir_Trunk_Stairs` | Escalier en bois de sapin |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de sapin · Builders |
| `Wood_Fire_Trunk_Half` | Dalle en bois d’arbre de feu |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche d’arbre de feu → ×2 · Builders |
| `Wood_Fire_Trunk_Stairs` | Escalier en bois d’arbre de feu |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche d’arbre de feu · Builders |
| `Wood_Goldenwood_Beam` | Poutre en bois doré |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois doré] → ×2 · Builders |
| `Wood_Goldenwood_Decorative` | Planches en bois doré décoratives |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois doré] · Builders |
| `Wood_Goldenwood_Fence` | Clôture en bois doré |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois doré] → ×2 · Builders |
| `Wood_Goldenwood_Fence_Gate` | Portillon en bois doré |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois doré] · Builders |
| `Wood_Goldenwood_Ornate` | Planches en bois doré ornées |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois doré] · Builders |
| `Wood_Goldenwood_Planks` | Planches en bois doré |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois doré] · Builders |
| `Wood_Goldenwood_Planks_Half` | Dalle en bois doré |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois doré] → ×2 · Builders |
| `Wood_Goldenwood_Roof` | Toit en bois doré |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois doré] · Builders |
| `Wood_Goldenwood_Roof_Flat` | Toit en bois doré : plat |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois doré] · Builders |
| `Wood_Goldenwood_Roof_Shallow` | Toit en bois doré : faible pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois doré] · Builders |
| `Wood_Goldenwood_Roof_Steep` | Toit en bois doré : forte pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois doré] · Builders |
| `Wood_Goldenwood_Stairs` | Escalier en bois doré |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois doré] · Builders |
| `Wood_Greenwood_Beam` | Poutre en bois vert |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois vert] → ×2 · Builders |
| `Wood_Greenwood_Decorative` | Planches en bois vert décoratives |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois vert] · Builders |
| `Wood_Greenwood_Fence` | Clôture en bois vert |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois vert] → ×2 · Builders |
| `Wood_Greenwood_Fence_Gate` | Portillon en bois vert |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois vert] · Builders |
| `Wood_Greenwood_Ornate` | Planches en bois vert ornées |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois vert] · Builders |
| `Wood_Greenwood_Planks` | Planches en bois vert |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois vert] · Builders |
| `Wood_Greenwood_Planks_Half` | Dalle en bois vert |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois vert] → ×2 · Builders |
| `Wood_Greenwood_Roof` | Toit en bois vert |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois vert] · Builders |
| `Wood_Greenwood_Roof_Flat` | Toit en bois vert : plat |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois vert] · Builders |
| `Wood_Greenwood_Roof_Shallow` | Toit en bois vert : faible pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois vert] · Builders |
| `Wood_Greenwood_Roof_Steep` | Toit en bois vert : forte pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois vert] · Builders |
| `Wood_Greenwood_Stairs` | Escalier en bois vert |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois vert] · Builders |
| `Wood_Gumboab_Trunk_Half` | Dalle en bois d’arbre à gomme |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche d’arbre à gomme → ×2 · Builders |
| `Wood_Gumboab_Trunk_Stairs` | Escalier en bois d’arbre à gomme |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche d’arbre à gomme · Builders |
| `Wood_Hardwood_Beam` | Poutre en bois dur |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Bois dur] → ×2 · Builders |
| `Wood_Hardwood_Decorative` | Planches en bois dur décoratives |  | 15 | 6 | 6 cuivres | calculé | V | 1× [Bûche de bois dur] · Builders |
| `Wood_Hardwood_Fence` | Clôture en bois dur |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Bois dur] → ×2 · Builders |
| `Wood_Hardwood_Fence_Gate` | Portillon en bois dur |  | 15 | 6 | 6 cuivres | calculé | V | 1× [Bois dur] · Builders |
| `Wood_Hardwood_Ornate` | Planches en bois dur ornées |  | 15 | 6 | 6 cuivres | calculé | V | 1× [Bûche de bois dur] · Builders |
| `Wood_Hardwood_Planks` | Planches en bois dur |  | 15 | 6 | 6 cuivres | calculé | V | 1× [Bûche de bois dur] · Builders |
| `Wood_Hardwood_Planks_Half` | Dalle en bois dur |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Bois dur] → ×2 · Builders |
| `Wood_Hardwood_Roof` | Toit en bois dur |  | 15 | 6 | 6 cuivres | calculé | V | 1× [Bois dur] · Builders |
| `Wood_Hardwood_Roof_Flat` | Toit en bois dur : plat |  | 15 | 6 | 6 cuivres | calculé | V | 1× [Bois dur] · Builders |
| `Wood_Hardwood_Roof_Shallow` | Toit en bois dur : faible pente |  | 15 | 6 | 6 cuivres | calculé | V | 1× [Bois dur] · Builders |
| `Wood_Hardwood_Roof_Steep` | Toit en bois dur : forte pente |  | 15 | 6 | 6 cuivres | calculé | V | 1× [Bois dur] · Builders |
| `Wood_Hardwood_Stairs` | Escalier en bois dur |  | 15 | 6 | 6 cuivres | calculé | V | 1× [Bois dur] · Builders |
| `Wood_Ice_Trunk_Half` | Dalle en bois d’arbre gelé |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois gelé] → ×2 · Builders |
| `Wood_Ice_Trunk_Stairs` | Escalier en bois d’arbre gelé |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois gelé] · Builders |
| `Wood_Jungle_Trunk_Half` | Dalle en bois d’arbre tropical |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche d’arbre tropical → ×2 · Builders |
| `Wood_Jungle_Trunk_Stairs` | Escalier en bois d’arbre tropical |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche d’arbre tropical · Builders |
| `Wood_Lightwood_Beam` | Poutre en bois clair |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois clair] → ×2 · Builders |
| `Wood_Lightwood_Decorative` | Planches en bois clair décoratives |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois clair] · Builders |
| `Wood_Lightwood_Fence` | Clôture en bois clair |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois clair] → ×2 · Builders |
| `Wood_Lightwood_Fence_Gate` | Portillon en bois clair |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois clair] · Builders |
| `Wood_Lightwood_Ornate` | Planches en bois clair ornées |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois clair] · Builders |
| `Wood_Lightwood_Planks` | Planches en bois clair |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois clair] · Builders |
| `Wood_Lightwood_Planks_Half` | Dalle en bois clair |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois clair] → ×2 · Builders |
| `Wood_Lightwood_Roof` | Toit en bois clair |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois clair] · Builders |
| `Wood_Lightwood_Roof_Flat` | Toit en bois clair : plat |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois clair] · Builders |
| `Wood_Lightwood_Roof_Shallow` | Toit en bois clair : faible pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois clair] · Builders |
| `Wood_Lightwood_Roof_Steep` | Toit en bois clair : forte pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois clair] · Builders |
| `Wood_Lightwood_Stairs` | Escalier en bois clair |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois clair] · Builders |
| `Wood_Maple_Trunk_Half` | Dalle en bois d’érable |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche d’érable → ×2 · Builders |
| `Wood_Maple_Trunk_Stairs` | Escalier en bois d’érable |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche d’érable · Builders |
| `Wood_Oak_Trunk_Half` | Dalle en bois de chêne |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de chêne → ×2 · Builders |
| `Wood_Oak_Trunk_Stairs` | Escalier en bois de chêne |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de chêne · Builders |
| `Wood_Palm_Trunk_Half` | Dalle en bois de palmier |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de palmier → ×2 · Builders |
| `Wood_Palm_Trunk_Stairs` | Escalier en bois de palmier |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de palmier · Builders |
| `Wood_Palo_Trunk_Half` | Dalle en bois de palo |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de palo → ×2 · Builders |
| `Wood_Palo_Trunk_Stairs` | Escalier en bois de palo |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de palo · Builders |
| `Wood_Petrified_Trunk_Half` | Dalle en bois d’arbre pétrifié |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche d’arbre pétrifié → ×2 · Builders |
| `Wood_Petrified_Trunk_Stairs` | Escalier en bois d’arbre pétrifié |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche d’arbre pétrifié · Builders |
| `Wood_Poisoned_Trunk_Half` | Dalle en bois d’arbre empoisonné |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Wood_Poisoned] → ×2 · Builders |
| `Wood_Poisoned_Trunk_Stairs` | Escalier en bois d’arbre empoisonné |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Wood_Poisoned] · Builders |
| `Wood_Redwood_Beam` | Poutre en bois rouge |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois rouge] → ×2 · Builders |
| `Wood_Redwood_Decorative` | Planches en bois rouge décoratives |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois rouge] · Builders |
| `Wood_Redwood_Fence` | Clôture en bois rouge |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois rouge] → ×2 · Builders |
| `Wood_Redwood_Fence_Gate` | Portillon en bois rouge |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois rouge] · Builders |
| `Wood_Redwood_Ornate` | Planches en bois rouge ornées |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois rouge] · Builders |
| `Wood_Redwood_Planks` | Planches en bois rouge |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois rouge] · Builders |
| `Wood_Redwood_Planks_Half` | Dalle en bois rouge |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois rouge] → ×2 · Builders |
| `Wood_Redwood_Roof` | Toit en bois rouge |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois rouge] · Builders |
| `Wood_Redwood_Roof_Flat` | Toit en bois rouge : plat |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois rouge] · Builders |
| `Wood_Redwood_Roof_Shallow` | Toit en bois rouge : faible pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois rouge] · Builders |
| `Wood_Redwood_Roof_Steep` | Toit en bois rouge : forte pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois rouge] · Builders |
| `Wood_Redwood_Stairs` | Escalier en bois rouge |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois rouge] · Builders |
| `Wood_Redwood_Trunk_Half` | Dalle en bois de séquoia |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de séquoia → ×2 · Builders |
| `Wood_Redwood_Trunk_Stairs` | Escalier en bois de séquoia |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de séquoia · Builders |
| `Wood_Sadwillow_Trunk_Half` | Dalle en bois de saule pleureur |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de saule pleureur → ×2 · Builders |
| `Wood_Sadwillow_Trunk_Stairs` | Escalier en bois de saule pleureur |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de saule pleureur · Builders |
| `Wood_Sallow_Trunk_Half` | Dalle en bois de saule cendré |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche de saule cendré → ×2 · Builders |
| `Wood_Sallow_Trunk_Stairs` | Escalier en bois de saule cendré |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche de saule cendré · Builders |
| `Wood_Softwood_Beam` | Poutre en bois tendre |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois tendre] → ×2 · Builders |
| `Wood_Softwood_Decorative` | Planches en bois tendre décoratives |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois tendre] · Builders |
| `Wood_Softwood_Fence` | Clôture en bois tendre |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois tendre] → ×2 · Builders |
| `Wood_Softwood_Fence_Gate` | Portillon en bois tendre |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois tendre] · Builders |
| `Wood_Softwood_Ornate` | Planches en bois tendre ornées |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois tendre] · Builders |
| `Wood_Softwood_Planks` | Planches en bois tendre |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois tendre] · Builders |
| `Wood_Softwood_Planks_Half` | Dalle en bois tendre |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois tendre] → ×2 · Builders |
| `Wood_Softwood_Roof` | Toit en bois tendre |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois tendre] · Builders |
| `Wood_Softwood_Roof_Flat` | Toit en bois tendre : plat |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois tendre] · Builders |
| `Wood_Softwood_Roof_Shallow` | Toit en bois tendre : faible pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois tendre] · Builders |
| `Wood_Softwood_Roof_Steep` | Toit en bois tendre : forte pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois tendre] · Builders |
| `Wood_Softwood_Stairs` | Escalier en bois tendre |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois tendre] · Builders |
| `Wood_Spiral_Trunk_Half` | Dalle en bois d’arbre spiralé |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Wood_Spiral] → ×2 · Builders |
| `Wood_Spiral_Trunk_Stairs` | Escalier en bois d’arbre spiralé |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Wood_Spiral] · Builders |
| `Wood_Sticks` | Tas de bâtons |  |  | 65 | 6 argent 5 cuivres | calculé | V | 5× Bâton · Farmingbench |
| `Wood_Stormbark_Trunk_Half` | Dalle en bois d’écorce des tempêtes |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Wood_Stormbark] → ×2 · Builders |
| `Wood_Stormbark_Trunk_Stairs` | Escalier en bois d’écorce des tempêtes |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Wood_Stormbark] · Builders |
| `Wood_Tropicalwood_Beam` | Poutre en bois tropical |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois tropical] → ×2 · Builders |
| `Wood_Tropicalwood_Decorative` | Planches en bois tropical décoratives |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois tropical] · Builders |
| `Wood_Tropicalwood_Fence` | Clôture en bois tropical |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois tropical] → ×2 · Builders |
| `Wood_Tropicalwood_Fence_Gate` | Portillon en bois tropical |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois tropical] · Builders |
| `Wood_Tropicalwood_Ornate` | Planches en bois tropical ornées |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois tropical] · Builders |
| `Wood_Tropicalwood_Planks` | Planches en bois tropical |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bûche de bois tropical] · Builders |
| `Wood_Tropicalwood_Planks_Half` | Dalle en bois tropical |  | 15 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois tropical] → ×2 · Builders |
| `Wood_Tropicalwood_Roof` | Toit en bois tropical |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois tropical] · Builders |
| `Wood_Tropicalwood_Roof_Flat` | Toit en bois tropical : plat |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois tropical] · Builders |
| `Wood_Tropicalwood_Roof_Shallow` | Toit en bois tropical : faible pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois tropical] · Builders |
| `Wood_Tropicalwood_Roof_Steep` | Toit en bois tropical : forte pente |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois tropical] · Builders |
| `Wood_Tropicalwood_Stairs` | Escalier en bois tropical |  | 15 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois tropical] · Builders |
| `Wood_Village_Wall_Black_Full` | Mur en bois noir |  | 15 | 72 | 7 argent 2 cuivres | calculé | V | 1× Argile noire lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Village_Wall_Blue_Full` | Mur en bois bleu |  | 15 | 65 | 6 argent 5 cuivres | calculé | V | 1× Argile bleue lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Village_Wall_Cyan_Full` | Mur en bois cyan |  | 15 | 65 | 6 argent 5 cuivres | calculé | V | 1× Argile cyan lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Village_Wall_Full` | Mur en bois : plein |  | 15 | 55 | 5 argent 5 cuivres | calculé | V | 1× Argile jaune lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Village_Wall_Green_Full` | Mur en bois vert |  | 15 | 65 | 6 argent 5 cuivres | calculé | V | 1× Argile verte lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Village_Wall_Grey_Full` | Mur en bois gris |  | 15 | 78 | 7 argent 8 cuivres | calculé | V | 1× Argile grise lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Village_Wall_GreyDark_Full` | Mur en bois gris foncé |  | 15 | 78 | 7 argent 8 cuivres | calculé | V | 1× Argile gris foncé lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Village_Wall_Lime_Full` | Mur en bois vert clair |  | 15 | 69 | 6 argent 9 cuivres | calculé | V | 1× Argile vert citron lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Village_Wall_Ocean_Full` | Mur en bois océanique |  | 15 | 65 | 6 argent 5 cuivres | calculé | V | 1× Argile verte lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Village_Wall_Orange_Full` | Mur en bois orange |  | 15 | 69 | 6 argent 9 cuivres | calculé | V | 1× Argile marron lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Village_Wall_Pink_Full` | Mur en bois rose |  | 15 | 65 | 6 argent 5 cuivres | calculé | V | 1× Argile rose lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Village_Wall_Purple_Full` | Mur en bois violet |  | 15 | 65 | 6 argent 5 cuivres | calculé | V | 1× Argile violette lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Village_Wall_Red_Full` | Mur en bois rouge |  | 15 | 65 | 6 argent 5 cuivres | calculé | V | 1× Argile rouge lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Village_Wall_RedDark_Full` | Mur en bois rouge foncé |  | 15 | 84 | 8 argent 4 cuivres | calculé | V | 1× Argile marron foncé lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Village_Wall_White_Full` | Mur en bois blanc |  | 15 | 59 | 5 argent 9 cuivres | calculé | V | 1× Argile blanche lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Village_Wall_Yellow_Full` | Mur en bois jaune |  | 15 | 55 | 5 argent 5 cuivres | calculé | V | 1× Argile orange lisse + 2× [Bois dur] → ×2 · Furniture_Bench |
| `Wood_Windwillow_Trunk_Half` | Dalle en bois de saule-vent |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Wood_Windwillow] → ×2 · Builders |
| `Wood_Windwillow_Trunk_Stairs` | Escalier en bois de saule-vent |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Wood_Windwillow] · Builders |
| `Wood_Wisteria_Wild_Trunk_Half` | Dalle en bois de glycine sauvage |  | 10 | 13 | 1 argent 3 cuivres | calculé | V | 1× [Wood_Wisteria_Wild] → ×2 · Builders |
| `Wood_Wisteria_Wild_Trunk_Stairs` | Escalier en bois de glycine sauvage |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 1× [Wood_Wisteria_Wild] · Builders |

<a id="fam-pierres-terres"></a>
## Pierres & terres

### Sans recette — prix fixés (274)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Rock_Aqua_Brick_Roof_Hollow` | Toit en brique marine : creux |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Aqua_Cobble` | Pavés marins |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Aqua_Cobble_Roof_Hollow` | Toit en pavés marins : creux |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Aqua_Stalactite_Large` | Grande stalactite marine |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Aqua_Stalactite_Small` | Petite stalactite marine |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Basalt_Brick_Roof_Hollow` | Toit en brique de basalte : creux |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Basalt_Cobble` | Pavés de basalte |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Basalt_Cobble_Roof_Hollow` | Toit en pavés de basalte : creux |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Basalt_Stalactite_Large` | Grande stalactite de basalte |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Basalt_Stalactite_Small` | Petite stalactite de basalte |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Bedrock` | Roche-mère |  |  | 2 | 2 cuivres | fixé | V |
| `Rock_Calcite` | Calcite |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Calcite_Brick_Roof_Hollow` | Toit en brique de calcite : creux |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Calcite_Cobble` | Pavés de calcite |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Calcite_Cobble_Roof_Hollow` | Toit en pavés de calcite : creux |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Calcite_Stalactite_Large` | Grande stalactite de calcite |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Calcite_Stalactite_Small` | Petite stalactite de calcite |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Chalk` | Craie |  |  | 2 | 2 cuivres | fixé | V |
| `Rock_Chalk_Brick` | Brique de craie |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Chalk_Brick_Beam` | Poutre en brique de craie |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Chalk_Brick_Decorative` | Brique de craie décorative |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Chalk_Brick_Roof_Hollow` | Toit en brique de craie : creux |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Chalk_Brick_Stairs` | Escalier en brique de craie |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Chalk_Cobble` | Pavés de craie |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Chalk_Cobble_Roof_Hollow` | Toit en pavés de craie : creux |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Chalk_Stalactite_Large` | Grande stalactite de craie |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Chalk_Stalactite_Small` | Petite stalactite de craie |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Gem_Diamond` | Diamant |  |  | 2 | 2 cuivres | fixé | V |
| `Rock_Gem_Emerald` | Émeraude |  |  | 2 | 2 cuivres | fixé | V |
| `Rock_Gem_Ruby` | Rubis |  |  | 2 | 2 cuivres | fixé | V |
| `Rock_Gem_Sapphire` | Saphir |  |  | 2 | 2 cuivres | fixé | V |
| `Rock_Gem_Topaz` | Topaze |  |  | 2 | 2 cuivres | fixé | V |
| `Rock_Gem_Voidstone` | Roche du Néant |  |  | 2 | 2 cuivres | fixé | V |
| `Rock_Gem_Zephyr` | Zéphyr |  |  | 2 | 2 cuivres | fixé | V |
| `Rock_Ice` | Glace |  |  | 2 | 2 cuivres | fixé | V |
| `Rock_Ice_Icicles` | Stalactites de glace |  |  | 2 | 2 cuivres | fixé | V |
| `Rock_Ice_Permafrost` | Glace bleue |  |  | 2 | 2 cuivres | fixé | V |
| `Rock_Ice_Stalactite_Large` | Grandes stalactites de glace |  |  | 2 | 2 cuivres | fixé | V |
| `Rock_Kenolite_Stalactite_Large` | Grande stalactite de kénolite |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Ledge_Brick_Beam` | Poutre en brique de pierre stratifiée |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Ledge_Cobble` | Pavés de pierre stratifiée |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Lime_Brick_Roof_Hollow` | Toit en brique de calcaire : creux |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Lime_Cobble` | Pavés de calcaire |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Lime_Cobble_Roof_Hollow` | Toit en pavés de calcaire : creux |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Lime_Stalactite_Large` | Grande stalactite de calcaire |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Lime_Stalactite_Small` | Petite stalactite de calcaire |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Magma_Cooled` | Magma refroidi |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Magma_Cooled_Brick_Roof_Hollow` | Toit en brique de magma refroidi : creux |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Magma_Cooled_Cobble` | Pavés de magma refroidi |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Magma_Cooled_Cobble_Roof_Hollow` | Toit en pavés de magma refroidi : creux |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Magma_Cooled_Half` | Dalle en magma refroidi |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Magma_Cooled_Stalactite_Large` | Grande stalactite de magma refroidi |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Magma_Cooled_Stalactite_Small` | Petite stalactite de magma refroidi |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Marble_Brick_Roof_Hollow` | Toit en brique de marbre : creux |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Marble_Cobble` | Pavés de marbre |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Marble_Cobble_Roof_Hollow` | Toit en pavés de marbre : creux |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Marble_Stalactite_Large` | Grande stalactite de marbre |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Marble_Stalactite_Small` | Petite stalactite de marbre |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Peach_Cobble` | Pavés de pierre pêche |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Quartzite_Brick_Roof_Hollow` | Toit en brique de quartzite : creux |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Quartzite_Cobble` | Pavés de quartzite |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Quartzite_Cobble_Roof_Hollow` | Toit en pavés de quartzite : creux |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Quartzite_Stalactite_Large` | Grande stalactite de quartzite |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Quartzite_Stalactite_Small` | Petite stalactite de quartzite |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Blue_Brick` | Brique runique bleue |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Blue_Brick_Beam` | Poutre en brique runique bleue |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Blue_Brick_Half` | Dalle en brique runique bleue |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Blue_Brick_Pillar_Base` | Pilier en brique runique bleue : base |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Blue_Brick_Pillar_Middle` | Pilier en brique runique bleue : milieu |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Blue_Brick_Pipe_Corner` | Tuyau en brique runique bleue : angle |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Blue_Brick_Pipe_Short` | Tuyau en brique runique bleue : court |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Blue_Brick_Stairs` | Escalier en brique runique bleue |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Blue_Brick_Wall` | Mur en brique runique bleue |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Brick` | Brique runique |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Brick_Half` | Dalle en brique runique |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Brick_Ornate` | Brique runique ornée |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Brick_Stairs` | Escalier en brique runique |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Cobble` | Pavés runiques |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Cobble_Beam` | Poutre en pavés runiques |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Cobble_Half` | Dalle en pavés runiques |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Cobble_Pillar_Base` | Pilier en pavés runiques : base |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Cobble_Pillar_Middle` | Pilier en pavés runiques : milieu |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Cobble_Stairs` | Escalier en pavés runiques |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Cobble_Wall` | Mur en pavés runiques |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Dark_Brick` | Brique runique sombre |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Dark_Brick_Beam` | Poutre en brique runique sombre |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Dark_Brick_Half` | Dalle en brique runique sombre |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Dark_Brick_Pillar_Base` | Pilier en brique runique sombre : base |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Dark_Brick_Pillar_Middle` | Pilier en brique runique sombre : milieu |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Dark_Brick_Stairs` | Escalier en brique runique sombre |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Dark_Brick_Wall` | Mur en brique runique sombre |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Teal_Brick` | Brique runique bleu foncé |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Teal_Brick_Beam` | Poutre en brique runique bleu foncé |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Teal_Brick_Half` | Dalle en brique runique bleu foncé |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Teal_Brick_Pillar_Base` | Pilier en brique runique bleu foncé : base |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Teal_Brick_Pillar_Middle` | Pilier en brique runique bleu foncé : milieu |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Teal_Brick_Pipe_Corner` | Tuyau en brique runique bleu foncé : angle |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Teal_Brick_Pipe_Short` | Tuyau en brique runique bleu foncé : court |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Teal_Brick_Stairs` | Escalier en brique runique bleu foncé |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Runic_Teal_Brick_Wall` | Mur en brique runique bleu foncé |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Salt` | Bloc de sel |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Sandstone_Brick_Roof_Hollow` | Toit en brique de grès : creux |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Sandstone_Cobble` | Pavés de grès |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Sandstone_Cobble_Roof_Hollow` | Toit en pavés de grès : creux |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Sandstone_Red_Brick_Roof_Hollow` | Toit en brique de grès rouge : creux |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Sandstone_Red_Cobble` | Pavés de grès rouge |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Sandstone_Red_Cobble_Roof_Hollow` | Toit en pavés de grès rouge : creux |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Sandstone_Red_Stalactite_Large` | Grande stalactite de grès rouge |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Sandstone_Red_Stalactite_Small` | Petite stalactite de grès rouge |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Sandstone_Stalactite_Large` | Grande stalactite de grès |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Sandstone_Stalactite_Small` | Petite stalactite de grès |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Sandstone_White_Brick_Roof_Hollow` | Toit en brique de grès blanc : creux |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Sandstone_White_Cobble` | Pavés de grès blanc |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Sandstone_White_Cobble_Roof_Hollow` | Toit en pavés de grès blanc : creux |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Sandstone_White_Stalactite_Large` | Grande stalactite de grès blanc |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Sandstone_White_Stalactite_Small` | Petite stalactite de grès blanc |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Shale_Brick_Roof_Hollow` | Toit en brique de schiste : creux |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Shale_Cobble` | Pavés de schiste |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Shale_Cobble_Corner` | Angle en pavés de schiste |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Shale_Cobble_Roof_Hollow` | Toit en pavés de schiste : creux |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Shale_Stalactites_Large` | Grande stalactite de schiste |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Shale_Stalactites_Small` | Petite stalactite de schiste |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Slate_Cobble` | Pavés d’ardoise |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Slate_Cobble_Corner` | Pavés d’ardoise : angle |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Slate_Cobble_Roof_Hollow` | Toit en pavés d’ardoise : creux |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Slate_Stalactite_Large` | Grande stalactite d’ardoise |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Slate_Stalactite_Small` | Petite stalactite d’ardoise |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Stone` | Pierre |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Stone_Brick_Roof_Hollow` | Toit en brique de pierre : creux |  | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Stone_Cobble` | Pavés de pierre |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Stone_Cobble_Corner` | Angle en pavés de pierre |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Stone_Cobble_Mossy` | Pavés de pierre moussus |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Stone_Cobble_Roof_Hollow` | Toit en pavés de pierre : creux |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Stone_Half` | Dalle en pierre |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Stone_Mossy` | Pierre moussue |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Stone_Stalactite_Large` | Grande stalactite de pierre |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Stone_Stalactite_Small` | Petite stalactite de pierre |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Volcanic_Brick_Roof_Hollow` | Toit en brique volcanique : creux | Rebut | 15 | 2 | 2 cuivres | fixé | V |
| `Rock_Volcanic_Cobble` | Pavés volcaniques | Rebut | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Volcanic_Cobble_Roof_Hollow` | Toit en pavés volcaniques : creux | Rebut | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Volcanic_Cracked_Incandescent` | Roche volcanique incandescente |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Volcanic_Cracked_Lava` | Roche volcanique fissurée |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Volcanic_Cracked_Poisoned` | Roche volcanique empoisonnée |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Volcanic_Half` | Dalle en roche volcanique |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Volcanic_Stalactite_Large` | Grande stalactite volcanique |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rock_Volcanic_Stalactite_Small` | Petite stalactite volcanique |  | 10 | 2 | 2 cuivres | fixé | V |
| `Rubble_Aqua` | Gravats marins |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Aqua_Medium` | Gravats marins moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Basalt` | Gravats de basalte |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Basalt_Medium` | Gravats de basalte moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Calcite` | Gravats de calcite |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Calcite_Medium` | Gravats de calcite moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Chalk` | Gravats de craie |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Chalk_Medium` | Gravats de craie moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Ice` | Morceaux de glace |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Ice_Medium` | Morceaux de glace moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Lime` | Gravats de calcaire |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Lime_Medium` | Gravats de calcaire moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Magma_Cooled` | Gravats de magma refroidi |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Magma_Cooled_Medium` | Gravats de magma refroidi moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Marble` | Gravats de marbre |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Marble_Medium` | Gravats de marbre moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Quartzite` | Gravats de quartzite |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Quartzite_Medium` | Gravats de quartzite moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Sandstone` | Gravats de grès |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Sandstone_Medium` | Gravats de grès moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Sandstone_Red` | Gravats de grès rouge |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Sandstone_Red_Medium` | Gravats de grès rouge moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Sandstone_White` | Gravats de grès blanc |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Sandstone_White_Medium` | Gravats de grès blanc moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Shale` | Gravats de schiste |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Shale_Medium` | Gravats de schiste moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Slate` | Gravats d’ardoise |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Slate_Medium` | Gravats d’ardoise moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Stone` | Gravats de pierre |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Stone_Medium` | Gravats de pierre moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Stone_Mossy` | Gravats de pierre moussue |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Stone_Mossy_Medium` | Gravats de pierre moussue moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Volcanic` | Gravats volcaniques |  |  | 2 | 2 cuivres | fixé | V |
| `Rubble_Volcanic_Medium` | Gravats volcaniques moyens |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Ash` | Sol cendré |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Beige` | Argile beige |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Black` | Argile noire |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Blue` | Argile bleue |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Cyan` | Argile cyan |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Green` | Argile verte |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Grey` | Argile grise |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_GreyLight` | Argile gris clair |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Lime` | Argile vert citron |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Ocean` | Argile océanique |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Ocean_Brick_Roof_Hollow` | Toit en brique d’argile océanique : creux |  | 15 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Ocean_Stalactite_Large` | Grande stalactite d’argile océanique |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Ocean_Stalactite_Small` | Petite stalactite d’argile océanique |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Orange` | Argile orange |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Pink` | Argile rose |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Purple` | Argile violette |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Red` | Argile rouge |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Scarlet` | Argile écarlate |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Black_Half` | Dalle en argile noire lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Black_Stairs` | Escalier en argile noire lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Blue_Half` | Dalle en argile bleue lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Blue_Stairs` | Escalier en argile bleue lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Cyan_Half` | Dalle en argile cyan lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Cyan_Stairs` | Escalier en argile cyan lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Green_Half` | Dalle en argile verte lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Green_Stairs` | Escalier en argile verte lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_GreenDark_Half` | Dalle en argile vert foncé lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_GreenDark_Stairs` | Escalier en argile vert foncé lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Grey2_Half` | Dalle en argile grise lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Grey2_Stairs` | Escalier en argile grise lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Grey_Half` | Dalle en argile gris foncé lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Grey_Stairs` | Escalier en argile gris foncé lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Lime_Half` | Dalle en argile vert clair lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Lime_Stairs` | Escalier en argile vert clair lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Orange_Half` | Dalle en argile marron lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Orange_Stairs` | Escalier en argile marron lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Pink_Half` | Dalle en argile rose lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Pink_Stairs` | Escalier en argile rose lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Purple_Half` | Dalle en argile violette lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Purple_Stairs` | Escalier en argile violette lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Red2_Half` | Dalle en argile rouge lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Red2_Stairs` | Escalier en argile rouge lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Red_Half` | Dalle en argile marron foncé lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Red_Stairs` | Escalier en argile marron foncé lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_White_Half` | Dalle en argile blanche lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_White_Stairs` | Escalier en argile blanche lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Yellow2_Half` | Dalle en argile orange lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Yellow2_Stairs` | Escalier en argile orange lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Yellow_Half` | Dalle en argile jaune lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Smooth_Yellow_Stairs` | Escalier en argile jaune lisse |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Stalactite_Large` | Grande stalactite d’argile |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Stalactite_Small` | Petite stalactite d’argile |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_White` | Argile blanche |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Clay_Yellow` | Argile jaune |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Dirt` | Terre |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Dirt_Burnt` | Terre brûlée |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Dirt_Burnt_Stairs` | Escalier en terre brûlée |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Dirt_Cold_Stairs` | Escalier en terre froide |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Dirt_Crystal` | Terre cristallisée |  |  | 2 | 2 cuivres | fixé | V |
| `Soil_Dirt_Dry_Stairs` | Escalier en terre sèche |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Dirt_Lush` | Terre luxuriante |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Dirt_Lush_Stairs` | Escalier en terre luxuriante |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Dirt_Stairs` | Escalier en terre |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Dirt_Tilled` | Terre labourée |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Dirt_Wet` | Terre mouillée |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Dirt_Wet_Stairs` | Escalier en terre mouillée |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Grass_Burnt_Full` | Herbe flétrie dense |  | 8 | 2 | 2 cuivres | fixé | V |
| `Soil_Grass_Cold_Full` | Herbe froide dense |  | 13 | 2 | 2 cuivres | fixé | V |
| `Soil_Grass_Deep_Full` | Herbe luxuriante dense |  | 5 | 2 | 2 cuivres | fixé | V |
| `Soil_Grass_Dry_Full` | Herbe sèche dense |  | 12 | 2 | 2 cuivres | fixé | V |
| `Soil_Grass_Sunny_Full` | Herbe ensoleillée dense |  | 6 | 2 | 2 cuivres | fixé | V |
| `Soil_Grass_Wet_Full` | Herbe mouillée dense |  | 14 | 2 | 2 cuivres | fixé | V |
| `Soil_Hive` | Sol de ruche |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Hive_Brick` | Brique de ruche |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Hive_Brick_Beam` | Poutre en brique de ruche |  | 15 | 2 | 2 cuivres | fixé | V |
| `Soil_Hive_Brick_Fence` | Clôture en brique de ruche |  | 15 | 2 | 2 cuivres | fixé | V |
| `Soil_Hive_Brick_Half` | Dalle en brique de ruche |  | 15 | 2 | 2 cuivres | fixé | V |
| `Soil_Hive_Brick_Smooth` | Brique de ruche lisse |  | 15 | 2 | 2 cuivres | fixé | V |
| `Soil_Hive_Brick_Stairs` | Escalier en brique de ruche |  | 15 | 2 | 2 cuivres | fixé | V |
| `Soil_Hive_Corrupted` | Sol de ruche corrompu |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Hive_Corrupted_Brick` | Brique de ruche corrompue |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Hive_Corrupted_Brick_Beam` | Poutre en brique de ruche corrompue |  | 15 | 2 | 2 cuivres | fixé | V |
| `Soil_Hive_Corrupted_Brick_Half` | Dalle en brique de ruche corrompue |  | 15 | 2 | 2 cuivres | fixé | V |
| `Soil_Hive_Corrupted_Brick_Smooth` | Brique de ruche corrompue lisse |  | 15 | 2 | 2 cuivres | fixé | V |
| `Soil_Hive_Corrupted_Brick_Stairs` | Escalier en brique de ruche corrompue |  | 15 | 2 | 2 cuivres | fixé | V |
| `Soil_Roots_Poisoned` | Racines empoisonnées |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Sand` | Sable |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Sand_Ashen` | Sable cendré |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Sand_Red` | Sable rouge |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Sand_White` | Sable blanc |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Seaweed_Block` | Bloc d’algues |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Snow` | Neige |  | 10 | 2 | 2 cuivres | fixé | V |
| `Soil_Snow_Half` | Dalle en neige |  | 10 | 2 | 2 cuivres | fixé | V |
| `Stalactite_Smoke_Floor` | Petite stalactite de pierre |  | 10 | 2 | 2 cuivres | fixé | V |

### Avec recette — prix calculés (584)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O  Recette |
|---|---|---|---:|---:|---|---|---|---|
| `Rock_Aqua` | Pierre marine |  | 10 | 5 | 5 cuivres | calculé | V | 2× [Pierre marine ou pavés marins] · Furnace |
| `Rock_Aqua_Beam` | Poutre en pierre marine |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre marine ou pavés marins] → ×2 · Builders |
| `Rock_Aqua_Brick` | Brique marine |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre marine ou pavés marins] · Builders |
| `Rock_Aqua_Brick_Beam` | Poutre en brique marine |  | 15 | 1 | 1 cuivre | calculé | V | 1× [Brique marine] → ×2 · Builders |
| `Rock_Aqua_Brick_Decorative` | Brique marine décorative |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre marine ou pavés marins] · Builders |
| `Rock_Aqua_Brick_Half` | Dalle en brique marine |  | 15 | 1 | 1 cuivre | calculé | V | 1× [Brique marine] → ×2 · Builders |
| `Rock_Aqua_Brick_Ornate` | Brique marine ornée |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre marine ou pavés marins] · Builders |
| `Rock_Aqua_Brick_Pillar_Base` | Pilier en brique marine : base |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Brique marine] · Builders |
| `Rock_Aqua_Brick_Pillar_Middle` | Pilier en brique marine : milieu |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Brique marine] · Builders |
| `Rock_Aqua_Brick_Roof` | Toit en brique marine |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Brique marine] · Builders |
| `Rock_Aqua_Brick_Roof_Flat` | Toit en brique marine : plat |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Brique marine] · Builders |
| `Rock_Aqua_Brick_Roof_Shallow` | Toit en brique marine : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Brique marine] · Builders |
| `Rock_Aqua_Brick_Roof_Steep` | Toit en brique marine : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Brique marine] · Builders |
| `Rock_Aqua_Brick_Smooth` | Brique marine lisse |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre marine ou pavés marins] · Builders |
| `Rock_Aqua_Brick_Smooth_Half` | Dalle en brique marine lisse |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique marine] → ×2 · Builders |
| `Rock_Aqua_Brick_Stairs` | Escalier en brique marine |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Brique marine] · Builders |
| `Rock_Aqua_Brick_Wall` | Mur en brique marine |  | 15 | 1 | 1 cuivre | calculé | V | 1× [Brique marine] → ×2 · Builders |
| `Rock_Aqua_Cobble_Beam` | Poutre en pavés marins |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre marine ou pavés marins] → ×2 · Builders |
| `Rock_Aqua_Cobble_Half` | Dalle en pavés marins |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre marine ou pavés marins] → ×2 · Builders |
| `Rock_Aqua_Cobble_Roof` | Toit en pavés marins |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Pierre marine ou pavés marins] · Builders |
| `Rock_Aqua_Cobble_Roof_Flat` | Toit en pavés marins : plat |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Pierre marine ou pavés marins] · Builders |
| `Rock_Aqua_Cobble_Roof_Shallow` | Toit en pavés marins : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre marine ou pavés marins] · Builders |
| `Rock_Aqua_Cobble_Roof_Steep` | Toit en pavés marins : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre marine ou pavés marins] · Builders |
| `Rock_Aqua_Cobble_Stairs` | Escalier en pavés marins |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Pierre marine ou pavés marins] · Builders |
| `Rock_Aqua_Cobble_Wall` | Mur en pavés marins |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre marine ou pavés marins] → ×2 · Builders |
| `Rock_Aqua_Half` | Dalle en pierre marine |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre marine ou pavés marins] → ×2 · Builders |
| `Rock_Aqua_Stairs` | Escalier en pierre marine |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Pierre marine ou pavés marins] · Builders |
| `Rock_Basalt` | Basalte |  | 10 | 5 | 5 cuivres | calculé | V | 2× [Basalte ou pavés de basalte] · Furnace |
| `Rock_Basalt_Beam` | Poutre en basalte |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Basalte ou pavés de basalte] → ×2 · Builders |
| `Rock_Basalt_Brick` | Brique de basalte |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Basalte ou pavés de basalte] · Builders |
| `Rock_Basalt_Brick_Beam` | Poutre en brique de basalte |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de basalte] → ×2 · Builders |
| `Rock_Basalt_Brick_Decorative` | Brique de basalte décorative |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Basalte ou pavés de basalte] · Builders |
| `Rock_Basalt_Brick_Half` | Dalle en brique de basalte |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de basalte] → ×2 · Builders |
| `Rock_Basalt_Brick_Ornate` | Brique de basalte ornée |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Basalte ou pavés de basalte] · Builders |
| `Rock_Basalt_Brick_Pillar_Base` | Pilier en brique de basalte : base |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de basalte] · Builders |
| `Rock_Basalt_Brick_Pillar_Middle` | Pilier en brique de basalte : milieu |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de basalte] · Builders |
| `Rock_Basalt_Brick_Roof` | Toit en brique de basalte |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de basalte] · Builders |
| `Rock_Basalt_Brick_Roof_Flat` | Toit en brique de basalte : plat |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de basalte] · Builders |
| `Rock_Basalt_Brick_Roof_Shallow` | Toit en brique de basalte : faible pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de basalte] · Builders |
| `Rock_Basalt_Brick_Roof_Steep` | Toit en brique de basalte : forte pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de basalte] · Builders |
| `Rock_Basalt_Brick_Smooth` | Brique de basalte lisse |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Basalte ou pavés de basalte] · Builders |
| `Rock_Basalt_Brick_Smooth_Half` | Dalle en brique de basalte lisse |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de basalte] → ×2 · Builders |
| `Rock_Basalt_Brick_Stairs` | Escalier en brique de basalte |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de basalte] · Builders |
| `Rock_Basalt_Brick_Wall` | Mur en brique de basalte |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de basalte] → ×2 · Builders |
| `Rock_Basalt_Cobble_Beam` | Poutre en pavés de basalte |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Basalte ou pavés de basalte] → ×2 · Builders |
| `Rock_Basalt_Cobble_Half` | Dalle en pavés de basalte |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Basalte ou pavés de basalte] → ×2 · Builders |
| `Rock_Basalt_Cobble_Roof` | Toit en pavés de basalte |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Basalte ou pavés de basalte] · Builders |
| `Rock_Basalt_Cobble_Roof_Flat` | Toit en pavés de basalte : plat |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Basalte ou pavés de basalte] · Builders |
| `Rock_Basalt_Cobble_Roof_Shallow` | Toit en pavés de basalte : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Basalte ou pavés de basalte] · Builders |
| `Rock_Basalt_Cobble_Roof_Steep` | Toit en pavés de basalte : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Basalte ou pavés de basalte] · Builders |
| `Rock_Basalt_Cobble_Stairs` | Escalier en pavés de basalte |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Basalte ou pavés de basalte] · Builders |
| `Rock_Basalt_Cobble_Wall` | Mur en pavés de basalte |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Basalte ou pavés de basalte] → ×2 · Builders |
| `Rock_Basalt_Half` | Dalle en basalte |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Basalte ou pavés de basalte] → ×2 · Builders |
| `Rock_Basalt_Stairs` | Escalier en basalte |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Basalte ou pavés de basalte] · Builders |
| `Rock_Calcite_Beam` | Poutre en calcite |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Calcite ou pavés de calcite] → ×2 · Builders |
| `Rock_Calcite_Brick` | Brique de calcite |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Calcite ou pavés de calcite] · Builders |
| `Rock_Calcite_Brick_Beam` | Poutre en brique de calcite |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de calcite] → ×2 · Builders |
| `Rock_Calcite_Brick_Decorative` | Brique de calcite décorative |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Calcite ou pavés de calcite] · Builders |
| `Rock_Calcite_Brick_Half` | Dalle en brique de calcite |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de calcite] → ×2 · Builders |
| `Rock_Calcite_Brick_Ornate` | Brique de calcite ornée |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Calcite ou pavés de calcite] · Builders |
| `Rock_Calcite_Brick_Pillar_Base` | Pilier en brique de calcite : base |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de calcite] · Builders |
| `Rock_Calcite_Brick_Pillar_Middle` | Pilier en brique de calcite : milieu |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de calcite] · Builders |
| `Rock_Calcite_Brick_Roof` | Toit en brique de calcite |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de calcite] · Builders |
| `Rock_Calcite_Brick_Roof_Flat` | Toit en brique de calcite : plat |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de calcite] · Builders |
| `Rock_Calcite_Brick_Roof_Shallow` | Toit en brique de calcite : faible pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de calcite] · Builders |
| `Rock_Calcite_Brick_Roof_Steep` | Toit en brique de calcite : forte pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de calcite] · Builders |
| `Rock_Calcite_Brick_Smooth` | Brique de calcite lisse |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Calcite ou pavés de calcite] · Builders |
| `Rock_Calcite_Brick_Smooth_Half` | Dalle en brique de calcite lisse |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de calcite] → ×2 · Builders |
| `Rock_Calcite_Brick_Stairs` | Escalier en brique de calcite |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de calcite] · Builders |
| `Rock_Calcite_Brick_Wall` | Mur en brique de calcite |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de calcite] → ×2 · Builders |
| `Rock_Calcite_Cobble_Beam` | Poutre en pavés de calcite |  | 15 | 1 | 1 cuivre | calculé | V | 1× [Calcite ou pavés de calcite] → ×2 · Builders |
| `Rock_Calcite_Cobble_Half` | Dalle en pavés de calcite |  | 15 | 1 | 1 cuivre | calculé | V | 1× [Calcite ou pavés de calcite] → ×2 · Builders |
| `Rock_Calcite_Cobble_Roof` | Toit en pavés de calcite |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Calcite ou pavés de calcite] · Builders |
| `Rock_Calcite_Cobble_Roof_Flat` | Toit en pavés de calcite : plat |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Calcite ou pavés de calcite] · Builders |
| `Rock_Calcite_Cobble_Roof_Shallow` | Toit en pavés de calcite : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Calcite ou pavés de calcite] · Builders |
| `Rock_Calcite_Cobble_Roof_Steep` | Toit en pavés de calcite : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Calcite ou pavés de calcite] · Builders |
| `Rock_Calcite_Cobble_Stairs` | Escalier en pavés de calcite |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Calcite ou pavés de calcite] · Builders |
| `Rock_Calcite_Cobble_Wall` | Mur en pavés de calcite |  | 15 | 1 | 1 cuivre | calculé | V | 1× [Calcite ou pavés de calcite] → ×2 · Builders |
| `Rock_Calcite_Half` | Dalle en calcite |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Calcite ou pavés de calcite] → ×2 · Builders |
| `Rock_Calcite_Stairs` | Escalier en calcite |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Calcite ou pavés de calcite] · Builders |
| `Rock_Chalk_Beam` | Poutre en craie |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Craie ou pavés de craie] → ×2 · Builders |
| `Rock_Chalk_Brick_Half` | Dalle en brique de craie |  | 15 | 1 | 1 cuivre | calculé | V | 1× [Brique de craie] → ×2 · Builders |
| `Rock_Chalk_Brick_Ornate` | Brique de craie ornée |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Craie ou pavés de craie] · Builders |
| `Rock_Chalk_Brick_Pillar_Base` | Pilier en brique de craie : base |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Brique de craie] · Builders |
| `Rock_Chalk_Brick_Pillar_Middle` | Pilier en brique de craie : milieu |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Brique de craie] · Builders |
| `Rock_Chalk_Brick_Roof` | Toit en brique de craie |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Brique de craie] · Builders |
| `Rock_Chalk_Brick_Roof_Flat` | Toit en brique de craie : plat |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Brique de craie] · Builders |
| `Rock_Chalk_Brick_Roof_Shallow` | Toit en brique de craie : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Brique de craie] · Builders |
| `Rock_Chalk_Brick_Roof_Steep` | Toit en brique de craie : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Brique de craie] · Builders |
| `Rock_Chalk_Brick_Smooth` | Brique de craie lisse |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Craie ou pavés de craie] · Builders |
| `Rock_Chalk_Brick_Smooth_Half` | Dalle en brique de craie lisse |  | 15 | 1 | 1 cuivre | calculé | V | 1× [Brique de craie] → ×2 · Builders |
| `Rock_Chalk_Brick_Wall` | Mur en brique de craie |  | 15 | 1 | 1 cuivre | calculé | V | 1× [Brique de craie] → ×2 · Builders |
| `Rock_Chalk_Cobble_Beam` | Poutre en pavés de craie |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Craie ou pavés de craie] → ×2 · Builders |
| `Rock_Chalk_Cobble_Half` | Dalle en pavés de craie |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Craie ou pavés de craie] → ×2 · Builders |
| `Rock_Chalk_Cobble_Roof` | Toit en pavés de craie |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Craie ou pavés de craie] · Builders |
| `Rock_Chalk_Cobble_Roof_Flat` | Toit en pavés de craie : plat |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Craie ou pavés de craie] · Builders |
| `Rock_Chalk_Cobble_Roof_Shallow` | Toit en pavés de craie : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Craie ou pavés de craie] · Builders |
| `Rock_Chalk_Cobble_Roof_Steep` | Toit en pavés de craie : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Craie ou pavés de craie] · Builders |
| `Rock_Chalk_Cobble_Stairs` | Escalier en pavés de craie |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Craie ou pavés de craie] · Builders |
| `Rock_Chalk_Cobble_Wall` | Mur en pavés de craie |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Craie ou pavés de craie] → ×2 · Builders |
| `Rock_Chalk_Half` | Dalle en craie |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Craie ou pavés de craie] → ×2 · Builders |
| `Rock_Chalk_Stairs` | Escalier en craie |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Craie ou pavés de craie] · Builders |
| `Rock_Crystal_Blue_Block` | Cristal bleu |  |  | 140 | 1 or 4 argent | calculé | V | 10× Fragments de cristal bleus · Builders |
| `Rock_Crystal_Blue_Large` | Grand cristal bleu |  |  | 86 | 8 argent 6 cuivres | calculé | V | 6× Fragments de cristal bleus · Builders |
| `Rock_Crystal_Blue_Medium` | Cristal bleu moyen |  |  | 57 | 5 argent 7 cuivres | calculé | V | 4× Fragments de cristal bleus · Builders |
| `Rock_Crystal_Blue_Small` | Petit cristal bleu |  |  | 29 | 2 argent 9 cuivres | calculé | V | 2× Fragments de cristal bleus · Builders |
| `Rock_Crystal_Cyan_Block` | Cristal cyan |  |  | 140 | 1 or 4 argent | calculé | V | 10× Fragments de cristal cyan · Builders |
| `Rock_Crystal_Cyan_Large` | Grand cristal cyan |  |  | 86 | 8 argent 6 cuivres | calculé | V | 6× Fragments de cristal cyan · Builders |
| `Rock_Crystal_Cyan_Medium` | Cristal cyan moyen |  |  | 57 | 5 argent 7 cuivres | calculé | V | 4× Fragments de cristal cyan · Builders |
| `Rock_Crystal_Cyan_Small` | Petit cristal cyan |  |  | 29 | 2 argent 9 cuivres | calculé | V | 2× Fragments de cristal cyan · Builders |
| `Rock_Crystal_Green_Block` | Cristal vert |  |  | 140 | 1 or 4 argent | calculé | V | 10× Fragments de cristal verts · Builders |
| `Rock_Crystal_Green_Large` | Grand cristal vert |  |  | 86 | 8 argent 6 cuivres | calculé | V | 6× Fragments de cristal verts · Builders |
| `Rock_Crystal_Green_Medium` | Cristal vert moyen |  |  | 57 | 5 argent 7 cuivres | calculé | V | 4× Fragments de cristal verts · Builders |
| `Rock_Crystal_Green_Small` | Petit cristal vert |  |  | 29 | 2 argent 9 cuivres | calculé | V | 2× Fragments de cristal verts · Builders |
| `Rock_Crystal_Pink_Block` | Cristal rose |  |  | 140 | 1 or 4 argent | calculé | V | 10× Fragments de cristal roses · Builders |
| `Rock_Crystal_Pink_Large` | Grand cristal rose |  |  | 86 | 8 argent 6 cuivres | calculé | V | 6× Fragments de cristal roses · Builders |
| `Rock_Crystal_Pink_Medium` | Cristal rose moyen |  |  | 57 | 5 argent 7 cuivres | calculé | V | 4× Fragments de cristal roses · Builders |
| `Rock_Crystal_Pink_Small` | Petit cristal rose |  |  | 29 | 2 argent 9 cuivres | calculé | V | 2× Fragments de cristal roses · Builders |
| `Rock_Crystal_Purple_Block` | Cristal violet |  |  | 140 | 1 or 4 argent | calculé | V | 10× Fragments de cristal violets · Builders |
| `Rock_Crystal_Purple_Large` | Grand cristal violet |  |  | 86 | 8 argent 6 cuivres | calculé | V | 6× Fragments de cristal violets · Builders |
| `Rock_Crystal_Purple_Medium` | Cristal violet moyen |  |  | 57 | 5 argent 7 cuivres | calculé | V | 4× Fragments de cristal violets · Builders |
| `Rock_Crystal_Purple_Small` | Petit cristal violet |  |  | 29 | 2 argent 9 cuivres | calculé | V | 2× Fragments de cristal violets · Builders |
| `Rock_Crystal_Red_Block` | Cristal rouge |  |  | 140 | 1 or 4 argent | calculé | V | 10× Fragments de cristal rouges · Builders |
| `Rock_Crystal_Red_Large` | Grand cristal rouge |  |  | 86 | 8 argent 6 cuivres | calculé | V | 6× Fragments de cristal rouges · Builders |
| `Rock_Crystal_Red_Medium` | Cristal rouge moyen |  |  | 57 | 5 argent 7 cuivres | calculé | V | 4× Fragments de cristal rouges · Builders |
| `Rock_Crystal_Red_Small` | Petit cristal rouge |  |  | 29 | 2 argent 9 cuivres | calculé | V | 2× Fragments de cristal rouges · Builders |
| `Rock_Crystal_Void_Block` | Cristal du Néant |  | 25 | 220 | 2 or 2 argent | calculé | V | 5× Fragments de cristal du Néant · GoblinLab_Workbench |
| `Rock_Crystal_Void_Large` | Grand cristal du Néant |  | 24 | 130 | 1 or 3 argent | calculé | V | 3× Fragments de cristal du Néant · GoblinLab_Workbench |
| `Rock_Crystal_Void_Medium` | Cristal du Néant moyen |  | 23 | 88 | 8 argent 8 cuivres | calculé | V | 2× Fragments de cristal du Néant · GoblinLab_Workbench |
| `Rock_Crystal_Void_Small` | Petit cristal du Néant |  | 22 | 44 | 4 argent 4 cuivres | calculé | V | 1× Fragments de cristal du Néant · GoblinLab_Workbench |
| `Rock_Crystal_White_Block` | Cristal blanc |  |  | 140 | 1 or 4 argent | calculé | V | 10× Fragments de cristal blancs · Builders |
| `Rock_Crystal_White_Large` | Grand cristal blanc |  |  | 86 | 8 argent 6 cuivres | calculé | V | 6× Fragments de cristal blancs · Builders |
| `Rock_Crystal_White_Medium` | Cristal blanc moyen |  |  | 57 | 5 argent 7 cuivres | calculé | V | 4× Fragments de cristal blancs · Builders |
| `Rock_Crystal_White_Small` | Petit cristal blanc |  |  | 29 | 2 argent 9 cuivres | calculé | V | 2× Fragments de cristal blancs · Builders |
| `Rock_Crystal_Yellow_Block` | Cristal jaune |  |  | 140 | 1 or 4 argent | calculé | V | 10× Fragments de cristal jaunes · Builders |
| `Rock_Crystal_Yellow_Large` | Grand cristal jaune |  |  | 86 | 8 argent 6 cuivres | calculé | V | 6× Fragments de cristal jaunes · Builders |
| `Rock_Crystal_Yellow_Medium` | Cristal jaune moyen |  |  | 57 | 5 argent 7 cuivres | calculé | V | 4× Fragments de cristal jaunes · Builders |
| `Rock_Crystal_Yellow_Small` | Petit cristal jaune |  |  | 29 | 2 argent 9 cuivres | calculé | V | 2× Fragments de cristal jaunes · Builders |
| `Rock_Gold_Brick` | Brique d’or |  | 15 | 840 | 8 or 4 argent | calculé | V | 1× Lingot d’or · Builders |
| `Rock_Gold_Brick_Beam` | Poutre en brique d’or |  | 15 | 550 | 5 or 5 argent | calculé | V | 1× [Brique d’or] → ×2 · Builders |
| `Rock_Gold_Brick_Decorative` | Brique d’or décorative |  | 15 | 1 100 | 1 diamant 1 or | calculé | V | 1× [Brique d’or] · Builders |
| `Rock_Gold_Brick_Half` | Dalle en brique d’or |  | 15 | 550 | 5 or 5 argent | calculé | V | 1× [Brique d’or] → ×2 · Builders |
| `Rock_Gold_Brick_Ornate` | Brique d’or ornée |  | 15 | 1 100 | 1 diamant 1 or | calculé | V | 1× [Brique d’or] · Builders |
| `Rock_Gold_Brick_Pillar_Base` | Pilier en brique d’or : base |  | 15 | 1 100 | 1 diamant 1 or | calculé | V | 1× [Brique d’or] · Builders |
| `Rock_Gold_Brick_Pillar_Middle` | Pilier en brique d’or : milieu |  | 15 | 1 100 | 1 diamant 1 or | calculé | V | 1× [Brique d’or] · Builders |
| `Rock_Gold_Brick_Roof` | Toit en brique d’or |  | 15 | 1 100 | 1 diamant 1 or | calculé | V | 1× [Brique d’or] · Builders |
| `Rock_Gold_Brick_Roof_Flat` | Toit en brique d’or : plat |  | 15 | 1 100 | 1 diamant 1 or | calculé | V | 1× [Brique d’or] · Builders |
| `Rock_Gold_Brick_Roof_Hollow` | Toit en brique d’or : creux |  | 15 | 1 100 | 1 diamant 1 or | calculé | V | 1× [Brique d’or] · Builders |
| `Rock_Gold_Brick_Roof_Shallow` | Toit en brique d’or : faible pente |  | 15 | 1 100 | 1 diamant 1 or | calculé | V | 1× [Brique d’or] · Builders |
| `Rock_Gold_Brick_Roof_Steep` | Toit en brique d’or : forte pente |  | 15 | 1 100 | 1 diamant 1 or | calculé | V | 1× [Brique d’or] · Builders |
| `Rock_Gold_Brick_Smooth` | Brique d’or lisse |  | 15 | 1 100 | 1 diamant 1 or | calculé | V | 1× [Brique d’or] · Builders |
| `Rock_Gold_Brick_Smooth_Half` | Dalle en brique d’or lisse |  | 15 | 550 | 5 or 5 argent | calculé | V | 1× [Brique d’or] → ×2 · Builders |
| `Rock_Gold_Brick_Stairs` | Escalier en brique d’or |  | 15 | 1 100 | 1 diamant 1 or | calculé | V | 1× [Brique d’or] · Builders |
| `Rock_Gold_Brick_Wall` | Mur en brique d’or |  | 15 | 550 | 5 or 5 argent | calculé | V | 1× [Brique d’or] → ×2 · Builders |
| `Rock_Kenolite` | Roche de kénolite |  | 10 | 5 | 5 cuivres | calculé | V | 2× [Pierre marine ou pavés marins] · Furnace |
| `Rock_Ledge_Brick` | Brique de pierre stratifiée |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre stratifiée ou pavés de pierre stratifiée] · Builders |
| `Rock_Ledge_Brick_Decorative` | Brique de pierre stratifiée décorative |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre stratifiée ou pavés de pierre stratifiée] · Builders |
| `Rock_Ledge_Brick_Half` | Dalle en brique de pierre stratifiée |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de pierre stratifiée] → ×2 · Builders |
| `Rock_Ledge_Brick_Ornate` | Brique de pierre stratifiée ornée |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre stratifiée ou pavés de pierre stratifiée] · Builders |
| `Rock_Ledge_Brick_Pillar_Base` | Pilier en brique de pierre stratifiée : base |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de pierre stratifiée] · Builders |
| `Rock_Ledge_Brick_Pillar_Middle` | Pilier en brique de pierre stratifiée : milieu |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de pierre stratifiée] · Builders |
| `Rock_Ledge_Brick_Smooth` | Brique de pierre stratifiée lisse |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre stratifiée ou pavés de pierre stratifiée] · Builders |
| `Rock_Ledge_Brick_Smooth_Half` | Dalle en brique de pierre stratifiée lisse |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de pierre stratifiée] → ×2 · Builders |
| `Rock_Ledge_Brick_Stairs` | Escalier en brique de pierre stratifiée |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de pierre stratifiée] · Builders |
| `Rock_Ledge_Brick_Wall` | Mur en brique de pierre stratifiée |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de pierre stratifiée] → ×2 · Builders |
| `Rock_Ledge_Cobble_Half` | Dalle en pavés de pierre stratifiée |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre stratifiée ou pavés de pierre stratifiée] → ×2 · Builders |
| `Rock_Ledge_Cobble_Pillar_Base` | Pilier en pavés de pierre stratifiée : base |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre stratifiée ou pavés de pierre stratifiée] · Builders |
| `Rock_Ledge_Cobble_Pillar_Middle` | Pilier en pavés de pierre stratifiée : milieu |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre stratifiée ou pavés de pierre stratifiée] · Builders |
| `Rock_Ledge_Cobble_Stairs` | Escalier en pavés de pierre stratifiée |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Pierre stratifiée ou pavés de pierre stratifiée] · Builders |
| `Rock_Ledge_Cobble_Wall` | Mur en pavés de pierre stratifiée |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre stratifiée ou pavés de pierre stratifiée] → ×2 · Builders |
| `Rock_Lime` | Calcaire |  | 10 | 5 | 5 cuivres | calculé | V | 2× [Calcaire ou pavés de calcaire] · Furnace |
| `Rock_Lime_Beam` | Poutre en calcaire |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Calcaire ou pavés de calcaire] → ×2 · Builders |
| `Rock_Lime_Brick` | Brique de calcaire |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Calcaire ou pavés de calcaire] · Builders |
| `Rock_Lime_Brick_Beam` | Poutre en brique de calcaire |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de calcaire] → ×2 · Builders |
| `Rock_Lime_Brick_Decorative` | Brique de calcaire décorative |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Calcaire ou pavés de calcaire] · Builders |
| `Rock_Lime_Brick_Half` | Dalle en brique de calcaire |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de calcaire] → ×2 · Builders |
| `Rock_Lime_Brick_Ornate` | Brique de calcaire ornée |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Calcaire ou pavés de calcaire] · Builders |
| `Rock_Lime_Brick_Pillar_Base` | Pilier en brique de calcaire : base |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de calcaire] · Builders |
| `Rock_Lime_Brick_Pillar_Middle` | Pilier en brique de calcaire : milieu |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de calcaire] · Builders |
| `Rock_Lime_Brick_Roof` | Toit en brique de calcaire |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de calcaire] · Builders |
| `Rock_Lime_Brick_Roof_Flat` | Toit en brique de calcaire : plat |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de calcaire] · Builders |
| `Rock_Lime_Brick_Roof_Shallow` | Toit en brique de calcaire : faible pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de calcaire] · Builders |
| `Rock_Lime_Brick_Roof_Steep` | Toit en brique de calcaire : forte pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de calcaire] · Builders |
| `Rock_Lime_Brick_Smooth` | Brique de calcaire lisse |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Calcaire ou pavés de calcaire] · Builders |
| `Rock_Lime_Brick_Smooth_Half` | Dalle en brique de calcaire lisse |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de calcaire] → ×2 · Builders |
| `Rock_Lime_Brick_Stairs` | Escalier en brique de calcaire |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de calcaire] · Builders |
| `Rock_Lime_Brick_Wall` | Mur en brique de calcaire |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de calcaire] → ×2 · Builders |
| `Rock_Lime_Cobble_Beam` | Poutre en pavés de calcaire |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Calcaire ou pavés de calcaire] → ×2 · Builders |
| `Rock_Lime_Cobble_Half` | Dalle en pavés de calcaire |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Calcaire ou pavés de calcaire] → ×2 · Builders |
| `Rock_Lime_Cobble_Pillar_Base` | Pilier en pavés de calcaire : base |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Calcaire ou pavés de calcaire] · Builders |
| `Rock_Lime_Cobble_Pillar_Middle` | Pilier en pavés de calcaire : milieu |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Calcaire ou pavés de calcaire] · Builders |
| `Rock_Lime_Cobble_Roof` | Toit en pavés de calcaire |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Calcaire ou pavés de calcaire] · Builders |
| `Rock_Lime_Cobble_Roof_Flat` | Toit en pavés de calcaire : plat |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Calcaire ou pavés de calcaire] · Builders |
| `Rock_Lime_Cobble_Roof_Shallow` | Toit en pavés de calcaire : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Calcaire ou pavés de calcaire] · Builders |
| `Rock_Lime_Cobble_Roof_Steep` | Toit en pavés de calcaire : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Calcaire ou pavés de calcaire] · Builders |
| `Rock_Lime_Cobble_Stairs` | Escalier en pavés de calcaire |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Calcaire ou pavés de calcaire] · Builders |
| `Rock_Lime_Cobble_Wall` | Mur en pavés de calcaire |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Calcaire ou pavés de calcaire] → ×2 · Builders |
| `Rock_Lime_Half` | Dalle en calcaire |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Calcaire ou pavés de calcaire] → ×2 · Builders |
| `Rock_Lime_Stairs` | Escalier en calcaire |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Calcaire ou pavés de calcaire] · Builders |
| `Rock_Magma_Cooled_Beam` | Poutre en magma refroidi |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Rock_Magma_Cooled] → ×2 · Builders |
| `Rock_Magma_Cooled_Brick` | Brique de magma refroidi |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Rock_Magma_Cooled] · Builders |
| `Rock_Magma_Cooled_Brick_Beam` | Poutre en brique de magma refroidi |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Rock_Magma_Cooled_Brick] → ×2 · Builders |
| `Rock_Magma_Cooled_Brick_Decorative` | Brique de magma refroidi décorative |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Rock_Magma_Cooled] · Builders |
| `Rock_Magma_Cooled_Brick_Half` | Dalle en brique de magma refroidi |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Rock_Magma_Cooled_Brick] → ×2 · Builders |
| `Rock_Magma_Cooled_Brick_Ornate` | Brique de magma refroidi ornée |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Rock_Magma_Cooled] · Builders |
| `Rock_Magma_Cooled_Brick_Pillar_Base` | Pilier en brique de magma refroidi : base |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Rock_Magma_Cooled_Brick] · Builders |
| `Rock_Magma_Cooled_Brick_Pillar_Middle` | Pilier en brique de magma refroidi : milieu |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Rock_Magma_Cooled_Brick] · Builders |
| `Rock_Magma_Cooled_Brick_Roof` | Toit en brique de magma refroidi |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Rock_Magma_Cooled_Brick] · Builders |
| `Rock_Magma_Cooled_Brick_Roof_Flat` | Toit en brique de magma refroidi : plat |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Rock_Magma_Cooled_Brick] · Builders |
| `Rock_Magma_Cooled_Brick_Roof_Shallow` | Toit en brique de magma refroidi : faible pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Rock_Magma_Cooled_Brick] · Builders |
| `Rock_Magma_Cooled_Brick_Roof_Steep` | Toit en brique de magma refroidi : forte pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Rock_Magma_Cooled_Brick] · Builders |
| `Rock_Magma_Cooled_Brick_Smooth` | Brique de magma refroidi lisse |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Rock_Magma_Cooled] · Builders |
| `Rock_Magma_Cooled_Brick_Smooth_Half` | Dalle en brique de magma refroidi lisse |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Rock_Magma_Cooled_Brick] → ×2 · Builders |
| `Rock_Magma_Cooled_Brick_Stairs` | Escalier en brique de magma refroidi |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Rock_Magma_Cooled_Brick] · Builders |
| `Rock_Magma_Cooled_Brick_Wall` | Mur en brique de magma refroidi |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Rock_Magma_Cooled_Brick] → ×2 · Builders |
| `Rock_Magma_Cooled_Cobble_Beam` | Poutre en pavés de magma refroidi |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Rock_Magma_Cooled] → ×2 · Builders |
| `Rock_Magma_Cooled_Cobble_Half` | Dalle en pavés de magma refroidi |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Rock_Magma_Cooled] → ×2 · Builders |
| `Rock_Magma_Cooled_Cobble_Roof` | Toit en pavés de magma refroidi |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Rock_Magma_Cooled] · Builders |
| `Rock_Magma_Cooled_Cobble_Roof_Flat` | Toit en pavés de magma refroidi : plat |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Rock_Magma_Cooled] · Builders |
| `Rock_Magma_Cooled_Cobble_Roof_Shallow` | Toit en pavés de magma refroidi : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Rock_Magma_Cooled] · Builders |
| `Rock_Magma_Cooled_Cobble_Roof_Steep` | Toit en pavés de magma refroidi : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Rock_Magma_Cooled] · Builders |
| `Rock_Magma_Cooled_Cobble_Stairs` | Escalier en pavés de magma refroidi |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Rock_Magma_Cooled] · Builders |
| `Rock_Magma_Cooled_Cobble_Wall` | Mur en pavés de magma refroidi |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Rock_Magma_Cooled] → ×2 · Builders |
| `Rock_Magma_Cooled_Stairs` | Escalier en magma refroidi |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Rock_Magma_Cooled] · Builders |
| `Rock_Marble` | Marbre |  | 10 | 5 | 5 cuivres | calculé | V | 2× [Marbre ou pavés de marbre] · Furnace |
| `Rock_Marble_Beam` | Poutre en marbre |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Marbre ou pavés de marbre] → ×2 · Builders |
| `Rock_Marble_Brick` | Brique de marbre |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Marbre ou pavés de marbre] · Builders |
| `Rock_Marble_Brick_Beam` | Poutre en brique de marbre |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de marbre] → ×2 · Builders |
| `Rock_Marble_Brick_Decorative` | Brique de marbre décorative |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Marbre ou pavés de marbre] · Builders |
| `Rock_Marble_Brick_Half` | Dalle en brique de marbre |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de marbre] → ×2 · Builders |
| `Rock_Marble_Brick_Ornate` | Brique de marbre ornée |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Marbre ou pavés de marbre] · Builders |
| `Rock_Marble_Brick_Pillar_Base` | Pilier en brique de marbre : base |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de marbre] · Builders |
| `Rock_Marble_Brick_Pillar_Middle` | Pilier en brique de marbre : milieu |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de marbre] · Builders |
| `Rock_Marble_Brick_Roof` | Toit en brique de marbre |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de marbre] · Builders |
| `Rock_Marble_Brick_Roof_Flat` | Toit en brique de marbre : plat |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de marbre] · Builders |
| `Rock_Marble_Brick_Roof_Shallow` | Toit en brique de marbre : faible pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de marbre] · Builders |
| `Rock_Marble_Brick_Roof_Steep` | Toit en brique de marbre : forte pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de marbre] · Builders |
| `Rock_Marble_Brick_Smooth` | Brique de marbre lisse |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Marbre ou pavés de marbre] · Builders |
| `Rock_Marble_Brick_Smooth_Half` | Dalle en brique de marbre lisse |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de marbre] → ×2 · Builders |
| `Rock_Marble_Brick_Stairs` | Escalier en brique de marbre |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de marbre] · Builders |
| `Rock_Marble_Brick_Wall` | Mur en brique de marbre |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de marbre] → ×2 · Builders |
| `Rock_Marble_Cobble_Beam` | Poutre en pavés de marbre |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Marbre ou pavés de marbre] → ×2 · Builders |
| `Rock_Marble_Cobble_Half` | Dalle en pavés de marbre |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Marbre ou pavés de marbre] → ×2 · Builders |
| `Rock_Marble_Cobble_Roof` | Toit en pavés de marbre |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Marbre ou pavés de marbre] · Builders |
| `Rock_Marble_Cobble_Roof_Flat` | Toit en pavés de marbre : plat |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Marbre ou pavés de marbre] · Builders |
| `Rock_Marble_Cobble_Roof_Shallow` | Toit en pavés de marbre : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Marbre ou pavés de marbre] · Builders |
| `Rock_Marble_Cobble_Roof_Steep` | Toit en pavés de marbre : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Marbre ou pavés de marbre] · Builders |
| `Rock_Marble_Cobble_Stairs` | Escalier en pavés de marbre |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Marbre ou pavés de marbre] · Builders |
| `Rock_Marble_Cobble_Wall` | Mur en pavés de marbre |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Marbre ou pavés de marbre] → ×2 · Builders |
| `Rock_Marble_Half` | Dalle en marbre |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Marbre ou pavés de marbre] → ×2 · Builders |
| `Rock_Marble_Stairs` | Escalier en marbre |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Marbre ou pavés de marbre] · Builders |
| `Rock_Peach_Brick` | Brique de pierre pêche |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre pêche ou pavés de pierre pêche] · Builders |
| `Rock_Peach_Brick_Beam` | Tuyau en brique de pierre pêche |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de pierre pêche] → ×2 · Builders |
| `Rock_Peach_Brick_Half` | Dalle en brique de pierre pêche |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de pierre pêche] → ×2 · Builders |
| `Rock_Peach_Brick_Pillar_Base` | Pilier en brique de pierre pêche : base |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de pierre pêche] · Builders |
| `Rock_Peach_Brick_Pillar_Middle` | Pilier en brique de pierre pêche : milieu |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de pierre pêche] · Builders |
| `Rock_Peach_Brick_Stairs` | Escalier en brique de pierre pêche |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de pierre pêche] · Builders |
| `Rock_Peach_Brick_Wall` | Mur en brique de pierre pêche |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de pierre pêche] → ×2 · Builders |
| `Rock_Peach_Cobble_Beam` | Tuyau en pavés de pierre pêche |  | 15 | 1 | 1 cuivre | calculé | V | 1× [Pierre pêche ou pavés de pierre pêche] → ×2 · Builders |
| `Rock_Peach_Cobble_Half` | Dalle en pavés de pierre pêche |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre pêche ou pavés de pierre pêche] → ×2 · Builders |
| `Rock_Peach_Cobble_Pillar_Base` | Pilier en pavés de pierre pêche : base |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre pêche ou pavés de pierre pêche] · Builders |
| `Rock_Peach_Cobble_Pillar_Middle` | Pilier en pavés de pierre pêche : milieu |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre pêche ou pavés de pierre pêche] · Builders |
| `Rock_Peach_Cobble_Stairs` | Escalier en pavés de pierre pêche |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Pierre pêche ou pavés de pierre pêche] · Builders |
| `Rock_Peach_Cobble_Wall` | Mur en pavés de pierre pêche |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre pêche ou pavés de pierre pêche] → ×2 · Builders |
| `Rock_Quartzite` | Quartzite |  | 10 | 5 | 5 cuivres | calculé | V | 2× [Quartzite ou pavés de quartzite] · Furnace |
| `Rock_Quartzite_Beam` | Poutre en quartzite |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Quartzite ou pavés de quartzite] → ×2 · Builders |
| `Rock_Quartzite_Brick` | Brique de quartzite |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Quartzite ou pavés de quartzite] · Builders |
| `Rock_Quartzite_Brick_Beam` | Poutre en brique de quartzite |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de quartzite] → ×2 · Builders |
| `Rock_Quartzite_Brick_Decorative` | Brique de quartzite décorative |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Quartzite ou pavés de quartzite] · Builders |
| `Rock_Quartzite_Brick_Half` | Dalle en brique de quartzite |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de quartzite] → ×2 · Builders |
| `Rock_Quartzite_Brick_Ornate` | Brique de quartzite ornée |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Quartzite ou pavés de quartzite] · Builders |
| `Rock_Quartzite_Brick_Pillar_Base` | Pilier en brique de quartzite : base |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de quartzite] · Builders |
| `Rock_Quartzite_Brick_Pillar_Middle` | Pilier en brique de quartzite : milieu |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de quartzite] · Builders |
| `Rock_Quartzite_Brick_Roof` | Toit en brique de quartzite |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de quartzite] · Builders |
| `Rock_Quartzite_Brick_Roof_Flat` | Toit en brique de quartzite : plat |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de quartzite] · Builders |
| `Rock_Quartzite_Brick_Roof_Shallow` | Toit en brique de quartzite : faible pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de quartzite] · Builders |
| `Rock_Quartzite_Brick_Roof_Steep` | Toit en brique de quartzite : forte pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de quartzite] · Builders |
| `Rock_Quartzite_Brick_Smooth` | Brique de quartzite lisse |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Quartzite ou pavés de quartzite] · Builders |
| `Rock_Quartzite_Brick_Smooth_Half` | Dalle en brique de quartzite lisse |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de quartzite] → ×2 · Builders |
| `Rock_Quartzite_Brick_Stairs` | Escalier en brique de quartzite |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de quartzite] · Builders |
| `Rock_Quartzite_Brick_Wall` | Mur en brique de quartzite |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de quartzite] → ×2 · Builders |
| `Rock_Quartzite_Cobble_Beam` | Poutre en pavés de quartzite |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Quartzite ou pavés de quartzite] → ×2 · Builders |
| `Rock_Quartzite_Cobble_Half` | Dalle en pavés de quartzite |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Quartzite ou pavés de quartzite] → ×2 · Builders |
| `Rock_Quartzite_Cobble_Roof` | Toit en pavés de quartzite |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Quartzite ou pavés de quartzite] · Builders |
| `Rock_Quartzite_Cobble_Roof_Flat` | Toit en pavés de quartzite : plat |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Quartzite ou pavés de quartzite] · Builders |
| `Rock_Quartzite_Cobble_Roof_Shallow` | Toit en pavés de quartzite : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Quartzite ou pavés de quartzite] · Builders |
| `Rock_Quartzite_Cobble_Roof_Steep` | Toit en pavés de quartzite : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Quartzite ou pavés de quartzite] · Builders |
| `Rock_Quartzite_Cobble_Stairs` | Escalier en pavés de quartzite |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Quartzite ou pavés de quartzite] · Builders |
| `Rock_Quartzite_Cobble_Wall` | Mur en pavés de quartzite |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Quartzite ou pavés de quartzite] → ×2 · Builders |
| `Rock_Quartzite_Half` | Dalle en quartzite |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Quartzite ou pavés de quartzite] → ×2 · Builders |
| `Rock_Quartzite_Stairs` | Escalier en quartzite |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Quartzite ou pavés de quartzite] · Builders |
| `Rock_Sandstone` | Grès |  | 10 | 5 | 5 cuivres | calculé | V | 2× [Grès ou pavés de grès] · Furnace |
| `Rock_Sandstone_Beam` | Poutre en grès |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Grès ou pavés de grès] → ×2 · Builders |
| `Rock_Sandstone_Brick` | Brique de grès |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès ou pavés de grès] · Builders |
| `Rock_Sandstone_Brick_Beam` | Poutre en brique de grès |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de grès] → ×2 · Builders |
| `Rock_Sandstone_Brick_Decorative` | Brique de grès décorative |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès ou pavés de grès] · Builders |
| `Rock_Sandstone_Brick_Half` | Dalle en brique de grès |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de grès] → ×2 · Builders |
| `Rock_Sandstone_Brick_Ornate` | Brique de grès ornée |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès ou pavés de grès] · Builders |
| `Rock_Sandstone_Brick_Pillar_Base` | Pilier en brique de grès : base |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès] · Builders |
| `Rock_Sandstone_Brick_Pillar_Middle` | Pilier en brique de grès : milieu |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès] · Builders |
| `Rock_Sandstone_Brick_Roof` | Toit en brique de grès |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès] · Builders |
| `Rock_Sandstone_Brick_Roof_Flat` | Toit en brique de grès : plat |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès] · Builders |
| `Rock_Sandstone_Brick_Roof_Shallow` | Toit en brique de grès : faible pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès] · Builders |
| `Rock_Sandstone_Brick_Roof_Steep` | Toit en brique de grès : forte pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès] · Builders |
| `Rock_Sandstone_Brick_Smooth` | Brique de grès lisse |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès ou pavés de grès] · Builders |
| `Rock_Sandstone_Brick_Smooth_Half` | Dalle en brique de grès lisse |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de grès] → ×2 · Builders |
| `Rock_Sandstone_Brick_Stairs` | Escalier en brique de grès |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès] · Builders |
| `Rock_Sandstone_Brick_Wall` | Mur en brique de grès |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de grès] → ×2 · Builders |
| `Rock_Sandstone_Cobble_Beam` | Poutre en pavés de grès |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Grès ou pavés de grès] → ×2 · Builders |
| `Rock_Sandstone_Cobble_Half` | Dalle en pavés de grès |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Grès ou pavés de grès] → ×2 · Builders |
| `Rock_Sandstone_Cobble_Roof` | Toit en pavés de grès |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Grès ou pavés de grès] · Builders |
| `Rock_Sandstone_Cobble_Roof_Flat` | Toit en pavés de grès : plat |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Grès ou pavés de grès] · Builders |
| `Rock_Sandstone_Cobble_Roof_Shallow` | Toit en pavés de grès : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès ou pavés de grès] · Builders |
| `Rock_Sandstone_Cobble_Roof_Steep` | Toit en pavés de grès : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès ou pavés de grès] · Builders |
| `Rock_Sandstone_Cobble_Stairs` | Escalier en pavés de grès |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Grès ou pavés de grès] · Builders |
| `Rock_Sandstone_Cobble_Wall` | Mur en pavés de grès |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Grès ou pavés de grès] → ×2 · Builders |
| `Rock_Sandstone_Half` | Dalle en grès |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Grès ou pavés de grès] → ×2 · Builders |
| `Rock_Sandstone_Red` | Grès rouge |  | 10 | 5 | 5 cuivres | calculé | V | 2× [Grès rouge ou pavés de grès rouge] · Furnace |
| `Rock_Sandstone_Red_Beam` | Poutre en grès rouge |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Grès rouge ou pavés de grès rouge] → ×2 · Builders |
| `Rock_Sandstone_Red_Brick` | Brique de grès rouge |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès rouge ou pavés de grès rouge] · Builders |
| `Rock_Sandstone_Red_Brick_Beam` | Poutre en brique de grès rouge |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de grès rouge] → ×2 · Builders |
| `Rock_Sandstone_Red_Brick_Decorative` | Brique de grès rouge décorative |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès rouge ou pavés de grès rouge] · Builders |
| `Rock_Sandstone_Red_Brick_Half` | Dalle en brique de grès rouge |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de grès rouge] → ×2 · Builders |
| `Rock_Sandstone_Red_Brick_Ornate` | Brique de grès rouge ornée |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès rouge ou pavés de grès rouge] · Builders |
| `Rock_Sandstone_Red_Brick_Pillar_Base` | Pilier en brique de grès rouge : base |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès rouge] · Builders |
| `Rock_Sandstone_Red_Brick_Pillar_Middle` | Pilier en brique de grès rouge : milieu |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès rouge] · Builders |
| `Rock_Sandstone_Red_Brick_Roof` | Toit en brique de grès rouge |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès rouge] · Builders |
| `Rock_Sandstone_Red_Brick_Roof_Flat` | Toit en brique de grès rouge : plat |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès rouge] · Builders |
| `Rock_Sandstone_Red_Brick_Roof_Shallow` | Toit en brique de grès rouge : faible pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès rouge] · Builders |
| `Rock_Sandstone_Red_Brick_Roof_Steep` | Toit en brique de grès rouge : forte pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès rouge] · Builders |
| `Rock_Sandstone_Red_Brick_Smooth` | Brique de grès rouge lisse |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès rouge ou pavés de grès rouge] · Builders |
| `Rock_Sandstone_Red_Brick_Smooth_Half` | Dalle en brique de grès rouge lisse |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de grès rouge] → ×2 · Builders |
| `Rock_Sandstone_Red_Brick_Stairs` | Escalier en brique de grès rouge |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès rouge] · Builders |
| `Rock_Sandstone_Red_Brick_Wall` | Mur en brique de grès rouge |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de grès rouge] → ×2 · Builders |
| `Rock_Sandstone_Red_Cobble_Beam` | Poutre en pavés de grès rouge |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Grès rouge ou pavés de grès rouge] → ×2 · Builders |
| `Rock_Sandstone_Red_Cobble_Half` | Dalle en pavés de grès rouge |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Grès rouge ou pavés de grès rouge] → ×2 · Builders |
| `Rock_Sandstone_Red_Cobble_Roof` | Toit en pavés de grès rouge |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Grès rouge ou pavés de grès rouge] · Builders |
| `Rock_Sandstone_Red_Cobble_Roof_Flat` | Toit en pavés de grès rouge : plat |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Grès rouge ou pavés de grès rouge] · Builders |
| `Rock_Sandstone_Red_Cobble_Roof_Shallow` | Toit en pavés de grès rouge : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès rouge ou pavés de grès rouge] · Builders |
| `Rock_Sandstone_Red_Cobble_Roof_Steep` | Toit en pavés de grès rouge : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès rouge ou pavés de grès rouge] · Builders |
| `Rock_Sandstone_Red_Cobble_Stairs` | Escalier en pavés de grès rouge |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Grès rouge ou pavés de grès rouge] · Builders |
| `Rock_Sandstone_Red_Cobble_Wall` | Mur en pavés de grès rouge |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Grès rouge ou pavés de grès rouge] → ×2 · Builders |
| `Rock_Sandstone_Red_Half` | Dalle en grès rouge |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Grès rouge ou pavés de grès rouge] → ×2 · Builders |
| `Rock_Sandstone_Red_Stairs` | Escalier en grès rouge |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Grès rouge ou pavés de grès rouge] · Builders |
| `Rock_Sandstone_Stairs` | Escalier en grès |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Grès ou pavés de grès] · Builders |
| `Rock_Sandstone_White` | Grès blanc |  | 10 | 5 | 5 cuivres | calculé | V | 2× [Grès blanc ou pavés de grès blanc] · Furnace |
| `Rock_Sandstone_White_Beam` | Poutre en grès blanc |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Grès blanc ou pavés de grès blanc] → ×2 · Builders |
| `Rock_Sandstone_White_Brick` | Brique de grès blanc |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès blanc ou pavés de grès blanc] · Builders |
| `Rock_Sandstone_White_Brick_Beam` | Poutre en brique de grès blanc |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de grès blanc] → ×2 · Builders |
| `Rock_Sandstone_White_Brick_Decorative` | Brique de grès blanc décorative |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès blanc ou pavés de grès blanc] · Builders |
| `Rock_Sandstone_White_Brick_Half` | Dalle en brique de grès blanc |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de grès blanc] → ×2 · Builders |
| `Rock_Sandstone_White_Brick_Ornate` | Brique de grès blanc ornée |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès blanc ou pavés de grès blanc] · Builders |
| `Rock_Sandstone_White_Brick_Pillar_Base` | Pilier en brique de grès blanc : base |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès blanc] · Builders |
| `Rock_Sandstone_White_Brick_Pillar_Middle` | Pilier en brique de grès blanc : milieu |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès blanc] · Builders |
| `Rock_Sandstone_White_Brick_Roof` | Toit en brique de grès blanc |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès blanc] · Builders |
| `Rock_Sandstone_White_Brick_Roof_Flat` | Toit en brique de grès blanc : plat |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès blanc] · Builders |
| `Rock_Sandstone_White_Brick_Roof_Shallow` | Toit en brique de grès blanc : faible pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès blanc] · Builders |
| `Rock_Sandstone_White_Brick_Roof_Steep` | Toit en brique de grès blanc : forte pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès blanc] · Builders |
| `Rock_Sandstone_White_Brick_Smooth` | Brique de grès blanc lisse |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès blanc ou pavés de grès blanc] · Builders |
| `Rock_Sandstone_White_Brick_Smooth_Half` | Dalle en brique de grès blanc lisse |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de grès blanc] → ×2 · Builders |
| `Rock_Sandstone_White_Brick_Stairs` | Escalier en brique de grès blanc |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de grès blanc] · Builders |
| `Rock_Sandstone_White_Brick_Wall` | Mur en brique de grès blanc |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de grès blanc] → ×2 · Builders |
| `Rock_Sandstone_White_Cobble_Beam` | Poutre en pavés de grès blanc |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Grès blanc ou pavés de grès blanc] → ×2 · Builders |
| `Rock_Sandstone_White_Cobble_Half` | Dalle en pavés de grès blanc |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Grès blanc ou pavés de grès blanc] → ×2 · Builders |
| `Rock_Sandstone_White_Cobble_Roof` | Toit en pavés de grès blanc |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Grès blanc ou pavés de grès blanc] · Builders |
| `Rock_Sandstone_White_Cobble_Roof_Flat` | Toit en pavés de grès blanc : plat |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Grès blanc ou pavés de grès blanc] · Builders |
| `Rock_Sandstone_White_Cobble_Roof_Shallow` | Toit en pavés de grès blanc : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès blanc ou pavés de grès blanc] · Builders |
| `Rock_Sandstone_White_Cobble_Roof_Steep` | Toit en pavés de grès blanc : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Grès blanc ou pavés de grès blanc] · Builders |
| `Rock_Sandstone_White_Cobble_Stairs` | Escalier en pavés de grès blanc |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Grès blanc ou pavés de grès blanc] · Builders |
| `Rock_Sandstone_White_Cobble_Wall` | Mur en pavés de grès blanc |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Grès blanc ou pavés de grès blanc] → ×2 · Builders |
| `Rock_Sandstone_White_Half` | Dalle en grès blanc |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Grès blanc ou pavés de grès blanc] → ×2 · Builders |
| `Rock_Sandstone_White_Stairs` | Escalier en grès blanc |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Grès blanc ou pavés de grès blanc] · Builders |
| `Rock_Shale` | Schiste |  | 10 | 5 | 5 cuivres | calculé | V | 2× [Schiste ou pavés de schiste] · Furnace |
| `Rock_Shale_Beam` | Poutre en schiste |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Schiste ou pavés de schiste] → ×2 · Builders |
| `Rock_Shale_Brick` | Brique de schiste |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Schiste ou pavés de schiste] · Builders |
| `Rock_Shale_Brick_Beam` | Poutre en brique de schiste |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de schiste] → ×2 · Builders |
| `Rock_Shale_Brick_Decorative` | Brique de schiste décorative |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Schiste ou pavés de schiste] · Builders |
| `Rock_Shale_Brick_Half` | Dalle en brique de schiste |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de schiste] → ×2 · Builders |
| `Rock_Shale_Brick_Ornate` | Brique de schiste ornée |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Schiste ou pavés de schiste] · Builders |
| `Rock_Shale_Brick_Pillar_Base` | Pilier en brique de schiste : base |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de schiste] · Builders |
| `Rock_Shale_Brick_Pillar_Middle` | Pilier en brique de schiste : milieu |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de schiste] · Builders |
| `Rock_Shale_Brick_Roof` | Toit en brique de schiste |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de schiste] · Builders |
| `Rock_Shale_Brick_Roof_Flat` | Toit en brique de schiste : plat |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de schiste] · Builders |
| `Rock_Shale_Brick_Roof_Shallow` | Toit en brique de schiste : faible pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de schiste] · Builders |
| `Rock_Shale_Brick_Roof_Steep` | Toit en brique de schiste : forte pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de schiste] · Builders |
| `Rock_Shale_Brick_Smooth` | Brique de schiste lisse |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Schiste ou pavés de schiste] · Builders |
| `Rock_Shale_Brick_Smooth_Half` | Dalle en brique de schiste lisse |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de schiste] → ×2 · Builders |
| `Rock_Shale_Brick_Stairs` | Escalier en brique de schiste |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de schiste] · Builders |
| `Rock_Shale_Brick_Wall` | Mur en brique de schiste |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de schiste] → ×2 · Builders |
| `Rock_Shale_Cobble_Beam` | Poutre en pavés de schiste |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Schiste ou pavés de schiste] → ×2 · Builders |
| `Rock_Shale_Cobble_Half` | Dalle en pavés de schiste |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Schiste ou pavés de schiste] → ×2 · Builders |
| `Rock_Shale_Cobble_Roof` | Toit en pavés de schiste |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Schiste ou pavés de schiste] · Builders |
| `Rock_Shale_Cobble_Roof_Flat` | Toit en pavés de schiste : plat |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Schiste ou pavés de schiste] · Builders |
| `Rock_Shale_Cobble_Roof_Shallow` | Toit en pavés de schiste : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Schiste ou pavés de schiste] · Builders |
| `Rock_Shale_Cobble_Roof_Steep` | Toit en pavés de schiste : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Schiste ou pavés de schiste] · Builders |
| `Rock_Shale_Cobble_Stairs` | Escalier en pavés de schiste |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Schiste ou pavés de schiste] · Builders |
| `Rock_Shale_Cobble_Wall` | Mur en pavés de schiste |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Schiste ou pavés de schiste] → ×2 · Builders |
| `Rock_Shale_Half` | Dalle en schiste |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Schiste ou pavés de schiste] → ×2 · Builders |
| `Rock_Shale_Stairs` | Escalier en schiste |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Schiste ou pavés de schiste] · Builders |
| `Rock_Slate` | Ardoise |  | 10 | 5 | 5 cuivres | calculé | V | 2× [Ardoise ou pavés d’ardoise] · Furnace |
| `Rock_Slate_Beam` | Poutre en ardoise |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Ardoise ou pavés d’ardoise] → ×2 · Builders |
| `Rock_Slate_Brick` | Brique d’ardoise |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Ardoise ou pavés d’ardoise] · Builders |
| `Rock_Slate_Brick_Beam` | Poutre en brique d’ardoise |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique d’ardoise] → ×2 · Builders |
| `Rock_Slate_Brick_Decorative` | Brique d’ardoise décorative |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Ardoise ou pavés d’ardoise] · Builders |
| `Rock_Slate_Brick_Half` | Dalle en brique d’ardoise |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique d’ardoise] → ×2 · Builders |
| `Rock_Slate_Brick_Ornate` | Brique d’ardoise ornée |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Ardoise ou pavés d’ardoise] · Builders |
| `Rock_Slate_Brick_Pillar_Base` | Pilier en brique d’ardoise : base |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique d’ardoise] · Builders |
| `Rock_Slate_Brick_Pillar_Middle` | Pilier en brique d’ardoise : milieu |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique d’ardoise] · Builders |
| `Rock_Slate_Brick_Roof` | Toit en brique d’ardoise |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique d’ardoise] · Builders |
| `Rock_Slate_Brick_Roof_Flat` | Toit en brique d’ardoise : plat |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique d’ardoise] · Builders |
| `Rock_Slate_Brick_Roof_Shallow` | Toit en brique d’ardoise : faible pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique d’ardoise] · Builders |
| `Rock_Slate_Brick_Roof_Steep` | Toit en brique d’ardoise : forte pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique d’ardoise] · Builders |
| `Rock_Slate_Brick_Smooth` | Brique d’ardoise lisse |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Ardoise ou pavés d’ardoise] · Builders |
| `Rock_Slate_Brick_Smooth_Half` | Dalle en brique d’ardoise lisse |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique d’ardoise] → ×2 · Builders |
| `Rock_Slate_Brick_Stairs` | Escalier en brique d’ardoise |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique d’ardoise] · Builders |
| `Rock_Slate_Brick_Wall` | Mur en brique d’ardoise |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique d’ardoise] → ×2 · Builders |
| `Rock_Slate_Cobble_Beam` | Poutre en pavés d’ardoise |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Ardoise ou pavés d’ardoise] → ×2 · Builders |
| `Rock_Slate_Cobble_Half` | Dalle en pavés d’ardoise |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Ardoise ou pavés d’ardoise] → ×2 · Builders |
| `Rock_Slate_Cobble_Roof` | Toit en pavés d’ardoise |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Ardoise ou pavés d’ardoise] · Builders |
| `Rock_Slate_Cobble_Roof_Flat` | Toit en pavés d’ardoise : plat |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Ardoise ou pavés d’ardoise] · Builders |
| `Rock_Slate_Cobble_Roof_Shallow` | Toit en pavés d’ardoise : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Ardoise ou pavés d’ardoise] · Builders |
| `Rock_Slate_Cobble_Roof_Steep` | Toit en pavés d’ardoise : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Ardoise ou pavés d’ardoise] · Builders |
| `Rock_Slate_Cobble_Stairs` | Escalier en pavés d’ardoise |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Ardoise ou pavés d’ardoise] · Builders |
| `Rock_Slate_Cobble_Wall` | Mur en pavés d’ardoise |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Ardoise ou pavés d’ardoise] → ×2 · Builders |
| `Rock_Slate_Half` | Dalle en ardoise |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Ardoise ou pavés d’ardoise] → ×2 · Builders |
| `Rock_Slate_Stairs` | Escalier en ardoise |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Ardoise ou pavés d’ardoise] · Builders |
| `Rock_Stone_Aqua` | Pierre marine |  | 10 | 5 | 5 cuivres | calculé | V | 2× [Pierre marine ou pavés marins] · Builders |
| `Rock_Stone_Aqua_Brick_Smooth_Half` | Dalle en brique marine lisse |  | 15 | 1 | 1 cuivre | calculé | V | 1× [Brique marine] → ×2 · Builders |
| `Rock_Stone_Beam` | Poutre en pierre |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre ou pavés de pierre] → ×2 · Builders |
| `Rock_Stone_Brick` | Brique de pierre |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre ou pavés de pierre] · Builders |
| `Rock_Stone_Brick_Beam` | Poutre en brique de pierre |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de pierre] → ×2 · Builders |
| `Rock_Stone_Brick_Decorative` | Brique de pierre décorative |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre ou pavés de pierre] · Builders |
| `Rock_Stone_Brick_Half` | Dalle en brique de pierre |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de pierre] → ×2 · Builders |
| `Rock_Stone_Brick_Mossy` | Brique de pierre moussue |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre moussue ou pavés de pierre moussue] · Builders |
| `Rock_Stone_Brick_Ornate` | Brique de pierre ornée |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre ou pavés de pierre] · Builders |
| `Rock_Stone_Brick_Pillar_Base` | Pilier en brique de pierre : base |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de pierre] · Builders |
| `Rock_Stone_Brick_Pillar_Middle` | Pilier en brique de pierre : milieu |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de pierre] · Builders |
| `Rock_Stone_Brick_Roof` | Toit en brique de pierre |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de pierre] · Builders |
| `Rock_Stone_Brick_Roof_Flat` | Toit en brique de pierre : plat |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de pierre] · Builders |
| `Rock_Stone_Brick_Roof_Shallow` | Toit en brique de pierre : faible pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de pierre] · Builders |
| `Rock_Stone_Brick_Roof_Steep` | Toit en brique de pierre : forte pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de pierre] · Builders |
| `Rock_Stone_Brick_Smooth` | Brique de pierre lisse |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre ou pavés de pierre] · Builders |
| `Rock_Stone_Brick_Smooth_Half` | Dalle en brique de pierre lisse |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de pierre] → ×2 · Builders |
| `Rock_Stone_Brick_Stairs` | Escalier en brique de pierre |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique de pierre] · Builders |
| `Rock_Stone_Brick_Wall` | Mur en brique de pierre |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique de pierre] → ×2 · Builders |
| `Rock_Stone_Cobble_Beam` | Poutre en pavés de pierre |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre ou pavés de pierre] → ×2 · Builders |
| `Rock_Stone_Cobble_Half` | Dalle en pavés de pierre |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre ou pavés de pierre] → ×2 · Builders |
| `Rock_Stone_Cobble_Mossy_Half` | Dalle en pavés de pierre moussus |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre moussue ou pavés de pierre moussue] → ×2 · Builders |
| `Rock_Stone_Cobble_Roof` | Toit en pavés de pierre |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Pierre ou pavés de pierre] · Builders |
| `Rock_Stone_Cobble_Roof_Flat` | Toit en pavés de pierre : plat |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Pierre ou pavés de pierre] · Builders |
| `Rock_Stone_Cobble_Roof_Shallow` | Toit en pavés de pierre : faible pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre ou pavés de pierre] · Builders |
| `Rock_Stone_Cobble_Roof_Steep` | Toit en pavés de pierre : forte pente |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre ou pavés de pierre] · Builders |
| `Rock_Stone_Cobble_Stairs` | Escalier en pavés de pierre |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Pierre ou pavés de pierre] · Builders |
| `Rock_Stone_Cobble_Wall` | Mur en pavés de pierre |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre ou pavés de pierre] → ×2 · Builders |
| `Rock_Stone_Stairs` | Escalier en pierre |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Pierre ou pavés de pierre] · Builders |
| `Rock_Volcanic` | Roche volcanique |  | 10 | 5 | 5 cuivres | calculé | V | 2× [Pierre volcanique ou pavés volcaniques] · Furnace |
| `Rock_Volcanic_Beam` | Poutre en roche volcanique |  | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre volcanique ou pavés volcaniques] → ×2 · Builders |
| `Rock_Volcanic_Brick` | Brique volcanique | Rebut | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre volcanique ou pavés volcaniques] · Builders |
| `Rock_Volcanic_Brick_Beam` | Poutre en brique volcanique | Rebut | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique volcanique] → ×2 · Builders |
| `Rock_Volcanic_Brick_Decorative` | Brique volcanique décorative | Rebut | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre volcanique ou pavés volcaniques] · Builders |
| `Rock_Volcanic_Brick_Half` | Dalle en brique volcanique | Rebut | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique volcanique] → ×2 · Builders |
| `Rock_Volcanic_Brick_Ornate` | Brique volcanique ornée | Rebut | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre volcanique ou pavés volcaniques] · Builders |
| `Rock_Volcanic_Brick_Pillar_Base` | Pilier en brique volcanique : base | Rebut | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique volcanique] · Builders |
| `Rock_Volcanic_Brick_Pillar_Middle` | Pilier en brique volcanique : milieu | Rebut | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique volcanique] · Builders |
| `Rock_Volcanic_Brick_Roof` | Toit en brique volcanique | Rebut | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique volcanique] · Builders |
| `Rock_Volcanic_Brick_Roof_Flat` | Toit en brique volcanique : plat | Rebut | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique volcanique] · Builders |
| `Rock_Volcanic_Brick_Roof_Shallow` | Toit en brique volcanique : faible pente | Rebut | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique volcanique] · Builders |
| `Rock_Volcanic_Brick_Roof_Steep` | Toit en brique volcanique : forte pente | Rebut | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique volcanique] · Builders |
| `Rock_Volcanic_Brick_Smooth` | Brique volcanique lisse | Rebut | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre volcanique ou pavés volcaniques] · Builders |
| `Rock_Volcanic_Brick_Smooth_Half` | Dalle en brique volcanique lisse | Rebut | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique volcanique] → ×2 · Builders |
| `Rock_Volcanic_Brick_Stairs` | Escalier en brique volcanique | Rebut | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique volcanique] · Builders |
| `Rock_Volcanic_Brick_Wall` | Mur en brique volcanique | Rebut | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique volcanique] → ×2 · Builders |
| `Rock_Volcanic_Cobble_Beam` | Poutre en pavés volcaniques | Rebut | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre volcanique ou pavés volcaniques] → ×2 · Builders |
| `Rock_Volcanic_Cobble_Half` | Dalle en pavés volcaniques | Rebut | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre volcanique ou pavés volcaniques] → ×2 · Builders |
| `Rock_Volcanic_Cobble_Roof` | Toit en pavés volcaniques | Rebut | 10 | 3 | 3 cuivres | calculé | V | 1× [Pierre volcanique ou pavés volcaniques] · Builders |
| `Rock_Volcanic_Cobble_Roof_Flat` | Toit en pavés volcaniques : plat | Rebut | 10 | 3 | 3 cuivres | calculé | V | 1× [Pierre volcanique ou pavés volcaniques] · Builders |
| `Rock_Volcanic_Cobble_Roof_Shallow` | Toit en pavés volcaniques : faible pente | Rebut | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre volcanique ou pavés volcaniques] · Builders |
| `Rock_Volcanic_Cobble_Roof_Steep` | Toit en pavés volcaniques : forte pente | Rebut | 15 | 3 | 3 cuivres | calculé | V | 1× [Pierre volcanique ou pavés volcaniques] · Builders |
| `Rock_Volcanic_Cobble_Stairs` | Escalier en pavés volcaniques | Rebut | 10 | 3 | 3 cuivres | calculé | V | 1× [Pierre volcanique ou pavés volcaniques] · Builders |
| `Rock_Volcanic_Cobble_Wall` | Mur en pavés volcaniques | Rebut | 10 | 1 | 1 cuivre | calculé | V | 1× [Pierre volcanique ou pavés volcaniques] → ×2 · Builders |
| `Rock_Volcanic_Stairs` | Escalier en roche volcanique |  | 10 | 3 | 3 cuivres | calculé | V | 1× [Pierre volcanique ou pavés volcaniques] · Builders |
| `Soil_Aqua_Gravel` | Gravier marin |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats marins · Farmingbench |
| `Soil_Aqua_Gravel_Half` | Dalle en gravier marin |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats marins → ×2 · Farmingbench |
| `Soil_Basalt_Gravel` | Gravier de basalte |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats de basalte · Farmingbench |
| `Soil_Basalt_Gravel_Half` | Dalle en gravier de basalte |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats de basalte → ×2 · Farmingbench |
| `Soil_Calcite_Gravel` | Gravier de calcite |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats de calcite · Farmingbench |
| `Soil_Calcite_Gravel_Half` | Dalle en gravier de calcite |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats de calcite → ×2 · Farmingbench |
| `Soil_Chalk_Gravel` | Gravier de craie |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats de craie · Farmingbench |
| `Soil_Chalk_Gravel_Half` | Dalle en gravier de craie |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats de craie → ×2 · Farmingbench |
| `Soil_Clay` | Argile |  | 10 | 26 | 2 argent 6 cuivres | calculé | V | 4× Grès rouge · Furniture_Bench |
| `Soil_Clay_Brick` | Brique d’argile |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Argiles] · Builders |
| `Soil_Clay_Brick_Beam` | Poutre en brique d’argile |  | 15 | 1 | 1 cuivre | calculé | V | 1× [Argiles] → ×2 · Builders |
| `Soil_Clay_Brick_Half` | Dalle en brique d’argile |  | 15 | 1 | 1 cuivre | calculé | V | 1× [Argiles] → ×2 · Builders |
| `Soil_Clay_Brick_Stairs` | Escalier en brique d’argile |  | 15 | 3 | 3 cuivres | calculé | V | 1× [Argiles] · Builders |
| `Soil_Clay_Brick_Wall` | Mur en brique d’argile |  | 15 | 1 | 1 cuivre | calculé | V | 1× [Argiles] → ×2 · Builders |
| `Soil_Clay_Ocean_Brick` | Brique d’argile océanique |  | 15 | 3 | 3 cuivres | calculé | V | 1× Argile océanique · Builders |
| `Soil_Clay_Ocean_Brick_Beam` | Poutre en brique d’argile océanique |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique d’argile océanique] → ×2 · Builders |
| `Soil_Clay_Ocean_Brick_Decorative` | Brique d’argile océanique décorative |  | 15 | 3 | 3 cuivres | calculé | V | 1× Argile océanique · Builders |
| `Soil_Clay_Ocean_Brick_Half` | Dalle en brique d’argile océanique |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique d’argile océanique] → ×2 · Builders |
| `Soil_Clay_Ocean_Brick_Ornate` | Brique d’argile océanique ornée |  | 15 | 3 | 3 cuivres | calculé | V | 1× Argile océanique · Builders |
| `Soil_Clay_Ocean_Brick_Pillar_Base` | Pilier en brique d’argile océanique : base |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique d’argile océanique] · Builders |
| `Soil_Clay_Ocean_Brick_Pillar_Middle` | Pilier en brique d’argile océanique : milieu |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique d’argile océanique] · Builders |
| `Soil_Clay_Ocean_Brick_Roof` | Toit en brique d’argile océanique |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique d’argile océanique] · Builders |
| `Soil_Clay_Ocean_Brick_Roof_Flat` | Toit en brique d’argile océanique : plat |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique d’argile océanique] · Builders |
| `Soil_Clay_Ocean_Brick_Roof_Shallow` | Toit en brique d’argile océanique : faible pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique d’argile océanique] · Builders |
| `Soil_Clay_Ocean_Brick_Roof_Steep` | Toit en brique d’argile océanique : forte pente |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique d’argile océanique] · Builders |
| `Soil_Clay_Ocean_Brick_Smooth` | Brique d’argile océanique lisse |  | 15 | 3 | 3 cuivres | calculé | V | 1× Argile océanique · Builders |
| `Soil_Clay_Ocean_Brick_Smooth_Half` | Dalle en brique d’argile océanique lisse |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique d’argile océanique] → ×2 · Builders |
| `Soil_Clay_Ocean_Brick_Stairs` | Escalier en brique d’argile océanique |  | 15 | 4 | 4 cuivres | calculé | V | 1× [Brique d’argile océanique] · Builders |
| `Soil_Clay_Ocean_Brick_Wall` | Mur en brique d’argile océanique |  | 15 | 2 | 2 cuivres | calculé | V | 1× [Brique d’argile océanique] → ×2 · Builders |
| `Soil_Clay_Smooth_Black` | Argile noire lisse |  | 75 | 100 | 1 or | calculé | V | 2× Argile + 1× Pétales noirs · Furniture_Bench |
| `Soil_Clay_Smooth_Blue` | Argile bleue lisse |  | 75 | 90 | 9 argent | calculé | V | 2× Argile + 1× Pétales bleus · Furniture_Bench |
| `Soil_Clay_Smooth_Cyan` | Argile cyan lisse |  | 75 | 90 | 9 argent | calculé | V | 2× Argile + 1× Pétales cyan · Furniture_Bench |
| `Soil_Clay_Smooth_Green` | Argile verte lisse |  | 75 | 90 | 9 argent | calculé | V | 2× Argile + 1× Pétales verts · Furniture_Bench |
| `Soil_Clay_Smooth_GreenDark` | Argile vert foncé lisse |  | 75 | 120 | 1 or 2 argent | calculé | V | 2× Argile + 1× Pétales verts + 1× Pétales noirs · Furniture_Bench |
| `Soil_Clay_Smooth_Grey` | Argile gris foncé lisse |  | 75 | 110 | 1 or 1 argent | calculé | V | 2× Argile + 1× Pétales noirs + 1× Pétales blancs · Furniture_Bench |
| `Soil_Clay_Smooth_Grey2` | Argile grise lisse |  | 75 | 110 | 1 or 1 argent | calculé | V | 2× Argile + 1× Pétales noirs + 1× Pétales blancs · Furniture_Bench |
| `Soil_Clay_Smooth_Lime` | Argile vert citron lisse |  | 75 | 96 | 9 argent 6 cuivres | calculé | V | 2× Argile + 1× Pétales verts + 1× Pétales jaunes · Furniture_Bench |
| `Soil_Clay_Smooth_Orange` | Argile marron lisse |  | 75 | 96 | 9 argent 6 cuivres | calculé | V | 2× Argile + 1× Pétales rouges + 1× Pétales jaunes · Furniture_Bench |
| `Soil_Clay_Smooth_Pink` | Argile rose lisse |  | 75 | 90 | 9 argent | calculé | V | 2× Argile + 1× Pétales roses · Furniture_Bench |
| `Soil_Clay_Smooth_Purple` | Argile violette lisse |  | 75 | 90 | 9 argent | calculé | V | 2× Argile + 1× Pétales violets · Furniture_Bench |
| `Soil_Clay_Smooth_Red` | Argile marron foncé lisse |  | 75 | 120 | 1 or 2 argent | calculé | V | 2× Argile + 1× Pétales rouges + 1× Pétales noirs · Furniture_Bench |
| `Soil_Clay_Smooth_Red2` | Argile rouge lisse |  | 75 | 90 | 9 argent | calculé | V | 2× Argile + 1× Pétales rouges · Furniture_Bench |
| `Soil_Clay_Smooth_White` | Argile blanche lisse |  | 75 | 81 | 8 argent 1 cuivre | calculé | V | 2× Argile + 1× Pétales blancs · Furniture_Bench |
| `Soil_Clay_Smooth_Yellow` | Argile jaune lisse |  | 75 | 74 | 7 argent 4 cuivres | calculé | V | 2× Argile + 1× Pétales jaunes · Furniture_Bench |
| `Soil_Clay_Smooth_Yellow2` | Argile orange lisse |  | 75 | 74 | 7 argent 4 cuivres | calculé | V | 2× Argile + 1× Pétales jaunes · Furniture_Bench |
| `Soil_Dirt_Burnt_Half` | Dalle en terre brûlée |  | 10 | 1 | 1 cuivre | calculé | V | 1× Terre brûlée → ×2 · Builders |
| `Soil_Dirt_Cold` | Terre froide |  | 11 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Dirt_Cold_Half` | Dalle en terre froide |  | 10 | 10 | 1 argent | calculé | V | 1× Terre froide → ×2 · Builders |
| `Soil_Dirt_Dry` | Terre sèche |  | 11 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Dirt_Dry_Half` | Dalle en terre sèche |  | 10 | 10 | 1 argent | calculé | V | 1× Terre sèche → ×2 · Builders |
| `Soil_Dirt_Half` | Dalle en terre |  | 10 | 1 | 1 cuivre | calculé | V | 1× Terre → ×2 · Builders |
| `Soil_Dirt_Lush_Half` | Dalle en terre luxuriante |  | 10 | 1 | 1 cuivre | calculé | V | 1× Terre luxuriante → ×2 · Builders |
| `Soil_Dirt_Poisoned` | Terre empoisonnée |  | 11 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Dirt_Wet_Half` | Dalle en terre mouillée |  | 10 | 1 | 1 cuivre | calculé | V | 1× Terre mouillée → ×2 · Builders |
| `Soil_Grass` | Herbe |  | 3 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Grass_Burnt` | Herbe flétrie |  | 8 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Grass_Cold` | Herbe froide |  | 13 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Grass_Deep` | Herbe luxuriante |  | 5 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Grass_Dry` | Herbe sèche |  | 12 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Grass_Full` | Herbe dense |  | 4 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Grass_Sunny` | Herbe ensoleillée |  | 6 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Grass_Wet` | Herbe mouillée |  | 14 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Gravel` | Gravier |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats de pierre · Farmingbench |
| `Soil_Gravel_Half` | Dalle en gravier |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats de pierre → ×2 · Farmingbench |
| `Soil_Gravel_Lime` | Gravier de calcaire |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats de calcaire · Farmingbench |
| `Soil_Gravel_Mossy` | Gravier moussu |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats de pierre moussue · Farmingbench |
| `Soil_Gravel_Mossy_Half` | Dalle en gravier moussu |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats de pierre moussue → ×2 · Farmingbench |
| `Soil_Gravel_Sand` | Gravier sableux |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats de grès · Farmingbench |
| `Soil_Gravel_Sand_Half` | Dalle en gravier sableux |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats de grès → ×2 · Farmingbench |
| `Soil_Gravel_Sand_Red` | Gravier sableux rouge |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats de grès rouge · Farmingbench |
| `Soil_Gravel_Sand_Red_Half` | Dalle en gravier sableux rouge |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats de grès rouge → ×2 · Farmingbench |
| `Soil_Gravel_Sand_White` | Gravier sableux blanc |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats de grès blanc · Farmingbench |
| `Soil_Gravel_Sand_White_Half` | Dalle en gravier sableux blanc |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats de grès blanc → ×2 · Farmingbench |
| `Soil_Hive_Brick_Smooth_Half` | Dalle en brique de ruche lisse |  | 15 | 17 | 1 argent 7 cuivres | calculé | V | 1× Argile → ×2 · Builders |
| `Soil_Hive_Corrupted_Brick_Smooth_Half` | Dalle en brique de ruche corrompue lisse |  | 15 | 17 | 1 argent 7 cuivres | calculé | V | 1× Argile → ×2 · Builders |
| `Soil_Leaves` | Terre feuillue |  | 7 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Leaves_Full` | Terre feuillue pleine |  | 7 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Lime_Gravel_Half` | Dalle en gravier de calcaire |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats de calcaire → ×2 · Farmingbench |
| `Soil_Magma_Cooled_Gravel` | Gravier de magma refroidi |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats de magma refroidi · Farmingbench |
| `Soil_Magma_Cooled_Gravel_Half` | Dalle en gravier de magma refroidi |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats de magma refroidi → ×2 · Farmingbench |
| `Soil_Marble_Gravel_Half` | Dalle en gravier de marbre |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats de marbre → ×2 · Farmingbench |
| `Soil_Mud` | Boue |  | 9 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Mud_Dry` | Boue sèche |  | 10 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Needles` | Terre d’aiguilles |  | 10 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Pathway` | Sentier de terre |  | 11 | 16 | 1 argent 6 cuivres | calculé | V | 1× [N’importe quel sol] + 1× Fibre de plante · Farmingbench |
| `Soil_Pathway_Half` | Dalle de sentier de terre |  | 10 | 10 | 1 argent | calculé | V | 1× Sentier de terre → ×2 · Builders |
| `Soil_Pathway_Quarter` | Quart de sentier de terre |  | 10 | 21 | 2 argent 1 cuivre | calculé | V | 1× Sentier de terre · Builders |
| `Soil_Pathway_ThreeQuarter` | Trois quarts de sentier de terre |  | 10 | 21 | 2 argent 1 cuivre | calculé | V | 1× Sentier de terre · Builders |
| `Soil_Pebbles` | Gravier de marbre |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats de marbre · Farmingbench |
| `Soil_Pebbles_Frozen` | Gravier de schiste |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats de schiste · Farmingbench |
| `Soil_Quartzite_Gravel` | Gravier de quartzite |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats de quartzite · Farmingbench |
| `Soil_Quartzite_Gravel_Half` | Dalle en gravier de quartzite |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats de quartzite → ×2 · Farmingbench |
| `Soil_Sand_White_Path_Half` | Dalle de sentier de sable blanc |  | 15 | 1 | 1 cuivre | calculé | V | 1× Sable blanc → ×2 · Builders |
| `Soil_Shale_Gravel_Half` | Dalle en gravier de schiste |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats de schiste → ×2 · Farmingbench |
| `Soil_Slate_Gravel` | Gravier d’ardoise |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats d’ardoise · Farmingbench |
| `Soil_Slate_Gravel_Half` | Dalle en gravier d’ardoise |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats d’ardoise → ×2 · Farmingbench |
| `Soil_Snow_Brick` | Brique de neige |  | 15 | 5 | 5 cuivres | calculé | V | 2× Neige · Furnace |
| `Soil_Snow_Brick_Beam` | Poutre en brique de neige |  | 15 | 3 | 3 cuivres | calculé | V | 1× Neige · Builders |
| `Soil_Snow_Brick_Half` | Dalle en brique de neige |  | 15 | 1 | 1 cuivre | calculé | V | 1× Neige → ×2 · Builders |
| `Soil_Snow_Brick_Stairs` | Escalier en brique de neige |  | 15 | 3 | 3 cuivres | calculé | V | 1× Neige · Builders |
| `Soil_Snow_Brick_Wall` | Mur en brique de neige |  | 15 | 3 | 3 cuivres | calculé | V | 1× Neige · Builders |
| `Soil_Volcanic_Gravel` | Gravier volcanique |  | 15 | 8 | 8 cuivres | calculé | V | 3× Gravats volcaniques · Farmingbench |
| `Soil_Volcanic_Gravel_Half` | Dalle en gravier volcanique |  | 15 | 4 | 4 cuivres | calculé | V | 3× Gravats volcaniques → ×2 · Farmingbench |

<a id="fam-plantes-cultures"></a>
## Plantes & cultures

### Sans recette — prix fixés (465)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Plant_Barnacles` | Bernacles |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Bramble_Dead_Lavathorn` | Ronce de lave |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Bramble_Dead_Twisted` | Ronce empoisonnée |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Bramble_Dry_Magic` | Ronce sèche magique |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Bramble_Dry_Sandthorn` | Ronce de sable |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Bramble_Dry_Twisted` | Ronce aride |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Bramble_Moss_Twisted` | Ronce verte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Bramble_Winter` | Ronce givrée |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Bush` | Buisson |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Arid` | Buisson aride |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Arid_Palm` | Buisson de palmier aride |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Arid_Red` | Buisson aride rouge |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Arid_Sharp` | Herbe aride touffue |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Bramble` | Ronce sèche |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Crystal` | Gros buisson de cristal |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Dead` | Buisson mort |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Dead_Hanging` | Buisson mort suspendu |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Dead_Tall` | Arbuste |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Dead_Twisted` | Grand buisson mort |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Green` | Buisson vert |  | 44 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Hanging` | Buisson suspendu exotique |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Jungle` | Buisson de jungle |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Lush` | Gros buisson luxuriant |  | 46 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Wet` | Buisson humide |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Winter` | Arbuste givré |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Winter_Red` | Buisson rouge givré |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Winter_Sharp` | Herbe givrée touffue |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Bush_Winter_Snow` | Buisson enneigé |  | 45 | 5 | 5 cuivres | fixé | V |
| `Plant_Cactus_1` | Cactus : bas |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Cactus_2` | Cactus : milieu |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Cactus_3` | Cactus : haut |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Cactus_Ball_1` | Cactus : sphérique |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Cactus_Flat_1` | Petit cactus plat |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Cactus_Flat_2` | Cactus plat |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Cactus_Flat_3` | Grand cactus plat |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Cactus_Flower` | Fleur de cactus |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Blue` | Bloc de corail bleu |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Cyan` | Bloc de corail cyan |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Dead` | Bloc de corail mort |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Dead2` | Bloc de corail mourant |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Dead2_Half` | Dalle de corail mourant | Commun | 10 | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Dead_Half` | Dalle de corail mort | Commun | 10 | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Green` | Bloc de corail vert |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Grey` | Bloc de corail gris |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Lime` | Bloc de corail vert clair |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Orange` | Bloc de corail orange |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Pink` | Bloc de corail rose |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Poison` | Bloc de corail empoisonné |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Purple` | Bloc de corail violet |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Red` | Bloc de corail rouge |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Violet` | Bloc de corail mauve |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_White` | Bloc de corail blanc |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Block_Yellow` | Bloc de corail jaune |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_Blue` | Buisson de corail bleu |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_Cyan` | Buisson de corail cyan |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_Dead` | Buisson de corail mort |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_Dead2` | Buisson de corail mourant |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_Dead3` | Buisson de corail blanchi |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_Green` | Buisson de corail vert |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_Grey` | Buisson de corail gris |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_Lime` | Buisson de corail néon |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_Orange` | Buisson de corail orange |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_Pink` | Buisson de corail rose |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_Poisoned` | Buisson de corail empoisonné |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_Purple` | Buisson de corail violet |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_Red` | Buisson de corail rouge |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_Violet` | Buisson de corail mauve |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_White` | Buisson de corail blanc |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Bush_Yellow` | Buisson de corail jaune |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Giant_Dead` | Branche de corail géant morte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Giant_Dead2` | Éventail de corail géant mort |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Blue` | Éponge bleue |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Cyan` | Éponge cyan |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Dead` | Éponge de corail morte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Dead2` | Corail tabulaire mort |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Dead3` | Corail tabulaire mourant |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Dead4` | Éponge de corail mourante |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Dead5` | Tubes de corail morts |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Dead6` | Tubes de corail mourants |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Dead7` | Rose de corail morte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Green` | Tubes de corail verts |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Grey` | Corail tabulaire gris |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Lime` | Éponge vert clair |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Orange` | Corail tabulaire orange |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Pink` | Corail rose en éventail |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Purple` | Tubes de corail violets |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Red` | Éponge rouge |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Violet` | Anémone de mer |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_White` | Éponge blanche |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Coral_Model_Yellow` | Tubes de corail jaunes |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Crop_Aubergine_Block_Eternal` | Aubergine mûre | Peu commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Aubergine_Item` | Aubergine | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Berry_Block` | Buisson à baies |  | 10 | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Berry_Wet_Block` | Buisson à baies humide |  | 11 | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Berry_Winter_Block` | Buisson à baies givrées |  | 12 | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Carrot_Block_Eternal` | Carotte | Peu commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Carrot_Item` | Carotte | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Cauliflower_Block_Eternal` | Chou-fleur mûr | Peu commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Cauliflower_Item` | Chou-fleur | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Chilli_Block_Eternal` | Piment mûr | Peu commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Chilli_Item` | Piment | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Corn_Block_Eternal` | Maïs mûr | Peu commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Corn_Item` | Maïs | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Cotton_Block_Eternal` | Coton mûr | Peu commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Cotton_Item` | Coton | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Health1` | Rose écarlate |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Health1_Block` | Graines de rose écarlate |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Health2` | Amanite écarlate |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Health2_Block` | Spores d’amanite écarlate |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Health3` | Népenthès écarlate |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Health3_Block` | Graines de népenthès écarlate |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Lettuce_Block_Eternal` | Laitue mûre | Peu commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Lettuce_Item` | Laitue | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mana1` | Fougère azurée |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mana1_Block` | Graines de fougère azurée |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mana2` | Amanite azurée |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mana2_Block` | Spores d’amanite azurée |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mana3` | Algue azurée |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mana3_Block` | Graines d’algue azurée |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block` | Spores de champignon |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Blue` | Bloc de champignon bleu |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Blue_Branch` | Branche de champignon bleu |  | 10 | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Blue_Mycelium` | Mycélium de champignon bleu |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Blue_Trunk` | Tronc de champignon bleu |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Brown` | Bloc de champignon brun |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Brown_Branch` | Branche de champignon brun |  | 10 | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Brown_Mycelium` | Mycélium de champignon brun |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Brown_Trunk` | Tronc de champignon brun |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Green` | Bloc de champignon vert |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Green_Branch` | Branche de champignon vert |  | 10 | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Green_Mycelium` | Mycélium de champignon vert |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Green_Trunk` | Tronc de champignon vert |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Purple` | Bloc de champignon violet |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Purple_Branch` | Branche de champignon violet |  | 10 | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Purple_Mycelium` | Mycélium de champignon violet |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Purple_Trunk` | Tronc de champignon violet |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Red` | Bloc de champignon rouge |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Red_Branch` | Branche de champignon rouge |  | 10 | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Red_Mycelium` | Mycélium de champignon rouge |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Red_Trunk` | Tronc de champignon rouge |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_White` | Bloc de champignon blanc |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_White_Branch` | Branche de champignon blanc |  | 10 | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_White_Mycelium` | Mycélium de champignon blanc |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_White_Trunk` | Tronc de champignon blanc |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Yellow` | Bloc de champignon jaune |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Yellow_Branch` | Branche de champignon jaune |  | 10 | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Yellow_Mycelium` | Mycélium de champignon jaune |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Block_Yellow_Trunk` | Tronc de champignon jaune |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Boomshroom_Large` | Grand champiboum |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Boomshroom_Small` | Petit champiboum |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Cap_Brown` | Bolet brun |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Cap_Green` | Amanite émeraude tachetée |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Cap_Poison` | Fongiail tacheté |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Cap_Red` | Amanite tue-mouche |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Cap_White` | Amanite blanche |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Common_Blue` | Champignon bleu commun |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Common_Brown` | Champignon brun commun |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Common_Lime` | Champignon vert rebondi |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Common_Lime_Large` | Champignon vert rebondi |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Flatcap_Blue` | Champignon plat bleu |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Flatcap_Green` | Champignon plat vert |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Glowing_Blue` | Champignon luminescent bleu |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Glowing_Green` | Champignon luminescent vert |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Glowing_Orange` | Bébés champiboums |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Glowing_Purple` | Champignon luminescent violet |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Glowing_Red` | Champignon luminescent rouge |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Glowing_Violet` | Champignon luminescent mauve |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Shelve_Brown` | Polypore brun |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Shelve_Green` | Polypore vert |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Mushroom_Shelve_Yellow` | Polypore jaune |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Onion_Block_Eternal` | Oignons mûrs | Peu commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Onion_Item` | Oignon | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Potato_Block_Eternal` | Pomme de terre mûre | Peu commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Potato_Item` | Pomme de terre | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Pumpkin_Block_Eternal` | Citrouille mûre | Peu commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Pumpkin_Item` | Citrouille | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Rice_Block_Eternal` | Riz mûr | Peu commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Rice_Item` | Riz | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Stamina1` | Chardon des tempêtes |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Stamina1_Block` | Graines de chardon des tempêtes |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Stamina2` | Fongéclat |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Stamina2_Block` | Spores de fongéclat |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Stamina3` | Jonc des tempêtes |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Stamina3_Block` | Graines de jonc des tempêtes |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Tomato_Block_Eternal` | Tomate mûre | Peu commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Tomato_Item` | Tomate | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Turnip_Block_Eternal` | Navet mûr | Peu commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Turnip_Item` | Navet | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Wheat_Block_Eternal` | Blé mûr | Peu commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Wheat_Item` | Blé | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Wheat_Stage_4_Burnt` | Blé brûlé |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Crop_Wild_Grass_Block` | Graines d’herbe sauvage |  |  | 3 | 3 cuivres | fixé | V |
| `Plant_Fern` | Fougère |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Fern_Arid` | Fougère aride |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Fern_Forest` | Fougère des bois |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Fern_Giant` | Fougère géante |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Fern_Jungle` | Fougère tropicale |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Fern_Jungle_Trunk` | Tronc de fougère tropicale |  | 10 | 5 | 5 cuivres | fixé | V |
| `Plant_Fern_Tall` | Fougère haute |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Fern_Trunk` | Tronc de fougère tropicale |  | 10 | 5 | 5 cuivres | fixé | V |
| `Plant_Fern_Wet` | Fougère humide |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Fern_Wet_Big` | Grande fougère humide |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Fern_Wet_Giant` | Fougère humide géante |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Fern_Wet_Giant_Trunk` | Tronc de fougère humide |  | 10 | 5 | 5 cuivres | fixé | V |
| `Plant_Fern_Winter` | Fougère imposante |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Bushy_Blue` | Ortie azurée |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Bushy_Cyan` | Fétuque cyan |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Bushy_Green` | Ortie |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Bushy_Grey` | Buisson cendré |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Bushy_Orange` | Fougère orange luxuriante |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Bushy_Poisoned` | Ortie empoisonnée |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Bushy_Purple` | Ortie violette |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Bushy_Red` | Feuillage plume rouge |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Bushy_Violet` | Fleurs violettes |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Bushy_White` | Feuille givrée |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Bushy_Yellow` | Buisson aride jaune fleuri |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Blue` | Hibiscus bleu |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Blue2` | Aloès bleu |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Cyan` | Fleur aride cyan |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Cyan2` | Hibiscus cyan |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Grey` | Lin sauvage |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Grey2` | Pissenlit des sables |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Lime` | Fleur tropicale |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Lime2` | Plante grasse vert clair |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Orange` | Chrysanthème |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Orange2` | Fleur commune orange |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Pink` | Fleur rose commune |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Pink2` | Allium |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Poisoned` | Ronce desséchée carmin |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Poisoned2` | Népenthès rose |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Purple` | Fleur aride violette |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Purple2` | Fleur de lave |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Red` | Fleur aride rouge |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Red2` | Coquelicot |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Violet` | Campanules |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Violet2` | Violettes |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_White` | Hortensia blanc |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_White2` | Marguerite |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Yellow` | Hibiscus jaune |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Common_Yellow2` | Pissenlit |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Flax_Blue` | Lin bleu |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Flax_Orange` | Fleur de feu |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Flax_Pink` | Lin rose |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Flax_Purple` | Lin à baies |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Flax_White` | Pâquerettes |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Flax_Yellow` | Luzerne |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Hemlock` | Grande ciguë |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Orchid_Blue` | Herbe des cavernes bleue |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Orchid_Cyan` | Fleur azurée |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Orchid_Orange` | Orchidée orange |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Orchid_Pink` | Orchidée rose |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Orchid_Poisoned` | Orchidée noire |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Orchid_Purple` | Orchidée violette |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Orchid_Red` | Orchidée rouge |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Orchid_White` | Orchidée blanche |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Orchid_Yellow` | Orchidée jaune |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Poisoned_Orange` | Fleur empoisonnée |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Tall_Blue` | Dauphinelle |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Tall_Cyan` | Fougère cyan luxuriante |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Tall_Cyan2` | Fleur cyan |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Tall_Pink` | Camélia rose |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Tall_Purple` | Lavande |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Tall_Red` | Grande rafflésie rouge |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Tall_Violet` | Pied d’alouette |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Tall_Yellow` | Tournesol |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Water_Blue` | Nénuphar bleu |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Water_Duckweed` | Lentille d’eau |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Water_Green` | Nénuphar |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Water_Purple` | Nénuphar violet |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Water_Red` | Nénuphar rouge |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Flower_Water_White` | Nénuphar blanc |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Fruit_Apple` | Pomme | Commun | 10 | 3 | 3 cuivres | fixé | V |
| `Plant_Fruit_Azure` | Fruit azuré | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Fruit_Berries_Red` | Baies sauvages | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Fruit_Coconut` | Noix de coco | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Fruit_Mango` | Mangue magique | Commun | 10 | 3 | 3 cuivres | fixé | V |
| `Plant_Fruit_Pinkberry` | Baie rose | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Fruit_Poison` | Fruit d’arbre empoisonné | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Fruit_Spiral` | Fruit d’arbre spiralé | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Fruit_Windwillow` | Fruit de saule-vent | Commun |  | 3 | 3 cuivres | fixé | V |
| `Plant_Grass_Arid` | Herbe sèche |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Arid_Short` | Herbe sèche courte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Arid_Tall` | Herbe sèche haute |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Cave_Short` | Herbe courte de grotte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Dry` | Herbe sèche |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Dry_Tall` | Herbe sèche foisonnante |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Gnarled` | Herbe flétrie |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Gnarled_Short` | Herbe flétrie courte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Gnarled_Tall` | Herbe flétrie haute |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Jungle` | Herbe tropicale |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Jungle_Short` | Herbe tropicale courte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Jungle_Tall` | Herbe tropicale haute |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Lush` | Herbe luxuriante |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Lush_Short` | Herbe luxuriante courte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Lush_Tall` | Herbe luxuriante haute |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Poisoned` | Herbe empoisonnée |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Poisoned_Short` | Herbe empoisonnée courte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Rocky` | Herbe rocheuse |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Rocky_Short` | Herbe rocheuse courte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Rocky_Tall` | Herbe rocheuse haute |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Sharp` | Herbe tranchante |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Sharp_Overgrown` | Herbe tranchante foisonnante |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Sharp_Short` | Herbe tranchante courte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Sharp_Tall` | Herbe haute |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Sharp_Wild` | Herbe tranchante sauvage |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Snowy` | Herbe froide |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Snowy_Short` | Herbe froide courte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Snowy_Tall` | Herbe froide haute |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Wet` | Herbe humide |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Wet_Overgrown` | Herbe humide foisonnante |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Wet_Short` | Herbe humide courte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Wet_Tall` | Herbe humide haute |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Wet_Wild` | Herbe humide sauvage |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Winter` | Herbe givrée |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Winter_Short` | Herbe givrée courte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Grass_Winter_Tall` | Herbe givrée haute |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Hay_Bundle` | Fagot de foin |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Lavender_Block` | Graines de lavande |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Lavender_Stage_0` | Graines de lavande |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Amber` | Feuilles d’ambre |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Apple` | Feuilles de pommier |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Ash` | Feuilles de frêne |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Aspen` | Feuilles de tremble |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Autumn` | Feuilles d’automne |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Autumn_Floor` | Feuilles de sol forestier |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Azure` | Feuilles azurées |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Bamboo` | Feuilles de bambou |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Banyan` | Feuilles de figuier des banians |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Beech` | Feuilles de hêtre |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Birch` | Feuilles de bouleau |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Bottle` | Feuilles d’arbre bouteille |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Bramble` | Feuilles de ronce |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Burnt` | Feuilles brûlées |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Camphor` | Feuilles de camphrier |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Cedar` | Feuilles de cèdre |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Crystal` | Feuilles de cristal |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Dead` | Feuilles mortes |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Dry` | Feuilles sèches |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Fig_Blue` | Feuilles de figuier bleu |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Fir` | Aiguilles de sapin |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Fir_Red` | Aiguilles de sapin rouge |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Fir_Snow` | Aiguilles de sapin enneigées |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Fir_Tip` | Bourgeons de sapin |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Fir_Tip_Snow` | Bourgeons de sapin enneigés |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Fire` | Feuilles de feu |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Frostwood` | Feuilles d’arbre gelé |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Goldentree` | Feuilles d’ambre |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Gumboab` | Feuilles d’arbre à gomme |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Jungle` | Feuilles tropicales |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Jungle_Floor` | Feuilles de sol tropical |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Maple` | Feuilles d’érable |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Oak` | Feuilles de chêne |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Palm` | Feuilles de palmier |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Palm_Arid` | Feuilles de palmier aride |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Palm_Oasis` | Feuilles de palmier géantes |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Palo` | Feuilles de palo |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Petrified` | Aiguilles de pin pétrifiées |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Poisoned` | Feuilles empoisonnées |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Poisoned_Floor` | Racines d’arbre empoisonné |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Redwood` | Feuilles de séquoia |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Sadwillow` | Feuilles de saule pleureur |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Sallow` | Feuilles de saule cendré |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Snow` | Feuilles enneigées |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Spiral` | Feuilles en spirale |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Stormbark` | Feuilles d’écorce des tempêtes |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Windwillow` | Feuilles de saule-vent |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Leaves_Wisteria_Wild` | Feuilles de glycine sauvage |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Liana_Void` | Liane du Néant |  | 35 | 5 | 5 cuivres | fixé | V |
| `Plant_Moss_Blue` | Mousse bleue |  | 17 | 5 | 5 cuivres | fixé | V |
| `Plant_Moss_Green` | Mousse |  | 17 | 5 | 5 cuivres | fixé | V |
| `Plant_Moss_Green_Dark` | Mousse verte foncée |  | 17 | 5 | 5 cuivres | fixé | V |
| `Plant_Moss_Red` | Mousse rouge |  | 17 | 5 | 5 cuivres | fixé | V |
| `Plant_Moss_Rug_Pink` | Tapis de mousse rose |  | 17 | 5 | 5 cuivres | fixé | V |
| `Plant_Moss_Wall_Blue` | Mousse murale bleue |  | 30 | 5 | 5 cuivres | fixé | V |
| `Plant_Moss_Wall_Green` | Mousse murale verte |  | 20 | 5 | 5 cuivres | fixé | V |
| `Plant_Moss_Wall_Green_Dark` | Mousse murale vert foncé |  | 25 | 5 | 5 cuivres | fixé | V |
| `Plant_Moss_Wall_Red` | Mousse murale rouge |  | 40 | 5 | 5 cuivres | fixé | V |
| `Plant_Moss_Wall_Yellow` | Mousse murale jaune |  | 35 | 5 | 5 cuivres | fixé | V |
| `Plant_Moss_Yellow` | Mousse jaune |  | 17 | 5 | 5 cuivres | fixé | V |
| `Plant_Petals_Azure` | Pétales azurés |  | 21 | 5 | 5 cuivres | fixé | V |
| `Plant_Petals_Blood` | Pétales écarlates |  | 21 | 5 | 5 cuivres | fixé | V |
| `Plant_Petals_Storm` | Pétales de tempête |  | 21 | 5 | 5 cuivres | fixé | V |
| `Plant_Petals_Yellow` | Pétales jaunes |  | 21 | 5 | 5 cuivres | fixé | V |
| `Plant_Reeds_Arid` | Papyrus |  | 43 | 5 | 5 cuivres | fixé | V |
| `Plant_Reeds_Lava` | Roseaux de lave |  | 43 | 5 | 5 cuivres | fixé | V |
| `Plant_Reeds_Marsh` | Roseaux de rivière |  | 43 | 5 | 5 cuivres | fixé | V |
| `Plant_Reeds_Poison` | Roseaux empoisonnés |  | 43 | 5 | 5 cuivres | fixé | V |
| `Plant_Reeds_Water` | Hauts roseaux aquatiques |  | 43 | 5 | 5 cuivres | fixé | V |
| `Plant_Reeds_Wet` | Roseaux humides |  | 43 | 5 | 5 cuivres | fixé | V |
| `Plant_Reeds_Winter` | Roseaux givrés |  | 43 | 5 | 5 cuivres | fixé | V |
| `Plant_Roots_Cave` | Racines de grotte suspendues |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Roots_Cave_Small` | Racines de grotte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Roots_Leafy` | Racines de grotte feuillues |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Sapling_Crystal` | Pousse d’arbre de cristal |  | 6 | 5 | 5 cuivres | fixé | V |
| `Plant_Sapling_Poisoned` | Pousse d’arbre empoisonné |  | 6 | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Arid_Red` | Algue aride rouge |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Arid_Short` | Algue aride courte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Arid_Stack` | Amas d’algues arides |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Arid_Tall` | Algue aride haute |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Arid_Yellow` | Algue aride jaune |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Dead_Eerie` | Algues mortes inquiétantes |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Dead_Ghostly` | Algues mortes fantomatiques |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Dead_Short` | Petites algues mortes |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Dead_Stack` | Amas d’algues mortes |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Dead_Tall` | Hautes algues mortes |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Grass` | Algues |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Grass_Bulbs` | Bulbes d’algues vertes |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Grass_Green` | Algues vertes |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Grass_Short` | Petites algues |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Grass_Stack` | Algues empilées |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Grass_Tall` | Hautes algues |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Wet_Stack` | Algues humides |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Winter_Aurora` | Algue d’aurore |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Winter_Blue` | Algue givrée bleue |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Winter_Short` | Algue givrée courte |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Winter_Stack` | Amas d’algues givrées |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seaweed_Winter_Tall` | Algue givrée haute |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Seeds_Amber` | Sac de graines d’ambre | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Apple` | Sac de graines de pommier | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Ash` | Sac de graines de frêne | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Aspen` | Sac de graines de tremble | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Azure` | Sac de graines d’arbre azuré | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Bamboo` | Sac de graines de bambou | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Banyan` | Sac de graines de figuier des banians | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Beans_Purple` | Haricots violets | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Beech` | Sac de graines de hêtre | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Birch` | Sac de graines de bouleau | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Bottletree` | Sac de graines d’arbre bouteille | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Camphor` | Sac de graines de camphrier | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Cedar` | Sac de graines de cèdre | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Crystal` | Sac de graines d’arbre de cristal | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Dry` | Sac de graines d’arbre sec | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Fig_Blue` | Sac de graines de figuier bleu | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Fir` | Sac de graines de sapin | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Fire` | Sac de graines d’arbre de feu | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Frostwood` | Sac de graines d’arbre gelé | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Gumboab` | Sac de graines d’arbre à gomme | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Jungle` | Sac de graines d’arbre tropical | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Maple` | Sac de graines d’érable | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Mushroom` | Sac de spores de champignon | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Oak` | Sac de graines de chêne | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Palm` | Sac de graines de palmier | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Palo` | Sac de graines de palo | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Petrified` | Sac de graines de pin pétrifié | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Pine` | Pomme de pin | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Poisoned` | Sac de graines d’arbre empoisonné | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Redwood` | Sac de graines de séquoia | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Sadwillow` | Sac de graines de saule pleureur | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Sallow` | Sac de graines de saule cendré | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Spiral` | Sac de graines d’arbre spiralé | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Stormbark` | Sac de graines d’écorce des tempêtes | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Wild` | Sac de graines d’herbe sauvage |  | 1 | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Willow` | Sac de graines de saule-vent | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Seeds_Wisteria_Wild` | Sac de graines de glycine sauvage | Rebut |  | 2 | 2 cuivres | fixé | V |
| `Plant_Sunflower_Block` | Graines de tournesol |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Sunflower_Stage_0` | Graines de tournesol |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Vine_Green_Hanging` | Liane en spirale verte |  | 40 | 5 | 5 cuivres | fixé | V |
| `Plant_Vine_Hanging` | Liane suspendue |  | 40 | 5 | 5 cuivres | fixé | V |
| `Plant_Vine_Jungle` | Lianes murales de jungle |  | 40 | 5 | 5 cuivres | fixé | V |
| `Plant_Vine_Liana_End` | Extrémité de liane |  | 40 | 5 | 5 cuivres | fixé | V |
| `Plant_Vine_Red_Hanging` | Liane suspendue carmin |  | 40 | 5 | 5 cuivres | fixé | V |
| `Plant_Vine_Sadwillow_Hanging` | Larmes de saule pleureur |  | 40 | 5 | 5 cuivres | fixé | V |
| `Plant_Vine_Thick_Roots` | Racines de lianes épaisses |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Vine_Thick_Vertical` | Liane épaisse |  |  | 5 | 5 cuivres | fixé | V |
| `Plant_Vine_Wall_Dead` | Lierre mort |  | 41 | 5 | 5 cuivres | fixé | V |

### Avec recette — prix calculés (107)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O  Recette |
|---|---|---|---:|---:|---|---|---|---|
| `Plant_Moss_Block_Blue` | Bloc de mousse bleue |  | 26 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse bleue + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Block_Green` | Bloc de mousse verte |  | 16 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Block_Green_Dark` | Bloc de mousse verte foncée |  | 21 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse verte foncée + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Block_Red` | Bloc de mousse rouge |  | 36 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse rouge + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Block_Yellow` | Bloc de mousse jaune |  | 31 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse jaune + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Cave_Blue` | Mousse bleue suspendue |  | 29 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse bleue + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Cave_Green` | Mousse verte suspendue |  | 19 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Cave_Green_Dark` | Mousse verte foncée suspendue |  | 24 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse verte foncée + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Cave_Red` | Mousse rouge suspendue |  | 39 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse rouge + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Cave_Yellow` | Mousse jaune suspendue |  | 34 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse jaune + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Rug_Blue` | Tapis en mousse bleue |  | 27 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse bleue + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Rug_Green` | Tapis en mousse verte |  | 17 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Rug_Green_Dark` | Tapis en mousse verte foncée |  | 22 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse verte foncée + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Rug_Lime` | Tapis d’oseille |  |  | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Rug_Red` | Tapis de mousse rouge |  | 37 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse rouge + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Rug_Yellow` | Tapis de mousse jaune |  | 32 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse jaune + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Short_Blue` | Mousse courte bleue |  | 28 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse bleue + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Short_Green` | Mousse courte |  | 18 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Short_Green_Dark` | Mousse courte vert foncé |  | 23 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse verte foncée + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Short_Red` | Mousse courte rouge |  | 38 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse rouge + 2× Fibre de plante · Farmingbench |
| `Plant_Moss_Short_Yellow` | Mousse courte jaune |  | 33 | 52 | 5 argent 2 cuivres | calculé | V | 4× Mousse jaune + 2× Fibre de plante · Farmingbench |
| `Plant_Petals_Black` | Pétales noirs |  | 22 | 26 | 2 argent 6 cuivres | calculé | V | 1× Charbon de bois + 1× Pétales blancs · Furniture_Bench |
| `Plant_Petals_Blue` | Pétales bleus |  | 23 | 17 | 1 argent 7 cuivres | calculé | V | 1× Champignon plat bleu + 1× Pétales blancs · Furniture_Bench |
| `Plant_Petals_Cyan` | Pétales cyan |  | 24 | 17 | 1 argent 7 cuivres | calculé | V | 1× Champignon bleu commun + 1× Pétales blancs · Furniture_Bench |
| `Plant_Petals_Green` | Pétales verts |  | 25 | 17 | 1 argent 7 cuivres | calculé | V | 1× Laitue + 1× Pétales blancs · Furniture_Bench |
| `Plant_Petals_Orange` | Pétales orange |  | 26 | 17 | 1 argent 7 cuivres | calculé | V | 1× Carotte + 1× Pétales blancs · Furniture_Bench |
| `Plant_Petals_Pink` | Pétales roses |  | 27 | 17 | 1 argent 7 cuivres | calculé | V | 1× Navet + 1× Pétales blancs · Furniture_Bench |
| `Plant_Petals_Purple` | Pétales violets |  | 28 | 17 | 1 argent 7 cuivres | calculé | V | 1× Aubergine + 1× Pétales blancs · Furniture_Bench |
| `Plant_Petals_Red` | Pétales rouges |  | 29 | 17 | 1 argent 7 cuivres | calculé | V | 1× Pomme + 1× Pétales blancs · Furniture_Bench |
| `Plant_Petals_White` | Pétales blancs |  | 21 | 10 | 1 argent | calculé | V | 1× Coton + 1× Pétales jaunes · Furniture_Bench |
| `Plant_Sapling_Amber` | Pousse d’ambre |  | 6 | 580 | 5 or 8 argent | calculé | V | 15× Essence de vie · Farmingbench |
| `Plant_Sapling_Apple` | Pousse de pommier |  | 5 | 5 100 | 5 diamants 1 or | calculé | V | 1× Grande essence de vie + 4× Pomme · Farmingbench |
| `Plant_Sapling_Ash` | Pousse de frêne |  | 5 | 580 | 5 or 8 argent | calculé | V | 15× Essence de vie · Farmingbench |
| `Plant_Sapling_Aspen` | Pousse de tremble |  | 2 | 200 | 2 or | calculé | V | 5× Essence de vie · Farmingbench |
| `Plant_Sapling_Azure` | Pousse d’arbre azuré |  | 8 | 780 | 7 or 8 argent | calculé | V | 20× Essence de vie · Farmingbench |
| `Plant_Sapling_Bamboo` | Pousse de bambou |  | 6 | 580 | 5 or 8 argent | calculé | V | 15× Essence de vie · Farmingbench |
| `Plant_Sapling_Banyan` | Pousse de figuier des banians |  | 6 | 580 | 5 or 8 argent | calculé | V | 15× Essence de vie · Farmingbench |
| `Plant_Sapling_Beech` | Pousse de hêtre |  | 2 | 200 | 2 or | calculé | V | 5× Essence de vie · Farmingbench |
| `Plant_Sapling_Birch` | Pousse de bouleau |  | 4 | 390 | 3 or 9 argent | calculé | V | 10× Essence de vie · Farmingbench |
| `Plant_Sapling_Bottletree` | Pousse d’arbre bouteille |  | 10 | 1 200 | 1 diamant 2 or | calculé | V | 30× Essence de vie · Farmingbench |
| `Plant_Sapling_Camphor` | Pousse de camphrier |  | 4 | 390 | 3 or 9 argent | calculé | V | 10× Essence de vie · Farmingbench |
| `Plant_Sapling_Cedar` | Pousse de cèdre |  | 9 | 980 | 9 or 8 argent | calculé | V | 25× Essence de vie · Farmingbench |
| `Plant_Sapling_Dry` | Pousse d’arbre sec |  | 10 | 1 200 | 1 diamant 2 or | calculé | V | 30× Essence de vie · Farmingbench |
| `Plant_Sapling_Fig_Blue` | Pousse de figuier bleu |  | 8 | 780 | 7 or 8 argent | calculé | V | 20× Essence de vie · Farmingbench |
| `Plant_Sapling_Fire` | Pousse d’arbre de feu |  | 10 | 1 200 | 1 diamant 2 or | calculé | V | 30× Essence de vie · Farmingbench |
| `Plant_Sapling_Gumboab` | Pousse d’arbre à gomme |  | 4 | 390 | 3 or 9 argent | calculé | V | 10× Essence de vie · Farmingbench |
| `Plant_Sapling_Ice` | Pousse d’arbre gelé |  | 7 | 580 | 5 or 8 argent | calculé | V | 15× Essence de vie · Farmingbench |
| `Plant_Sapling_Jungle` | Pousse d’arbre tropical |  | 6 | 580 | 5 or 8 argent | calculé | V | 15× Essence de vie · Farmingbench |
| `Plant_Sapling_Maple` | Pousse d’érable |  | 8 | 780 | 7 or 8 argent | calculé | V | 20× Essence de vie · Farmingbench |
| `Plant_Sapling_Oak` | Pousse de chêne |  | 6 | 580 | 5 or 8 argent | calculé | V | 15× Essence de vie · Farmingbench |
| `Plant_Sapling_Palm` | Pousse de palmier |  | 11 | 1 400 | 1 diamant 4 or | calculé | V | 35× Essence de vie · Farmingbench |
| `Plant_Sapling_Palo` | Pousse de palo |  | 6 | 580 | 5 or 8 argent | calculé | V | 15× Essence de vie · Farmingbench |
| `Plant_Sapling_Petrified` | Pousse de pin pétrifié |  | 10 | 1 200 | 1 diamant 2 or | calculé | V | 30× Essence de vie · Farmingbench |
| `Plant_Sapling_Redwood` | Pousse de séquoia |  | 8 | 780 | 7 or 8 argent | calculé | V | 20× Essence de vie · Farmingbench |
| `Plant_Sapling_Sadwillow` | Pousse de saule pleureur |  | 6 | 580 | 5 or 8 argent | calculé | V | 15× Essence de vie · Farmingbench |
| `Plant_Sapling_Sallow` | Pousse de saule cendré |  | 6 | 580 | 5 or 8 argent | calculé | V | 15× Essence de vie · Farmingbench |
| `Plant_Sapling_Spiral` | Pousse d’arbre spiralé |  | 8 | 780 | 7 or 8 argent | calculé | V | 20× Essence de vie · Farmingbench |
| `Plant_Sapling_Spruce` | Pousse de sapin |  | 6 | 580 | 5 or 8 argent | calculé | V | 15× Essence de vie · Farmingbench |
| `Plant_Sapling_Spruce_Frozen` | Pousse de sapin givré |  | 7 | 580 | 5 or 8 argent | calculé | V | 15× Essence de vie · Farmingbench |
| `Plant_Sapling_Stormbark` | Pousse d’écorce des tempêtes |  | 6 | 580 | 5 or 8 argent | calculé | V | 15× Essence de vie · Farmingbench |
| `Plant_Sapling_Windwillow` | Pousse de saule-vent |  | 6 | 580 | 5 or 8 argent | calculé | V | 15× Essence de vie · Farmingbench |
| `Plant_Sapling_Wisteria_Wild` | Pousse de glycine sauvage |  | 9 | 980 | 9 or 8 argent | calculé | V | 25× Essence de vie · Farmingbench |
| `Plant_Seeds_Aubergine` | Sac de graines d’aubergine | Rebut | 12 | 230 | 2 or 3 argent | calculé | V | 6× Essence de vie · Farmingbench |
| `Plant_Seeds_Aubergine_Eternal` | Sac de graines d’aubergine (éternelles) | Peu commun | 18 | 2 800 | 2 diamants 8 or | calculé | V | 60× Essence de vie + 30× Aubergine + 1× Sac de graines d’aubergine · Farmingbench |
| `Plant_Seeds_Carrot` | Sac de graines de carotte | Rebut | 4 | 120 | 1 or 2 argent | calculé | V | 3× Essence de vie · Farmingbench |
| `Plant_Seeds_Carrot_Eternal` | Sac de graines de carotte (éternelles) | Peu commun | 10 | 1 400 | 1 diamant 4 or | calculé | V | 30× Essence de vie + 15× Carotte + 1× Sac de graines de carotte · Farmingbench |
| `Plant_Seeds_Cauliflower` | Sac de graines de chou-fleur | Rebut | 8 | 200 | 2 or | calculé | V | 5× Essence de vie · Farmingbench |
| `Plant_Seeds_Cauliflower_Eternal` | Sac de graines de chou-fleur (éternelles) | Peu commun | 14 | 2 300 | 2 diamants 3 or | calculé | V | 50× Essence de vie + 25× Chou-fleur + 1× Sac de graines de chou-fleur · Farmingbench |
| `Plant_Seeds_Chilli` | Sac de graines de piment | Rebut | 17 | 230 | 2 or 3 argent | calculé | V | 6× Essence de vie · Farmingbench |
| `Plant_Seeds_Chilli_Eternal` | Sac de graines de piment (éternelles) | Peu commun | 23 | 2 800 | 2 diamants 8 or | calculé | V | 60× Essence de vie + 30× Piment + 1× Sac de graines de piment · Farmingbench |
| `Plant_Seeds_Corn` | Sac de graines de maïs | Rebut | 5 | 120 | 1 or 2 argent | calculé | V | 3× Essence de vie · Farmingbench |
| `Plant_Seeds_Corn_Eternal` | Sac de graines de maïs (éternelles) | Peu commun | 11 | 1 400 | 1 diamant 4 or | calculé | V | 30× Essence de vie + 15× Maïs + 1× Sac de graines de maïs · Farmingbench |
| `Plant_Seeds_Cotton` | Sac de graines de coton | Rebut | 20 | 310 | 3 or 1 argent | calculé | V | 8× Essence de vie · Farmingbench |
| `Plant_Seeds_Cotton_Eternal` | Sac de graines de coton (éternelles) | Peu commun | 26 | 3 700 | 3 diamants 7 or | calculé | V | 80× Essence de vie + 40× Coton + 1× Sac de graines de coton · Farmingbench |
| `Plant_Seeds_Health1` | Sac de graines de rose écarlate | Rebut | 1 | 26 | 2 argent 6 cuivres | calculé | V | 2× Essence du Néant → ×3 · Alchemybench |
| `Plant_Seeds_Health2` | Sac de spores écarlates | Rebut | 4 | 78 | 7 argent 8 cuivres | calculé | V | 4× Essence du Néant → ×2 · Alchemybench |
| `Plant_Seeds_Health3` | Sac de graines de népenthès écarlate | Rebut | 7 | 310 | 3 or 1 argent | calculé | V | 8× Essence du Néant · Alchemybench |
| `Plant_Seeds_Lettuce` | Sac de graines de laitue | Rebut | 3 | 78 | 7 argent 8 cuivres | calculé | V | 2× Essence de vie · Farmingbench |
| `Plant_Seeds_Lettuce_Eternal` | Sac de graines de laitue (éternelles) | Peu commun | 7 | 920 | 9 or 2 argent | calculé | V | 20× Essence de vie + 10× Laitue + 1× Sac de graines de laitue · Farmingbench |
| `Plant_Seeds_Mana1` | Sac de graines de fougère azurée | Rebut | 3 | 26 | 2 argent 6 cuivres | calculé | V | 2× Essence du Néant → ×3 · Alchemybench |
| `Plant_Seeds_Mana2` | Sac de spores d’amanite azurée | Rebut | 6 | 78 | 7 argent 8 cuivres | calculé | V | 4× Essence du Néant → ×2 · Alchemybench |
| `Plant_Seeds_Mana3` | Sac de graines d’algue azurée | Rebut | 9 | 310 | 3 or 1 argent | calculé | V | 8× Essence du Néant · Alchemybench |
| `Plant_Seeds_Onion` | Bulbe d’oignon | Rebut | 24 | 390 | 3 or 9 argent | calculé | V | 10× Essence de vie · Farmingbench |
| `Plant_Seeds_Onion_Eternal` | Bulbe d’oignon (éternel) | Peu commun | 28 | 4 600 | 4 diamants 6 or | calculé | V | 100× Essence de vie + 50× Oignon + 1× Bulbe d’oignon · Farmingbench |
| `Plant_Seeds_Potato` | Germe de pomme de terre | Rebut | 25 | 390 | 3 or 9 argent | calculé | V | 10× Essence de vie · Farmingbench |
| `Plant_Seeds_Potato_Eternal` | Germe de pomme de terre (éternel) | Peu commun | 29 | 4 600 | 4 diamants 6 or | calculé | V | 100× Essence de vie + 50× Pomme de terre + 1× Germe de pomme de terre · Farmingbench |
| `Plant_Seeds_Pumpkin` | Sac de graines de citrouille | Rebut | 13 | 230 | 2 or 3 argent | calculé | V | 6× Essence de vie · Farmingbench |
| `Plant_Seeds_Pumpkin_Eternal` | Sac de graines de citrouille (éternelles) | Peu commun | 19 | 2 800 | 2 diamants 8 or | calculé | V | 60× Essence de vie + 30× Citrouille + 1× Sac de graines de citrouille · Farmingbench |
| `Plant_Seeds_Rice` | Sac de graines de riz | Rebut | 21 | 310 | 3 or 1 argent | calculé | V | 8× Essence de vie · Farmingbench |
| `Plant_Seeds_Rice_Eternal` | Sac de graines de riz (éternelles) | Peu commun | 27 | 3 700 | 3 diamants 7 or | calculé | V | 80× Essence de vie + 40× Riz + 1× Sac de graines de riz · Farmingbench |
| `Plant_Seeds_Stamina1` | Sac de graines de chardon des tempêtes | Rebut | 2 | 26 | 2 argent 6 cuivres | calculé | V | 2× Essence du Néant → ×3 · Alchemybench |
| `Plant_Seeds_Stamina2` | Sac de spores de fongéclat | Rebut | 5 | 78 | 7 argent 8 cuivres | calculé | V | 4× Essence du Néant → ×2 · Alchemybench |
| `Plant_Seeds_Stamina3` | Sac de graines de pousse des tempêtes | Rebut | 8 | 310 | 3 or 1 argent | calculé | V | 8× Essence du Néant · Alchemybench |
| `Plant_Seeds_Sunflower` | Sac de graines de tournesol | Rebut |  | 100 | 1 or | calculé | V | 5× Essence de vie + 1× Tournesol → ×2 · Farmingbench |
| `Plant_Seeds_Tomato` | Sac de graines de tomate | Rebut | 16 | 230 | 2 or 3 argent | calculé | V | 6× Essence de vie · Farmingbench |
| `Plant_Seeds_Tomato_Eternal` | Sac de graines de tomate (éternelles) | Peu commun | 22 | 2 800 | 2 diamants 8 or | calculé | V | 60× Essence de vie + 30× Tomate + 1× Sac de graines de tomate · Farmingbench |
| `Plant_Seeds_Turnip` | Sac de graines de navet | Rebut | 9 | 200 | 2 or | calculé | V | 5× Essence de vie · Farmingbench |
| `Plant_Seeds_Turnip_Eternal` | Sac de graines de navet (éternelles) | Peu commun | 15 | 2 300 | 2 diamants 3 or | calculé | V | 50× Essence de vie + 25× Navet + 1× Sac de graines de navet · Farmingbench |
| `Plant_Seeds_Wheat` | Sac de graines de blé | Rebut | 2 | 78 | 7 argent 8 cuivres | calculé | V | 2× Essence de vie · Farmingbench |
| `Plant_Seeds_Wheat_Eternal` | Sac de graines de blé (éternelles) | Peu commun | 6 | 920 | 9 or 2 argent | calculé | V | 20× Essence de vie + 10× Blé + 1× Sac de graines de blé · Farmingbench |
| `Plant_Vine` | Lierre mural |  | 40 | 65 | 6 argent 5 cuivres | calculé | V | 1× Essence de vie + 2× Fibre de plante · Farmingbench |
| `Plant_Vine_Liana` | Liane |  | 42 | 65 | 6 argent 5 cuivres | calculé | V | 1× Essence de vie + 2× Fibre de plante · Farmingbench |
| `Plant_Vine_Rug` | Tapis de lianes |  |  | 91 | 9 argent 1 cuivre | calculé | V | 4× Fibre de plante + 1× Essence de vie · Farmingbench |
| `Plant_Vine_Wall` | Lierre |  | 41 | 65 | 6 argent 5 cuivres | calculé | V | 1× Essence de vie + 2× Fibre de plante · Farmingbench |
| `Plant_Vine_Wall_Dry` | Liane sèche |  | 41 | 65 | 6 argent 5 cuivres | calculé | V | 1× Essence de vie + 2× Fibre de plante · Farmingbench |
| `Plant_Vine_Wall_Poisoned` | Lierre empoisonné |  | 41 | 65 | 6 argent 5 cuivres | calculé | V | 1× Essence de vie + 2× Fibre de plante · Farmingbench |
| `Plant_Vine_Wall_Winter` | Liane |  | 42 | 65 | 6 argent 5 cuivres | calculé | V | 1× Essence de vie + 2× Fibre de plante · Farmingbench |

<a id="fam-ingredients"></a>
## Ingrédients

### Sans recette — prix fixés (57)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Ingredient_Bone_Fragment` | Fragments d’os | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Charcoal` | Charbon de bois |  |  | 10 | 1 argent | fixé | V |
| `Ingredient_Chitin_Sturdy` | Chitine solide | Peu commun | 25 | 38 | 3 argent 8 cuivres | fixé | V |
| `Ingredient_Crystal_Blue` | Fragments de cristal bleus |  | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Crystal_Cyan` | Fragments de cristal cyan |  | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Crystal_Green` | Fragments de cristal verts |  | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Crystal_Pink` | Fragments de cristal roses |  | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Crystal_Purple` | Fragments de cristal violets |  | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Crystal_Red` | Fragments de cristal rouges |  | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Crystal_Void` | Fragments de cristal du Néant | Peu commun | 15 | 34 | 3 argent 4 cuivres | fixé | V |
| `Ingredient_Crystal_White` | Fragments de cristal blancs |  | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Crystal_Yellow` | Fragments de cristal jaunes |  | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Deep_Resource` | Paquet de trésors | Rare | 25 | 150 | 1 or 5 argent | fixé | V |
| `Ingredient_Enkindled_Resource` | Sceau des Embrasés | Rare | 20 | 140 | 1 or 4 argent | fixé | V |
| `Ingredient_Fabric_Scrap_Cindercloth` | Chutes d’étoffe de braise | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Fabric_Scrap_Cotton` | Chutes de coton | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Fabric_Scrap_Linen` | Chutes de lin | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Fabric_Scrap_Prismaloom` | Chutes de tissu prismatique | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Fabric_Scrap_Shadoweave` | Chutes de tisse-ombre | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Fabric_Scrap_Silk` | Chutes de soie | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Fabric_Scrap_Stormsilk` | Chutes de soie des tempêtes | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Fabric_Scrap_Wool` | Chutes de laine | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Feathers_Blue` | Plumes bleues |  |  | 10 | 1 argent | fixé | V |
| `Ingredient_Feathers_Dark` | Plumes sombres |  |  | 10 | 1 argent | fixé | V |
| `Ingredient_Feathers_Light` | Plumes blanches |  |  | 10 | 1 argent | fixé | V |
| `Ingredient_Feathers_Red` | Plumes rouges |  |  | 10 | 1 argent | fixé | V |
| `Ingredient_Fibre` | Fibre de plante |  |  | 10 | 1 argent | fixé | V |
| `Ingredient_Fire_Essence` | Essence de feu | Peu commun |  | 30 | 3 argent | fixé | V |
| `Ingredient_Fishbone` | Arête de poisson | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Gobliterator_Core` | Noyau du Goblitérateur | Légendaire | 40 | 17 000 | 1 zénith 7 diamants | fixé | V |
| `Ingredient_Hide_Dark` | Cuir brut sombre | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Hide_Heavy` | Cuir brut dense | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Hide_Light` | Cuir brut fin | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Hide_Medium` | Cuir brut moyen | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Hide_Prismic` | Cuir brut prismatique | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Hide_Scaled` | Cuir brut écaillé | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Hide_Soft` | Cuir brut doux | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Hide_Storm` | Cuir brut de tempête | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Ice_Essence` | Essence de glace | Peu commun |  | 30 | 3 argent | fixé | V |
| `Ingredient_Life_Essence` | Essence de vie | Peu commun |  | 30 | 3 argent | fixé | V |
| `Ingredient_Lightning_Essence` | Essence de foudre | Peu commun |  | 30 | 3 argent | fixé | V |
| `Ingredient_Motes_Light` | Particules de lumière | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Poop` | Excrément |  |  | 10 | 1 argent | fixé | V |
| `Ingredient_Rune_Shard` | Fragment de pierre runique | Rare |  | 120 | 1 or 2 argent | fixé | V |
| `Ingredient_Sac_Venom` | Glande à venin | Peu commun | 25 | 38 | 3 argent 8 cuivres | fixé | V |
| `Ingredient_Scraps` | Ferraille | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Sinue_Cindersinue` | Tendon de braise | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Spider_Silk` | Soie d'araignée | Peu commun | 15 | 34 | 3 argent 4 cuivres | fixé | V |
| `Ingredient_Stick` | Bâton |  |  | 10 | 1 argent | fixé | V |
| `Ingredient_Strap_Leather` | Sangle en cuir | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Stud_Iron` | Rivet en fer | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Ingredient_Tree_Bark` | Écorce d’arbre |  |  | 10 | 1 argent | fixé | V |
| `Ingredient_Tree_Sap` | Sève |  |  | 10 | 1 argent | fixé | V |
| `Ingredient_Void_Essence` | Essence du Néant | Peu commun |  | 30 | 3 argent | fixé | V |
| `Ingredient_Voidheart` | Cœur du Néant | Épique | 30 | 650 | 6 or 5 argent | fixé | V |
| `Ingredient_Voidtaken_Grizzly_Hide` | Peau de grizzly corrompu du Néant | Rare | 20 | 140 | 1 or 4 argent | fixé | V |
| `Ingredient_Water_Essence` | Essence d’eau | Peu commun |  | 30 | 3 argent | fixé | V |

### Avec recette — prix calculés (38)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O  Recette |
|---|---|---|---:|---:|---|---|---|---|
| `Ingredient_Bolt_Cindercloth` | Rouleau d’étoffe de braise | Commun | 10 | 4 | 4 cuivres | calculé | V | 1× Coton · Loombench |
| `Ingredient_Bolt_Cotton` | Rouleau de coton | Commun | 10 | 4 | 4 cuivres | calculé | V | 1× Coton · Loombench |
| `Ingredient_Bolt_Linen` | Rouleau de lin | Commun | 10 | 4 | 4 cuivres | calculé | V | 1× Coton · Loombench |
| `Ingredient_Bolt_Prismaloom` | Rouleau de tissu prismatique | Commun | 10 | 4 | 4 cuivres | calculé | V | 1× Coton · Loombench |
| `Ingredient_Bolt_Shadoweave` | Rouleau de tisse-ombre | Commun | 10 | 4 | 4 cuivres | calculé | V | 1× Coton · Loombench |
| `Ingredient_Bolt_Silk` | Rouleau de soie | Commun | 10 | 4 | 4 cuivres | calculé | V | 1× Coton · Loombench |
| `Ingredient_Bolt_Stormsilk` | Rouleau de soie des tempêtes | Commun | 10 | 4 | 4 cuivres | calculé | V | 1× Coton · Loombench |
| `Ingredient_Bolt_Wool` | Rouleau de laine | Commun | 1 | 4 | 4 cuivres | calculé | V | 1× Chutes de laine → ×4 · Furniture_Bench |
| `Ingredient_Dough` | Pâte | Commun | 10 | 79 | 7 argent 9 cuivres | calculé | V | 1× Farine + 2× Œuf + 1× Chope (Filled Water) · Cookingbench |
| `Ingredient_Flour` | Farine | Commun | 10 | 39 | 3 argent 9 cuivres | calculé | V | 10× Blé · Cookingbench |
| `Ingredient_Gadget_Assembly` | Montage douteux | Peu commun | 12 | 450 | 4 or 5 argent | calculé | V | 25× Ferraille + 2× Fragments de cristal du Néant · GoblinLab_Workbench |
| `Ingredient_Leather_Dark` | Cuir sombre | Commun | 35 | 14 | 1 argent 4 cuivres | calculé | V | 1× Cuir brut sombre · Tannery |
| `Ingredient_Leather_Heavy` | Cuir épais | Commun | 20 | 14 | 1 argent 4 cuivres | calculé | V | 1× Cuir brut dense · Tannery |
| `Ingredient_Leather_Light` | Cuir léger | Commun | 10 | 14 | 1 argent 4 cuivres | calculé | V | 1× Cuir brut fin · Tannery |
| `Ingredient_Leather_Medium` | Cuir moyen | Commun | 15 | 14 | 1 argent 4 cuivres | calculé | V | 1× Cuir brut moyen · Tannery |
| `Ingredient_Leather_Prismic` | Cuir prismatique | Commun | 40 | 14 | 1 argent 4 cuivres | calculé | V | 1× Cuir brut prismatique · Tannery |
| `Ingredient_Leather_Scaled` | Cuir écailleux | Commun | 25 | 14 | 1 argent 4 cuivres | calculé | V | 1× Cuir brut écaillé · Tannery |
| `Ingredient_Leather_Soft` | Cuir souple | Commun | 5 | 14 | 1 argent 4 cuivres | calculé | V | 1× Cuir brut doux · Tannery |
| `Ingredient_Leather_Storm` | Cuir de tempête | Commun | 30 | 14 | 1 argent 4 cuivres | calculé | V | 1× Cuir brut de tempête · Tannery |
| `Ingredient_Life_Essence_100` | Essence de vie | Peu commun | 1 | 12 | 1 argent 2 cuivres | calculé | V | 1× Grande essence de vie → ×100 · Farmingbench |
| `Ingredient_Life_Essence_Aubergine` | Essence de vie | Peu commun | 7 | 0 | 0 | calculé | V | 1× Aubergine → ×8 · Farmingbench |
| `Ingredient_Life_Essence_Carrot` | Essence de vie | Peu commun | 3 | 0 | 0 | calculé | V | 1× Carotte → ×4 · Farmingbench |
| `Ingredient_Life_Essence_Cauliflower` | Essence de vie | Peu commun | 5 | 0 | 0 | calculé | V | 1× Chou-fleur → ×6 · Farmingbench |
| `Ingredient_Life_Essence_Chilli` | Essence de vie | Peu commun | 10 | 0 | 0 | calculé | V | 1× Piment → ×3 · Farmingbench |
| `Ingredient_Life_Essence_Concentrated` | Grande essence de vie | Rare |  | 3 900 | 3 diamants 9 or | calculé | V | 100× Essence de vie · Farmingbench |
| `Ingredient_Life_Essence_Corn` | Essence de vie | Peu commun | 4 | 0 | 0 | calculé | V | 1× Maïs · Farmingbench |
| `Ingredient_Life_Essence_Cotton` | Essence de vie | Peu commun | 11 | 0 | 0 | calculé | V | 1× Coton → ×3 · Farmingbench |
| `Ingredient_Life_Essence_Lettuce` | Essence de vie | Peu commun | 2 | 0 | 0 | calculé | V | 1× Laitue → ×3 · Farmingbench |
| `Ingredient_Life_Essence_Onion` | Essence de vie | Peu commun | 13 | 0 | 0 | calculé | V | 1× Oignon → ×4 · Farmingbench |
| `Ingredient_Life_Essence_Potato` | Essence de vie | Peu commun | 13 | 0 | 0 | calculé | V | 1× Pomme de terre → ×4 · Farmingbench |
| `Ingredient_Life_Essence_Pumpkin` | Essence de vie | Peu commun | 8 | 0 | 0 | calculé | V | 1× Citrouille → ×8 · Farmingbench |
| `Ingredient_Life_Essence_Rice` | Essence de vie | Peu commun | 12 | 0 | 0 | calculé | V | 1× Riz → ×3 · Farmingbench |
| `Ingredient_Life_Essence_Tomato` | Essence de vie | Peu commun | 9 | 0 | 0 | calculé | V | 1× Tomate → ×3 · Farmingbench |
| `Ingredient_Life_Essence_Turnip` | Essence de vie | Peu commun | 6 | 0 | 0 | calculé | V | 1× Navet → ×6 · Farmingbench |
| `Ingredient_Life_Essence_Wheat` | Essence de vie | Peu commun | 1 | 0 | 0 | calculé | V | 1× Blé · Farmingbench |
| `Ingredient_Powder_Boom` | Poudre explosive | Commun | 10 | 21 | 2 argent 1 cuivre | calculé | V | 2× Petit champiboum + 1× Charbon de bois · GoblinLab_Workbench |
| `Ingredient_Salt` | Sel | Commun | 10 | 1 | 1 cuivre | calculé | V | 1× Bloc de sel → ×5 · Cookingbench |
| `Ingredient_Spices` | Épices | Commun | 10 | 36 | 3 argent 6 cuivres | calculé | V | 1× [N’importe quel type de légume] + 5× [N’importe quelle fleur] · Cookingbench |

<a id="fam-poissons"></a>
## Poissons

### Sans recette — prix fixés (30)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Fish_Bluegill_Item` | Crapet arlequin | Commun |  | 10 | 1 argent | fixé | V |
| `Fish_Catfish_Item` | Poisson-chat | Commun |  | 10 | 1 argent | fixé | V |
| `Fish_Clownfish_Item` | Poisson-clown | Rare |  | 120 | 1 or 2 argent | fixé | V |
| `Fish_Crab_Item` | Crabe | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | V |
| `Fish_Eel_Moray_Item` | Murène | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | V |
| `Fish_Frostgill_Item` | Branchie-de-givre | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | V |
| `Fish_Jellyfish_Blue_Item` | Méduse bleue | Épique |  | 500 | 5 or | fixé | V |
| `Fish_Jellyfish_Cyan_Item` | Méduse cyan | Épique |  | 500 | 5 or | fixé | V |
| `Fish_Jellyfish_Green_Item` | Méduse verte | Épique |  | 500 | 5 or | fixé | V |
| `Fish_Jellyfish_Man_Of_War_Item` | Méduse physalie | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | V |
| `Fish_Jellyfish_Red_Item` | Méduse rouge | Épique |  | 500 | 5 or | fixé | V |
| `Fish_Jellyfish_Yellow_Item` | Méduse jaune | Épique |  | 500 | 5 or | fixé | V |
| `Fish_Lobster_Item` | Homard | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | V |
| `Fish_Minnow_Item` | Vairon | Commun |  | 10 | 1 argent | fixé | V |
| `Fish_Pike_Item` | Brochet | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | V |
| `Fish_Piranha_Black_Item` | Piranha noir | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | V |
| `Fish_Piranha_Item` | Piranha | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | V |
| `Fish_Pufferfish_Item` | Poisson-globe | Rare |  | 120 | 1 or 2 argent | fixé | V |
| `Fish_Salmon_Item` | Saumon | Rare |  | 120 | 1 or 2 argent | fixé | V |
| `Fish_Shark_Hammerhead_Item` | Requin-marteau | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | V |
| `Fish_Shellfish_Lava_Item` | Cœlacanthe de lave | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | V |
| `Fish_Snapjaw_Item` | Gueule d’acier | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | V |
| `Fish_Tang_Blue_Item` | Poisson chirurgien bleu | Peu commun |  | 30 | 3 argent | fixé | V |
| `Fish_Tang_Chevron_Item` | Poisson chirurgien à chevrons | Peu commun |  | 30 | 3 argent | fixé | V |
| `Fish_Tang_Lemon_Peel_Item` | Poisson chirurgien citron | Peu commun |  | 30 | 3 argent | fixé | V |
| `Fish_Tang_Sailfin_Item` | Poisson chirurgien à voile | Peu commun |  | 30 | 3 argent | fixé | V |
| `Fish_Trilobite_Black_Item` | Trilobite noir | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | V |
| `Fish_Trilobite_Item` | Trilobite | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | V |
| `Fish_Trout_Rainbow_Item` | Truite arc-en-ciel | Rare |  | 120 | 1 or 2 argent | fixé | V |
| `Fish_Whale_Humpback_Item` | Baleine à bosse | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | V |

<a id="fam-nourriture"></a>
## Nourriture

### Sans recette — prix fixés (43)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Benediction_Archeveque` | Benediction d'Archeveque | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | E |
| `Benediction_Cardinal` | Benediction de Cardinal | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | E |
| `Benediction_Pretre` | Benediction de Pretre | Épique |  | 500 | 5 or | fixé | E |
| `Consomable_Stat_Agilite_Five` | Pillule D'Agilite Superieure | Épique |  | 10 000 | 1 zénith | fixé | E |
| `Consomable_Stat_Agilite_One` | Pillule D'Agilite | Rare |  | 2 000 | 2 diamants | fixé | E |
| `Consomable_Stat_Agilite_Ten` | Pillule D'Agilite Ultime | Unique |  | 50 000 | 5 zéniths | fixé | E |
| `Consomable_Stat_Chance_Five` | Pillule De Chance Superieure | Épique |  | 10 000 | 1 zénith | fixé | E |
| `Consomable_Stat_Chance_One` | Pillule De Chance | Rare |  | 2 000 | 2 diamants | fixé | E |
| `Consomable_Stat_Chance_Ten` | Pillule De Chance Ultime | Unique |  | 50 000 | 5 zéniths | fixé | E |
| `Consomable_Stat_Endurance_Five` | Pillule D'Endurance Superieure | Épique |  | 10 000 | 1 zénith | fixé | E |
| `Consomable_Stat_Endurance_One` | Pillule D'Endurance | Rare |  | 2 000 | 2 diamants | fixé | E |
| `Consomable_Stat_Endurance_Ten` | Pillule D'Endurance Ultime | Unique |  | 50 000 | 5 zéniths | fixé | E |
| `Consomable_Stat_Force_Five` | Pillule De Force Superieure | Épique |  | 10 000 | 1 zénith | fixé | E |
| `Consomable_Stat_Force_One` | Pillule De Force | Rare |  | 2 000 | 2 diamants | fixé | E |
| `Consomable_Stat_Force_Ten` | Pillule De Force Ultime | Unique |  | 50 000 | 5 zéniths | fixé | E |
| `Consomable_Stat_Intelligence_Five` | Pillule D'Intelligence Superieure | Épique |  | 10 000 | 1 zénith | fixé | E |
| `Consomable_Stat_Intelligence_One` | Pillule D'Intelligence | Rare |  | 2 000 | 2 diamants | fixé | E |
| `Consomable_Stat_Intelligence_Ten` | Pillule D'Intelligence Ultime | Unique |  | 50 000 | 5 zéniths | fixé | E |
| `Consomable_Stat_Vitalite_Five` | Pillule De Vitalite Superieure | Épique |  | 10 000 | 1 zénith | fixé | E |
| `Consomable_Stat_Vitalite_One` | Pillule De Vitalite | Rare |  | 2 000 | 2 diamants | fixé | E |
| `Consomable_Stat_Vitalite_Ten` | Pillule De Vitalite Ultime | Unique |  | 50 000 | 5 zéniths | fixé | E |
| `Decret_Baron` | Decret de Baron | Épique |  | 500 | 5 or | fixé | E |
| `Decret_Chevalier` | Decret de Chevalier | Épique |  | 500 | 5 or | fixé | E |
| `Decret_Comte` | Decret de Comte | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | E |
| `Decret_Duc` | Decret de Duc | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | E |
| `Decret_Marquis` | Decret de Marquis | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | E |
| `Elixir_Archer` | Elixir de l'Archer | Unique |  | 2 500 | 2 diamants 5 or | fixé | E |
| `Elixir_Assassin` | Elixir de l'Assassin | Unique |  | 2 500 | 2 diamants 5 or | fixé | E |
| `Elixir_Complet` | Elixir Complet | Unique |  | 2 500 | 2 diamants 5 or | fixé | E |
| `Elixir_Guerrier` | Elixir du Guerrier | Unique |  | 2 500 | 2 diamants 5 or | fixé | E |
| `Elixir_Mage` | Elixir du Mage | Unique |  | 2 500 | 2 diamants 5 or | fixé | E |
| `Essence_Dignite` | Essence de Dignite | Divin |  | 300 000 | 3 astrales | fixé | E |
| `Food_Beef_Raw` | Bœuf cru | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Food_Candy_Cane` | Sucre d’orge |  | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Food_Chicken_Raw` | Poulet cru | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Food_Egg` | Œuf | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Food_Pork_Raw` | Porc cru | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Food_Wildmeat_Raw` | Viande sauvage crue | Commun | 10 | 11 | 1 argent 1 cuivre | fixé | V |
| `Halloween_Basket_Pumpkin` | Panier citrouille d’Halloween |  |  | 10 | 1 argent | fixé | V |
| `Halloween_Basket_Straw` | Panier de paille d’Halloween |  |  | 10 | 1 argent | fixé | V |
| `Parchemin_Relance` | Parchemin de Relance | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | E |
| `Tome_Experience` | Tome d'Experience | Épique |  | 3 000 | 3 diamants | fixé | E |
| `Tome_Niveau` | Tome de Niveau | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | E |

### Avec recette — prix calculés (21)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O  Recette |
|---|---|---|---:|---:|---|---|---|---|
| `Food_Bread` | Pain | Peu commun | 7 | 110 | 1 or 1 argent | calculé | V | 1× Pâte + 3× [N’importe quel combustible] · Cookingbench |
| `Food_Cheese` | Fromage | Peu commun | 8 | 0 | 0 | calculé | V | 1× [N’importe quel seau de lait] · Cookingbench |
| `Food_Fish_Grilled` | Poisson grillé | Commun | 10 | 17 | 1 argent 7 cuivres | calculé | V | 1× Poisson cru · Campfire |
| `Food_Fish_Raw` | Poisson cru | Commun | 1 | 13 | 1 argent 3 cuivres | calculé | V | 1× [N’importe quel poisson] · Cookingbench |
| `Food_Fish_Raw_Epic` | Food_Fish_Raw_Epic | Commun | 4 | 64 | 6 argent 4 cuivres | calculé | V | 1× [N’importe quel poisson épique] → ×8 · Cookingbench |
| `Food_Fish_Raw_Legendary` | Food_Fish_Raw_Legendary | Commun | 5 | 0 | 0 | calculé | V | 1× [N’importe quel poisson légendaire] → ×16 · Cookingbench |
| `Food_Fish_Raw_Rare` | Food_Fish_Raw_Rare | Commun | 3 | 0 | 0 | calculé | V | 1× [N’importe quel poisson rare] → ×4 · Cookingbench |
| `Food_Fish_Raw_Uncommon` | Food_Fish_Raw_Uncommon | Commun | 2 | 6 | 6 cuivres | calculé | V | 1× [N’importe quel poisson peu commun] → ×2 · Cookingbench |
| `Food_Kebab_Fruit` | Brochette de fruits | Peu commun | 2 | 29 | 2 argent 9 cuivres | calculé | V | 1× Bâton + 4× [N’importe quel type de fruit] · Cookingbench |
| `Food_Kebab_Meat` | Brochette de viande | Peu commun | 3 | 70 | 7 argent | calculé | V | 1× Bâton + 4× [N’importe quel type de viande crue] · Cookingbench |
| `Food_Kebab_Mushroom` | Brochette de champignons | Peu commun | 1 | 25 | 2 argent 5 cuivres | calculé | V | 1× Bâton + 3× [N’importe quel type de champignon] · Cookingbench |
| `Food_Kebab_Vegetable` | Brochette de légumes | Peu commun | 4 | 29 | 2 argent 9 cuivres | calculé | V | 1× Bâton + 4× [N’importe quel type de légume] · Cookingbench |
| `Food_Pie_Apple` | Tarte aux pommes | Rare | 11 | 170 | 1 or 7 argent | calculé | V | 1× Pâte + 3× Pomme + 1× Épices + 3× [N’importe quel combustible] · Cookingbench |
| `Food_Pie_Meat` | Tourte à la viande | Rare | 12 | 170 | 1 or 7 argent | calculé | V | 1× Pâte + 1× [N’importe quel type de viande crue] + 1× Épices + 1× Sel + 3× [N’importe quel combustible] · Cookingbench |
| `Food_Pie_Pumpkin` | Tarte à la citrouille | Rare | 13 | 160 | 1 or 6 argent | calculé | V | 1× Pâte + 1× Citrouille + 1× Épices + 3× [N’importe quel combustible] · Cookingbench |
| `Food_Popcorn` | Popcorn | Peu commun | 10 | 13 | 1 argent 3 cuivres | calculé | V | 2× Maïs + 1× Sel + 3× [N’importe quel combustible] · Cookingbench |
| `Food_Salad_Berry` | Salade de baies sauvages | Peu commun | 6 | 23 | 2 argent 3 cuivres | calculé | V | 1× Laitue + 5× Baies sauvages · Cookingbench |
| `Food_Salad_Caesar` | Salade César | Rare | 9 | 66 | 6 argent 6 cuivres | calculé | V | 1× Laitue + 1× Fromage + 1× [N’importe quel type de viande crue] + 1× Sel + 1× Épices · Cookingbench |
| `Food_Salad_Mushroom` | Salade de champignons | Peu commun | 5 | 16 | 1 argent 6 cuivres | calculé | V | 1× Laitue + 3× [N’importe quel type de champignon] · Cookingbench |
| `Food_Vegetable_Cooked` | Légumes rôtis | Commun | 10 | 4 | 4 cuivres | calculé | V | 1× [N’importe quel type de légume] · Campfire |
| `Food_Wildmeat_Cooked` | Viande sauvage cuite | Commun | 10 | 14 | 1 argent 4 cuivres | calculé | V | 1× [N’importe quel type de viande crue] · Campfire |

<a id="fam-potions"></a>
## Potions

### Sans recette — prix fixés (21)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Decorative_Potion_Template` | Modèle de potion décorative |  |  | 20 | 2 argent | fixé | V |
| `Potion_Health_Large` | Grande potion de soin | Rare | 2 | 240 | 2 or 4 argent | fixé | V |
| `Potion_Mana` | Potion décorative (bleue) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Mana_Large` | Grande potion décorative (bleue) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Mana_Small` | Petite potion décorative (bleue) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Poison` | Potion décorative (verte) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Poison_Large` | Grande potion décorative (verte) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Poison_Minor` | Petite potion décorative (verte) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Purify` | Potion décorative (iridescente) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Regen_Health` | Potion décorative (rose) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Regen_Health_Large` | Grande potion décorative (rose) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Regen_Health_Small` | Petite potion décorative (rose) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Regen_Mana` | Potion décorative (violette) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Regen_Mana_Large` | Grande potion décorative (violette) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Regen_Mana_Small` | Petite potion décorative (violette) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Regen_Stamina` | Potion décorative (orange) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Regen_Stamina_Large` | Grande potion décorative (orange) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Regen_Stamina_Small` | Petite potion décorative (orange) |  |  | 20 | 2 argent | fixé | V |
| `Potion_Signature_Large` | Grande potion d’énergie | Rare | 12 | 270 | 2 or 7 argent | fixé | V |
| `Potion_Stamina_Large` | Grande potion d’endurance | Rare |  | 240 | 2 or 4 argent | fixé | V |
| `Potion_Template` | Modèle de potion |  |  | 20 | 2 argent | fixé | V |

### Avec recette — prix calculés (44)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O  Recette |
|---|---|---|---:|---:|---|---|---|---|
| `Eld_Potion_Antidote` | Antidote Ancien | Peu commun | 50 | 200 | 2 or | calculé | E | 1× Petite fiole + 1× Glande à Venin + 1× Soie d'Araignée · Alchemybench |
| `Eld_Potion_Antigel` | Potion Antigel | Rare | 300 | 300 | 3 or | calculé | E | 1× Fiole vide + 1× Branchie Gelée + 1× Perle de Givre · Alchemybench |
| `Eld_Potion_Arcane` | Potion d'Arcane | Épique | 600 | 1 300 | 1 diamant 3 or | calculé | E | 1× Fiole vide + 1× Âme du Vide · Alchemybench |
| `Eld_Potion_Bouclier` | Potion de Bouclier | Épique | 500 | 82 | 8 argent 2 cuivres | calculé | E | 1× Fiole vide + 1× Plaque Royale · Alchemybench |
| `Eld_Potion_Celerite` | Potion de Célérité | Rare | 300 | 310 | 3 or 1 argent | calculé | E | 1× Petite fiole + 1× Griffe de Raptor · Alchemybench |
| `Eld_Potion_Force_Vitale` | Potion de Force Vitale | Épique | 700 | 1 300 | 1 diamant 3 or | calculé | E | 1× Grande fiole + 1× Cœur de Rex · Alchemybench |
| `Eld_Potion_Fortifiant` | Fortifiant du Marcheur | Peu commun | 100 | 82 | 8 argent 2 cuivres | calculé | E | 1× Fiole vide + 1× Graisse de Baleine · Alchemybench |
| `Eld_Potion_Fortune` | Potion de Fortune | Légendaire | 700 | 210 | 2 or 1 argent | calculé | E | 1× Fiole vide + 1× Rire Figé · Alchemybench |
| `Eld_Potion_Ignifuge` | Potion Ignifuge | Rare | 300 | 36 | 3 argent 6 cuivres | calculé | E | 1× Fiole vide + 1× Écaille de Lézard + 1× Bave de Magma · Alchemybench |
| `Eld_Potion_Mana` | Potion de Mana | Peu commun | 200 | 1 300 | 1 diamant 3 or | calculé | E | 1× Fiole vide + 1× Âme de Givre · Alchemybench |
| `Eld_Potion_Mana_Grande` | Grande Potion de Mana | Rare | 500 | 1 300 | 1 diamant 3 or | calculé | E | 1× Grande fiole + 1× Noyau de Givre · Alchemybench |
| `Eld_Potion_Mana_Petite` | Petite Potion de Mana | Commun | 50 | 81 | 8 argent 1 cuivre | calculé | E | 1× Petite fiole + 1× Flocon Spirituel · Alchemybench |
| `Eld_Potion_Mana_Vive` | Élixir de Mana Vive | Épique | 600 | 1 600 | 1 diamant 6 or | calculé | E | 1× Fiole vide + 1× Âme de Foudre + 1× Larme Gelée · Alchemybench |
| `Eld_Potion_Peau_Pierre` | Potion de Peau de Pierre | Épique | 500 | 340 | 3 or 4 argent | calculé | E | 1× Grande fiole + 1× Pierre Vive + 1× Carapace de Scorpion · Alchemybench |
| `Eld_Potion_Plume` | Potion de Plume | Peu commun | 200 | 81 | 8 argent 1 cuivre | calculé | E | 1× Petite fiole + 1× Plume d'Archéoptéryx · Alchemybench |
| `Eld_Potion_Precision` | Potion de Précision | Épique | 500 | 220 | 2 or 2 argent | calculé | E | 1× Fiole vide + 1× Œil de Léopard · Alchemybench |
| `Eld_Potion_Rage` | Potion de Rage | Épique | 500 | 150 | 1 or 5 argent | calculé | E | 1× Fiole vide + 1× Griffe de Tigre + 1× Croc de Braise · Alchemybench |
| `Eld_Potion_Regeneration` | Baume de Régénération | Peu commun | 100 | 360 | 3 or 6 argent | calculé | E | 1× Petite fiole + 1× Pulpe de Cactee + 1× Sève Spirituelle · Alchemybench |
| `Eld_Potion_Sagesse` | Potion de Sagesse | Épique | 600 | 82 | 8 argent 2 cuivres | calculé | E | 1× Fiole vide + 1× Pupille du Vide · Alchemybench |
| `Eld_Potion_Tonique` | Tonique du Coureur | Commun | 50 | 250 | 2 or 5 argent | calculé | E | 1× Petite fiole + 1× Glande de Crapaud · Alchemybench |
| `Eld_Potion_Vie` | Potion de Vie | Peu commun | 100 | 520 | 5 or 2 argent | calculé | E | 1× Fiole vide + 1× Cœur de Loup + 1× Sève Spirituelle · Alchemybench |
| `Eld_Potion_Vie_Grande` | Grande Potion de Vie | Rare | 300 | 560 | 5 or 6 argent | calculé | E | 1× Fiole vide + 1× Cœur d'Ours + 1× Sève Spirituelle · Alchemybench |
| `Eld_Potion_Vie_Supreme` | Potion de Vie Suprême | Épique | 600 | 2 600 | 2 diamants 6 or | calculé | E | 1× Grande fiole + 1× Cœur de Yéti + 1× Âme des Racines · Alchemybench |
| `Potion_Antidote` | Antidote | Commun | 15 | 200 | 2 or | calculé | V | 1× Fiole vide + 5× Fibre de plante + 2× Essence de vie + 1× Glande à venin · Alchemybench |
| `Potion_Empty` | Fiole vide |  |  | 3 | 3 cuivres | calculé | V | 1× [N’importe quel sable] · Furnace |
| `Potion_Empty_Large` | Grande fiole |  | 16 | 8 | 8 cuivres | calculé | V | 2× Fiole vide · Alchemybench |
| `Potion_Empty_Small` | Petite fiole |  |  | 2 | 2 cuivres | calculé | V | 1× Fiole vide → ×2 · Alchemybench |
| `Potion_Health` | Potion de soin | Commun | 7 | 94 | 9 argent 4 cuivres | calculé | V | 1× Fiole vide + 12× Baies sauvages + 6× Pétales écarlates + 1× Amanite écarlate · Alchemybench |
| `Potion_Health_Greater` | Potion de soin majeure | Peu commun | 12 | 130 | 1 or 3 argent | calculé | V | 1× Potion de soin + 1× Népenthès écarlate · Alchemybench |
| `Potion_Health_Lesser` | Potion de Soin Mineure | Commun | 1 | 46 | 4 argent 6 cuivres | calculé | V | 1× Petite fiole + 6× Baies sauvages + 3× Pétales écarlates · Alchemybench |
| `Potion_Health_Small` | Petite potion de soin | Peu commun | 4 | 64 | 6 argent 4 cuivres | calculé | V | 1× Potion de Soin Mineure + 1× Rose écarlate · Alchemybench |
| `Potion_Morph_Dog` | Potion de métamorphose en chien | Rare | 50 | 410 | 4 or 1 argent | calculé | V | 1× Fiole vide + 10× Essence de vie + 1× Fragments d’os · Alchemybench |
| `Potion_Morph_Frog` | Potion de métamorphose en grenouille | Rare | 51 | 400 | 4 or | calculé | V | 1× Fiole vide + 10× Essence de vie + 1× Nénuphar · Alchemybench |
| `Potion_Morph_Mouse` | Potion de métamorphose en souris | Rare | 53 | 390 | 3 or 9 argent | calculé | V | 1× Fiole vide + 10× Essence de vie + 1× Fromage · Alchemybench |
| `Potion_Morph_Pigeon` | Potion de métamorphose en pigeon | Rare | 52 | 1 800 | 1 diamant 8 or | calculé | V | 1× Fiole vide + 10× Essence de vie + 1× Dagues en fer · Alchemybench |
| `Potion_Signature` | Potion d’énergie | Commun | 9 | 94 | 9 argent 4 cuivres | calculé | V | 1× Fiole vide + 12× Fruit azuré + 6× Pétales azurés + 1× Amanite azurée · Alchemybench |
| `Potion_Signature_Greater` | Potion d’énergie majeure | Peu commun | 14 | 130 | 1 or 3 argent | calculé | V | 1× Potion d’énergie + 1× Algue azurée · Alchemybench |
| `Potion_Signature_Lesser` | Potion d’énergie mineure | Commun | 3 | 46 | 4 argent 6 cuivres | calculé | V | 1× Petite fiole + 6× Fruit azuré + 3× Pétales azurés · Alchemybench |
| `Potion_Signature_Small` | Petite potion d’énergie | Peu commun | 6 | 64 | 6 argent 4 cuivres | calculé | V | 1× Potion d’énergie mineure + 1× Fougère azurée · Alchemybench |
| `Potion_Stamina` | Potion d’endurance | Commun | 8 | 120 | 1 or 2 argent | calculé | V | 1× Fiole vide + 12× Tournesol + 6× Pétales de tempête + 1× Fongéclat · Alchemybench |
| `Potion_Stamina_Greater` | Potion d’endurance majeure | Peu commun | 13 | 160 | 1 or 6 argent | calculé | V | 1× Potion d’endurance + 1× Jonc des tempêtes · Alchemybench |
| `Potion_Stamina_Lesser` | Potion d’endurance mineure | Commun | 2 | 61 | 6 argent 1 cuivre | calculé | V | 1× Petite fiole + 6× Tournesol + 3× Pétales de tempête · Alchemybench |
| `Potion_Stamina_Small` | Petite potion d’endurance | Peu commun | 5 | 83 | 8 argent 3 cuivres | calculé | V | 1× Potion d’endurance mineure + 1× Chardon des tempêtes · Alchemybench |
| `Potion_Stats_Small` | Potion de Soin Mineure | Légendaire | 10 | 20 | 2 argent | calculé | E | 3× Fiole vide + 6× Baies sauvages + 1× Rose écarlate + 3× Pétales écarlates → ×3 · Alchemybench |

<a id="fam-butins-de-monstres"></a>
## Butins de monstres

### Sans recette — prix fixés (285)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Butin_Ambre_Gris` | Ambre Gris | Épique | 700 | 1 000 | 1 diamant | fixé | E |
| `Butin_Ame_Braise` | Âme de Braise | Épique | 635 | 1 000 | 1 diamant | fixé | E |
| `Butin_Ame_Errante` | Âme Errante | Épique | 350 | 1 000 | 1 diamant | fixé | E |
| `Butin_Ame_Foudre` | Âme de Foudre | Épique | 660 | 1 000 | 1 diamant | fixé | E |
| `Butin_Ame_Givre` | Âme de Givre | Épique | 615 | 1 000 | 1 diamant | fixé | E |
| `Butin_Ame_Racines` | Âme des Racines | Épique | 600 | 1 000 | 1 diamant | fixé | E |
| `Butin_Ame_Vide` | Âme du Vide | Épique | 580 | 1 000 | 1 diamant | fixé | E |
| `Butin_Antenne_Trilobite` | Antenne de Trilobite | Rare | 740 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Arc_Fige` | Arc Figé | Rare | 660 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Bandage_Ensable` | Bandage Ensablé | Rare | 335 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Bandelette_Antique` | Bandelette Antique | Peu commun | 270 | 60 | 6 argent | fixé | E |
| `Butin_Bave_Magma` | Bave de Magma | Commun | 25 | 12 | 1 argent 2 cuivres | fixé | E |
| `Butin_Bec_Pterodactyle` | Bec de Ptérodactyle | Épique | 750 | 1 000 | 1 diamant | fixé | E |
| `Butin_Bile_Goule` | Bile de Goule | Épique | 385 | 1 000 | 1 diamant | fixé | E |
| `Butin_Bobine_Vivante` | Bobine Vivante | Rare | 320 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Bourse_Volee` | Bourse Volée | Rare | 40 | 170 | 1 or 7 argent | fixé | E |
| `Butin_Braise_Vive` | Braise Vive | Rare | 635 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Branchie_Gelee` | Branchie Gelée | Peu commun | 50 | 45 | 4 argent 5 cuivres | fixé | E |
| `Butin_Canine_Sabre` | Canine de Sabre | Rare | 70 | 200 | 2 or | fixé | E |
| `Butin_Cape_Ombre` | Cape d'Ombre | Rare | 750 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Carapace_Scorpion` | Carapace de Scorpion | Commun | 40 | 14 | 1 argent 4 cuivres | fixé | E |
| `Butin_Carapace_Trilobite` | Carapace de Trilobite | Peu commun | 740 | 60 | 6 argent | fixé | E |
| `Butin_Carte_Au_Tresor` | Carte au Trésor | Épique | 30 | 650 | 6 or 5 argent | fixé | E |
| `Butin_Cendre_Spirituelle` | Cendre Spirituelle | Peu commun | 635 | 60 | 6 argent | fixé | E |
| `Butin_Cendres_Alchimiste` | Cendres d'Alchimiste | Rare | 210 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Cerveau_Fletri` | Cerveau Flétri | Épique | 330 | 1 000 | 1 diamant | fixé | E |
| `Butin_Chair_Carbonisee` | Chair Carbonisée | Peu commun | 345 | 60 | 6 argent | fixé | E |
| `Butin_Chair_Gelee` | Chair Gelée | Peu commun | 340 | 60 | 6 argent | fixé | E |
| `Butin_Chair_Mutee` | Chair Mutée | Peu commun | 395 | 60 | 6 argent | fixé | E |
| `Butin_Chair_Putride` | Chair Putride | Peu commun | 330 | 60 | 6 argent | fixé | E |
| `Butin_Chair_Sechee` | Chair Séchée | Peu commun | 335 | 60 | 6 argent | fixé | E |
| `Butin_Chair_Vide` | Chair du Vide | Peu commun | 600 | 60 | 6 argent | fixé | E |
| `Butin_Chant_Fige` | Chant Figé | Légendaire | 700 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Chitine_Reine` | Chitine de Reine | Peu commun | 865 | 60 | 6 argent | fixé | E |
| `Butin_Chitine_Scarak` | Chitine de Scarak | Peu commun | 800 | 60 | 6 argent | fixé | E |
| `Butin_Cle_Voute` | Clé de Voûte | Épique | 530 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Aberrant` | Cœur Aberrant | Légendaire | 395 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Coeur_Bleme` | Cœur Blême | Épique | 355 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Braise` | Cœur de Braise | Rare | 250 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Coeur_Brute` | Cœur de Brute | Épique | 530 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Calcine` | Cœur Calciné | Épique | 345 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Chef` | Cœur de Chef | Épique | 460 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Corrompu` | Cœur Corrompu | Épique | 600 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Desseche` | Cœur Desséché | Épique | 335 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Dragon_Givre` | Cœur de Dragon de Givre | Épique | 920 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Etincelle` | Cœur d'Étincelle | Épique | 320 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Gele` | Cœur Gelé | Épique | 340 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Hedera` | Cœur d'Hedera | Épique | 650 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Loup` | Cœur de Loup | Rare | 30 | 160 | 1 or 6 argent | fixé | E |
| `Butin_Coeur_Loup_Garou` | Cœur de Loup-garou | Épique | 500 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Ombre` | Cœur d'Ombre | Épique | 750 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Ours` | Cœur d'Ours | Rare | 60 | 190 | 1 or 9 argent | fixé | E |
| `Butin_Coeur_Rejeton` | Cœur de Rejeton | Épique | 600 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Rex` | Cœur de Rex | Épique | 805 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Snapdragon` | Cœur de Snapdragon | Épique | 860 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Trillodon` | Cœur de Trillodon | Épique | 785 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coeur_Yeti` | Cœur de Yéti | Épique | 550 | 1 000 | 1 diamant | fixé | E |
| `Butin_Collier_Meute` | Collier de Meute | Épique | 410 | 1 000 | 1 diamant | fixé | E |
| `Butin_Coquille_Magma` | Coquille de Magma | Peu commun | 25 | 38 | 3 argent 8 cuivres | fixé | E |
| `Butin_Corne_Crapaud` | Corne de Crapaud | Peu commun | 60 | 48 | 4 argent 8 cuivres | fixé | E |
| `Butin_Corne_Dragon_Feu` | Corne du Dragon de Feu | Légendaire | 940 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Corne_Dragon_Givre` | Corne du Dragon de Givre | Légendaire | 920 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Corne_Trillodon` | Corne de Trillodon | Rare | 785 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Couronne_Rejeton` | Couronne du Rejeton | Légendaire | 600 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Couronne_Ruche` | Couronne de la Ruche | Légendaire | 865 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Couronne_Spectrale` | Couronne Spectrale | Légendaire | 350 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Couronne_Trork` | Couronne Trork | Légendaire | 460 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Couteau_Boucher` | Couteau de Boucher | Rare | 95 | 230 | 2 or 3 argent | fixé | E |
| `Butin_Crane_Capitaine` | Crâne du Capitaine | Légendaire | 30 | 16 000 | 1 zénith 6 diamants | fixé | E |
| `Butin_Crane_Fele` | Crâne Fêlé | Peu commun | 15 | 34 | 3 argent 4 cuivres | fixé | E |
| `Butin_Crane_Fumant` | Crâne Fumant | Rare | 345 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Crane_Givre` | Crâne de Givre | Peu commun | 150 | 60 | 6 argent | fixé | E |
| `Butin_Crane_Incandescent` | Crâne Incandescent | Peu commun | 215 | 60 | 6 argent | fixé | E |
| `Butin_Crane_Loup_Garou` | Crâne de Loup-garou | Légendaire | 500 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Crane_Noirci` | Crâne Noirci | Peu commun | 210 | 60 | 6 argent | fixé | E |
| `Butin_Crane_Yeti` | Crâne de Yéti | Légendaire | 550 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Crete_Snapdragon` | Crête de Snapdragon | Légendaire | 860 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Crin_Spectral` | Crin Spectral | Peu commun | 80 | 54 | 5 argent 4 cuivres | fixé | E |
| `Butin_Cristal_Ardent` | Cristal Ardent | Rare | 740 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Cristal_Charge` | Cristal Chargé | Rare | 695 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Croc_Bleme` | Croc Blême | Rare | 355 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Croc_Braise` | Croc de Braise | Peu commun | 250 | 60 | 6 argent | fixé | E |
| `Butin_Croc_Hyene` | Croc de Hyène | Peu commun | 30 | 39 | 3 argent 9 cuivres | fixé | E |
| `Butin_Croc_Loup` | Croc de Loup | Peu commun | 30 | 39 | 3 argent 9 cuivres | fixé | E |
| `Butin_Croc_Loup_Trork` | Croc de Loup Trork | Rare | 410 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Croc_Maudit` | Croc Maudit | Rare | 480 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Croc_Murene` | Croc de Murène | Peu commun | 40 | 42 | 4 argent 2 cuivres | fixé | E |
| `Butin_Croc_Serpent` | Croc de Serpent | Peu commun | 20 | 36 | 3 argent 6 cuivres | fixé | E |
| `Butin_Croc_Snapjaw` | Croc de Snapjaw | Peu commun | 40 | 42 | 4 argent 2 cuivres | fixé | E |
| `Butin_Cuir_Trillodon` | Cuir de Trillodon | Peu commun | 785 | 60 | 6 argent | fixé | E |
| `Butin_Cuir_Trork` | Cuir de Trork | Peu commun | 400 | 60 | 6 argent | fixé | E |
| `Butin_Dard_Reine` | Dard de Reine | Rare | 865 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Dard_Scorpion` | Dard de Scorpion | Rare | 40 | 170 | 1 or 7 argent | fixé | E |
| `Butin_Defense_Chef` | Défense de Chef | Rare | 460 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Defense_Trork` | Défense de Trork | Rare | 400 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Dent_Corrompue` | Dent Corrompue | Rare | 560 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Dent_Crocodile` | Dent de Crocodile | Peu commun | 50 | 45 | 4 argent 5 cuivres | fixé | E |
| `Butin_Dent_En_Or` | Dent en Or | Peu commun | 25 | 38 | 3 argent 8 cuivres | fixé | E |
| `Butin_Dent_Gobelin` | Dent de Gobelin | Peu commun | 40 | 42 | 4 argent 2 cuivres | fixé | E |
| `Butin_Dent_Piranha` | Dent de Piranha | Peu commun | 20 | 36 | 3 argent 6 cuivres | fixé | E |
| `Butin_Dent_Pourrie` | Dent Pourrie | Rare | 330 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Dent_Requin` | Dent de Requin | Rare | 80 | 220 | 2 or 2 argent | fixé | E |
| `Butin_Doigt_Gele` | Doigt Gelé | Rare | 340 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Ecaille_Gelee` | Écaille Gelée | Commun | 50 | 15 | 1 argent 5 cuivres | fixé | E |
| `Butin_Ecaille_Lezard` | Écaille de Lézard | Commun | 30 | 13 | 1 argent 3 cuivres | fixé | E |
| `Butin_Ecaille_Piranha` | Écaille de Piranha | Commun | 20 | 12 | 1 argent 2 cuivres | fixé | E |
| `Butin_Ecaille_Snapdragon` | Écaille de Snapdragon | Peu commun | 860 | 60 | 6 argent | fixé | E |
| `Butin_Ecaille_Snapjaw` | Écaille de Snapjaw | Commun | 40 | 14 | 1 argent 4 cuivres | fixé | E |
| `Butin_Eclat_Anomalie` | Éclat d'Anomalie | Peu commun | 530 | 60 | 6 argent | fixé | E |
| `Butin_Eclat_Armure_Noire` | Éclat d'Armure Noire | Peu commun | 750 | 60 | 6 argent | fixé | E |
| `Butin_Eclat_Flamme` | Éclat de Flamme | Peu commun | 740 | 60 | 6 argent | fixé | E |
| `Butin_Eclat_Foudre` | Éclat de Foudre | Peu commun | 695 | 60 | 6 argent | fixé | E |
| `Butin_Eclat_Givre` | Éclat de Givre | Peu commun | 710 | 60 | 6 argent | fixé | E |
| `Butin_Eclat_Sable` | Éclat de Sable | Peu commun | 685 | 60 | 6 argent | fixé | E |
| `Butin_Eclat_Terre` | Éclat de Terre | Peu commun | 670 | 60 | 6 argent | fixé | E |
| `Butin_Ecorce_Ronceuse` | Écorce Ronceuse | Peu commun | 60 | 48 | 4 argent 8 cuivres | fixé | E |
| `Butin_Ectoplasme_Noir` | Ectoplasme Noir | Rare | 580 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Epine_Cactee` | Épine de Cactee | Commun | 20 | 12 | 1 argent 2 cuivres | fixé | E |
| `Butin_Epine_Hedera` | Épine d'Hedera | Rare | 650 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Etincelle_Spirituelle` | Étincelle Spirituelle | Peu commun | 660 | 60 | 6 argent | fixé | E |
| `Butin_Fanon_Baleine` | Fanon de Baleine | Rare | 700 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Fer_Maudit` | Fer Maudit | Rare | 80 | 220 | 2 or 2 argent | fixé | E |
| `Butin_Ferraille_Lourde` | Ferraille Lourde | Peu commun | 100 | 60 | 6 argent | fixé | E |
| `Butin_Fetiche_Trork` | Fétiche Trork | Rare | 440 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Fiole_Felee` | Fiole Fêlée | Commun | 60 | 16 | 1 argent 6 cuivres | fixé | E |
| `Butin_Flamme_Eternelle` | Flamme Éternelle | Rare | 280 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Fleur_Cactee` | Fleur de Cactee | Rare | 20 | 140 | 1 or 4 argent | fixé | E |
| `Butin_Fleur_Hedera` | Fleur d'Hedera | Légendaire | 650 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Flocon_Spirituel` | Flocon Spirituel | Peu commun | 615 | 60 | 6 argent | fixé | E |
| `Butin_Foie_Requin` | Foie de Requin | Épique | 80 | 900 | 9 or | fixé | E |
| `Butin_Formule_Secrete` | Formule Secrète | Rare | 60 | 190 | 1 or 9 argent | fixé | E |
| `Butin_Fossile_Trilobite` | Fossile de Trilobite | Épique | 740 | 1 000 | 1 diamant | fixé | E |
| `Butin_Fourrure_Loup_Garou` | Fourrure de Loup-garou | Peu commun | 500 | 60 | 6 argent | fixé | E |
| `Butin_Fourrure_Outlander` | Fourrure Outlander | Peu commun | 460 | 60 | 6 argent | fixé | E |
| `Butin_Fourrure_Yeti` | Fourrure de Yéti | Peu commun | 550 | 60 | 6 argent | fixé | E |
| `Butin_Glace_Eternelle` | Glace Éternelle | Rare | 710 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Glande_Crapaud` | Glande de Crapaud | Rare | 60 | 190 | 1 or 9 argent | fixé | E |
| `Butin_Glande_Scarak` | Glande de Scarak | Épique | 800 | 1 000 | 1 diamant | fixé | E |
| `Butin_Glande_Venin` | Glande à Venin | Rare | 20 | 140 | 1 or 4 argent | fixé | E |
| `Butin_Graine_Maudite` | Graine Maudite | Rare | 60 | 190 | 1 or 9 argent | fixé | E |
| `Butin_Graisse_Baleine` | Graisse de Baleine | Peu commun | 700 | 60 | 6 argent | fixé | E |
| `Butin_Griffe_Corrompue` | Griffe Corrompue | Rare | 600 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Griffe_Dragon_Feu` | Griffe de Dragon de Feu | Rare | 940 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Griffe_Dragon_Givre` | Griffe de Dragon de Givre | Rare | 920 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Griffe_Fen` | Griffe de Fen | Peu commun | 100 | 60 | 6 argent | fixé | E |
| `Butin_Griffe_Goule` | Griffe de Goule | Peu commun | 385 | 60 | 6 argent | fixé | E |
| `Butin_Griffe_Leopard` | Griffe de Léopard | Peu commun | 40 | 42 | 4 argent 2 cuivres | fixé | E |
| `Butin_Griffe_Lezard` | Griffe de Lézard | Peu commun | 30 | 39 | 3 argent 9 cuivres | fixé | E |
| `Butin_Griffe_Loup_Garou` | Griffe de Loup-garou | Rare | 500 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Griffe_Mutante` | Griffe Mutante | Rare | 395 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Griffe_Ours` | Griffe d'Ours | Peu commun | 60 | 48 | 4 argent 8 cuivres | fixé | E |
| `Butin_Griffe_Rampant` | Griffe de Rampant | Rare | 530 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Griffe_Raptor` | Griffe de Raptor | Rare | 770 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Griffe_Rejeton` | Griffe de Rejeton | Rare | 600 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Griffe_Rex` | Griffe de Rex | Rare | 805 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Griffe_Snapdragon` | Griffe de Snapdragon | Rare | 860 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Griffe_Tigre` | Griffe de Tigre | Peu commun | 70 | 51 | 5 argent 1 cuivre | fixé | E |
| `Butin_Griffe_Yeti` | Griffe de Yéti | Rare | 550 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Grimoire_Dechire` | Grimoire Déchiré | Épique | 465 | 1 000 | 1 diamant | fixé | E |
| `Butin_Heaume_L_Ombre` | Heaume de l'Ombre | Légendaire | 750 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Insigne_Ruche` | Insigne de la Ruche | Épique | 845 | 1 000 | 1 diamant | fixé | E |
| `Butin_Lame_Chitine` | Lame de Chitine | Rare | 845 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Lame_Ebrechee` | Lame Ébréchée | Rare | 460 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Langue_Goule` | Langue de Goule | Rare | 385 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Larme_Gelee` | Larme Gelée | Rare | 615 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Larme_Spectre` | Larme de Spectre | Rare | 350 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Larve_Cristallisee` | Larve Cristallisée | Épique | 530 | 1 000 | 1 diamant | fixé | E |
| `Butin_Liane_Hedera` | Liane d'Hedera | Peu commun | 650 | 60 | 6 argent | fixé | E |
| `Butin_Longue_Vue_Brisee` | Longue-vue Brisée | Rare | 30 | 160 | 1 or 6 argent | fixé | E |
| `Butin_Machoire_Brute` | Mâchoire de Brute | Rare | 530 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Machoire_Piranha` | Mâchoire de Piranha | Rare | 20 | 140 | 1 or 4 argent | fixé | E |
| `Butin_Machoire_Rex` | Mâchoire de Rex | Légendaire | 805 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Machoire_Snapjaw` | Mâchoire de Snapjaw | Rare | 40 | 170 | 1 or 7 argent | fixé | E |
| `Butin_Mandibule_Scarak` | Mandibule de Scarak | Rare | 800 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Masse_Brute` | Masse de Brute | Légendaire | 530 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Meche_Spectrale` | Mèche Spectrale | Commun | 280 | 20 | 2 argent | fixé | E |
| `Butin_Membrane_Pterodactyle` | Membrane de Ptérodactyle | Peu commun | 750 | 60 | 6 argent | fixé | E |
| `Butin_Moelle_Glacee` | Moelle Glacée | Rare | 150 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Moelle_Spectrale` | Moelle Spectrale | Rare | 15 | 140 | 1 or 4 argent | fixé | E |
| `Butin_Moteur_Gobelin` | Moteur Gobelin | Épique | 100 | 1 000 | 1 diamant | fixé | E |
| `Butin_Mucus_Vide` | Mucus du Vide | Peu commun | 530 | 60 | 6 argent | fixé | E |
| `Butin_Nerf_Optique` | Nerf Optique | Rare | 540 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Noyau_Firesteel` | Noyau de Firesteel | Épique | 725 | 1 000 | 1 diamant | fixé | E |
| `Butin_Noyau_Flamme` | Noyau de Flamme | Épique | 740 | 1 000 | 1 diamant | fixé | E |
| `Butin_Noyau_Foudre` | Noyau de Foudre | Épique | 695 | 1 000 | 1 diamant | fixé | E |
| `Butin_Noyau_Gardien` | Noyau du Gardien | Épique | 850 | 1 000 | 1 diamant | fixé | E |
| `Butin_Noyau_Givre` | Noyau de Givre | Épique | 710 | 1 000 | 1 diamant | fixé | E |
| `Butin_Noyau_Magma` | Noyau de Magma | Rare | 215 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Noyau_Sable` | Noyau de Sable | Épique | 685 | 1 000 | 1 diamant | fixé | E |
| `Butin_Noyau_Terre` | Noyau de Terre | Épique | 670 | 1 000 | 1 diamant | fixé | E |
| `Butin_Oeil_Crocodile` | Œil de Crocodile | Rare | 50 | 180 | 1 or 8 argent | fixé | E |
| `Butin_Oeil_Fen` | Œil de Fen | Rare | 100 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Oeil_Laiteux` | Œil Laiteux | Rare | 30 | 160 | 1 or 6 argent | fixé | E |
| `Butin_Oeil_Leopard` | Œil de Léopard | Rare | 40 | 170 | 1 or 7 argent | fixé | E |
| `Butin_Oeil_Murene` | Œil de Murène | Rare | 40 | 170 | 1 or 7 argent | fixé | E |
| `Butin_Oeil_Raptor` | Œil de Raptor | Épique | 770 | 1 000 | 1 diamant | fixé | E |
| `Butin_Oeuf_Archeopteryx` | Œuf d'Archéoptéryx | Épique | 745 | 1 000 | 1 diamant | fixé | E |
| `Butin_Oeuf_Reine` | Œuf de Reine | Épique | 865 | 1 000 | 1 diamant | fixé | E |
| `Butin_Oreille_Corrompue` | Oreille Corrompue | Peu commun | 560 | 60 | 6 argent | fixé | E |
| `Butin_Oreille_Gobelin` | Oreille de Gobelin | Commun | 40 | 14 | 1 argent 4 cuivres | fixé | E |
| `Butin_Os_Ancien` | Os Ancien | Commun | 15 | 12 | 1 argent 2 cuivres | fixé | E |
| `Butin_Os_Braise` | Os de Braise | Commun | 215 | 20 | 2 argent | fixé | E |
| `Butin_Os_Calcine` | Os Calciné | Commun | 210 | 20 | 2 argent | fixé | E |
| `Butin_Os_Cheval` | Os de Cheval | Commun | 80 | 18 | 1 argent 8 cuivres | fixé | E |
| `Butin_Os_Ensable` | Os Ensablé | Commun | 270 | 20 | 2 argent | fixé | E |
| `Butin_Os_Gele` | Os Gelé | Commun | 150 | 20 | 2 argent | fixé | E |
| `Butin_Os_Pourri` | Os Pourri | Peu commun | 30 | 39 | 3 argent 9 cuivres | fixé | E |
| `Butin_Os_Sale` | Os Salé | Commun | 25 | 12 | 1 argent 2 cuivres | fixé | E |
| `Butin_Patte_Araignee` | Patte d'Araignée | Peu commun | 25 | 38 | 3 argent 8 cuivres | fixé | E |
| `Butin_Peau_Bleme` | Peau Blême | Peu commun | 355 | 60 | 6 argent | fixé | E |
| `Butin_Peau_Brute` | Peau de Brute | Peu commun | 530 | 60 | 6 argent | fixé | E |
| `Butin_Peau_Chef` | Peau de Chef | Peu commun | 460 | 60 | 6 argent | fixé | E |
| `Butin_Peau_Corrompue` | Peau Corrompue | Peu commun | 600 | 60 | 6 argent | fixé | E |
| `Butin_Peau_Crapaud` | Peau de Crapaud | Commun | 60 | 16 | 1 argent 6 cuivres | fixé | E |
| `Butin_Peau_Crocodile` | Peau de Crocodile | Commun | 50 | 15 | 1 argent 5 cuivres | fixé | E |
| `Butin_Peau_Dragon_Feu` | Peau de Dragon de Feu | Peu commun | 940 | 60 | 6 argent | fixé | E |
| `Butin_Peau_Dragon_Givre` | Peau de Dragon de Givre | Peu commun | 920 | 60 | 6 argent | fixé | E |
| `Butin_Peau_Fen` | Peau de Fen | Commun | 100 | 20 | 2 argent | fixé | E |
| `Butin_Peau_Hyene` | Peau de Hyène | Commun | 30 | 13 | 1 argent 3 cuivres | fixé | E |
| `Butin_Peau_Leopard` | Peau de Léopard | Commun | 40 | 14 | 1 argent 4 cuivres | fixé | E |
| `Butin_Peau_Loup` | Peau de Loup | Commun | 30 | 13 | 1 argent 3 cuivres | fixé | E |
| `Butin_Peau_Murene` | Peau de Murène | Commun | 40 | 14 | 1 argent 4 cuivres | fixé | E |
| `Butin_Peau_Ours` | Peau d'Ours | Commun | 60 | 16 | 1 argent 6 cuivres | fixé | E |
| `Butin_Peau_Raptor` | Peau de Raptor | Peu commun | 770 | 60 | 6 argent | fixé | E |
| `Butin_Peau_Requin` | Peau de Requin | Peu commun | 80 | 54 | 5 argent 4 cuivres | fixé | E |
| `Butin_Peau_Rex` | Peau de Rex | Peu commun | 805 | 60 | 6 argent | fixé | E |
| `Butin_Peau_Serpent` | Peau de Serpent | Commun | 20 | 12 | 1 argent 2 cuivres | fixé | E |
| `Butin_Peau_Tigre` | Peau de Tigre | Commun | 70 | 17 | 1 argent 7 cuivres | fixé | E |
| `Butin_Peinture_Guerre` | Peinture de Guerre | Épique | 400 | 1 000 | 1 diamant | fixé | E |
| `Butin_Pelage_Braise` | Pelage de Braise | Commun | 250 | 20 | 2 argent | fixé | E |
| `Butin_Pelage_Loup_Trork` | Pelage de Loup Trork | Peu commun | 410 | 60 | 6 argent | fixé | E |
| `Butin_Pelage_Maudit` | Pelage Maudit | Peu commun | 480 | 60 | 6 argent | fixé | E |
| `Butin_Perle_Givre` | Perle de Givre | Rare | 50 | 180 | 1 or 8 argent | fixé | E |
| `Butin_Perle_Magma` | Perle de Magma | Rare | 25 | 150 | 1 or 5 argent | fixé | E |
| `Butin_Philtre_Sorciere` | Philtre de Sorcière | Épique | 440 | 1 000 | 1 diamant | fixé | E |
| `Butin_Pierre_Vive` | Pierre Vive | Rare | 670 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Pince_Scorpion` | Pince de Scorpion | Peu commun | 40 | 42 | 4 argent 2 cuivres | fixé | E |
| `Butin_Plaque_Firesteel` | Plaque de Firesteel | Peu commun | 725 | 60 | 6 argent | fixé | E |
| `Butin_Plaque_Royale` | Plaque Royale | Peu commun | 845 | 60 | 6 argent | fixé | E |
| `Butin_Plaque_Vide` | Plaque du Vide | Peu commun | 850 | 60 | 6 argent | fixé | E |
| `Butin_Plume_Archeopteryx` | Plume d'Archéoptéryx | Peu commun | 745 | 60 | 6 argent | fixé | E |
| `Butin_Plume_Rituelle` | Plume Rituelle | Peu commun | 440 | 60 | 6 argent | fixé | E |
| `Butin_Prisme_Instable` | Prisme Instable | Rare | 530 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Pulpe_Cactee` | Pulpe de Cactee | Peu commun | 20 | 36 | 3 argent 6 cuivres | fixé | E |
| `Butin_Pupille_Vide` | Pupille du Vide | Peu commun | 540 | 60 | 6 argent | fixé | E |
| `Butin_Queue_Lezard` | Queue de Lézard | Rare | 30 | 160 | 1 or 6 argent | fixé | E |
| `Butin_Radicelle` | Butin_Radicelle | Peu commun | 600 | 60 | 6 argent | fixé | E |
| `Butin_Recette_Interdite` | Recette Interdite | Épique | 95 | 980 | 9 or 8 argent | fixé | E |
| `Butin_Regard_Petrifie` | Regard Pétrifié | Épique | 540 | 1 000 | 1 diamant | fixé | E |
| `Butin_Residu_Electrique` | Résidu Électrique | Peu commun | 320 | 60 | 6 argent | fixé | E |
| `Butin_Rhum_Maudit` | Rhum Maudit | Rare | 25 | 150 | 1 or 5 argent | fixé | E |
| `Butin_Rire_Fige` | Rire Figé | Rare | 30 | 160 | 1 or 6 argent | fixé | E |
| `Butin_Robe_Rituelle` | Robe Rituelle | Peu commun | 465 | 60 | 6 argent | fixé | E |
| `Butin_Ronce_Vive` | Ronce Vive | Commun | 60 | 16 | 1 argent 6 cuivres | fixé | E |
| `Butin_Rouage_Ardent` | Rouage Ardent | Rare | 725 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Rouage_Labo` | Rouage de Labo | Peu commun | 60 | 48 | 4 argent 8 cuivres | fixé | E |
| `Butin_Rouage_Vide` | Rouage du Vide | Rare | 850 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Sac_Encre_Vide` | Sac d'Encre du Vide | Épique | 590 | 1 000 | 1 diamant | fixé | E |
| `Butin_Sac_Venin_Ancien` | Sac de Venin Ancien | Rare | 25 | 150 | 1 or 5 argent | fixé | E |
| `Butin_Scarabee_Funeraire` | Scarabée Funéraire | Rare | 270 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Sceau_Gardien` | Sceau du Gardien | Légendaire | 850 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Sceau_Lupin` | Sceau Lupin | Épique | 480 | 1 000 | 1 diamant | fixé | E |
| `Butin_Scie_Tordue` | Scie Tordue | Rare | 100 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Serre_Archeopteryx` | Serre d'Archéoptéryx | Rare | 745 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Serre_Pterodactyle` | Serre de Ptérodactyle | Rare | 750 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Seve_Spirituelle` | Sève Spirituelle | Rare | 600 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Soie_Araignee` | Soie d'Araignée | Commun | 25 | 12 | 1 argent 2 cuivres | fixé | E |
| `Butin_Tablier_Graisseux` | Tablier Graisseux | Peu commun | 95 | 58 | 5 argent 8 cuivres | fixé | E |
| `Butin_Talisman_Os` | Talisman d'Os | Rare | 465 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Tentacule_Ours` | Tentacule d'Ours | Légendaire | 600 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `Butin_Tentacule_Tranche` | Tentacule Tranché | Rare | 590 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Tete_Requin` | Tête de Requin | Légendaire | 80 | 22 000 | 2 zéniths 2 diamants | fixé | E |
| `Butin_Toque_Festin` | Toque du Festin | Légendaire | 95 | 23 000 | 2 zéniths 3 diamants | fixé | E |
| `Butin_Tricorne_Use` | Tricorne Usé | Peu commun | 30 | 39 | 3 argent 9 cuivres | fixé | E |
| `Butin_Trophee_Raid` | Trophée de Raid | Épique | 460 | 1 000 | 1 diamant | fixé | E |
| `Butin_Tumeur_Palpitante` | Tumeur Palpitante | Épique | 395 | 1 000 | 1 diamant | fixé | E |
| `Butin_Tumeur_Vide` | Tumeur du Vide | Épique | 560 | 1 000 | 1 diamant | fixé | E |
| `Butin_Ventouse` | Butin_Ventouse | Peu commun | 590 | 60 | 6 argent | fixé | E |
| `Butin_Verre_Desert` | Verre du Désert | Rare | 685 | 240 | 2 or 4 argent | fixé | E |
| `Butin_Verre_Lanterne` | Verre de Lanterne | Peu commun | 280 | 60 | 6 argent | fixé | E |
| `Butin_Viande_Avariee` | Viande Avariée | Commun | 30 | 13 | 1 argent 3 cuivres | fixé | E |
| `Butin_Voile_Spectral` | Voile Spectral | Peu commun | 350 | 60 | 6 argent | fixé | E |
| `Butin_Voile_Vide` | Voile du Vide | Peu commun | 580 | 60 | 6 argent | fixé | E |
| `Butin_Volant_Pilote` | Volant du Pilote | Légendaire | 100 | 24 000 | 2 zéniths 4 diamants | fixé | E |
| `DragonHeart` | Coeur de Dragon | Légendaire |  | 12 000 | 1 zénith 2 diamants | fixé | E |

<a id="fam-armes"></a>
## Armes

### Sans recette — prix fixés (218)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Artefact_Daggers_Observateur` | Mumei et Kagerō, Lames de l'Observateur | Légendaire | 580 | **à fixer** |  | à fixer | E |
| `Artefact_Mousquet_BlackBess` | Black Bess, l'Arcane Sniper | Légendaire | 560 | 400 000 | 4 astrales | fixé | E |
| `Artefact_Mousquet_BlueTiger` | Blue Tiger, le Perceur | Légendaire | 560 | 400 000 | 4 astrales | fixé | E |
| `Artefact_Mousquet_GreenColt` | Green Colt, le Rapide | Légendaire | 540 | 400 000 | 4 astrales | fixé | E |
| `Artefact_Sword_Katana_Gotetsu` | Gōtetsu, l'Acier Cramoisi | Unique | 370 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Katana_Gotetsu_Degainee` | Gōtetsu, l'Acier Cramoisi | Unique | 370 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Katana_Gotetsu_Fourreau` | Fourreau — Gōtetsu, l'Acier Cramoisi | Unique | 370 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Katana_Gotetsu_Fourreau_Vide` | Fourreau — Gōtetsu, l'Acier Cramoisi | Unique | 370 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Katana_Hien` | Hien, l'Hirondelle Écarlate | Unique | 360 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Katana_Hien_Degainee` | Hien, l'Hirondelle Écarlate | Unique | 360 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Katana_Hien_Fourreau` | Fourreau — Hien, l'Hirondelle Écarlate | Unique | 360 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Katana_Hien_Fourreau_Vide` | Fourreau — Hien, l'Hirondelle Écarlate | Unique | 360 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Katana_Shirasagi` | Shirasagi, l'Aigrette Blanche | Unique | 380 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Katana_Shirasagi_Degainee` | Shirasagi, l'Aigrette Blanche | Unique | 380 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Katana_Shirasagi_Fourreau` | Fourreau — Shirasagi, l'Aigrette Blanche | Unique | 380 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Katana_Shirasagi_Fourreau_Vide` | Fourreau — Shirasagi, l'Aigrette Blanche | Unique | 380 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Katana_Yamikiri` | Yamikiri, Tranche-Ténèbres | Unique | 380 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Katana_Yamikiri_Degainee` | Yamikiri, Tranche-Ténèbres | Unique | 380 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Katana_Yamikiri_Fourreau` | Fourreau — Yamikiri, Tranche-Ténèbres | Unique | 380 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Katana_Yamikiri_Fourreau_Vide` | Fourreau — Yamikiri, Tranche-Ténèbres | Unique | 380 | 100 000 | 1 astrale | fixé | E |
| `Artefact_Sword_Kinryu` | Kinryū-Ō, Lame du Dieu-Dragon d'Or | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Artefact_Sword_Kinryu_Degainee` | Kinryū-Ō, Lame du Dieu-Dragon d'Or | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Artefact_Sword_Kinryu_Fourreau` | Fourreau — Kinryū-Ō, Lame du Dieu-Dragon d'Or | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Artefact_Sword_Kinryu_Fourreau_Vide` | Fourreau — Kinryū-Ō, Lame du Dieu-Dragon d'Or | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Artefact_Sword_Tenryumaru` | Tenryūmaru, Lame du Premier Serment | Légendaire | 550 | 400 000 | 4 astrales | fixé | E |
| `Artefact_Sword_Tenryumaru_Degainee` | Tenryūmaru, Lame du Premier Serment | Légendaire | 550 | 400 000 | 4 astrales | fixé | E |
| `Artefact_Tenryumaru_Fourreau` | Fourreau de Tenryūmaru | Légendaire | 550 | 400 000 | 4 astrales | fixé | E |
| `Artefact_Tenryumaru_Fourreau_Vide` | Fourreau de Tenryūmaru | Légendaire | 550 | 400 000 | 4 astrales | fixé | E |
| `Eldanior_Weapon_Sword_Braise` | Lame de Braise | Rare | 85 | 7 400 | 7 diamants 4 or | fixé | E |
| `Eldanior_Weapon_Sword_Bronze` | Épée de bronze | Commun | 15 | 580 | 5 or 8 argent | fixé | E |
| `Eldanior_Weapon_Sword_Courte` | Épée courte | Commun | 10 | 550 | 5 or 5 argent | fixé | E |
| `Eldanior_Weapon_Sword_Croc_Wyverne` | Croc de Wyverne | Épique | 190 | 20 000 | 2 zéniths | fixé | E |
| `Eldanior_Weapon_Sword_Croissant` | Épée du Croissant | Rare | 80 | 7 200 | 7 diamants 2 or | fixé | E |
| `Eldanior_Weapon_Sword_Entrainement` | Épée d'entraînement | Commun | 5 | 520 | 5 or 2 argent | fixé | E |
| `Eldanior_Weapon_Sword_Fer` | Épée en fer | Commun | 20 | 600 | 6 or | fixé | E |
| `Eldanior_Weapon_Sword_Fer_Usee` | Épée de fer usée | Commun | 12 | 560 | 5 or 6 argent | fixé | E |
| `Eldanior_Weapon_Sword_Gardien` | Épée du Gardien | Rare | 90 | 7 600 | 7 diamants 6 or | fixé | E |
| `Eldanior_Weapon_Sword_Givre` | Lame du Givre | Épique | 200 | 20 000 | 2 zéniths | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Aonami` | Aonami, la Vague Bleue | Rare | 85 | 7 400 | 7 diamants 4 or | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Aonami_Degainee` | Aonami, la Vague Bleue | Rare | 85 | 7 400 | 7 diamants 4 or | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Aonami_Fourreau` | Fourreau — Aonami, la Vague Bleue | Rare | 85 | 7 400 | 7 diamants 4 or | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Aonami_Fourreau_Vide` | Fourreau — Aonami, la Vague Bleue | Rare | 85 | 7 400 | 7 diamants 4 or | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Bokken` | Bokken du Dojo Luo | Commun | 3 | 520 | 5 or 2 argent | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Bokken_Degainee` | Bokken du Dojo Luo | Commun | 3 | 520 | 5 or 2 argent | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Bokken_Fourreau` | Fourreau — Bokken du Dojo Luo | Commun | 3 | 520 | 5 or 2 argent | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Bokken_Fourreau_Vide` | Fourreau — Bokken du Dojo Luo | Commun | 3 | 520 | 5 or 2 argent | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Fer` | Katana de fer du Clan Luo | Commun | 18 | 590 | 5 or 9 argent | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Fer_Degainee` | Katana de fer du Clan Luo | Commun | 18 | 590 | 5 or 9 argent | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Fer_Fourreau` | Fourreau — Katana de fer du Clan Luo | Commun | 18 | 590 | 5 or 9 argent | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Fer_Fourreau_Vide` | Fourreau — Katana de fer du Clan Luo | Commun | 18 | 590 | 5 or 9 argent | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Tsukikage` | Tsukikage, Ombre de Lune | Épique | 190 | 20 000 | 2 zéniths | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Tsukikage_Degainee` | Tsukikage, Ombre de Lune | Épique | 190 | 20 000 | 2 zéniths | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Tsukikage_Fourreau` | Fourreau — Tsukikage, Ombre de Lune | Épique | 190 | 20 000 | 2 zéniths | fixé | E |
| `Eldanior_Weapon_Sword_Katana_Tsukikage_Fourreau_Vide` | Fourreau — Tsukikage, Ombre de Lune | Épique | 190 | 20 000 | 2 zéniths | fixé | E |
| `Eldanior_Weapon_Sword_Lame_Garde` | Lame de garde | Commun | 22 | 610 | 6 or 1 argent | fixé | E |
| `Eldanior_Weapon_Sword_Mercenaire` | Épée de mercenaire | Commun | 25 | 620 | 6 or 2 argent | fixé | E |
| `Eldanior_Weapon_Sword_Runique` | Épée runique | Rare | 100 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Weapon_Sword_Sabre_Soldat` | Sabre de soldat | Commun | 18 | 590 | 5 or 9 argent | fixé | E |
| `Eldanior_Weapon_Sword_Sabre_Vagabond` | Sabre du Vagabond | Rare | 70 | 6 800 | 6 diamants 8 or | fixé | E |
| `Eldanior_Weapon_Sword_Sabre_Vent` | Sabre du Vent | Épique | 180 | 20 000 | 2 zéniths | fixé | E |
| `Eldanior_Weapon_Sword_Tempetes` | Épée des Tempêtes | Épique | 210 | 20 000 | 2 zéniths | fixé | E |
| `Halloween_Broomstick` | Manche à balai d’Halloween | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Arrow_Clearshot` | Flèche de tir dégagé | Commun | 55 | 780 | 7 or 8 argent | fixé | V |
| `Weapon_Arrow_Deadeye` | Flèche œil de lynx | Commun | 35 | 680 | 6 or 8 argent | fixé | V |
| `Weapon_Arrow_Iron` | Flèche en fer | Peu commun | 20 | 1 800 | 1 diamant 8 or | fixé | V |
| `Weapon_Arrow_Trueshot` | Flèche de tir précis | Commun | 75 | 880 | 8 or 8 argent | fixé | V |
| `Weapon_Axe_Bone` | Hache en os de Feran | Rare | 25 | 5 000 | 5 diamants | fixé | V |
| `Weapon_Axe_Doomed` | Hache de l’Exilé maudit | Rare | 30 | 5 200 | 5 diamants 2 or | fixé | V |
| `Weapon_Axe_Iron_Rusty` | Hache en fer rouillé | Commun | 15 | 580 | 5 or 8 argent | fixé | V |
| `Weapon_Axe_Mithril` | Hache en mithril | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Weapon_Axe_Onyxium` | Hache en onyxium | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Weapon_Battleaxe_Doomed` | Hache de combat de l’Exilé maudit | Rare | 30 | 5 200 | 5 diamants 2 or | fixé | V |
| `Weapon_Battleaxe_Onyxium` | Hache de combat en onyxium | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Weapon_Battleaxe_Scarab` | Hache de combat du scarabée | Commun | 60 | 800 | 8 or | fixé | V |
| `Weapon_Battleaxe_Scythe_Void` | Faux du Néant | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Battleaxe_Steel_Rusty` | Hallebarde en acier rouillé | Commun | 15 | 580 | 5 or 8 argent | fixé | V |
| `Weapon_Battleaxe_Stone_Trork` | Hache de combat en pierre Trork | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Weapon_Battleaxe_Wood_Fence` | Clôture en bois | Commun | 60 | 800 | 8 or | fixé | V |
| `Weapon_Bomb` | Bombe | Commun | 20 | 600 | 6 or | fixé | V |
| `Weapon_Bomb_Continuous` | Bombe incendiaire continue | Peu commun | 20 | 1 800 | 1 diamant 8 or | fixé | V |
| `Weapon_Bomb_Large_Fire` | Grosse bombe incendiaire | Peu commun | 20 | 1 800 | 1 diamant 8 or | fixé | V |
| `Weapon_Bomb_Potion_Poison` | Bombe de potion de poison | Peu commun | 20 | 1 800 | 1 diamant 8 or | fixé | V |
| `Weapon_Bomb_Stun` | Bombe étourdissante | Peu commun | 20 | 1 800 | 1 diamant 8 or | fixé | V |
| `Weapon_Claws_Tribal` | Griffes tribales | Commun | 60 | 800 | 8 or | fixé | V |
| `Weapon_Club_Crude` | Gourdin rudimentaire | Commun | 5 | 520 | 5 or 2 argent | fixé | V |
| `Weapon_Club_Doomed` | Gourdin de l’Exilé maudit | Rare | 30 | 5 200 | 5 diamants 2 or | fixé | V |
| `Weapon_Club_Iron_Rusty` | Gourdin en fer rouillé | Commun | 15 | 580 | 5 or 8 argent | fixé | V |
| `Weapon_Club_Mithril` | Gourdin en mithril | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Weapon_Club_Onyxium` | Gourdin en onyxium | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Weapon_Club_Scrap` | Gourdin en ferraille | Peu commun | 15 | 1 700 | 1 diamant 7 or | fixé | V |
| `Weapon_Club_Scrap_Washboard` | Gourdin planche à laver en ferraille | Peu commun | 15 | 1 700 | 1 diamant 7 or | fixé | V |
| `Weapon_Club_Steel_Flail_Rusty` | Fléau en acier rouillé | Commun | 15 | 580 | 5 or 8 argent | fixé | V |
| `Weapon_Club_Stone_Trork` | Gourdin en pierre Trork | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Weapon_Club_Zombie_Arm` | Bras de Zombie | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Club_Zombie_Burnt_Arm` | Bras de Zombie carbonisé | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Club_Zombie_Burnt_Leg` | Jambe de Zombie carbonisé | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Club_Zombie_Frost_Arm` | Bras de Zombie gelé | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Club_Zombie_Frost_Leg` | Jambe de Zombie gelé | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Club_Zombie_Leg` | Jambe de Zombie | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Club_Zombie_Sand_Arm` | Bras de Zombie ensablé | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Club_Zombie_Sand_Leg` | Jambe de Zombie ensablé | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Crossbow_Ancient_Steel` | Arbalète de poing en acier ancien | Rare | 30 | 5 200 | 5 diamants 2 or | fixé | V |
| `Weapon_Daggers_Adamantite_Saurian` | Dagues sauriennes en adamantite | Rare | 40 | 5 600 | 5 diamants 6 or | fixé | V |
| `Weapon_Daggers_Bone` | Dagues en os de Feran | Rare | 25 | 5 000 | 5 diamants | fixé | V |
| `Weapon_Daggers_Bronze` | Dagues en bronze | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Weapon_Daggers_Bronze_Ancient` | Dagues en bronze ancien | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Weapon_Daggers_Claw_Bone` | Griffes en os | Rare | 25 | 5 000 | 5 diamants | fixé | V |
| `Weapon_Daggers_Doomed` | Dagues de l’Exilé maudit | Rare | 30 | 5 200 | 5 diamants 2 or | fixé | V |
| `Weapon_Daggers_Fang_Doomed` | Dagues à croc de l’Exilé maudit | Rare | 30 | 5 200 | 5 diamants 2 or | fixé | V |
| `Weapon_Daggers_Onyxium` | Dagues en onyxium | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Weapon_Daggers_Stone_Trork` | Dagues en pierre Trork | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Weapon_Daggers_Void_Crystal` | Dagues en cristal du Néant |  | 30 | 390 | 3 or 9 argent | fixé | V |
| `Weapon_Deployable_Turret` | Tourelle déployable | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Flamethrower_Scrap` | Lance-flammes en ferraille |  | 40 | 420 | 4 or 2 argent | fixé | V |
| `Weapon_Kunai` | Kunai | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Longsword_Adamantite_Saurian` | Sabre saurien en adamantite | Rare | 40 | 5 600 | 5 diamants 6 or | fixé | V |
| `Weapon_Longsword_Flame` | Épée longue enflammée | Légendaire | 30 | 260 000 | 2 astrales 6 zéniths | fixé | V |
| `Weapon_Longsword_Katana` | Katana | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Longsword_Mithril` | Épée longue en mithril | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Weapon_Longsword_Onyxium` | Épée longue en onyxium | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Weapon_Longsword_Praetorian` | Épée longue prétorienne | Commun | 25 | 620 | 6 or 2 argent | fixé | V |
| `Weapon_Longsword_Ruined_Giant` | Épée longue géante délabrée | Commun | 25 | 620 | 6 or 2 argent | fixé | V |
| `Weapon_Longsword_Spectral` | Épée longue spectrale | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Longsword_Stone_Trork` | Épée longue en pierre Trork | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Weapon_Longsword_Tribal` | Épée longue tribale | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Longsword_Void` | Épée longue du Néant | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Mace_Prisma` | Massue prismatique | Commun | 50 | 750 | 7 or 5 argent | fixé | V |
| `Weapon_Mace_Scrap` | Massue en ferraille | Peu commun | 15 | 1 700 | 1 diamant 7 or | fixé | V |
| `Weapon_Mace_Stone_Trork` | Massue en pierre Trork | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Weapon_Mousquet` | Weapon_Mousquet | Peu commun | 40 | 2 100 | 2 diamants 1 or | fixé | E |
| `Weapon_Shield_Doomed` | Bouclier de l’Exilé maudit | Rare | 30 | 5 200 | 5 diamants 2 or | fixé | V |
| `Weapon_Shield_Onyxium` | Bouclier en onyxium | Commun | 50 | 750 | 7 or 5 argent | fixé | V |
| `Weapon_Shield_Orbis_Incandescent` | Bouclier incandescent de chevalier ancien | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Shield_Orbis_Knight` | Bouclier de chevalier ancien | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Shield_Praetorian` | Bouclier prétorien | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Shield_Ruined_Giant` | Bouclier géant délabré | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Shield_Rusty` | Bouclier rouillé | Commun | 15 | 580 | 5 or 8 argent | fixé | V |
| `Weapon_Shield_Scrap` | Bouclier en ferraille | Peu commun | 15 | 1 700 | 1 diamant 7 or | fixé | V |
| `Weapon_Shield_Scrap_Spiked` | Bouclier en ferraille à pointes | Peu commun | 15 | 1 700 | 1 diamant 7 or | fixé | V |
| `Weapon_Shortbow_Bomb` | Arc explosif | Commun | 10 | 550 | 5 or 5 argent | fixé | V |
| `Weapon_Shortbow_Bronze` | Arc en bronze | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Weapon_Shortbow_Combat` | Arc de combat | Commun | 10 | 550 | 5 or 5 argent | fixé | V |
| `Weapon_Shortbow_Doomed` | Arc de l’Exilé maudit | Rare | 30 | 5 200 | 5 diamants 2 or | fixé | V |
| `Weapon_Shortbow_Flame` | Arc enflammé | Commun | 20 | 600 | 6 or | fixé | V |
| `Weapon_Shortbow_Frost` | Arc de glace | Commun | 20 | 600 | 6 or | fixé | V |
| `Weapon_Shortbow_Iron_Rusty` | Arc en fer rouillé | Commun | 15 | 580 | 5 or 8 argent | fixé | V |
| `Weapon_Shortbow_Onyxium` | Arc en onyxium | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Weapon_Shortbow_Pull` | Arc d’attraction | Commun | 10 | 550 | 5 or 5 argent | fixé | V |
| `Weapon_Shortbow_Ricochet` | Arc à rebond | Commun | 10 | 550 | 5 or 5 argent | fixé | V |
| `Weapon_Shortbow_Vampire` | Arc vampire | Commun | 10 | 550 | 5 or 5 argent | fixé | V |
| `Weapon_Spear_Adamantite` | Lance en adamantite | Rare | 40 | 5 600 | 5 diamants 6 or | fixé | V |
| `Weapon_Spear_Adamantite_Saurian` | Lance saurienne en adamantite | Rare | 40 | 5 600 | 5 diamants 6 or | fixé | V |
| `Weapon_Spear_Bone` | Lance en os | Rare | 25 | 5 000 | 5 diamants | fixé | V |
| `Weapon_Spear_Bronze` | Lance en bronze | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Weapon_Spear_Cobalt` | Lance en cobalt | Rare | 35 | 5 400 | 5 diamants 4 or | fixé | V |
| `Weapon_Spear_Copper` | Lance en cuivre | Commun | 10 | 550 | 5 or 5 argent | fixé | V |
| `Weapon_Spear_Double_Incandescent` | Double lame incandescente | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Spear_Fishbone` | Harpon en arête de poisson | Commun | 25 | 620 | 6 or 2 argent | fixé | V |
| `Weapon_Spear_Iron` | Lance en fer | Peu commun | 20 | 1 800 | 1 diamant 8 or | fixé | V |
| `Weapon_Spear_Leaf` | Lance en feuille | Peu commun | 15 | 1 700 | 1 diamant 7 or | fixé | V |
| `Weapon_Spear_Mithril` | Lance en mithril | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Weapon_Spear_Onyxium` | Lance en onyxium | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Weapon_Spear_Scrap` | Lance en ferraille | Peu commun | 15 | 1 700 | 1 diamant 7 or | fixé | V |
| `Weapon_Spear_Stone_Trork` | Lance en pierre Trork | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Weapon_Spear_Thorium` | Lance en thorium | Rare | 30 | 5 200 | 5 diamants 2 or | fixé | V |
| `Weapon_Spear_Tribal` | Lance tribale | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Spellbook_Demon` | Livre de sorts démoniaque | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Spellbook_Fire` | Livre de sorts de flammes | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Spellbook_Frost` | Livre de sorts de givre | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Spellbook_Grimoire_Brown` | Grimoire marron | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Spellbook_Grimoire_Purple` | Grimoire violet | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Spellbook_Rekindle_Embers` | Grimoire : raviver les braises | Rare | 40 | 5 600 | 5 diamants 6 or | fixé | V |
| `Weapon_Staff_Adamantite` | Bâton en adamantite | Rare | 40 | 5 600 | 5 diamants 6 or | fixé | V |
| `Weapon_Staff_Bo_Bamboo` | Bâton en bambou | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Staff_Bo_Wood` | Bâton en bois | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Staff_Bone` | Bâton en os | Commun | 25 | 620 | 6 or 2 argent | fixé | V |
| `Weapon_Staff_Bronze` | Bâton en bronze | Commun | 25 | 620 | 6 or 2 argent | fixé | V |
| `Weapon_Staff_Cane` | Canne | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Staff_Cobalt` | Bâton en cobalt | Rare | 35 | 5 400 | 5 diamants 4 or | fixé | V |
| `Weapon_Staff_Copper` | Bâton en cuivre | Peu commun | 10 | 1 700 | 1 diamant 7 or | fixé | V |
| `Weapon_Staff_Crystal_Fire_Trork` | Bâton Trork | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Staff_Crystal_Purple` | Bâton cristal violet | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Staff_Crystal_Red` | Bâton cristal rouge | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Staff_Doomed` | Bâton de l’Exilé maudit | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Staff_Frost` | Bâton de givre | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Staff_Iron` | Bâton en fer | Peu commun | 20 | 1 800 | 1 diamant 8 or | fixé | V |
| `Weapon_Staff_Mithril` | Bâton en mithril | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Weapon_Staff_Onion` | Bâton oignon | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Staff_Onyxium` | Bâton en onyxium | Commun | 50 | 750 | 7 or 5 argent | fixé | V |
| `Weapon_Staff_Scrap_Lightbulb` | Bâton ampoule en ferraille | Peu commun | 20 | 1 800 | 1 diamant 8 or | fixé | V |
| `Weapon_Staff_Student` | Bâton Magique | Rare | 40 | 5 600 | 5 diamants 6 or | fixé | E |
| `Weapon_Staff_Thorium` | Bâton en thorium | Rare | 30 | 5 200 | 5 diamants 2 or | fixé | V |
| `Weapon_Staff_Voidcrystal` | Bâton cristal du Néant | Peu commun | 20 | 1 800 | 1 diamant 8 or | fixé | V |
| `Weapon_Staff_Wizard` | Bâton de sorcier | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Staff_Wood` | Bâton de bois terrestre | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Staff_Wood_Kweebec` | Bâton de Kweebec | Commun | 20 | 600 | 6 or | fixé | V |
| `Weapon_Staff_Wood_Rotten` | Bâton en bois pourri | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Sword_Bone` | Épée en os | Rare | 25 | 5 000 | 5 diamants | fixé | V |
| `Weapon_Sword_Bronze` | Épée en bronze | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Weapon_Sword_Bronze_Ancient` | Épée en bronze ancien | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Weapon_Sword_Cutlass` | Sabre d’abordage | Commun | 15 | 580 | 5 or 8 argent | fixé | V |
| `Weapon_Sword_Doomed` | Épée de l’Exilé maudit | Rare | 30 | 5 200 | 5 diamants 2 or | fixé | V |
| `Weapon_Sword_Frost` | Épée de givre | Rare | 30 | 5 200 | 5 diamants 2 or | fixé | V |
| `Weapon_Sword_Nexus` | Épée du Nexus | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Sword_Onyxium` | Épée en onyxium | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Weapon_Sword_Runic` | Épée runique | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Weapon_Sword_Scrap` | Épée en ferraille | Peu commun | 15 | 1 700 | 1 diamant 7 or | fixé | V |
| `Weapon_Sword_Silversteel` | Épée en acier argenté | Commun | 45 | 720 | 7 or 2 argent | fixé | V |
| `Weapon_Sword_Steel` | Épée en acier | Peu commun | 30 | 2 000 | 2 diamants | fixé | V |
| `Weapon_Sword_Steel_Incandescent` | Épée en acier incandescent | Peu commun | 30 | 2 000 | 2 diamants | fixé | V |
| `Weapon_Sword_Steel_Rusty` | Épée en acier rouillé | Commun | 15 | 580 | 5 or 8 argent | fixé | V |
| `Weapon_Sword_Stone_Trork` | Épée en pierre Trork | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Weapon_Sword_Wood` | Épée en bois | Commun | 10 | 550 | 5 or 5 argent | fixé | V |
| `Weapon_Wand_Root` | Baguette des racines | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Wand_Stoneskin` | Baguette voile-de-pierre | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Wand_Tribal` | Baguette tribale | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Wand_Wood` | Baguette de terre en bois | Commun | 40 | 700 | 7 or | fixé | V |
| `Weapon_Wand_Wood_Rotten` | Baguette en bois pourri | Commun | 40 | 700 | 7 or | fixé | V |

### Avec recette — prix calculés (109)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O  Recette |
|---|---|---|---:|---:|---|---|---|---|
| `Artefact_Axe_Skadi` | Hache de Skaði, Croc de l'Hiver | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Battleaxe_Faux_Thanatos` | Faux de Thanatos | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Battleaxe_Labrys` | Labrys, Hache du Minotaure | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Club_Dagda` | Massue du Dagda | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Club_Heracles` | Gourdin d'Héraclès | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Daggers_Carnwennan` | Carnwennan, Dague de l'Ombre | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Eclipse_Sanglante` | Éclipse Sanglante | Unique | 500 | 7 800 000 | 7 impériales 8 astrales | calculé | E | 6× Lingot Divin + 10× Lingot d’adamantite · Forge_Divine |
| `Artefact_Gungnir` | Gungnir, la Lance d'Odin | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Longsword_Durandal` | Durandal, Lame de Roland | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Mace_Mjollnir` | Mjöllnir, Marteau de Thor | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Mace_Sharur` | Sharur, Écraseur de Milliers | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Masamune_Celeste` | Masamune Céleste | Légendaire | 500 | 16 000 000 | 16 impériales | calculé | E | 12× Lingot Divin + 20× Lingot d’adamantite · Forge_Divine |
| `Artefact_Shield_Egide` | Égide, Bouclier d'Athéna | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Shortbow_Apollon` | Arc d'Apollon, l'Arc du Soleil | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Shortbow_Artemis` | Arc d'Artémis, l'Arc d'Argent | Légendaire | 550 | 16 000 000 | 16 impériales | calculé | E | 12× Lingot Divin + 20× Lingot d’adamantite · Forge_Divine |
| `Artefact_Spear_Guandao` | Guandao du Dragon de Jade | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Staff_Ouas_Ra` | Ouas de Râ, Sceptre du Soleil | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Staff_Ruyi_Jingu_Bang` | Ruyi Jingu Bang, Bâton du Roi Singe | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Sword_Croc_Du_Neant` | Croc du Néant | Unique | 360 | 7 800 000 | 7 impériales 8 astrales | calculé | E | 6× Lingot Divin + 10× Lingot d’adamantite · Forge_Divine |
| `Artefact_Sword_Excalibur` | Excalibur, Lame du Créateur | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Sword_Gram` | Gram, Tueuse de Dragons | Légendaire | 560 | 16 000 000 | 16 impériales | calculé | E | 12× Lingot Divin + 20× Lingot d’adamantite · Forge_Divine |
| `Artefact_Sword_Katanas_Jumeaux` | Amaterasu et Tsukuyomi, Lames Jumelles | Légendaire | 580 | 16 000 000 | 16 impériales | calculé | E | 12× Lingot Divin + 20× Lingot d’adamantite · Forge_Divine |
| `Artefact_Sword_Masamune` | Artefact_Sword_Masamune | Unique | 380 | 7 800 000 | 7 impériales 8 astrales | calculé | E | 6× Lingot Divin + 10× Lingot d’adamantite · Forge_Divine |
| `Artefact_Sword_Roi_Dechu` | Lame du Roi Déchu | Unique | 340 | 7 800 000 | 7 impériales 8 astrales | calculé | E | 6× Lingot Divin + 10× Lingot d’adamantite · Forge_Divine |
| `Artefact_Sword_Ryukiri` | Ryūkiri, Tranche-Dragon des Runkandel | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Artefact_Sword_Solarys` | Solarys, Épée du Soleil | Légendaire | 540 | 16 000 000 | 16 impériales | calculé | E | 12× Lingot Divin + 20× Lingot d’adamantite · Forge_Divine |
| `Artefact_Wand_Circe` | Baguette de Circé | Divin | 800 | 31 000 000 | 31 impériales | calculé | E | 24× Lingot Divin + 40× Lingot d’adamantite · Forge_Divine |
| `Munition_Balle_Divine` | Balle Divine | Épique | 300 | 110 000 | 1 astrale 1 zénith | calculé | E | 1× Lingot Divin + 1× Lingot de fer → ×12 · Forge_Divine |
| `Munition_Balle_Fer` | Balle de fer | Commun | 20 | 32 | 3 argent 2 cuivres | calculé | E | 1× Lingot de fer → ×8 · Weapon_Bench |
| `Munition_Balle_Sniper_Divine` | Balle de sniper divine | Épique | 320 | 130 000 | 1 astrale 3 zéniths | calculé | E | 1× Lingot Divin + 2× Lingot de fer → ×10 · Forge_Divine |
| `Munition_Obus_Divin` | Obus Divin | Épique | 340 | 1 300 000 | 1 impériale 3 astrales | calculé | E | 2× Lingot Divin + 4× Lingot de fer + 4× Poudre à canon → ×2 · Forge_Divine |
| `Munition_Poudre_Canon` | Poudre à canon | Commun | 20 | 7 | 7 cuivres | calculé | E | 2× Charbon de bois + 1× Sel → ×4 · Alchemybench |
| `Weapon_Arrow_Crude` | Flèche rudimentaire | Commun | 9 | 14 | 1 argent 4 cuivres | calculé | V | 4× Bâton + 1× [N’importe quels gravats] → ×4 · Workbench |
| `Weapon_Axe_Adamantite` | Hache en adamantite | Rare | 40 | 6 900 | 6 diamants 9 or | calculé | V | 4× Lingot d’adamantite + 3× Chutes d’étoffe de braise + 4× Cuir épais · Weapon_Bench |
| `Weapon_Axe_Cobalt` | Hache en cobalt | Rare | 35 | 4 200 | 4 diamants 2 or | calculé | V | 4× Lingot de cobalt + 3× Chutes de tisse-ombre + 4× Cuir épais · Weapon_Bench |
| `Weapon_Axe_Copper` | Hache en cuivre | Commun | 10 | 470 | 4 or 7 argent | calculé | V | 4× Lingot de cuivre + 2× [N’importe quelle bûche] + 4× Fibre de plante · Weapon_Bench |
| `Weapon_Axe_Crude` | Hache rudimentaire | Commun | 5 | 99 | 9 argent 9 cuivres | calculé | V | 2× Bâton + 3× Gravats de pierre + 3× Fibre de plante + 2× Sève |
| `Weapon_Axe_Iron` | Hache en fer | Peu commun | 20 | 1 200 | 1 diamant 2 or | calculé | V | 4× Lingot de fer + 3× Chutes de lin + 4× Cuir léger · Weapon_Bench |
| `Weapon_Axe_Stone_Trork` | Hache en pierre Trork | Peu commun | 25 | 39 | 3 argent 9 cuivres | calculé | V | 2× [N’importe quelle bûche] + 6× [N’importe quelle pierre] + 1× Cuir léger · Armory |
| `Weapon_Axe_Thorium` | Hache en thorium | Rare | 30 | 2 800 | 2 diamants 8 or | calculé | V | 4× Lingot de thorium + 3× Chutes de lin + 4× Cuir moyen · Weapon_Bench |
| `Weapon_Axe_Tribal` | Hache tribale | Commun | 15 | 1 100 | 1 diamant 1 or | calculé | V | 4× Lingot de fer + 2× [N’importe quelle bûche] + 4× Cuir léger |
| `Weapon_Bangstick` | Bâton-pétard | Peu commun | 11 | 410 | 4 or 1 argent | calculé | V | 10× Ferraille + 8× Poudre explosive + 1× Soie d'araignée · GoblinLab_Workbench |
| `Weapon_Battleaxe_Adamantite` | Hache de combat en adamantite | Rare | 40 | 27 000 | 2 zéniths 7 diamants | calculé | V | 16× Lingot d’adamantite + 5× Cuir épais + 3× Chutes d’étoffe de braise · Weapon_Bench |
| `Weapon_Battleaxe_Cobalt` | Hache de combat en cobalt | Rare | 35 | 14 000 | 1 zénith 4 diamants | calculé | V | 14× Lingot de cobalt + 4× Cuir épais + 3× Chutes de tisse-ombre · Weapon_Bench |
| `Weapon_Battleaxe_Copper` | Hache de combat en cuivre | Commun | 10 | 700 | 7 or | calculé | V | 6× Lingot de cuivre + 6× [N’importe quelle bûche] + 4× Fibre de plante · Weapon_Bench |
| `Weapon_Battleaxe_Crude` | Hache de combat rudimentaire | Commun | 7 | 79 | 7 argent 9 cuivres | calculé | V | 6× [N’importe quelle pierre] + 3× [N’importe quelle bûche] + 4× Fibre de plante · Workbench |
| `Weapon_Battleaxe_Iron` | Hache de combat en fer | Peu commun | 20 | 2 700 | 2 diamants 7 or | calculé | V | 10× Lingot de fer + 4× Cuir léger + 3× Chutes de lin · Weapon_Bench |
| `Weapon_Battleaxe_Mithril` | Hache de combat en mithril | Épique | 50 | 27 000 | 2 zéniths 7 diamants | calculé | V | 10× Lingot de mithril + 4× Cuir de tempête + 1× Cœur du Néant · Weapon_Bench |
| `Weapon_Battleaxe_Thorium` | Hache de combat en thorium | Rare | 30 | 8 200 | 8 diamants 2 or | calculé | V | 12× Lingot de thorium + 4× Cuir moyen + 3× Chutes de lin · Weapon_Bench |
| `Weapon_Battleaxe_Tribal` | Hache de combat tribale | Commun | 15 | 1 700 | 1 diamant 7 or | calculé | V | 6× Lingot de fer + 3× [N’importe quelle bûche] + 4× Cuir léger |
| `Weapon_Blowgun_Tribal` | Sarbacane tribale |  | 15 | 2 800 | 2 diamants 8 or | calculé | V | 4× [N’importe quelle bûche] + 6× Fibre de plante + 4× Cuir léger + 10× Lingot de fer |
| `Weapon_Bomb_Fire` | Bombe incendiaire | Peu commun | 20 | 680 | 6 or 8 argent | calculé | V | 6× Poudre explosive + 2× Lingot de fer |
| `Weapon_Bomb_Popberry` | Bombe de baies explosives | Peu commun |  | 65 | 6 argent 5 cuivres | calculé | V | 6× Baies sauvages + 2× Poudre explosive + 4× Fibre de plante → ×2 · Alchemybench |
| `Weapon_Club_Adamantite` | Gourdin en adamantite | Rare | 40 | 6 900 | 6 diamants 9 or | calculé | V | 4× Lingot d’adamantite + 3× Chutes d’étoffe de braise + 4× Cuir épais · Armory |
| `Weapon_Club_Cobalt` | Gourdin en cobalt | Rare | 35 | 4 200 | 4 diamants 2 or | calculé | V | 4× Lingot de cobalt + 3× Chutes de tisse-ombre + 4× Cuir épais · Armory |
| `Weapon_Club_Copper` | Gourdin en cuivre | Commun | 10 | 470 | 4 or 7 argent | calculé | V | 4× Lingot de cuivre + 6× [N’importe quelle bûche] + 2× Fibre de plante · Armory |
| `Weapon_Club_Iron` | Gourdin en fer | Peu commun | 20 | 1 200 | 1 diamant 2 or | calculé | V | 4× Lingot de fer + 3× Chutes de lin + 4× Cuir léger · Armory |
| `Weapon_Club_Thorium` | Gourdin en thorium | Rare | 30 | 2 800 | 2 diamants 8 or | calculé | V | 4× Lingot de thorium + 3× Chutes de lin + 4× Cuir moyen · Armory |
| `Weapon_Club_Tribal` | Gourdin tribal | Commun | 40 | 1 200 | 1 diamant 2 or | calculé | V | 4× Lingot de fer + 6× [N’importe quelle bûche] + 4× Cuir léger |
| `Weapon_Crossbow_Iron` | Arbalète de poing en fer | Peu commun | 20 | 1 700 | 1 diamant 7 or | calculé | V | 3× Chutes de lin + 4× Cuir léger + 6× Lingot de fer · Weapon_Bench |
| `Weapon_Daggers_Adamantite` | Dagues en adamantite | Rare | 40 | 15 000 | 1 zénith 5 diamants | calculé | V | 9× Lingot d’adamantite + 3× Cuir épais + 3× Chutes d’étoffe de braise · Weapon_Bench |
| `Weapon_Daggers_Cobalt` | Dagues en cobalt | Rare | 35 | 8 200 | 8 diamants 2 or | calculé | V | 8× Lingot de cobalt + 2× Cuir épais + 3× Chutes de tisse-ombre · Weapon_Bench |
| `Weapon_Daggers_Copper` | Dagues en cuivre | Commun | 10 | 460 | 4 or 6 argent | calculé | V | 3× Fibre de plante + 3× [N’importe quelle bûche] + 4× Lingot de cuivre · Weapon_Bench |
| `Weapon_Daggers_Crude` | Dagues rudimentaires | Commun | 6 | 60 | 6 argent | calculé | V | 3× [N’importe quels gravats] + 2× Fibre de plante + 2× Bâton · Workbench |
| `Weapon_Daggers_Iron` | Dagues en fer | Peu commun | 20 | 1 100 | 1 diamant 1 or | calculé | V | 4× Lingot de fer + 2× Cuir léger + 3× Chutes de lin · Weapon_Bench |
| `Weapon_Daggers_Mithril` | Dagues en mithril | Épique | 50 | 27 000 | 2 zéniths 7 diamants | calculé | V | 10× Lingot de mithril + 3× Cuir de tempête + 1× Cœur du Néant · Weapon_Bench |
| `Weapon_Daggers_Thorium` | Dagues en thorium | Rare | 30 | 4 100 | 4 diamants 1 or | calculé | V | 6× Lingot de thorium + 2× Cuir moyen + 3× Chutes de lin · Weapon_Bench |
| `Weapon_Dart_Tribal` | Fléchette tribale | Commun | 15 | 67 | 6 argent 7 cuivres | calculé | V | 1× Lingot de fer + 1× [N’importe quelle bûche] → ×4 |
| `Weapon_Deployable_Healing_Totem` | Totem de soin | Commun | 40 | 17 000 | 1 zénith 7 diamants | calculé | V | 50× Essence de vie + 20× Lingot de thorium + 10× Potion de soin majeure · Arcanebench |
| `Weapon_Deployable_Slowness_Totem` | Totem de ralentissement | Commun | 40 | 11 000 | 1 zénith 1 diamant | calculé | V | 30× Essence de glace + 10× Lingot de cobalt + 40× Glace · Arcanebench |
| `Weapon_Longsword_Adamantite` | Épée longue en adamantite | Rare | 40 | 10 000 | 1 zénith | calculé | V | 6× Lingot d’adamantite + 3× Chutes d’étoffe de braise + 4× Cuir épais · Weapon_Bench |
| `Weapon_Longsword_Cobalt` | Épée longue en cobalt | Rare | 35 | 6 200 | 6 diamants 2 or | calculé | V | 6× Lingot de cobalt + 3× Chutes de tisse-ombre + 4× Cuir épais · Weapon_Bench |
| `Weapon_Longsword_Copper` | Épée longue en cuivre | Commun | 10 | 670 | 6 or 7 argent | calculé | V | 6× Lingot de cuivre + 2× [N’importe quelle bûche] + 4× Fibre de plante · Weapon_Bench |
| `Weapon_Longsword_Crude` | Épée longue rudimentaire | Commun | 5 | 73 | 7 argent 3 cuivres | calculé | V | 6× [N’importe quelle pierre] + 2× [N’importe quelle bûche] + 4× Fibre de plante · Weapon_Bench |
| `Weapon_Longsword_Iron` | Épée longue en fer | Peu commun | 20 | 1 700 | 1 diamant 7 or | calculé | V | 6× Lingot de fer + 3× Chutes de lin + 4× Cuir léger · Weapon_Bench |
| `Weapon_Longsword_Scarab` | Épée longue scarabée | Commun | 40 | 2 200 | 2 diamants 2 or | calculé | V | 8× Lingot de fer + 6× [N’importe quelle bûche] + 4× Cuir moyen · Armory |
| `Weapon_Longsword_Thorium` | Épée longue en thorium | Rare | 30 | 4 200 | 4 diamants 2 or | calculé | V | 6× Lingot de thorium + 3× Chutes de lin + 4× Cuir moyen · Weapon_Bench |
| `Weapon_Mace_Adamantite` | Massue en adamantite | Rare | 40 | 27 000 | 2 zéniths 7 diamants | calculé | V | 16× Lingot d’adamantite + 5× Cuir épais + 3× Chutes d’étoffe de braise · Weapon_Bench |
| `Weapon_Mace_Cobalt` | Massue en cobalt | Rare | 35 | 14 000 | 1 zénith 4 diamants | calculé | V | 14× Lingot de cobalt + 4× Cuir épais + 3× Chutes de tisse-ombre · Weapon_Bench |
| `Weapon_Mace_Copper` | Massue en cuivre | Commun | 10 | 700 | 7 or | calculé | V | 6× Lingot de cuivre + 10× [N’importe quelle bûche] + 2× Fibre de plante · Weapon_Bench |
| `Weapon_Mace_Crude` | Massue rudimentaire | Commun | 8 | 79 | 7 argent 9 cuivres | calculé | V | 6× [N’importe quelle pierre] + 3× [N’importe quelle bûche] + 4× Fibre de plante · Workbench |
| `Weapon_Mace_Iron` | Massue en fer | Peu commun | 20 | 2 700 | 2 diamants 7 or | calculé | V | 10× Lingot de fer + 4× Cuir léger + 3× Chutes de lin · Weapon_Bench |
| `Weapon_Mace_Mithril` | Massue en mithril | Épique | 50 | 27 000 | 2 zéniths 7 diamants | calculé | V | 10× Lingot de mithril + 4× Cuir de tempête + 1× Cœur du Néant · Weapon_Bench |
| `Weapon_Mace_Onyxium` | Massue en onyxium | Épique | 50 | 43 | 4 argent 3 cuivres | calculé | V | 2× [N’importe quelle bûche] + 3× [N’importe quelle pierre] + 2× Fibre de plante |
| `Weapon_Mace_Thorium` | Massue en thorium | Rare | 30 | 8 200 | 8 diamants 2 or | calculé | V | 12× Lingot de thorium + 4× Cuir moyen + 3× Chutes de lin · Weapon_Bench |
| `Weapon_Shield_Adamantite` | Bouclier en adamantite | Rare | 40 | 14 000 | 1 zénith 4 diamants | calculé | V | 8× Lingot d’adamantite + 4× Cuir épais + 3× Chutes d’étoffe de braise · Armor_Bench |
| `Weapon_Shield_Cobalt` | Bouclier en cobalt | Rare | 35 | 6 200 | 6 diamants 2 or | calculé | V | 6× Lingot de cobalt + 3× Cuir épais + 3× Chutes de tisse-ombre · Armor_Bench |
| `Weapon_Shield_Copper` | Bouclier en cuivre | Peu commun | 10 | 250 | 2 or 5 argent | calculé | V | 2× Lingot de cuivre + 1× [N’importe quelle bûche] + 3× Fibre de plante · Armor_Bench |
| `Weapon_Shield_Iron` | Bouclier en fer | Peu commun | 20 | 1 100 | 1 diamant 1 or | calculé | V | 4× Lingot de fer + 3× Cuir léger + 3× Chutes de lin · Armor_Bench |
| `Weapon_Shield_Mithril` | Bouclier en mithril | Épique | 50 | 10 000 | 1 zénith | calculé | V | 4× Lingot de mithril + 3× Cuir de tempête + 3× Chutes de soie des tempêtes · Armor_Bench |
| `Weapon_Shield_Thorium` | Bouclier en thorium | Rare | 30 | 3 500 | 3 diamants 5 or | calculé | V | 5× Lingot de thorium + 3× Cuir moyen + 3× Chutes de lin · Armor_Bench |
| `Weapon_Shield_Wood` | Bouclier en bois | Commun | 40 | 110 | 1 or 1 argent | calculé | V | 6× [N’importe quelle bûche] + 4× Fibre de plante + 1× Cuir léger |
| `Weapon_Shortbow_Adamantite` | Arc en adamantite | Rare | 40 | 19 000 | 1 zénith 9 diamants | calculé | V | 11× Lingot d’adamantite + 3× Cuir épais + 3× Chutes d’étoffe de braise · Weapon_Bench |
| `Weapon_Shortbow_Cobalt` | Arc en cobalt | Rare | 35 | 10 000 | 1 zénith | calculé | V | 10× Lingot de cobalt + 2× Cuir épais + 3× Chutes de tisse-ombre · Weapon_Bench |
| `Weapon_Shortbow_Copper` | Arc en cuivre | Commun | 10 | 510 | 5 or 1 argent | calculé | V | 4× Lingot de cuivre + 4× [N’importe quelle bûche] + 6× Fibre de plante · Weapon_Bench |
| `Weapon_Shortbow_Crude` | Arc rudimentaire | Commun | 9 | 130 | 1 or 3 argent | calculé | V | 4× Bâton + 6× Fibre de plante · Workbench |
| `Weapon_Shortbow_Iron` | Arc en fer | Peu commun | 20 | 1 600 | 1 diamant 6 or | calculé | V | 6× Lingot de fer + 2× Cuir léger + 3× Chutes de lin · Weapon_Bench |
| `Weapon_Shortbow_Mithril` | Arc en mithril | Épique | 50 | 16 000 | 1 zénith 6 diamants | calculé | V | 6× Lingot de mithril + 2× Cuir de tempête + 1× Cœur du Néant · Weapon_Bench |
| `Weapon_Shortbow_Thorium` | Arc en thorium | Rare | 30 | 5 500 | 5 diamants 5 or | calculé | V | 8× Lingot de thorium + 2× Cuir moyen + 3× Chutes de lin · Weapon_Bench |
| `Weapon_Spear_Crude` | Lance rudimentaire | Commun | 5 | 20 | 2 argent | calculé | V | 3× [N’importe quels gravats] + 3× Bâton + 1× Fibre de plante → ×3 |
| `Weapon_Staff_Crystal_Flame` | Bâton cristal de flammes | Rare | 50 | 12 000 | 1 zénith 2 diamants | calculé | V | 1× Rubis + 8× Lingot d’argent + 8× Lingot d’or + 20× Essence de feu · Arcanebench |
| `Weapon_Staff_Crystal_Ice` | Bâton cristal de glace | Rare | 50 | 5 100 | 5 diamants 1 or | calculé | V | 1× Saphir + 8× Lingot d’argent + 10× Bûche azurée + 20× Essence de glace · Arcanebench |
| `Weapon_Sword_Adamantite` | Épée en adamantite | Rare | 40 | 19 000 | 1 zénith 9 diamants | calculé | V | 11× Lingot d’adamantite + 4× Cuir épais + 3× Chutes d’étoffe de braise · Weapon_Bench |
| `Weapon_Sword_Cobalt` | Épée en cobalt | Rare | 35 | 10 000 | 1 zénith | calculé | V | 10× Lingot de cobalt + 3× Cuir épais + 3× Chutes de tisse-ombre · Weapon_Bench |
| `Weapon_Sword_Copper` | Épée en cuivre | Commun | 10 | 470 | 4 or 7 argent | calculé | V | 4× Lingot de cuivre + 4× [N’importe quelle bûche] + 3× Fibre de plante · Weapon_Bench |
| `Weapon_Sword_Crude` | Épée rudimentaire | Commun | 3 | 57 | 5 argent 7 cuivres | calculé | V | 2× [N’importe quels gravats] + 2× Fibre de plante + 2× Bâton · Fieldcraft |
| `Weapon_Sword_Iron` | Épée en fer | Peu commun | 20 | 1 700 | 1 diamant 7 or | calculé | V | 6× Lingot de fer + 3× Cuir léger + 3× Chutes de lin · Weapon_Bench |
| `Weapon_Sword_Mithril` | Épée en mithril | Épique | 50 | 16 000 | 1 zénith 6 diamants | calculé | V | 6× Lingot de mithril + 3× Cuir de tempête + 1× Cœur du Néant · Weapon_Bench |
| `Weapon_Sword_Thorium` | Épée en thorium | Rare | 30 | 5 500 | 5 diamants 5 or | calculé | V | 8× Lingot de thorium + 3× Cuir moyen + 3× Chutes de lin · Weapon_Bench |

<a id="fam-armures"></a>
## Armures

### Sans recette — prix fixés (159)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Armor_Banquier_Chest` | Haori du Banquier | Rare | 150 | 8 000 | 8 diamants | fixé | E |
| `Armor_Banquier_Hands` | Manchettes du Banquier | Rare | 150 | 8 000 | 8 diamants | fixé | E |
| `Armor_Banquier_Head` | Bonnet du Banquier | Rare | 150 | 8 000 | 8 diamants | fixé | E |
| `Armor_Banquier_Legs` | Hakama du Banquier | Rare | 150 | 8 000 | 8 diamants | fixé | E |
| `Armor_Divin_Archer_Chest` | Pourpoint du Cerf Céleste | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Divin_Archer_Hands` | Brassards du Cerf Céleste | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Divin_Archer_Head` | Capuche du Cerf Céleste | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Divin_Archer_Legs` | Bottes du Cerf Céleste | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Divin_Assassin_Chest` | Dō de la Lune Noire | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Divin_Assassin_Hands` | Kote de la Lune Noire | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Divin_Assassin_Head` | Voile d'Ombre de la Lune Noire | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Divin_Assassin_Legs` | Hakama de la Lune Noire | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Divin_Guerrier_Chest` | Cuirasse du Séraphin | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Divin_Guerrier_Hands` | Gantelets du Séraphin | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Divin_Guerrier_Head` | Heaume du Séraphin | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Divin_Guerrier_Legs` | Grèves du Séraphin | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Divin_Mage_Chest` | Robe de l'Astre | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Divin_Mage_Hands` | Bracelets de l'Astre | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Divin_Mage_Head` | Chapeau de l'Astre | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Divin_Mage_Legs` | Chausses de l'Astre | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Diving_Crude_Chest` | Combinaison de plongée rudimentaire | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Armor_Diving_Crude_Hands` | Gants de plongée rudimentaires | Peu commun | 30 | 2 000 | 2 diamants | fixé | V |
| `Armor_Diving_Crude_Head` | Masque de plongée rudimentaire | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Armor_Diving_Crude_Legs` | Palmes de plongée rudimentaires | Peu commun | 30 | 2 000 | 2 diamants | fixé | V |
| `Armor_Eldanior_Roi_Chest` | Harnois du Roi d'Eldanior | Unique | 600 | 100 000 | 1 astrale | fixé | E |
| `Armor_Eldanior_Roi_Hands` | Gantelets du Roi d'Eldanior | Unique | 600 | 100 000 | 1 astrale | fixé | E |
| `Armor_Eldanior_Roi_Head` | Heaume Couronné d'Eldanior | Unique | 600 | 100 000 | 1 astrale | fixé | E |
| `Armor_Eldanior_Roi_Legs` | Grèves du Roi d'Eldanior | Unique | 600 | 100 000 | 1 astrale | fixé | E |
| `Armor_Forgeron_Luo_Chest` | Samue du Marteau du Dragon | Légendaire | 600 | 400 000 | 4 astrales | fixé | E |
| `Armor_Forgeron_Luo_Hands` | Gants du Marteau du Dragon | Légendaire | 600 | 400 000 | 4 astrales | fixé | E |
| `Armor_Forgeron_Luo_Head` | Tenugui du Marteau du Dragon | Légendaire | 600 | 400 000 | 4 astrales | fixé | E |
| `Armor_Forgeron_Luo_Legs` | Momohiki du Marteau du Dragon | Légendaire | 600 | 400 000 | 4 astrales | fixé | E |
| `Armor_Kimono_Alchimiste_Chest` | Kariginu de l'Alchimiste Royal | Épique | 350 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Alchimiste_Hands` | Manchettes de l'Alchimiste Royal | Épique | 350 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Alchimiste_Head` | Eboshi de l'Alchimiste Royal | Épique | 350 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Alchimiste_Legs` | Sashinuki de l'Alchimiste Royal | Épique | 350 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Chest` | Kimono de Sakura | Épique | 180 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Cour_Chest` | Robe de Cour | Épique | 320 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Cour_Hands` | Manches de Cour | Épique | 320 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Cour_Head` | Coiffe de Cour | Épique | 320 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Cour_Legs` | Hakama de Cour | Épique | 320 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Divin_Chest` | Kimono Ancestral du Dieu-Dragon d'Or | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Kimono_Divin_Hands` | Tekkō du Dieu-Dragon d'Or | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Kimono_Divin_Head` | Kasa du Dieu-Dragon d'Or | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Kimono_Divin_Legs` | Hakama du Dieu-Dragon d'Or | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Kimono_Divin_Legs_Eveil` | Hakama du Dieu-Dragon d'Or | Divin | 800 | 3 000 000 | 3 impériales | fixé | E |
| `Armor_Kimono_Empereur_Chest` | Kimono de l'Empereur | Légendaire | 550 | 400 000 | 4 astrales | fixé | E |
| `Armor_Kimono_Empereur_Hands` | Manchettes de l'Empereur | Légendaire | 550 | 400 000 | 4 astrales | fixé | E |
| `Armor_Kimono_Empereur_Head` | Kanmuri de l'Empereur | Légendaire | 550 | 400 000 | 4 astrales | fixé | E |
| `Armor_Kimono_Empereur_Legs` | Hakama de l'Empereur | Légendaire | 550 | 400 000 | 4 astrales | fixé | E |
| `Armor_Kimono_Garde_Chest` | Haori de la Garde d'Honneur | Épique | 340 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Garde_Hands` | Kote de la Garde d'Honneur | Épique | 340 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Garde_Head` | Hachigane de la Garde d'Honneur | Épique | 340 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Garde_Legs` | Hakama de la Garde d'Honneur | Épique | 340 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Garde_Royale_Chest` | Kimono de la Garde Royale | Légendaire | 480 | 400 000 | 4 astrales | fixé | E |
| `Armor_Kimono_Garde_Royale_Hands` | Kote de la Garde Royale | Légendaire | 480 | 400 000 | 4 astrales | fixé | E |
| `Armor_Kimono_Garde_Royale_Head` | Jingasa de la Garde Royale | Légendaire | 480 | 400 000 | 4 astrales | fixé | E |
| `Armor_Kimono_Garde_Royale_Legs` | Hakama de la Garde Royale | Légendaire | 480 | 400 000 | 4 astrales | fixé | E |
| `Armor_Kimono_Hands` | Tekkō de Sakura | Épique | 180 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Head` | Kasa de Sakura | Épique | 180 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Legs` | Hakama de Sakura | Épique | 180 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Lettre_Chest` | Kariginu du Lettré | Épique | 330 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Lettre_Hands` | Manchettes du Lettré | Épique | 330 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Lettre_Head` | Eboshi du Lettré | Épique | 330 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Lettre_Legs` | Sashinuki du Lettré | Épique | 330 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Marchand_Chest` | Kimono du Comptoir | Rare | 120 | 8 000 | 8 diamants | fixé | E |
| `Armor_Kimono_Marchand_Hands` | Manchettes du Comptoir | Rare | 120 | 8 000 | 8 diamants | fixé | E |
| `Armor_Kimono_Marchand_Head` | Kasa du Comptoir | Rare | 120 | 8 000 | 8 diamants | fixé | E |
| `Armor_Kimono_Marchand_Legs` | Momohiki du Comptoir | Rare | 120 | 8 000 | 8 diamants | fixé | E |
| `Armor_Kimono_Negociant_Chest` | Haori du Grand Négociant | Rare | 160 | 8 000 | 8 diamants | fixé | E |
| `Armor_Kimono_Negociant_Hands` | Manchettes du Grand Négociant | Rare | 160 | 8 000 | 8 diamants | fixé | E |
| `Armor_Kimono_Negociant_Head` | Daikoku-zukin du Grand Négociant | Rare | 160 | 8 000 | 8 diamants | fixé | E |
| `Armor_Kimono_Negociant_Legs` | Hakama du Grand Négociant | Rare | 160 | 8 000 | 8 diamants | fixé | E |
| `Armor_Kimono_Noble_Chest` | Haori du Doyen (noir) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Noble_Cramoisi_Chest` | Haori du Doyen (cramoisi) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Noble_Cramoisi_Hands` | Gants du Doyen (cramoisi) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Noble_Cramoisi_Head` | Eboshi du Doyen (cramoisi) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Noble_Cramoisi_Legs` | Hakama du Doyen (cramoisi) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Noble_Hands` | Gants du Doyen (noir) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Noble_Head` | Eboshi du Doyen (noir) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Noble_Jade_Chest` | Haori du Doyen (blanc et jade) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Noble_Jade_Hands` | Gants du Doyen (blanc et jade) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Noble_Jade_Head` | Eboshi du Doyen (blanc et jade) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Noble_Jade_Legs` | Hakama du Doyen (blanc et jade) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Noble_Legs` | Hakama du Doyen (noir) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Noble_Violet_Chest` | Haori du Doyen (violet) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Noble_Violet_Hands` | Gants du Doyen (violet) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Noble_Violet_Head` | Eboshi du Doyen (violet) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Noble_Violet_Legs` | Hakama du Doyen (violet) | Unique | 450 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Ombre_Chest` | Shinobi-gi de l'Ombre | Unique | 400 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Ombre_Hands` | Tekkō de l'Ombre | Unique | 400 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Ombre_Head` | Zukin de l'Ombre | Unique | 400 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Ombre_Legs` | Hakama de l'Ombre | Unique | 400 | 100 000 | 1 astrale | fixé | E |
| `Armor_Kimono_Pauvre_Chest` | Kimono Rapiécé | Commun | 15 | 580 | 5 or 8 argent | fixé | E |
| `Armor_Kimono_Pauvre_Hands` | Bandes de Chiffon | Commun | 15 | 580 | 5 or 8 argent | fixé | E |
| `Armor_Kimono_Pauvre_Head` | Kasa du Vagabond | Commun | 15 | 580 | 5 or 8 argent | fixé | E |
| `Armor_Kimono_Pauvre_Legs` | Pantalon Déchiré | Commun | 15 | 580 | 5 or 8 argent | fixé | E |
| `Armor_Kimono_Pelerin_Chest` | Kimono du Pèlerin | Épique | 300 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Pelerin_Hands` | Tekkō du Pèlerin | Épique | 300 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Pelerin_Head` | Kasa du Pèlerin | Épique | 300 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Pelerin_Legs` | Hakama du Pèlerin | Épique | 300 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Recrue_Chest` | Keikogi de Recrue | Rare | 60 | 6 400 | 6 diamants 4 or | fixé | E |
| `Armor_Kimono_Recrue_Hands` | Bandes de Recrue | Rare | 60 | 6 400 | 6 diamants 4 or | fixé | E |
| `Armor_Kimono_Recrue_Head` | Hachimaki de Recrue | Rare | 60 | 6 400 | 6 diamants 4 or | fixé | E |
| `Armor_Kimono_Recrue_Legs` | Hakama de Recrue | Rare | 60 | 6 400 | 6 diamants 4 or | fixé | E |
| `Armor_Kimono_Rizieres_Chest` | Noragi des Rizières | Commun | 10 | 550 | 5 or 5 argent | fixé | E |
| `Armor_Kimono_Rizieres_Hands` | Tekkō des Rizières | Commun | 10 | 550 | 5 or 5 argent | fixé | E |
| `Armor_Kimono_Rizieres_Head` | Sugegasa des Rizières | Commun | 10 | 550 | 5 or 5 argent | fixé | E |
| `Armor_Kimono_Rizieres_Legs` | Momohiki des Rizières | Commun | 10 | 550 | 5 or 5 argent | fixé | E |
| `Armor_Kimono_Ronin_Chest` | Haori du Rōnin | Rare | 100 | 8 000 | 8 diamants | fixé | E |
| `Armor_Kimono_Ronin_Hands` | Bandes du Rōnin | Rare | 100 | 8 000 | 8 diamants | fixé | E |
| `Armor_Kimono_Ronin_Head` | Hachimaki du Rōnin | Rare | 100 | 8 000 | 8 diamants | fixé | E |
| `Armor_Kimono_Ronin_Legs` | Hakama du Rōnin | Rare | 100 | 8 000 | 8 diamants | fixé | E |
| `Armor_Kimono_Sama_Chest` | Dōgi de Sama | Épique | 340 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Sama_Hands` | Poings de Sama | Épique | 340 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Sama_Head` | Hachimaki de Sama | Épique | 340 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Sama_Legs` | Tattsuke de Sama | Épique | 340 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Seigneur_Chest` | Kamishimo du Seigneur | Légendaire | 500 | 400 000 | 4 astrales | fixé | E |
| `Armor_Kimono_Seigneur_Hands` | Gants du Seigneur | Légendaire | 500 | 400 000 | 4 astrales | fixé | E |
| `Armor_Kimono_Seigneur_Head` | Eboshi du Seigneur | Légendaire | 500 | 400 000 | 4 astrales | fixé | E |
| `Armor_Kimono_Seigneur_Legs` | Nagabakama du Seigneur | Légendaire | 500 | 400 000 | 4 astrales | fixé | E |
| `Armor_Kimono_Setsuka_Chest` | Kimono de Setsuka | Épique | 180 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Setsuka_Hands` | Tekkō de Setsuka | Épique | 180 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Setsuka_Head` | Kasa de Setsuka | Épique | 180 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kimono_Setsuka_Legs` | Hakama de Setsuka | Épique | 180 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Kweebec_Chest` | Cuirasse de Kweebec | Peu commun | 20 | 1 800 | 1 diamant 8 or | fixé | V |
| `Armor_Kweebec_Head` | Casque de Kweebec | Peu commun | 20 | 1 800 | 1 diamant 8 or | fixé | V |
| `Armor_Onyxium_Chest` | Cuirasse en onyxium | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Armor_Onyxium_Hands` | Gantelets en onyxium | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Armor_Onyxium_Head` | Casque en onyxium | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Armor_Onyxium_Legs` | Jambières en onyxium | Épique | 50 | 15 000 | 1 zénith 5 diamants | fixé | V |
| `Armor_Prisma_Chest` | Cuirasse prismatique | Commun | 75 | 880 | 8 or 8 argent | fixé | V |
| `Armor_Prisma_Hands` | Gantelets prismatiques | Commun | 75 | 880 | 8 or 8 argent | fixé | V |
| `Armor_Prisma_Head` | Casque prismatique | Commun | 75 | 880 | 8 or 8 argent | fixé | V |
| `Armor_Prisma_Legs` | Jambières prismatiques | Commun | 75 | 880 | 8 or 8 argent | fixé | V |
| `Armor_Runkandel_Capitaine_Chest` | Cuirasse de Capitaine | Épique | 250 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Runkandel_Capitaine_Hands` | Gantelets de Capitaine | Épique | 250 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Runkandel_Capitaine_Head` | Bourguignotte de Capitaine | Épique | 250 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Runkandel_Capitaine_Legs` | Jambières de Capitaine | Épique | 250 | 20 000 | 2 zéniths | fixé | E |
| `Armor_Runkandel_Patriarche_Chest` | Harnois de Vorn | Légendaire | 550 | 400 000 | 4 astrales | fixé | E |
| `Armor_Runkandel_Patriarche_Hands` | Gantelets de Vorn | Légendaire | 550 | 400 000 | 4 astrales | fixé | E |
| `Armor_Runkandel_Patriarche_Head` | Armet Couronné de Vorn | Légendaire | 550 | 400 000 | 4 astrales | fixé | E |
| `Armor_Runkandel_Patriarche_Legs` | Bottes de Vorn | Légendaire | 550 | 400 000 | 4 astrales | fixé | E |
| `Armor_Runkandel_Recrue_Chest` | Brigandine de Recrue | Rare | 60 | 6 400 | 6 diamants 4 or | fixé | E |
| `Armor_Runkandel_Recrue_Hands` | Gants de Recrue | Rare | 60 | 6 400 | 6 diamants 4 or | fixé | E |
| `Armor_Runkandel_Recrue_Head` | Bassinet de Recrue | Rare | 60 | 6 400 | 6 diamants 4 or | fixé | E |
| `Armor_Runkandel_Recrue_Legs` | Chausses de Recrue | Rare | 60 | 6 400 | 6 diamants 4 or | fixé | E |
| `Armor_Steel_Chest` | Cuirasse en acier | Peu commun | 20 | 1 800 | 1 diamant 8 or | fixé | V |
| `Armor_Steel_Hands` | Gantelets en acier | Peu commun | 20 | 1 800 | 1 diamant 8 or | fixé | V |
| `Armor_Steel_Head` | Casque en acier | Peu commun | 20 | 1 800 | 1 diamant 8 or | fixé | V |
| `Armor_Steel_Legs` | Jambières en acier | Peu commun | 20 | 1 800 | 1 diamant 8 or | fixé | V |
| `Armor_Trork_Chest` | Cuirasse de guerrier Trork | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Armor_Trork_Hands` | Gantelets de guerrier Trork | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Armor_Trork_Head` | Casque de guerrier Trork | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Armor_Trork_Legs` | Pantalon de guerrier Trork | Peu commun | 25 | 1 900 | 1 diamant 9 or | fixé | V |
| `Armor_Wool_Chest` | Tunique en laine | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Armor_Wool_Hands` | Bracelets en laine | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Armor_Wool_Head` | Capuche en laine | Commun | 30 | 650 | 6 or 5 argent | fixé | V |
| `Armor_Wool_Legs` | Bottes en laine | Commun | 30 | 650 | 6 or 5 argent | fixé | V |

### Avec recette — prix calculés (79)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O  Recette |
|---|---|---|---:|---:|---|---|---|---|
| `Armor_Adamantite_Chest` | Cuirasse en adamantite | Rare | 40 | 48 000 | 4 zéniths 8 diamants | calculé | V | 28× Lingot d’adamantite + 8× Cuir épais + 7× Chutes d’étoffe de braise · Armor_Bench |
| `Armor_Adamantite_Hands` | Gantelets en adamantite | Rare | 40 | 20 000 | 2 zéniths | calculé | V | 12× Lingot d’adamantite + 4× Cuir épais + 3× Chutes d’étoffe de braise · Armor_Bench |
| `Armor_Adamantite_Head` | Casque en adamantite | Rare | 40 | 25 000 | 2 zéniths 5 diamants | calculé | V | 15× Lingot d’adamantite + 5× Cuir épais + 4× Chutes d’étoffe de braise · Armor_Bench |
| `Armor_Adamantite_Legs` | Jambières en adamantite | Rare | 40 | 37 000 | 3 zéniths 7 diamants | calculé | V | 22× Lingot d’adamantite + 7× Cuir épais + 6× Chutes d’étoffe de braise · Armor_Bench |
| `Armor_Bronze_Chest` | Cuirasse en bronze | Peu commun | 25 | 2 400 | 2 diamants 4 or | calculé | V | 6× Lingot de bronze + 2× [N’importe quelle bûche] + 2× Cuir léger · TODO |
| `Armor_Bronze_Hands` | Gantelets en bronze | Peu commun | 25 | 820 | 8 or 2 argent | calculé | V | 2× Lingot de bronze + 1× [N’importe quelle bûche] + 2× Cuir léger · TODO |
| `Armor_Bronze_Head` | Casque en bronze | Peu commun | 25 | 1 200 | 1 diamant 2 or | calculé | V | 3× Lingot de bronze + 2× [N’importe quelle bûche] + 2× Cuir léger · TODO |
| `Armor_Bronze_Legs` | Jambières en bronze | Peu commun | 25 | 1 600 | 1 diamant 6 or | calculé | V | 4× Lingot de bronze + 2× [N’importe quelle bûche] + 3× Cuir léger · TODO |
| `Armor_Bronze_Ornate_Chest` | Cuirasse ornée en bronze | Rare | 28 | 2 400 | 2 diamants 4 or | calculé | V | 6× Lingot de bronze + 2× [N’importe quelle bûche] + 2× Cuir moyen · TODO |
| `Armor_Bronze_Ornate_Hands` | Gantelets ornés en bronze | Rare | 28 | 820 | 8 or 2 argent | calculé | V | 2× Lingot de bronze + 1× [N’importe quelle bûche] + 2× Cuir moyen · TODO |
| `Armor_Bronze_Ornate_Head` | Casque orné en bronze | Rare | 28 | 1 200 | 1 diamant 2 or | calculé | V | 3× Lingot de bronze + 2× [N’importe quelle bûche] + 2× Cuir moyen · TODO |
| `Armor_Cloth_Cindercloth_Chest` | Tunique en étoffe de braise | Rare | 45 | 3 400 | 3 diamants 4 or | calculé | V | 6× Rouleau d’étoffe de braise + 2× [N’importe quelle bûche] + 2× Lingot d’adamantite · TODO |
| `Armor_Cloth_Cindercloth_Hands` | Gants en étoffe de braise | Rare | 45 | 3 400 | 3 diamants 4 or | calculé | V | 2× Rouleau d’étoffe de braise + 1× [N’importe quelle bûche] + 2× Lingot d’adamantite · TODO |
| `Armor_Cloth_Cindercloth_Head` | Capuche en étoffe de braise | Rare | 45 | 3 400 | 3 diamants 4 or | calculé | V | 3× Rouleau d’étoffe de braise + 2× [N’importe quelle bûche] + 2× Lingot d’adamantite · TODO |
| `Armor_Cloth_Cindercloth_Legs` | Pantalon en étoffe de braise | Rare | 45 | 5 100 | 5 diamants 1 or | calculé | V | 4× Rouleau d’étoffe de braise + 2× [N’importe quelle bûche] + 3× Lingot d’adamantite · TODO |
| `Armor_Cloth_Cotton_Chest` | Tunique en coton | Commun | 25 | 1 400 | 1 diamant 4 or | calculé | V | 6× Rouleau de coton + 2× [N’importe quelle bûche] + 2× Lingot de thorium · TODO |
| `Armor_Cloth_Cotton_Hands` | Gants en coton | Commun | 25 | 1 400 | 1 diamant 4 or | calculé | V | 2× Rouleau de coton + 1× [N’importe quelle bûche] + 2× Lingot de thorium · TODO |
| `Armor_Cloth_Cotton_Head` | Capuche en coton | Commun | 25 | 1 400 | 1 diamant 4 or | calculé | V | 3× Rouleau de coton + 2× [N’importe quelle bûche] + 2× Lingot de thorium · TODO |
| `Armor_Cloth_Cotton_Legs` | Pantalon en coton | Commun | 25 | 2 100 | 2 diamants 1 or | calculé | V | 4× Rouleau de coton + 2× [N’importe quelle bûche] + 3× Lingot de thorium · TODO |
| `Armor_Cloth_Linen_Chest` | Tunique en lin | Commun | 15 | 560 | 5 or 6 argent | calculé | V | 6× Rouleau de lin + 2× [N’importe quelle bûche] + 2× Lingot de fer · TODO |
| `Armor_Cloth_Linen_Hands` | Gants en lin | Commun | 15 | 540 | 5 or 4 argent | calculé | V | 2× Rouleau de lin + 1× [N’importe quelle bûche] + 2× Lingot de fer · TODO |
| `Armor_Cloth_Linen_Head` | Capuche en lin | Commun | 15 | 550 | 5 or 5 argent | calculé | V | 3× Rouleau de lin + 2× [N’importe quelle bûche] + 2× Lingot de fer · TODO |
| `Armor_Cloth_Linen_Legs` | Pantalon en lin | Commun | 15 | 810 | 8 or 1 argent | calculé | V | 4× Rouleau de lin + 2× [N’importe quelle bûche] + 3× Lingot de fer · TODO |
| `Armor_Cloth_Silk_Chest` | Tunique en soie | Commun | 35 | 2 100 | 2 diamants 1 or | calculé | V | 6× Rouleau de soie + 2× [N’importe quelle bûche] + 2× Lingot de cobalt · TODO |
| `Armor_Cloth_Silk_Hands` | Bracelets en soie | Commun | 35 | 2 000 | 2 diamants | calculé | V | 2× Rouleau de soie + 1× [N’importe quelle bûche] + 2× Lingot de cobalt · TODO |
| `Armor_Cloth_Silk_Head` | Diadème en soie | Commun | 35 | 2 100 | 2 diamants 1 or | calculé | V | 3× Rouleau de soie + 2× [N’importe quelle bûche] + 2× Lingot de cobalt · TODO |
| `Armor_Cloth_Silk_Legs` | Pantalon en soie | Commun | 35 | 3 100 | 3 diamants 1 or | calculé | V | 4× Rouleau de soie + 2× [N’importe quelle bûche] + 3× Lingot de cobalt · TODO |
| `Armor_Cloth_Wool_Chest` | Cuirasse en laine | Commun | 10 | 250 | 2 or 5 argent | calculé | V | 6× Rouleau de laine + 2× [N’importe quelle bûche] + 2× Lingot de cuivre · TODO |
| `Armor_Cloth_Wool_Hands` | Gantelets en laine | Commun | 10 | 220 | 2 or 2 argent | calculé | V | 2× Rouleau de laine + 1× [N’importe quelle bûche] + 2× Lingot de cuivre · TODO |
| `Armor_Cloth_Wool_Head` | Casque en laine | Commun | 10 | 230 | 2 or 3 argent | calculé | V | 3× Rouleau de laine + 2× [N’importe quelle bûche] + 2× Lingot de cuivre · TODO |
| `Armor_Cloth_Wool_Legs` | Pantalon en laine | Commun | 10 | 340 | 3 or 4 argent | calculé | V | 4× Rouleau de laine + 2× [N’importe quelle bûche] + 3× Lingot de cuivre · TODO |
| `Armor_Cobalt_Chest` | Cuirasse en cobalt | Rare | 35 | 25 000 | 2 zéniths 5 diamants | calculé | V | 24× Lingot de cobalt + 7× Cuir épais + 7× Chutes de tisse-ombre · Armor_Bench |
| `Armor_Cobalt_Hands` | Gantelets en cobalt | Rare | 35 | 8 200 | 8 diamants 2 or | calculé | V | 8× Lingot de cobalt + 3× Cuir épais + 3× Chutes de tisse-ombre · Armor_Bench |
| `Armor_Cobalt_Head` | Casque en cobalt | Rare | 35 | 12 000 | 1 zénith 2 diamants | calculé | V | 12× Lingot de cobalt + 4× Cuir épais + 4× Chutes de tisse-ombre · Armor_Bench |
| `Armor_Cobalt_Legs` | Jambières en cobalt | Rare | 35 | 18 000 | 1 zénith 8 diamants | calculé | V | 18× Lingot de cobalt + 6× Cuir épais + 6× Chutes de tisse-ombre · Armor_Bench |
| `Armor_Copper_Chest` | Cuirasse en cuivre | Commun | 10 | 1 200 | 1 diamant 2 or | calculé | V | 11× Lingot de cuivre + 4× Fibre de plante · Armor_Bench |
| `Armor_Copper_Hands` | Gantelets en cuivre | Commun | 10 | 520 | 5 or 2 argent | calculé | V | 5× Lingot de cuivre + 1× Fibre de plante · Armor_Bench |
| `Armor_Copper_Head` | Casque en cuivre | Commun | 10 | 630 | 6 or 3 argent | calculé | V | 6× Lingot de cuivre + 2× Fibre de plante · Armor_Bench |
| `Armor_Copper_Legs` | Jambières en cuivre | Commun | 10 | 950 | 9 or 5 argent | calculé | V | 9× Lingot de cuivre + 3× Fibre de plante · Armor_Bench |
| `Armor_Iron_Chest` | Cuirasse en fer | Peu commun | 20 | 4 400 | 4 diamants 4 or | calculé | V | 16× Lingot de fer + 7× Cuir léger + 6× Chutes de lin · Armor_Bench |
| `Armor_Iron_Hands` | Gantelets en fer | Peu commun | 20 | 1 900 | 1 diamant 9 or | calculé | V | 7× Lingot de fer + 3× Cuir léger + 3× Chutes de lin · Armor_Bench |
| `Armor_Iron_Head` | Casque en fer | Peu commun | 20 | 2 500 | 2 diamants 5 or | calculé | V | 9× Lingot de fer + 4× Cuir léger + 3× Chutes de lin · Armor_Bench |
| `Armor_Iron_Legs` | Jambières en fer | Peu commun | 20 | 3 500 | 3 diamants 5 or | calculé | V | 13× Lingot de fer + 6× Cuir léger + 4× Chutes de lin · Armor_Bench |
| `Armor_Leather_Heavy_Chest` | Cuirasse en cuir épais | Commun | 35 | 130 | 1 or 3 argent | calculé | V | 6× Cuir épais + 2× [N’importe quelle bûche] + 2× Rouleau de laine · TODO |
| `Armor_Leather_Heavy_Hands` | Gantelets en cuir épais | Commun | 35 | 53 | 5 argent 3 cuivres | calculé | V | 2× Cuir épais + 1× [N’importe quelle bûche] + 2× Rouleau de laine · TODO |
| `Armor_Leather_Heavy_Head` | Casque en cuir épais | Commun | 35 | 78 | 7 argent 8 cuivres | calculé | V | 3× Cuir épais + 2× [N’importe quelle bûche] + 2× Rouleau de laine · TODO |
| `Armor_Leather_Heavy_Legs` | Pantalon en cuir épais | Commun | 35 | 100 | 1 or | calculé | V | 4× Cuir épais + 2× [N’importe quelle bûche] + 3× Rouleau de laine · TODO |
| `Armor_Leather_Light_Chest` | Cuirasse en cuir léger | Commun | 15 | 130 | 1 or 3 argent | calculé | V | 6× Cuir léger + 2× [N’importe quelle bûche] + 2× Rouleau de laine · TODO |
| `Armor_Leather_Light_Hands` | Gantelets en cuir léger | Commun | 15 | 53 | 5 argent 3 cuivres | calculé | V | 2× Cuir léger + 1× [N’importe quelle bûche] + 2× Rouleau de laine · TODO |
| `Armor_Leather_Light_Head` | Casque en cuir léger | Commun | 15 | 78 | 7 argent 8 cuivres | calculé | V | 3× Cuir léger + 2× [N’importe quelle bûche] + 2× Rouleau de laine · TODO |
| `Armor_Leather_Light_Legs` | Pantalon en cuir léger | Commun | 15 | 100 | 1 or | calculé | V | 4× Cuir léger + 2× [N’importe quelle bûche] + 3× Rouleau de laine · TODO |
| `Armor_Leather_Medium_Chest` | [TMP] Cuirasse en cuir moyen | Commun | 25 | 130 | 1 or 3 argent | calculé | V | 6× Cuir moyen + 2× [N’importe quelle bûche] + 2× Rouleau de laine · TODO |
| `Armor_Leather_Medium_Hands` | [TMP] Gants en cuir moyen | Commun | 25 | 53 | 5 argent 3 cuivres | calculé | V | 2× Cuir moyen + 1× [N’importe quelle bûche] + 2× Rouleau de laine · TODO |
| `Armor_Leather_Medium_Head` | [TMP] Casque en cuir moyen | Commun | 25 | 78 | 7 argent 8 cuivres | calculé | V | 3× Cuir moyen + 2× [N’importe quelle bûche] + 2× Rouleau de laine · TODO |
| `Armor_Leather_Medium_Legs` | [TMP] Pantalon en cuir moyen | Commun | 25 | 100 | 1 or | calculé | V | 4× Cuir moyen + 2× [N’importe quelle bûche] + 3× Rouleau de laine · TODO |
| `Armor_Leather_Raven_Chest` | Cuirasse en cuir de corbeau | Peu commun | 32 | 160 | 1 or 6 argent | calculé | V | 6× Cuir épais + 3× Plumes sombres + 2× Rouleau de coton · TODO |
| `Armor_Leather_Raven_Hands` | Gantelets en cuir de corbeau | Peu commun | 32 | 55 | 5 argent 5 cuivres | calculé | V | 2× Cuir épais + 1× Plumes sombres + 1× Rouleau de coton · TODO |
| `Armor_Leather_Raven_Head` | Capuche en cuir de corbeau | Peu commun | 32 | 91 | 9 argent 1 cuivre | calculé | V | 3× Cuir épais + 2× Plumes sombres + 2× Rouleau de coton · TODO |
| `Armor_Leather_Raven_Legs` | Pantalon en cuir de corbeau | Peu commun | 32 | 120 | 1 or 2 argent | calculé | V | 4× Cuir épais + 3× Plumes sombres + 2× Rouleau de coton · TODO |
| `Armor_Leather_Soft_Chest` | Cuirasse en cuir souple | Commun | 10 | 130 | 1 or 3 argent | calculé | V | 6× Cuir léger + 2× [N’importe quelle bûche] + 2× Rouleau de laine · TODO |
| `Armor_Leather_Soft_Hands` | Gantelets en cuir souple | Commun | 10 | 53 | 5 argent 3 cuivres | calculé | V | 2× Cuir léger + 1× [N’importe quelle bûche] + 2× Rouleau de laine · TODO |
| `Armor_Leather_Soft_Head` | Capuche en cuir souple | Commun | 10 | 78 | 7 argent 8 cuivres | calculé | V | 3× Cuir léger + 2× [N’importe quelle bûche] + 2× Rouleau de laine · TODO |
| `Armor_Leather_Soft_Legs` | Pantalon en cuir souple | Commun | 10 | 100 | 1 or | calculé | V | 4× Cuir léger + 2× [N’importe quelle bûche] + 3× Rouleau de laine · TODO |
| `Armor_Mithril_Chest` | Cuirasse en mithril | Épique | 50 | 66 000 | 6 zéniths 6 diamants | calculé | V | 24× Lingot de mithril + 8× Cuir de tempête + 80× Essence du Néant · Armor_Bench |
| `Armor_Mithril_Hands` | Gantelets en mithril | Épique | 50 | 22 000 | 2 zéniths 2 diamants | calculé | V | 8× Lingot de mithril + 2× Cuir de tempête + 20× Essence du Néant · Armor_Bench |
| `Armor_Mithril_Head` | Casque en mithril | Épique | 50 | 33 000 | 3 zéniths 3 diamants | calculé | V | 12× Lingot de mithril + 4× Cuir de tempête + 40× Essence du Néant · Armor_Bench |
| `Armor_Mithril_Legs` | Jambières en mithril | Épique | 50 | 49 000 | 4 zéniths 9 diamants | calculé | V | 18× Lingot de mithril + 6× Cuir de tempête + 60× Essence du Néant · Armor_Bench |
| `Armor_Steel_Ancient_Chest` | Cuirasse en acier ancien | Rare | 30 | 2 400 | 2 diamants 4 or | calculé | V | 6× Lingot de bronze + 2× [N’importe quelle bûche] + 2× Cuir léger · TODO |
| `Armor_Steel_Ancient_Hands` | Gantelets en acier ancien | Rare | 30 | 820 | 8 or 2 argent | calculé | V | 2× Lingot de bronze + 1× [N’importe quelle bûche] + 2× Cuir léger · TODO |
| `Armor_Steel_Ancient_Head` | Casque en acier ancien | Rare | 30 | 1 200 | 1 diamant 2 or | calculé | V | 3× Lingot de bronze + 2× [N’importe quelle bûche] + 2× Cuir léger · TODO |
| `Armor_Steel_Ancient_Legs` | Jambières en acier ancien | Rare | 30 | 1 600 | 1 diamant 6 or | calculé | V | 4× Lingot de bronze + 2× [N’importe quelle bûche] + 3× Cuir léger · TODO |
| `Armor_Thorium_Chest` | Cuirasse en thorium | Rare | 30 | 14 000 | 1 zénith 4 diamants | calculé | V | 20× Lingot de thorium + 7× Cuir moyen + 7× Chutes de lin + 4× Glande à venin · Armor_Bench |
| `Armor_Thorium_Hands` | Gantelets en thorium | Rare | 30 | 6 200 | 6 diamants 2 or | calculé | V | 9× Lingot de thorium + 3× Cuir moyen + 3× Chutes de lin + 1× Glande à venin · Armor_Bench |
| `Armor_Thorium_Head` | Casque en thorium | Rare | 30 | 7 700 | 7 diamants 7 or | calculé | V | 11× Lingot de thorium + 4× Cuir moyen + 4× Chutes de lin + 2× Glande à venin · Armor_Bench |
| `Armor_Thorium_Legs` | Jambières en thorium | Rare | 30 | 10 000 | 1 zénith | calculé | V | 15× Lingot de thorium + 6× Cuir moyen + 6× Chutes de lin + 3× Glande à venin · Armor_Bench |
| `Armor_Wood_Chest` | Cuirasse en bois | Commun | 20 | 140 | 1 or 4 argent | calculé | V | 3× Fibre de plante + 15× [N’importe quelle bûche] |
| `Armor_Wood_Hands` | Gantelets en bois | Commun | 5 | 39 | 3 argent 9 cuivres | calculé | V | 2× [N’importe quelle bûche] + 2× Fibre de plante |
| `Armor_Wood_Head` | Casque en bois | Commun | 5 | 58 | 5 argent 8 cuivres | calculé | V | 3× [N’importe quelle bûche] + 3× Fibre de plante |
| `Armor_Wood_Legs` | Jambières en bois | Commun | 5 | 84 | 8 argent 4 cuivres | calculé | V | 3× [N’importe quelle bûche] + 5× Fibre de plante |

<a id="fam-outils-utilitaires"></a>
## Outils & utilitaires

### Sans recette — prix fixés (22)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Survival_Trap_Spike_Wood_Large` | Grand piège à pointes en bois |  |  | 300 | 3 or | fixé | V |
| `Tool_Bark_Scraper` | Gratte-écorce | Commun | 4 | 520 | 5 or 2 argent | fixé | V |
| `Tool_Hatchet_Onyxium` | Hachette en onyxium | Épique | 4 | 10 000 | 1 zénith | fixé | V |
| `Tool_Hatchet_Wood` | Hachette en bois | Commun | 4 | 520 | 5 or 2 argent | fixé | V |
| `Tool_Map` | Carte |  |  | 300 | 3 or | fixé | V |
| `Tool_Pickaxe_Onyxium` | Pioche en onyxium | Épique | 5 | 10 000 | 1 zénith | fixé | V |
| `Tool_Pickaxe_Scrap` | Pioche en ferraille | Peu commun | 5 | 1 600 | 1 diamant 6 or | fixé | V |
| `Tool_Pickaxe_Wood` | Pioche en bois | Commun | 5 | 520 | 5 or 2 argent | fixé | V |
| `Tool_Repair_Kit_Crude` | Kit de réparation rudimentaire | Commun |  | 500 | 5 or | fixé | V |
| `Tool_Repair_Kit_Rare` | Kit de réparation rare |  |  | 300 | 3 or | fixé | V |
| `Tool_Shovel_Cobalt` | Pelle en cobalt | Rare | 3 | 4 100 | 4 diamants 1 or | fixé | V |
| `Tool_Shovel_Copper` | Pelle en cuivre | Commun | 3 | 520 | 5 or 2 argent | fixé | V |
| `Tool_Shovel_Crude` | Pelle rudimentaire | Commun | 3 | 520 | 5 or 2 argent | fixé | V |
| `Tool_Shovel_Thorium` | Pelle en thorium | Rare | 3 | 4 100 | 4 diamants 1 or | fixé | V |
| `Tool_Sickle_Steel_Rusty` | Faucille en acier rouillé | Commun | 5 | 520 | 5 or 2 argent | fixé | V |
| `Trap_Ancient_Platform` | Piège de plateforme ancienne fissurée |  |  | 300 | 3 or | fixé | V |
| `Trap_Ice` | Piège de glace fissuré |  |  | 300 | 3 or | fixé | V |
| `Trap_Slate` | Piège en ardoise fissurée |  | 10 | 330 | 3 or 3 argent | fixé | V |
| `Upgrade_Backpack_1` | Déverrouiller le sac | Peu commun | 20 | 2 200 | 2 diamants 2 or | fixé | E |
| `Upgrade_Backpack_2` | Amélioration de sac I | Peu commun | 30 | 10 000 | 1 zénith | fixé | E |
| `Upgrade_Backpack_3` | Amélioration de sac II | Peu commun | 18 | 50 000 | 5 zéniths | fixé | E |
| `Upgrade_Backpack_4` | Amélioration de sac III | Rare | 60 | 6 400 | 6 diamants 4 or | fixé | V |

### Avec recette — prix calculés (51)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O  Recette |
|---|---|---|---:|---:|---|---|---|---|
| `Glider_Scrap` | Planeur en ferraille | Rare | 19 | 6 300 | 6 diamants 3 or | calculé | V | 150× Ferraille + 6× Montage douteux + 6× Soie d'araignée + 2× Peau de grizzly corrompu du Néant · GoblinLab_Workbench |
| `Rail` | Rail |  |  | 67 | 6 argent 7 cuivres | calculé | V | 2× [N’importe quelles planches] + 1× Lingot de fer → ×4 · Workbench |
| `Rail_Kart` | Wagonnet |  |  | 3 100 | 3 diamants 1 or | calculé | V | 12× Lingot de fer · Workbench |
| `Rocket_Boots` | Bottes-fusées | Rare | 17 | 4 000 | 4 diamants | calculé | V | 30× Ferraille + 4× Montage douteux + 2× Bâton-pétard + 1× Peau de grizzly corrompu du Néant · GoblinLab_Workbench |
| `Scrap_Hook` | Ancre de grappin | Commun | 14 | 310 | 3 or 1 argent | calculé | V | 10× Ferraille + 3× Lingot de fer → ×3 · GoblinLab_Workbench |
| `Survival_Trap_Grass` | Piège en herbe |  |  | 39 | 3 argent 9 cuivres | calculé | V | 3× Fibre de plante |
| `Survival_Trap_Snapjaw` | Piège à ours |  |  | 780 | 7 or 8 argent | calculé | V | 3× Lingot de fer · Workbench |
| `Survival_Trap_Spike_Iron` | Piège à pointes en métal |  |  | 260 | 2 or 6 argent | calculé | V | 1× Lingot de fer · Workbench |
| `Survival_Trap_Spike_Wood` | Piège à pointes en bois |  |  | 26 | 2 argent 6 cuivres | calculé | V | 2× [N’importe quelle bûche] + 1× Sève · Workbench |
| `Teleporter` | Téléporteur | Rare | 30 | 110 | 1 or 1 argent | calculé | V | 8× Bûche azurée + 2× Algue azurée + 10× [N’importe quelle pierre] → ×2 · Arcanebench |
| `Tool_Capture_Crate` | Caisse de capture |  | 18 | 2 000 | 2 diamants | calculé | V | 4× [N’importe quel bois] + 50× Essence de vie · Farmingbench |
| `Tool_Drill_Scrap` | Foreuse en ferraille | Rare | 20 | 10 000 | 1 zénith | calculé | V | 100× Ferraille + 15× Lingot de fer + 8× Montage douteux + 2× Sceau des Embrasés · GoblinLab_Workbench |
| `Tool_Feedbag` | Sac de nourriture | Commun |  | 470 | 4 or 7 argent | calculé | V | 10× Blé + 5× [N’importe quel type de légume] + 5× [N’importe quel type de fruit] + 10× Essence de vie · Farmingbench |
| `Tool_Fertilizer` | Sac d’engrais |  | 21 | 540 | 5 or 4 argent | calculé | V | 10× Excrément + 10× Essence de vie + 5× [N’importe quel type de légume] · Farmingbench |
| `Tool_Fertilizer_Crystal` | Sac d’engrais cristal |  |  | 400 | 4 or | calculé | V | 25× [N’importe quel fragment de cristal] + 1× Essence du Néant · Alchemybench |
| `Tool_Fishing_Trap` | Piège à poissons |  | 19 | 2 500 | 2 diamants 5 or | calculé | V | 10× [N’importe quel bois] + 50× Essence de vie + 20× Fibre de plante + 1× Lingot de fer · Farmingbench |
| `Tool_Hammer_Crude` | Marteau de constructeur rudimentaire | Commun | 2 | 83 | 8 argent 3 cuivres | calculé | V | 2× [N’importe quels gravats] + 3× Fibre de plante + 3× Bâton · Workbench |
| `Tool_Hammer_Iron` | Marteau de constructeur en fer | Peu commun | 20 | 1 600 | 1 diamant 6 or | calculé | V | 6× Lingot de fer + 3× Cuir léger + 2× Chutes de lin · Workbench |
| `Tool_Hatchet_Adamantite` | Hachette en adamantite | Rare | 40 | 14 000 | 1 zénith 4 diamants | calculé | V | 8× Lingot d’adamantite + 3× Cuir épais + 4× Chutes d’étoffe de braise · Workbench |
| `Tool_Hatchet_Cobalt` | Hachette en cobalt | Rare | 35 | 7 200 | 7 diamants 2 or | calculé | V | 7× Lingot de cobalt + 2× Cuir épais + 4× Chutes de tisse-ombre · Workbench |
| `Tool_Hatchet_Copper` | Hachette en cuivre | Commun | 11 | 380 | 3 or 8 argent | calculé | V | 3× Lingot de cuivre + 6× [N’importe quelle bûche] + 3× Fibre de plante · Workbench |
| `Tool_Hatchet_Crude` | Hachette rudimentaire | Commun | 4 | 57 | 5 argent 7 cuivres | calculé | V | 2× [N’importe quels gravats] + 2× Fibre de plante + 2× Bâton · Fieldcraft |
| `Tool_Hatchet_Iron` | Hachette en fer | Peu commun | 20 | 1 400 | 1 diamant 4 or | calculé | V | 5× Lingot de fer + 2× Cuir léger + 2× Chutes de lin · Workbench |
| `Tool_Hatchet_Mithril` | Hachette en mithril | Épique | 50 | 23 000 | 2 zéniths 3 diamants | calculé | V | 9× Lingot de mithril + 4× Cuir de tempête · Workbench |
| `Tool_Hatchet_Thorium` | Hachette en thorium | Rare | 30 | 4 100 | 4 diamants 1 or | calculé | V | 6× Lingot de thorium + 2× Cuir moyen + 3× Chutes de lin · Workbench |
| `Tool_Hoe_Copper` | Houe en cuivre | Commun | 2 | 330 | 3 or 3 argent | calculé | V | 4× [N’importe quelle bûche] + 3× Lingot de cuivre · Farmingbench |
| `Tool_Hoe_Crude` | Houe rudimentaire | Commun | 1 | 30 | 3 argent | calculé | V | 4× [N’importe quelle bûche] + 3× [N’importe quelle pierre] · Farmingbench |
| `Tool_Hoe_Iron` | Houe en fer | Peu commun | 3 | 810 | 8 or 1 argent | calculé | V | 4× [N’importe quelle bûche] + 3× Lingot de fer · Farmingbench |
| `Tool_Hoe_Thorium` | Houe en thorium | Rare | 4 | 2 100 | 2 diamants 1 or | calculé | V | 4× [N’importe quelle bûche] + 3× Lingot de thorium · Farmingbench |
| `Tool_Hookshot` | Grappin | Rare | 15 | 1 600 | 1 diamant 6 or | calculé | V | 30× Ferraille + 4× Soie d'araignée + 1× Montage douteux + 1× Ancre de grappin · GoblinLab_Workbench |
| `Tool_Pickaxe_Adamantite` | Pioche en adamantite | Rare | 40 | 14 000 | 1 zénith 4 diamants | calculé | V | 8× Lingot d’adamantite + 3× Cuir épais + 4× Chutes d’étoffe de braise · Workbench |
| `Tool_Pickaxe_Cobalt` | Pioche en cobalt | Rare | 35 | 7 200 | 7 diamants 2 or | calculé | V | 7× Lingot de cobalt + 2× Cuir épais + 4× Chutes de tisse-ombre · Workbench |
| `Tool_Pickaxe_Copper` | Pioche en cuivre | Commun | 12 | 360 | 3 or 6 argent | calculé | V | 3× Lingot de cuivre + 6× [N’importe quelle bûche] + 1× Fibre de plante · Workbench |
| `Tool_Pickaxe_Crude` | Pioche rudimentaire | Commun | 5 | 57 | 5 argent 7 cuivres | calculé | V | 2× [N’importe quels gravats] + 2× Fibre de plante + 2× Bâton · Fieldcraft |
| `Tool_Pickaxe_Iron` | Pioche en fer | Peu commun | 20 | 1 400 | 1 diamant 4 or | calculé | V | 5× Lingot de fer + 2× Cuir léger + 2× Chutes de lin · Workbench |
| `Tool_Pickaxe_Mithril` | Pioche en mithril | Épique | 50 | 23 000 | 2 zéniths 3 diamants | calculé | V | 9× Lingot de mithril + 4× Cuir de tempête · Workbench |
| `Tool_Pickaxe_Thorium` | Pioche en thorium | Rare | 30 | 4 100 | 4 diamants 1 or | calculé | V | 6× Lingot de thorium + 2× Cuir moyen + 3× Chutes de lin · Workbench |
| `Tool_Repair_Kit_Goblin` | Kit de réparation gobelin | Peu commun | 16 | 1 200 | 1 diamant 2 or | calculé | V | 30× Ferraille + 3× Montage douteux + 2× Soie d'araignée + 1× Paquet de trésors → ×2 · GoblinLab_Workbench |
| `Tool_Repair_Kit_Iron` | Kit de réparation | Commun | 1 | 310 | 3 or 1 argent | calculé | V | 2× Chutes de lin + 1× Lingot de fer + 1× Cuir léger · Workbench |
| `Tool_Shears_Basic` | Cisailles rudimentaires | Commun | 16 | 260 | 2 or 6 argent | calculé | V | 1× Lingot de fer · Farmingbench |
| `Tool_Shovel_Iron` | Pelle en fer | Peu commun | 20 | 1 600 | 1 diamant 6 or | calculé | V | 6× Lingot de fer + 3× Cuir léger + 2× Chutes de lin · Workbench |
| `Tool_Sickle_Copper` | Faucille en cuivre | Commun | 10 | 110 | 1 or 1 argent | calculé | V | 2× [N’importe quelle bûche] + 1× Lingot de cuivre · Farmingbench |
| `Tool_Sickle_Crude` | Faucille rudimentaire | Commun | 5 | 14 | 1 argent 4 cuivres | calculé | V | 2× [N’importe quelle bûche] + 1× [N’importe quelle pierre] · Farmingbench |
| `Tool_Sickle_Iron` | Faucille en fer | Peu commun | 20 | 530 | 5 or 3 argent | calculé | V | 2× [N’importe quelle bûche] + 2× Lingot de fer · Farmingbench |
| `Tool_Spyglass` | Longue-vue | Peu commun | 13 | 1 400 | 1 diamant 4 or | calculé | V | 25× Ferraille + 10× Lingot de cuivre + 1× Fragments de cristal du Néant · GoblinLab_Workbench |
| `Tool_Trap_Bait` | Appât à poissons (sauvage) | Commun | 20 | 100 | 1 or | calculé | V | 3× [N’importe quel type de fruit] + 5× Essence de vie → ×2 · Farmingbench |
| `Tool_Watering_Can` | Arrosoir vide | Commun | 10 | 780 | 7 or 8 argent | calculé | V | 3× Lingot de fer · Farmingbench |
| `Utility_Helipack` | Hélisac | Commun | 20 | 1 100 | 1 diamant 1 or | calculé | V | 2× [N’importe quelle bûche] + 4× Lingot de fer · ArmorBench |
| `Utility_Leather_Backpack` | Sac à dos en cuir | Commun | 20 | 1 100 | 1 diamant 1 or | calculé | V | 2× [N’importe quelle bûche] + 4× Lingot de fer · ArmorBench |
| `Utility_Leather_Medium_Backpack` | Grand sac à dos | Commun | 20 | 1 100 | 1 diamant 1 or | calculé | V | 2× [N’importe quelle bûche] + 4× Lingot de fer · ArmorBench |
| `Utility_Leather_Quiver` | Carquois en cuir | Commun | 20 | 1 100 | 1 diamant 1 or | calculé | V | 2× [N’importe quelle bûche] + 4× Lingot de fer · ArmorBench |

<a id="fam-runes-competences"></a>
## Runes & compétences

### Sans recette — prix fixés (674)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Eldanior_Manuel_Magie_Air_Bourrasque` | Manuel d'Air : Bourrasque | Commun | 10 | 300 | 3 or | fixé | E |
| `Eldanior_Manuel_Magie_Air_CourrouxFujin` | Manuel d'Air : Courroux de Fūjin | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Eldanior_Manuel_Magie_Air_DanseDesVents` | Manuel d'Air : Danse des Vents | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Air_TempeteFeuilles` | Manuel d'Air : Tempête de Feuilles | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Air_WindStrike` | Manuel d'Air : Frappe de vent | Rare | 10 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Combat_AuraBlade` | Manuel de Combat : Aura Tranchante | Rare | 10 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Combat_ChainHook` | Manuel de Combat : Grappin à chaîne | Rare | 10 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Combat_Enrage` | Manuel de Combat : Rage | Rare | 10 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Combat_GilgameshBabylon` | Manuel de Combat : Gilgamesh Babylon | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Eldanior_Manuel_Magie_Combat_LancerDePieces` | Manuel de Combat : Lancer de Pièces | Commun | 10 | 300 | 3 or | fixé | E |
| `Eldanior_Manuel_Magie_Combat_PluieDeFleches` | Manuel de Combat : Pluie de Flèches | Commun | 10 | 300 | 3 or | fixé | E |
| `Eldanior_Manuel_Magie_Combat_Ruee` | Manuel de Combat : Ruée | Commun | 10 | 300 | 3 or | fixé | E |
| `Eldanior_Manuel_Magie_Combat_TransformationArme` | Manuel de Combat : Transformation d'Arme | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Dragonic_AvatarDragon` | Manuel Draconique : Avatar du Dragon Divin | Légendaire | 100 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Dragonic_AvenementBahamut` | Manuel Draconique : Avènement de Bahamut | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Eldanior_Manuel_Magie_Espace_Bannissement` | Manuel de l'Espace : Bannissement | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Espace_FissureVide` | Manuel de l'Espace : Fissure du Vide | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Espace_Labyrinthe` | Manuel de l'Espace : Labyrinthe | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Espace_LoiArcanique` | Manuel de l'Espace : Loi Arcanique | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Espace_OeilOmniscient` | Manuel de l'Espace : Œil Omniscient | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Espace_PontBifrost` | Manuel de l'Espace : Pont du Bifröst | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Eldanior_Manuel_Magie_Espace_PorteMondes` | Manuel de l'Espace : Porte des Mondes | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Espace_ProjectionAstrale` | Manuel de l'Espace : Projection Astrale | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Espace_Reecriture` | Manuel de l'Espace : Réécriture | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Feu_AuraDeBraise` | Manuel de Feu : Aura de Braise | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Feu_CourrouxMuspelheim` | Manuel de Feu : Courroux de Muspelheim | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Eldanior_Manuel_Magie_Feu_DomaineElementaire` | Manuel de Feu : Domaine Élémentaire | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Feu_EruptionPrimordiale` | Manuel de Feu : Éruption Primordiale | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Feu_Fireball` | Manuel de Feu : Boule de Feu | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Feu_Flammeche` | Manuel de Feu : Flammèche | Commun | 10 | 300 | 3 or | fixé | E |
| `Eldanior_Manuel_Magie_Feu_MarqueInfernale` | Manuel de Feu : Marque Infernale | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Feu_Meteore` | Manuel de Feu : Météore | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Feu_MurDeFlammes` | Manuel de Feu : Mur de Flammes | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Feu_PluieCometes` | Manuel de Feu : Pluie de Comètes | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Feu_Supernova` | Manuel de Feu : Supernova | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Foudre_AvatarOrage` | Manuel de Foudre : Avatar de l'Orage | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Foudre_ChaineDEclairs` | Manuel de Foudre : Chaîne d'Éclairs | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Foudre_Decharge` | Manuel de Foudre : Décharge | Commun | 10 | 300 | 3 or | fixé | E |
| `Eldanior_Manuel_Magie_Foudre_GlyphesFoudroyants` | Manuel de Foudre : Glyphes Foudroyants | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Foudre_NuageOrage` | Manuel de Foudre : Nuage d'Orage | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Foudre_OrbeStatique` | Manuel de Foudre : Orbe Statique | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Foudre_SanctuaireTaranis` | Manuel de Foudre : Sanctuaire de Taranis | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Eldanior_Manuel_Magie_Glace_Blizzard` | Manuel de Glace : Blizzard | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Glace_ChargedShot` | Manuel de Glace : Tir chargé | Rare | 10 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Glace_EclatDeGivre` | Manuel de Glace : Éclat de Givre | Commun | 10 | 300 | 3 or | fixé | E |
| `Eldanior_Manuel_Magie_Glace_LanceDeCristal` | Manuel de Glace : Lance de Cristal | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Glace_PorteNiflheim` | Manuel de Glace : Porte de Niflheim | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Eldanior_Manuel_Magie_Glace_PrisonDeGlace` | Manuel de Glace : Prison de Glace | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Glace_SculpteurDeGlace` | Manuel de Glace : Sculpteur de Glace | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Glace_SentierDHiver` | Manuel de Glace : Sentier d'Hiver | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Glace_SentinelleDeGivre` | Manuel de Glace : Sentinelle de Givre | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_ArmeBenie` | Manuel de Lumière : Arme Bénie | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_Clonage` | Manuel de Lumière : Clonage | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_EgideRoyale` | Manuel de Lumière : Égide Royale | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_Enseignement` | Manuel de Lumière : Enseignement | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_EspritFollet` | Manuel de Lumière : Esprit Follet | Commun | 10 | 300 | 3 or | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_FontaineDeVie` | Manuel de Lumière : Fontaine de Vie | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_GalerieDesGlaces` | Manuel de Lumière : Galerie des Glaces | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_HaloProtecteur` | Manuel de Lumière : Halo Protecteur | Commun | 10 | 300 | 3 or | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_LanceDeLumiere` | Manuel de Lumière : Lance de Lumière | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_LienDAme` | Manuel de Lumière : Lien d'Âme | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_Meditation` | Manuel de Lumière : Méditation | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_Miracle` | Manuel de Lumière : Miracle | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_MiroirTrompeur` | Manuel de Lumière : Miroir Trompeur | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_NaineBlanche` | Manuel de Lumière : Naine Blanche | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_PierrePhilosophale` | Manuel de Lumière : Pierre Philosophale | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_RayonArcanique` | Manuel de Lumière : Rayon Arcanique | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_RayonCeleste` | Manuel de Lumière : Rayon Céleste | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_Reflexion` | Manuel de Lumière : Réflexion | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_Resurrection` | Manuel de Lumière : Résurrection | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_Revelation` | Manuel de Lumière : Révélation | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_SalveArcanique` | Manuel de Lumière : Salve Arcanique | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_SanctuaireApollon` | Manuel de Lumière : Sanctuaire d'Apollon | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_SanctuaireMineur` | Manuel de Lumière : Sanctuaire Mineur | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Lumiere_SoinMineur` | Manuel de Lumière : Soin Mineur | Commun | 10 | 300 | 3 or | fixé | E |
| `Eldanior_Manuel_Magie_Spatial_AbimeChaos` | Manuel Spatial : Abîme du Chaos | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Eldanior_Manuel_Magie_Spatial_AutoriteGravitationnelle` | Manuel Spatial : Autorité Gravitationnelle | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Spatial_CeintureAsteroides` | Manuel Spatial : Ceinture d'Astéroïdes | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Spatial_CouronneAutorite` | Manuel Spatial : Couronne d'Autorité | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Spatial_Decret` | Manuel Spatial : Décret | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Spatial_Inversion` | Manuel Spatial : Inversion | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Spatial_LentilleGravitationnelle` | Manuel Spatial : Lentille Gravitationnelle | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Spatial_MareeGravitationnelle` | Manuel Spatial : Marée Gravitationnelle | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Spatial_MatiereVive` | Manuel Spatial : Matière Vive | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Spatial_OndeOriginelle` | Manuel Spatial : Onde Originelle | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Spatial_OndeRepulsion` | Manuel Spatial : Onde de Répulsion | Commun | 10 | 300 | 3 or | fixé | E |
| `Eldanior_Manuel_Magie_Spatial_PuitsGravitationnel` | Manuel Spatial : Puits Gravitationnel | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Spatial_Telekinesie` | Manuel Spatial : Télékinésie | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Speciales_Aura` | Manuel Spécial : Aura | Légendaire | 10 | **à fixer** |  | à fixer | E |
| `Eldanior_Manuel_Magie_Temps_ArretDuTemps` | Manuel du Temps : Arrêt du Temps | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Temps_BulleTemporelle` | Manuel du Temps : Bulle Temporelle | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Temps_Distorsion` | Manuel du Temps : Distorsion | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Temps_EchosDuPasse` | Manuel du Temps : Échos du Passé | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Temps_Eternite` | Manuel du Temps : Éternité | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Temps_Presage` | Manuel du Temps : Présage | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Temps_Prescience` | Manuel du Temps : Prescience | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Temps_Prophetie` | Manuel du Temps : Prophétie | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Temps_RetourEnArriere` | Manuel du Temps : Retour en Arrière | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Temps_SablierChronos` | Manuel du Temps : Sablier de Chronos | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Eldanior_Manuel_Magie_Temps_SavoirAccelere` | Manuel du Temps : Savoir Accéléré | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Temps_Sentence` | Manuel du Temps : Sentence | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Temps_Surregime` | Manuel du Temps : Surrégime | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_AmesErrantes` | Manuel des Ténèbres : Âmes Errantes | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_ArkiesDomini` | Manuel des Ténèbres : Arkies Domini | Légendaire | 90 | **à fixer** |  | à fixer | E |
| `Eldanior_Manuel_Magie_Tenebres_ArmeeDesTombes` | Manuel des Ténèbres : Armée des Tombes | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_ChainesNoires` | Manuel des Ténèbres : Chaînes Noires | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_DomaineDeLaMort` | Manuel des Ténèbres : Domaine de la Mort | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_Doppelganger` | Manuel des Ténèbres : Doppelgänger | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_Effacement` | Manuel des Ténèbres : Effacement | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_FormeSpectrale` | Manuel des Ténèbres : Forme Spectrale | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_Hallucination` | Manuel des Ténèbres : Hallucination | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_Marionnette` | Manuel des Ténèbres : Marionnette | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_MarqueFuneste` | Manuel des Ténèbres : Marque Funeste | Commun | 10 | 300 | 3 or | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_MeuteSpectrale` | Manuel des Ténèbres : Meute Spectrale | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_NuageToxique` | Manuel des Ténèbres : Nuage Toxique | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_PacteDeSang` | Manuel des Ténèbres : Pacte de Sang | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_Phylactere` | Manuel des Ténèbres : Phylactère | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_PoisonImbue` | Manuel des Ténèbres : Imprégnation de poison | Rare | 10 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_PuitsDAmes` | Manuel des Ténèbres : Puits d'Âmes | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_ReleveDesMorts` | Manuel des Ténèbres : Relève des Morts | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_SansVisage` | Manuel des Ténèbres : Sans Visage | Légendaire | 90 | **à fixer** |  | à fixer | E |
| `Eldanior_Manuel_Magie_Tenebres_SentenceSilencieuse` | Manuel des Ténèbres : Sentence Silencieuse | Légendaire | 90 | **à fixer** |  | à fixer | E |
| `Eldanior_Manuel_Magie_Tenebres_SilenceNeant` | Manuel des Ténèbres : Silence du Néant | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_VoileDeFumee` | Manuel des Ténèbres : Voile de Fumée | Commun | 10 | 300 | 3 or | fixé | E |
| `Eldanior_Manuel_Magie_Tenebres_VoileNyx` | Manuel des Ténèbres : Trône du Néant Éternel | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Eldanior_Manuel_Magie_Terre_Bosquet` | Manuel de Terre : Bosquet | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Terre_ColossePierre` | Manuel de Terre : Colosse de Pierre | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Terre_EspritForet` | Manuel de Terre : Esprit de la Forêt | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Terre_EveilYggdrasil` | Manuel de Terre : Éveil d'Yggdrasil | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Eldanior_Manuel_Magie_Terre_Faconnage` | Manuel de Terre : Façonnage | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Terre_GolemDePierre` | Manuel de Terre : Golem de Pierre | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Terre_GolemsCristal` | Manuel de Terre : Golems de Cristal | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Terre_GroundSlam` | Manuel de Terre : Frappe au sol | Rare | 10 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Terre_Petrification` | Manuel de Terre : Pétrification | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Terre_PilierAscendant` | Manuel de Terre : Pilier Ascendant | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Terre_PilierDuMonde` | Manuel de Terre : Pilier du Monde | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Manuel_Magie_Terre_PlanArchitecte` | Manuel de Terre : Plan d'Architecte | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Terre_Racines` | Manuel de Terre : Racines | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Terre_Rempart` | Manuel de Terre : Rempart | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Manuel_Magie_Terre_ReveilGaia` | Manuel de Terre : Réveil de Gaïa | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Eldanior_Manuel_Magie_Terre_SablesMouvants` | Manuel de Terre : Sables Mouvants | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Manuel_Magie_Terre_TremblementTectonique` | Manuel de Terre : Tremblement Tectonique | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Eldanior_Parchemin_Metier_Agriculture` | Parchemin de Métier : Agriculture | Commun |  | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Parchemin_Metier_Alchimie` | Parchemin de Métier : Alchimie | Rare |  | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Parchemin_Metier_Armurerie` | Parchemin de Métier : Armurerie | Épique |  | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Parchemin_Metier_Artisan` | Parchemin de Métier : Artisan | Commun |  | 800 | 8 or | fixé | E |
| `Eldanior_Parchemin_Metier_Artisanat` | Parchemin de Métier : Artisanat | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `Eldanior_Parchemin_Metier_Cuisine` | Parchemin de Métier : Cuisine | Peu commun |  | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Parchemin_Metier_Fonderie` | Parchemin de Métier : Fonderie | Épique |  | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Parchemin_Metier_Forge_Armes` | Parchemin de Métier : Forge d'Armes | Épique |  | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Parchemin_Metier_Forge_Divine` | Parchemin de Métier : Forge Divine | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Eldanior_Parchemin_Metier_Recyclage` | Parchemin de Métier : Recyclage | Peu commun |  | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Parchemin_Metier_Scierie` | Parchemin de Métier : Scierie | Peu commun |  | 8 000 | 8 diamants | fixé | E |
| `Eldanior_Parchemin_Metier_Tannerie` | Parchemin de Métier : Tannerie | Peu commun |  | 8 000 | 8 diamants | fixé | E |
| `Rune_AbimeChaos` | Rune de l'Abîme du Chaos | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Rune_AmesErrantes` | Rune des Âmes Errantes | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_ArkiesDomini` | Rune Arkies Domini | Légendaire | 90 | **à fixer** |  | à fixer | E |
| `Rune_ArmeBenie` | Rune de l'Arme Bénie | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_ArmeeDesTombes` | Rune de l'Armée des Tombes | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_ArretDuTemps` | Rune de l'Arrêt du Temps | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_Aura` | Rune d'Aura | Légendaire | 10 | **à fixer** |  | à fixer | E |
| `Rune_AuraBlade` | Rune d'Aura Tranchante | Rare | 10 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_AuraDeBraise` | Rune de l'Aura de Braise | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_AutoriteGravitationnelle` | Rune de l'Autorité Gravitationnelle | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Rune_AvatarDragon` | Rune de l'Avatar du Dragon Divin | Légendaire | 100 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Rune_AvatarOrage` | Rune de l'Avatar de l'Orage | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_AvenementBahamut` | Rune de l'Avènement de Bahamut | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Rune_Bannissement` | Rune du Bannissement | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_Blizzard` | Rune du Blizzard | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_Bosquet` | Rune du Bosquet | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_Bourrasque` | Rune de Bourrasque | Commun | 10 | 300 | 3 or | fixé | E |
| `Rune_BulleTemporelle` | Rune de la Bulle Temporelle | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_CeintureAsteroides` | Rune de la Ceinture d'Astéroïdes | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_ChaineDEclairs` | Rune de la Chaîne d'Éclairs | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_ChainesNoires` | Rune des Chaînes Noires | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_ChainHook` | Grappin à chaîne | Rare | 10 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_ChargedShot` | Tir chargé | Rare | 10 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_Clonage` | Rune de Clonage | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_ColossePierre` | Rune du Colosse de Pierre | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Rune_CouronneAutorite` | Rune de la Couronne d'Autorité | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_CourrouxFujin` | Rune du Courroux de Fūjin | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Rune_CourrouxMuspelheim` | Rune du Courroux de Muspelheim | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Rune_CroissantKi` | Rune du Croissant de Ki | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_DanseDesVents` | Rune de la Danse des Vents | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_Decharge` | Rune de Décharge | Commun | 10 | 300 | 3 or | fixé | E |
| `Rune_Decret` | Rune du Décret | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_Distorsion` | Rune de Distorsion | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_DomaineDeLaMort` | Rune du Domaine de la Mort | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_DomaineDivin` | Rune du Domaine Divin | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Rune_DomaineElementaire` | Rune du Domaine Élémentaire | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_DomaineEpee` | Rune du Domaine de l'Épée | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_Doppelganger` | Rune du Doppelgänger | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_EchosDuPasse` | Rune des Échos du Passé | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_EclatDeGivre` | Rune d'Éclat de Givre | Commun | 10 | 300 | 3 or | fixé | E |
| `Rune_Effacement` | Rune de l'Effacement | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_EgideRoyale` | Rune de l'Égide Royale | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_Enrage` | Rage | Rare | 10 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_Enseignement` | Rune de l'Enseignement | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_EruptionPrimordiale` | Rune de l'Éruption Primordiale | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_EspritFollet` | Rune de l'Esprit Follet | Commun | 10 | 300 | 3 or | fixé | E |
| `Rune_EspritForet` | Rune de l'Esprit de la Forêt | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_Eternite` | Rune de l'Éternité | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_EveilYggdrasil` | Rune de l'Éveil d'Yggdrasil | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Rune_ExecutionCeleste` | Rune de l'Exécution Céleste | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Rune_Faconnage` | Rune du Façonnage | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_Fireball` | Rune de Boule de Feu | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_FissureVide` | Rune de la Fissure du Vide | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_Flammeche` | Rune de Flammèche | Commun | 10 | 300 | 3 or | fixé | E |
| `Rune_FleurPrunier` | Rune de la Fleur de Prunier | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_FontaineDeVie` | Rune de la Fontaine de Vie | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_FormeSpectrale` | Rune de la Forme Spectrale | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_FrappeMeridiens` | Rune de la Frappe des Méridiens | Peu commun | 25 | 800 | 8 or | fixé | E |
| `Rune_GalerieDesGlaces` | Rune de la Galerie des Glaces | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_GardeFleuve` | Rune de la Garde du Fleuve | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_GilgameshBabylon` | Rune de Gilgamesh Babylon | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Rune_GlyphesFoudroyants` | Rune des Glyphes Foudroyants | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_GolemDePierre` | Rune du Golem de Pierre | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_GolemsCristal` | Rune des Golems de Cristal | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_GroundSlam` | Frappe au sol | Rare | 10 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_Hallucination` | Rune de l'Hallucination | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_HaloProtecteur` | Rune du Halo Protecteur | Commun | 10 | 300 | 3 or | fixé | E |
| `Rune_IncarnationAme` | Rune de l'Incarnation de l'Âme | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Rune_Inversion` | Rune de l'Inversion | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_KiEpee` | Rune du Ki de l'Épée | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_Labyrinthe` | Rune du Labyrinthe | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_LanceDeCristal` | Rune de la Lance de Cristal | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_LanceDeLumiere` | Rune de la Lance de Lumière | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_LancerDePieces` | Rune du Lancer de Pièces | Commun | 10 | 300 | 3 or | fixé | E |
| `Rune_LentilleGravitationnelle` | Rune de la Lentille Gravitationnelle | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_LienDAme` | Rune du Lien d'Âme | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_LoiArcanique` | Rune de la Loi Arcanique | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_MareeGravitationnelle` | Rune de la Marée Gravitationnelle | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Rune_Marionnette` | Rune de la Marionnette | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_MarqueFuneste` | Rune de la Marque Funeste | Commun | 10 | 300 | 3 or | fixé | E |
| `Rune_MarqueInfernale` | Rune de la Marque Infernale | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_MatiereVive` | Rune de la Matière Vive | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_Meditation` | Rune de la Méditation | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_MeditationMartiale` | Rune de la Méditation martiale | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_Meteore` | Rune du Météore | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_MeuteSpectrale` | Rune de la Meute Spectrale | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_Miracle` | Rune du Miracle | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_MiroirTrompeur` | Rune du Miroir Trompeur | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_MurDeFlammes` | Rune du Mur de Flammes | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_NaineBlanche` | Rune de la Naine Blanche | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_NuageOrage` | Rune du Nuage d'Orage | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_NuageToxique` | Rune du Nuage Toxique | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_OeilOmniscient` | Rune de l'Œil Omniscient | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_OndeOriginelle` | Rune de l'Onde Originelle | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_OndeRepulsion` | Rune d'Onde de Répulsion | Commun | 10 | 300 | 3 or | fixé | E |
| `Rune_OrbeStatique` | Rune de l'Orbe Statique | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_PacteDeSang` | Rune du Pacte de Sang | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `rune_page_abime_chaos` | Parchemin de Rune : Abîme du Chaos | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `rune_page_ames_errantes` | Parchemin de Rune : Âmes Errantes | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_arkiesdomini` | Parchemin de Rune : Arkies Domini | Légendaire |  | **à fixer** |  | à fixer | E |
| `rune_page_arme_benie` | Parchemin de Rune : Arme Bénie | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_armee_des_tombes` | Parchemin de Rune : Armée des Tombes | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_arret_du_temps` | Parchemin de Rune : Arrêt du Temps | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_aura_blade` | Parchemin de Rune : Aura Tranchante | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_aura_de_braise` | Parchemin de Rune : Aura de Braise | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_autorite_gravitationnelle` | Parchemin de Rune : Autorité Gravitationnelle | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `rune_page_avatar_dragon` | Parchemin de Rune : Avatar du Dragon Divin | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `rune_page_avatar_orage` | Parchemin de Rune : Avatar de l'Orage | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_avenement_bahamut` | Parchemin de Rune : Avènement de Bahamut | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `rune_page_bannissement` | Parchemin de Rune : Bannissement | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_blizzard` | Parchemin de Rune : Blizzard | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_bosquet` | Parchemin de Rune : Bosquet | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_bourrasque` | Parchemin de Rune : Bourrasque | Commun |  | 300 | 3 or | fixé | E |
| `rune_page_bulle_temporelle` | Parchemin de Rune : Bulle Temporelle | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_ceinture_asteroides` | Parchemin de Rune : Ceinture d'Astéroïdes | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_chain_hook` | Parchemin de Rune : Grappin | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_chaine_d_eclairs` | Parchemin de Rune : Chaîne d'Éclairs | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_chaines_noires` | Parchemin de Rune : Chaînes Noires | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_charged_shot` | Parchemin de Rune : Tir Charge | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_clonage` | Parchemin de Rune : Clonage | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_colosse_pierre` | Parchemin de Rune : Colosse de Pierre | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `rune_page_couronne_autorite` | Parchemin de Rune : Couronne d'Autorité | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_courroux_fujin` | Parchemin de Rune : Courroux de Fūjin | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `rune_page_courroux_muspelheim` | Parchemin de Rune : Courroux de Muspelheim | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `rune_page_danse_des_vents` | Parchemin de Rune : Danse des Vents | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_decharge` | Parchemin de Rune : Décharge | Commun |  | 300 | 3 or | fixé | E |
| `rune_page_decret` | Parchemin de Rune : Décret | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_distorsion` | Parchemin de Rune : Distorsion | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_domaine_de_la_mort` | Parchemin de Rune : Domaine de la Mort | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_domaine_elementaire` | Parchemin de Rune : Domaine Élémentaire | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_doppelganger` | Parchemin de Rune : Doppelgänger | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_echos_du_passe` | Parchemin de Rune : Échos du Passé | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_eclat_de_givre` | Parchemin de Rune : Éclat de Givre | Commun |  | 300 | 3 or | fixé | E |
| `rune_page_effacement` | Parchemin de Rune : Effacement | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_egide_royale` | Parchemin de Rune : Égide Royale | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_enrage` | Parchemin de Rune : Rage | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_enseignement` | Parchemin de Rune : Enseignement | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_eruption_primordiale` | Parchemin de Rune : Éruption Primordiale | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_esprit_follet` | Parchemin de Rune : Esprit Follet | Commun |  | 300 | 3 or | fixé | E |
| `rune_page_esprit_foret` | Parchemin de Rune : Esprit de la Forêt | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_eternite` | Parchemin de Rune : Éternité | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_eveil_yggdrasil` | Parchemin de Rune : Éveil d'Yggdrasil | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `rune_page_faconnage` | Parchemin de Rune : Façonnage | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_fireball` | Parchemin de Rune : Boule de Feu | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_fissure_vide` | Parchemin de Rune : Fissure du Vide | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_flammeche` | Parchemin de Rune : Flammèche | Commun |  | 300 | 3 or | fixé | E |
| `rune_page_fontaine_de_vie` | Parchemin de Rune : Fontaine de Vie | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_forme_spectrale` | Parchemin de Rune : Forme Spectrale | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_galerie_des_glaces` | Parchemin de Rune : Galerie des Glaces | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_gilgamesh_babylon` | Parchemin de Rune : Gilgamesh Babylon | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `rune_page_glyphes_foudroyants` | Parchemin de Rune : Glyphes Foudroyants | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_golem_de_pierre` | Parchemin de Rune : Golem de Pierre | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_golems_cristal` | Parchemin de Rune : Golems de Cristal | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_ground_slam` | Parchemin de Rune : Frappe du Sol | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_hallucination` | Parchemin de Rune : Hallucination | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_halo_protecteur` | Parchemin de Rune : Halo Protecteur | Commun |  | 300 | 3 or | fixé | E |
| `rune_page_inversion` | Parchemin de Rune : Inversion | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_labyrinthe` | Parchemin de Rune : Labyrinthe | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_lance_de_cristal` | Parchemin de Rune : Lance de Cristal | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_lance_de_lumiere` | Parchemin de Rune : Lance de Lumière | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_lancer_de_pieces` | Parchemin de Rune : Lancer de Pièces | Commun |  | 300 | 3 or | fixé | E |
| `rune_page_lentille_gravitationnelle` | Parchemin de Rune : Lentille Gravitationnelle | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_lien_d_ame` | Parchemin de Rune : Lien d'Âme | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_loi_arcanique` | Parchemin de Rune : Loi Arcanique | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_maree_gravitationnelle` | Parchemin de Rune : Marée Gravitationnelle | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `rune_page_marionnette` | Parchemin de Rune : Marionnette | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_marque_funeste` | Parchemin de Rune : Marque Funeste | Commun |  | 300 | 3 or | fixé | E |
| `rune_page_marque_infernale` | Parchemin de Rune : Marque Infernale | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_matiere_vive` | Parchemin de Rune : Matière Vive | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_meditation` | Parchemin de Rune : Méditation | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_meteore` | Parchemin de Rune : Météore | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_meute_spectrale` | Parchemin de Rune : Meute Spectrale | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_miracle` | Parchemin de Rune : Miracle | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_miroir_trompeur` | Parchemin de Rune : Miroir Trompeur | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_mur_de_flammes` | Parchemin de Rune : Mur de Flammes | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_naine_blanche` | Parchemin de Rune : Naine Blanche | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_nuage_orage` | Parchemin de Rune : Nuage d'Orage | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_nuage_toxique` | Parchemin de Rune : Nuage Toxique | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_oeil_omniscient` | Parchemin de Rune : Œil Omniscient | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_onde_originelle` | Parchemin de Rune : Onde Originelle | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_onde_repulsion` | Parchemin de Rune : Onde de Répulsion | Commun |  | 300 | 3 or | fixé | E |
| `rune_page_orbe_statique` | Parchemin de Rune : Orbe Statique | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_pacte_de_sang` | Parchemin de Rune : Pacte de Sang | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_petrification` | Parchemin de Rune : Pétrification | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_phylactere` | Parchemin de Rune : Phylactère | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_pierre_philosophale` | Parchemin de Rune : Pierre Philosophale | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_pilier_ascendant` | Parchemin de Rune : Pilier Ascendant | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_pilier_du_monde` | Parchemin de Rune : Pilier du Monde | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_plan_architecte` | Parchemin de Rune : Plan d'Architecte | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_pluie_cometes` | Parchemin de Rune : Pluie de Comètes | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_pluie_de_fleches` | Parchemin de Rune : Pluie de Flèches | Commun |  | 300 | 3 or | fixé | E |
| `rune_page_poison_imbue` | Parchemin de Rune : Impregnation Toxique | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_pont_bifrost` | Parchemin de Rune : Pont du Bifröst | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `rune_page_porte_mondes` | Parchemin de Rune : Porte des Mondes | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `rune_page_porte_niflheim` | Parchemin de Rune : Porte de Niflheim | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `rune_page_presage` | Parchemin de Rune : Présage | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_prescience` | Parchemin de Rune : Prescience | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_prison_de_glace` | Parchemin de Rune : Prison de Glace | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_projection_astrale` | Parchemin de Rune : Projection Astrale | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_prophetie` | Parchemin de Rune : Prophétie | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `rune_page_puits_d_ames` | Parchemin de Rune : Puits d'Âmes | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_puits_gravitationnel` | Parchemin de Rune : Puits Gravitationnel | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_racines` | Parchemin de Rune : Racines | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_rayon_arcanique` | Parchemin de Rune : Rayon Arcanique | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_rayon_celeste` | Parchemin de Rune : Rayon Céleste | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_reecriture` | Parchemin de Rune : Réécriture | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_reflexion` | Parchemin de Rune : Réflexion | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_releve_des_morts` | Parchemin de Rune : Relève des Morts | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_rempart` | Parchemin de Rune : Rempart | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_resurrection` | Parchemin de Rune : Résurrection | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_retour_en_arriere` | Parchemin de Rune : Retour en Arrière | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_reveil_gaia` | Parchemin de Rune : Réveil de Gaïa | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `rune_page_revelation` | Parchemin de Rune : Révélation | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_ruee` | Parchemin de Rune : Ruée | Commun |  | 300 | 3 or | fixé | E |
| `rune_page_sables_mouvants` | Parchemin de Rune : Sables Mouvants | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_sablier_chronos` | Parchemin de Rune : Sablier de Chronos | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `rune_page_salve_arcanique` | Parchemin de Rune : Salve Arcanique | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_sanctuaire_apollon` | Parchemin de Rune : Sanctuaire d'Apollon | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `rune_page_sanctuaire_mineur` | Parchemin de Rune : Sanctuaire Mineur | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_sanctuaire_taranis` | Parchemin de Rune : Sanctuaire de Taranis | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `rune_page_sansvisage` | Parchemin de Rune : Sans Visage | Légendaire |  | **à fixer** |  | à fixer | E |
| `rune_page_savoir_accelere` | Parchemin de Rune : Savoir Accéléré | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_sculpteur_de_glace` | Parchemin de Rune : Sculpteur de Glace | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_sentence` | Parchemin de Rune : Sentence | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_sentencesilencieuse` | Parchemin de Rune : Sentence Silencieuse | Légendaire |  | **à fixer** |  | à fixer | E |
| `rune_page_sentier_d_hiver` | Parchemin de Rune : Sentier d'Hiver | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_sentinelle_de_givre` | Parchemin de Rune : Sentinelle de Givre | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `rune_page_silence_neant` | Parchemin de Rune : Silence du Néant | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_soin_mineur` | Parchemin de Rune : Soin Mineur | Commun |  | 300 | 3 or | fixé | E |
| `rune_page_supernova` | Parchemin de Rune : Supernova | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `rune_page_surregime` | Parchemin de Rune : Surrégime | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_telekinesie` | Parchemin de Rune : Télékinésie | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_tempete_feuilles` | Parchemin de Rune : Tempête de Feuilles | Épique |  | 8 000 | 8 diamants | fixé | E |
| `rune_page_transformation_arme` | Parchemin de Rune : Transformation d'Arme | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `rune_page_tremblement_tectonique` | Parchemin de Rune : Tremblement Tectonique | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `rune_page_voile_de_fumee` | Parchemin de Rune : Voile de Fumée | Commun |  | 300 | 3 or | fixé | E |
| `rune_page_voile_nyx` | Parchemin de Rune : Trône du Néant Éternel | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `rune_page_wind_strike` | Parchemin de Rune : Frappe du Vent | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_PasLeger` | Rune du Pas léger | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_PaumeKi` | Rune de la Paume de Ki | Commun | 10 | 300 | 3 or | fixé | E |
| `Rune_Petrification` | Rune de la Pétrification | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_Phylactere` | Rune du Phylactère | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_PierrePhilosophale` | Rune de la Pierre Philosophale | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_PilierAscendant` | Rune du Pilier Ascendant | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_PilierDuMonde` | Rune du Pilier du Monde | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_PlanArchitecte` | Rune du Plan d'Architecte | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_PluieCometes` | Rune de la Pluie de Comètes | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_PluieDeFleches` | Rune de Pluie de Flèches | Commun | 10 | 300 | 3 or | fixé | E |
| `Rune_PoisonImbue` | Imprégnation de poison | Rare | 10 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_PontBifrost` | Rune du Pont du Bifröst | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Rune_PorteMondes` | Rune de la Porte des Mondes | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Rune_PorteNiflheim` | Rune de la Porte de Niflheim | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Rune_Presage` | Rune du Présage | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_Prescience` | Rune de Prescience | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_PrisonDeGlace` | Rune de la Prison de Glace | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_ProjectionAstrale` | Rune de Projection Astrale | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_ProjectionEpeeAura` | Rune de la Projection d'épée d'Aura | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_Prophetie` | Rune de la Prophétie | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Rune_PuitsDAmes` | Rune du Puits d'Âmes | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_PuitsGravitationnel` | Rune du Puits Gravitationnel | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_Racines` | Rune des Racines | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_RayonArcanique` | Rune du Rayon Arcanique | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_RayonCeleste` | Rune du Rayon Céleste | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_Reecriture` | Rune de la Réécriture | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_Reflexion` | Rune de la Réflexion | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_ReleveDesMorts` | Rune de la Relève des Morts | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_Rempart` | Rune du Rempart | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_RenforcementCorps` | Rune du Renforcement corporel | Peu commun | 25 | 800 | 8 or | fixé | E |
| `Rune_Resurrection` | Rune de la Résurrection | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_RetourEnArriere` | Rune du Retour en Arrière | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_ReveilGaia` | Rune du Réveil de Gaïa | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Rune_Revelation` | Rune de la Révélation | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_Ruee` | Rune de Ruée | Commun | 10 | 300 | 3 or | fixé | E |
| `Rune_SablesMouvants` | Rune des Sables Mouvants | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_SablierChronos` | Rune du Sablier de Chronos | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Rune_SalveArcanique` | Rune de la Salve Arcanique | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_SanctuaireApollon` | Rune du Sanctuaire d'Apollon | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Rune_SanctuaireMineur` | Rune du Sanctuaire Mineur | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_SanctuaireTaranis` | Rune du Sanctuaire de Taranis | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Rune_SansVisage` | Rune Sans Visage | Légendaire | 90 | **à fixer** |  | à fixer | E |
| `Rune_SavoirAccelere` | Rune du Savoir Accéléré | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_SculpteurDeGlace` | Rune du Sculpteur de Glace | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_SensMartial` | Rune du Sens martial | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_Sentence` | Rune de la Sentence | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_SentenceSilencieuse` | Rune Sentence Silencieuse | Légendaire | 90 | **à fixer** |  | à fixer | E |
| `Rune_SentierDHiver` | Rune du Sentier d'Hiver | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_SentinelleDeGivre` | Rune de la Sentinelle de Givre | Rare | 40 | 2 500 | 2 diamants 5 or | fixé | E |
| `Rune_SilenceNeant` | Rune du Silence du Néant | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_SoinMineur` | Rune de Soin Mineur | Commun | 10 | 300 | 3 or | fixé | E |
| `Rune_Supernova` | Rune de la Supernova | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Rune_Surregime` | Rune du Surrégime | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_Telekinesie` | Rune de Télékinésie | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_TempeteFeuilles` | Rune de la Tempête de Feuilles | Épique | 60 | 8 000 | 8 diamants | fixé | E |
| `Rune_TransformationArme` | Rune de Transformation d'Arme | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Rune_TransformationHwakyung` | Rune de la Transformation Hwakyung | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Rune_TremblementTectonique` | Rune du Tremblement Tectonique | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_UniteCielTerre` | Rune de l'Unité du Ciel et de la Terre | Légendaire | 90 | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `Rune_VoileDeFumee` | Rune du Voile de Fumée | Commun | 10 | 300 | 3 or | fixé | E |
| `Rune_VoileNyx` | Rune du Trône du Néant Éternel | Divin | 100 | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `Rune_VolEpeeKi` | Rune du Vol sur épée de Ki | Unique | 75 | 30 000 | 3 zéniths | fixé | E |
| `Rune_WindStrike` | Frappe de vent | Rare | 10 | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_absolute_supremacy` | Puissance Débordante : Suprématie Absolue (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_abyss_blade` | Entaille Profonde : Lame de l'Abîme (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_acrobatic_poise` | Souplesse : Souplesse Acrobatique (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_adamantine_bulwark` | Rempart : Rempart d'Adamantine (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_adaptive_shield` | Rempart : Bouclier Adaptatif (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_ancient_blood` | Sang Vital : Sang Ancestral (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_ange_bienveillance` | Bienveillance (Chamuel) | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `skill_page_ange_charite` | Charité (Gabriel) | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `skill_page_ange_chastete` | Chasteté (Azraël) | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `skill_page_ange_diligence` | Diligence (Métatron) | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `skill_page_ange_humilite` | Humilité (Michaël) | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `skill_page_ange_patience` | Patience (Raphaël) | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `skill_page_ange_temperance` | Tempérance (Uriel) | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `skill_page_annihilating_pressure` | Point de Pression : Pression Annihilante (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_annihilator_strike` | Frappe Instinctive : Frappe de l'Annihilateur (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_arcane_annihilation` | Frappe Arcanique : Annihilation Arcanique (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_arcane_devastation` | Frappe Arcanique : Dévastation Arcanique (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_arcane_genesis` | Frappe Arcanique : Genèse Arcanique (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_arcane_oblivion` | Frappe Arcanique : Oubli Arcanique (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_arcane_shield` | Barrière de Mana : Bouclier Arcanique (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_arcane_strike` | Frappe Arcanique (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_arcane_supremacy` | Puissance Débordante : Suprématie Arcanique (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_arcane_vampirism` | Siphon Spirituel : Vampirisme Arcanique (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_artifact_hunter` | Chasseur de Trésors : Chasseur d'Artefacts (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_assassin_blade_mastery` | Maîtrise de la Dague : Maîtrise de la Lame d'Assassin (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_astral_cloak` | Voile Mystique : Cape Astrale (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_awakened_mind` | Esprit Éveillé (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_axe_mastery` | Maîtrise de la Hache (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_battle_frenzy` | Vigueur Combative : Frénésie de Combat (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_beast_guard` | Garde du Chasseur : Garde Bestiale (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_blood_frenzy` | Vigueur Combative : Frénésie Sanguinaire (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_blood_hunt` | Traque : Traque Sanguinaire (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_bow_mastery` | Maîtrise de l'Arc (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_brilliant_mind` | Esprit Éveillé : Esprit Brillant (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_bronze_skin` | Peau de Pierre : Peau de Bronze (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_bursting_life` | Force Vitale : Vie Explosive (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_catlike_poise` | Souplesse : Souplesse Féline (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_celestial_constitution` | Constitution : Constitution Céleste (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_celestial_fortress` | Posture Solide : Forteresse Céleste (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_celestial_immortality` | Ténacité : Immortalité Céleste (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_celestial_lungs` | Souffle Inépuisable : Poumons Célestes (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_celestial_parry` | Parade : Parade Céleste (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_celestial_step` | Pas de Vent : Pas Céleste (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_cellular_regeneration` | Régénération Cellulaire (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_combat_intuition` | Instinct de Survie : Intuition de Combat (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_combat_vigor` | Vigueur Combative (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_cosmic_body` | Corps Robuste : Corps Cosmique (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_cosmic_bulwark` | Rempart : Rempart Cosmique (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_cosmic_constitution` | Constitution : Constitution Cosmique (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_cosmic_lungs` | Souffle Inépuisable : Poumons Cosmiques (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_cosmic_mind` | Esprit Éveillé : Esprit Cosmique (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_cosmic_omen` | Bon Présage : Présage Cosmique (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_cosmic_strike` | Frappe Chanceuse : Frappe Cosmique (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_cosmic_vampirism` | Siphon Spirituel : Vampirisme Cosmique (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_crimson_blade` | Entaille Profonde : Lame Pourpre (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_critical_luck` | Frappe Chanceuse : Chance Critique (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_crushing_pressure` | Point de Pression : Pression Écrasante (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_dagger_mastery` | Maîtrise de la Dague (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_danger_sense` | Instinct de Survie : Sens du Danger (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_death_hunt` | Traque : Traque Mortelle (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_deep_slash` | Entaille Profonde (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_destiny_strike` | Frappe Chanceuse : Frappe du Destin (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_diamond_body` | Corps Robuste : Corps de Diamant (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_diamond_skin` | Peau de Pierre : Peau de Diamant (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_dimensional_dodge` | Esquive : Esquive Dimensionnelle (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_dimensional_step` | Pas de Vent : Pas Dimensionnel (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_divin_axe_mastery` | Maîtrise de la Hache : Maîtrise Divine de la Hache (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_divin_dagger_mastery` | Maîtrise de la Dague : Maîtrise Divine de la Dague (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_divine_bulwark` | Rempart : Rempart Divin (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_divine_fortress` | Posture Solide : Forteresse Divine (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_divine_parry` | Parade : Parade Divine (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_divine_strike` | Frappe Chanceuse : Frappe Divine (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_dragon_blood` | Sang Vital : Sang de Dragon (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_dragon_lungs` | Souffle Inépuisable : Poumons de Dragon (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_dragon_slayer_guard` | Garde du Chasseur : Garde du Tueur de Dragon (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_dragon_spear_mastery` | Maîtrise de la Lance : Maîtrise de la Lance Dragon (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_enriched_blood` | Sang Vital : Sang Enrichi (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_eternal_blood` | Régénération Cellulaire : Sang Éternel (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_eternal_fortress` | Posture Solide : Forteresse Éternelle (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_eternal_life` | Force Vitale : Vie Éternelle (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_eternity_resolve` | Résolution de Fer : Résolution Éternelle (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_expanded_mind` | Esprit Éveillé : Esprit Élargi (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_expert_parry` | Parade : Parade Experte (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_fate_vision` | Instinct de Survie : Vision du Destin (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_fated_omen` | Bon Présage : Présage du Destin (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_fly` | Vol (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_fury_strike` | Frappe Instinctive : Frappe de Fureur (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_gale_step` | Pas de Vent : Pas de Bourrasque (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_genesis_blood` | Régénération Cellulaire : Sang de la Genèse (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_genesis_bulwark` | Rempart : Rempart de la Genèse (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_genesis_edge` | Entaille Profonde : Tranchant de la Genèse (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_genesis_strike` | Frappe Chanceuse : Frappe de la Genèse (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_genius_mind` | Esprit Éveillé : Esprit de Génie (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_god_constitution` | Constitution : Constitution Divine (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_god_resolve` | Résolution de Fer : Résolution Divine (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_good_omen` | Bon Présage (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_gravity_defiance` | Souplesse : Défi à la Gravité (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_great_axe_mastery` | Maîtrise de la Hache : Maîtrise de la Grande Hache (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_halberd_mastery` | Maîtrise de la Lance : Maîtrise de la Hallebarde (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_haunting_thrust` | Traque : Estocade Obsédante (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_heart_of_diamond` | Cœur de Chêne : Coeur de Diamant (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_heart_of_eternity` | Cœur de Chêne : Coeur d'Éternité (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_heart_of_genesis` | Cœur de Chêne : Coeur de la Genèse (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_heart_of_iron` | Cœur de Chêne : Coeur de Fer (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_heart_of_oak` | Cœur de Chêne : Coeur de Chêne (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_heart_of_steel` | Cœur de Chêne : Coeur d'Acier (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_hunter_guard` | Garde du Chasseur : Garde de Chasseur (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_hydra_blood` | Régénération Cellulaire : Sang d'Hydre (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_immortal_absolute` | Ténacité : Immortel Absolu (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_immortal_resolve` | Résolution de Fer : Résolution Immortelle (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_infinite_life` | Force Vitale : Vie Infinie (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_infinite_mind` | Esprit Éveillé : Esprit Infini (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_infinite_vampirism` | Siphon Spirituel : Vampirisme Infini (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_instinctive_strike` | Frappe Instinctive (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_invincible` | Ténacité : Invincible (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_iron_body` | Corps Robuste : Corps de Fer (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_iron_resolve` | Résolution de Fer (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_iron_skin` | Peau de Pierre : Peau de Fer (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_iron_will` | Nerfs d'Acier : Volonté de Fer (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_legend_hunter` | Chasseur de Trésors : Chasseur de Légendes (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_legendary_axe_mastery` | Maîtrise de la Hache : Maîtrise Légendaire de la Hache (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_legendary_dagger_mastery` | Maîtrise de la Dague : Maîtrise Légendaire de la Dague (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_legendary_sniper_mastery` | Maîtrise de l'Arc : Maîtrise Légendaire du Tir (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_life_force` | Force Vitale (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_living_fortress` | Posture Solide : Forteresse Vivante (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_lucky_strike` | Frappe Chanceuse (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_lungs_of_steel` | Souffle Inépuisable : Poumons d'Acier (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_mana_barrier` | Barrière de Mana (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_mana_citadel` | Barrière de Mana : Citadelle de Mana (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_mana_font` | Source de Mana (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_mana_fortress` | Barrière de Mana : Forteresse de Mana (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_mana_infinity` | Source de Mana : Mana Infini (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_mana_ocean` | Source de Mana : Océan de Mana (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_mana_river` | Source de Mana : Rivière de Mana (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_mana_stream` | Source de Mana : Flux de Mana (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_marksman_mastery` | Maîtrise de l'Arc : Maîtrise du Tir (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_master_parry` | Parade : Parade de Maître (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_minor_parry` | Parade : Parade Mineure (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_minor_sword_mastery` | Maîtrise de l'Épée : Initiation à l'Épée (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_miracle_dodge` | Esquive : Esquive Miraculeuse (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_monster_slayer_guard` | Garde du Chasseur : Garde du Tueur (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_mystic_veil` | Voile Mystique (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_myth_hunter` | Chasseur de Trésors : Chasseur de Mythes (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_natural_recovery` | Rétablissement Naturel (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_night_vision` | Vision Nocturne (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_obsidian_skin` | Peau de Pierre : Peau d'Obsidienne (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_opportunist_strike` | Frappe Opportuniste (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_overflowing_life` | Force Vitale : Vie Débordante (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_overflowing_power` | Puissance Débordante (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_peche_avarice` | Avarice | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `skill_page_peche_colere` | Colère | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `skill_page_peche_envie` | Envie | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `skill_page_peche_gourmandise` | Gourmandise | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `skill_page_peche_luxure` | Luxure | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `skill_page_peche_orgueil` | Orgueil | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `skill_page_peche_paresse` | Paresse | Divin |  | 1 500 000 | 1 impériale 5 astrales | fixé | E |
| `skill_page_perfect_parry` | Parade : Parade Parfaite (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_perseverance` | Persévérance (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_phantom_dodge` | Esquive : Esquive Fantôme (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_phantom_strike` | Frappe Instinctive : Frappe Fantôme (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_phoenix_blood` | Régénération Cellulaire : Sang de Phénix (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_predatory_strike` | Frappe Instinctive : Frappe de Prédateur (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_pressure_point` | Point de Pression (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_prophecy_omen` | Bon Présage : Présage Prophétique (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_pure_magic` | Puissance Débordante : Magie Pure (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_rage_frenzy` | Vigueur Combative : Frénésie Enragée (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_reactive_bulwark` | Rempart : Rempart Réactif (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_reality_dodge` | Esquive : Esquive de la Réalité (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_relentless_hunt` | Traque : Traque Implacable (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_relic_hunter` | Chasseur de Trésors : Chasseur de Reliques (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_robust_constitution` | Constitution : Constitution Robuste (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_second_wind` | Second Souffle (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_seismic_strike` | Frappe Instinctive : Frappe Sismique (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_shadow_blade_mastery` | Maîtrise de la Dague : Maîtrise de la Lame d'Ombre (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_shadow_dodge` | Esquive : Esquive de l'Ombre (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_sharp_blade` | Entaille Profonde : Lame Aiguisée (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_sniper_mastery` | Maîtrise de l'Arc : Maîtrise du Sniper (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_solid_stance` | Posture Solide (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_soul_crushing_pressure` | Point de Pression : Pression Brise-Âme (Unique) | Unique |  | 30 000 | 3 zéniths | fixé | E |
| `skill_page_soul_stealer` | Siphon Spirituel : Voleur d'Âmes (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_spear_mastery` | Maîtrise de la Lance (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_spellblade` | Frappe Arcanique : Lame Ensorcelée (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_spirit_drain` | Siphon Spirituel : Drain Spirituel (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_spiritual_siphon` | Siphon Spirituel (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_steel_body` | Corps Robuste : Corps d'Acier (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_steel_constitution` | Constitution : Constitution d'Acier (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_steel_nerves` | Nerfs d'Acier (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_steel_resolve` | Résolution de Fer : Résolution d'Acier (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_steel_skin` | Peau de Pierre : Peau d'Acier (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_stone_skin` | Peau de Pierre (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_storm_step` | Pas de Vent : Pas de Tempête (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_sturdy_body` | Corps Robuste (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_suppleness` | Souplesse : Souplesse d'Eldanior (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_survival_instinct` | Instinct de Survie (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_survivor_spirit` | Second Souffle : Esprit du Survivant (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_sword_mastery` | Maîtrise de l'Épée (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_tenacity` | Ténacité (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_tenacity adaptive_shield pain_tolerance hardening steel_nerves robust_constitution life_force vital_blood heart_of_oak perseverance` | server.items.skill_page_tenacity adaptive_shield pain_tolerance hardening steel_nerves robust_constitution life_force vital_blood heart_of_oak perseverance.name | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_time_dodge` | Esquive : Esquive Temporelle (Légendaire) | Légendaire |  | 150 000 | 1 astrale 5 zéniths | fixé | E |
| `skill_page_tireless_breath` | Souffle Inépuisable (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_titan_constitution` | Constitution : Constitution de Titan (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_titan_lungs` | Souffle Inépuisable : Poumons de Titan (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_titan_resolve` | Résolution de Fer : Résolution de Titan (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_treasure_hunter` | Chasseur de Trésors (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_troll_blood` | Régénération Cellulaire : Sang de Troll (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_unbreakable` | Ténacité : Incassable (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_undying` | Persévérance : Immortel (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_unleashed_magic` | Puissance Débordante : Magie Déchaînée (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_unmovable_mountain` | Posture Solide : Montagne Inébranlable (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_unyielding` | Ténacité : Inflexible (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_vital_blood` | Sang Vital (Commun) | Commun |  | 300 | 3 or | fixé | E |
| `skill_page_vital_pressure` | Point de Pression : Pression Vitale (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_vital_recovery` | Rétablissement Naturel : Rétablissement Vital (Inhabituel) | Peu commun |  | 800 | 8 or | fixé | E |
| `skill_page_void_blade` | Entaille Profonde : Lame du Néant (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_void_step` | Pas de Vent : Pas du Néant (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_war_axe_mastery` | Maîtrise de la Hache : Maîtrise de la Hache de Guerre (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_war_frenzy` | Vigueur Combative : Frénésie Guerrière (Rare) | Rare |  | 2 500 | 2 diamants 5 or | fixé | E |
| `skill_page_war_prophecy` | Instinct de Survie : Prophétie de Guerre (Épique) | Épique |  | 8 000 | 8 diamants | fixé | E |
| `skill_page_wind_step` | Pas de Vent (Commun) | Commun |  | 300 | 3 or | fixé | E |

### Avec recette — prix calculés (8)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O  Recette |
|---|---|---|---:|---:|---|---|---|---|
| `Rune_Aoe_Up` | Gigantisme | Peu commun | 20 | 160 | 1 or 6 argent | calculé | V | 1× Fragment de pierre runique · GoblinLab_Workbench |
| `Rune_Duration_Up` | Endurance | Peu commun | 20 | 160 | 1 or 6 argent | calculé | V | 1× Fragment de pierre runique · GoblinLab_Workbench |
| `Rune_Fork` | Fragmentation | Peu commun | 20 | 160 | 1 or 6 argent | calculé | V | 1× Fragment de pierre runique · GoblinLab_Workbench |
| `Rune_Quickening` | Célérité | Peu commun | 20 | 160 | 1 or 6 argent | calculé | V | 1× Fragment de pierre runique · GoblinLab_Workbench |
| `Rune_Rend` | Lacération | Peu commun | 20 | 160 | 1 or 6 argent | calculé | V | 1× Fragment de pierre runique · GoblinLab_Workbench |
| `Rune_Ricochet` | Rebond | Peu commun | 20 | 160 | 1 or 6 argent | calculé | V | 1× Fragment de pierre runique · GoblinLab_Workbench |
| `Rune_Thrift` | Économie | Peu commun | 20 | 160 | 1 or 6 argent | calculé | V | 1× Fragment de pierre runique · GoblinLab_Workbench |
| `Rune_Wither` | Déclin | Peu commun | 20 | 160 | 1 or 6 argent | calculé | V | 1× Fragment de pierre runique · GoblinLab_Workbench |

<a id="fam-construction"></a>
## Construction

### Sans recette — prix fixés (191)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Cloth_Modern_Blue_Roof` | Toit moderne incliné en tissu bleu |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Blue_Roof_Flat` | Toit moderne horizontal en tissu bleu |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Blue_Roof_Shallow` | Toit moderne en tissu bleu : faible pente |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Blue_Roof_Steep` | Toit moderne en tissu bleu : forte pente |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Blue_Roof_Vertical` | Toit moderne vertical en tissu bleu |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Blue_Roof_Vertical_Flap` | Auvent moderne en tissu bleu |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_DarkGreen_Roof` | Toit moderne incliné en tissu vert foncé |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_DarkGreen_Roof_Flat` | Toit moderne horizontal en tissu vert foncé |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_DarkGreen_Roof_Shallow` | Toit moderne en tissu vert foncé : faible pente |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_DarkGreen_Roof_Steep` | Toit moderne en tissu vert foncé : forte pente |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_DarkGreen_Roof_Vertical` | Toit moderne vertical en tissu vert foncé |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_DarkGreen_Roof_Vertical_Flap` | Auvent moderne en tissu vert foncé |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Orange_Roof` | Toit moderne incliné en tissu orange |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Orange_Roof_Flat` | Toit moderne horizontal en tissu orange |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Orange_Roof_Shallow` | Toit moderne en tissu orange : faible pente |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Orange_Roof_Steep` | Toit moderne en tissu orange : forte pente |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Orange_Roof_Vertical` | Toit moderne vertical en tissu orange |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Orange_Roof_Vertical_Flap` | Auvent moderne en tissu orange |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Red_Roof` | Toit moderne incliné en tissu rouge |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Red_Roof_Flat` | Toit moderne horizontal en tissu rouge |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Red_Roof_Shallow` | Toit moderne en tissu rouge : faible pente |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Red_Roof_Steep` | Toit moderne en tissu rouge : forte pente |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Red_Roof_Vertical` | Toit moderne vertical en tissu rouge |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Red_Roof_Vertical_Flap` | Auvent moderne en tissu rouge |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Yellow_Roof` | Toit moderne incliné en tissu jaune |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Yellow_Roof_Flat` | Toit moderne horizontal en tissu jaune |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Yellow_Roof_Shallow` | Toit moderne en tissu jaune : faible pente |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Yellow_Roof_Steep` | Toit moderne en tissu jaune : forte pente |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Yellow_Roof_Vertical` | Toit moderne vertical en tissu jaune |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Modern_Yellow_Roof_Vertical_Flap` | Auvent moderne en tissu jaune |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Roof_Hide` | Toit incliné en cuir brut |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Roof_Hide_Flap` | Toit déchiré en cuir brut |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Roof_Hide_Flat` | Toit horizontal en cuir brut |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Roof_Hide_Shallow` | Tente en cuir brut : faible pente |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Roof_Hide_Steep` | Tente en cuir brut : forte pente |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Roof_Hide_Vertical` | Toit vertical en cuir brut |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Roof_Leather` | Toit incliné en cuir |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Roof_Leather_Flap` | Toit déchiré en cuir |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Roof_Leather_Flat` | Toit horizontal en cuir |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Roof_Leather_Shallow` | Tente en cuir : faible pente |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Roof_Leather_Steep` | Tente en cuir : forte pente |  |  | 50 | 5 argent | fixé | V |
| `Cloth_Roof_Leather_Vertical` | Toit vertical en cuir |  |  | 50 | 5 argent | fixé | V |
| `Metal_Bronze` | Bronze - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Bronze_Decorative` | Bloc de bronze décoratif - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Bronze_Fence` | Clôture en bronze - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Bronze_Half` | Dalle en bronze - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Bronze_Ornate` | Bloc de bronze orné - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Bronze_Pipe_Chimney` | Tuyau en bronze : cheminée - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Bronze_Pipe_Corner` | Tuyau en bronze : angle - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Bronze_Pipe_Large` | Tuyau en bronze : grand - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Bronze_Pipe_Large_Corner` | Tuyau en bronze : grand angle - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Bronze_Pipe_Large_Mouthpiece` | Tuyau en bronze : grande ouverture - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Bronze_Pipe_Long` | Tuyau en bronze : long - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Bronze_Pipe_Short` | Tuyau en bronze : court - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Bronze_Roof` | Toit en bronze incliné - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Bronze_Roof_Flat` | Toit en bronze : plat - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Bronze_Roof_Shallow` | Toit en bronze : faible pente - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Bronze_Roof_Steep` | Toit en bronze : forte pente - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Bronze_Smooth` | Bloc de bronze lisse - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Bronze_Smooth_Half` | Dalle en bronze lisse - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Bronze_Stairs` | Escalier en bronze - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Copper` | Cuivre - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Copper_Decorative` | Bloc de cuivre décoratif - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Copper_Fence` | Clôture en cuivre - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Copper_Half` | Dalle en cuivre - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Copper_Ornate` | Bloc de cuivre orné - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Copper_Pipe_Chimney` | Tuyau en cuivre : cheminée - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Copper_Pipe_Corner` | Tuyau en cuivre : angle - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Copper_Pipe_Large` | Tuyau en cuivre : grand - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Copper_Pipe_Large_Corner` | Tuyau en cuivre : grand angle - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Copper_Pipe_Large_Mouthpiece` | Tuyau en cuivre : grande ouverture - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Copper_Pipe_Long` | Tuyau en cuivre : long - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Copper_Pipe_Short` | Tuyau en cuivre : court - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Copper_Roof` | Toit en cuivre incliné - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Copper_Roof_Flat` | Toit en cuivre : plat - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Copper_Roof_Shallow` | Toit en cuivre : faible pente - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Copper_Roof_Steep` | Toit en cuivre : forte pente - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Copper_Smooth` | Bloc de cuivre lisse - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Copper_Smooth_Half` | Dalle de cuivre lisse - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Copper_Stairs` | Escalier en cuivre - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron` | Fer gobelin | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Goblin_Iron_Bars` | Barres de fer gobelin |  |  | 50 | 5 argent | fixé | V |
| `Metal_Goblin_Iron_Bars_Corner` | Barres de fer gobelin en angle |  |  | 50 | 5 argent | fixé | V |
| `Metal_Goblin_Iron_Bars_Platforms` | Barres de fer gobelin horizontales |  |  | 50 | 5 argent | fixé | V |
| `Metal_Goblin_Iron_Chain_Small` | Petite chaîne en fer gobelin |  |  | 50 | 5 argent | fixé | V |
| `Metal_Goblin_Iron_Chains` | Chaîne en fer gobelin diagonale |  |  | 50 | 5 argent | fixé | V |
| `Metal_Goblin_Iron_Chains_Vertical` | Chaîne en fer gobelin |  |  | 50 | 5 argent | fixé | V |
| `Metal_Goblin_Iron_Decorative` | Bloc de fer gobelin décoratif | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Goblin_Iron_Fence` | Clôture en fer gobelin | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Half` | Dalle en fer gobelin | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Ornate` | Bloc de fer gobelin orné | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Pipe_Chimney` | Tuyau en fer gobelin : cheminée | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Goblin_Iron_Pipe_Corner` | Tuyau en fer gobelin : angle | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Goblin_Iron_Pipe_Glowy` | Tuyau en fer gobelin lumineux | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Pipe_Large` | Tuyau en fer gobelin : grand | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Pipe_Large_Corner` | Tuyau en fer gobelin : grand angle | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Pipe_Large_Glowy` | Tuyau en fer gobelin lumineux : grand | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Pipe_Large_Glowy_Corner` | Tuyau en fer gobelin lumineux : grand angle | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Pipe_Large_Glowy_Mouthpiece` | Tuyau en fer gobelin lumineux : grande ouverture | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Pipe_Large_Mouthpiece` | Tuyau en fer gobelin : grande ouverture | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Pipe_Long` | Tuyau en fer gobelin : long | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Goblin_Iron_Pipe_Short` | Tuyau en fer gobelin : court | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Goblin_Iron_Roof` | Toit en fer gobelin incliné | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Roof_Flat` | Toit en fer gobelin : plat | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Roof_Shallow` | Toit en fer gobelin : faible pente | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Roof_Steep` | Toit en fer gobelin : forte pente | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Smooth` | Bloc de fer gobelin lisse | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Smooth_Half` | Dalle de fer gobelin lisse | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Goblin_Iron_Stack` | Pile de barres de fer gobelin |  |  | 50 | 5 argent | fixé | V |
| `Metal_Goblin_Iron_Stairs` | Escalier en fer gobelin | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Gold_Pipe_Chimney` | Tuyau en or : cheminée | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Gold_Pipe_Corner` | Tuyau en or : angle | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Gold_Pipe_Large` | Tuyau en or : grand | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Gold_Pipe_Large_Corner` | Tuyau en or : grand angle | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Gold_Pipe_Large_Mouthpiece` | Tuyau en or : grande ouverture | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Gold_Pipe_Long` | Tuyau en or : long | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Gold_Pipe_Short` | Tuyau en or : court | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Iron` | Fer - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Iron_Decorative` | Bloc de fer décoratif - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Iron_Fence` | Clôture en fer - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Iron_Half` | Dalle en fer - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Iron_Ornate` | Bloc de fer orné - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Iron_Pipe_Chimney` | Tuyau en fer : cheminée - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Iron_Pipe_Corner` | Tuyau en fer : angle - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Iron_Pipe_Large` | Tuyau en fer : grand - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Iron_Pipe_Large_Corner` | Tuyau en fer : grand angle - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Iron_Pipe_Large_Mouthpiece` | Tuyau en fer : grande ouverture - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Iron_Pipe_Long` | Tuyau en fer : long - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Iron_Pipe_Short` | Tuyau en fer : court - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Iron_Roof` | Toit en fer incliné - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Iron_Roof_Flat` | Toit en fer : plat - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Iron_Roof_Shallow` | Toit en fer : faible pente - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Iron_Roof_Steep` | Toit en fer : forte pente - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Iron_Smooth` | Bloc de fer lisse - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Iron_Smooth_Half` | Dalle de fer lisse - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Iron_Stairs` | Escalier en fer - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Copper` | Cuivre embrasé | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Orbis_Copper_Decorative` | Bloc de cuivre embrasé décoratif | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Orbis_Copper_Half` | Dalle en cuivre embrasé | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Copper_Ornate` | Bloc de cuivre embrasé orné | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Copper_Pipe_Chimney` | Tuyau en cuivre embrasé : cheminée | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Orbis_Copper_Pipe_Corner` | Tuyau en cuivre embrasé : angle | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Orbis_Copper_Pipe_Large` | Tuyau en cuivre embrasé : grand | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Copper_Pipe_Large_Corner` | Tuyau en cuivre embrasé : grand angle | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Copper_Pipe_Large_Mouthpiece` | Tuyau en cuivre embrasé : grande ouverture | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Copper_Pipe_Long` | Tuyau en cuivre embrasé : long | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Orbis_Copper_Pipe_Short` | Tuyau en cuivre embrasé : court | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Orbis_Copper_Roof` | Toit en cuivre embrasé incliné | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Copper_Roof_Flat` | Toit en cuivre embrasé : plat | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Copper_Roof_Shallow` | Toit en cuivre embrasé : faible pente | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Copper_Roof_Steep` | Toit en cuivre embrasé : forte pente | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Copper_Smooth` | Bloc de cuivre embrasé lisse | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Orbis_Copper_Smooth_Half` | Dalle de cuivre embrasé lisse | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Copper_Stairs` | Escalier en cuivre embrasé | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Iron` | Fer embrasé | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Orbis_Iron_Decorative` | Bloc de fer embrasé décoratif | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Orbis_Iron_Half` | Dalle en fer embrasé | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Iron_Ornate` | Bloc de fer embrasé orné | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Iron_Pipe_Chimney` | Tuyau en fer embrasé : cheminée | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Orbis_Iron_Pipe_Corner` | Tuyau en fer embrasé : angle | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Orbis_Iron_Pipe_Large` | Tuyau en fer embrasé : grand | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Iron_Pipe_Large_Corner` | Tuyau en fer embrasé : grand angle | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Iron_Pipe_Large_Mouthpiece` | Tuyau en fer embrasé : grande ouverture | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Iron_Pipe_Long` | Tuyau en fer embrasé : long | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Orbis_Iron_Pipe_Short` | Tuyau en fer embrasé : court | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Orbis_Iron_Roof` | Toit en fer embrasé incliné | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Iron_Roof_Flat` | Toit en fer embrasé : plat | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Iron_Roof_Shallow` | Toit en fer embrasé : faible pente | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Iron_Roof_Steep` | Toit en fer embrasé : forte pente | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Iron_Smooth` | Bloc de fer embrasé lisse | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Iron_Smooth_Half` | Dalle de fer embrasé lisse | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Orbis_Iron_Stairs` | Escalier en fer embrasé | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Zinc` | Zinc - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Zinc_Decorative` | Bloc de zinc décoratif - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Zinc_Fence` | Clôture en zinc - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Zinc_Half` | Dalle en zinc - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Zinc_Ornate` | Bloc de zinc orné - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Zinc_Pipe_Chimney` | Tuyau en zinc : cheminée - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Zinc_Pipe_Corner` | Tuyau en zinc : angle - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Zinc_Pipe_Large` | Tuyau en zinc : grand - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Zinc_Pipe_Large_Corner` | Tuyau en zinc : grand angle - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Zinc_Pipe_Large_Mouthpiece` | Tuyau en zinc : grande ouverture - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Zinc_Pipe_Long` | Tuyau en zinc : long - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Zinc_Pipe_Short` | Tuyau en zinc : court - Cybercity | Commun | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Metal_Zinc_Roof` | Toit en zinc incliné - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Zinc_Roof_Flat` | Toit en zinc : plat - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Zinc_Roof_Shallow` | Toit en zinc : faible pente - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Zinc_Roof_Steep` | Toit en zinc : forte pente - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Zinc_Smooth` | Bloc de zinc lisse - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Zinc_Smooth_Half` | Dalle de zinc lisse - Cybercity |  | 15 | 57 | 5 argent 7 cuivres | fixé | V |
| `Metal_Zinc_Stairs` | Escalier en zinc - Cybercity | Commun | 15 | 57 | 5 argent 7 cuivres | fixé | V |

### Avec recette — prix calculés (98)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O  Recette |
|---|---|---|---:|---:|---|---|---|---|
| `Cloth_Block_Wool_Black` | Tissu noir |  | 4 | 40 | 4 argent | calculé | V | 1× Pétales noirs + 1× Tissu blanc · Furniture_Bench |
| `Cloth_Block_Wool_Black_Half` | Dalle en tissu noir |  |  | 26 | 2 argent 6 cuivres | calculé | V | 1× Tissu noir → ×2 · Builders |
| `Cloth_Block_Wool_Black_Stairs` | Escalier en tissu noir |  |  | 52 | 5 argent 2 cuivres | calculé | V | 1× Tissu noir · Builders |
| `Cloth_Block_Wool_Blue` | Tissu bleu |  | 6 | 29 | 2 argent 9 cuivres | calculé | V | 1× Pétales bleus + 1× Tissu blanc · Furniture_Bench |
| `Cloth_Block_Wool_Blue_Half` | Dalle en tissu bleu |  |  | 19 | 1 argent 9 cuivres | calculé | V | 1× Tissu bleu → ×2 · Builders |
| `Cloth_Block_Wool_Blue_Light` | Tissu bleu clair |  | 7 | 51 | 5 argent 1 cuivre | calculé | V | 1× Tissu bleu + 1× Pétales blancs · Furniture_Bench |
| `Cloth_Block_Wool_Blue_Light_Half` | Dalle en tissu bleu clair |  |  | 33 | 3 argent 3 cuivres | calculé | V | 1× Tissu bleu clair → ×2 · Builders |
| `Cloth_Block_Wool_Blue_Light_Stairs` | Escalier en tissu bleu clair |  |  | 66 | 6 argent 6 cuivres | calculé | V | 1× Tissu bleu clair · Builders |
| `Cloth_Block_Wool_Blue_Stairs` | Escalier en tissu bleu |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu bleu · Builders |
| `Cloth_Block_Wool_Cyan` | Tissu cyan |  | 8 | 29 | 2 argent 9 cuivres | calculé | V | 1× Pétales cyan + 1× Tissu blanc · Furniture_Bench |
| `Cloth_Block_Wool_Cyan_Half` | Dalle en tissu cyan |  |  | 19 | 1 argent 9 cuivres | calculé | V | 1× Tissu cyan → ×2 · Builders |
| `Cloth_Block_Wool_Cyan_Light` | Tissu cyan clair |  | 9 | 51 | 5 argent 1 cuivre | calculé | V | 1× Tissu cyan + 1× Pétales blancs · Furniture_Bench |
| `Cloth_Block_Wool_Cyan_Light_Half` | Dalle en tissu cyan clair |  |  | 33 | 3 argent 3 cuivres | calculé | V | 1× Tissu cyan clair → ×2 · Builders |
| `Cloth_Block_Wool_Cyan_Light_Stairs` | Escalier en tissu cyan clair |  |  | 66 | 6 argent 6 cuivres | calculé | V | 1× Tissu cyan clair · Builders |
| `Cloth_Block_Wool_Cyan_Stairs` | Escalier en tissu cyan |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu cyan · Builders |
| `Cloth_Block_Wool_Gray` | Tissu gris |  | 5 | 65 | 6 argent 5 cuivres | calculé | V | 1× Tissu noir + 1× Pétales blancs · Furniture_Bench |
| `Cloth_Block_Wool_Gray_Half` | Dalle en tissu gris |  |  | 42 | 4 argent 2 cuivres | calculé | V | 1× Tissu gris → ×2 · Builders |
| `Cloth_Block_Wool_Gray_Light` | Tissu gris clair |  | 3 | 98 | 9 argent 8 cuivres | calculé | V | 1× Tissu gris + 1× Pétales blancs · Furniture_Bench |
| `Cloth_Block_Wool_Gray_Light_Half` | Dalle en tissu gris clair |  |  | 64 | 6 argent 4 cuivres | calculé | V | 1× Tissu gris clair → ×2 · Builders |
| `Cloth_Block_Wool_Gray_Light_Stairs` | Escalier en tissu gris clair |  |  | 130 | 1 or 3 argent | calculé | V | 1× Tissu gris clair · Builders |
| `Cloth_Block_Wool_Gray_Stairs` | Escalier en tissu gris |  |  | 84 | 8 argent 4 cuivres | calculé | V | 1× Tissu gris · Builders |
| `Cloth_Block_Wool_Green` | Tissu vert |  | 10 | 29 | 2 argent 9 cuivres | calculé | V | 1× Pétales verts + 1× Tissu blanc · Furniture_Bench |
| `Cloth_Block_Wool_Green_Half` | Dalle en tissu vert |  |  | 19 | 1 argent 9 cuivres | calculé | V | 1× Tissu vert → ×2 · Builders |
| `Cloth_Block_Wool_Green_Light` | Tissu vert clair |  | 11 | 51 | 5 argent 1 cuivre | calculé | V | 1× Tissu vert + 1× Pétales blancs · Furniture_Bench |
| `Cloth_Block_Wool_Green_Light_Half` | Dalle en tissu vert clair |  |  | 33 | 3 argent 3 cuivres | calculé | V | 1× Tissu vert clair → ×2 · Builders |
| `Cloth_Block_Wool_Green_Light_Stairs` | Escalier en tissu vert clair |  |  | 66 | 6 argent 6 cuivres | calculé | V | 1× Tissu vert clair · Builders |
| `Cloth_Block_Wool_Green_Stairs` | Escalier en tissu vert |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu vert · Builders |
| `Cloth_Block_Wool_Orange` | Tissu orange |  | 12 | 29 | 2 argent 9 cuivres | calculé | V | 1× Pétales orange + 1× Tissu blanc · Furniture_Bench |
| `Cloth_Block_Wool_Orange_Half` | Dalle en tissu orange |  |  | 19 | 1 argent 9 cuivres | calculé | V | 1× Tissu orange → ×2 · Builders |
| `Cloth_Block_Wool_Orange_Light` | Tissu orange clair |  | 13 | 51 | 5 argent 1 cuivre | calculé | V | 1× Tissu orange + 1× Pétales blancs · Furniture_Bench |
| `Cloth_Block_Wool_Orange_Light_Half` | Dalle en tissu orange clair |  |  | 33 | 3 argent 3 cuivres | calculé | V | 1× Tissu orange clair → ×2 · Builders |
| `Cloth_Block_Wool_Orange_Light_Stairs` | Escalier en tissu orange clair |  |  | 66 | 6 argent 6 cuivres | calculé | V | 1× Tissu orange clair · Builders |
| `Cloth_Block_Wool_Orange_Stairs` | Escalier en tissu orange |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu orange · Builders |
| `Cloth_Block_Wool_Pink` | Tissu rose |  | 14 | 29 | 2 argent 9 cuivres | calculé | V | 1× Pétales roses + 1× Tissu blanc · Furniture_Bench |
| `Cloth_Block_Wool_Pink_Half` | Dalle en tissu rose |  |  | 19 | 1 argent 9 cuivres | calculé | V | 1× Tissu rose → ×2 · Builders |
| `Cloth_Block_Wool_Pink_Light` | Tissu rose clair |  | 15 | 51 | 5 argent 1 cuivre | calculé | V | 1× Tissu rose + 1× Pétales blancs · Furniture_Bench |
| `Cloth_Block_Wool_Pink_Light_Half` | Dalle en tissu rose clair |  |  | 33 | 3 argent 3 cuivres | calculé | V | 1× Tissu rose clair → ×2 · Builders |
| `Cloth_Block_Wool_Pink_Light_Stairs` | Escalier en tissu rose clair |  |  | 66 | 6 argent 6 cuivres | calculé | V | 1× Tissu rose clair · Builders |
| `Cloth_Block_Wool_Pink_Stairs` | Escalier en tissu rose |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu rose · Builders |
| `Cloth_Block_Wool_Purple` | Tissu violet |  | 16 | 29 | 2 argent 9 cuivres | calculé | V | 1× Pétales violets + 1× Tissu blanc · Furniture_Bench |
| `Cloth_Block_Wool_Purple_Half` | Dalle en tissu violet |  |  | 19 | 1 argent 9 cuivres | calculé | V | 1× Tissu violet → ×2 · Builders |
| `Cloth_Block_Wool_Purple_Light` | Tissu violet clair |  | 17 | 51 | 5 argent 1 cuivre | calculé | V | 1× Tissu violet + 1× Pétales blancs · Furniture_Bench |
| `Cloth_Block_Wool_Purple_Light_Half` | Dalle en tissu violet clair |  |  | 33 | 3 argent 3 cuivres | calculé | V | 1× Tissu violet clair → ×2 · Builders |
| `Cloth_Block_Wool_Purple_Light_Stairs` | Escalier en tissu violet clair |  |  | 66 | 6 argent 6 cuivres | calculé | V | 1× Tissu violet clair · Builders |
| `Cloth_Block_Wool_Purple_Stairs` | Escalier en tissu violet |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu violet · Builders |
| `Cloth_Block_Wool_Red` | Tissu rouge |  | 18 | 29 | 2 argent 9 cuivres | calculé | V | 1× Pétales rouges + 1× Tissu blanc · Furniture_Bench |
| `Cloth_Block_Wool_Red_Half` | Dalle en tissu rouge |  |  | 19 | 1 argent 9 cuivres | calculé | V | 1× Tissu rouge → ×2 · Builders |
| `Cloth_Block_Wool_Red_Light` | Tissu rouge clair |  | 19 | 51 | 5 argent 1 cuivre | calculé | V | 1× Tissu rouge + 1× Pétales blancs · Furniture_Bench |
| `Cloth_Block_Wool_Red_Light_Half` | Dalle en tissu rouge clair |  |  | 33 | 3 argent 3 cuivres | calculé | V | 1× Tissu rouge clair → ×2 · Builders |
| `Cloth_Block_Wool_Red_Light_Stairs` | Escalier en tissu rouge clair |  |  | 66 | 6 argent 6 cuivres | calculé | V | 1× Tissu rouge clair · Builders |
| `Cloth_Block_Wool_Red_Stairs` | Escalier en tissu rouge |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu rouge · Builders |
| `Cloth_Block_Wool_White` | Tissu blanc |  | 2 | 5 | 5 cuivres | calculé | V | 1× Rouleau de laine · Furniture_Bench |
| `Cloth_Block_Wool_White_Half` | Dalle en tissu blanc |  |  | 3 | 3 cuivres | calculé | V | 1× Tissu blanc → ×2 · Builders |
| `Cloth_Block_Wool_White_Stairs` | Escalier en tissu blanc |  |  | 6 | 6 cuivres | calculé | V | 1× Tissu blanc · Builders |
| `Cloth_Block_Wool_Yellow` | Tissu jaune |  | 20 | 13 | 1 argent 3 cuivres | calculé | V | 1× Pétales jaunes + 1× Tissu blanc · Furniture_Bench |
| `Cloth_Block_Wool_Yellow_Half` | Dalle en tissu jaune |  |  | 8 | 8 cuivres | calculé | V | 1× Tissu jaune → ×2 · Builders |
| `Cloth_Block_Wool_Yellow_Light` | Tissu jaune clair |  | 21 | 30 | 3 argent | calculé | V | 1× Tissu jaune + 1× Pétales blancs · Furniture_Bench |
| `Cloth_Block_Wool_Yellow_Light_Half` | Dalle en tissu jaune clair |  |  | 20 | 2 argent | calculé | V | 1× Tissu jaune clair → ×2 · Builders |
| `Cloth_Block_Wool_Yellow_Light_Stairs` | Escalier en tissu jaune clair |  |  | 39 | 3 argent 9 cuivres | calculé | V | 1× Tissu jaune clair · Builders |
| `Cloth_Block_Wool_Yellow_Stairs` | Escalier en tissu jaune |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× Tissu jaune · Builders |
| `Cloth_Roof_Blue` | Toit incliné en tissu bleu |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu bleu · Builders |
| `Cloth_Roof_Blue_Flap` | Auvent en tissu bleu |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu bleu · Builders |
| `Cloth_Roof_Blue_Flat` | Toit horizontal en tissu bleu |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu bleu · Builders |
| `Cloth_Roof_Blue_Shallow` | Toit en tissu bleu : faible pente |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu bleu · Builders |
| `Cloth_Roof_Blue_Steep` | Toit en tissu bleu : forte pente |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu bleu · Builders |
| `Cloth_Roof_Blue_Vertical` | Toit vertical en tissu bleu |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu bleu · Builders |
| `Cloth_Roof_Green` | Toit incliné en tissu vert |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu vert · Builders |
| `Cloth_Roof_Green_Flap` | Auvent en tissu vert |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu vert · Builders |
| `Cloth_Roof_Green_Flat` | Toit horizontal en tissu vert |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu vert · Builders |
| `Cloth_Roof_Green_Shallow` | Toit en tissu vert : faible pente |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu vert · Builders |
| `Cloth_Roof_Green_Steep` | Toit en tissu vert : forte pente |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu vert · Builders |
| `Cloth_Roof_Green_Vertical` | Toit vertical en tissu vert |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu vert · Builders |
| `Cloth_Roof_Orange` | Toit incliné en tissu orange |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu orange · Builders |
| `Cloth_Roof_Orange_Flap` | Auvent en tissu orange |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu orange · Builders |
| `Cloth_Roof_Orange_Flat` | Toit horizontal en tissu orange |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu orange · Builders |
| `Cloth_Roof_Orange_Shallow` | Toit en tissu orange : faible pente |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu orange · Builders |
| `Cloth_Roof_Orange_Steep` | Toit en tissu orange : forte pente |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu orange · Builders |
| `Cloth_Roof_Orange_Vertical` | Toit vertical en tissu orange |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu orange · Builders |
| `Cloth_Roof_Red` | Toit incliné en tissu rouge |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu rouge · Builders |
| `Cloth_Roof_Red_Flap` | Auvent en tissu rouge |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu rouge · Builders |
| `Cloth_Roof_Red_Flat` | Toit horizontal en tissu rouge |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu rouge · Builders |
| `Cloth_Roof_Red_Shallow` | Toit en tissu rouge : faible pente |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu rouge · Builders |
| `Cloth_Roof_Red_Steep` | Toit en tissu rouge : forte pente |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu rouge · Builders |
| `Cloth_Roof_Red_Vertical` | Toit vertical en tissu rouge |  |  | 38 | 3 argent 8 cuivres | calculé | V | 1× Tissu rouge · Builders |
| `Cloth_Roof_White` | Toit incliné en tissu blanc |  |  | 6 | 6 cuivres | calculé | V | 1× Tissu blanc · Builders |
| `Cloth_Roof_White_Flap` | Auvent en tissu blanc |  |  | 6 | 6 cuivres | calculé | V | 1× Tissu blanc · Builders |
| `Cloth_Roof_White_Flat` | Toit horizontal en tissu blanc |  |  | 6 | 6 cuivres | calculé | V | 1× Tissu blanc · Builders |
| `Cloth_Roof_White_Shallow` | Toit en tissu blanc : faible pente |  |  | 6 | 6 cuivres | calculé | V | 1× Tissu blanc · Builders |
| `Cloth_Roof_White_Steep` | Toit en tissu blanc : forte pente |  |  | 6 | 6 cuivres | calculé | V | 1× Tissu blanc · Builders |
| `Cloth_Roof_White_Vertical` | Toit vertical en tissu blanc |  |  | 6 | 6 cuivres | calculé | V | 1× Tissu blanc · Builders |
| `Cloth_Roof_Yellow` | Toit incliné en tissu jaune |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× Tissu jaune · Builders |
| `Cloth_Roof_Yellow_Flap` | Auvent en tissu jaune |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× Tissu jaune · Builders |
| `Cloth_Roof_Yellow_Flat` | Toit horizontal en tissu jaune |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× Tissu jaune · Builders |
| `Cloth_Roof_Yellow_Shallow` | Toit en tissu jaune : faible pente |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× Tissu jaune · Builders |
| `Cloth_Roof_Yellow_Steep` | Toit en tissu jaune : forte pente |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× Tissu jaune · Builders |
| `Cloth_Roof_Yellow_Vertical` | Toit vertical en tissu jaune |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× Tissu jaune · Builders |
| `Metal_Divin` | Bloc Divin | Divin | 800 | 5 200 000 | 5 impériales 2 astrales | calculé | E | 4× Lingot Divin · Forge_Divine |
| `Metal_Orbis_Copper_Fence` | Barres de cuivre embrasé | Commun | 15 | 51 | 5 argent 1 cuivre | calculé | V | 1× Lingot de cuivre → ×2 · Builders |

<a id="fam-meubles-decoration"></a>
## Meubles & décoration

### Sans recette — prix fixés (292)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Arcade_Machine` | Borne d’arcade |  |  | 50 | 5 argent | fixé | V |
| `Bench_Abilities` | Entrelacs du Lieur de runes |  |  | 50 | 5 argent | fixé | V |
| `Bench_Trough` | Mangeoire |  |  | 50 | 5 argent | fixé | V |
| `ChestTreasureCleBossA` | Coffre à Clé de Boss - Rang A |  | 18 | 59 | 5 argent 9 cuivres | fixé | E |
| `ChestTreasureCleBossCB` | Coffre à Clé de Boss - Rang C-B |  | 18 | 59 | 5 argent 9 cuivres | fixé | E |
| `ChestTreasureCleBossED` | Coffre à Clé de Boss - Rang E-D |  | 18 | 59 | 5 argent 9 cuivres | fixé | E |
| `ChestTreasureCleBossS` | Coffre à Clé de Boss - Rang S |  | 18 | 59 | 5 argent 9 cuivres | fixé | E |
| `ChestTreasureDefault` | Coffre Basic |  | 7 | 54 | 5 argent 4 cuivres | fixé | E |
| `ChestTreasureDungeonDefault` | Coffre de Donjon Basic |  | 7 | 54 | 5 argent 4 cuivres | fixé | E |
| `ChestTreasureLegendary` | Coffre Legendaire |  |  | 50 | 5 argent | fixé | E |
| `Deco_Bone_Full` | Os |  |  | 50 | 5 argent | fixé | V |
| `Deco_Bone_Pile` | Tas d’os |  |  | 50 | 5 argent | fixé | V |
| `Deco_Bone_Ribs` | Côte |  |  | 50 | 5 argent | fixé | V |
| `Deco_Bone_Ribs_Long` | Côte longue |  |  | 50 | 5 argent | fixé | V |
| `Deco_Bone_Skulls` | Tas de crânes |  |  | 50 | 5 argent | fixé | V |
| `Deco_Bone_Spike` | Pointe osseuse |  |  | 50 | 5 argent | fixé | V |
| `Deco_Bone_Spike_Large` | Grande pointe osseuse |  |  | 50 | 5 argent | fixé | V |
| `Deco_Bone_Spine` | Épine dorsale osseuse |  |  | 50 | 5 argent | fixé | V |
| `Deco_Bucket` | Seau ancien |  |  | 50 | 5 argent | fixé | V |
| `Deco_Campfire_Off` | Braises éteintes |  |  | 50 | 5 argent | fixé | V |
| `Deco_Coral_Shell` | Coquillage corail |  |  | 50 | 5 argent | fixé | V |
| `Deco_Coral_Shell_Purple` | Coquillage corail violacé |  |  | 50 | 5 argent | fixé | V |
| `Deco_Coral_Shell_Sanddollar` | Clypéaster |  |  | 50 | 5 argent | fixé | V |
| `Deco_Coral_Shell_Swirly` | Conque |  |  | 50 | 5 argent | fixé | V |
| `Deco_Coral_Shell_Urchin` | Coquille d’oursin |  |  | 50 | 5 argent | fixé | V |
| `Deco_Fire` | Feu |  |  | 50 | 5 argent | fixé | V |
| `Deco_Goblin_Plush` | Peluche gobelin |  |  | 50 | 5 argent | fixé | V |
| `Deco_Iron_Bars_Corner` | Barres de fer en angle |  |  | 50 | 5 argent | fixé | V |
| `Deco_Lantern_Ceiling` | Lanterne |  |  | 50 | 5 argent | fixé | V |
| `Deco_Lever` | Levier |  |  | 50 | 5 argent | fixé | V |
| `Deco_Map` | Carte ancienne |  |  | 50 | 5 argent | fixé | V |
| `Deco_Nest` | Nid d’oiseau |  |  | 50 | 5 argent | fixé | V |
| `Deco_Pot_Clay_Broken` | Pot en argile brisé |  |  | 50 | 5 argent | fixé | V |
| `Deco_Scarak_Cocoon` | Cocon de Scarak |  |  | 50 | 5 argent | fixé | V |
| `Deco_Scarak_Eggsacks` | Sac d’œufs de Scarak moyen |  |  | 50 | 5 argent | fixé | V |
| `Deco_Scarak_Spit` | Petit amas de crachat de Scarak |  |  | 50 | 5 argent | fixé | V |
| `Deco_Scarak_Spit_Full` | Amas de crachat de Scarak |  |  | 50 | 5 argent | fixé | V |
| `Deco_Scrap_Pile_Shiny` | Tas de déchets brillant |  |  | 50 | 5 argent | fixé | V |
| `Deco_Spider_Cocoon` | Cocon d’araignée |  |  | 50 | 5 argent | fixé | V |
| `Deco_SpiderWeb` | Petite toile d’araignée |  |  | 50 | 5 argent | fixé | V |
| `Deco_SpiderWeb_Flat` | Tapis en toile d’araignée |  |  | 50 | 5 argent | fixé | V |
| `Deco_SpiderWeb_Full` | Toile d’araignée |  |  | 50 | 5 argent | fixé | V |
| `Deco_SpiderWeb_Straight` | Corde en toile d’araignée |  |  | 50 | 5 argent | fixé | V |
| `Deco_Starfish` | Étoile de mer |  |  | 50 | 5 argent | fixé | V |
| `Deco_Trash` | Petit tas de déchets |  |  | 50 | 5 argent | fixé | V |
| `Deco_Trash_Banana_Peel` | Banane à moitié mangée |  |  | 50 | 5 argent | fixé | V |
| `Deco_Trash_Eaten_Fish` | Poisson à moitié mangé |  |  | 50 | 5 argent | fixé | V |
| `Deco_Trash_Pile_Large` | Grand tas de déchets |  |  | 50 | 5 argent | fixé | V |
| `Deco_Trash_Pile_Small` | Tas de déchets |  |  | 50 | 5 argent | fixé | V |
| `Deco_Treasure` | Trésor |  |  | 50 | 5 argent | fixé | V |
| `Deco_Treasure_Pile_Large` | Grand tas de trésor |  |  | 50 | 5 argent | fixé | V |
| `Deco_Treasure_Pile_Small` | Petit tas de trésor |  |  | 50 | 5 argent | fixé | V |
| `Deco_Trork_Cage` | Cage à Trork |  |  | 50 | 5 argent | fixé | V |
| `Eldanior_Banniere_Famille_Eldanior` | Bannière de famille - Eldanior | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Banniere_Famille_Frostguard` | Bannière de famille - Frostguard | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Banniere_Famille_Goldcrest` | Bannière de famille - Goldcrest | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Banniere_Famille_Hanazakura` | Bannière de famille - Hanazakura | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Banniere_Famille_Ironveil` | Bannière de famille - Ironveil | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Banniere_Famille_Kage` | Bannière de famille - Kage | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Banniere_Famille_Luo` | Bannière de famille - Luo | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Banniere_Famille_Runkandel` | Bannière de famille - Runkandel | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Banniere_Famille_Sama` | Bannière de famille - Sama | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Banniere_Famille_Setsuka` | Bannière de famille - Setsuka | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Banniere_Famille_Silkroad` | Bannière de famille - Silkroad | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Banniere_Famille_Spellweave` | Bannière de famille - Spellweave | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Banniere_Famille_Valmontis` | Bannière de famille - Valmontis | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Banniere_Famille_Warbane` | Bannière de famille - Warbane | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Banniere_Famille_Zippel` | Bannière de famille - Zippel | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Bloc_Noir_Transparent` | Bloc Noir Transparent | Commun |  | 50 | 5 argent | fixé | E |
| `Eldanior_Borne_Ville` | Borne de ville | Rare |  | **à fixer** |  | à fixer | E |
| `Eldanior_Cercle_Ancrage` | Cercle d'Ancrage | Rare | 1 | 500 | 5 or | fixé | E |
| `Eldanior_Portail_Continental` | Pierre de fondation du Portail Continental | Légendaire |  | **à fixer** |  | à fixer | E |
| `Eldanior_Porte_Boss_A` | Porte de Boss - Rang A | Épique |  | 2 000 | 2 diamants | fixé | E |
| `Eldanior_Porte_Boss_ED` | Porte de Boss - Rang E-D | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Porte_Boss_S` | Porte de Boss - Rang S | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Porte_Famille_Eldanior` | Porte du Trésor - Eldanior | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Porte_Famille_Frostguard` | Porte du Trésor - Frostguard | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Porte_Famille_Goldcrest` | Porte du Trésor - Goldcrest | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Porte_Famille_Hanazakura` | Porte du Trésor - Hanazakura | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Porte_Famille_Ironveil` | Porte du Trésor - Ironveil | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Porte_Famille_Kage` | Porte du Trésor - Kage | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Porte_Famille_Luo` | Porte du Trésor - Luo | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Porte_Famille_Runkandel` | Porte du Trésor - Runkandel | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Porte_Famille_Sama` | Porte du Trésor - Sama | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Porte_Famille_Setsuka` | Porte du Trésor - Setsuka | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Porte_Famille_Silkroad` | Porte du Trésor - Silkroad | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Porte_Famille_Spellweave` | Porte du Trésor - Spellweave | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Porte_Famille_Valmontis` | Porte du Trésor - Valmontis | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Porte_Famille_Warbane` | Porte du Trésor - Warbane | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Porte_Famille_Zippel` | Porte du Trésor - Zippel | Légendaire |  | 50 000 | 5 zéniths | fixé | E |
| `Eldanior_Presentoir_Famille_Eldanior` | Présentoir de famille - Eldanior | Épique | 300 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Presentoir_Famille_Frostguard` | Présentoir de famille - Frostguard | Épique | 300 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Presentoir_Famille_Goldcrest` | Présentoir de famille - Goldcrest | Épique | 300 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Presentoir_Famille_Hanazakura` | Présentoir de famille - Hanazakura | Épique | 300 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Presentoir_Famille_Ironveil` | Présentoir de famille - Ironveil | Épique | 300 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Presentoir_Famille_Kage` | Présentoir de famille - Kage | Épique | 300 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Presentoir_Famille_Luo` | Présentoir de famille - Luo | Épique | 300 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Presentoir_Famille_Runkandel` | Présentoir de famille - Runkandel | Épique | 300 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Presentoir_Famille_Sama` | Présentoir de famille - Sama | Épique | 300 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Presentoir_Famille_Setsuka` | Présentoir de famille - Setsuka | Épique | 300 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Presentoir_Famille_Silkroad` | Présentoir de famille - Silkroad | Épique | 300 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Presentoir_Famille_Spellweave` | Présentoir de famille - Spellweave | Épique | 300 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Presentoir_Famille_Valmontis` | Présentoir de famille - Valmontis | Épique | 300 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Presentoir_Famille_Warbane` | Présentoir de famille - Warbane | Épique | 300 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Presentoir_Famille_Zippel` | Présentoir de famille - Zippel | Épique | 300 | 4 000 | 4 diamants | fixé | E |
| `Farming_Collar` | Collier pour animal de compagnie | Commun | 8 | 54 | 5 argent 4 cuivres | fixé | V |
| `Furniture_Ancient_Barrel` | Vieux tonneau |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Ancient_Chest_Large` | Grand coffre ancien |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Ancient_Coffin` | Cercueil ancien |  | 3 | 52 | 5 argent 2 cuivres | fixé | V |
| `Furniture_Ancient_Crate` | Caisse ancienne cassée |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Ancient_Painting` | Tableau ancien |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Ancient_Pot` | Vase ancien |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Ancient_Sack` | Sac ancien |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Ancient_Statue` | Statue ancienne |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Ancient_Torch` | Torche ancienne |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Bookshelf_Corner` | Prototype : bibliothèque - coin en L unique |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Bookshelf_Left` | Prototype : bibliothèque - coin inférieur gauche |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Bookshelf_Middle` | Prototype : bibliothèque - bord inférieur |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Bookshelf_Right` | Prototype : bibliothèque - coin inférieur droit |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Crude_Brazier` | Petit feu de camp |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Crude_Chest_Large` | Grand coffre rudimentaire |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Desert_Bed` | Lit ensablé |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Desert_Chest_Large` | Grand coffre ensablé |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Desert_Lantern_Ceiling` | Lanterne ensablée |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Dungeon_Chest_Epic_Large` | Gros coffre chanceux |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Dungeon_Chest_Legendary_Large` | Coffre légendaire |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Dungeon_Earth_Brazier` | Brasero en terre |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Faun_Stool` | Tabouret de faune |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Feran_Bench` | Banc de Feran |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Feran_Bookshelf` | Bibliothèque de Feran |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Feran_Chest_Large` | Grand coffre de Feran |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Feran_Door` | Porte de Feran |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Feran_Ladder` | Échelle de Feran |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Feran_Platform` | Plateforme de Feran |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Feran_Pot` | Vase de Feran |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Feran_Pot_Secondary` | Vase de Feran |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Feran_Shelf` | Étagère de Feran |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Feran_Sign` | Pancarte de Feran |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Feran_Stool` | Tabouret de Feran |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Feran_Table` | Table de Feran |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Feran_Trapdoor` | Trappe de Feran |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Feran_Wardrobe` | Armoire de Feran |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Feran_Window` | Fenêtre de Feran |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Frozen_Castle_Bed` | Lit de château givré |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Frozen_Castle_Bench` | Banc de château givré |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Frozen_Castle_Chest_Large` | Grand coffre de château givré |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Frozen_Castle_Lamp` | Lampe de château givré |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Frozen_Castle_Pot` | Vase de château givré |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Frozen_Castle_Secondary_Lamp` | Lampe de château givré |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Frozen_Castle_Secondary_Pot` | Vase de château givré |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Goblin_Chest_Large` | Grand coffre gobelin |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Goblin_Coffer` | Coffre du Voile |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Goblin_Pot_Loot` | Pot à butin de gobelin | Commun |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Banner` | Bannière de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Bed` | Lit de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Bench` | Banc de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Bookshelf` | Bibliothèque de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Brazier` | Brasero de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Candle` | Bougie de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Chair` | Chaise de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Chest_Large` | Grand coffre de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Chest_Small` | Petit coffre de civilisation perdue |  | 7 | 54 | 5 argent 4 cuivres | fixé | V |
| `Furniture_Human_Ruins_Coffin` | Cercueil de civilisation perdue |  | 1 | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Desk` | Comptoir de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Door` | Porte en bois de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Door_Large` | Grande porte de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Door_Medium` | Porte moyenne de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Ladder` | Échelle de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Lantern` | Lanterne de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Lantern_Ceiling` | Lanterne de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Planter` | Jardinière de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Platform` | Plateforme de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Pot` | Vase de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Pot_Small` | Petit vase de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Shelf` | Étagère de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Sign` | Pancarte de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Statue` | Statue du Porteur de feu |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Statue_Broken` | Statue en ruines |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Stool` | Tabouret de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Table` | Table de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Torch` | Torche de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Trapdoor` | Trappe de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Wardrobe` | Armoire de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Human_Ruins_Window` | Fenêtre de civilisation perdue |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Jungle_Bookshelf` | Bibliothèque en bambou |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Jungle_Brazier` | Brasero en bambou |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Jungle_Chest_Large` | Grand coffre en bambou |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Jungle_Painting` | Tableau en bambou |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Jungle_Pot` | Vase en bambou |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Jungle_Pot_Secondary` | Vase en bambou |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Kweebec_Chest_Large` | Grand coffre de Kweebec |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Kweebec_Door` | Porte de Kweebec |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Kweebec_Ladder` | Échelle de Kweebec |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Kweebec_Lantern_Ceiling` | Lanterne de Kweebec |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Kweebec_Painting` | Tableau de Kweebec |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Kweebec_Platform` | Plateforme de Kweebec |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Kweebec_Shelf` | Étagère de Kweebec |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Kweebec_Sign` | Pancarte de Kweebec |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Kweebec_Statue` | Figurine de Kweebec sculptée |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Kweebec_Stool` | Tabouret de Kweebec |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Kweebec_Table` | Table de Kweebec |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Kweebec_Trapdoor` | Trappe de Kweebec |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Kweebec_Wardrobe` | Armoire de Kweebec |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Kweebec_Window` | Fenêtre de Kweebec |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Lumberjack_Chest_Large` | Grand coffre de bûcheron |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Lumberjack_Painting` | Tableau de bûcheron |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Outlander_Banner` | Bannière des Exilés |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Royal_Magic_Bed` | Lit magique royal |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Royal_Magic_Chair` | Chaise magique royale |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Royal_Magic_Chest_Large` | Grand coffre magique royal |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Royal_Magic_Couch` | Canapé magique royal |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Royal_Magic_Medium_Door` | Porte magique royale |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Royal_Magic_Pot` | Vase magique royal |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Royal_Magic_Potion_Glow_Ceiling` | Potion magique royale lumineuse |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Royal_Magic_Table` | Table magique royale |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Royal_Magic_Wardrobe` | Armoire magique royale |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Scarak_Hive_Chest_Large` | Grand coffre de ruche de Scarak |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Scarak_Hive_Door_Large` | Grande porte de ruche de Scarak |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Scarak_Hive_Door_Medium` | Porte moyenne de ruche de Scarak |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Scarak_Hive_Ladder` | Liane de Scarak |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Scarak_Hive_Lamp` | Larve de Scarak lumineuse |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Scarak_Hive_Lantern` | Larve de Scarak lumineuse |  | 10 | 55 | 5 argent 5 cuivres | fixé | V |
| `Furniture_Scarak_Hive_Platform` | Plateforme de ruche de Scarak |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Scarak_Hive_Window` | Fenêtre de ruche de Scarak |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Tavern_Barrel` | Tonneau de taverne |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Tavern_Chest_Large` | Grand coffre de taverne |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Tavern_Painting` | Tableau de taverne |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Dark_Candle` | Bougie du temple sombre |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Dark_Chest_Large` | Grand coffre du temple sombre |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Dark_Coffin` | Cercueil du temple sombre |  | 2 | 51 | 5 argent 1 cuivre | fixé | V |
| `Furniture_Temple_Dark_Door_Large` | Grande porte du temple sombre |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Dark_Door_Medium` | Porte moyenne du temple sombre |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Dark_Pot` | Jarre du temple sombre |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Dark_Statue` | Statue de l’observateur |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Dark_Statue_Gaia` | Statue de divinité des Exilés |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Emerald_Bed` | Lit de temple abandonné |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Emerald_Chest_Large` | Grand coffre de temple abandonné |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Emerald_Door_Medium` | Porte moyenne de temple abandonné |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Emerald_Statue` | Figurine en bois abandonnée |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Light_Bed` | Autel du temple de la lumière |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Light_Bench` | Banc du temple de la lumière |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Light_Chest_Large` | Grand coffre du temple de la lumière |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Light_Door_Large` | Grande porte du temple de la lumière |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Light_Door_Medilum` | Porte moyenne du temple de la lumière |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Light_Lantern_Ceiling` | Lanterne du temple de la lumière |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Light_Painting` | Tableau du temple de la lumière |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Light_Platform` | Plateforme du temple de la lumière |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Light_Pot` | Vase du temple de la lumière |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Light_Statue` | Statue de divinité assoupie |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Scarak_Brazier` | Brasero Scarak primordial |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Scarak_Chest_Large` | Grand coffre Scarak primordial |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Scarak_Chest_Small` | Petit coffre Scarak primordial |  | 20 | 60 | 6 argent | fixé | V |
| `Furniture_Temple_Scarak_Door_Large` | Grande porte Scarak primordial |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Scarak_Door_Medium` | Porte moyenne Scarak primordial |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Scarak_Ladder` | Échelle Scarak primordial |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Scarak_Lamp` | Lampe Scarak primordial |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Scarak_Lantern` | Lanterne Scarak primordial |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Scarak_Planter` | Jardinière Scarak primordial |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Scarak_Platform` | Plateforme Scarak primordial |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Scarak_Pot` | Vase Scarak primordial |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Scarak_Secondary_Pot` | Vase Scarak primordial |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Scarak_Statue` | Statue du Bâtisseur fondateur |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Scarak_Window` | Fenêtre Scarak primordial |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Wind_Chest_Large` | Grand coffre du temple du vent |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Wind_Door_Large` | Grande porte du temple du vent |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Wind_Door_Medium` | Porte moyenne du temple du vent |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Wind_Pot` | Vase du temple du vent |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Wind_Statue` | Statue de bête légendaire |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Temple_Wind_Statue_Gaia` | Statue de divinité silencieuse |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Village_Bed` | Lit en bois simple |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Village_Bench` | Banc en bois simple |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Village_Bookcase` | Bibliothèque en bois simple |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Village_Brazier` | Brasero en pierre simple |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Village_Chest_Large` | Grand coffre en fer |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Village_Coffin` | Cercueil en pierre |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Village_Painting_1x1` | Toile de peinture simple (1x1) |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Village_Pot` | Pot en argile |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Village_Statue` | Statue d’un gardien ancien |  |  | 50 | 5 argent | fixé | V |
| `Furniture_Village_Tombstone` | Pierre tombale |  |  | 50 | 5 argent | fixé | V |
| `Goblin_Graffiti_Fire` | Graffiti d’avertissement : feu |  |  | 50 | 5 argent | fixé | V |
| `Goblin_Graffiti_Large_Overheat` | Grand graffiti d’avertissement : surchauffe |  |  | 50 | 5 argent | fixé | V |
| `Goblin_Graffiti_Large_Spider` | Grand graffiti d’avertissement : araignée |  |  | 50 | 5 argent | fixé | V |
| `Goblin_Graffiti_Skeleton` | Graffiti d’avertissement : squelette |  |  | 50 | 5 argent | fixé | V |
| `Goblin_Graffiti_Spider` | Graffiti d’avertissement : araignée |  |  | 50 | 5 argent | fixé | V |
| `Goblin_Painting_Breach` | Tableau gobelin : Les brèches | Commun |  | 50 | 5 argent | fixé | V |
| `Goblin_Painting_Fail` | Tableau gobelin : Les expéditions ratées | Commun |  | 50 | 5 argent | fixé | V |
| `Goblin_Painting_ForgottenTemple` | Tableau gobelin : L’endroit étrange | Commun |  | 50 | 5 argent | fixé | V |
| `Goblin_Painting_Orbis` | Tableau gobelin : Le Voile du Néant | Commun |  | 50 | 5 argent | fixé | V |
| `Goblin_Painting_Portal` | Tableau gobelin : Le gobelin itinérant | Commun |  | 50 | 5 argent | fixé | V |
| `Goblin_Painting_Repair` | Tableau gobelin : La réparation indispensable | Commun |  | 50 | 5 argent | fixé | V |
| `Klops_Away_Sign` | FERMÉ | Commun |  | 50 | 5 argent | fixé | V |
| `Throwable_Explosive_Barrel` | Tonneau boum de gobelin | Commun |  | 50 | 5 argent | fixé | V |

### Avec recette — prix calculés (280)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O  Recette |
|---|---|---|---:|---:|---|---|---|---|
| `Alchemy_Cauldron` | Chaudron |  |  | 1 100 | 1 diamant 1 or | calculé | V | 6× Lingot de cuivre + 4× Argile blanche lisse + 2× Sève · Furniture_Bench |
| `Alchemy_Cauldron_Big` | Grand chaudron |  |  | 4 000 | 4 diamants | calculé | V | 12× Lingot de fer + 8× Argile blanche lisse + 4× Sève · Furniture_Bench |
| `Bench_Alchemy` | Établi de l’alchimiste |  | 9 | 4 900 | 4 diamants 9 or | calculé | V | 20× [N’importe quelle pierre] + 5× Lingot d’or + 10× Glande à venin + 10× Fragments d’os · Workbench |
| `Bench_Arcane` | Établi de l’arcaniste |  | 10 | 8 000 | 8 diamants | calculé | V | 10× Lingot de thorium + 30× Chutes de lin + 20× Essence du Néant · Workbench |
| `Bench_Armour` | Établi de l’armurier |  | 2 | 270 | 2 or 7 argent | calculé | V | 2× Lingot de cuivre + 10× [N’importe quelle bûche] + 5× [N’importe quelle pierre] · Workbench |
| `Bench_Builders` | Établi du bâtisseur |  | 2 | 43 | 4 argent 3 cuivres | calculé | V | 6× [N’importe quelle bûche] + 3× [N’importe quelle pierre] · Fieldcraft |
| `Bench_Campfire` | Feu de camp |  |  | 57 | 5 argent 7 cuivres | calculé | V | 4× Bâton + 2× [N’importe quels gravats] · Fieldcraft |
| `Bench_Cooking` | Cuisinière du chef |  | 6 | 270 | 2 or 7 argent | calculé | V | 2× Lingot de cuivre + 10× [N’importe quelle bûche] + 5× [N’importe quelle pierre] · Workbench |
| `Bench_Farming` | Établi du fermier |  | 4 | 300 | 3 or | calculé | V | 6× [N’importe quelle bûche] + 20× Fibre de plante · Workbench |
| `Bench_Forge_Divine` | Forge Divine | Divin | 800 | 16 000 000 | 16 impériales | calculé | E | 12× Lingot Divin + 12× Lingot d’adamantite + 20× [N’importe quelle pierre] · Weapon_Bench |
| `Bench_Furnace` | Fourneau |  | 1 | 47 | 4 argent 7 cuivres | calculé | V | 6× [N’importe quelle bûche] + 6× [N’importe quelle pierre] · Workbench |
| `Bench_Furniture` | Établi de menuisier |  | 7 | 44 | 4 argent 4 cuivres | calculé | V | 6× [N’importe quelle bûche] + 4× [N’importe quelle pierre] · Workbench |
| `Bench_Lumbermill` | Billot de bûcheron |  |  | 64 | 6 argent 4 cuivres | calculé | V | 1× [N’importe quelle bûche] + 2× [N’importe quels gravats] + 2× Fibre de plante + 2× Bâton · Furniture_Bench |
| `Bench_Salvage` | Récupérateur |  | 8 | 1 600 | 1 diamant 6 or | calculé | V | 6× Lingot de fer + 5× [N’importe quelle bûche] + 5× [N’importe quelle pierre] · Workbench |
| `Bench_Tannery` | Atelier de tannage |  | 3 | 86 | 8 argent 6 cuivres | calculé | V | 6× [N’importe quelle bûche] + 3× [N’importe quelle pierre] + 3× Cuir brut fin · Workbench |
| `Bench_Weapon` | Enclume du forgeron |  | 2 | 270 | 2 or 7 argent | calculé | V | 2× Lingot de cuivre + 10× [N’importe quelle bûche] + 5× [N’importe quelle pierre] · Workbench |
| `Bench_WorkBench` | Établi |  | 1 | 30 | 3 argent | calculé | V | 4× [N’importe quelle bûche] + 3× [N’importe quelle pierre] · Fieldcraft |
| `ChestTreasureDungeon` | Coffre de Donjon |  | 18 | 1 700 | 1 diamant 7 or | calculé | E | 3× Planches en bois dur + 2× Lingot d’or · Furniture_Bench |
| `ChestTreasureGold` | Coffre d'Argent |  | 17 | 950 | 9 or 5 argent | calculé | E | 3× Planches en bois mort + 1× Lingot d’or · Furniture_Bench |
| `Container_Bucket` | Seau en bois |  | 15 | 270 | 2 or 7 argent | calculé | V | 3× [N’importe quel bois] + 1× Lingot de fer · Farmingbench |
| `Coop_Chicken` | Poulailler |  | 17 | 2 200 | 2 diamants 2 or | calculé | V | 20× [N’importe quel bois] + 50× Essence de vie + 10× Fibre de plante · Farmingbench |
| `Deco_Bone_Ribs_Feran` | Côte de Feran |  |  | 86 | 8 argent 6 cuivres | calculé | V | 6× Fragments d’os · Furniture_Bench |
| `Deco_Bone_Skulls_Feran` | Tas de crânes de Feran |  |  | 43 | 4 argent 3 cuivres | calculé | V | 3× Fragments d’os · Furniture_Bench |
| `Deco_Bone_Skulls_Feran_Large` | Grand tas de crânes de Feran |  |  | 86 | 8 argent 6 cuivres | calculé | V | 6× Fragments d’os · Furniture_Bench |
| `Deco_Bone_Skulls_Feran_Wall` | Mur de crânes de Feran |  |  | 57 | 5 argent 7 cuivres | calculé | V | 4× Fragments d’os · Furniture_Bench |
| `Deco_Bone_Skulls_Wall` | Mur de crânes |  |  | 57 | 5 argent 7 cuivres | calculé | V | 4× Fragments d’os · Furniture_Bench |
| `Deco_Book_Pile_Large` | Grand tas de livres |  |  | 230 | 2 or 3 argent | calculé | V | 12× Fibre de plante + 4× Cuir léger · Furniture_Bench |
| `Deco_Book_Pile_Small` | Petit tas de livres |  |  | 110 | 1 or 1 argent | calculé | V | 6× Fibre de plante + 2× Cuir léger · Furniture_Bench |
| `Deco_Chair_Scrap` | Trône de gobelin |  |  | 650 | 6 or 5 argent | calculé | V | 3× Planches en bois noir + 2× Lingot de fer + 2× Fragments d’os · Furniture_Bench |
| `Deco_Halloween_Lights` | Lumières d’Halloween |  |  | 86 | 8 argent 6 cuivres | calculé | V | 2× Fragments de cristal rouges + 2× Fragments de cristal jaunes + 2× Fragments de cristal violets · Furniture_Bench |
| `Deco_Halloween_Pumpkin_Cute` | Citrouille d’Halloween |  |  | 43 | 4 argent 3 cuivres | calculé | V | 1× Citrouille + 2× Bâton + 1× Charbon de bois · Furniture_Bench |
| `Deco_Halloween_Pumpkin_Hypixel` | Citrouille d’Halloween |  |  | 43 | 4 argent 3 cuivres | calculé | V | 1× Citrouille + 2× Bâton + 1× Charbon de bois · Furniture_Bench |
| `Deco_Halloween_Pumpkin_Scary` | Citrouille d’Halloween |  |  | 43 | 4 argent 3 cuivres | calculé | V | 1× Citrouille + 2× Bâton + 1× Charbon de bois · Furniture_Bench |
| `Deco_Hive` | Ruche |  | 1 | 780 | 7 or 8 argent | calculé | V | 20× Essence de vie · Farmingbench |
| `Deco_Inkwell` | Encrier |  |  | 270 | 2 or 7 argent | calculé | V | 1× Lingot de fer + 1× Charbon de bois · Furniture_Bench |
| `Deco_Iron_Bars` | Barres de fer |  |  | 130 | 1 or 3 argent | calculé | V | 1× Lingot de fer → ×2 · Builders |
| `Deco_Iron_Bars_Platforms` | Barres de fer horizontales |  |  | 130 | 1 or 3 argent | calculé | V | 1× Lingot de fer → ×2 · Builders |
| `Deco_Iron_Chain_Small` | Petite chaîne en fer |  |  | 130 | 1 or 3 argent | calculé | V | 1× Lingot de fer → ×2 · Builders |
| `Deco_Iron_Chains` | Chaîne en fer diagonale |  |  | 260 | 2 or 6 argent | calculé | V | 1× Lingot de fer · Builders |
| `Deco_Iron_Chains_Vertical` | Chaîne en fer |  |  | 260 | 2 or 6 argent | calculé | V | 1× Lingot de fer · Builders |
| `Deco_Iron_Stack` | Pile de barres de fer |  |  | 260 | 2 or 6 argent | calculé | V | 1× Lingot de fer · Builders |
| `Deco_Kweebec_Plush` | Peluche de Kweebec |  |  | 130 | 1 or 3 argent | calculé | V | 2× Tissu cyan + 4× Fibre de plante · Furniture_Bench |
| `Deco_Lantern` | Lanterne |  |  | 210 | 2 or 1 argent | calculé | V | 2× [N’importe quelle bûche] + 1× Bougie + 4× Sève · Furniture_Bench |
| `Deco_Mug` | Tasse en bois |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× [N’importe quelles planches] + 1× Bâton · Furniture_Bench |
| `Deco_Plate` | Assiette |  |  | 220 | 2 or 2 argent | calculé | V | 2× Argile blanche lisse + 1× Sève · Furniture_Bench |
| `Deco_Potion` | Vieille potion |  |  | 140 | 1 or 4 argent | calculé | V | 1× Argile blanche lisse + 1× Sève + 1× Pétales rouges · Furniture_Bench |
| `Deco_Rope` | Corde |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× Fibre de plante · Builders |
| `Deco_Rope_Diagonal` | Corde diagonale |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× Fibre de plante · Builders |
| `Deco_Scarecrow` | Épouvantail |  | 1 | 440 | 4 or 4 argent | calculé | V | 3× [N’importe quel bois] + 3× Sève + 1× Citrouille + 10× Essence de vie · Farmingbench |
| `Deco_Scroll` | Parchemin Décoratif |  |  | 78 | 7 argent 8 cuivres | calculé | V | 4× Fibre de plante + 2× Bâton · Furniture_Bench |
| `Deco_Shutter` | Volet en bois |  |  | 39 | 3 argent 9 cuivres | calculé | V | 2× [N’importe quelle bûche] + 2× Sève · Architectsbench |
| `Deco_SpiderWeb_Diagonal` | Corde en toile d’araignée diagonale |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× Fibre de plante · Builders |
| `Deco_Tankard` | Chope |  |  | 270 | 2 or 7 argent | calculé | V | 1× Planches en bois dur + 1× Lingot de fer · Farmingbench |
| `Deco_Target` | Cible en paille |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× Fibre de plante · Workbench |
| `Deco_Trophy_Harvest` | Trophée de récolte | Légendaire | 22 | 520 000 | 5 astrales 2 zéniths | calculé | V | 100× Grande essence de vie + 100× Baies sauvages + 100× Blé + 100× Laitue + 100× Carotte + 100× Maïs + 100× Chou-fleur + 100× Navet + 100× Aubergine + 100× Citrouille + 100× Tomate + 100× Piment + 100× Coton + 100× Riz + 100× Oignon + 100× Pomme de terre + 100× Pomme + 100× Œuf + 100× Chutes de laine + 100× [N’importe quel poisson] + 100× [N’importe quelle mousse] + 100× [N’importe quelle bûche] + 100× Excrément + 100× Plumes blanches + 100× Sève + 100× Fibre de plante + 8× [N’importe quel seau de lait] + 8× [N’importe quel seau de lait de Cornemousse] · Farmingbench |
| `Deco_Wallchain` | Anneau de mur |  |  | 530 | 5 or 3 argent | calculé | V | 2× Lingot de fer + 1× Bâton · Furniture_Bench |
| `Eldanior_Bibliotheque` | Eldanior_Bibliotheque |  |  | 39 | 3 argent 9 cuivres | calculé | E | 6× [Bois dur] · Furniture_Bench |
| `Eldanior_Mannequin_Dojo` | Mannequin d'entraînement | Peu commun | 10 | 96 | 9 argent 6 cuivres | calculé | E | 2× [N’importe quelle bûche] + 6× Fibre de plante + 4× [N’importe quelle pierre] · Furniture_Bench |
| `Eldanior_Porte_Boss_CB` | Porte de Boss - Rang C-B | Rare |  | 530 | 5 or 3 argent | calculé | E | 2× [N’importe quelle bûche] + 2× Lingot de fer · Furniture_Bench |
| `Eldanior_Presentoir` | Eldanior_Presentoir | Peu commun | 20 | 3 400 | 3 diamants 4 or | calculé | E | 16× [N’importe quelle pierre] + 4× Lingot d’or · Furniture_Bench |
| `Eldanior_Presentoir_Petit` | Petit présentoir | Peu commun | 20 | 850 | 8 or 5 argent | calculé | E | 6× [N’importe quelle pierre] + 1× Lingot d’or · Furniture_Bench |
| `Furniture_Ancient_Bed` | Lit en bois délabré |  | 3 | 180 | 1 or 8 argent | calculé | V | 3× [Bois noir ou bois mort] + 2× Tissu vert + 1× Tissu blanc · Furniture_Bench |
| `Furniture_Ancient_Bench` | Banc ancien |  |  | 34 | 3 argent 4 cuivres | calculé | V | 1× [Bois noir ou bois mort] · Builders |
| `Furniture_Ancient_Bookshelf` | Bibliothèque ancienne |  |  | 34 | 3 argent 4 cuivres | calculé | V | 1× [Bois noir ou bois mort] · Builders |
| `Furniture_Ancient_Candle` | Bougie ancienne |  |  | 52 | 5 argent 2 cuivres | calculé | V | 2× Fibre de plante + 2× Sève · Furniture_Bench |
| `Furniture_Ancient_Chair` | Chaise ancienne |  |  | 68 | 6 argent 8 cuivres | calculé | V | 2× [Bois noir ou bois mort] · Builders |
| `Furniture_Ancient_Chest_Small` | Petit coffre ancien |  | 8 | 300 | 3 or | calculé | V | 3× Planches en bois mort + 2× Lingot de cuivre · Furniture_Bench |
| `Furniture_Ancient_Door` | Porte en bois ancienne |  |  | 68 | 6 argent 8 cuivres | calculé | V | 2× [Bois noir ou bois mort] · Builders |
| `Furniture_Ancient_Ladder` | Échelle ancienne |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× [Bois noir ou bois mort] → ×2 · Builders |
| `Furniture_Ancient_Platform` | Plateforme ancienne |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× [Bois noir ou bois mort] → ×2 · Builders |
| `Furniture_Ancient_Shelf` | Étagère ancienne |  |  | 34 | 3 argent 4 cuivres | calculé | V | 1× [Bois noir ou bois mort] · Builders |
| `Furniture_Ancient_Sign` | Pancarte ancienne |  |  | 34 | 3 argent 4 cuivres | calculé | V | 1× [Bois noir ou bois mort] · Builders |
| `Furniture_Ancient_Table` | Table ancienne |  |  | 140 | 1 or 4 argent | calculé | V | 4× [Bois noir ou bois mort] · Builders |
| `Furniture_Ancient_Trapdoor` | Trappe ancienne |  |  | 34 | 3 argent 4 cuivres | calculé | V | 1× [Bois noir ou bois mort] · Builders |
| `Furniture_Ancient_Wardrobe` | Armoire ancienne |  |  | 140 | 1 or 4 argent | calculé | V | 4× [Bois noir ou bois mort] · Builders |
| `Furniture_Ancient_Window` | Fenêtre ancienne |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× [Bois noir ou bois mort] → ×2 · Builders |
| `Furniture_Castle_Bench` | Banc en pierre |  |  | 4 | 4 cuivres | calculé | V | 1× [Brique de pierre] · Builders |
| `Furniture_Christmas_Chest_Small` | Petit cadeau de Noël |  |  | 60 | 6 argent | calculé | V | 3× Pétales jaunes + 1× Chutes de lin + 2× Fibre de plante · Furniture_Bench |
| `Furniture_Christmas_Chest_Small_Green` | Petit cadeau de Noël vert |  |  | 110 | 1 or 1 argent | calculé | V | 3× Pétales verts + 1× Chutes de lin + 2× Fibre de plante · Furniture_Bench |
| `Furniture_Christmas_Chest_Small_Red` | Petit cadeau de Noël rouge |  |  | 60 | 6 argent | calculé | V | 3× Pétales jaunes + 1× Chutes de lin + 2× Fibre de plante · Furniture_Bench |
| `Furniture_Christmas_Chest_Small_RedDotted` | Petit cadeau de Noël rouge à pois |  |  | 91 | 9 argent 1 cuivre | calculé | V | 2× Pétales rouges + 1× Pétales jaunes + 1× Chutes de lin + 2× Fibre de plante · Furniture_Bench |
| `Furniture_Christmas_Chest_Small_White` | Petit cadeau de Noël blanc |  |  | 79 | 7 argent 9 cuivres | calculé | V | 3× Pétales blancs + 1× Chutes de lin + 2× Fibre de plante · Furniture_Bench |
| `Furniture_Crude_Bed` | Paillasse de voyage |  |  | 68 | 6 argent 8 cuivres | calculé | V | 3× Fibre de plante + 2× Cuir brut fin · Fieldcraft |
| `Furniture_Crude_Candle` | Bougie |  |  | 110 | 1 or 1 argent | calculé | V | 1× Sève + 1× Lingot de cuivre · Furniture_Bench |
| `Furniture_Crude_Chest_Small` | Petit coffre rudimentaire |  |  | 65 | 6 argent 5 cuivres | calculé | V | 10× [N’importe quelle bûche] · Fieldcraft |
| `Furniture_Crude_Door` | Porte rudimentaire |  |  | 68 | 6 argent 8 cuivres | calculé | V | 2× [Bois clair ou bois tendre] · Builders |
| `Furniture_Crude_Ladder` | Échelle rudimentaire |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× [Bois clair ou bois tendre] → ×2 · Builders |
| `Furniture_Crude_Platform` | Plateforme rudimentaire |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× [Bois clair ou bois tendre] → ×2 · Builders |
| `Furniture_Crude_Shelf` | Grande étagère en bois rudimentaire |  |  | 34 | 3 argent 4 cuivres | calculé | V | 1× [Bois clair ou bois tendre] · Builders |
| `Furniture_Crude_Sign` | Pancarte rudimentaire |  |  | 34 | 3 argent 4 cuivres | calculé | V | 1× [Bois clair ou bois tendre] · Builders |
| `Furniture_Crude_Stool` | Tabouret rudimentaire |  |  | 34 | 3 argent 4 cuivres | calculé | V | 1× [Bois clair ou bois tendre] · Builders |
| `Furniture_Crude_Table` | Table rudimentaire |  |  | 140 | 1 or 4 argent | calculé | V | 4× [Bois clair ou bois tendre] · Builders |
| `Furniture_Crude_Torch` | Torche rudimentaire |  |  | 10 | 1 argent | calculé | V | 1× Fibre de plante + 1× Sève + 1× Bâton → ×4 · Fieldcraft |
| `Furniture_Crude_Trapdoor` | Trappe rudimentaire |  |  | 34 | 3 argent 4 cuivres | calculé | V | 1× [Bois clair ou bois tendre] · Builders |
| `Furniture_Crude_Wardrobe` | Armoire rudimentaire |  |  | 140 | 1 or 4 argent | calculé | V | 4× [Bois clair ou bois tendre] · Builders |
| `Furniture_Crude_Window` | Fenêtre rudimentaire |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× [Bois clair ou bois tendre] → ×2 · Builders |
| `Furniture_Desert_Chair` | Chaise ensablée |  |  | 68 | 6 argent 8 cuivres | calculé | V | 2× [Planches de bois sec ou de bois doré] · Builders |
| `Furniture_Desert_Chest_Small` | Petit coffre ensablé |  | 13 | 1 500 | 1 diamant 5 or | calculé | V | 3× Planches en bois sec + 2× Lingot de thorium + 2× Tissu bleu · Furniture_Bench |
| `Furniture_Desert_Door` | Porte ensablée |  |  | 68 | 6 argent 8 cuivres | calculé | V | 2× [Planches de bois sec ou de bois doré] · Builders |
| `Furniture_Desert_Ladder` | Échelle ensablée |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× [Planches de bois sec ou de bois doré] → ×2 · Builders |
| `Furniture_Desert_Lantern` | Lanterne ensablée |  |  | 780 | 7 or 8 argent | calculé | V | 1× [Planches de bois sec ou de bois doré] + 1× Lingot de thorium + 1× Tissu cyan clair + 1× Pétales azurés · Furniture_Bench |
| `Furniture_Desert_Lantern_Tall` | Grande lanterne ensablée |  |  | 2 000 | 2 diamants | calculé | V | 2× Lanterne ensablée · Furniture_Bench |
| `Furniture_Desert_Platform` | Plateforme ensablée |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× [Planches de bois sec ou de bois doré] → ×2 · Builders |
| `Furniture_Desert_Shelf` | Étagère ensablée |  |  | 34 | 3 argent 4 cuivres | calculé | V | 1× [Planches de bois sec ou de bois doré] · Builders |
| `Furniture_Desert_Sign` | Pancarte ensablée |  |  | 34 | 3 argent 4 cuivres | calculé | V | 1× [Planches de bois sec ou de bois doré] · Builders |
| `Furniture_Desert_Table` | Table ensablée |  |  | 140 | 1 or 4 argent | calculé | V | 4× [Planches de bois sec ou de bois doré] · Builders |
| `Furniture_Desert_Torch` | Torche ensablée |  |  | 1 500 | 1 diamant 5 or | calculé | V | 1× [Planches de bois sec ou de bois doré] + 2× Lingot de thorium + 1× Tissu gris + 1× Charbon de bois · Furniture_Bench |
| `Furniture_Desert_Trapdoor` | Trappe ensablée |  |  | 34 | 3 argent 4 cuivres | calculé | V | 1× [Planches de bois sec ou de bois doré] · Builders |
| `Furniture_Desert_Wardrobe` | Armoire ensablée |  |  | 140 | 1 or 4 argent | calculé | V | 4× [Planches de bois sec ou de bois doré] · Builders |
| `Furniture_Desert_Window` | Fenêtre ensablée |  |  | 17 | 1 argent 7 cuivres | calculé | V | 1× [Planches de bois sec ou de bois doré] → ×2 · Builders |
| `Furniture_Dungeon_Chest_Epic` | Coffre chanceux |  | 18 | 1 700 | 1 diamant 7 or | calculé | V | 3× Planches en bois dur + 2× Lingot d’or · Furniture_Bench |
| `Furniture_Feran_Bed` | Lit de Feran |  | 5 | 300 | 3 or | calculé | V | 3× Planches en bois sec + 4× Fibre de plante + 2× Cuir moyen + 2× [Os] · Furniture_Bench |
| `Furniture_Feran_Candle` | Bougie de Feran |  |  | 280 | 2 or 8 argent | calculé | V | 2× Fibre de plante + 2× Sève + 4× [Os] · Furniture_Bench |
| `Furniture_Feran_Chandelier` | Lustre de Feran |  |  | 1 900 | 1 diamant 9 or | calculé | V | 4× Lingot de fer + 4× Bougie + 2× Planches en bois sec + 4× [Os] + 1× Cuir léger · Furniture_Bench |
| `Furniture_Feran_Chest_Small` | Petit coffre de Feran |  | 12 | 160 | 1 or 6 argent | calculé | V | 3× [Argiles] + 2× [Os] + 2× Cuir léger · Furniture_Bench |
| `Furniture_Feran_Planter` | Jardinière de Feran |  | 3 | 400 | 4 or | calculé | V | 3× [Argiles] + 2× [N’importe quel sol] + 10× Essence de vie · Farmingbench |
| `Furniture_Feran_Torch` | Torche de Feran |  |  | 280 | 2 or 8 argent | calculé | V | 2× Fibre de plante + 2× Sève + 4× [Os] · Furniture_Bench |
| `Furniture_Feran_Torch_Tall` | Grande torche de Feran |  |  | 610 | 6 or 1 argent | calculé | V | 1× Torche de Feran + 2× Charbon de bois + 4× [Os] · Furniture_Bench |
| `Furniture_Frozen_Castle_Chair` | Chaise de château givré |  |  | 52 | 5 argent 2 cuivres | calculé | V | 2× Bûche d’arbre gelé · Builders |
| `Furniture_Frozen_Castle_Chest_Small` | Petit coffre de château givré |  | 22 | 170 | 1 or 7 argent | calculé | V | 3× Essence de glace + 2× Glace + 2× Bûche d’arbre gelé · Furniture_Bench |
| `Furniture_Frozen_Castle_Door` | Porte de château givré |  |  | 52 | 5 argent 2 cuivres | calculé | V | 2× Bûche d’arbre gelé · Builders |
| `Furniture_Frozen_Castle_Ladder` | Échelle de château givré |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche d’arbre gelé → ×2 · Builders |
| `Furniture_Frozen_Castle_Platform` | Plateforme de château givré |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche d’arbre gelé → ×2 · Builders |
| `Furniture_Frozen_Castle_Shelf` | Étagère de château givré |  |  | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche d’arbre gelé · Builders |
| `Furniture_Frozen_Castle_Sign` | Pancarte de château givré |  |  | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche d’arbre gelé · Builders |
| `Furniture_Frozen_Castle_Table` | Table de château givré |  |  | 100 | 1 or | calculé | V | 4× Bûche d’arbre gelé · Builders |
| `Furniture_Frozen_Castle_Trapdoor` | Trappe de château givré |  |  | 26 | 2 argent 6 cuivres | calculé | V | 1× Bûche d’arbre gelé · Builders |
| `Furniture_Frozen_Castle_Wardrobe` | Armoire de château givré |  |  | 100 | 1 or | calculé | V | 4× Bûche d’arbre gelé · Builders |
| `Furniture_Frozen_Castle_Window` | Fenêtre de château givré |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× Bûche d’arbre gelé → ×2 · Builders |
| `Furniture_Goblin_Bed` | Lit de gobelin |  | 10 | 690 | 6 or 9 argent | calculé | V | 30× Ferraille + 10× [N’importe quelles planches] + 5× Soie d'araignée · GoblinLab_Workbench |
| `Furniture_Goblin_Brazier` | Brasero de gobelin |  | 11 | 350 | 3 or 5 argent | calculé | V | 20× Ferraille + 5× Charbon de bois · GoblinLab_Workbench |
| `Furniture_Goblin_Candle` | Bougie de gobelin |  | 12 | 98 | 9 argent 8 cuivres | calculé | V | 5× Ferraille + 2× Charbon de bois · GoblinLab_Workbench |
| `Furniture_Goblin_Chest_Small` | Petit coffre gobelin |  | 17 | 950 | 9 or 5 argent | calculé | V | 3× Planches en bois mort + 1× Lingot d’or · Furniture_Bench |
| `Furniture_Goblin_ChestStolen_Small` | Coffre volé |  | 17 | 950 | 9 or 5 argent | calculé | V | 3× Planches en bois mort + 1× Lingot d’or · Furniture_Bench |
| `Furniture_Goblin_Door` | Porte de gobelin |  | 13 | 400 | 4 or | calculé | V | 25× Ferraille + 10× [N’importe quelles planches] · GoblinLab_Workbench |
| `Furniture_Goblin_Door_Medium` | Porte de gobelin moyenne |  | 14 | 2 100 | 2 diamants 1 or | calculé | V | 50× Ferraille + 20× [N’importe quelles planches] + 5× Lingot de fer · GoblinLab_Workbench |
| `Furniture_Goblin_Ladder` | Échelle de gobelin |  | 15 | 330 | 3 or 3 argent | calculé | V | 5× Ferraille + 1× Lingot de fer · GoblinLab_Workbench |
| `Furniture_Goblin_Sign` | Pancarte de gobelin |  | 16 | 230 | 2 or 3 argent | calculé | V | 15× Ferraille + 5× [N’importe quelles planches] · GoblinLab_Workbench |
| `Furniture_Goblin_Statue_Graffiti` | Statue de gardien dégradée |  | 20 | 770 | 7 or 7 argent | calculé | V | 50× Ferraille + 20× Pavés de pierre moussus · GoblinLab_Workbench |
| `Furniture_Goblin_Stool` | Tabouret de gobelin |  | 17 | 460 | 4 or 6 argent | calculé | V | 1× Brasero de gobelin · GoblinLab_Workbench |
| `Furniture_Goblin_Table` | Table de gobelin |  | 18 | 260 | 2 or 6 argent | calculé | V | 30× [N’importe quelles planches] + 10× Ferraille · GoblinLab_Workbench |
| `Furniture_Goblin_Window` | Fenêtre de gobelin |  | 19 | 990 | 9 or 9 argent | calculé | V | 15× Ferraille + 3× Lingot de fer · GoblinLab_Workbench |
| `Furniture_Jungle_Bed` | Lit en bambou |  | 7 | 220 | 2 or 2 argent | calculé | V | 2× Bûche de bambou + 4× Fibre de plante + 2× Tissu cyan + 1× Tissu orange + 1× Tissu blanc · Furniture_Bench |
| `Furniture_Jungle_Bench` | Banc en bambou |  |  | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois tropical] · Builders |
| `Furniture_Jungle_Candle` | Bougie en bambou |  |  | 78 | 7 argent 8 cuivres | calculé | V | 2× Fibre de plante + 2× Sève + 1× Bûche de bambou · Furniture_Bench |
| `Furniture_Jungle_Chair` | Chaise en bambou |  |  | 52 | 5 argent 2 cuivres | calculé | V | 2× [Bois tropical] · Builders |
| `Furniture_Jungle_Chest_Small` | Petit coffre en bambou |  | 10 | 150 | 1 or 5 argent | calculé | V | 3× Planches en bois tropical + 2× Bûche de bambou · Furniture_Bench |
| `Furniture_Jungle_Door` | Porte en bambou |  |  | 52 | 5 argent 2 cuivres | calculé | V | 2× [Bois tropical] · Builders |
| `Furniture_Jungle_Ladder` | Échelle en bambou |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois tropical] → ×2 · Builders |
| `Furniture_Jungle_Planter` | Jardinière en bambou |  | 4 | 680 | 6 or 8 argent | calculé | V | 20× Fibre de plante + 2× [N’importe quel sol] + 10× Essence de vie + 1× Bûche de bambou · Farmingbench |
| `Furniture_Jungle_Platform` | Plateforme en bambou |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois tropical] → ×2 · Builders |
| `Furniture_Jungle_Shelf` | Étagère en bambou |  |  | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois tropical] · Builders |
| `Furniture_Jungle_Table` | Table en bambou |  |  | 100 | 1 or | calculé | V | 4× [Bois tropical] · Builders |
| `Furniture_Jungle_Torch` | Grande torche en bambou |  |  | 160 | 1 or 6 argent | calculé | V | 1× Bougie en bambou + 1× Tissu jaune clair + 1× Tissu jaune · Furniture_Bench |
| `Furniture_Jungle_Trapdoor` | Trappe en bambou |  |  | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois tropical] · Builders |
| `Furniture_Jungle_Wardrobe` | Armoire en bambou |  |  | 100 | 1 or | calculé | V | 4× [Bois tropical] · Builders |
| `Furniture_Jungle_Window` | Fenêtre en bambou |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois tropical] → ×2 · Builders |
| `Furniture_Kweebec_Bed` | Lit de Kweebec |  | 1 | 64 | 6 argent 4 cuivres | calculé | V | 3× [N’importe quel bois] + 4× Fibre de plante · Furniture_Bench |
| `Furniture_Kweebec_Candle` | Bougie de Kweebec |  |  | 52 | 5 argent 2 cuivres | calculé | V | 2× Fibre de plante + 2× Sève · Furniture_Bench |
| `Furniture_Kweebec_Chest_Small` | Petit coffre de Kweebec |  | 11 | 120 | 1 or 2 argent | calculé | V | 2× Essence de vie + 2× Fibre de plante + 1× Bâton · Furniture_Bench |
| `Furniture_Kweebec_Lantern` | Lanterne de Kweebec |  |  | 680 | 6 or 8 argent | calculé | V | 2× Lingot de fer + 1× Bougie + 1× Sève · Furniture_Bench |
| `Furniture_Lumberjack_Bed` | Lit de bûcheron |  | 4 | 110 | 1 or 1 argent | calculé | V | 3× Planches en bois dur + 4× Fibre de plante + 2× Cuir brut dense + 1× Tissu blanc · Furniture_Bench |
| `Furniture_Lumberjack_Chair` | Chaise de bûcheron |  |  | 52 | 5 argent 2 cuivres | calculé | V | 2× [Bois rouge] · Builders |
| `Furniture_Lumberjack_Chest_Small` | Petit coffre de bûcheron |  | 4 | 360 | 3 or 6 argent | calculé | V | 3× Planches en bois rouge + 1× Lingot de fer · Furniture_Bench |
| `Furniture_Lumberjack_Door` | Porte de bûcheron |  |  | 52 | 5 argent 2 cuivres | calculé | V | 2× [Bois rouge] · Builders |
| `Furniture_Lumberjack_Ladder` | Échelle de bûcheron |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois rouge] → ×2 · Builders |
| `Furniture_Lumberjack_Lamp` | Lampe de bûcheron |  |  | 670 | 6 or 7 argent | calculé | V | 2× Lingot de fer + 1× Bougie + 1× Planches en bois dur · Furniture_Bench |
| `Furniture_Lumberjack_Lantern` | Bougie de bûcheron |  |  | 680 | 6 or 8 argent | calculé | V | 2× Lingot de fer + 1× Bougie + 1× Sève · Furniture_Bench |
| `Furniture_Lumberjack_Platform` | Plateforme de bûcheron |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois rouge] → ×2 · Builders |
| `Furniture_Lumberjack_Shelf` | Étagère de bûcheron |  |  | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois rouge] · Builders |
| `Furniture_Lumberjack_Sign` | Pancarte de bûcheron |  |  | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois rouge] · Builders |
| `Furniture_Lumberjack_Table` | Table de bûcheron |  |  | 100 | 1 or | calculé | V | 4× [Bois rouge] · Builders |
| `Furniture_Lumberjack_Trapdoor` | Trappe de bûcheron |  |  | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois rouge] · Builders |
| `Furniture_Lumberjack_Wardrobe` | Armoire de bûcheron |  |  | 100 | 1 or | calculé | V | 4× [Bois rouge] · Builders |
| `Furniture_Lumberjack_Window` | Fenêtre de bûcheron |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois rouge] → ×2 · Builders |
| `Furniture_Royal_Magic_Chest_Small` | Petit coffre magique royal |  | 21 | 1 700 | 1 diamant 7 or | calculé | V | 2× Cuir sombre + 2× Lingot d’or + 1× Rubis · Furniture_Bench |
| `Furniture_Royal_Magic_Potion_Glow` | Potion magique royale lumineuse |  |  | 20 | 2 argent | calculé | V | 1× [N’importe quelle bûche] + 1× Sève |
| `Furniture_Scarak_Hive_Chest_Small` | Petit coffre de ruche de Scarak |  | 19 | 1 500 | 1 diamant 5 or | calculé | V | 3× Chitine solide + 2× Lingot de thorium + 2× Cuir moyen · Furniture_Bench |
| `Furniture_Tavern_Bed` | Lit de taverne |  | 2 | 240 | 2 or 4 argent | calculé | V | 3× Planches en bois sombre + 4× Fibre de plante + 2× Tissu rouge + 1× Tissu blanc · Furniture_Bench |
| `Furniture_Tavern_Bench` | Banc de taverne |  |  | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sombre] · Builders |
| `Furniture_Tavern_Candle` | Bougie de taverne |  |  | 52 | 5 argent 2 cuivres | calculé | V | 2× Fibre de plante + 2× Sève · Furniture_Bench |
| `Furniture_Tavern_Chair` | Chaise de taverne |  |  | 52 | 5 argent 2 cuivres | calculé | V | 2× [Bois sombre] · Builders |
| `Furniture_Tavern_Chandelier` | Lustre de taverne |  |  | 1 700 | 1 diamant 7 or | calculé | V | 4× Lingot de fer + 4× Bougie + 2× Planches en bois sombre · Furniture_Bench |
| `Furniture_Tavern_Chest_Small` | Petit coffre de taverne |  | 6 | 620 | 6 or 2 argent | calculé | V | 3× Planches en bois sombre + 2× Lingot de fer · Furniture_Bench |
| `Furniture_Tavern_Door` | Porte de taverne |  |  | 52 | 5 argent 2 cuivres | calculé | V | 2× [Bois sombre] · Builders |
| `Furniture_Tavern_Ladder` | Échelle de taverne |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois sombre] → ×2 · Builders |
| `Furniture_Tavern_Planter` | Jardinière de taverne |  | 2 | 670 | 6 or 7 argent | calculé | V | 3× [N’importe quelle bûche] + 1× Lingot de fer + 2× [N’importe quel sol] + 10× Essence de vie · Farmingbench |
| `Furniture_Tavern_Platform` | Plateforme de taverne |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois sombre] → ×2 · Builders |
| `Furniture_Tavern_Shelf` | Étagère de taverne |  |  | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sombre] · Builders |
| `Furniture_Tavern_Sign` | Pancarte de taverne |  |  | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sombre] · Builders |
| `Furniture_Tavern_Stool` | Tabouret de taverne |  |  | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sombre] · Builders |
| `Furniture_Tavern_Table` | Table de taverne |  |  | 100 | 1 or | calculé | V | 4× [Bois sombre] · Builders |
| `Furniture_Tavern_Trapdoor` | Trappe de taverne |  |  | 26 | 2 argent 6 cuivres | calculé | V | 1× [Bois sombre] · Builders |
| `Furniture_Tavern_Wardrobe` | Armoire de taverne |  |  | 100 | 1 or | calculé | V | 4× [Bois sombre] · Builders |
| `Furniture_Tavern_Window` | Fenêtre de taverne |  |  | 13 | 1 argent 3 cuivres | calculé | V | 1× [Bois sombre] → ×2 · Builders |
| `Furniture_Temple_Dark_Bed` | Lit du temple sombre |  | 6 | 61 | 6 argent 1 cuivre | calculé | V | 3× Schiste + 4× Gravier de schiste · Furniture_Bench |
| `Furniture_Temple_Dark_Brazier` | Brasero du temple sombre |  |  | 1 100 | 1 diamant 1 or | calculé | V | 4× Lingot de fer + 3× [N’importe quelle pierre] + 2× Charbon de bois · Furniture_Bench |
| `Furniture_Temple_Dark_Chest_Small` | Petit coffre du temple sombre |  | 9 | 700 | 7 or | calculé | V | 3× Planches en bois noir + 2× Lingot de fer + 2× Essence du Néant · Furniture_Bench |
| `Furniture_Temple_Dark_Door` | Porte du temple sombre |  |  | 5 | 5 cuivres | calculé | V | 2× [Schiste ou pavés de schiste] · Builders |
| `Furniture_Temple_Dark_Ladder` | Échelle du temple sombre |  |  | 1 | 1 cuivre | calculé | V | 1× [Schiste ou pavés de schiste] → ×2 · Builders |
| `Furniture_Temple_Dark_Platform` | Plateforme du temple sombre |  |  | 1 | 1 cuivre | calculé | V | 1× [Schiste ou pavés de schiste] → ×2 · Builders |
| `Furniture_Temple_Dark_Shelf` | Étagère du temple sombre |  |  | 3 | 3 cuivres | calculé | V | 1× [Schiste ou pavés de schiste] · Builders |
| `Furniture_Temple_Dark_Sign` | Pancarte du temple sombre |  |  | 3 | 3 cuivres | calculé | V | 1× [Schiste ou pavés de schiste] · Builders |
| `Furniture_Temple_Dark_Stool` | Tabouret du temple sombre |  |  | 3 | 3 cuivres | calculé | V | 1× [Schiste ou pavés de schiste] · Builders |
| `Furniture_Temple_Dark_Table` | Table du temple sombre |  |  | 10 | 1 argent | calculé | V | 4× [Schiste ou pavés de schiste] · Builders |
| `Furniture_Temple_Dark_Trapdoor` | Trappe du temple sombre |  |  | 3 | 3 cuivres | calculé | V | 1× [Schiste ou pavés de schiste] · Builders |
| `Furniture_Temple_Dark_Wardrobe` | Armoire du temple sombre |  |  | 10 | 1 argent | calculé | V | 4× [Schiste ou pavés de schiste] · Builders |
| `Furniture_Temple_Dark_Window` | Fenêtre du temple sombre |  |  | 1 | 1 cuivre | calculé | V | 1× [Schiste ou pavés de schiste] → ×2 · Builders |
| `Furniture_Temple_Earth_Door` | Porte du temple de la terre |  |  | 530 | 5 or 3 argent | calculé | V | 2× [N’importe quelle bûche] + 2× Lingot de fer · Furniture_Bench |
| `Furniture_Temple_Emerald_Chest_Small` | Petit coffre de temple abandonné |  | 14 | 72 | 7 argent 2 cuivres | calculé | V | 3× Quartzite + 2× Bûche azurée · Furniture_Bench |
| `Furniture_Temple_Emerald_Door` | Porte de temple abandonné |  |  | 5 | 5 cuivres | calculé | V | 2× [Quartzite ou pavés de quartzite] · Builders |
| `Furniture_Temple_Emerald_Ladder` | Échelle de temple abandonné |  |  | 1 | 1 cuivre | calculé | V | 1× [Quartzite ou pavés de quartzite] → ×2 · Builders |
| `Furniture_Temple_Emerald_Platform` | Plateforme de temple abandonné |  |  | 1 | 1 cuivre | calculé | V | 1× [Quartzite ou pavés de quartzite] → ×2 · Builders |
| `Furniture_Temple_Emerald_Shelf` | Étagère de temple abandonné |  |  | 3 | 3 cuivres | calculé | V | 1× [Quartzite ou pavés de quartzite] · Builders |
| `Furniture_Temple_Emerald_Sign` | Pancarte de temple abandonné |  |  | 3 | 3 cuivres | calculé | V | 1× [Quartzite ou pavés de quartzite] · Builders |
| `Furniture_Temple_Emerald_Statue_Head` | Statue de souverain ancien : tête |  | 21 | 600 | 6 or | calculé | V | 20× Pavés de quartzite + 3× Sceau des Embrasés · GoblinLab_Workbench |
| `Furniture_Temple_Emerald_Stool` | Tabouret de temple abandonné |  |  | 3 | 3 cuivres | calculé | V | 1× [Quartzite ou pavés de quartzite] · Builders |
| `Furniture_Temple_Emerald_Table` | Table de temple abandonné |  |  | 10 | 1 argent | calculé | V | 4× [Quartzite ou pavés de quartzite] · Builders |
| `Furniture_Temple_Emerald_Torch` | Torche de temple abandonné |  |  | 29 | 2 argent 9 cuivres | calculé | V | 1× Quartzite + 1× Pétales verts · Furniture_Bench |
| `Furniture_Temple_Emerald_Trapdoor` | Trappe de temple abandonné |  |  | 3 | 3 cuivres | calculé | V | 1× [Quartzite ou pavés de quartzite] · Builders |
| `Furniture_Temple_Emerald_Wardrobe` | Armoire de temple abandonné |  |  | 10 | 1 argent | calculé | V | 4× [Quartzite ou pavés de quartzite] · Builders |
| `Furniture_Temple_Emerald_Window` | Fenêtre de temple abandonné |  |  | 1 | 1 cuivre | calculé | V | 1× [Quartzite ou pavés de quartzite] → ×2 · Builders |
| `Furniture_Temple_Light_Brazier` | Brasero du temple de la lumière |  |  | 58 | 5 argent 8 cuivres | calculé | V | 3× Marbre + 2× Charbon de bois + 2× Pétales azurés · Furniture_Bench |
| `Furniture_Temple_Light_Chest_Small` | Petit coffre du temple de la lumière |  | 16 | 22 | 2 argent 2 cuivres | calculé | V | 3× Marbre + 1× [Schiste ou pavés de schiste] · Furniture_Bench |
| `Furniture_Temple_Light_Door` | Porte du temple de la lumière |  |  | 5 | 5 cuivres | calculé | V | 2× [Marbre ou pavés de marbre] · Builders |
| `Furniture_Temple_Light_Ladder` | Échelle du temple de la lumière |  |  | 1 | 1 cuivre | calculé | V | 1× [Marbre ou pavés de marbre] → ×2 · Builders |
| `Furniture_Temple_Light_Lantern` | Lanterne du temple de la lumière |  |  | 16 | 1 argent 6 cuivres | calculé | V | 1× Marbre + 1× [Schiste ou pavés de schiste] + 1× Pétales azurés · Furniture_Bench |
| `Furniture_Temple_Light_Shelf` | Étagère du temple de la lumière |  |  | 3 | 3 cuivres | calculé | V | 1× [Marbre ou pavés de marbre] · Builders |
| `Furniture_Temple_Light_Stool` | Tabouret du temple de la lumière |  |  | 3 | 3 cuivres | calculé | V | 1× [Marbre ou pavés de marbre] · Builders |
| `Furniture_Temple_Light_Table` | Table du temple de la lumière |  |  | 10 | 1 argent | calculé | V | 4× [Marbre ou pavés de marbre] · Builders |
| `Furniture_Temple_Light_Trapdoor` | Trappe du temple de la lumière |  |  | 3 | 3 cuivres | calculé | V | 1× [Marbre ou pavés de marbre] · Builders |
| `Furniture_Temple_Light_Wardrobe` | Armoire du temple de la lumière |  |  | 10 | 1 argent | calculé | V | 4× [Marbre ou pavés de marbre] · Builders |
| `Furniture_Temple_Light_Window` | Fenêtre du temple de la lumière |  |  | 1 | 1 cuivre | calculé | V | 1× [Marbre ou pavés de marbre] → ×2 · Builders |
| `Furniture_Temple_Wind_Candle` | Bougie du temple du vent |  |  | 140 | 1 or 4 argent | calculé | V | 1× Grès + 1× Argile cyan lisse + 1× Sève · Furniture_Bench |
| `Furniture_Temple_Wind_Chandelier` | Lustre du temple du vent |  |  | 200 | 2 or | calculé | V | 2× Corde + 1× Lingot de cuivre + 5× Sève · Furniture_Bench |
| `Furniture_Temple_Wind_Chest_Small` | Petit coffre du temple du vent |  | 15 | 22 | 2 argent 2 cuivres | calculé | V | 3× Grès + 1× Diamant · Furniture_Bench |
| `Furniture_Temple_Wind_Door` | Porte du temple du vent |  |  | 5 | 5 cuivres | calculé | V | 2× [Grès ou pavés de grès] · Builders |
| `Furniture_Temple_Wind_Ladder` | Échelle du temple du vent |  |  | 1 | 1 cuivre | calculé | V | 1× [Grès ou pavés de grès] → ×2 · Builders |
| `Furniture_Temple_Wind_Platform` | Plateforme du temple du vent |  |  | 1 | 1 cuivre | calculé | V | 1× [Grès ou pavés de grès] → ×2 · Builders |
| `Furniture_Temple_Wind_Sign` | Pancarte du temple du vent |  |  | 3 | 3 cuivres | calculé | V | 1× [Grès ou pavés de grès] · Builders |
| `Furniture_Temple_Wind_Stool` | Tabouret du temple du vent |  |  | 3 | 3 cuivres | calculé | V | 1× [Grès ou pavés de grès] · Builders |
| `Furniture_Temple_Wind_Table` | Table du temple du vent |  |  | 10 | 1 argent | calculé | V | 4× [Grès ou pavés de grès] · Builders |
| `Furniture_Temple_Wind_Trapdoor` | Trappe du temple du vent |  |  | 3 | 3 cuivres | calculé | V | 1× [Grès ou pavés de grès] · Builders |
| `Furniture_Temple_Wind_Wardrobe` | Armoire du temple du vent |  |  | 10 | 1 argent | calculé | V | 4× [Grès ou pavés de grès] · Builders |
| `Furniture_Temple_Wind_Window` | Fenêtre du temple du vent |  |  | 1 | 1 cuivre | calculé | V | 1× [Grès ou pavés de grès] → ×2 · Builders |
| `Furniture_Village_Chair` | Chaise en bois simple |  |  | 13 | 1 argent 3 cuivres | calculé | V | 2× [Bois dur] · Builders |
| `Furniture_Village_Chest_Small` | Petit coffre en fer |  | 5 | 540 | 5 or 4 argent | calculé | V | 3× Planches en bois dur + 2× Lingot de fer · Furniture_Bench |
| `Furniture_Village_Counter` | Comptoir en bois simple |  |  | 280 | 2 or 8 argent | calculé | V | 3× Planches en bois dur + 1× Lingot de fer · Furniture_Misc |
| `Furniture_Village_Crate` | Caisse en bois simple |  |  | 42 | 4 argent 2 cuivres | calculé | V | 2× Planches en bois dur + 2× Bâton · Furniture_Misc |
| `Furniture_Village_Door` | Porte en bois simple |  |  | 13 | 1 argent 3 cuivres | calculé | V | 2× [Bois dur] · Builders |
| `Furniture_Village_Ladder` | Échelle en bois simple |  |  | 3 | 3 cuivres | calculé | V | 1× [Bois dur] → ×2 · Builders |
| `Furniture_Village_Planter` | Jardinière en pierre |  | 1 | 400 | 4 or | calculé | V | 3× [N’importe quelle pierre] + 2× [N’importe quel sol] + 10× Essence de vie · Farmingbench |
| `Furniture_Village_Platform` | Plateforme en bois simple |  |  | 3 | 3 cuivres | calculé | V | 1× [Bois dur] → ×2 · Builders |
| `Furniture_Village_Shelf_Full` | Étagère en bois simple |  |  | 6 | 6 cuivres | calculé | V | 1× [Bois dur] · Builders |
| `Furniture_Village_Sign` | Pancarte en bois simple |  |  | 6 | 6 cuivres | calculé | V | 1× [Bois dur] · Builders |
| `Furniture_Village_Stool` | Tabouret en bois simple |  |  | 6 | 6 cuivres | calculé | V | 1× [Bois dur] · Builders |
| `Furniture_Village_Table` | Table en bois simple |  |  | 26 | 2 argent 6 cuivres | calculé | V | 4× [Bois dur] · Builders |
| `Furniture_Village_Trapdoor` | Trappe en bois simple |  |  | 6 | 6 cuivres | calculé | V | 1× [Bois dur] · Builders |
| `Furniture_Village_Wardrobe` | Armoire en bois simple |  |  | 26 | 2 argent 6 cuivres | calculé | V | 4× [Bois dur] · Builders |
| `Furniture_Village_Window` | Fenêtre en bois simple |  |  | 3 | 3 cuivres | calculé | V | 1× [Bois dur] → ×2 · Builders |
| `Goblin_Painting_Breach_Replica` | Tableau gobelin : Les brèches (réplique) | Commun | 35 | 2 900 | 2 diamants 9 or | calculé | V | 20× Ferraille + 10× Lingot de fer + 2× [N’importe quel type de champignon] · GoblinLab_Workbench |
| `Goblin_Painting_Fail_Replica` | Tableau gobelin : Les expéditions ratées (réplique) | Commun | 31 | 2 900 | 2 diamants 9 or | calculé | V | 20× Ferraille + 10× Lingot de fer + 2× [N’importe quel type de champignon] · GoblinLab_Workbench |
| `Goblin_Painting_ForgottenTemple_Replica` | Tableau gobelin : L’endroit étrange (réplique) | Commun | 33 | 330 | 3 or 3 argent | calculé | V | 20× Ferraille + 10× [N’importe quelles planches] + 2× [N’importe quel type de champignon] · GoblinLab_Workbench |
| `Goblin_Painting_Orbis_Replica` | Tableau gobelin : Le Voile du Néant (réplique) | Commun | 30 | 2 900 | 2 diamants 9 or | calculé | V | 20× Ferraille + 10× Lingot de fer + 2× [N’importe quel type de champignon] · GoblinLab_Workbench |
| `Goblin_Painting_Portal_Replica` | Tableau gobelin : Le gobelin itinérant (réplique) | Commun | 32 | 330 | 3 or 3 argent | calculé | V | 20× Ferraille + 10× [N’importe quelles planches] + 2× [N’importe quel type de champignon] · GoblinLab_Workbench |
| `Goblin_Painting_Repair_Replica` | Tableau gobelin : La réparation indispensable (réplique) | Commun | 34 | 330 | 3 or 3 argent | calculé | V | 20× Ferraille + 10× [N’importe quelles planches] + 2× [N’importe quel type de champignon] · GoblinLab_Workbench |
| `Halloween_Wagon` | Chariot d’Halloween |  |  | 330 | 3 or 3 argent | calculé | V | 1× Planches en bois sombre + 2× Dalle en bois sombre + 1× Lingot de fer · Furniture_Bench |
| `Ingredient_Hay` | Botte de foin |  | 2 | 32 | 3 argent 2 cuivres | calculé | V | 1× Corde + 4× Blé · Farmingbench |
| `Test` | Parchemin Décoratif |  |  | 78 | 7 argent 8 cuivres | calculé | E | 4× Fibre de plante + 2× Bâton · Furniture_Bench |
| `Tinkering_Target_Dummy` | Mannequin d’entraînement |  |  | 84 | 8 argent 4 cuivres | calculé | V | 1× [N’importe quelle bûche] + 3× Sève + 3× Bâton · Workbench |
| `Winter_Holiday_Wreath` | Couronne d’hiver |  |  | 130 | 1 or 3 argent | calculé | V | 6× Fibre de plante + 4× Pomme de pin + 2× Pétales rouges · Furniture_Bench |
| `WinterHoliday_Cane` | Sucre d’orge |  |  | 61 | 6 argent 1 cuivre | calculé | V | 1× Bâton + 2× Sève + 1× Pétales rouges · Furniture_Bench |
| `WinterHoliday_Garland` | Guirlande d’hiver |  |  | 91 | 9 argent 1 cuivre | calculé | V | 3× Fibre de plante + 3× Pomme de pin + 2× Pétales rouges · Furniture_Bench |
| `WinterHoliday_Lights` | Guirlande lumineuse |  |  | 86 | 8 argent 6 cuivres | calculé | V | 2× Fragments de cristal rouges + 2× Fragments de cristal verts + 2× Fragments de cristal bleus · Furniture_Bench |
| `WinterHoliday_Ornament_Bauble` | Boule de sapin |  |  | 42 | 4 argent 2 cuivres | calculé | V | 1× Fragments de cristal rouges + 1× Fragments de cristal jaunes + 1× Fibre de plante · Furniture_Bench |
| `WinterHoliday_Single_Light` | Boule lumineuse |  |  | 43 | 4 argent 3 cuivres | calculé | V | 3× Fragments de cristal jaunes · Furniture_Bench |
| `WinterHoliday_Snowflake` | Étoile de sapin en flocon de neige |  |  | 3 | 3 cuivres | calculé | V | 1× Neige · Furniture_Bench |
| `WinterHoliday_Snowman` | Bonhomme de neige |  |  | 95 | 9 argent 5 cuivres | calculé | V | 3× Neige + 2× Bâton + 3× Charbon de bois + 1× Pétales rouges · Furniture_Bench |
| `WinterHoliday_Stocking` | Chaussette d’hiver |  |  | 130 | 1 or 3 argent | calculé | V | 6× Chutes de lin + 1× Pétales rouges + 1× Pétales verts · Furniture_Bench |
| `WinterHoliday_WinterRoll` | Roulé d’hiver |  |  | 58 | 5 argent 8 cuivres | calculé | V | 4× Fibre de plante + 1× Pétales jaunes · Furniture_Bench |

<a id="fam-livres-histoires"></a>
## Livres & histoires

### Sans recette — prix fixés (49)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Eldanior_Histoire_Roi_Fou` | Chronique d'Eldanior : le Roi Fou | Peu commun |  | 150 | 1 or 5 argent | fixé | E |
| `Eldanior_Indice_Journal_Garde` | Journal d'un garde du palais | Commun |  | 50 | 5 argent | fixé | E |
| `Eldanior_Indice_Lettre_Reine` | Lettre de la reine (révélée à l'encre d'azur) | Commun |  | 50 | 5 argent | fixé | E |
| `Eldanior_Indice_Page_Grimoire` | Page arrachée du grimoire royal | Commun |  | 50 | 5 argent | fixé | E |
| `Eldanior_Indice_Sceau_Brise` | Feuillet déchiré : le sceau brisé | Commun |  | 50 | 5 argent | fixé | E |
| `Eldanior_Tome_01` | Tome I : La Guerre Divine | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Tome_02` | Tome II : Les Dons du Dragon | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Tome_03` | Tome III : L'Épreuve du Choix | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Tome_04` | Tome IV : La Fondation des Deux Rives | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Tome_05` | Tome V : La Longue Paix | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Tome_06` | Tome VI : La Mort du Premier Roi | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Tome_07` | Tome VII : La Nuit Écarlate | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Tome_08` | Tome VIII : Le Tombeau des Épreuves Divines | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Tome_09` | Tome IX : La Fureur du Dragon | Rare |  | 500 | 5 or | fixé | E |
| `Eldanior_Tome_10` | Tome X : La Mort du Roi Fou | Rare |  | 500 | 5 or | fixé | E |
| `Lore_Note_Died_Improperly` | Lore_Note_Died_Improperly | Commun |  | 50 | 5 argent | fixé | V |
| `Lore_Note_Enkindled_Journal_Rite` | Vieux journal d’Embrasés : Le rite | Commun |  | 50 | 5 argent | fixé | V |
| `Lore_Note_Enkindled_Journal_Vermin` | Vieux journal d’Embrasés : Vermine | Commun |  | 50 | 5 argent | fixé | V |
| `Lore_Note_Enkindled_Ledger_Trade` | Livre de comptes des Embrasés | Commun |  | 50 | 5 argent | fixé | V |
| `Lore_Note_Enkindled_Letter_Workshops` | Lettre d’un Embrasé : Les ateliers de guerre | Commun |  | 50 | 5 argent | fixé | V |
| `Lore_Note_Enkindled_Research_Gateways` | Lore_Note_Enkindled_Research_Gateways | Commun |  | 50 | 5 argent | fixé | V |
| `Lore_Note_Goblin_Commission_Abandoned_Parts` | Commande gobeline : Pièces abandonnées | Commun |  | 50 | 5 argent | fixé | V |
| `Lore_Note_Goblin_Feastmaster_Menu` | Menu du jour du Maître des festins | Commun |  | 50 | 5 argent | fixé | V |
| `Lore_Note_Goblin_Musings_Too_Heavy` | Lore_Note_Goblin_Musings_Too_Heavy | Commun |  | 50 | 5 argent | fixé | V |
| `Lore_Note_Goblin_Musings_Translocation` | Lore_Note_Goblin_Musings_Translocation | Commun |  | 50 | 5 argent | fixé | V |
| `Lore_Note_Goblin_Poem` | Lore_Note_Goblin_Poem | Commun |  | 50 | 5 argent | fixé | V |
| `Lore_Note_Goblin_Why_Purple` | Pourquoi violet ? | Commun |  | 50 | 5 argent | fixé | V |
| `Recipe_Book_Magic_Air` | Grimoire du vent |  |  | 50 | 5 argent | fixé | V |
| `Recipe_Book_Magic_Void` | Grimoire du Néant |  |  | 50 | 5 argent | fixé | V |
| `Recipe_Food_Pie_Apple` | Recette : {itemName} | Rare |  | 500 | 5 or | fixé | V |
| `Recipe_Food_Pie_Meat` | Recette : {itemName} | Rare |  | 500 | 5 or | fixé | V |
| `Recipe_Food_Pie_Pumpkin` | Recette : {itemName} | Rare |  | 500 | 5 or | fixé | V |
| `Recipe_Food_Salad_Caesar` | Recette : {itemName} | Rare |  | 500 | 5 or | fixé | V |
| `Recipe_Goblin_Painting_Breach_Replica` | Recette : {itemName} | Rare |  | 500 | 5 or | fixé | V |
| `Recipe_Goblin_Painting_Fail_Replica` | Recette : {itemName} | Rare |  | 500 | 5 or | fixé | V |
| `Recipe_Goblin_Painting_ForgottenTemple_Replica` | Recette : {itemName} | Rare |  | 500 | 5 or | fixé | V |
| `Recipe_Goblin_Painting_Orbis_Replica` | Recette : {itemName} | Rare |  | 500 | 5 or | fixé | V |
| `Recipe_Goblin_Painting_Portal_Replica` | Recette : {itemName} | Rare |  | 500 | 5 or | fixé | V |
| `Recipe_Goblin_Painting_Repair_Replica` | Recette : {itemName} | Rare |  | 500 | 5 or | fixé | V |
| `Recipe_Page` | Page de recette |  |  | 50 | 5 argent | fixé | V |
| `Recipe_Plant_Seeds_Health1` | Recette : {itemName} | Peu commun |  | 150 | 1 or 5 argent | fixé | V |
| `Recipe_Plant_Seeds_Health2` | Recette : {itemName} | Peu commun |  | 150 | 1 or 5 argent | fixé | V |
| `Recipe_Plant_Seeds_Health3` | Recette : {itemName} | Peu commun |  | 150 | 1 or 5 argent | fixé | V |
| `Recipe_Plant_Seeds_Mana1` | Recette : {itemName} | Peu commun |  | 150 | 1 or 5 argent | fixé | V |
| `Recipe_Plant_Seeds_Mana2` | Recette : {itemName} | Peu commun |  | 150 | 1 or 5 argent | fixé | V |
| `Recipe_Plant_Seeds_Mana3` | Recette : {itemName} | Peu commun |  | 150 | 1 or 5 argent | fixé | V |
| `Recipe_Plant_Seeds_Stamina1` | Recette : {itemName} | Peu commun |  | 150 | 1 or 5 argent | fixé | V |
| `Recipe_Plant_Seeds_Stamina2` | Recette : {itemName} | Peu commun |  | 150 | 1 or 5 argent | fixé | V |
| `Recipe_Plant_Seeds_Stamina3` | Recette : {itemName} | Peu commun |  | 150 | 1 or 5 argent | fixé | V |

<a id="fam-divers"></a>
## Divers

### Sans recette — prix fixés (97)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O |
|---|---|---|---:|---:|---|---|---|
| `Coffer_Key` | Clé du Voile | Épique | 51 | 3 000 | 3 diamants | fixé | V |
| `Dead_Fish` | Poisson mort |  |  | 50 | 5 argent | fixé | V |
| `Eldanior_Cle_Boss_A` | Clé de Boss - Rang A | Épique | 500 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Cle_Boss_CB` | Clé de Boss - Rang C-B | Rare | 250 | 1 000 | 1 diamant | fixé | E |
| `Eldanior_Cle_Boss_ED` | Clé de Boss - Rang E-D | Rare | 100 | 1 000 | 1 diamant | fixé | E |
| `Eldanior_Cle_Boss_S` | Clé de Boss - Rang S | Légendaire | 800 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Cle_Famille_Eldanior` | Clé du Trésor - Eldanior | Légendaire | 300 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Cle_Famille_Frostguard` | Clé du Trésor - Frostguard | Légendaire | 300 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Cle_Famille_Goldcrest` | Clé du Trésor - Goldcrest | Légendaire | 300 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Cle_Famille_Hanazakura` | Clé du Trésor - Hanazakura | Légendaire | 300 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Cle_Famille_Ironveil` | Clé du Trésor - Ironveil | Légendaire | 300 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Cle_Famille_Kage` | Clé du Trésor - Kage | Légendaire | 300 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Cle_Famille_Luo` | Clé du Trésor - Luo | Légendaire | 300 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Cle_Famille_Runkandel` | Clé du Trésor - Runkandel | Légendaire | 300 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Cle_Famille_Sama` | Clé du Trésor - Sama | Légendaire | 300 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Cle_Famille_Setsuka` | Clé du Trésor - Setsuka | Légendaire | 300 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Cle_Famille_Silkroad` | Clé du Trésor - Silkroad | Légendaire | 300 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Cle_Famille_Spellweave` | Clé du Trésor - Spellweave | Légendaire | 300 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Cle_Famille_Valmontis` | Clé du Trésor - Valmontis | Légendaire | 300 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Cle_Famille_Warbane` | Clé du Trésor - Warbane | Légendaire | 300 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Cle_Famille_Zippel` | Clé du Trésor - Zippel | Légendaire | 300 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Faille_Scellee_D` | Faille scellée (rang D) | Peu commun | 150 | 15 000 | 1 zénith 5 diamants | fixé | E |
| `Eldanior_Faille_Scellee_E` | Faille scellée (rang E) | Peu commun | 60 | 6 000 | 6 diamants | fixé | E |
| `Eldanior_Faille_Scellee_F` | Faille scellée (rang F) | Commun | 10 | 2 000 | 2 diamants | fixé | E |
| `Eldanior_Manuel_Luo_CroissantKi` | Manuel secret du clan Setsuka — Croissant de Ki | Rare | 32 | 660 | 6 or 6 argent | fixé | E |
| `Eldanior_Manuel_Luo_DomaineDivin` | Manuel secret de la voie martiale — Domaine Divin | Légendaire | 90 | 95 000 | 9 zéniths 5 diamants | fixé | E |
| `Eldanior_Manuel_Luo_DomaineEpee` | Manuel secret du clan Hanazakura — Domaine de l'Épée | Épique | 40 | 2 800 | 2 diamants 8 or | fixé | E |
| `Eldanior_Manuel_Luo_FleurPrunier` | Manuel secret du clan Hanazakura — Fleur de Prunier | Unique | 50 | 15 000 | 1 zénith 5 diamants | fixé | E |
| `Eldanior_Manuel_Luo_FrappeMeridiens` | Manuel secret du clan Sama — Frappe des Méridiens | Peu commun | 20 | 180 | 1 or 8 argent | fixé | E |
| `Eldanior_Manuel_Luo_GardeFleuve` | Manuel secret du clan Setsuka — Garde du Fleuve | Rare | 25 | 620 | 6 or 2 argent | fixé | E |
| `Eldanior_Manuel_Luo_IncarnationAme` | Manuel secret de la voie martiale — Incarnation de l'Âme | Légendaire | 80 | 90 000 | 9 zéniths | fixé | E |
| `Eldanior_Manuel_Luo_KiEpee` | Manuel secret de la voie martiale — Ki de l'Épée | Rare | 20 | 600 | 6 or | fixé | E |
| `Eldanior_Manuel_Luo_MeditationMartiale` | Manuel secret de la voie martiale — Méditation martiale | Rare | 20 | 600 | 6 or | fixé | E |
| `Eldanior_Manuel_Luo_PasLeger` | Manuel secret de la voie martiale — Pas léger | Rare | 40 | 700 | 7 or | fixé | E |
| `Eldanior_Manuel_Luo_PaumeKi` | Manuel secret du clan Sama — Paume de Ki | Commun | 12 | 56 | 5 argent 6 cuivres | fixé | E |
| `Eldanior_Manuel_Luo_ProjectionEpeeAura` | Manuel secret de la voie martiale — Projection d'épée d'Aura | Épique | 60 | 3 200 | 3 diamants 2 or | fixé | E |
| `Eldanior_Manuel_Luo_RenforcementCorps` | Manuel secret de la voie martiale — Renforcement corporel | Peu commun | 30 | 200 | 2 or | fixé | E |
| `Eldanior_Manuel_Luo_SensMartial` | Manuel secret de la voie martiale — Sens martial | Rare | 50 | 750 | 7 or 5 argent | fixé | E |
| `Eldanior_Manuel_Luo_TransformationHwakyung` | Manuel secret du clan Sama — Transformation Hwakyung | Légendaire | 60 | 80 000 | 8 zéniths | fixé | E |
| `Eldanior_Manuel_Luo_UniteCielTerre` | Manuel secret du clan Hanazakura — Unité du Ciel et de la Terre | Légendaire | 70 | 85 000 | 8 zéniths 5 diamants | fixé | E |
| `Eldanior_Manuel_Luo_VolEpeeKi` | Manuel secret de la voie martiale — Vol sur épée de Ki | Unique | 70 | 17 000 | 1 zénith 7 diamants | fixé | E |
| `Eldanior_Mouvement_Luo_01` | Manuel de la famille Luo — 1er Mouvement : Exécution Céleste | Rare | 20 | 600 | 6 or | fixé | E |
| `Eldanior_Mouvement_Luo_02` | Manuel de la famille Luo — 2e Mouvement | Rare | 30 | 650 | 6 or 5 argent | fixé | E |
| `Eldanior_Mouvement_Luo_03` | Manuel de la famille Luo — 3e Mouvement | Épique | 40 | 2 800 | 2 diamants 8 or | fixé | E |
| `Eldanior_Mouvement_Luo_04` | Manuel de la famille Luo — 4e Mouvement | Épique | 50 | 3 000 | 3 diamants | fixé | E |
| `Eldanior_Mouvement_Luo_05` | Manuel de la famille Luo — 5e Mouvement | Unique | 60 | 16 000 | 1 zénith 6 diamants | fixé | E |
| `Eldanior_Mouvement_Luo_06` | Manuel de la famille Luo — 6e Mouvement | Unique | 70 | 17 000 | 1 zénith 7 diamants | fixé | E |
| `Eldanior_Mouvement_Luo_07` | Manuel de la famille Luo — 7e Mouvement | Unique | 70 | 17 000 | 1 zénith 7 diamants | fixé | E |
| `Eldanior_Mouvement_Luo_08` | Manuel de la famille Luo — 8e Mouvement | Légendaire | 80 | 90 000 | 9 zéniths | fixé | E |
| `Eldanior_Mouvement_Luo_09` | Manuel de la famille Luo — 9e Mouvement | Légendaire | 80 | 90 000 | 9 zéniths | fixé | E |
| `Eldanior_Mouvement_Luo_10` | Manuel de la famille Luo — 10e Mouvement | Légendaire | 80 | 90 000 | 9 zéniths | fixé | E |
| `Eldanior_Mouvement_Luo_11` | Manuel de la famille Luo — 11e Mouvement | Légendaire | 90 | 95 000 | 9 zéniths 5 diamants | fixé | E |
| `Eldanior_Mouvement_Luo_12` | Manuel de la famille Luo — 12e Mouvement | Légendaire | 90 | 95 000 | 9 zéniths 5 diamants | fixé | E |
| `Eldanior_Oeuf_Chat` | Œuf de Chat | Commun | 5 | 52 | 5 argent 2 cuivres | fixé | E |
| `Eldanior_Oeuf_Cheval` | Œuf de Cheval | Peu commun | 10 | 160 | 1 or 6 argent | fixé | E |
| `Eldanior_Oeuf_Chien` | Œuf de Chien | Commun | 5 | 52 | 5 argent 2 cuivres | fixé | E |
| `Eldanior_Oeuf_Chouette` | Œuf de Chouette des neiges | Rare | 15 | 580 | 5 or 8 argent | fixé | E |
| `Eldanior_Oeuf_Corbeau` | Œuf de Corbeau | Commun | 5 | 52 | 5 argent 2 cuivres | fixé | E |
| `Eldanior_Oeuf_DragonFeu` | Œuf de Dragon de feu | Légendaire | 20 | 60 000 | 6 zéniths | fixé | E |
| `Eldanior_Oeuf_DragonGivre` | Œuf de Dragon de givre | Légendaire | 20 | 60 000 | 6 zéniths | fixé | E |
| `Eldanior_Oeuf_Faucon` | Œuf de Faucon | Peu commun | 10 | 160 | 1 or 6 argent | fixé | E |
| `Eldanior_Oeuf_Fenrir` | Œuf de Fenrir | Légendaire | 20 | 60 000 | 6 zéniths | fixé | E |
| `Eldanior_Oeuf_Leopard` | Œuf de Léopard des neiges | Rare | 15 | 580 | 5 or 8 argent | fixé | E |
| `Eldanior_Oeuf_Loup` | Œuf de Loup sombre | Peu commun | 10 | 160 | 1 or 6 argent | fixé | E |
| `Eldanior_Oeuf_LoupBlanc` | Œuf de Loup blanc | Rare | 15 | 580 | 5 or 8 argent | fixé | E |
| `Eldanior_Oeuf_Mystere` | Œuf mystérieux | Épique | 15 | 2 300 | 2 diamants 3 or | fixé | E |
| `Eldanior_Oeuf_Ours` | Œuf de Ours brun | Rare | 15 | 580 | 5 or 8 argent | fixé | E |
| `Eldanior_Oeuf_OursPolaire` | Œuf de Ours polaire | Rare | 15 | 580 | 5 or 8 argent | fixé | E |
| `Eldanior_Oeuf_Pterodactyle` | Œuf de Ptérodactyle | Épique | 18 | 2 400 | 2 diamants 4 or | fixé | E |
| `Eldanior_Oeuf_Raptor` | Œuf de Raptor des cavernes | Épique | 18 | 2 400 | 2 diamants 4 or | fixé | E |
| `Eldanior_Oeuf_Renard` | Œuf de Renard roux | Commun | 5 | 52 | 5 argent 2 cuivres | fixé | E |
| `Eldanior_Oeuf_Tigre` | Œuf de Tigre à dents de sabre | Épique | 18 | 2 400 | 2 diamants 4 or | fixé | E |
| `Eldanior_Parchemin_Voie_Martiale` | Rouleau de la Voie Martiale | Épique | 40 | 2 800 | 2 diamants 8 or | fixé | E |
| `Eldanior_Plaque_Luo_Clan_Hanazakura` | Plaque d'identification — Clan Hanazakura | Rare | 80 | 900 | 9 or | fixé | E |
| `Eldanior_Plaque_Luo_Clan_Kage` | Plaque d'identification — Clan Kage | Rare | 80 | 900 | 9 or | fixé | E |
| `Eldanior_Plaque_Luo_Clan_Sama` | Plaque d'identification — Clan Sama | Rare | 80 | 900 | 9 or | fixé | E |
| `Eldanior_Plaque_Luo_Clan_Setsuka` | Plaque d'identification — Clan Setsuka | Rare | 80 | 900 | 9 or | fixé | E |
| `Eldanior_Plaque_Luo_Doyen` | Plaque d'identification — Doyen | Unique | 400 | 20 000 | 2 zéniths | fixé | E |
| `Eldanior_Plaque_Luo_Marchand` | Plaque d'identification — Marchand agréé du Comptoir | Rare | 60 | 800 | 8 or | fixé | E |
| `Eldanior_Plaque_Luo_Noble` | Plaque d'identification — Noble | Rare | 100 | 1 000 | 1 diamant | fixé | E |
| `Eldanior_Plaque_Luo_Patriarche` | Plaque d'identification — Patriarche | Légendaire | 550 | 100 000 | 1 astrale | fixé | E |
| `Eldanior_Plaque_Luo_Recrue` | Plaque d'identification — Recrue | Commun | 20 | 60 | 6 argent | fixé | E |
| `Eldanior_Plaque_Luo_Seigneur` | Plaque d'identification — Seigneur | Épique | 250 | 4 000 | 4 diamants | fixé | E |
| `Eldanior_Plaque_Luo_Visiteur` | Plaque d'identification — Visiteur (laissez-passer) | Peu commun | 20 | 500 | 5 or | fixé | E |
| `Eldanior_Poche_Dimensionnelle` | Poche dimensionnelle | Épique | 1 | 10 000 000 | 10 impériales | fixé | E |
| `Kweebec_Ancient` | Vénérable Kweebec |  |  | 50 | 5 argent | fixé | V |
| `Luo_Onguent_Cerisier` | Onguent de fleur de cerisier | Rare | 80 | 900 | 9 or | fixé | E |
| `Luo_Onguent_Dragon` | Onguent du Dragon d'Or | Unique | 380 | 20 000 | 2 zéniths | fixé | E |
| `Luo_The_Eclipse` | Thé noir de l'Éclipse | Légendaire | 500 | 100 000 | 1 astrale | fixé | E |
| `Luo_The_Jade` | Thé de jade impérial | Épique | 200 | 4 000 | 4 diamants | fixé | E |
| `Luo_The_Jasmin` | Thé au jasmin des Mille Cerisiers | Rare | 90 | 950 | 9 or 5 argent | fixé | E |
| `Luo_The_Vert` | Thé vert de la vallée | Commun | 20 | 60 | 6 argent | fixé | E |
| `Media_RemoteControl` | Télécommande multimédia |  |  | 50 | 5 argent | fixé | V |
| `PortalKey_Henges` | Fragment : Orbis - Henges silencieux |  | 51 | 76 | 7 argent 6 cuivres | fixé | V |
| `PortalKey_Jungles` | Fragment : Orbis - Refuge de Tal’Kesi |  | 51 | 76 | 7 argent 6 cuivres | fixé | V |
| `Vehicle_Wood_Boat` | Barque en bois |  |  | 50 | 5 argent | fixé | V |
| `Vehicle_Wood_Raft` | Radeau en bois |  |  | 50 | 5 argent | fixé | V |

### Avec recette — prix calculés (9)

| ID | Nom | Qualité | Niv. | Prix | En pièces | Source | O  Recette |
|---|---|---|---:|---:|---|---|---|---|
| `Bandage_Crude` | Bandage | Commun |  | 120 | 1 or 2 argent | calculé | V | 6× Fibre de plante + 3× Sève |
| `Eldanior_Affiche_Propriete` | Affiche de propriété | Peu commun |  | 14 | 1 argent 4 cuivres | calculé | E | 1× Chutes de coton · Builders |
| `Portal_Device` | Portail antique | Rare | 50 | 7 600 | 7 diamants 6 or | calculé | V | 30× Pavés de pierre + 10× Lingot de thorium + 10× Essence de vie + 10× Essence du Néant · Arcanebench |
| `PortalKey_Dungeon_V1` | Clé du DONJON D'ENTRAINEMENT | Peu commun | 51 | 17 000 | 1 zénith 7 diamants | calculé | E | 10× Lingot d’adamantite + 15× Essence de feu · Arcanebench |
| `PortalKey_Goblin_Breach` | Fragment : Orbis - Les Antres Vacillants | Rare | 51 | 1 900 | 1 diamant 9 or | calculé | V | 15× Ferraille + 10× Essence du Néant + 5× Lingot de fer · GoblinLab_Workbench |
| `PortalKey_Hederas_Lair` | Fragment : Orbis - Le Gué de l’effroi | Peu commun | 51 | 18 000 | 1 zénith 8 diamants | calculé | V | 10× Lingot d’adamantite + 30× Essence de feu · Arcanebench |
| `PortalKey_Taiga` | Fragment : Orbis - Bois de la Flèche du dragon | Peu commun | 51 | 11 000 | 1 zénith 1 diamant | calculé | V | 10× Lingot de cobalt + 30× Essence de glace · Arcanebench |
| `PortalKey_The_Deepworks` | Fragment : Orbis - Les Ateliers du Bas-Fond | Légendaire | 60 | 1 500 | 1 diamant 5 or | calculé | V | 30× Ferraille + 5× Fragments de cristal du Néant + 1× Cœur du Néant · GoblinLab_Workbench |
| `PortalKey_Windsurf_Valley` | Fragment : Orbis - Vallée des chevauchevents | Peu commun | 51 | 7 900 | 7 diamants 9 or | calculé | V | 10× Lingot de thorium + 30× Essence de vie · Arcanebench |

<a id="fam-types-de-ressource"></a>
## Types de ressource

| Type | Nom | Prix | Objet le moins cher | Nb d'objets |
|---|---|---:|---|---:|
| `Bone` | Os | 43 | `Deco_Bone_Skulls_Feran` | 14 |
| `Books` | Livres | 50 | `Recipe_Book_Magic_Air` | 2 |
| `Bricks` | N’importe quelle brique | 2 | `Soil_Hive_Brick` | 27 |
| `Charcoal` | Charbon de bois | 1 | `Furniture_Temple_Emerald_Window` | 410 |
| `Clays` | Argiles | 2 | `Soil_Clay_Beige` | 32 |
| `Copper_Iron_Bar` | Barres de cuivre ou de fer | 300 | `Ingredient_Bar_Bronze` | 1 |
| `Crystal_Shards` | N’importe quel fragment de cristal | 11 | `Ingredient_Crystal_White` | 9 |
| `Fish` | N’importe quel poisson | 10 | `Fish_Bluegill_Item` | 67 |
| `Fish_Common` | N’importe quel poisson commun | 10 | `Fish_Bluegill_Item` | 3 |
| `Fish_Epic` | N’importe quel poisson épique | 500 | `Fish_Jellyfish_Yellow_Item` | 5 |
| `Fish_Legendary` | N’importe quel poisson légendaire | 10 | `*Fish_Bluegill_Item_State_Legendary` | 30 |
| `Fish_Rare` | N’importe quel poisson rare | 10 | `*Fish_Bluegill_Item_State_Rare` | 22 |
| `Fish_Uncommon` | N’importe quel poisson peu commun | 10 | `*Fish_Bluegill_Item_State_Uncommon` | 7 |
| `Flowers` | N’importe quelle fleur | 5 | `Plant_Flower_Common_Cyan2` | 66 |
| `Foods` | Foods | 0 | `Food_Cheese` | 28 |
| `Fruits` | N’importe quel type de fruit | 3 | `Plant_Fruit_Windwillow` | 9 |
| `Fuel` | N’importe quel combustible | 1 | `Furniture_Temple_Emerald_Window` | 413 |
| `Ice` | Glace | 2 | `Rock_Ice_Icicles` | 7 |
| `Meats` | N’importe quel type de viande crue | 11 | `Food_Chicken_Raw` | 4 |
| `Metal_Bars` | N’importe quelles barres de métal | 78 | `Ingredient_Bar_Copper` | 11 |
| `Milk_Bucket` | N’importe quel seau de lait | 50 | `*Deco_Bucket_State_Filled_Milk` | 2 |
| `Milk_Mosshorn_Bucket` | N’importe quel seau de lait de Cornemousse | 50 | `*Deco_Bucket_State_Filled_Mosshorn_Milk` | 2 |
| `Moss` | N’importe quelle mousse | 5 | `Plant_Moss_Yellow` | 5 |
| `Mushrooms` | N’importe quel type de champignon | 3 | `Plant_Crop_Mushroom_Glowing_Purple` | 45 |
| `Plant_Red` | Matière végétale rouge | 5 | `Plant_Bush_Winter_Red` | 5 |
| `Rock` | N’importe quelle pierre | 1 | `Rock_Ledge_Cobble_Half` | 124 |
| `Rock_Aqua` | Pierre marine ou pavés marins | 2 | `Rock_Aqua_Cobble` | 3 |
| `Rock_Aqua_Brick` | Brique marine | 2 | `Rock_Aqua_Brick_Smooth_Half` | 5 |
| `Rock_Basalt` | Basalte ou pavés de basalte | 2 | `Rock_Basalt_Cobble` | 4 |
| `Rock_Basalt_Brick` | Brique de basalte | 3 | `Rock_Basalt_Brick_Ornate` | 4 |
| `Rock_Calcite` | Calcite ou pavés de calcite | 2 | `Rock_Calcite_Cobble` | 2 |
| `Rock_Calcite_Brick` | Brique de calcite | 3 | `Rock_Calcite_Brick_Ornate` | 4 |
| `Rock_Chalk` | Craie ou pavés de craie | 2 | `Rock_Chalk` | 2 |
| `Rock_Chalk_Brick` | Brique de craie | 2 | `Rock_Chalk_Brick` | 4 |
| `Rock_Gold_Brick` | Brique d’or | 840 | `Rock_Gold_Brick` | 4 |
| `Rock_Ledge` | Pierre stratifiée ou pavés de pierre stratifiée | 2 | `Rock_Ledge_Cobble` | 1 |
| `Rock_Ledge_Brick` | Brique de pierre stratifiée | 3 | `Rock_Ledge_Brick_Ornate` | 4 |
| `Rock_Lime` | Calcaire ou pavés de calcaire | 2 | `Rock_Lime_Cobble` | 2 |
| `Rock_Lime_Brick` | Brique de calcaire | 3 | `Rock_Lime_Brick` | 4 |
| `Rock_Magma_Cooled` | Rock_Magma_Cooled | 2 | `Rock_Magma_Cooled_Cobble` | 4 |
| `Rock_Magma_Cooled_Brick` | Rock_Magma_Cooled_Brick | 3 | `Rock_Magma_Cooled_Brick_Ornate` | 4 |
| `Rock_Marble` | Marbre ou pavés de marbre | 2 | `Rock_Marble_Cobble` | 2 |
| `Rock_Marble_Brick` | Brique de marbre | 3 | `Rock_Marble_Brick_Decorative` | 4 |
| `Rock_Peach` | Pierre pêche ou pavés de pierre pêche | 2 | `Rock_Peach_Cobble` | 1 |
| `Rock_Peach_Brick` | Brique de pierre pêche | 3 | `Rock_Peach_Brick` | 1 |
| `Rock_Quartzite` | Quartzite ou pavés de quartzite | 2 | `Rock_Quartzite_Cobble` | 2 |
| `Rock_Quartzite_Brick` | Brique de quartzite | 3 | `Rock_Quartzite_Brick_Ornate` | 4 |
| `Rock_Sandstone` | Grès ou pavés de grès | 2 | `Rock_Sandstone_Cobble` | 2 |
| `Rock_Sandstone_Brick` | Brique de grès | 3 | `Rock_Sandstone_Brick_Decorative` | 4 |
| `Rock_Sandstone_Red` | Grès rouge ou pavés de grès rouge | 2 | `Rock_Sandstone_Red_Cobble` | 2 |
| `Rock_Sandstone_Red_Brick` | Brique de grès rouge | 3 | `Rock_Sandstone_Red_Brick_Smooth` | 4 |
| `Rock_Sandstone_White` | Grès blanc ou pavés de grès blanc | 2 | `Rock_Sandstone_White_Cobble` | 2 |
| `Rock_Sandstone_White_Brick` | Brique de grès blanc | 3 | `Rock_Sandstone_White_Brick_Decorative` | 4 |
| `Rock_Shale` | Schiste ou pavés de schiste | 2 | `Rock_Shale_Cobble` | 3 |
| `Rock_Shale_Brick` | Brique de schiste | 3 | `Rock_Shale_Brick_Smooth` | 4 |
| `Rock_Slate` | Ardoise ou pavés d’ardoise | 2 | `Rock_Slate_Cobble` | 4 |
| `Rock_Slate_Brick` | Brique d’ardoise | 3 | `Rock_Slate_Brick_Decorative` | 4 |
| `Rock_Stone` | Pierre ou pavés de pierre | 2 | `Rock_Stone_Cobble_Mossy` | 5 |
| `Rock_Stone_Brick` | Brique de pierre | 3 | `Rock_Stone_Brick_Mossy` | 5 |
| `Rock_Stone_Mossy` | Pierre moussue ou pavés de pierre moussue | 2 | `Rock_Stone_Cobble_Mossy` | 2 |
| `Rock_Volcanic` | Pierre volcanique ou pavés volcaniques | 2 | `Rock_Volcanic_Cracked_Incandescent` | 5 |
| `Rock_Volcanic_Brick` | Brique volcanique | 3 | `Rock_Volcanic_Brick_Ornate` | 4 |
| `Rubble` | N’importe quels gravats | 2 | `Rubble_Sandstone` | 32 |
| `Sands` | N’importe quel sable | 2 | `Soil_Sand_White` | 4 |
| `Soil_Clay_Brick` | Brique d’argile | 3 | `Soil_Clay_Brick` | 1 |
| `Soil_Clay_Ocean_Brick` | Brique d’argile océanique | 3 | `Soil_Clay_Ocean_Brick_Ornate` | 4 |
| `Soil_Hive_Brick` | Brique de ruche | 2 | `Soil_Hive_Brick` | 2 |
| `Soil_Hive_Corrupted_Brick` | Brique de ruche corrompue | 2 | `Soil_Hive_Corrupted_Brick` | 2 |
| `Soil_Snow_Brick` | Brique de neige | 5 | `Soil_Snow_Brick` | 1 |
| `Soils` | N’importe quel sol | 2 | `Soil_Dirt_Wet` | 8 |
| `Vegetables` | N’importe quel type de légume | 3 | `Plant_Crop_Mushroom_Glowing_Purple` | 50 |
| `Wood_All` | N’importe quel bois | 3 | `Wood_Hardwood_Planks_Half` | 86 |
| `Wood_Amber` | Wood_Amber | 20 | `Wood_Amber_Trunk_Full` | 2 |
| `Wood_Apple` | Wood_Apple | 20 | `Wood_Apple_Trunk` | 1 |
| `Wood_Ash` | Wood_Ash | 20 | `Wood_Ash_Trunk` | 2 |
| `Wood_Aspen` | Wood_Aspen | 20 | `Wood_Aspen_Trunk` | 2 |
| `Wood_Azure` | Wood_Azure | 20 | `Wood_Azure_Trunk_Full` | 2 |
| `Wood_Bamboo` | Wood_Bamboo | 20 | `Wood_Bamboo_Trunk_Deco` | 4 |
| `Wood_Banyan` | Wood_Banyan | 20 | `Wood_Banyan_Trunk` | 2 |
| `Wood_Beech` | Wood_Beech | 20 | `Wood_Beech_Trunk_Full` | 2 |
| `Wood_Birch` | Wood_Birch | 20 | `Wood_Birch_Trunk_Full` | 2 |
| `Wood_Blackwood` | Bois noir | 20 | `Wood_Fire_Trunk_Full` | 7 |
| `Wood_Blackwood_Deadwood` | Bois noir ou bois mort | 26 | `Wood_Blackwood_Decorative` | 6 |
| `Wood_Blackwood_Trunk` | Bûche de bois noir | 20 | `Wood_Fire_Trunk_Full` | 4 |
| `Wood_Bottletree` | Wood_Bottletree | 20 | `Wood_Bottletree_Trunk_Full` | 2 |
| `Wood_Burnt` | Wood_Burnt | 20 | `Wood_Burnt_Trunk` | 2 |
| `Wood_Camphor` | Wood_Camphor | 20 | `Wood_Camphor_Trunk_Full` | 2 |
| `Wood_Cedar` | Wood_Cedar | 20 | `Wood_Cedar_Trunk_Full` | 2 |
| `Wood_Crystal` | Wood_Crystal | 20 | `Wood_Crystal_Trunk_Full` | 2 |
| `Wood_Darkwood` | Bois sombre | 20 | `Wood_Cedar_Trunk_Full` | 5 |
| `Wood_Darkwood_Trunk` | Bûche de bois sombre | 20 | `Wood_Cedar_Trunk_Full` | 2 |
| `Wood_Deadwood` | Bois mort | 20 | `Wood_Petrified_Trunk` | 5 |
| `Wood_Deadwood_Trunk` | Bûche de bois mort | 20 | `Wood_Petrified_Trunk` | 2 |
| `Wood_Dry` | Wood_Dry | 20 | `Wood_Dry_Trunk_Full` | 2 |
| `Wood_Drywood` | Bois sec | 20 | `Wood_Dry_Trunk_Full` | 7 |
| `Wood_Drywood_Goldenwood` | Planches de bois sec ou de bois doré | 26 | `Wood_Goldenwood_Ornate` | 6 |
| `Wood_Drywood_Trunk` | Bûche de bois sec | 20 | `Wood_Dry_Trunk_Full` | 4 |
| `Wood_Fig_Blue` | Wood_Fig_Blue | 20 | `Wood_Fig_Blue_Trunk` | 2 |
| `Wood_Fir` | Wood_Fir | 20 | `Wood_Fir_Trunk` | 2 |
| `Wood_Fire` | Wood_Fire | 20 | `Wood_Fire_Trunk_Full` | 2 |
| `Wood_Goldenwood` | Bois doré | 20 | `Wood_Wisteria_Wild_Trunk` | 11 |
| `Wood_Goldenwood_Trunk` | Bûche de bois doré | 20 | `Wood_Wisteria_Wild_Trunk` | 8 |
| `Wood_Greenwood` | Bois vert | 20 | `Wood_Palo_Trunk_Full` | 7 |
| `Wood_Greenwood_Trunk` | Bûche de bois vert | 20 | `Wood_Palo_Trunk_Full` | 4 |
| `Wood_Gumboab` | Wood_Gumboab | 20 | `Wood_Gumboab_Trunk` | 2 |
| `Wood_Hardwood` | Bois dur | 5 | `Plant_Fern_Trunk` | 14 |
| `Wood_Hardwood_Trunk` | Bûche de bois dur | 5 | `Plant_Fern_Trunk` | 11 |
| `Wood_Ice` | Bois gelé | 20 | `Wood_Ice_Trunk_Full` | 2 |
| `Wood_Jungle` | Wood_Jungle | 20 | `Wood_Jungle_Trunk_Full` | 2 |
| `Wood_Lightwood` | Bois clair | 20 | `Wood_Camphor_Trunk_Full` | 11 |
| `Wood_Lightwood_Softwood` | Bois clair ou bois tendre | 26 | `Wood_Softwood_Ornate` | 6 |
| `Wood_Lightwood_Trunk` | Bûche de bois clair | 20 | `Wood_Camphor_Trunk_Full` | 8 |
| `Wood_Maple` | Wood_Maple | 20 | `Wood_Maple_Trunk` | 2 |
| `Wood_Oak` | Wood_Oak | 5 | `Plant_Fern_Trunk` | 6 |
| `Wood_Palm` | Wood_Palm | 20 | `Wood_Palm_Trunk_Full` | 2 |
| `Wood_Palo` | Wood_Palo | 20 | `Wood_Palo_Trunk_Full` | 2 |
| `Wood_Petrified` | Wood_Petrified | 20 | `Wood_Petrified_Trunk` | 2 |
| `Wood_Planks` | N’importe quelles planches | 3 | `Wood_Hardwood_Planks_Half` | 44 |
| `Wood_Poisoned` | Wood_Poisoned | 20 | `Wood_Poisoned_Trunk_Full` | 2 |
| `Wood_Redwood` | Bois rouge | 20 | `Wood_Maple_Trunk` | 9 |
| `Wood_Redwood_Trunk` | Bûche de bois rouge | 20 | `Wood_Maple_Trunk` | 6 |
| `Wood_Sadwillow` | Wood_Sadwillow | 20 | `Wood_Sadwillow_Trunk` | 2 |
| `Wood_Sallow` | Wood_Sallow | 20 | `Wood_Sallow_Trunk_Full` | 2 |
| `Wood_Softwood` | Bois tendre | 20 | `Wood_Aspen_Trunk` | 9 |
| `Wood_Softwood_Trunk` | Bûche de bois tendre | 20 | `Wood_Aspen_Trunk` | 6 |
| `Wood_Spiral` | Wood_Spiral | 20 | `Wood_Spiral_Trunk_Full` | 2 |
| `Wood_Stormbark` | Wood_Stormbark | 20 | `Wood_Stormbark_Trunk` | 2 |
| `Wood_Tropicalwood` | Bois tropical | 20 | `Wood_Bamboo_Trunk_Deco` | 7 |
| `Wood_Tropicalwood_Trunk` | Bûche de bois tropical | 20 | `Wood_Bamboo_Trunk_Deco` | 4 |
| `Wood_Trunk` | N’importe quelle bûche | 5 | `Plant_Fern_Trunk` | 72 |
| `Wood_Windwillow` | Wood_Windwillow | 20 | `Wood_Windwillow_Trunk` | 2 |
| `Wood_Wisteria_Wild` | Wood_Wisteria_Wild | 20 | `Wood_Wisteria_Wild_Trunk` | 2 |

<a id="fam-hors-economie"></a>
## Hors économie

Objets techniques (debug, éditeur, effets visuels, prototypes, gabarits) : listés pour être complet, jamais vendus.

| ID | Nom | Raison | O |
|---|---|---|---|
| `Armor_QA_Chest` | Cuirasse QA - PV | qualité Debug | V |
| `Armor_QA_Hands` | Gants QA - Énergie signature | qualité Debug | V |
| `Armor_QA_Legs` | Jambières QA - Énergie endurance | qualité Debug | V |
| `Armor_Trooper_Chest` | Cuirasse de soldat | qualité Developer | V |
| `Armor_Trooper_Head` | Casque de soldat | qualité Developer | V |
| `Armor_Trooper_Legs` | Pantalon de soldat | qualité Developer | V |
| `Artefact_Canon_Main` | Canon de char à main (Blue Tiger) | forme temporaire d'une arme (transformation, baïonnette) | E |
| `Artefact_Handgun_Aigles_Desert` | Colts de cavalerie (Green Colt) | forme temporaire d'une arme (transformation, baïonnette) | E |
| `Artefact_Mousquet_BlackBess_Baionnette` | Black Bess, l'Arcane Sniper | forme temporaire d'une arme (transformation, baïonnette) | E |
| `Artefact_Mousquet_BlueTiger_Baionnette` | Blue Tiger, le Perceur | forme temporaire d'une arme (transformation, baïonnette) | E |
| `Artefact_Mousquet_GreenColt_Baionnette` | Green Colt, le Rapide | forme temporaire d'une arme (transformation, baïonnette) | E |
| `Artefact_Sniper_SARS` | SARS militaire (Black Bess) | forme temporaire d'une arme (transformation, baïonnette) | E |
| `Bandage_Potion_Test` | Test : potion de bandage | qualité Developer | V |
| `Barrier` | Bloc barrière | dossier Hypixel | V |
| `Bench_Armory` | Forge | qualité Developer | V |
| `Bench_Class_System` | Banc des Souvenirs | qualité Technical | E |
| `Bench_Loom` | Métier à tisser | qualité Developer | V |
| `Bench_Memories` | Banc des Souvenirs | qualité Technical | V |
| `Block_Spawner_Block` | Générateur de blocs | qualité Technical | V |
| `Block_Spawner_Block_Large` | Grand générateur de blocs | qualité Technical | V |
| `Build_Black_Cube` | Cube de construction noir | qualité Technical | V |
| `Build_Black_Fence` | Clôture de construction noire | qualité Technical | V |
| `Build_Black_Half` | Dalle de construction noire | qualité Technical | V |
| `Build_Black_Pipe` | Tuyau de construction noir | qualité Technical | V |
| `Build_Black_Pipe_Corner` | Tuyau de construction noir : angle | qualité Technical | V |
| `Build_Black_Roof` | Toit de construction noir | qualité Technical | V |
| `Build_Black_Roof_Flat` | Toit de construction noir : plat | qualité Technical | V |
| `Build_Black_Roof_Shallow` | Toit de construction noir : faible pente | qualité Technical | V |
| `Build_Black_Roof_Steep` | Toit de construction noir : forte pente | qualité Technical | V |
| `Build_Black_Stairs` | Escalier de construction noir | qualité Technical | V |
| `Build_Grey_Cube` | Cube de construction gris | qualité Technical | V |
| `Build_Grey_Fence` | Clôture de construction grise | qualité Technical | V |
| `Build_Grey_Half` | Dalle de construction grise | qualité Technical | V |
| `Build_Grey_Pipe` | Tuyau de construction gris | qualité Technical | V |
| `Build_Grey_Pipe_Corner` | Tuyau de construction gris : angle | qualité Technical | V |
| `Build_Grey_Roof` | Toit de construction gris | qualité Technical | V |
| `Build_Grey_Roof_Flat` | Toit de construction gris : plat | qualité Technical | V |
| `Build_Grey_Roof_Shallow` | Toit de construction gris : faible pente | qualité Technical | V |
| `Build_Grey_Roof_Steep` | Toit de construction gris : forte pente | qualité Technical | V |
| `Build_Grey_Stairs` | Escalier de construction gris | qualité Technical | V |
| `Build_GreyDark_Cube` | Cube de construction gris foncé | qualité Technical | V |
| `Build_GreyDark_Fence` | Clôture de construction gris foncé | qualité Technical | V |
| `Build_GreyDark_Half` | Dalle de construction gris foncé | qualité Technical | V |
| `Build_GreyDark_Pipe` | Tuyau de construction gris foncé | qualité Technical | V |
| `Build_GreyDark_Pipe_Corner` | Tuyau de construction gris foncé : angle | qualité Technical | V |
| `Build_GreyDark_Roof` | Toit de construction gris foncé | qualité Technical | V |
| `Build_GreyDark_Roof_Flat` | Toit de construction gris foncé : plat | qualité Technical | V |
| `Build_GreyDark_Roof_Shallow` | Toit de construction gris foncé : faible pente | qualité Technical | V |
| `Build_GreyDark_Roof_Steep` | Toit de construction gris foncé : forte pente | qualité Technical | V |
| `Build_GreyDark_Stairs` | Escalier de construction gris foncé | qualité Technical | V |
| `Build_GreyLight_Cube` | Cube de construction gris clair | qualité Technical | V |
| `Build_GreyLight_Fence` | Clôture de construction gris clair | qualité Technical | V |
| `Build_GreyLight_Half` | Dalle de construction gris clair | qualité Technical | V |
| `Build_GreyLight_Pipe` | Tuyau de construction gris clair | qualité Technical | V |
| `Build_GreyLight_Pipe_Corner` | Tuyau de construction gris clair : angle | qualité Technical | V |
| `Build_GreyLight_Roof` | Toit de construction gris clair | qualité Technical | V |
| `Build_GreyLight_Roof_Flat` | Toit de construction gris clair : plat | qualité Technical | V |
| `Build_GreyLight_Roof_Shallow` | Toit de construction gris clair : faible pente | qualité Technical | V |
| `Build_GreyLight_Roof_Steep` | Toit de construction gris clair : forte pente | qualité Technical | V |
| `Build_GreyLight_Stairs` | Escalier de construction gris clair | qualité Technical | V |
| `Build_Lightsource_Blue` | Source de lumière de construction bleue | qualité Technical | V |
| `Build_Lightsource_Cyan` | Source de lumière de construction cyan | qualité Technical | V |
| `Build_Lightsource_Green` | Source de lumière de construction verte | qualité Technical | V |
| `Build_Lightsource_Orange` | Source de lumière de construction orange | qualité Technical | V |
| `Build_Lightsource_Pink` | Source de lumière de construction rose | qualité Technical | V |
| `Build_Lightsource_Red` | Source de lumière de construction rouge | qualité Technical | V |
| `Build_Lightsource_White` | Source de lumière de construction blanche | qualité Technical | V |
| `Build_Lightsource_Yellow` | Source de lumière de construction jaune | qualité Technical | V |
| `Build_Lightsource_YellowLight` | Source de lumière de construction jaune clair | qualité Technical | V |
| `Build_White_Cube` | Cube de construction blanc | qualité Technical | V |
| `Build_White_Fence` | Clôture de construction blanche | qualité Technical | V |
| `Build_White_Half` | Dalle de construction blanche | qualité Technical | V |
| `Build_White_Pipe` | Tuyau de construction blanc | qualité Technical | V |
| `Build_White_Pipe_Corner` | Tuyau de construction blanc : angle | qualité Technical | V |
| `Build_White_Roof` | Toit de construction blanc | qualité Technical | V |
| `Build_White_Roof_Flat` | Toit de construction blanc : plat | qualité Technical | V |
| `Build_White_Roof_Shallow` | Toit de construction blanc : faible pente | qualité Technical | V |
| `Build_White_Roof_Steep` | Toit de construction blanc : forte pente | qualité Technical | V |
| `Build_White_Stairs` | Escalier de construction blanc | qualité Technical | V |
| `Cocoon_Spider_Cave` | Cocon d’araignée des cavernes | qualité Developer | V |
| `Debug_Armor_D_Chest_Of_Damage_Reduction` | Debug : cuirasse en cuir de réduction des dégâts | gabarit / outil de développement | V |
| `Debug_Armor_D_Hands_Of_Regeneration` | Debug : gantelets en cuir de régénération | gabarit / outil de développement | V |
| `Debug_Armor_D_Head_Of_Dash_Reduction` | Debug : casque en cuir de réduction du coût de ruée | gabarit / outil de développement | V |
| `Debug_Armor_D_Legs_Of_Knockback_Reduction` | Debug : pantalon en cuir de réduction du recul | gabarit / outil de développement | V |
| `Debug_Armor_Leather_Light_Chest_Mods` | Debug : mod de cuirasse en cuir léger | gabarit / outil de développement | V |
| `Debug_Armor_O_Chest_Of_Damage_Enhancement` | Debug : cuirasse en acier de renforcement des dégâts | gabarit / outil de développement | V |
| `Debug_Armor_O_Head_Of_Dash_Exhaustion` | Debug : casque en acier d’augmentation du coût de ruée | gabarit / outil de développement | V |
| `Debug_Armor_O_Legs_Of_Knockback_Enhancement` | Debug : jambières en acier de renforcement du recul | gabarit / outil de développement | V |
| `Debug_Beacon_Horn` | Debug : corne de balise | dossier _Debug | V |
| `Debug_Block` | Debug : bloc | dossier _Debug | V |
| `Debug_Bow_Two_Handed` | Arc à deux mains | gabarit / outil de développement | V |
| `Debug_Combo` | Debug : objet combo | dossier _Debug | V |
| `Debug_Companion_Block_Spawner` | Debug : générateur de blocs compagnon | dossier _Debug | V |
| `Debug_Companion_Block_Spawner_Explicit` | Debug : générateur de blocs compagnon explicite | dossier _Debug | V |
| `Debug_Companion_Block_Spawner_Requirement` | Debug : condition du générateur de blocs compagnon | dossier _Debug | V |
| `Debug_Continue` | Debug : objet d’interaction | dossier _Debug | V |
| `Debug_Continue_Equip` | Debug : gants d’équipement continu | dossier _Debug | V |
| `Debug_Continue_Equip_Legs` | Debug : jambes d’équipement continu | dossier _Debug | V |
| `Debug_Donut_Selector` | Debug : sélecteur en anneau | dossier _Debug | V |
| `Debug_Durability_Condition` | Debug : objet à condition de durabilité | dossier _Debug | V |
| `Debug_Explosive` | Debug : bloc explosif | dossier _Debug | V |
| `Debug_Falling_Barrel` | Debug : tonneau qui tombe | dossier _Debug | V |
| `Debug_Falling_Explosive` | Debug : explosif qui tombe | dossier _Debug | V |
| `Debug_Forking` | Debug : bloc | dossier _Debug | V |
| `Debug_Forking_2` | Debug : bloc | dossier _Debug | V |
| `Debug_Launch_Projectile_Fixed_Pitch` | Debug : lancer de projectile (tangage fixe) | dossier _Debug | V |
| `Debug_Launch_Projectile_Fixed_Yaw` | Debug : lancer de projectile (lacet fixe) | dossier _Debug | V |
| `Debug_MusicEmitter_CaveEncounter` | Debug : émetteur de musique (rencontre en grotte) | dossier _Debug | V |
| `Debug_MusicEmitter_DeepJungles` | Debug : émetteur de musique (Deep Jungles) | dossier _Debug | V |
| `Debug_MusicEmitter_ForgottenTemple` | Debug : émetteur de musique (Forgotten Temple) | dossier _Debug | V |
| `Debug_MusicEmitter_HowlingSands` | Debug : émetteur de musique (Howling Sands) | dossier _Debug | V |
| `Debug_MusicItem_DeepJungles` | Debug : objet musical : Deep Jungles | dossier _Debug | V |
| `Debug_MusicItem_ForgottenTemple` | Debug : objet musical : Forgotten Temple | dossier _Debug | V |
| `Debug_MusicPlayer` | Debug : lecteur de musique | dossier _Debug | V |
| `Debug_Pickaxe_Shaped` | Debug : pioche à forme (3x3) | gabarit / outil de développement | V |
| `Debug_Pickaxe_Shaped_Cylinder` | Debug : pioche à forme (cylindre 5x5) | gabarit / outil de développement | V |
| `Debug_Projectile` | Debug : objet projectile | dossier _Debug | V |
| `Debug_Projectile_Fixed_Pitch` | Debug : projectile (tangage fixe) | dossier _Debug | V |
| `Debug_Projectile_Fixed_Yaw` | Debug : projectile (lacet fixe) | dossier _Debug | V |
| `Debug_Projectile_Scale` | Debug : projectile (taille aléatoire) | dossier _Debug | V |
| `Debug_Projectile_TriggerVolume` | Debug : projectile (volume de déclenchement) | dossier _Debug | V |
| `Debug_Raycast` | Debug : objet de raycast | dossier _Debug | V |
| `Debug_Rune_Burning` | [TMP] Debug : compétence de soutien Brûlure | gabarit / outil de développement | V |
| `Debug_Rune_Fork` | [TMP] Debug : compétence de soutien Fourche | gabarit / outil de développement | V |
| `Debug_Rune_Frostbolt` | [TMP] Debug : compétence Trait de givre | gabarit / outil de développement | V |
| `Debug_Rune_Speed` | [TMP] Debug : compétence de soutien Modificateur | gabarit / outil de développement | V |
| `Debug_Rune_Test` | [TMP] Debug : compétence de soutien Test | gabarit / outil de développement | V |
| `Debug_Rune_Tornado` | [TMP] Debug : compétence Tornade | gabarit / outil de développement | V |
| `Debug_Spawn_Marker_Block` | Debug : bloc marqueur d’apparition | dossier _Debug | V |
| `Debug_Stick` | Bâton | gabarit / outil de développement | V |
| `Debug_Stick_Delay` | Bâton de charge différée | gabarit / outil de développement | V |
| `Debug_Stick_Parry` | Bâton de parade | gabarit / outil de développement | V |
| `Debug_Stick_Slow` | Bâton lent | gabarit / outil de développement | V |
| `Debug_Stick_Spawn_Deployable` | Bâton d’apparition de déployables | gabarit / outil de développement | V |
| `Debug_Stick_Stun` | Bâton d’étourdissement | gabarit / outil de développement | V |
| `Deco_Moving_Box` | Carton de déménagement | dossier Hypixel | V |
| `Deco_Moving_Box1x2` | Grand carton de déménagement | dossier Hypixel | V |
| `Dungeon_Checkpoint_Statue` | Statue de point de sauvegarde de donjon | qualité Developer | V |
| `Dungeon_Offering_Bomb` | Grosse bombe incendiaire | qualité Technical | V |
| `Dungeon_Offering_Fish` | Poisson cru | qualité Technical | V |
| `Dungeon_Offering_Pork` | Porc cru | qualité Technical | V |
| `Dungeon_Offering_Shroom` | Petit champiboum | qualité Technical | V |
| `Dungeon_Offering_Steak` | Bœuf cru | qualité Technical | V |
| `Dungeon_Portal_Early_Exit` | Portail de fuite | qualité Technical | V |
| `Dungeon_Portal_End` | Portail de fin de donjon | qualité Technical | V |
| `Dungeon_Signal_Generator` | Générateur d’énergie des Ateliers du Bas-Fond | qualité Technical | V |
| `Editor_Anchor` | Ancre d’éditeur | dossier Editor | V |
| `Editor_Block` | Bloc d’éditeur | dossier Editor | V |
| `Editor_Empty` | Bloc vide d’éditeur | dossier Editor | V |
| `EditorTool_Boulder` | Pinceau de rocher | dossier EditorTool | V |
| `EditorTool_Building` | Pinceau de construction | dossier EditorTool | V |
| `EditorTool_Cave` | Pinceau de grotte | dossier EditorTool | V |
| `EditorTool_Color` | EditorTool_Color | dossier EditorTool | V |
| `EditorTool_Cracks` | Pinceau de fissure de lave | dossier EditorTool | V |
| `EditorTool_Decoration` | Pinceau de décoration | dossier EditorTool | V |
| `EditorTool_Entity` | Outil d’entité | dossier EditorTool | V |
| `EditorTool_ExampleToolArg` | Exemple de pinceau scripté : argument d’outil | dossier EditorTool | V |
| `EditorTool_Extrude` | EditorTool_Extrude | dossier EditorTool | V |
| `EditorTool_Forest` | Pinceau de forêt | dossier EditorTool | V |
| `EditorTool_GrassBrush` | EditorTool_GrassBrush | dossier EditorTool | V |
| `EditorTool_Hitbox` | Outil de hitbox | dossier EditorTool | V |
| `EditorTool_Hotsprings` | Pinceau de source chaude | dossier EditorTool | V |
| `EditorTool_LaserPointer` | Pointeur laser | dossier EditorTool | V |
| `EditorTool_Layers` | Pinceau de couches | dossier EditorTool | V |
| `EditorTool_Line` | Pinceau de ligne | dossier EditorTool | V |
| `EditorTool_Machinima` | Outil Machinima | dossier EditorTool | V |
| `EditorTool_Mountain` | Pinceau de montagne | dossier EditorTool | V |
| `EditorTool_Noise` | Pinceau de bruit | dossier EditorTool | V |
| `EditorTool_Paint` | Pinceau de peinture | dossier EditorTool | V |
| `EditorTool_Paste` | Outil Coller | dossier EditorTool | V |
| `EditorTool_Path` | Pinceau de chemin | dossier EditorTool | V |
| `EditorTool_Point` | EditorTool_Point | dossier EditorTool | V |
| `EditorTool_PrefabEditing_SelectPrefab` | Sélecteur de préfab | dossier EditorTool | V |
| `EditorTool_Revolve` | Outil Rotation | dossier EditorTool | V |
| `EditorTool_River` | Pinceau de rivière | dossier EditorTool | V |
| `EditorTool_RuinRoofs` | Pinceau de toits en ruine | dossier EditorTool | V |
| `EditorTool_Ruins` | Pinceau de ruines | dossier EditorTool | V |
| `EditorTool_Ruler` | Outil Règle | dossier EditorTool | V |
| `EditorTool_Scatter` | Pinceau de dispersion | dossier EditorTool | V |
| `EditorTool_ScriptedBrushTemplate` | EditorTool_ScriptedBrushTemplate | dossier EditorTool | V |
| `EditorTool_Sculpt` | Pinceau de sculptage | dossier EditorTool | V |
| `EditorTool_Selection` | Outil de sélection | dossier EditorTool | V |
| `EditorTool_Spiral` | Pinceau de spirale | dossier EditorTool | V |
| `EditorTool_Tentacle` | Pinceau de tentacule | dossier EditorTool | V |
| `EditorTool_Tint` | Pinceau de teinture | dossier EditorTool | V |
| `EditorTool_TriggerVolume` | EditorTool_TriggerVolume | dossier EditorTool | V |
| `Egg_Spawner_Kweebec_Razorleaf` | Œuf d’apparition de Kweebec feuille-rasoir | dossier EggSpawner | V |
| `Egg_Spawner_Lantern` | Œuf d’apparition de lanterne de compagnie | dossier EggSpawner | V |
| `Egg_Spawner_Sheep` | Œuf d’apparition de mouton | dossier EggSpawner | V |
| `Egg_Spawner_Trork` | Œuf d’apparition de Trork | dossier EggSpawner | V |
| `Eldanior_Borne_Territoire` | Borne de territoire | qualité Technical | E |
| `Eldanior_Faille_D` | Faille de rang D | qualité Technical | E |
| `Eldanior_Faille_D_Etincelle` | Faille de rang D (sur le point de s'ouvrir) | qualité Technical | E |
| `Eldanior_Faille_E` | Faille de rang E | qualité Technical | E |
| `Eldanior_Faille_E_Etincelle` | Faille de rang E (sur le point de s'ouvrir) | qualité Technical | E |
| `Eldanior_Faille_F` | Faille de rang F | qualité Technical | E |
| `Eldanior_Faille_F_Etincelle` | Faille de rang F (sur le point de s'ouvrir) | qualité Technical | E |
| `Eldanior_Faille_Invasion` | Faille d'invasion | qualité Technical | E |
| `Eldanior_Faille_Sortie` | Sortie de la faille | qualité Technical | E |
| `Eldanior_Generateur_Monstres` | Générateur de monstres | qualité Technical | E |
| `Eldanior_Poste_PNJ` | Poste de PNJ | qualité Technical | E |
| `Eldanior_Route_PNJ` | Balise de route de PNJ | qualité Technical | E |
| `Eldanior_Spawner_Boss_Duc` | Ancien générateur (sans effet) | qualité Technical | E |
| `Eldanior_Spawner_Boss_Roi_Squelette` | Ancien générateur (sans effet) | qualité Technical | E |
| `Eldanior_Spawner_Chevalier_Squelette` | Ancien générateur (sans effet) | qualité Technical | E |
| `Eldanior_Spawner_Gardien_Gobelin` | Ancien générateur (sans effet) | qualité Technical | E |
| `Eldanior_Spawner_Glouton` | Ancien générateur (sans effet) | qualité Technical | E |
| `Eldanior_Spawner_Gobelins` | Ancien générateur (sans effet) | qualité Technical | E |
| `Eldanior_Spawner_Gobelins_Lanceurs` | Ancien générateur (sans effet) | qualité Technical | E |
| `Eldanior_Spawner_Squelettes` | Ancien générateur (sans effet) | qualité Technical | E |
| `Eldanior_Spawner_Squelettes_Archers` | Ancien générateur (sans effet) | qualité Technical | E |
| `Eldanior_Spawner_Squelettes_Mages` | Ancien générateur (sans effet) | qualité Technical | E |
| `Eldanior_Tableau_Classement` | Tableau des classements | qualité Technical | E |
| `Eldanior_Tableau_Duel` | Tableau des duels | qualité Technical | E |
| `Eldanior_Tableau_Guilde` | Tableau de guilde | qualité Technical | E |
| `Eldanior_Tableau_Prime` | Tableau de prime | qualité Technical | E |
| `Eldanior_Tableau_Quetes` | Tableau des quêtes | qualité Technical | E |
| `Eldanior_Visage_Antelope` | Eldanior_Visage_Antelope | qualité Developer | E |
| `Eldanior_Visage_Archaeopteryx` | Eldanior_Visage_Archaeopteryx | qualité Developer | E |
| `Eldanior_Visage_Armadillo` | Eldanior_Visage_Armadillo | qualité Developer | E |
| `Eldanior_Visage_Bat` | Eldanior_Visage_Bat | qualité Developer | E |
| `Eldanior_Visage_Bat_Ice` | Bat Ice | qualité Developer | E |
| `Eldanior_Visage_Bear_Grizzly` | Bear Grizzly | qualité Developer | E |
| `Eldanior_Visage_Bear_Grizzly_Voidtaken` | Bear Grizzly Voidtaken | qualité Developer | E |
| `Eldanior_Visage_Bear_Grizzly_Voidtaken2` | Bear Grizzly Voidtaken2 | qualité Developer | E |
| `Eldanior_Visage_Bear_Polar` | Bear Polar | qualité Developer | E |
| `Eldanior_Visage_Bear_Voidtaken_Tentacle` | Bear Voidtaken Tentacle | qualité Developer | E |
| `Eldanior_Visage_Bear_Voidtaken_Tentacle_Model` | Bear Voidtaken Tentacle Model | qualité Developer | E |
| `Eldanior_Visage_Bison` | Eldanior_Visage_Bison | qualité Developer | E |
| `Eldanior_Visage_Bison_Calf` | Bison Calf | qualité Developer | E |
| `Eldanior_Visage_Bluebird` | Eldanior_Visage_Bluebird | qualité Developer | E |
| `Eldanior_Visage_Bluegill` | Eldanior_Visage_Bluegill | qualité Developer | E |
| `Eldanior_Visage_Boar` | Eldanior_Visage_Boar | qualité Developer | E |
| `Eldanior_Visage_Boar_Piglet` | Boar Piglet | qualité Developer | E |
| `Eldanior_Visage_Bramblekin` | Eldanior_Visage_Bramblekin | qualité Developer | E |
| `Eldanior_Visage_Bramblekin_Shaman` | Bramblekin Shaman | qualité Developer | E |
| `Eldanior_Visage_Bunny` | Eldanior_Visage_Bunny | qualité Developer | E |
| `Eldanior_Visage_Cactee` | Eldanior_Visage_Cactee | qualité Developer | E |
| `Eldanior_Visage_Calf` | Eldanior_Visage_Calf | qualité Developer | E |
| `Eldanior_Visage_Camel` | Eldanior_Visage_Camel | qualité Developer | E |
| `Eldanior_Visage_Camel_Calf` | Camel Calf | qualité Developer | E |
| `Eldanior_Visage_Cat` | Eldanior_Visage_Cat | qualité Developer | E |
| `Eldanior_Visage_Catfish` | Eldanior_Visage_Catfish | qualité Developer | E |
| `Eldanior_Visage_Chick` | Eldanior_Visage_Chick | qualité Developer | E |
| `Eldanior_Visage_Chick_Desert` | Chick Desert | qualité Developer | E |
| `Eldanior_Visage_Chicken` | Eldanior_Visage_Chicken | qualité Developer | E |
| `Eldanior_Visage_Chicken_Desert` | Chicken Desert | qualité Developer | E |
| `Eldanior_Visage_Chicken_Undead` | Chicken Undead | qualité Developer | E |
| `Eldanior_Visage_Cinematic_Dragon_Void` | Cinematic Dragon Void | qualité Developer | E |
| `Eldanior_Visage_Cinematic_Shrouded_Figure` | Cinematic Shrouded Figure | qualité Developer | E |
| `Eldanior_Visage_Clownfish` | Eldanior_Visage_Clownfish | qualité Developer | E |
| `Eldanior_Visage_Coffer_Goblin_Burner` | Coffer Goblin Burner | qualité Developer | E |
| `Eldanior_Visage_Coffer_Goblin_Glutton` | Coffer Goblin Glutton | qualité Developer | E |
| `Eldanior_Visage_Coffer_Goblin_Guardian` | Coffer Goblin Guardian | qualité Developer | E |
| `Eldanior_Visage_Coffer_Goblin_Lobber` | Coffer Goblin Lobber | qualité Developer | E |
| `Eldanior_Visage_Coffer_Goblin_Miner` | Coffer Goblin Miner | qualité Developer | E |
| `Eldanior_Visage_Coffer_Goblin_Scrapper` | Coffer Goblin Scrapper | qualité Developer | E |
| `Eldanior_Visage_Corgi` | Eldanior_Visage_Corgi | qualité Developer | E |
| `Eldanior_Visage_Cow` | Eldanior_Visage_Cow | qualité Developer | E |
| `Eldanior_Visage_Cow_Undead` | Cow Undead | qualité Developer | E |
| `Eldanior_Visage_Crab` | Eldanior_Visage_Crab | qualité Developer | E |
| `Eldanior_Visage_Crawler_Void` | Crawler Void | qualité Developer | E |
| `Eldanior_Visage_Crocodile` | Eldanior_Visage_Crocodile | qualité Developer | E |
| `Eldanior_Visage_Crow` | Eldanior_Visage_Crow | qualité Developer | E |
| `Eldanior_Visage_Deer_Doe` | Deer Doe | qualité Developer | E |
| `Eldanior_Visage_Dog` | Eldanior_Visage_Dog | qualité Developer | E |
| `Eldanior_Visage_Dragon_Fire` | Dragon Fire | qualité Developer | E |
| `Eldanior_Visage_Dragon_Frost` | Dragon Frost | qualité Developer | E |
| `Eldanior_Visage_Dragon_Void` | Dragon Void | qualité Developer | E |
| `Eldanior_Visage_Duck` | Eldanior_Visage_Duck | qualité Developer | E |
| `Eldanior_Visage_Dungeon_Feastmaster_Boss` | Dungeon Feastmaster Boss | qualité Developer | E |
| `Eldanior_Visage_Dungeon_Goblin_Food_Carrier` | Dungeon Goblin Food Carrier | qualité Developer | E |
| `Eldanior_Visage_Dungeon_Goblin_Mace` | Dungeon Goblin Mace | qualité Developer | E |
| `Eldanior_Visage_Dungeon_Goblin_Scrapper_Enrage` | Dungeon Goblin Scrapper Enrage | qualité Developer | E |
| `Eldanior_Visage_Dungeon_Goblin_Shroom_Carrier` | Dungeon Goblin Shroom Carrier | qualité Developer | E |
| `Eldanior_Visage_Eel_Moray` | Eel Moray | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Dragon_Avatar` | Dragon Avatar | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Esprit_Foret` | Esprit Foret | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Chat_Adolescent` | Familier Chat Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Chat_Adulte` | Familier Chat Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Chat_Jeune` | Familier Chat Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Cheval_Adolescent` | Familier Cheval Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Cheval_Adulte` | Familier Cheval Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Cheval_Jeune` | Familier Cheval Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Chien_Adolescent` | Familier Chien Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Chien_Adulte` | Familier Chien Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Chien_Jeune` | Familier Chien Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Chouette_Adolescent` | Familier Chouette Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Chouette_Adulte` | Familier Chouette Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Chouette_Jeune` | Familier Chouette Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Corbeau_Adolescent` | Familier Corbeau Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Corbeau_Adulte` | Familier Corbeau Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Corbeau_Jeune` | Familier Corbeau Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_DragonFeu_Adolescent` | Familier DragonFeu Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_DragonFeu_Adulte` | Familier DragonFeu Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_DragonFeu_Jeune` | Familier DragonFeu Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_DragonGivre_Adolescent` | Familier DragonGivre Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_DragonGivre_Adulte` | Familier DragonGivre Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_DragonGivre_Jeune` | Familier DragonGivre Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Faucon_Adolescent` | Familier Faucon Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Faucon_Adulte` | Familier Faucon Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Faucon_Jeune` | Familier Faucon Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Fenrir_Adolescent` | Familier Fenrir Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Fenrir_Adulte` | Familier Fenrir Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Fenrir_Jeune` | Familier Fenrir Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Leopard_Adolescent` | Familier Leopard Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Leopard_Adulte` | Familier Leopard Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Leopard_Jeune` | Familier Leopard Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Loup_Adolescent` | Familier Loup Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Loup_Adulte` | Familier Loup Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Loup_Jeune` | Familier Loup Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_LoupBlanc_Adolescent` | Familier LoupBlanc Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_LoupBlanc_Adulte` | Familier LoupBlanc Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_LoupBlanc_Jeune` | Familier LoupBlanc Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Ours_Adolescent` | Familier Ours Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Ours_Adulte` | Familier Ours Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Ours_Jeune` | Familier Ours Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_OursPolaire_Adolescent` | Familier OursPolaire Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_OursPolaire_Adulte` | Familier OursPolaire Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_OursPolaire_Jeune` | Familier OursPolaire Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Pterodactyle_Adolescent` | Familier Pterodactyle Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Pterodactyle_Adulte` | Familier Pterodactyle Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Pterodactyle_Jeune` | Familier Pterodactyle Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Raptor_Adolescent` | Familier Raptor Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Raptor_Adulte` | Familier Raptor Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Raptor_Jeune` | Familier Raptor Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Renard_Adolescent` | Familier Renard Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Renard_Adulte` | Familier Renard Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Renard_Jeune` | Familier Renard Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Tigre_Adolescent` | Familier Tigre Adolescent | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Tigre_Adulte` | Familier Tigre Adulte | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Familier_Tigre_Jeune` | Familier Tigre Jeune | qualité Developer | E |
| `Eldanior_Visage_Eldanior_Mannequin_Dojo` | Mannequin Dojo | qualité Developer | E |
| `Eldanior_Visage_Emberwulf` | Eldanior_Visage_Emberwulf | qualité Developer | E |
| `Eldanior_Visage_Eye_Void` | Eye Void | qualité Developer | E |
| `Eldanior_Visage_Fen_Stalker` | Fen Stalker | qualité Developer | E |
| `Eldanior_Visage_Feran` | Eldanior_Visage_Feran | qualité Developer | E |
| `Eldanior_Visage_Feran_Burrower` | Feran Burrower | qualité Developer | E |
| `Eldanior_Visage_Feran_Civilian` | Feran Civilian | qualité Developer | E |
| `Eldanior_Visage_Feran_Cub` | Feran Cub | qualité Developer | E |
| `Eldanior_Visage_Feran_Longtooth` | Feran Longtooth | qualité Developer | E |
| `Eldanior_Visage_Feran_Sharptooth` | Feran Sharptooth | qualité Developer | E |
| `Eldanior_Visage_Feran_Windwalker` | Feran Windwalker | qualité Developer | E |
| `Eldanior_Visage_Finch_Green` | Finch Green | qualité Developer | E |
| `Eldanior_Visage_Flamingo` | Eldanior_Visage_Flamingo | qualité Developer | E |
| `Eldanior_Visage_Fox` | Eldanior_Visage_Fox | qualité Developer | E |
| `Eldanior_Visage_Frog_Blue` | Frog Blue | qualité Developer | E |
| `Eldanior_Visage_Frog_Green` | Frog Green | qualité Developer | E |
| `Eldanior_Visage_Frog_Orange` | Frog Orange | qualité Developer | E |
| `Eldanior_Visage_Frostgill` | Eldanior_Visage_Frostgill | qualité Developer | E |
| `Eldanior_Visage_Gecko` | Eldanior_Visage_Gecko | qualité Developer | E |
| `Eldanior_Visage_Ghoul` | Eldanior_Visage_Ghoul | qualité Developer | E |
| `Eldanior_Visage_Goat` | Eldanior_Visage_Goat | qualité Developer | E |
| `Eldanior_Visage_Goat_Kid` | Goat Kid | qualité Developer | E |
| `Eldanior_Visage_Goblin` | Eldanior_Visage_Goblin | qualité Developer | E |
| `Eldanior_Visage_Goblin_Boss` | Goblin Boss | qualité Developer | E |
| `Eldanior_Visage_Goblin_Burner` | Goblin Burner | qualité Developer | E |
| `Eldanior_Visage_Goblin_Duke` | Goblin Duke | qualité Developer | E |
| `Eldanior_Visage_Goblin_Duke_Large` | Goblin Duke Large | qualité Developer | E |
| `Eldanior_Visage_Goblin_Glutton` | Goblin Glutton | qualité Developer | E |
| `Eldanior_Visage_Goblin_Gobliterator_Bottom_Phase3` | Goblin Gobliterator Bottom Phase3 | qualité Developer | E |
| `Eldanior_Visage_Goblin_Gobliterator_Explosive_Barrel` | Goblin Gobliterator Explosive Barrel | qualité Developer | E |
| `Eldanior_Visage_Goblin_Gobliterator_Phase1` | Goblin Gobliterator Phase1 | qualité Developer | E |
| `Eldanior_Visage_Goblin_Gobliterator_Phase2` | Goblin Gobliterator Phase2 | qualité Developer | E |
| `Eldanior_Visage_Goblin_Gobliterator_Phase3` | Goblin Gobliterator Phase3 | qualité Developer | E |
| `Eldanior_Visage_Goblin_Guardian` | Goblin Guardian | qualité Developer | E |
| `Eldanior_Visage_Goblin_Lab_Scrapper` | Goblin Lab Scrapper | qualité Developer | E |
| `Eldanior_Visage_Goblin_Lobber` | Goblin Lobber | qualité Developer | E |
| `Eldanior_Visage_Goblin_Merchant` | Goblin Merchant | qualité Developer | E |
| `Eldanior_Visage_Goblin_Miner` | Goblin Miner | qualité Developer | E |
| `Eldanior_Visage_Goblin_Pilot` | Goblin Pilot | qualité Developer | E |
| `Eldanior_Visage_Goblin_Scientist` | Goblin Scientist | qualité Developer | E |
| `Eldanior_Visage_Goblin_Scrapper` | Goblin Scrapper | qualité Developer | E |
| `Eldanior_Visage_Goblin_Spectator` | Goblin Spectator | qualité Developer | E |
| `Eldanior_Visage_Goblin_Thief` | Goblin Thief | qualité Developer | E |
| `Eldanior_Visage_Golem_Crystal_Earth` | Golem Crystal Earth | qualité Developer | E |
| `Eldanior_Visage_Golem_Crystal_Flame` | Golem Crystal Flame | qualité Developer | E |
| `Eldanior_Visage_Golem_Crystal_Frost` | Golem Crystal Frost | qualité Developer | E |
| `Eldanior_Visage_Golem_Crystal_Sand` | Golem Crystal Sand | qualité Developer | E |
| `Eldanior_Visage_Golem_Crystal_Thunder` | Golem Crystal Thunder | qualité Developer | E |
| `Eldanior_Visage_Golem_Firesteel` | Golem Firesteel | qualité Developer | E |
| `Eldanior_Visage_Golem_Guardian_Void` | Golem Guardian Void | qualité Developer | E |
| `Eldanior_Visage_Grooble` | Eldanior_Visage_Grooble | qualité Developer | E |
| `Eldanior_Visage_Hatworm` | Eldanior_Visage_Hatworm | qualité Developer | E |
| `Eldanior_Visage_Hawk` | Eldanior_Visage_Hawk | qualité Developer | E |
| `Eldanior_Visage_Hedera` | Eldanior_Visage_Hedera | qualité Developer | E |
| `Eldanior_Visage_Horse` | Eldanior_Visage_Horse | qualité Developer | E |
| `Eldanior_Visage_Horse_Foal` | Horse Foal | qualité Developer | E |
| `Eldanior_Visage_Horse_Skeleton` | Horse Skeleton | qualité Developer | E |
| `Eldanior_Visage_Horse_Skeleton_Armored` | Horse Skeleton Armored | qualité Developer | E |
| `Eldanior_Visage_Horse_Tamed` | Horse Tamed | qualité Developer | E |
| `Eldanior_Visage_Hound_Bleached` | Hound Bleached | qualité Developer | E |
| `Eldanior_Visage_Hyena` | Eldanior_Visage_Hyena | qualité Developer | E |
| `Eldanior_Visage_Jellyfish_Blue` | Jellyfish Blue | qualité Developer | E |
| `Eldanior_Visage_Jellyfish_Cyan` | Jellyfish Cyan | qualité Developer | E |
| `Eldanior_Visage_Jellyfish_Green` | Jellyfish Green | qualité Developer | E |
| `Eldanior_Visage_Jellyfish_Man_Of_War` | Jellyfish Man Of War | qualité Developer | E |
| `Eldanior_Visage_Jellyfish_Red` | Jellyfish Red | qualité Developer | E |
| `Eldanior_Visage_Jellyfish_Yellow` | Jellyfish Yellow | qualité Developer | E |
| `Eldanior_Visage_Kitten` | Eldanior_Visage_Kitten | qualité Developer | E |
| `Eldanior_Visage_Klops` | Eldanior_Visage_Klops | qualité Developer | E |
| `Eldanior_Visage_Klops_Gentleman` | Klops Gentleman | qualité Developer | E |
| `Eldanior_Visage_Klops_Merchant` | Klops Merchant | qualité Developer | E |
| `Eldanior_Visage_Klops_Miner` | Klops Miner | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Rootling` | Kweebec Rootling | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sapling` | Kweebec Sapling | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sapling_Brown` | Kweebec Sapling Brown | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sapling_Christmas_Blue` | Kweebec Sapling Christmas Blue | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sapling_Christmas_Green` | Kweebec Sapling Christmas Green | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sapling_Christmas_Pink` | Kweebec Sapling Christmas Pink | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sapling_Green` | Kweebec Sapling Green | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sapling_HardHat` | Kweebec Sapling HardHat | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sapling_Orange` | Kweebec Sapling Orange | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sapling_Pink` | Kweebec Sapling Pink | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sapling_Razorleaf` | Kweebec Sapling Razorleaf | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sapling_Red` | Kweebec Sapling Red | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sapling_Treesinger` | Kweebec Sapling Treesinger | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sapling_Yellow` | Kweebec Sapling Yellow | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Seedling` | Kweebec Seedling | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sproutling` | Kweebec Sproutling | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sproutling_Blue` | Kweebec Sproutling Blue | qualité Developer | E |
| `Eldanior_Visage_Kweebec_Sproutling_Lime` | Kweebec Sproutling Lime | qualité Developer | E |
| `Eldanior_Visage_Lamb` | Eldanior_Visage_Lamb | qualité Developer | E |
| `Eldanior_Visage_Larva_Silk` | Larva Silk | qualité Developer | E |
| `Eldanior_Visage_Larva_Void` | Larva Void | qualité Developer | E |
| `Eldanior_Visage_Leopard_Snow` | Leopard Snow | qualité Developer | E |
| `Eldanior_Visage_Lizard_Sand` | Lizard Sand | qualité Developer | E |
| `Eldanior_Visage_Lobster` | Eldanior_Visage_Lobster | qualité Developer | E |
| `Eldanior_Visage_Mannequin` | Eldanior_Visage_Mannequin | qualité Developer | E |
| `Eldanior_Visage_Meerkat` | Eldanior_Visage_Meerkat | qualité Developer | E |
| `Eldanior_Visage_Minnow` | Eldanior_Visage_Minnow | qualité Developer | E |
| `Eldanior_Visage_Model_Deer_Stag` | Model Deer Stag | qualité Developer | E |
| `Eldanior_Visage_Molerat` | Eldanior_Visage_Molerat | qualité Developer | E |
| `Eldanior_Visage_Moose_Bull` | Moose Bull | qualité Developer | E |
| `Eldanior_Visage_Moose_Cow` | Moose Cow | qualité Developer | E |
| `Eldanior_Visage_Mosshorn` | Eldanior_Visage_Mosshorn | qualité Developer | E |
| `Eldanior_Visage_Mosshorn_Plain` | Mosshorn Plain | qualité Developer | E |
| `Eldanior_Visage_Mouflon` | Eldanior_Visage_Mouflon | qualité Developer | E |
| `Eldanior_Visage_Mouflon_Lamb` | Mouflon Lamb | qualité Developer | E |
| `Eldanior_Visage_Mouse` | Eldanior_Visage_Mouse | qualité Developer | E |
| `Eldanior_Visage_Mushee` | Eldanior_Visage_Mushee | qualité Developer | E |
| `Eldanior_Visage_Necromancer_Void` | Necromancer Void | qualité Developer | E |
| `Eldanior_Visage_Outlander_Brute` | Outlander Brute | qualité Developer | E |
| `Eldanior_Visage_Owl_Brown` | Owl Brown | qualité Developer | E |
| `Eldanior_Visage_Owl_Snow` | Owl Snow | qualité Developer | E |
| `Eldanior_Visage_Parrot` | Eldanior_Visage_Parrot | qualité Developer | E |
| `Eldanior_Visage_Penguin` | Eldanior_Visage_Penguin | qualité Developer | E |
| `Eldanior_Visage_Pig` | Eldanior_Visage_Pig | qualité Developer | E |
| `Eldanior_Visage_Pig_Undead` | Pig Undead | qualité Developer | E |
| `Eldanior_Visage_Pig_Wild` | Pig Wild | qualité Developer | E |
| `Eldanior_Visage_Pigeon` | Eldanior_Visage_Pigeon | qualité Developer | E |
| `Eldanior_Visage_Piglet` | Eldanior_Visage_Piglet | qualité Developer | E |
| `Eldanior_Visage_Piglet_Wild` | Piglet Wild | qualité Developer | E |
| `Eldanior_Visage_Pike` | Eldanior_Visage_Pike | qualité Developer | E |
| `Eldanior_Visage_Piranha` | Eldanior_Visage_Piranha | qualité Developer | E |
| `Eldanior_Visage_Piranha_Black` | Piranha Black | qualité Developer | E |
| `Eldanior_Visage_Pterodactyl` | Eldanior_Visage_Pterodactyl | qualité Developer | E |
| `Eldanior_Visage_Pufferfish` | Eldanior_Visage_Pufferfish | qualité Developer | E |
| `Eldanior_Visage_Rabbit` | Eldanior_Visage_Rabbit | qualité Developer | E |
| `Eldanior_Visage_Ram` | Eldanior_Visage_Ram | qualité Developer | E |
| `Eldanior_Visage_Ram_Lamb` | Ram Lamb | qualité Developer | E |
| `Eldanior_Visage_Raptor_Cave` | Raptor Cave | qualité Developer | E |
| `Eldanior_Visage_Rat` | Eldanior_Visage_Rat | qualité Developer | E |
| `Eldanior_Visage_Raven` | Eldanior_Visage_Raven | qualité Developer | E |
| `Eldanior_Visage_Reindeer_Christmas` | Reindeer Christmas | qualité Developer | E |
| `Eldanior_Visage_Rex_Cave` | Rex Cave | qualité Developer | E |
| `Eldanior_Visage_Salmon` | Eldanior_Visage_Salmon | qualité Developer | E |
| `Eldanior_Visage_Saurian` | Eldanior_Visage_Saurian | qualité Developer | E |
| `Eldanior_Visage_Saurian_Hunter` | Saurian Hunter | qualité Developer | E |
| `Eldanior_Visage_Saurian_Rogue` | Saurian Rogue | qualité Developer | E |
| `Eldanior_Visage_Saurian_Warrior` | Saurian Warrior | qualité Developer | E |
| `Eldanior_Visage_Scarak_Broodmother` | Scarak Broodmother | qualité Developer | E |
| `Eldanior_Visage_Scarak_Broodmother_Young` | Scarak Broodmother Young | qualité Developer | E |
| `Eldanior_Visage_Scarak_Defender` | Scarak Defender | qualité Developer | E |
| `Eldanior_Visage_Scarak_Fighter` | Scarak Fighter | qualité Developer | E |
| `Eldanior_Visage_Scarak_Fighter_Royal_Guard` | Scarak Fighter Royal Guard | qualité Developer | E |
| `Eldanior_Visage_Scarak_Louse` | Scarak Louse | qualité Developer | E |
| `Eldanior_Visage_Scarak_Seeker` | Scarak Seeker | qualité Developer | E |
| `Eldanior_Visage_Scorpion` | Eldanior_Visage_Scorpion | qualité Developer | E |
| `Eldanior_Visage_Shadow_Knight` | Shadow Knight | qualité Developer | E |
| `Eldanior_Visage_Shark_Hammerhead` | Shark Hammerhead | qualité Developer | E |
| `Eldanior_Visage_Sheep` | Eldanior_Visage_Sheep | qualité Developer | E |
| `Eldanior_Visage_Shellfish_Lava` | Shellfish Lava | qualité Developer | E |
| `Eldanior_Visage_Shrouded_Figure` | Shrouded Figure | qualité Developer | E |
| `Eldanior_Visage_Skeleton` | Eldanior_Visage_Skeleton | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Archer` | Skeleton Archer | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Archmage` | Skeleton Archmage | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Burnt_Alchemist` | Skeleton Burnt Alchemist | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Burnt_Archer` | Skeleton Burnt Archer | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Burnt_Gunner` | Skeleton Burnt Gunner | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Burnt_Knight` | Skeleton Burnt Knight | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Burnt_Lancer` | Skeleton Burnt Lancer | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Burnt_Praetorian` | Skeleton Burnt Praetorian | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Burnt_Soldier` | Skeleton Burnt Soldier | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Burnt_Wizard` | Skeleton Burnt Wizard | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Elite` | Skeleton Elite | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Fighter` | Skeleton Fighter | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Frost_Archer` | Skeleton Frost Archer | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Frost_Archmage` | Skeleton Frost Archmage | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Frost_Fighter` | Skeleton Frost Fighter | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Frost_Knight` | Skeleton Frost Knight | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Frost_Mage` | Skeleton Frost Mage | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Frost_Ranger` | Skeleton Frost Ranger | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Frost_Scout` | Skeleton Frost Scout | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Frost_Soldier` | Skeleton Frost Soldier | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Incandescent_Fighter` | Skeleton Incandescent Fighter | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Incandescent_Footman` | Skeleton Incandescent Footman | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Incandescent_Head` | Skeleton Incandescent Head | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Incandescent_Mage` | Skeleton Incandescent Mage | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Knight` | Skeleton Knight | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Mage` | Skeleton Mage | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Pirate_Captain` | Skeleton Pirate Captain | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Pirate_Gunner` | Skeleton Pirate Gunner | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Pirate_Striker` | Skeleton Pirate Striker | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Ranger` | Skeleton Ranger | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Sand_Archer` | Skeleton Sand Archer | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Sand_Archmage` | Skeleton Sand Archmage | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Sand_Assassin` | Skeleton Sand Assassin | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Sand_Guard` | Skeleton Sand Guard | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Sand_Mage` | Skeleton Sand Mage | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Sand_Ranger` | Skeleton Sand Ranger | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Sand_Scout` | Skeleton Sand Scout | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Sand_Soldier` | Skeleton Sand Soldier | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Scout` | Skeleton Scout | qualité Developer | E |
| `Eldanior_Visage_Skeleton_Soldier` | Skeleton Soldier | qualité Developer | E |
| `Eldanior_Visage_Skrill` | Eldanior_Visage_Skrill | qualité Developer | E |
| `Eldanior_Visage_Skrill_Chick` | Skrill Chick | qualité Developer | E |
| `Eldanior_Visage_Slothian` | Eldanior_Visage_Slothian | qualité Developer | E |
| `Eldanior_Visage_Slothian_Elder` | Slothian Elder | qualité Developer | E |
| `Eldanior_Visage_Slothian_Kid` | Slothian Kid | qualité Developer | E |
| `Eldanior_Visage_Slothian_Monk` | Slothian Monk | qualité Developer | E |
| `Eldanior_Visage_Slothian_Scout` | Slothian Scout | qualité Developer | E |
| `Eldanior_Visage_Slothian_Villager` | Slothian Villager | qualité Developer | E |
| `Eldanior_Visage_Slothian_Warrior` | Slothian Warrior | qualité Developer | E |
| `Eldanior_Visage_Slug_Magma` | Slug Magma | qualité Developer | E |
| `Eldanior_Visage_Snail_Frost` | Snail Frost | qualité Developer | E |
| `Eldanior_Visage_Snail_Magma` | Snail Magma | qualité Developer | E |
| `Eldanior_Visage_Snake_Cobra` | Snake Cobra | qualité Developer | E |
| `Eldanior_Visage_Snake_Marsh` | Snake Marsh | qualité Developer | E |
| `Eldanior_Visage_Snake_Rattle` | Snake Rattle | qualité Developer | E |
| `Eldanior_Visage_Snapdragon` | Eldanior_Visage_Snapdragon | qualité Developer | E |
| `Eldanior_Visage_Snapjaw` | Eldanior_Visage_Snapjaw | qualité Developer | E |
| `Eldanior_Visage_Spark_Living` | Spark Living | qualité Developer | E |
| `Eldanior_Visage_Sparrow` | Eldanior_Visage_Sparrow | qualité Developer | E |
| `Eldanior_Visage_Spawn_Void` | Spawn Void | qualité Developer | E |
| `Eldanior_Visage_Spider` | Eldanior_Visage_Spider | qualité Developer | E |
| `Eldanior_Visage_Spider_Cave` | Spider Cave | qualité Developer | E |
| `Eldanior_Visage_Spider_Cave_Spiderling` | Spider Cave Spiderling | qualité Developer | E |
| `Eldanior_Visage_Spirit_Ember` | Spirit Ember | qualité Developer | E |
| `Eldanior_Visage_Spirit_Frost` | Spirit Frost | qualité Developer | E |
| `Eldanior_Visage_Spirit_Root` | Spirit Root | qualité Developer | E |
| `Eldanior_Visage_Spirit_Thunder` | Spirit Thunder | qualité Developer | E |
| `Eldanior_Visage_Squirrel` | Eldanior_Visage_Squirrel | qualité Developer | E |
| `Eldanior_Visage_Swarm_Bees` | Swarm Bees | qualité Developer | E |
| `Eldanior_Visage_Tadpole_Rhino` | Tadpole Rhino | qualité Developer | E |
| `Eldanior_Visage_Tang_Blue` | Tang Blue | qualité Developer | E |
| `Eldanior_Visage_Tang_Chevron` | Tang Chevron | qualité Developer | E |
| `Eldanior_Visage_Tang_Lemon_Peel` | Tang Lemon Peel | qualité Developer | E |
| `Eldanior_Visage_Tang_Sailfin` | Tang Sailfin | qualité Developer | E |
| `Eldanior_Visage_Tetrabird` | Eldanior_Visage_Tetrabird | qualité Developer | E |
| `Eldanior_Visage_Tiger_Sabertooth` | Tiger Sabertooth | qualité Developer | E |
| `Eldanior_Visage_Toad_Rhino` | Toad Rhino | qualité Developer | E |
| `Eldanior_Visage_Toad_Rhino_Green` | Toad Rhino Green | qualité Developer | E |
| `Eldanior_Visage_Toad_Rhino_Magma` | Toad Rhino Magma | qualité Developer | E |
| `Eldanior_Visage_Tortoise` | Eldanior_Visage_Tortoise | qualité Developer | E |
| `Eldanior_Visage_Trillodon` | Eldanior_Visage_Trillodon | qualité Developer | E |
| `Eldanior_Visage_Trilobite` | Eldanior_Visage_Trilobite | qualité Developer | E |
| `Eldanior_Visage_Trilobite_Black` | Trilobite Black | qualité Developer | E |
| `Eldanior_Visage_Trork` | Eldanior_Visage_Trork | qualité Developer | E |
| `Eldanior_Visage_Trork_Brawler` | Trork Brawler | qualité Developer | E |
| `Eldanior_Visage_Trork_Chieftain` | Trork Chieftain | qualité Developer | E |
| `Eldanior_Visage_Trork_Christmas` | Trork Christmas | qualité Developer | E |
| `Eldanior_Visage_Trork_Doctor_Witch` | Trork Doctor Witch | qualité Developer | E |
| `Eldanior_Visage_Trork_Guard` | Trork Guard | qualité Developer | E |
| `Eldanior_Visage_Trork_Hunter` | Trork Hunter | qualité Developer | E |
| `Eldanior_Visage_Trork_Mauler` | Trork Mauler | qualité Developer | E |
| `Eldanior_Visage_Trork_Sentry` | Trork Sentry | qualité Developer | E |
| `Eldanior_Visage_Trork_Shaman` | Trork Shaman | qualité Developer | E |
| `Eldanior_Visage_Trork_Warrior` | Trork Warrior | qualité Developer | E |
| `Eldanior_Visage_Trout_Rainbow` | Trout Rainbow | qualité Developer | E |
| `Eldanior_Visage_Tuluk` | Eldanior_Visage_Tuluk | qualité Developer | E |
| `Eldanior_Visage_Tuluk_Fisherman` | Tuluk Fisherman | qualité Developer | E |
| `Eldanior_Visage_Turkey` | Eldanior_Visage_Turkey | qualité Developer | E |
| `Eldanior_Visage_Turkey_Chick` | Turkey Chick | qualité Developer | E |
| `Eldanior_Visage_Void_Anomaly_Crystal_Large` | Void Anomaly Crystal Large | qualité Developer | E |
| `Eldanior_Visage_Void_Anomaly_Crystal_Medium` | Void Anomaly Crystal Medium | qualité Developer | E |
| `Eldanior_Visage_Void_Anomaly_Crystal_Small` | Void Anomaly Crystal Small | qualité Developer | E |
| `Eldanior_Visage_Void_Anomaly_Stutter` | Void Anomaly Stutter | qualité Developer | E |
| `Eldanior_Visage_Void_Spectre` | Void Spectre | qualité Developer | E |
| `Eldanior_Visage_Void_Tentacle` | Void Tentacle | qualité Developer | E |
| `Eldanior_Visage_Voidtaken_Goblin` | Voidtaken Goblin | qualité Developer | E |
| `Eldanior_Visage_Vulture` | Eldanior_Visage_Vulture | qualité Developer | E |
| `Eldanior_Visage_Warthog` | Eldanior_Visage_Warthog | qualité Developer | E |
| `Eldanior_Visage_Warthog_Piglet` | Warthog Piglet | qualité Developer | E |
| `Eldanior_Visage_Werewolf` | Eldanior_Visage_Werewolf | qualité Developer | E |
| `Eldanior_Visage_Whale_Humpback` | Whale Humpback | qualité Developer | E |
| `Eldanior_Visage_Wolf_Black` | Wolf Black | qualité Developer | E |
| `Eldanior_Visage_Wolf_Outlander_Priest` | Wolf Outlander Priest | qualité Developer | E |
| `Eldanior_Visage_Wolf_Outlander_Sorcerer` | Wolf Outlander Sorcerer | qualité Developer | E |
| `Eldanior_Visage_Wolf_Trork_Hunter` | Wolf Trork Hunter | qualité Developer | E |
| `Eldanior_Visage_Wolf_Trork_Shaman` | Wolf Trork Shaman | qualité Developer | E |
| `Eldanior_Visage_Wolf_White` | Wolf White | qualité Developer | E |
| `Eldanior_Visage_Woodpecker` | Eldanior_Visage_Woodpecker | qualité Developer | E |
| `Eldanior_Visage_Wraith` | Eldanior_Visage_Wraith | qualité Developer | E |
| `Eldanior_Visage_Wraith_Lantern` | Wraith Lantern | qualité Developer | E |
| `Eldanior_Visage_Wurmling_Frost` | Wurmling Frost | qualité Developer | E |
| `Eldanior_Visage_Yeti` | Eldanior_Visage_Yeti | qualité Developer | E |
| `Eldanior_Visage_Zombie` | Eldanior_Visage_Zombie | qualité Developer | E |
| `Eldanior_Visage_Zombie_Aberrant` | Zombie Aberrant | qualité Developer | E |
| `Eldanior_Visage_Zombie_Aberrant_Big` | Zombie Aberrant Big | qualité Developer | E |
| `Eldanior_Visage_Zombie_Aberrant_Small` | Zombie Aberrant Small | qualité Developer | E |
| `Eldanior_Visage_Zombie_Burnt` | Zombie Burnt | qualité Developer | E |
| `Eldanior_Visage_Zombie_Frost` | Zombie Frost | qualité Developer | E |
| `Eldanior_Visage_Zombie_Sand` | Zombie Sand | qualité Developer | E |
| `Eldanior_Visage_Zombie_Werewolf` | Zombie Werewolf | qualité Developer | E |
| `Eldanior_Voile_Portail` | Voile du Portail Continental | qualité Technical | E |
| `Eldanior_Voile_Portail_Coeur` | Cœur du Portail Continental | qualité Technical | E |
| `ExamplePrefabPlacementBlock_Tree` | Exemple de bloc préfab (arbre) | dossier Categories | V |
| `Filter_Air_Block` | Filtre : bloc d’air | dossier Categories | V |
| `Filter_AllScatter` | Filtre : toutes les dispersions | dossier Categories | V |
| `Filter_Fluids` | Filtre : fluides | dossier Categories | V |
| `Filter_Gravel` | Filtre : gravier | dossier Categories | V |
| `Filter_Ores` | Filtre : minerais | dossier Categories | V |
| `Filter_PlantsAndTrees` | Filtre : plantes et arbres | dossier Categories | V |
| `Filter_PlantScatter` | Filtre : dispersion de plantes | dossier Categories | V |
| `Filter_Rock` | Filtre : roche | dossier Categories | V |
| `Filter_Snow` | Filtre : neige | dossier Categories | V |
| `Filter_Soils` | Filtre : sols | dossier Categories | V |
| `Filter_TreeLeaves` | Filtre : feuilles d’arbres | dossier Categories | V |
| `Filter_TreeWood` | Filtre : bois d’arbre | dossier Categories | V |
| `Filter_TreeWoodAndLeaves` | Filtre : bois d’arbre et feuilles | dossier Categories | V |
| `Fluid_Fire` | Feu | dossier Fluid | V |
| `Fluid_Lava` | Lave | dossier Fluid | V |
| `Fluid_Poison` | Poison | dossier Fluid | V |
| `Fluid_Slime` | Slime | dossier Fluid | V |
| `Fluid_Slime_Red` | Slime rouge | gabarit / outil de développement | V |
| `Fluid_Tar` | Goudron | dossier Fluid | V |
| `Fluid_Tar_Void` | Goudron du Néant | dossier Fluid | V |
| `Fluid_Water` | Eau | dossier Fluid | V |
| `Fluid_Water_Test` | Eau limitée | dossier Fluid | V |
| `Forgotten_Temple_Portal_Enter` | Entrée du Temple de l’Oubli | qualité Technical | V |
| `Forgotten_Temple_Portal_Exit` | Sortie du Temple de l’Oubli | qualité Technical | V |
| `Furniture_Bookshelf_Single` | Prototype : bibliothèque - unique | qualité Developer | V |
| `Furniture_Construction_Sign` | Panneau « Travaux en cours » | qualité Technical | V |
| `Furniture_Crude_Windows_1X2_Test` | Prototype de fenêtre rudimentaire 1x2 | qualité Developer | V |
| `Furniture_Crude_Windows_2X2_Test` | Prototype de fenêtre rudimentaire 2x2 | qualité Developer | V |
| `Furniture_Cybercity_Sign_Fruit` | Panneau néon : fruit | dossier Hypixel | V |
| `Furniture_Cybercity_Sign_Tea` | Panneau néon : thé | dossier Hypixel | V |
| `Furniture_Cybercity_Windows` | Prototype : fenêtre de ville futuriste | qualité Developer | V |
| `Furniture_Cybercity_Windows_Bot` | Prototype : fenêtre de ville futuriste - Bas | qualité Developer | V |
| `Furniture_Cybercity_Windows_Bottom_Right` | Prototype : fenêtre de ville futuriste - Coin inférieur droit | qualité Developer | V |
| `Furniture_Cybercity_Windows_C` | Prototype : fenêtre de ville futuriste en C | qualité Developer | V |
| `Furniture_Cybercity_Windows_Full` | Prototype : fenêtre de ville futuriste complète | qualité Developer | V |
| `Furniture_Cybercity_Windows_LR` | Prototype : fenêtre de ville futuriste - Gauche et droite | qualité Developer | V |
| `Furniture_Cybercity_Windows_Purple` | Prototype : fenêtre de ville futuriste - Haut et bas, violette | qualité Developer | V |
| `Furniture_Cybercity_Windows_Red` | Prototype : fenêtre de ville futuriste - Haut et bas, rouge | qualité Developer | V |
| `Furniture_Cybercity_Windows_Right` | Prototype : fenêtre de ville futuriste - Droite | qualité Developer | V |
| `Furniture_Cybercity_Windows_TopBot` | Prototype : fenêtre de ville futuriste - Haut et bas | qualité Developer | V |
| `Furniture_Cybercity_Windows_U` | Prototype : fenêtre de ville futuriste en U | qualité Developer | V |
| `Furniture_Cybercity_Windows_Yellow` | Prototype : fenêtre de ville futuriste - Haut et bas, jaune | qualité Developer | V |
| `Furniture_Flag_Orange` | Drapeau orange | qualité Technical | V |
| `Furniture_Flag_Small_Red` | Drapeau rouge | qualité Technical | V |
| `Furniture_Human_Ruins_Banner_Double` | Prototype de double bannière de civilisation perdue | qualité Developer | V |
| `Furniture_Human_Ruins_Banner_Triple` | Prototype de triple bannière de civilisation perdue | qualité Developer | V |
| `Furniture_Royal_Magic_Carpet` | Tapis magique royal | qualité Developer | V |
| `Furniture_Royal_Magic_Window` | Fenêtre magique royale | qualité Developer | V |
| `Furniture_Village_Painting_1x2` | Portrait simple | qualité Developer | V |
| `Furniture_Village_Painting_2x1` | Paysage simple | qualité Developer | V |
| `Furniture_Village_Painting_2x2` | Tableau carré simple | qualité Developer | V |
| `Furniture_Village_Painting_3x2` | Grand paysage simple | qualité Developer | V |
| `Geyzer_Spawner1` | Générateur de geyser (5 sec) | qualité Developer | V |
| `Glass_Test_Blue` | Test : verre (bleu) | qualité Developer | V |
| `Glass_Test_Green` | Test : verre (vert) | qualité Developer | V |
| `Glass_Test_Purple` | Test : verre (violet) | qualité Developer | V |
| `Glass_Test_Red` | Test : verre (rouge) | qualité Developer | V |
| `Glass_Test_White` | Test : verre (blanc) | qualité Developer | V |
| `Glass_Test_Yellow` | Test : verre (jaune) | qualité Developer | V |
| `Goblin_Breach_Portal` | Portail antique | qualité Developer | V |
| `Goblin_Gobliterator_Difficulty_Button` | Bouton de difficulté du Goblitérateur | dossier Puzzles | V |
| `Goblin_Gobliterator_Difficulty_Lever` | Levier de difficulté du Goblitérateur | dossier Puzzles | V |
| `Goblin_Gobliterator_Difficulty_Lever_Hard` | Levier de difficulté du Goblitérateur (difficile) | dossier Puzzles | V |
| `GoblinLab_Console` | Console des brèches gobelins | dossier Puzzles | V |
| `GoblinLab_Fragment_Portal` | Le portail gobelin | dossier Puzzles | V |
| `GoblinLab_Generator` | Générateur d’énergie gobelin | dossier Puzzles | V |
| `GoblinLab_Teleporter` | [TMP] Téléporteur du laboratoire gobelin | dossier Puzzles | V |
| `GoblinLab_Tinkering_Bench` | Établi du bricoleur gobelin | dossier Puzzles | V |
| `GoblinLab_Tinkering_Bench_Recipeset_A` | Chaudière | dossier Puzzles | V |
| `GoblinLab_Tinkering_Bench_Recipeset_B` | Moteur | dossier Puzzles | V |
| `GoblinLab_Tinkering_Bench_Recipeset_C` | Compresseur | dossier Puzzles | V |
| `Greenscreen` | Fond vert | qualité Technical | V |
| `Hub_Portal_Default` | Portail de monde d’exploration | gabarit / outil de développement | V |
| `Hub_Portal_Flat` | Portail de monde plat | gabarit / outil de développement | V |
| `Hub_Portal_Zone3_Taiga1` | Portail de la zone 3 Taïga | gabarit / outil de développement | V |
| `Hunt_Clawmark` | Marques de griffes | qualité Developer | V |
| `Hunt_Footprint` | Traces de prédateur | qualité Developer | V |
| `Hunt_Trap` | [TMP] Piège à ours à base de Cœur du Néant | qualité Developer | V |
| `Instance_Gateway` | Portail d’instance | qualité Developer | V |
| `Launchpad` | Plateforme de lancement | qualité Developer | V |
| `Leave_Instance` | Quitter l’instance | qualité Technical | V |
| `Lobby_Wall_Poster01` | Poster mural | dossier Hypixel | V |
| `Lobby_Wall_Poster_Small01` | Petit poster mural | dossier Hypixel | V |
| `Memory_Particle` | Souvenir | qualité Technical | V |
| `Metal_Iron_Gutter` | Tuyau en fer : long - Cybercity | qualité Developer | V |
| `Nyx_Goudron` | Goudron du Néant | qualité Technical | E |
| `Objective_Bounty` | Prime | qualité Developer | V |
| `Plant_Crop_Apple_Block` | Pomme mûre | qualité Technical | V |
| `Plant_Crop_Apple_Block_Wall` | Pomme mûre | qualité Technical | V |
| `Plant_Crop_Aubergine_Block` | Aubergine mûre | qualité Technical | V |
| `Plant_Crop_Carrot_Block` | Carotte mûre | qualité Technical | V |
| `Plant_Crop_Cauliflower_Block` | Chou-fleur mûr | qualité Technical | V |
| `Plant_Crop_Chilli_Block` | Piment mûr | qualité Technical | V |
| `Plant_Crop_Corn_Block` | Maïs mûr | qualité Technical | V |
| `Plant_Crop_Cotton_Block` | Coton mûr | qualité Technical | V |
| `Plant_Crop_Lettuce_Block` | Laitue mûre | qualité Technical | V |
| `Plant_Crop_Onion_Block` | Oignons mûrs | qualité Technical | V |
| `Plant_Crop_Potato_Block` | Pomme de terre mûre | qualité Technical | V |
| `Plant_Crop_Pumpkin_Block` | Citrouille mûre | qualité Technical | V |
| `Plant_Crop_Rice_Block` | Riz mûr | qualité Technical | V |
| `Plant_Crop_Tomato_Block` | Tomate mûre | qualité Technical | V |
| `Plant_Crop_Turnip_Block` | Navet mûr | qualité Technical | V |
| `Plant_Crop_Wheat_Block` | Blé mûr | qualité Technical | V |
| `Plant_Seeds_Test_Tree` | Arbre de test de graine | qualité Developer | V |
| `Plant_Test_Tree_Block` | Arbre test 0 | qualité Developer | V |
| `Portal_Prefab` | Portail préfab | dossier _Debug | V |
| `Portal_Return` | Portail de retour | qualité Technical | V |
| `Prefab_Spawner_Block` | Générateur de préfabs | qualité Technical | V |
| `Prototype_Ancient_Sign_Hanging` | Prototype : ancienne enseigne suspendue | gabarit / outil de développement | V |
| `Prototype_Cacti_Flower_Giant_Pink` | Prototype : fleur de cactus rose géante | gabarit / outil de développement | V |
| `Prototype_Cactus_Five` | Prototype : bloc de cactus 5 | gabarit / outil de développement | V |
| `Prototype_Cactus_Flower_Giant_White` | Prototype : fleur de cactus blanche géante | gabarit / outil de développement | V |
| `Prototype_Cactus_Flower_Giant_Yellow` | Prototype : fleur de cactus jaune géante | gabarit / outil de développement | V |
| `Prototype_Cactus_Flower_Pink` | Prototype : fleur de cactus rose | gabarit / outil de développement | V |
| `Prototype_Cactus_Flower_White` | Prototype : fleur de cactus blanche | gabarit / outil de développement | V |
| `Prototype_Cactus_Flower_Yellow` | Prototype : fleur de cactus jaune | gabarit / outil de développement | V |
| `Prototype_Cactus_Four` | Prototype : bloc de cactus 4 | gabarit / outil de développement | V |
| `Prototype_Cactus_Kit_Flat_Base` | Prototype : cactus plat : base | gabarit / outil de développement | V |
| `Prototype_Cactus_Kit_Flat_Middle` | Prototype : cactus plat : milieu | gabarit / outil de développement | V |
| `Prototype_Cactus_Kit_Flat_Top` | Prototype : cactus plat : haut | gabarit / outil de développement | V |
| `Prototype_Cactus_Kit_Tall_Arm` | Prototype : grand cactus : bras | gabarit / outil de développement | V |
| `Prototype_Cactus_Kit_Tall_Arm_Large` | Prototype : grand cactus : grand bras | gabarit / outil de développement | V |
| `Prototype_Cactus_Kit_Tall_Arm_Secondary` | Prototype : grand cactus : bras secondaire | gabarit / outil de développement | V |
| `Prototype_Cactus_Kit_Tall_Base` | Prototype : grand cactus : base | gabarit / outil de développement | V |
| `Prototype_Cactus_Kit_Tall_Middle` | Prototype : grand cactus : milieu | gabarit / outil de développement | V |
| `Prototype_Cactus_Kit_Tall_Top` | Prototype : grand cactus : haut | gabarit / outil de développement | V |
| `Prototype_Cactus_One` | Prototype : bloc de cactus 1 | gabarit / outil de développement | V |
| `Prototype_Cactus_Spike_Long` | Prototype : longue épine de cactus | gabarit / outil de développement | V |
| `Prototype_Cactus_Succulent_One` | Prototype : grande succulente | gabarit / outil de développement | V |
| `Prototype_Cactus_Succulent_Small` | Prototype : petite succulente | gabarit / outil de développement | V |
| `Prototype_Cactus_Tall_Thick_Arm` | Prototype : grand cactus épais : bras | gabarit / outil de développement | V |
| `Prototype_Cactus_Tall_Thick_Arm_Large` | Prototype : grand cactus épais : grand bras | gabarit / outil de développement | V |
| `Prototype_Cactus_Tall_Thick_Arm_Secondary` | Prototype : grand cactus épais : bras secondaire | gabarit / outil de développement | V |
| `Prototype_Cactus_Tall_Thick_Base` | Prototype : grand cactus épais : base | gabarit / outil de développement | V |
| `Prototype_Cactus_Tall_Thick_Middle` | Prototype : grand cactus épais : milieu | gabarit / outil de développement | V |
| `Prototype_Cactus_Tall_Thick_Top` | Prototype : grand cactus épais : haut | gabarit / outil de développement | V |
| `Prototype_Cactus_Three` | Prototype : bloc de cactus 3 | gabarit / outil de développement | V |
| `Prototype_Cactus_Two` | Prototype : bloc de cactus 2 | gabarit / outil de développement | V |
| `Prototype_Crude_Sign_Ground` | Prototype : panneau au sol rudimentaire | gabarit / outil de développement | V |
| `Prototype_Geyser` | Prototype : générateur de geyser | gabarit / outil de développement | V |
| `Prototype_Goldenwood_Roof_Gutter_Slope` | Prototype : gouttière de toit en bois doré | gabarit / outil de développement | V |
| `Prototype_Human_Ruins_Sign_Arrow` | Prototype : panneau flèche de civilisation perdue | gabarit / outil de développement | V |
| `Prototype_Human_Ruins_Sign_Hanging` | Prototype : panneau suspendu de civilisation perdue | gabarit / outil de développement | V |
| `Prototype_Human_Ruins_Sign_Shield` | Prototype : pancarte bouclier de civilisation perdue | gabarit / outil de développement | V |
| `Prototype_NumericalState_Candle` | Prototype : état numérique - bougie 1 | gabarit / outil de développement | V |
| `Prototype_NumericalState_Candle2` | Prototype : état numérique - bougie 2 | gabarit / outil de développement | V |
| `Prototype_NumericalState_Candle3` | Prototype : état numérique - bougie 3 | gabarit / outil de développement | V |
| `Prototype_Rock_Chalk_Brick_Pipe_Corner` | Tuyau en brique de craie : angle | gabarit / outil de développement | V |
| `Prototype_Rock_Chalk_Brick_Pipe_Short` | Tuyau en brique de craie : court | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Brick` | Brique en béton | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Brick_Beam` | Poutre en brique de béton | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Brick_Decorative` | Brique en béton décorative | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Brick_Half` | Dalle en brique de béton | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Brick_Ornate` | Brique en béton ornée | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Brick_Pillar_Base` | Pilier en brique de béton : base | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Brick_Pillar_Middle` | Pilier en brique de béton : milieu | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Brick_Roof` | Toit en brique de béton | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Brick_Roof_Flat` | Toit en brique de béton : plat | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Brick_Roof_Hollow` | Toit en brique de béton : creux | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Brick_Roof_Shallow` | Toit en brique de béton : faible pente | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Brick_Roof_Steep` | Toit en brique de béton : forte pente | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Brick_Stairs` | Escalier en brique de béton | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Brick_Wall` | Mur en brique de béton | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Slab` | Dalle de béton | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Slab_Brick` | Dalle de brique en béton | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Slab_Brick_Decorative` | Dalle de brique en béton décorative | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Slab_Brick_Half` | Demi-dalle de brique en béton | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Slab_Mid_Top` | Dalle de béton : moitié supérieure | gabarit / outil de développement | V |
| `Prototype_Rock_Concrete_Slab_Stairs` | Escalier en dalle de béton | gabarit / outil de développement | V |
| `Prototype_Rock_Ledge_Cobble_Pipe_Corner` | Tuyau en pavés de pierre stratifiée : angle | gabarit / outil de développement | V |
| `Prototype_Rock_Ledge_Cobble_Pipe_Long` | Tuyau en pavés de pierre stratifiée : long | gabarit / outil de développement | V |
| `Prototype_Rock_Ledge_Cobble_Pipe_Short` | Tuyau en pavés de pierre stratifiée : court | gabarit / outil de développement | V |
| `Prototype_Rock_Peach_Brick_Pipe_Corner` | Tuyau en brique de pierre pêche : angle | gabarit / outil de développement | V |
| `Prototype_Rock_Peach_Brick_Pipe_Short` | Tuyau en brique de pierre pêche : court | gabarit / outil de développement | V |
| `Prototype_Rock_Peach_Cobble_Pipe_Corner` | Tuyau en pavés de pierre pêche : angle | gabarit / outil de développement | V |
| `Prototype_Rock_Peach_Cobble_Pipe_Short` | Tuyau en pavés de pierre pêche : court | gabarit / outil de développement | V |
| `Prototype_Rock_Runic_Cobble_Pipe_Corner` | Tuyau en pavés runiques : angle | gabarit / outil de développement | V |
| `Prototype_Rock_Runic_Cobble_Pipe_Short` | Tuyau en pavés runiques : court | gabarit / outil de développement | V |
| `Prototype_Rock_Runic_Dark_Brick_Pipe_Corner` | Tuyau en brique runique sombre : angle | gabarit / outil de développement | V |
| `Prototype_Rock_Runic_Dark_Brick_Pipe_Short` | Tuyau en brique runique sombre : court | gabarit / outil de développement | V |
| `Prototype_Softwood_Roof_Gutter_Slope` | Prototype : toit en bois tendre - gouttière | gabarit / outil de développement | V |
| `Prototype_Succulent_Hanging_One_Corner` | Succulente suspendue 1 : angle | gabarit / outil de développement | V |
| `Prototype_Succulent_Hanging_One_End` | Succulente suspendue 1 : extrémité | gabarit / outil de développement | V |
| `Prototype_Succulent_Hanging_One_Horizontal` | Succulente suspendue 1 : horizontale | gabarit / outil de développement | V |
| `Prototype_Succulent_Hanging_One_Vertical` | Succulente suspendue 1 : verticale | gabarit / outil de développement | V |
| `Prototype_Succulent_Hanging_Three_Corner` | Succulente suspendue 3 : angle | gabarit / outil de développement | V |
| `Prototype_Succulent_Hanging_Three_End` | Succulente suspendue 3 : extrémité | gabarit / outil de développement | V |
| `Prototype_Succulent_Hanging_Three_Horizontal` | Succulente suspendue 3 : horizontale | gabarit / outil de développement | V |
| `Prototype_Succulent_Hanging_Three_Vertical` | Succulente suspendue 3 : verticale | gabarit / outil de développement | V |
| `Prototype_Succulent_Hanging_Two_Corner` | Succulente suspendue 2 : angle | gabarit / outil de développement | V |
| `Prototype_Succulent_Hanging_Two_End` | Succulente suspendue 2 : extrémité | gabarit / outil de développement | V |
| `Prototype_Succulent_Hanging_Two_Horizontal` | Succulente suspendue 2 : horizontale | gabarit / outil de développement | V |
| `Prototype_Succulent_Hanging_Two_Vertical` | Succulente suspendue 2 : verticale | gabarit / outil de développement | V |
| `Prototype_Succulent_Hanging_Vertical` | Succulente suspendue : verticale | gabarit / outil de développement | V |
| `Prototype_Temple_Emerald_Sign_Ground` | Prototype : panneau de temple d’émeraude | gabarit / outil de développement | V |
| `Prototype_Tool_Book_Mana` | Prototype : livre de mana | gabarit / outil de développement | V |
| `Prototype_Tool_Guitar_Mana` | Prototype : guitare de mana | gabarit / outil de développement | V |
| `Prototype_Tool_Staff_Mana` | Prototype : bâton de mana | gabarit / outil de développement | V |
| `Prototype_Window_Corner_Bottom_Left` | Prototype : coin de fenêtre inférieur gauche | gabarit / outil de développement | V |
| `Prototype_Window_Corner_Bottom_Right` | Prototype : coin de fenêtre inférieur droit | gabarit / outil de développement | V |
| `Prototype_Window_Corner_Rounded_Bottom_Left` | Prototype : coin de fenêtre inférieur gauche | gabarit / outil de développement | V |
| `Prototype_Window_Corner_Rounded_Bottom_Right` | Prototype : coin de fenêtre inférieur droit | gabarit / outil de développement | V |
| `Prototype_Window_Corner_Rounded_Top_Left` | Prototype : coin de fenêtre supérieur gauche | gabarit / outil de développement | V |
| `Prototype_Window_Corner_Rounded_Top_Right` | Prototype : coin de fenêtre supérieur droit | gabarit / outil de développement | V |
| `Prototype_Window_Corner_Sloped_Bottom_Left` | Prototype : coin de fenêtre inférieur gauche | gabarit / outil de développement | V |
| `Prototype_Window_Corner_Sloped_Bottom_Right` | Prototype : coin de fenêtre inférieur droit | gabarit / outil de développement | V |
| `Prototype_Window_Corner_Sloped_Top_Left` | Prototype : coin de fenêtre supérieur gauche | gabarit / outil de développement | V |
| `Prototype_Window_Corner_Sloped_Top_Right` | Prototype : coin de fenêtre supérieur droit | gabarit / outil de développement | V |
| `Prototype_Window_Corner_Top_Left` | Prototype : coin de fenêtre supérieur gauche | gabarit / outil de développement | V |
| `Prototype_Window_Corner_Top_Right` | Prototype : coin de fenêtre supérieur droit | gabarit / outil de développement | V |
| `Prototype_Window_Edge_Bottom` | Prototype : bord de fenêtre inférieur | gabarit / outil de développement | V |
| `Prototype_Window_Edge_Left` | Prototype : bord de fenêtre gauche | gabarit / outil de développement | V |
| `Prototype_Window_Edge_Right` | Prototype : bord de fenêtre droit | gabarit / outil de développement | V |
| `Prototype_Window_Edge_Top` | Prototype : bord de fenêtre supérieur | gabarit / outil de développement | V |
| `Prototype_Window_Long_Horizontal` | Prototype : fenêtre horizontale | gabarit / outil de développement | V |
| `Prototype_Window_Long_Horizontal_End_L` | Prototype : fenêtre horizontale extrémité G | gabarit / outil de développement | V |
| `Prototype_Window_Long_Horizontal_End_R` | Prototype : fenêtre horizontale extrémité D | gabarit / outil de développement | V |
| `Prototype_Window_Long_Vertical` | Prototype : fenêtre verticale | gabarit / outil de développement | V |
| `Prototype_Window_Long_Vertical_End_Top` | Prototype : fenêtre verticale extrémité haut | gabarit / outil de développement | V |
| `Prototype_Window_Middle` | Prototype : fenêtre milieu | gabarit / outil de développement | V |
| `Prototype_Window_Single` | Vitraux | gabarit / outil de développement | V |
| `Prototype_Window_Vertical_End_Bottom` | Prototype : fenêtre verticale extrémité bas | gabarit / outil de développement | V |
| `Prototype_Window_Vertical_Round_Bottom` | Prototype : fenêtre verticale extrémité bas | gabarit / outil de développement | V |
| `Prototype_Window_Vertical_Round_Top` | Prototype : fenêtre verticale extrémité haut | gabarit / outil de développement | V |
| `Prototype_Window_Vertical_Triangle_Bottom` | Prototype : fenêtre verticale extrémité bas | gabarit / outil de développement | V |
| `Prototype_Window_Vertical_Triangle_Top` | Prototype : fenêtre verticale extrémité haut | gabarit / outil de développement | V |
| `Prototype_Wood_Beech_Branch_Corner_Sideways` | Prototype : coin latéral | gabarit / outil de développement | V |
| `Prototype_Wood_Beech_Branch_Five` | Prototype : cinq | gabarit / outil de développement | V |
| `Prototype_Wood_Beech_Branch_Five_Sideways` | Prototype : cinq latéraux | gabarit / outil de développement | V |
| `Prototype_Wood_Beech_Branch_Four_Edge` | Prototype : quatre bords | gabarit / outil de développement | V |
| `Prototype_Wood_Beech_Branch_Four_Edge_Sideways` | Prototype : quatre bords latéraux | gabarit / outil de développement | V |
| `Prototype_Wood_Beech_Branch_Fullsided` | Prototype : toutes faces | gabarit / outil de développement | V |
| `Prototype_Wood_Beech_Branch_Three_Corner` | Prototype : branche de hêtre - trois coins | gabarit / outil de développement | V |
| `Prototype_Wood_Beech_Branch_Three_T` | Prototype : T | gabarit / outil de développement | V |
| `Prototype_Wood_Beech_Branch_Three_T_Sideways` | Prototype : T latéral | gabarit / outil de développement | V |
| `Prototype_Wood_Beech_Branch_X` | Prototype : X | gabarit / outil de développement | V |
| `Prototype_Wood_Beech_Branch_X_Sideways` | Prototype : X latéral | gabarit / outil de développement | V |
| `Prototype_Wood_Blackwood_Fence_Platform` | Prototype : bois noir - plateforme de clôture | gabarit / outil de développement | V |
| `Prototype_Wood_Darkwood_Fence_Platform` | Prototype : bois sombre - plateforme de clôture | gabarit / outil de développement | V |
| `Prototype_Wood_Deadwood_Fence_Platform` | Prototype : bois mort - plateforme de clôture | gabarit / outil de développement | V |
| `Prototype_Wood_Drywood_Fence_Platform` | Prototype : bois sec - plateforme de clôture | gabarit / outil de développement | V |
| `Prototype_Wood_Goldenwood_Fence_Platform` | Prototype : bois doré - plateforme de clôture | gabarit / outil de développement | V |
| `Prototype_Wood_Greenwood_Fence_Platform` | Prototype : bois vert - plateforme de clôture | gabarit / outil de développement | V |
| `Prototype_Wood_Hardwood_Fence_Platform` | Prototype : bois dur - plateforme de clôture | gabarit / outil de développement | V |
| `Prototype_Wood_Lightwood_Fence_Platform` | Prototype : bois clair - plateforme de clôture | gabarit / outil de développement | V |
| `Prototype_Wood_Redwood_Fence_Platform` | Prototype : bois rouge - plateforme de clôture | gabarit / outil de développement | V |
| `Prototype_Wood_Softwood_Fence_Platform` | Prototype : bois tendre - plateforme de clôture | gabarit / outil de développement | V |
| `Prototype_Wood_Tropicalwood_Fence_Platform` | Prototype : bois tropical - plateforme de clôture | gabarit / outil de développement | V |
| `Rock_Crystal_Iridescent_Large` | Grand cristal iridescent | qualité Developer | V |
| `Rock_Crystal_Iridescent_Medium` | Cristal iridescent moyen | qualité Developer | V |
| `Rock_Crystal_Iridescent_Small` | Petit cristal iridescent | qualité Developer | V |
| `Rock_Dawnstone` | Pierre d’aube | qualité Developer | V |
| `Rock_Dawnstone_Cobble` | Pavés de pierre d’aube | qualité Developer | V |
| `Rock_Dawnstone_Cobble_Half` | Dalle en pavés de pierre d’aube | qualité Developer | V |
| `Rock_Dawnstone_Half` | Dalle en pierre d’aube | qualité Developer | V |
| `Rock_Iridescent_Brick_Beam` | Poutre en brique iridescente | qualité Developer | V |
| `Rock_Iridescent_Brick_Fence` | Clôture en brique iridescente | qualité Developer | V |
| `Rock_Iridescent_Brick_Pillar_Base` | Base de pilier en brique iridescente | qualité Developer | V |
| `Rock_Iridescent_Brick_Pillar_Middle` | Pilier en brique iridescente : milieu | qualité Developer | V |
| `Rock_Iridescent_Half` | Dalle en roche iridescente | qualité Developer | V |
| `Rock_Iridescent_Processed_Brick_Half` | Dalle en brique iridescente | qualité Developer | V |
| `Rock_Iridescent_Processed_Brick_Smooth` | Brique iridescente lisse | qualité Developer | V |
| `Rock_Iridescent_Processed_Brick_Stairs` | Escalier en brique iridescente | qualité Developer | V |
| `Rock_Iridescent_Processed_Decorative` | Brique iridescente décorative | qualité Developer | V |
| `Rock_Iridescent_Processed_Ornate` | Brique iridescente ornée | qualité Developer | V |
| `Rock_Iridescent_Processed_Ornate_Corner` | Brique iridescente ornée en angle | qualité Developer | V |
| `Rock_Iridescent_Processed_Ornate_Half` | Dalle en brique iridescente ornée | qualité Developer | V |
| `Rock_Iridescent_Processed_Ornate_Horizontal` | Brique iridescente ornée horizontale | qualité Developer | V |
| `Rock_Iridescent_Processed_Ornate_Stairs` | Escalier en brique iridescente orné | qualité Developer | V |
| `Rock_Iridescent_Stairs` | Escalier en roche iridescente | qualité Developer | V |
| `Rubble_Dawnstone` | Gravats de pierre d’aube | qualité Developer | V |
| `Rubble_Dawnstone_Medium` | Gravats de pierre d’aube moyens | qualité Developer | V |
| `Rune_Convert_Lightning` | Conversion en foudre | qualité Developer | V |
| `Rune_Convert_Wind` | Conversion en vent | qualité Developer | V |
| `Rune_Damage_Up` | Amplification des dégâts | qualité Developer | V |
| `Rune_Empty` | Pierre runique vierge | qualité Developer | V |
| `skill_page_absolute_precision` | Frappe Chanceuse : Frappe Cosmique (Unique) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_absolute_resolve` | Résolution de Fer : Résolution Éternelle (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_absolute_sight` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_abyssal_vision` | Vision Nocturne (Commun) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_active_breathing` | Vigueur Combative (Commun) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_adamantine_lungs` | Vigueur Combative : Frénésie Guerrière (Rare) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_adamantine_skin` | Corps Robuste : Corps de Diamant (Épique) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_all_seeing_eye` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_arcane_creation` | Frappe Arcanique : Genèse Arcanique (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_armored_skin` | Corps Robuste : Corps de Fer (Inhabituel) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_artisanat` | Ancienne page : Artisanat | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_athleticism` | Pas de Vent : Pas de Bourrasque (Inhabituel) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_battle_scars` | Résolution de Fer : Résolution d'Acier (Inhabituel) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_berserker_swiftness` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_combatant_swiftness` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_cosmic_reflexes` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_craft_agriculture` | Ancienne page : Agriculture | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_craft_alchimie` | Ancienne page : Alchimie | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_craft_armurerie` | Ancienne page : Armurerie | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_craft_cuisine` | Ancienne page : Cuisine | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_craft_fonderie` | Ancienne page : Fonderie | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_craft_forge_armes` | Ancienne page : Forge d'Armes | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_craft_forge_divine` | Ancienne page : Forge Divine | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_craft_recyclage` | Ancienne page : Recyclage | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_craft_scierie` | Ancienne page : Scierie | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_craft_tannerie` | Ancienne page : Tannerie | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_axe_mastery` | Maîtrise de la Hache : Maîtrise Divine de la Hache (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_blood` | Régénération Cellulaire : Sang de la Genèse (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_bulwark` | Rempart : Rempart de la Genèse (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_constitution` | Constitution : Constitution Céleste (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_dagger_mastery` | Maîtrise de la Dague : Maîtrise Divine de la Dague (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_edge` | Entaille Profonde : Tranchant de la Genèse (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_fortress` | Posture Solide : Forteresse Céleste (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_lungs` | Souffle Inépuisable : Poumons Célestes (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_mind` | Esprit Éveillé : Esprit Infini (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_parry` | Parade : Parade Céleste (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_precision` | Frappe Chanceuse : Frappe de la Genèse (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_step` | Pas de Vent : Pas Céleste (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_strike` | Frappe Chanceuse : Frappe de la Genèse (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_swiftness` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_vampirism` | Siphon Spirituel : Vampirisme Infini (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_creator_vision` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_dark_vision` | Vision Nocturne (Commun) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_deadly_precision` | Frappe Chanceuse : Frappe du Destin (Rare) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_demigod_swiftness` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_detection_of_vital_points` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_divine_aegis` | Peau de Pierre : Peau de Diamant (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_divine_reflexes` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_duelist_swiftness` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_dyna_aegis` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_eagle_eye` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_eagle_vision` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_eternal_fortification` | Peau de Pierre : Peau d'Obsidienne (Unique) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_fatal_precision` | Frappe Chanceuse : Frappe Divine (Épique) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_fate_dodge` | Esquive : Esquive Temporelle (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_fortification` | Peau de Pierre : Peau de Bronze (Inhabituel) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_fortified_skin` | Corps Robuste : Corps d'Acier (Rare) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_fortune_coins` | Chasseur de Trésors : Chasseur de Reliques (Inhabituel) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_genesis_vision` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_god_slayer_swiftness` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_golden_touch` | Chasseur de Trésors : Chasseur de Légendes (Épique) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_hardening` | Peau de Pierre (Commun) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_hawk_eye` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_heart_of_creation` | Cœur de Chêne : Coeur de la Genèse (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_iron_fortification` | Peau de Pierre : Peau de Fer (Rare) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_iron_lungs` | Vigueur Combative : Frénésie de Combat (Inhabituel) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_judgment_of_genesis` | Frappe Instinctive : Frappe de l'Annihilateur (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_keen_senses` | Frappe Chanceuse (Commun) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_koda_judgment` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_light_reflexes` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_lightning_reflexes` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_mana_heart` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_mana_well` | Esprit Éveillé : Esprit de Génie (Épique) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_marathon_runner` | Pas de Vent : Pas de Tempête (Rare) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_master_tracker` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_mauvais_presage` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_mind_reader` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_mythril_fortification` | Peau de Pierre : Peau d'Acier (Épique) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_mythril_lungs` | Vigueur Combative : Frénésie Sanguinaire (Épique) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_omni_sight` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_omniscient_precision` | Frappe Chanceuse : Frappe de la Genèse (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_omniscient_vision` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_pain_tolerance` | Résolution de Fer (Commun) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_psychic_awareness` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_razor_senses` | Frappe Chanceuse : Chance Critique (Inhabituel) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_sixth_sense` | Instinct de Survie (Commun) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_soul_reader` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_thick_skin` | Corps Robuste (Commun) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_thunder_reflexes` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_tono_squive` | Esquive : Esquive de la Réalité (Unique) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_tracker` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_true_immortality` | Ténacité : Immortalité Céleste (Légendaire) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_true_sight` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_universal_detection` | Instinct de Survie : Prophétie de Guerre (Épique) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_void_sight` | Vision Nocturne (Commun) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_war_legend` | Résolution de Fer : Résolution Divine (Épique) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_war_veteran` | Résolution de Fer : Résolution de Titan (Rare) | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `skill_page_warrior_swiftness` | Page effacée | ancienne page de passif (doublon ou passif supprimé, refonte du 2026-11-25) | E |
| `Soil_Clay_Cobble_Orange` | Pavés d’argile orange | qualité Developer | V |
| `Soil_Clay_Cobble_Orange_Half` | Dalle en pavés d’argile orange | qualité Developer | V |
| `Soil_Clay_Cobble_Orange_Stairs` | Escalier en pavés d’argile orange | qualité Developer | V |
| `Soil_Clay_Raw_Brick` | Brique d’argile brute | qualité Developer | V |
| `Soil_Clay_Raw_Brick_Half` | Dalle en brique d’argile brute | qualité Developer | V |
| `Soil_Clay_Raw_Brick_Stairs` | Escalier en brique d’argile brute | qualité Developer | V |
| `Spawn_Butterfly` | Générateur de papillons | dossier VFX | V |
| `Spawn_Goblin_Breach_Sparks` | Générateur d’étincelles de brèche gobelin | dossier WorldEvents | V |
| `Spawn_Goblin_Initial_Breach_Marker` | Marque de carte de la brèche gobelin initiale | dossier WorldEvents | V |
| `Spawn_Portal` | Portail d’apparition | dossier VFX | V |
| `Spawn_Temple_Circle` | Générateur de cercles | dossier VFX | V |
| `Spawn_Temple_Circle_Flipped` | Générateur de petits cercles inversés | dossier VFX | V |
| `Spawn_Temple_Circle_Small` | Générateur de petits cercles | dossier VFX | V |
| `Spawn_Temple_Goblin_Explosion` | Générateur d’explosions gobelin | dossier VFX | V |
| `Spawn_Temple_Goblin_Fence` | Générateur de barrière électrique gobelin | dossier VFX | V |
| `Spawn_Temple_Goblin_Fireworks` | Générateur de feux d’artifice gobelin | dossier VFX | V |
| `Spawn_Temple_Goblin_FireworksCloser` | Générateur de feux d’artifice gobelin (proche) | dossier VFX | V |
| `Spawn_Temple_Goblin_Geyser` | Générateur de geysers gobelin | dossier VFX | V |
| `Spawn_Temple_Goblin_Lightning` | Générateur de foudre gobelin | dossier VFX | V |
| `Spawn_Temple_Goblin_Sludge` | Générateur de boue gobelin | dossier VFX | V |
| `Spawn_Temple_Goblin_Sludge2` | Générateur de boue gobelin 2 | dossier VFX | V |
| `Spawn_Temple_Goblin_Smoke` | Générateur de fumée gobelin | dossier VFX | V |
| `Spawn_Temple_Goblin_Sparks` | Générateur d’étincelles gobelin | dossier VFX | V |
| `Spawn_Temple_Goblin_SparksBurst` | Générateur de jets d’étincelles gobelin | dossier VFX | V |
| `Spawn_Temple_Helix` | Générateur d’hélices | dossier VFX | V |
| `Spawn_Temple_RoofSparks` | Générateur d’étincelles | dossier VFX | V |
| `Spawn_TempleLeaves` | Générateur de feuilles | dossier VFX | V |
| `Spawn_TempleLeaves_Small` | Générateur de petites feuilles | dossier VFX | V |
| `Spawn_Wind` | Générateur de vent | dossier VFX | V |
| `Spawner_Rat` | Générateur de rats | dossier _Debug | V |
| `Spider_Cocoon_Falling` | Prototype : cocon d’araignée qui tombe | qualité Developer | V |
| `Template_Crop_Berry_Block` | Modèle de buisson à baies | gabarit / outil de développement | V |
| `Template_Crop_Block` | Modèle de culture | gabarit / outil de développement | V |
| `Template_Crop_Item` | Modèle de culture | gabarit / outil de développement | V |
| `Template_Eclispe_Sanglante` | Éclipse Sanglante | gabarit / outil de développement | E |
| `Template_Fish_Item` | Crapet arlequin | gabarit / outil de développement | V |
| `Template_Food` | Modèle de nourriture | gabarit / outil de développement | V |
| `Template_Fruit` | Modèle de fruit | gabarit / outil de développement | V |
| `Template_Glider` | Modèle de planeur | gabarit / outil de développement | V |
| `Template_Lore_Note` | Modèle de note de lore | gabarit / outil de développement | V |
| `Template_Lore_Note_Goblin_Journal_Open` | Modèle de note de lore - Pages gobelines ouvertes | gabarit / outil de développement | V |
| `Template_Lore_Note_Goblin_Pages` | Modèle de note de lore - Pages gobelines | gabarit / outil de développement | V |
| `Template_Lore_Note_Pages_A` | Modèle de note de lore - Pages A | gabarit / outil de développement | V |
| `Template_Lore_Note_Pages_B` | Modèle de note de lore - Pages B | gabarit / outil de développement | V |
| `Template_Masamune_Celeste` | Masamune Céleste | gabarit / outil de développement | E |
| `Template_Seeds` | Modèle de graines | gabarit / outil de développement | V |
| `Template_Soil` | Modèle de sol | gabarit / outil de développement | V |
| `Template_Tool_Watering_Can` | Arrosoir vide | gabarit / outil de développement | V |
| `Template_Weapon_Battleaxe` | Modèle de hache de combat | gabarit / outil de développement | V |
| `Template_Weapon_Crossbow` | Modèle d’arbalète | gabarit / outil de développement | V |
| `Template_Weapon_Daggers` | Éclipse Sanglante | gabarit / outil de développement | V |
| `Template_Weapon_Mace` | Modèle de massue | gabarit / outil de développement | V |
| `Template_Weapon_Shield` | Modèle de bouclier | gabarit / outil de développement | V |
| `Template_Weapon_Shortbow` | Modèle d’arc | gabarit / outil de développement | V |
| `Template_Weapon_Sword` | Masamune Céleste | gabarit / outil de développement | V |
| `Test_All_Rotation` | Prototype Toutes rotations | gabarit / outil de développement | V |
| `Test_Backpack` | Objet de test sac à dos | dossier _Debug | V |
| `Test_Camera_Item` | Objet de test caméra | dossier _Debug | V |
| `Test_Objective_Line_Interaction` | Carte aux trésors | gabarit / outil de développement | V |
| `Test_PlaceBlockProcedure` | Test : PlaceBlockProcedure | dossier _Debug | V |
| `Test_Spawn_Marker_Trigger` | Test de déclencheur de marqueur d’apparition | dossier _Debug | V |
| `Throwable_Explosive_Barrel_Gobliterator` | Throwable_Explosive_Barrel_Gobliterator | qualité Developer | V |
| `Tool_Growth_Potion` | Potion de stade de croissance | qualité Tool | V |
| `Tool_Sap_Shunt` | Extracteur de sève | qualité Developer | V |
| `Tree_Sap_Glob` | Goutte de sève | qualité Developer | V |
| `Utility_Bag_Seed` | Sac de graines | qualité Developer | V |
| `VFX_Dungeon_Goblin_Bouncy_Mushroom` | Générateur gobelin : champignon rebondissant | dossier VFX | V |
| `VFX_Dungeon_Goblin_Cauldron` | Générateur gobelin : chaudron | dossier VFX | V |
| `VFX_Dungeon_Goblin_Cave_Drips` | Générateur gobelin : gouttes de grotte | dossier VFX | V |
| `VFX_Dungeon_Goblin_Cave_Drips_Stalactite` | Générateur gobelin : gouttes de stalactite | dossier VFX | V |
| `VFX_Dungeon_Goblin_Electric_Fence` | Générateur de barrière électrique gobelin | dossier VFX | V |
| `VFX_Dungeon_Goblin_Energy_Barrier` | Générateur gobelin : barrière d’énergie | dossier VFX | V |
| `VFX_Dungeon_Goblin_Leaking_Pipe` | Générateur gobelin : tuyau qui fuit | dossier VFX | V |
| `VFX_Dungeon_Goblin_Leaking_Pipe_Fast` | Générateur gobelin : tuyau qui fuit (rapide) | dossier VFX | V |
| `VFX_Dungeon_Goblin_Polluted_Water` | Générateur gobelin : eau polluée | dossier VFX | V |
| `VFX_Dungeon_Goblin_Polluted_Water_Small` | Générateur gobelin : eau polluée (petite taille) | dossier VFX | V |
| `VFX_Dungeon_Goblin_Puddle_Goo` | Générateur gobelin : flaque de glu | dossier VFX | V |
| `VFX_Dungeon_Goblin_Puddle_Slime` | Générateur gobelin : flaque de slime | dossier VFX | V |
| `VFX_Dungeon_Goblin_Puddle_Water` | Générateur gobelin : flaque d’eau | dossier VFX | V |
| `VFX_Dungeon_Goblin_Smoke_Pipe_Large` | Générateur gobelin : grand tuyau à fumée | dossier VFX | V |
| `VFX_Dungeon_Goblin_Smoke_Pipe_Small` | Générateur gobelin : petit tuyau à fumée | dossier VFX | V |
| `VFX_Dungeon_Goblin_Spores_Blue` | Générateur gobelin : spores bleues | dossier VFX | V |
| `VFX_Dungeon_Goblin_Spores_Green` | Générateur gobelin : spores vertes | dossier VFX | V |
| `VFX_Dungeon_Goblin_Spores_Orange` | Générateur gobelin : spores orange | dossier VFX | V |
| `VFX_Dungeon_Goblin_Spores_Orange_Dense` | Générateur gobelin : spores orange denses | dossier VFX | V |
| `VFX_Dungeon_Goblin_Spores_Purple` | Générateur gobelin : spores violettes | dossier VFX | V |
| `VFX_Dungeon_Goblin_Spores_Skylight` | Générateur gobelin : spores de puits de lumière | dossier VFX | V |
| `VFX_Dungeon_Goblin_Stinky_Pile` | Générateur gobelin : tas puant | dossier VFX | V |
| `VFX_Dungeon_Goblin_Stinky_Pile_Small` | Générateur gobelin : petit tas puant | dossier VFX | V |
| `VFX_Dungeon_Goblin_Sun_Shafts_Large` | Générateur gobelin : grands rayons de soleil | dossier VFX | V |
| `VFX_Dungeon_Goblin_Sun_Shafts_Small` | Générateur gobelin : petits rayons de soleil | dossier VFX | V |
| `VFX_Dungeon_Goblin_Sun_Shafts_Tiny` | Générateur gobelin : minuscules rayons de soleil | dossier VFX | V |
| `VFX_Dungeon_Goblin_Temple_Motes` | Générateur gobelin : particules du temple | dossier VFX | V |
| `VFX_Dungeon_Goblin_Temple_Motes_Floor` | Générateur gobelin : particules du temple (sol) | dossier VFX | V |
| `VFX_Dungeon_Goblin_Toxic_Water` | Générateur gobelin : eau toxique | dossier VFX | V |
| `VFX_Dungeon_Goblin_Toxic_Water_Small` | Générateur gobelin : eau toxique (petite taille) | dossier VFX | V |
| `VFX_Dungeon_Goblin_Void_Cinematic_Portal` | Générateur gobelin : portail cinématique du Néant | dossier VFX | V |
| `VFX_Dungeon_Goblin_Void_Motes_Cluster` | Générateur gobelin : amas de particules du Néant | dossier VFX | V |
| `VFX_Dungeon_Goblin_Void_Rift` | Générateur gobelin : faille du Néant | dossier VFX | V |
| `VFX_Dungeon_Goblin_Void_Rift_Wind` | Générateur gobelin : vent de faille du Néant | dossier VFX | V |
| `VFX_Dungeon_Goblin_Water_Spout` | Générateur gobelin : jet d’eau | dossier VFX | V |
| `VFX_Dungeon_Goblin_Water_Spout_Splash` | Générateur gobelin : éclaboussures de jet d’eau | dossier VFX | V |
| `VFX_Eldanior_Kingdom_Barrier` | Barriere du Royaume | gabarit / outil de développement | E |
| `VoidInvasion_Portal` | Portail d’invasion du Néant | qualité Technical | V |
| `Weapon_Assault_Rifle` | Fusil | dossier Hypixel | V |
| `Weapon_Flamethrower_Scrap_Dungeon` | Weapon_Flamethrower_Scrap_Dungeon | qualité Developer | V |
| `Weapon_Grenade_Frag` | Grenade à fragmentation | dossier Hypixel | V |
| `Weapon_Gun` | Arme à feu | qualité Technical | V |
| `Weapon_Gun_Blunderbuss` | Tromblon | qualité Technical | V |
| `Weapon_Gun_Blunderbuss_Rusty` | Tromblon rouillé | qualité Technical | V |
| `Weapon_Handgun` | Pistolet | dossier Hypixel | V |
| `Weapon_Longsword_Praetorian_NPC` | Épée longue du squelette prétorien | qualité Developer | V |
| `Weapon_Longsword_Ruined_Giant_Dual_NPC` | Épées jumelles délabrées du Gardien d’outre-tombe | qualité Developer | V |
| `Weapon_Longsword_Ruined_Giant_NPC` | Épée délabrée du Gardien d’outre-tombe | qualité Developer | V |
| `Weapon_Mace_Scrap_NPC` | Massue en ferraille du duc gobelin | qualité Developer | V |
| `Weapon_Mousquet_Baionnette` | Weapon_Mousquet_Baionnette | forme temporaire d'une arme (transformation, baïonnette) | E |
| `Weapon_Shortbow_Test_Zoom` | Test : zoom de l’arc | qualité Developer | V |
| `Weapon_Test_Sniper_Rifle` | Test : zoom du sniper | dossier Hypixel | V |
| `Weapon_Test_Zoom_Rifle` | Test : zoom du fusil | dossier Hypixel | V |
