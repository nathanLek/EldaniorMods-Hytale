# Frontières dynamiques — idée développée le 2026-11-19 (rien n'est codé)

Question de l'utilisateur : deux villes A et B (cylindres de 1000 × 1000) se recouvrent en partie. Peut-on redéfinir la frontière
de la partie commune, et s'en servir plus tard pour des guerres de territoire, puis plusieurs royaumes et des guerres de royaumes ?

**Réponse courte : oui.** Aujourd'hui une zone est une forme fixe (rectangle ou cylindre) et deux sœurs du même type ne peuvent
pas se chevaucher (contrôle de `BornePage.save`). Pour des frontières qui bougent, il faut séparer deux choses :

1. **la revendication** : la forme posée par la borne (ce qu'une ville *prétend* tenir) — elle ne change pas ;
2. **le contrôle** : à qui appartient *vraiment* chaque morceau de terrain — c'est lui qui bouge.

## 1. Le terrain découpé en cases

On découpe le monde en cases de **16 × 16 blocs** (un demi-chunk ; 32 × 32 pour aller plus vite). Pour chaque case dans au moins
une revendication, on garde **un seul propriétaire** (l'id de la zone qui la contrôle). Une ville de 1000 × 1000 = ~3 000 cases :
léger en mémoire (une table « case → zone »), rapide à consulter (`contains` devient « la case est-elle à moi ? »).

- Case revendiquée par **une seule** ville : elle est à elle (rien ne change par rapport à aujourd'hui).
- Case revendiquée par **plusieurs** villes : **terre contestée**, attribuée selon la règle choisie ci-dessous.

## 2. Qui prend la terre contestée (au choix de l'utilisateur)

| Règle | Principe | Effet en jeu |
|---|---|---|
| **A. Ligne médiane** (Voronoï) | la case va à la borne la plus proche | frontière droite au milieu, calcul instantané, aucune guerre |
| **B. Poids des villes** | distance ÷ puissance (population, trésor, rang du seigneur) | une grande ville repousse la frontière d'une petite |
| **C. Influence** | chaque ville a des points d'influence par case, qui montent avec la présence de ses habitants, ses bâtiments, ses gardes | la frontière bouge seule, lentement, selon qui *occupe* le terrain |
| **D. Conquête** | la case appartient à qui la tient militairement (voir § 4) | base des guerres de territoire |

Proposition : **A au départ** (ce qu'il faut pour que deux villes se touchent sans erreur), puis **C + D** quand les guerres arrivent.
La règle B est un bon intermédiaire si l'on veut que la noblesse compte.

## 3. Ce que ça change pour le reste du mod

- `ParcelData.contains` → pour les domaines (ville, territoire, Marquisat, royaume) : « forme ET case contrôlée ». Les biens
  (parcelles, logements, chambres, fermes, mines) gardent leur forme exacte : un bien est toujours dans une seule ville.
- **Taxe du commerce, lois, couvre-feu, bannis, annonces, invasions** : déjà basés sur « dans quelle zone suis-je » → ils suivent
  automatiquement la nouvelle frontière.
- **Carte** : la frontière n'est plus un cercle mais un contour en escalier de cases → à dessiner case par case (marqueurs aux
  coins, ou contour en fil de fer comme celui des bornes, déjà fait le 2026-11-19).
- **Biens à cheval** : un bien dont le centre passe chez le voisin change de ville (ses taxes aussi), ou bien on interdit de poser
  un bien dans une case contestée. À trancher.
- Recalcul : à chaque pose/déplacement de borne (règle A/B), ou toutes les heures (règle C).

## 4. Guerres de territoire (plus tard)

Bâties sur la règle D :

1. **Déclaration de guerre** chez l'intendant (Patriarche / Comte), coût en or, délai de préparation (ex. 24 h), annonce à tous.
2. **Bannières de siège** : objet posé dans une case ennemie *contestée ou frontalière* ; elle se charge si des soldats de
   l'attaquant restent autour (comme une capture de point), se vide si des défenseurs s'en approchent.
3. Bannière chargée → la case passe à l'attaquant ; les cases voisines deviennent attaquables (on avance de proche en proche,
   pas de saut au milieu du territoire).
4. **Cœur de la ville** : la borne de ville. Si toutes les cases autour sont prises et la borne tenue N minutes → la ville tombe
   (elle change de maître, son trésor est pillé en partie, comme l'invasion perdue).
5. **Trêve** obligatoire après une guerre ; PvP permis dans les cases contestées pendant la guerre seulement.

## 5. Plusieurs royaumes et guerres de royaumes

- Un royaume = une borne de royaume (déjà possible), avec son roi et sa maison. Le contrôle des cases remonte la hiérarchie :
  une ville conquise par le royaume B quitte le royaume A avec ses biens.
- Les Marquisats (grands territoires) deviennent les **fronts** : la guerre de royaumes = des guerres de territoire entre
  Marquisats frontaliers, la capitale tombant en dernier.
- Diplomatie à prévoir : alliance, vassalité (un royaume vaincu paie un tribut, comme la Taxe Impériale de Luo), paix signée chez
  l'intendant.

## 6. Ordre de travail proposé

1. Table des cases + règle A, contrôle au lieu de l'interdiction de chevauchement (petit chantier, aucun effet visible hors des
   zones qui se recouvrent).
2. Contour des cases sur la carte et en fil de fer.
3. Règle C (influence) si l'utilisateur la veut.
4. Guerres de territoire (règle D), puis guerres de royaumes.

**Questions pour l'utilisateur** : quelle règle pour la terre contestée (A, B, C) ? Taille des cases (16 ou 32) ? Un bien à
cheval change-t-il de ville ou est-il interdit ?
