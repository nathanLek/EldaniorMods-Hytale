# Ressources Manquantes (Assets & JSON)

#bug #haute #ressources #items #assets

## 1. Decret_Marquis.json — champs manquants
**Fichier** : `src/main/resources/Server/Item/Items/Food/Decret_Marquis.json`

**Probleme** : Il manque les proprietes `Model` et `Texture` presentes dans les autres Decrets.

| Item | Model | Texture | Status |
|------|-------|---------|--------|
| Decret_Baron.json | `Scrolls.blockymodel` | `Scrolls_Texture.png` | OK |
| Decret_Chevalier.json | `Scrolls.blockymodel` | `Scrolls_Texture.png` | OK |
| Decret_Comte.json | `Scrolls.blockymodel` | `Scrolls_Texture.png` | OK |
| Decret_Duc.json | `Scrolls.blockymodel` | `Scrolls_Texture.png` | OK |
| Decret_Marquis.json | `Scrolls.blockymodel` | `Scrolls_Texture.png` | CORRIGE |

### Correction
Ajouter dans Decret_Marquis.json :
```json
"Model": "Blocks/Miscellaneous/Scrolls.blockymodel",
"Texture": "Blocks/Miscellaneous/Scrolls_Texture.png"
```

---

## 2. Assets references mais inexistants (29 fichiers)

### Modeles .blockymodel manquants (7)
| Modele | Reference par |
|--------|-------------|
| `Blocks/Miscellaneous/Scrolls.blockymodel` | 5 items Decret |
| `Items/Consumables/Food/Egg.blockymodel` | 18+ consommables |
| `Blocks/Miscellaneous/Map.blockymodel` | PortalKey |
| `Blocks/Benches/Memory_Bench.blockymodel` | Bench_Class_System |
| `Blocks/Miscellaneous/Portal_Shard.blockymodel` | PortalKey_Dungeon_V1 |
| `Items/Coins/Elda_Coins.blockymodel` | 5 types de pieces |
| `Items/Consumables/Recipes/Recipe.blockymodel` | Items recettes |

### Textures .png manquantes (11)
| Texture | Reference par |
|---------|-------------|
| `Scrolls_Texture.png` | Decrets |
| `Map_Texture.png` | PortalKey |
| `Consomable_Stats_Force_One.png` | Stats consumables |
| `Recipe_Texture.png` | Recettes |
| `Memory_Bench_Texture.png` | Bench |
| `Portal_Shard_Texture.png` | PortalKey |
| `Elda_Copper_Coins.png` | Piece de cuivre |
| `Elda_Diamond_Coins.png` | Piece de diamant |
| `Elda_Gold_Coins.png` | Piece d'or |
| `Elda_Silver_Coins.png` | Piece d'argent |
| `Elda_Zenith_Coins.png` | Piece zenith |

### Icones .png manquantes (11)
| Icone | Reference par |
|-------|-------------|
| `Deco_Scroll.png` | Icone Decrets |
| `Food_Egg.png` | Icone consommables |
| `Deco_Map.png` | Icone carte |
| `Bench_Memories.png` | Icone bench |
| `PortalKey_Howling_Sands.png` | Icone portail |
| `Recipe_Page.png` | Icone recette |
| `Elda_Copper_Coins_Icone.png` | Icone cuivre |
| `Elda_Diamond_Coins_Icone.png` | Icone diamant |
| `Elda_Gold_Coins_Icone.png` | Icone or |
| `Elda_Silver_Coins_Icone.png` | Icone argent |
| `Elda_Zenith_Coins_Icone.png` | Icone zenith |

## Note importante
Ces assets sont probablement fournis par **Hytale lui-meme** (assets du jeu) et non par le plugin. Ils peuvent exister dans le runtime Hytale mais pas dans les sources du plugin. A verifier en jeu.

Si ce sont des assets **custom** du plugin, il faut les creer ou les sourcer.

---

## 3. Comptage des items
- **35 items consommables** trouves (vs 34 dans la doc) — verifier s'il y en a un en trop ou si la doc est en retard
- Tous les JSON sont syntaxiquement valides
- Toutes les interactions (.json) sont correctement referencees

---

## Priorite
**HAUTE** — Le Decret_Marquis ne s'affiche pas correctement en jeu

## Liens
- [[Systems/Noblesse]] - Decrets royaux
- [[Items/Consommables]] - Liste des consommables
- [[Config/Monnaie]] - Pieces de monnaie