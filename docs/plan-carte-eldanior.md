# Plan : la carte du continent d'Eldanior (v2 du 2026-11-22)

> **Remplacé le soir du 2026-11-22 par la v3 (section 9)** : la carte n'est plus dessinée par `continent.py`, elle est
> tirée d'une image fournie par l'utilisateur. Plus de territoires ni de détroit : l'utilisateur fera les territoires.
> Les sections 1 à 8 restent pour l'historique et les pièges techniques.

But : une nouvelle carte pour le mod.
- Au centre, **le continent d'Eldanior** : le Royaume et 3 marquisats.
- Au sud-est, **l'île de l'Empire Luo**, séparée du continent par un **détroit**.
- Autour, un anneau d'océan, puis **le reste du monde généré automatiquement** par Hytale.

Je fais **le terrain, les biomes et la végétation**. L'utilisateur fait **les grottes et les constructions** à la main.

Aperçu retenu : `scripts/carte/apercus/continent_Finale.png`, produit par `scripts/carte/continent.py --variantes Finale`
(graine 3313).

## 1. Décisions prises

| Sujet | Décision |
|---|---|
| Forme | **Proposition C**, et l'Empire Luo devient une **île** séparée par la mer |
| Centre | **Royaume d'Eldanior**, avec la capitale sur un **plateau** dégagé |
| Marquisats | **3** : Nord, Ouest, Est (noms définitifs à donner, ce seront les noms affichés à l'entrée) |
| Empire Luo | territoire du clan Luo, **au sud-est**, de l'autre côté du **Détroit de Luo** |
| Structures | **nature seulement** (rochers, arbres, souches), aucun camp, village ni ruine de Hytale |
| Climats | variés, mais **cohérents avec la position** |
| Grottes | **aucune sur le continent ni sur l'île de Luo**, celles de Hytale restent ailleurs |
| Ancien monde | pas de transfert maintenant (prefabs plus tard, par l'utilisateur) |

## 2. La carte retenue

Image de 12 000 × 12 000 blocs, Nord en haut. Terres ≈ 67 km² ; le continent fait environ 9 500 blocs d'un bout à l'autre.

| Territoire | Part des terres | Climat | Paysage prévu | Biomes Hytale de base |
|---|---|---|---|---|
| **Royaume d'Eldanior** (centre) | 22 % | tempéré | plateau de la capitale, plaines, collines, bosquets, petits lacs | Zone 1 : `Plains_Smooth`, `Plains_Tallgrass`, `Forest_Birch`, `Forest_Flower`, `Lake` |
| **Marquisat du Nord** | 21 % | froid | taïga de sapins, toundra et neige au bord de la mer du nord | Zone 3 : `Forest_Fir`, `Forest_Redwood`, `Forest_Tundra`, `Plains_Frozen`, `Plains_Tundra` |
| **Marquisat de l'Ouest** | 21 % | sec, derrière la grande chaîne | savane, désert, canyons rouges, oasis | Zone 2 : `Savannah_Plains`, `Savannah_Rock`, `Desert_Red`, `Desert_Oasis`, `Scrub_Bushland` |
| **Marquisat de l'Est** | 18 % | tempéré frais | forêts de bouleaux et de trembles, collines, deux grands lacs | Zone 1 : `Forest_Aspen`, `Plains_Birch`, `Forest_Gully`, `Lake_Chalk` |
| **Empire Luo** (île) | 19 % | humide, doux | chaîne montagneuse face au détroit, bambouseraies, plaines humides, glycines | Zone 1 humide (`Forest_Moss`, `Lake_Moss`) + **motifs de bambous** de la Zone 4 (`Jungle_Bamboo`) + **arbres glycine** (`Prefabs/Trees/Wisteria`) |

Le climat de l'Ouest s'explique : la grande chaîne de montagnes retient la pluie, d'où un pays sec.

**Frontières :**

| Entre | Frontière | Passages |
|---|---|---|
| Royaume et Ouest | **grande chaîne de montagnes** enneigée, du nord au sud | 2 ou 3 cols |
| Royaume et Nord | **ceinture de forêt dense** | sentiers là où elle s'éclaircit |
| Royaume et Est | **ceinture de forêt dense**, qui finit sur une baie du détroit | sentiers |
| Nord et Ouest | **chapelet de lacs** et marais froid | gués entre les lacs |
| Nord et Est | **montagnes** côtières | 1 col |
| Continent et Luo | **Détroit de Luo** (≈ 500 à 850 blocs de mer), puis **chaîne côtière** sur l'île | **par bateau** uniquement, ou par un pont / port construit par l'utilisateur |

## 3. Comment c'est construit : trois couches

### Couche 1 : le dessin (fait, à valider)

`scripts/carte/continent.py` (Python sans dépendance) dessine la carte à partir d'une graine :
1. un disque déformé pour la côte, avec deux niveaux de détail ;
2. des territoires en cellules de puissance, aux bords déformés ;
3. pour chaque paire de territoires, une frontière : montagnes avec cols, forêt, lacs, ou détroit pour Luo ;
4. le plateau de la capitale, des lacs intérieurs, et les biomes selon le climat de chaque territoire.

**À ajouter avant la génération** : la sortie `Mask.png`, aux couleurs exactes de `Zones.json` (une couleur par zone et sans
anticrénelage), au même cadrage que l'aperçu.

### Couche 2 : le générateur Hytale du continent (fichiers du mod)

Dans `src/main/resources/Server/World/Eldanior/` :
- `World.json` : masque de 12 000 × 12 000 centré sur 0,0, avec `OverrideDataFolder: ../Default/` pour hériter des réglages
  de Hytale. Modèle dans les assets : `World/Instance_Default_Old`.
- `Zones.json` : couleur → zone.
- **Une zone par territoire**, créée en copiant la zone de Hytale qui lui ressemble :
  - `Discovery.ZoneName` = nom du territoire (affiché à l'entrée) ;
  - **pas de dossier `Cave`** ;
  - dans `PrefabPatterns`, **seulement la nature** (arbres, rochers, souches) : on retire camps, villages, ruines et
    monuments ;
  - biomes réduits à ceux du tableau ci-dessus.
- **Zones de relief** :

  | Zone | Contenu |
  |---|---|
  | `Eldanior_Montagnes` | biomes `Mountain_*`, relief renforcé, sommets enneigés au nord |
  | `Eldanior_Foret_Frontiere` | forêt très dense |
  | `Eldanior_Lacs` | `Lake_*` et marais |
  | `Eldanior_Plateau` | la capitale : terrain plat et surélevé |
  | `Eldanior_Cote` | plages et côtes rocheuses |
  | `Eldanior_Detroit` | eau profonde |

### Couche 3 : le reste du monde

Le mod enregistre un générateur `Eldanior`, qui choisit selon la distance au centre :
- **jusqu'à ≈ 6 000 blocs** : la couche 2 ;
- **au-delà** : le générateur normal de Hytale (`Default`, avec ses grottes et ses structures) ;
- **dans une bande de transition** (≈ 300 blocs) : les terres de Hytale sont abaissées sous la mer, pour éviter un mur
  visible.

Repli si le branchement échoue : un océan infini avec les îles de la zone `Oceans` de Hytale.

## 4. Les phases (une validation de ta part à chaque fin de phase)

| # | Phase | Ce que tu reçois | État |
|---|---|---|---|
| 1 | Forme, territoires, frontières (aperçus) | `continent_Finale.png` | **à valider** |
| 0 | **Essais techniques** sur un serveur isolé : monde ajouté par le mod, masque image, zone sans grottes, nom à l'entrée, bambous et glycines, détroit, jonction avec le reste du monde | un mini-continent (≈ 2 000 blocs, mêmes ingrédients) à ouvrir en solo | à faire |
| 2 | **Zones des 5 territoires** et des zones de relief (biomes, nature seulement, noms) | sauvegarde d'essai : le plateau et un morceau de chaque territoire | à faire |
| 3 | **Frontières en vrai** : hauteur des chaînes, cols, lacs, forêts, détroit | vue du dessus prise sur les vrais blocs, plus quelques lieux à visiter | à faire |
| 4 | **Océan et reste du monde** | sauvegarde d'essai au bord du continent | à faire |
| 5 | **Génération complète**, point d'apparition sur le plateau, vue du dessus du continent entier | la sauvegarde « Eldanior » prête à construire | à faire |
| 6 | **Liens avec le mod** (au choix) | — | à discuter |

Pistes pour la phase 6 :
- bornes et domaines alignés sur les 5 territoires ;
- Minerai Divin : le `Modifier` doit viser les nouvelles zones ;
- monstres selon le territoire ;
- lieux des Boss Mondiaux ;
- carte du royaume ;
- téléporteur continental vers l'île de Luo.

**Outil de contrôle** à chaque phase : une vue du dessus fabriquée à partir des **vrais blocs** générés (comme pour
Downston), comparée à l'aperçu.

## 5. Ordres de grandeur

- Continent et île ≈ 67 km², soit environ **66 000 chunks**. La pré-génération prendra de quelques dizaines de minutes à une
  heure sur un serveur isolé. Taille : **plusieurs Go**.
- Le reste du monde se génère au fil de l'exploration.

## 6. Questions encore ouvertes

1. **Noms des 3 marquisats** (et éventuellement des chaînes de montagnes, du détroit, des lacs) : affichés à l'entrée.
2. **Passage vers Luo** : uniquement par bateau, ou bien un isthme, un pont naturel ou un gué rocheux à marée basse ?
3. **Hauteur des montagnes** : le monde est limité à 320 blocs de haut. Grande chaîne de l'Ouest très haute (≈ 220) ou
   modérée (≈ 160) ?

## 7. Risques connus

- **Monde ajouté par le mod** (`Server/World/Eldanior`) : à confirmer en phase 0. En repli, notre propre générateur lit le
  masque.
- **Jonction avec le reste du monde** : le point le plus délicat. Le repli « océan infini avec îles » est prêt.
- **Bambous et glycines** : ils viennent de motifs d'autres zones (Zone 4, prefabs d'arbres). Leur rendu dans une zone
  tempérée est à vérifier en phase 0.
- **Rivières** : celles de Hytale sont des biomes. Une rivière qui suit exactement un tracé demandera de creuser le lit dans le
  générateur `Eldanior`. Ce n'est pas prévu pour l'instant : les frontières sont des montagnes, des forêts, des lacs ou la mer.
- **Le masque doit avoir des couleurs exactes** : une couleur inconnue dans `Zones.json` donne une zone par défaut. Le script
  écrira le PNG sans adoucissement et vérifiera la palette.

## 8. Résultats de la phase 0 (2026-11-22)

Les essais ont tourné sur un serveur isolé dans le scratchpad, en priorité minimale (une partie était en cours), sans toucher
au jar du mod ni à `UserData/Mods`.

**Outils (dans `scripts/carte/`)**

| Fichier | Rôle |
|---|---|
| `continent.py --variantes Finale --px 1200 --masque <dossier>` | écrit `Mask.png` (une couleur exacte par zone, 10 blocs/px), `zones.json` et `Relief.bin` (soulèvement 0 à 255 blocs) |
| `gen_monde.py <dossier masque> <racine pack>` | écrit `Server/World/Eldanior/` (World.json, Zones.json, Mask.png, Relief.bin), les 15 zones `Server/World/Default/Zones/Eldanior_*` lues dans `Assets.zip` (sans `Cave`, nature seulement, objets retirés des couvertures) et le bloc de noms dans `server.lang` |
| `java/com/eldanior/carte/EldaniorWorldGen.java` | générateur `Eldanior` (voir ci-dessous) |
| `java/com/eldanior/carte/CarteOutil.java` | outil d'essai : `-Dcarte.box=x0,z0,x1,z1` pré-génère la zone, `-Dcarte.dump=<fichier>` relève le bloc du dessus de chaque colonne, puis arrête le serveur |
| `vue_dessus.py <relevé> <png> [--masque Mask.png] [--pas N]` | vue du dessus à partir des vrais blocs, avec les limites des zones prévues tracées en rouge |

**Ce qui marche (vérifié sur les blocs générés)**
- Le continent dessiné est bien généré par Hytale : côtes, plages, détroit, rivières et lacs de Hytale dans le Royaume, savane
  et désert à l'Ouest, taïga au Nord, glycines et bambous chez Luo. **Aucune grotte**, et les monuments, camps et PNJ sont
  retirés des biomes.
- **Plateau de la capitale** : sol à environ 144, contre environ 120 pour les plaines, avec une rampe douce.
- **Montagnes** : jusqu'à environ 260 de haut, avec des crêtes, des pics et de la neige au-dessus de 200 à 244 selon la
  latitude. **Aucun mur** aux limites des zones.
- **Reste du monde** : au-delà de 6 000 blocs, c'est le générateur normal de Hytale (avec ses structures). Entre 6 000 et
  6 400 blocs, ses terres sont abaissées sous la mer, avec une côte déformée par du bruit.
- **Vitesse** : environ 70 à 100 chunks par seconde en priorité minimale. Un essai de 22 800 chunks (4,8 × 4,8 km) prend
  3,5 min et pèse 191 Mo. Le continent complet (≈ 140 000 chunks dans le carré de 12 km) devrait prendre environ 25 min et
  peser environ 1,2 Go.
- Une sauvegarde rechargée **sans** notre générateur (Void) est complète : aucune colonne vide.

**Trois découvertes qui ont changé la méthode**
1. Hytale n'accepte un monde nommé (`WorldGen: {Type: Hytale, Name: X}`) que s'il existe **dans ses propres assets**. Le
   générateur `Eldanior` construit donc lui-même le `ChunkGenerator` depuis le pack du mod. Il ouvre d'abord un
   `AssetFileSystem` : `World.json` est lu avant que le chargeur n'ouvre le sien, et les fichiers sont ensuite cherchés dans
   tous les packs.
2. **Aucune transition de hauteur entre deux zones** : on a mesuré un passage de 142 à 114 en 8 blocs. Les zones ne donnent
   donc que les sols, plantes et arbres, à hauteur normale. Le relief (chaînes, plateau) vient de `Relief.bin`, appliqué par
   le générateur : il décale la colonne vers le haut, ajoute des crêtes en bruit « ridged », puis la neige.
3. Il faut enregistrer le type `Eldanior` **avant** le chargement des mondes. Dans les essais, le plugin le fait dans
   `setup()`. Attention : un serveur qu'on arrête réécrit `config.json`, donc il faut attendre son arrêt avant de modifier la
   configuration.

**Sauvegarde d'essai** : `Saves/Eldanior_Essai_Continent`, en mode Créatif. Elle couvre de x −2 800 à 2 000 et de z −1 200 à
3 600 ; au-delà, c'est le vide (Void), puisque cette sauvegarde n'a pas besoin du générateur. Repères :
- capitale en (0, 144, 0) ;
- chaîne de l'Ouest vers x = −2 000 ;
- détroit au sud-est de la capitale (z ≈ 800 à 2 000) ;
- montagnes de Luo vers z ≈ 2 000 à 2 800 ;
- intérieur de Luo vers (500, 3 300).

**Pas encore vu en jeu** : les noms affichés à l'entrée des territoires, l'aspect réel des pentes et des neiges, et les
bambous.

**À faire en phase 2 et après** :
- intégrer `EldaniorWorldGen` dans le mod, avec enregistrement du codec dans `EldaniorSystem.setup()` ;
- ajouter les fichiers générés dans `src/main/resources` ;
- côte de jonction : encore assez droite par endroits ;
- les arbres restent verts sur la neige ;
- largeur des chaînes ;
- les montagnes du Nord-Est sont coupées en deux zones (Neige et Montagnes).

**Continent complet (2026-11-22, le soir)** : `Saves/Eldanior_Continent` (« Eldanior - continent », mode Créatif).
- Le carré de −6 400 à 6 400 est entièrement pré-généré : 160 000 chunks en 25 min, 1,6 Go. Il contient le continent,
  l'île de Luo, l'anneau d'océan et, dans les coins, un début du monde normal de Hytale.
- Au-delà du carré, c'est le vide (Void), tant que le générateur `Eldanior` n'est pas dans le mod.
- Contrôle : la sauvegarde relue sans générateur est complète (0 section absente).
- Vue du dessus prise sur les vrais blocs : `scripts/carte/apercus/continent_genere.png`.
- `CarteOutil` travaille par lots de 2 048 chunks, relève chaque chunk dès qu'il est chargé (`-Dcarte.apercu`), et attend
  qu'il reste moins de 6 000 chunks chargés avant le lot suivant. Sinon la mémoire déborde à la relecture.

## 9. v3 : la carte tirée d'une image (2026-11-22, le soir)

Demande : reproduire la carte de `scripts/carte/sources/continent_reference.png` (960 × 960 px, tirée de l'image de
présentation d'une map Minecraft « Kintsukuroi », dans `~/Documents/Maps`). Plus de 10 000 × 10 000 blocs, terrain,
végétation et biomes cohérents, plages au bord de l'eau, plus de pics et de relief, rivières, lacs et fleuves respectés,
forêts et biomes variés, **monde normal de Hytale au-delà**. Pas de séparation pour l'Empire Luo : l'utilisateur fera les
territoires.

**Chaîne**

| Étape | Outil | Résultat |
|---|---|---|
| 1 | `carte_image.py <dossier>` | `Mask.png` (15 zones, 13 blocs/px), `Terrain.bin` (gzip, 1 920 × 1 920, 6,5 blocs/px), `zones.json`, `apercu.png` |
| 2 | `gen_monde.py <dossier> src/main/resources` | `Server/World/Eldanior/` + `Server/World/Default/Zones/Eldanior_*` (recette `RECETTES_IMAGE`) |
| 3 | mod : `com.eldanior.system.carte.EldaniorWorldGen` | générateur `Eldanior`, enregistré dans `EldaniorSystem.setup()` |
| 4 | `java/com/eldanior/carte/CarteOutil.java` (outil, serveur isolé) | pré-génération de la carte entière, vue d'ensemble |

**Ce que `carte_image.py` déduit de l'image**
- Classes de couleur : eau (bleu et bleu-vert des rivières), neige, roche, forêt sombre, prairie, collines, sec.
- La mer est l'eau reliée au bord de l'image (après une ouverture de 3 px). Le reste est l'eau intérieure.
- Le relief combine un indice de montagne par couleur (lissé à deux échelles) et les crêtes retrouvées dans l'ombrage de
  l'image. L'ombrage est intégré le long de la direction de la lumière, trouvée automatiquement : elle vient du nord.
  Les sommets sont compressés en douceur vers 152 blocs au-dessus de la mer.
- Les rivières : leur niveau monte de l'embouchure vers la source, sans jamais remonter vers l'aval. Les lacs sont plats.
  Le terrain descend vers chaque cours d'eau : la vallée est large en plaine et étroite en montagne.
- La limite des neiges est la médiane des hauteurs des pixels blancs de l'image (Y 212 environ).
- Les zones : Nord froid (taïga, prairies du Nord), centre tempéré (plaines, collines), Sud plus doux (forêts d'automne).
  Il y a aussi des forêts profondes, des montagnes et des sommets, et des bambouseraies avec glycines au sud-est. Les
  limites sont déformées par du bruit.

**Ce que fait le générateur** (sur chaque chunk du continent)
1. Hytale produit les sols, les plantes et les arbres de la zone **sur un sol plat** : Y 120 sur terre et Y 100 sous la mer.
   Tous les biomes sont aplatis par `gen_monde.py`, et aucun n'a de rivière ni de grotte.
2. Chaque colonne est déplacée à la hauteur lue dans `Terrain.bin`, plus un bruit de détail : crêtes « ridged » qui
   aiguisent les pics en montagne, collines douces ailleurs. Les arbres suivent le décalage de leur tronc (rayon 7), pour
   ne pas être cisaillés sur les pentes.
3. Ensuite :
   - l'eau : la mer à Y 114, les rivières et les lacs à leur niveau ;
   - le sable des plages (gravier par endroits au nord) et le sable ou gravier au bord des rivières ;
   - un lit d'argile, de gravier ou de sable sous les rivières ;
   - la roche sur les pentes raides ;
   - la neige au-dessus de la limite, sans arbres juste en dessous.
4. Au-delà de `Rayon` = 6 100, mesuré en carré arrondi : le générateur normal de Hytale. Entre 6 100 et 6 600, ses terres
   sont abaissées sous la mer (côte déformée par du bruit).

**Ordres de grandeur** : image de 12 480 blocs, continent d'environ 11 200 × 11 000 blocs, 85 km² de terres. Un essai de
2 304 chunks prend 8 s.

**Résultat du 2026-11-22** :
- `Saves/Eldanior_Carte` (« Eldanior - carte », mode Créatif, seed 1503, `WorldGen: Eldanior`, mod activé).
- Le carré de −6 656 à 6 656 est pré-généré : 173 056 chunks en 19,5 min, 1,9 Go. Au-delà, le mod génère le monde à la
  volée : la carte jusqu'à 6 100, puis le monde de Hytale.
- Vue sur les vrais blocs : `scripts/carte/apercus/carte_generee.png`. Aperçu prévu : `carte_prevue.png`.
- Contrôle : un serveur isolé avec le seul jar du mod charge le monde avec le générateur `Eldanior` sans erreur.
- Pour refaire : lancer `carte_image.py`, puis `gen_monde.py` vers `src/main/resources`, puis `./gradlew jar` (jeu fermé),
  puis pré-générer avec `CarteOutil` sur un serveur isolé. Le monde doit avoir `IsUnloadingChunks: true`, sinon l'outil
  attend sans fin le déchargement des chunks.

**Points à vérifier en jeu** (non vus) : les arbres sur les pentes ; les grands fleuves (environ 40 à 60 blocs de large) ;
la mer au bord de la carte (jonction avec le monde de Hytale) ; la carte du jeu (touche M), qui montre les couleurs des
zones et pas le relief.
