# Processus : Creer une Classe

#processus #classe #creation #guide

## Structure d'une classe
```java
public class MaClasse extends ClassModel {
    public MaClasse() {
        super(
            "ma_classe",           // ID unique
            "Ma Classe",           // Nom affiche
            "Description RP.",     // Description
            Rarity.RARE,           // Rarete
            ClassType.WARRIOR,     // Type (WARRIOR/MAGE/ASSASSIN/ARCHER/MERCHANT)
            List.of(               // Skills passifs innes
                PassiveSkill.SKILL_1,
                PassiveSkill.SKILL_2,
                PassiveSkill.SKILL_3
            ),
            List.of(               // Armes autorisees
                WeaponMastery.SWORD,
                WeaponMastery.AXE,
                WeaponMastery.SHIELD
            ),
            List.of(               // Evolutions possibles (IDs)
                "evolution_1",
                "evolution_2",
                "evolution_3"
            ),
            400,                   // promotionLevel (niveau pour evoluer)
            false,                 // adminAccess
            50, 30, 10, 40, 20, 30 // STR, VIT, INT, END, AGL, LCK
        );
    }
}
```

## Etapes

### 1. Creer le fichier Java
Dossier : `classes/definitions/<famille>/` (ou `<famille>/400/` pour Tier 2)

### 2. Enregistrer dans ClassManager
Fichier : `classes/ClassManager.java`
```java
register(new MaClasse());
```

### 3. Ajouter comme evolution de la classe parente
Dans la classe parente, ajouter l'ID dans `nextClassId` :
```java
List.of("ma_classe", "autre_evolution_1", "autre_evolution_2")
```

## Regles de rarete pour les evolutions
| Parent | Evolution |
|--------|-----------|
| COMMON → | RARE |
| RARE → | EPIC |
| EPIC → | UNIQUE |
| UNIQUE → | UNIQUE (reste) |
| LEGENDARY → | LEGENDARY (reste) |
| DIVINE → | DIVINE (1 seule evolution) |

## Niveaux de promotion
| Palier | Niveau |
|--------|--------|
| Base (depuis Novice) | 20 |
| Tier 1 (depuis base) | 180 |
| Tier 2 (depuis Tier 1) | 400 |

## Gacha
- Tier 1 : 3 choix proposes au joueur
- Tier 2 : 1 seul choix propose
- Marchand : toujours 1 seul choix
- Poids par rarete (voir [[Config/Raretes]])

## Fichiers a modifier
1. `classes/definitions/<famille>/MaClasse.java` (creer)
2. `classes/ClassManager.java` (register)
3. Classe parente (ajouter dans nextClassId)

## Liens
- [[Systems/Classes]] - Vue d'ensemble
- [[Classes/Arbre des classes]] - Arbre complet
- [[Config/Raretes]] - Poids du gacha