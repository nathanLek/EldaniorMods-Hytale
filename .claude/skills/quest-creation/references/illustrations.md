# Illustrations des cartes de quête

Générées en Python pur (aucune dépendance) par `scripts/quetes/gen_chapitres_ui.py`, style **vitrail** automatique
(`paint_art` : cellules de Voronoï, plomb sombre, vignettage) + version grisée pour les cartes non choisies.

## Fichiers produits

- `src/main/resources/Common/UI/Custom/Quest/ChapArt<Nom>C@2x.png` (couleur) et `...G@2x.png` (grisée), 334 × 604 px,
  découpe de la carte du client (`art_mask`, alpha de `ChapterArtGoblin.png`).
- `Chapitres.ui` : 6 groupes par thème (`#ChapArt<Nom>{C|G}{0..2}`), régénéré automatiquement par `--art=`.
  `QuestChaptersPage` affiche celui de `a.art` (`artId` : `TRORK` → `Trork`). **Pas de tiret bas** dans le nom
  (`#ChapArtRoi_Fou` casserait le `.ui`).

## Commandes

```
python3 scripts/quetes/gen_chapitres_ui.py --art=Dojo --art=Vent --planche=<scratchpad>/planche.png   # ~4 s par image
python3 scripts/quetes/gen_chapitres_ui.py                                                           # tout (lent)
```

## Écrire une scène

```python
def scene_xxx(u, v, rnd):
    """Quete / etape : ce que montre l'image (date)."""
    x, y = u * AW, v * AH                                  # pixels 0..334 x 0..604
    col = vgrad([(0, "0b0c1a"), (0.5, "191b31"), (1, "141527")], v)   # ciel / mur
    if y > ridge(u, 330, 14, 7, 15, 0.4): col = hexc("9fb86a")          # collines
    ...                                                    # couches suivantes : chaque test ecrase la couleur
    return col                                             # [r, g, b] (0-255)
```

Puis `ARTS += ["Xxx"]`, `SCENES["Xxx"] = scene_xxx`, et `Art.XXX` dans `ChapterQuests`.

Outils disponibles : `vgrad`, `mix`, `hexc`, `ridge` (ligne de crête), `seg_dist` (segments épais : membres, armes,
branches), `inpoly` (polygones), `rot` (rotation de points), `rnd(i, j)` (aléa stable : étoiles, pétales, herbes),
`_person(x, y, cx, pieds, échelle, main, pointe, hood=, beard=, wide=)` (combattant de face, arme à deux mains ;
renvoie la partie touchée → `_paint(col, partie, palette)`), `_zombie`, `_deer`, `_trork` (silhouettes déjà faites).

## Règles de composition (apprises sur Trône, Dojo, Vent, Brigands, Trork, Sensei)

- Le sujet entre **y = 40 et y = 470** : le bas de la carte est assombri et porte le nom de l'étape.
- Un fond qui dit le lieu et l'heure, un sujet principal grand et contrasté, 1-2 détails qui disent l'objectif
  (couronne tombée, défenses arrachées, coffre pillé…).
- Les cellules du vitrail mangent les petits détails : rien de plus fin que ~3 px, pas de texte.
- **Contraste** : liseré de lumière sur les silhouettes (masque à `x ± 3` : `not _person(x - 3, ...)`), contour sombre des
  animaux ; une silhouette noire sur fond sombre disparaît (le Sensei a dû passer en couleurs).
- Les têtes passent **devant** les bras levés (`_person` le gère).
- Un masque réutilisable par créature (`_trork(x, y)`) plutôt qu'un test répété : le liseré se calcule sur tout le corps.
- Toujours regarder la planche avant de livrer ; corriger ce qui est illisible.

## Thèmes existants

| Art | Image | Utilisée par |
|---|---|---|
| AUBE | aube, torii, bambous, bûches | Un Œuf venu d'ailleurs (Dresseur), affiches du jour |
| NUIT | cimetière, lune, crâne | contrats de monstres (Primes) |
| DUEL | deux bretteurs, dojo, pétales | avis de recherche (Primes) |
| TRONE | salle du trône du Roi Fou, fantôme, couronne tombée | Principale « Les Secrets du Roi Fou » |
| DOJO | dojo, rouleau, 3 disciples au Bokken | Épreuves du Dojo 1 |
| VENT | plaines venteuses, cerf en bond, lièvre, renard | Épreuves du Dojo 2 |
| BRIGANDS | camp de nuit, feu, 3 bandits encapuchonnés | Épreuves du Dojo 3 |
| TRORK | crépuscule, Trork à massue, Grand Maître au katana d'aura | Épreuves du Dojo 4 |
| SENSEI | Sensei à barbe blanche devant une lune et un ensō | Épreuves du Dojo 5 |
| LISIERE | crépuscule, sapins, deux loups aux yeux jaunes, clôture, mouton | Les Crocs de la Lisière |
| MARAIS | marais vert, arbres morts, crapaud rhino, ronceux, lucioles | Remèdes du Marais |
| FORGE | fourneau rougeoyant, enclume et lame chauffée, brasero d'os, cœur de braise | Braises pour la Forge |
| DUNES | soleil couchant, caravane brisée, zombies en bandages | Les Morts du Désert |
| OUTLANDERS | aurore polaire, guerrier à cornes et hache, mystique au talisman, totem | Trophées pour un Collectionneur |
| ARENE | arène de guilde, foule, bannières, écusson, deux duellistes | Le Droit d'Entrée |
| VIDE | faille, œil géant à pupille fendue, tentacules, prismes | L'Œil du Vide |
| OMBRE | lumière braise / glace, Croc du Néant au-dessus de l'enclume, noyau, heaume | L'Acier de l'Ombre |

Silhouettes réutilisables ajoutées : `_wolf(x, y, cx, sol, échelle, sens)`.
