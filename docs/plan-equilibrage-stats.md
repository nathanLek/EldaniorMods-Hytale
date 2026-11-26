# Plan : équilibrage des stats et refonte de l'esquive

Rédigé le 2026-11-25. Décisions prises le jour même (§ 4) ; étapes 0, 1 et 2 codées et compilées le 2026-11-25,
jar NON reconstruit, non vues en jeu (§ 5).

Fichiers concernés : `config/configs/StatConfig.java` (formules), `Leveling/systems/CombatStatsSystem.java` (combat),
`Leveling/systems/LuckSystem.java` (critique), `skills/skillsInteraction/PassiveEffects.java` (passif Esquive),
`guards/AdventurerPassives.java` (esquive des aventuriers), `config/configs/MobsWorldConfig.java` (monstres),
`classes/definitions/**` (bonus de classe), `gui/tabs/ProfilTab.java` (affichage).

## 1. Constat : l'esquive aujourd'hui

| # | Problème | Où | Effet en jeu |
|---|----------|----|--------------|
| E1 | **Chaque esquive joue l'effet vanilla `Dodge_Left`/`Dodge_Right`** = animation `DashLeft`/`DashRight` (0,25 s) sur le joueur | `CombatStatsSystem.tryDodge` l. 196-199 | L'animation de dash **remplace l'animation d'attaque en cours** : on frappe, on se fait toucher au même moment, et c'est un pas de côté qui se joue. Cause la plus probable du « j'attaque et ça esquive à la place ». |
| E2 | **0,5 % d'esquive par point d'Agilité**, plafond 80 % (atteint à 160 points), puis plafond souple jusqu'à 92 % | `StatConfig.DODGE_CHANCE` | Un joueur niveau ~55 qui met tout en Agilité est au plafond. |
| E3 | **64 classes sur 408 atteignent 80 % avec leur seul bonus de classe** (Ombre Suprême +250, Demi-Ombre +765, Empereur Souterrain +1190…). Médiane des classes : +55 = 27,5 % offerts. | `classes/definitions` | L'esquive devient la norme, pas l'exception. |
| E4 | **Deux jets séparés** : la stat (jusqu'à 92 %) puis le passif Esquive Miraculeuse (5 à 30 %, ×maîtrise) | `tryDodge` + `PassiveEffects.esquive` | Ombre Suprême (passif rang 5) ≈ 94 % des coups annulés. |
| E5 | **Aucun test de la source** : chute, feu, poison, noyade, sorts de zone peuvent être « esquivés » | `tryDodge` | Irréaliste (on n'esquive pas une chute). La rune Prescience, elle, filtre déjà ces cas. |
| E6 | **Plancher de 2 %** même à 0 Agilité | `tryDodge` l. 188 | Esquives inexpliquées en tout début de partie. |
| E7 | Le malus de niveau ne joue que dans un sens (attaquant +5 niveaux) ; **la précision de l'attaquant n'existe pas** | `tryDodge` | Rien ne contre l'esquive, à part le niveau. |
| E8 | **Aventuriers PNJ : jusqu'à 50 % d'esquive**, jet indépendant | `AdventurerPassives.onDefend` | Quand c'est le joueur qui frappe un aventurier, un coup sur deux ne passe pas. Autre lecture possible de « j'attaque et ça esquive ». |
| E9 | La classe Spectre annonce « attaques impossibles à esquiver » : **non codé** | `Spectre.java` | Promesse non tenue. |
| E10 | Esquive possible pendant qu'on attaque, qu'on est immobile, étourdi, à court d'endurance, ou frappé dans le dos | `tryDodge` | Aucune logique physique. |

À côté de ça, le jeu vanilla a **déjà une vraie esquive active** : direction gauche ou droite + touche d'esquive →
0,25 s d'invulnérabilité (`Dodge_Invulnerability`), poussée latérale, 2 points d'endurance
(`Server/Item/Interactions/Dodge/`). Le mod ne s'en sert pas.

## 2. Constat : les autres stats (check-up)

Formule commune : `valeur = base + points × ratio`, points = joueur + classe + titres (×2 sous Arkies Domini),
puis passifs, puis Rune d'Aura, puis plafond souple (au-delà du plafond, 30 % d'efficacité, max plafond + 15 %).
Le joueur gagne **3 points par niveau** (niveau max 999 → ~3 000 points). Les monstres gagnent +7 PV et +0,5 dégât par niveau.

| Stat | Ratio | Plafond atteint à | Constat |
|------|-------|-------------------|---------|
| Vitalité → PV | 0,5 PV/pt | aucun | À comparer aux +0,5 dégât/niveau des monstres (simulation § 3). |
| Intelligence → Mana | 3,33/pt | aucun | Cohérence avec le coût des runes à vérifier. |
| Endurance → Endurance (stamina) | 0,5/pt | aucun | — |
| Force → dégâts | **+0,5 dégât fixe**/pt | 50 (100 pts) | Bonus fixe par coup : favorise énormément les armes rapides ; plafond atteint vers le niveau 35. Commentaire du code faux (« 0,032 »). |
| Endurance → défense | **−0,1 dégât fixe**/pt | 35 (350 pts) | 5× plus faible que la Force ; plancher 1 dégât. |
| Chance → critique | **0,05 %**/pt | 80 % (1 600 pts) | **10× moins rentable que l'esquive** (0,5 %/pt). Critique ×2 (commentaire faux : « 1,8 »). |
| Agilité → esquive | 0,5 %/pt | 80 % (160 pts) | Voir § 1 : très au-dessus de tout le reste. |
| Agilité → vitesse | ×0,0005/pt | ×3 (3 000 pts) | — |
| Agilité → saut | 0,004/pt | 20 blocs | — |
| Agilité → vitesse d'attaque | 0,005/pt | ×4 | **Stat morte : jamais lue nulle part.** Le joueur y investit pour rien. |
| Agilité → chute, détection | divers | — | À vérifier que ces valeurs sont bien lues. |
| Chance → butin, événements | 0,005/pt | 100 / 50 | — |

Autres points : bonus de classe très dispersés (de +0 à +2 720 sur une même stat) ; multiplicateurs empilés
(passifs × Aura × Arkies Domini × titres) sans plafond global ; affichage du profil à aligner sur les nouvelles formules.

## 3. Le plan, étape par étape

### Étape 0 : mesurer avant de toucher (outils)
1. **Simulateur** `scripts/equilibrage/stats_sim.py` : reproduit `StatConfig` et le calcul de `CombatStatsSystem`.
   Sortie : un tableau par niveau (1, 25, 50, 100, 200, 400, 999) × profil (classe médiane, assassin, tank, mage,
   tout en Agilité, tout en Force) : PV, dégâts par coup, critique, esquive, réduction, **nombre de coups pour tuer
   un monstre du même niveau et pour mourir**. Les bonus de classe sont lus directement dans les fichiers Java.
2. **Journal de combat** `/es combat log` (admin, bascule) : affiche dans le chat chaque jet (esquive tirée, %, source,
   annulé ou non, dégâts avant/après Force, critique, défense, passifs). Sert à confirmer E1 / E8 en jeu.
3. Garder le tableau « avant » comme référence pour comparer après chaque étape.

### Étape 1 : correctifs immédiats de l'esquive (sans changer l'équilibre)
- **Supprimer l'animation `Dodge_Left/Right`** sur l'esquive automatique ; la remplacer par un retour discret qui ne
  touche pas aux animations : texte flottant « Esquive », son léger, petite particule.
- **Filtrer la source** : seulement les coups d'une entité (corps à corps, projectile). Jamais chute, feu, poison,
  noyade, saignement, dégâts sur la durée.
- **Supprimer le plancher de 2 %**.
- **Un seul jet** : la stat et le passif Esquive s'additionnent avant le jet (plus de second jet).
- **Spectre** : ses coups ne peuvent pas être esquivés.

### Étape 2 : nouvelle esquive « réaliste » (à valider, § 4)
Principe : **l'esquive active (vanilla) devient la vraie esquive ; l'esquive automatique devient rare.**
- **Esquive active** : l'Agilité l'améliore (invulnérabilité 0,25 s → jusqu'à ~0,45 s, coût en endurance réduit,
  poussée plus longue). À rechercher : comment détecter côté serveur l'esquive vanilla et prolonger l'effet
  (effet maison appliqué en plus de `Dodge_Invulnerability`).
- **Esquive automatique** (« réflexe ») :
  - formule à rendements décroissants : `chance = plafond × pts / (pts + K)`, par ex. plafond 30 %, K = 300
    → 100 pts ≈ 7,5 %, 300 pts = 15 %, 1 000 pts ≈ 23 % ;
  - **impossible pendant sa propre attaque** (fenêtre ~0,6 s après un coup donné), étourdi, immobilisé,
    endurance vide, ou frappé dans le dos ;
  - **coûte de l'endurance** et a un **temps de recharge interne** (~2 s) : pas de séries d'esquives ;
  - **précision de l'attaquant** : son Agilité/Chance et l'écart de niveau (dans les deux sens) réduisent la chance.
  - Variante possible : « **coup effleuré** » au lieu d'annuler (−50 à −70 % des dégâts) pour la majorité des cas,
    l'annulation complète restant réservée à l'esquive active.
- Passifs Esquive (Miraculeuse → Temporelle) : réduire les valeurs (ex. 2 → 12 %) ou les changer en bonus d'esquive
  active (durée d'invulnérabilité, endurance rendue après une esquive réussie).
- **Aventuriers PNJ** : même formule que les joueurs, plafond plus bas (15-20 %).
- Rune Prescience et passifs de riposte : vérifier qu'ils restent cohérents.

### Étape 3 : check-up des autres stats
- **Vitesse d'attaque** : la brancher pour de vrai, ou la retirer et redonner la valeur à autre chose.
- **Force / Défense** : passer d'un bonus fixe par coup à un pourcentage (ou mixte), pour que les armes lentes et
  rapides profitent pareil ; remonter la défense (aujourd'hui 5× plus faible que la Force).
- **Critique** : rapprocher sa rentabilité de celle des autres stats (ratio et/ou plafond).
- **Plafonds** : un même modèle de rendements décroissants pour toutes les stats en %.
- **Empilement** : ordre et plafond global des multiplicateurs (passifs, Aura, Arkies Domini, titres, armes divines).
- **Monstres** : ajuster PV/dégâts par niveau d'après la simulation (cible : temps pour tuer et pour mourir stable
  du niveau 1 au niveau 999 face à un monstre du même niveau).
- Corriger les commentaires faux de `StatConfig` / `CombatStatsSystem`.

### Étape 4 : bonus des classes
- Normaliser par rareté : chaque rareté a un budget de points, réparti selon le rôle de la classe ; les extrêmes
  (+1 190 en Agilité, +2 720 en Chance) ramenés dans ce budget.
- Script de génération ou de vérification (lit les 408 classes, signale les dépassements).

### Étape 5 : affichage et documentation
- Onglet Profil et `/es admin` Compétences : nouvelles formules, esquive active et automatique séparées.
- Mettre à jour `docs/a-faire.md` et le Brain IA (via Camille).

### Étape 6 : tests en jeu
- [ ] Attaquer un monstre en se faisant frapper : plus jamais d'animation de dash pendant l'attaque.
- [ ] Chute, feu, poison : jamais d'« Esquive ».
- [ ] Personnage sans Agilité : aucune esquive automatique.
- [ ] Assassin haut niveau : esquive automatique rare (≤ plafond), pas de série.
- [ ] Esquive active (gauche/droite + touche) : invulnérabilité plus longue avec beaucoup d'Agilité.
- [ ] Combat contre un aventurier : ses esquives restent rares.
- [ ] Classe Spectre : ses coups ne sont jamais esquivés.
- [ ] Journal de combat : les chiffres correspondent à la simulation.

## 4. Décisions (2026-11-25)

1. L'esquive automatique **annule le coup**, avec un **dash à l'opposé de l'ennemi** (droite, gauche, avant ou arrière
   selon sa position) et l'animation d'esquive gauche/droite du jeu pendant le dash.
2. **30 % au maximum**, atteint vers **3 000 à 4 000 points d'Agilité** (retenu : 3 500).
3. L'Agilité améliore l'**esquive automatique** (pas de changement de l'esquive active vanilla).
4. Passifs Esquive : **valeurs baissées**.
5. Force/Défense en pourcentage : **oui si c'est impactant** (simulation : ça l'est, § 6 ; valeurs à valider).
6. Vitesse d'attaque : **retirée**.
7. Bonus des classes : **après la simulation**.

## 5. Fait le 2026-11-25 (compilé, jar non reconstruit, non vu en jeu)

- `Leveling/systems/AutoDodge.java` (nouveau) remplace `CombatStatsSystem.tryDodge` :
  - chance = `StatConfig.DODGE_CHANCE` = 30 × √(Agilité / 3 500) + passifs Esquive, **plafond dur 30 %** ;
    100 pts ≈ 5 %, 500 ≈ 11 %, 1 000 ≈ 16 %, 2 000 ≈ 23 %, 3 500 = 30 % ;
  - écart de niveau dans les deux sens (attaquant +6 niveaux et plus : −5 %/niveau, mini ×0,25 ; victime plus forte :
    +2 %/niveau, maxi ×1,3), **moitié de chance contre un coup dans le dos** ;
  - seulement les coups `Physical`, `Bludgeoning`, `Slashing`, `Projectile` d'une entité (jamais chute, feu, poison,
    sorts élémentaires) ;
  - **jamais pendant sa propre attaque ou sa garde** (chaîne Primary/Secondary active), ni étourdi (`Stun`, `Root`,
    `Hit_Interrupt_Stunned`, `Bomb_Explode_Stun`), ni avec moins de 2 d'endurance, ni moins de 2 s après la précédente ;
  - coups de la classe Spectre (`spectre_assassin`) inesquivables ;
  - réussite : coup annulé, −2 d'endurance, **dash** (vitesse 13, réglages de l'esquive vanilla) : ennemi à gauche →
    à droite, à droite → à gauche, devant → arrière en diagonale, derrière → avant en diagonale ; animation
    `Dodge_Left`/`Dodge_Right` du côté latéral ; `Dodge_Invulnerability` (0,25 s) ;
  - la maîtrise des passifs Esquive progresse à chaque esquive réussie.
- Plus de plancher de 2 %, plus de second jet : `PassiveEffects.esquive` donne un bonus à la chance (2, 3, 5, 7, 9,
  12 % selon le rang ; `conception.py` → `gen_passifs.py` relancé, pages et traductions à jour).
- Aventuriers : esquive plafonnée à 20 % (au lieu de 50 %), seulement contre les coups physiques et projectiles.
- `ATTACK_SPEED` retirée de `StatConfig` ; commentaires faux corrigés ; textes d'aide Force / Endurance / Agilité
  corrigés dans `SystemPage.ui`.
- Simulateur `scripts/equilibrage/stats_sim.py` (`--pourcent`, `--classe <id>`, `--classes`).

### À vérifier en jeu
- [ ] Frapper en se faisant frapper : jamais de dash pendant l'attaque.
- [ ] Le dash part bien à l'opposé de l'ennemi (gauche/droite pas inversées) et l'animation correspond au côté.
- [ ] Chute, feu, poison : jamais d'esquive.
- [ ] Pas deux esquives en moins de 2 s ; l'endurance baisse de 2.
- [ ] Profil : la valeur « Esquive » suit la nouvelle courbe.
- [ ] Classe Spectre : ses coups ne sont jamais esquivés.

## 5 bis. Après le premier essai en jeu (2026-11-25, 2e version ; compilé, jar NON reconstruit)

Retour : niveau 999 contre 25 squelettes, « pas pris un seul coup », esquives peu claires, « téléporté de droite à
gauche sans rien faire ». Le jar testé contenait bien AutoDodge, sans erreur dans le journal. Personnage : 1 point
d'Agilité à soi, ~120 avec la classe et les titres → ~6 % d'esquive : l'esquive n'explique pas l'absence de dégâts
(cause probable : squelettes de bas niveau dont les coups tombent à 1 avec l'ancienne Défense fixe ; à confirmer avec
le journal). Avec 25 squelettes, le dash partait à chaque fin d'attente (2 s), sans geste du joueur.

- Dash plus court (vitesse 9 au lieu de 13), **attente 5 s** au lieu de 2, **plus d'invulnérabilité** après l'esquive.
- Retours lisibles : effets `Eldanior_Esquive_Gauche/Droite` (`Server/Entity/Effects/Eldanior/Esquive/`) =
  animation DashLeft/DashRight + traînée `Daggers_Dash_Straight` + `ModelVFX Dagger_Dash` + particules à la 1re personne
  `Dagger_Dash_Left_FP` (recette du dash des dagues vanilla) ; son `SFX_Player_Roll` ; secousse de caméra
  `Daggers_Dash_Side` pour le joueur qui esquive.
- **Journal de combat** `/es combat log` (admin) : chaque coup reçu ou donné, valeur brute → après attaque → finale,
  critique, et pour l'esquive : chance, réussie/ratée ou la raison (en attente, en attaque, cause, endurance…).
  Avant armure vanilla et PV virtuels des monstres.
- **Force en %** : +0,5 % de dégâts par point, sans plafond (0,15 % essayé en jeu le 2026-11-25 : trop faible ;
  niveau 999, 1 158 points, arme de ~10,5 → 28,7 par coup ; avec 0,5 % → 71). **Défense en %** : 50 × pts / (pts + 1 000), plafond
  60 % passifs compris (Corps Robuste 3 → 15 %, Cœur de Chêne 1 → 6 %). Aventuriers : leur défense de passifs en %.
  Profil, `/es admin` et aide du menu Système mis à jour. Simulateur aligné (`--ancien` pour comparer).

### À vérifier en jeu (2e essai)
- [ ] `/es combat log` puis se battre : lire pourquoi on ne prend pas de dégâts.
- [ ] Esquive : traînée et animation visibles en 3e personne, secousse + son en 1re personne ; pas plus d'une toutes les 5 s.
- [ ] Profil : « Dégâts +X % » et « -X % de dégâts reçus ».

## 6. Résultats de la simulation (2026-11-25)

Hypothèses : épée de fer (coup moyen 14 × (1 + niveau/200)), Guerrier Trork (61 PV, 23 dégâts) au niveau du joueur,
**armure non comptée**. `python3 scripts/equilibrage/stats_sim.py --pourcent`.

- **Force fixe** : très forte au début (« Tout Force » tue un monstre niv. 25 en 5 coups contre 15), puis inutile :
  le plafond (+50) est atteint vers le niveau 35 ; au niveau 999, 50 coups pour tuer. En pourcentage
  (+0,15 %/point, essai) : 14 à 28 coups du niveau 25 au 999, la Force garde sa valeur toute la partie.
- **Défense fixe** : plafond −35 atteint vers le niveau 400, insignifiant face à un coup de 480 au niveau 999.
  En pourcentage (50 % × pts/(pts + 1 000), essai) : le Tank survit 2,2 coups au lieu de 1,8.
- **Vie** : +0,5 PV par point contre +0,5 dégât par niveau pour les monstres → sans classe ni armure, un joueur
  équilibré meurt en moins d'un coup d'un monstre de son niveau à partir du niveau ~600. À revoir avec l'armure
  (la simulation ne la compte pas encore).
- **Classes** : médianes par rareté de 6-10 (Commun) à 250-600 (Divin), extrêmes jusqu'à +2 720 ; une Ombre Suprême
  niv. 1 tue un monstre en 1 coup (+120 Force = +50 de dégâts plafonnés dès le départ).

Prochaine étape : valider les ratios en pourcentage de Force/Défense et la courbe de vie, ajouter l'armure au
simulateur, puis normaliser les bonus des classes par rareté.
