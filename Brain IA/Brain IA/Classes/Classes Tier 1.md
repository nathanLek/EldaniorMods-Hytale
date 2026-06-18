# Classes Tier 1 (Base)

#classes #tier1 #guerrier #mage #assassin #archer #marchand

## 5 Classes de base

### Warrior (Guerrier)
| Stat | Bonus |
|------|-------|
| STR | +10 |
| VIT | +10 |
| INT | +4 |
| END | +4 |
| AGL | +6 |
| LCK | +2 |

- **Skills innes** : INSTINCTIVE_STRIKE, IRON_RESOLVE
- **Maitrises d'armes** : Epee, Hache, Bouclier
- **Evolutions tier 2** : 77 classes (Centurion, Berserker Enrage, Champion de l'Arene...)
- **Description** : Combattant melee avec force brute

### Assassin
| Stat | Bonus |
|------|-------|
| STR | +6 |
| VIT | +4 |
| INT | +2 |
| END | +2 |
| AGL | +14 |
| LCK | +8 |

- **Skills innes** : WIND_STEP, KEEN_SENSES
- **Maitrises d'armes** : Dague, Epee
- **Evolutions tier 2** : 77 classes
- **Description** : Predateur de l'ombre, haute agilite

### Mage
| Stat | Bonus |
|------|-------|
| STR | +2 |
| VIT | +4 |
| INT | +20 |
| END | +4 |
| AGL | +4 |
| LCK | +2 |

- **Skills innes** : MANA_FONT, AWAKENED_MIND
- **Maitrises d'armes** : Baton, Grimoire
- **Evolutions tier 2** : 76 classes (Arche-Liche [Unique], Archimage Royal, Druide Primordial...)
- **Description** : Lanceur de sorts, maitrise arcane

### Archer
| Stat | Bonus |
|------|-------|
| STR | +4 |
| VIT | +4 |
| INT | +4 |
| END | +2 |
| AGL | +12 |
| LCK | +10 |

- **Skills innes** : EAGLE_EYE, LIGHT_REFLEXES
- **Maitrises d'armes** : Arc, Dague
- **Evolutions tier 2** : 43 classes
- **Description** : Combattant longue portee, precision

### Merchant (Marchand)
| Stat | Bonus |
|------|-------|
| STR | +4 |
| VIT | +2 |
| INT | +4 |
| END | +4 |
| AGL | +4 |
| LCK | +20 |

- **Skills innes** : ARTISANAT, FORTUNE_COINS
- **Maitrises d'armes** : Toutes (13 types)
- **Evolutions tier 2** : 15 classes (Maitre Artisan, Chasseur de Reliques, Baron de l'Or...)
- **Description** : Specialiste commerce et artisanat
- **Specificite** : Seule classe avec acces au systeme d'**Echanges**
- **Subtilite tier 2** : 1 seule evolution possible (pas de choix gacha)

## Systeme de Gacha (choix de classe)
- **Tier 1** (niveau 1) : 3 classes proposees, le joueur en choisit 1
- **Tier 2** (niveau 180) : 3 evolutions proposees, le joueur en choisit 1
- **Tier 2+** (niveau 400) : 1 seule evolution proposee (pas de choix)
- **Relance** : possible avec un item Parchemin_Relance

## Fichiers cles
- `classes/definitions/` — chaque classe tier 1
- `classes/ClassManager.java` — registre et evolutions
- `classes/ClassModel.java` — modele de classe ; supporte un champ optionnel `activeSkillIds` (`List<String>`) pour lier des sorts actifs via constructeur surcharge

## Liens
- [[Arbre des classes]] - Arbre complet des 406 classes
- [[Familles/Familles Royales]] - Skills de famille
- [[../Systems/Classes]] - Systeme de classes