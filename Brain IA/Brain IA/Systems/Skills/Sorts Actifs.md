# Sorts Actifs (Active Skills)

#skills #sorts #actifs #mage #combat

## Vue d'ensemble
Les sorts actifs sont des compétences déclenchées par le joueur, distinctes des [[Categories de Skills|compétences passives]]. Ils sont matérialisés par des **Spellbooks** (grimoires) équipés comme arme principale. Chaque sort a un coût en mana et suit un cycle **Charge → Lancer**.

## Mécanique charge / lancer
1. **Charge** (Secondary — clic droit) : dépense le mana, charge l'énergie de signature (`SignatureEnergy = 1`), joue une animation et des particules de chargement.
2. **Lancer** (Ability1) : consomme la `SignatureEnergy`, tire le projectile ou déclenche l'effet.
- Si la charge n'est pas active (`SignatureEnergy = 0`), le lancer échoue (son `SFX_Bow_No_Ammo`).

## Enregistrement Java
Chaque sort est enregistré dans `skills/SkillManager.java` via :
```java
register("skill_page_<id>", new SkillModel(
    "<ID_ENUM>", "<ItemId>", "<Nom>", "<tags>",
    <mana>, <cooldown>, <castTime>, <damage>, <range>, <duration>,
    List.of(), List.of()
));
```

## Fichiers JSON impliqués par sort
| Fichier | Rôle |
|---------|------|
| `Server/Item/Items/ActiveSkill/Spells/<Cat>/<NomSort>.json` | Item Spellbook (stats, interactions, icône) |
| `Server/Item/Interactions/Spells/<NomSort>_Charge.json` | Logique de charge |
| `Server/Item/Interactions/Spells/<NomSort>_Launch.json` | Logique de lancer |
| `Server/Item/RootInteractions/Spells/Root_<NomSort>_Charge.json` | Racine interaction charge |
| `Server/Item/RootInteractions/Spells/Root_<NomSort>_Launch.json` | Racine interaction lancer |
| `Server/ProjectileConfigs/Spells/Projectile_Config_<NomSort>.json` | Comportement projectile (dégâts, effets à l'impact) |
| `Server/Projectiles/Spells/<NomSort>.json` | Apparence et physique du projectile |
| `Server/Models/Projectiles/Spells/<NomSort>.json` | Modèle 3D du projectile |

## Liste des sorts actifs implémentés

### Glace (Cryomancien)
| Sort | ID Technique | Mana | Dégâts | Effet à l'impact | Classe |
|------|-------------|------|--------|-----------------|--------|
| [[Eclat de Glace\|Éclat de Glace]] | `ECLAT_DE_GLACE` | 80 | 18 (Ice) | `Elda_Givre` -50% vitesse 6s | [[../../Classes/Cryomancien\|Cryomancien]] (Tier 2, Common) |
| Armure de Glace | `ARMURE_DE_GLACE` | — | — | — | Cryomancien |
| Champ de Glace | `CHAMP_DE_GLACE` | — | — | — | Cryomancien |
| Coeur de Glace | `COEUR_DE_GLACE` | — | — | — | Cryomancien |

> Les sorts Armure, Champ et Coeur de Glace sont présents en JSON mais pas encore enregistrés dans SkillManager (à vérifier).

## Liens
- [[../Skills]] — Vue d'ensemble des compétences
- [[Cryomancien]] — Fiche classe avec la liste des sorts actifs
- [[../../Processus/Creer un skill]] — Processus compétences passives (référence partielle)
- [[../../Config/LootTables]] — Tables de loot pour les Spellbooks
