# Processus : Creer une Competence Passive

#processus #skill #creation #guide

## 6 etapes pour creer un nouveau skill

### Etape 1 : Creer la classe Java
Dossier : `skills/skills/passives/<Rarete>/<Categorie>/`

```java
public class MonSkill implements IPassiveCombatSkill {
    private static final float CHANCE = 0.10f; // 10% de proc
    
    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, 
                         Store<EntityStore> store, 
                         Ref<EntityStore> attackerRef, 
                         Ref<EntityStore> victimRef) {
        if (Math.random() < CHANCE) {
            damage.setAmount(damage.getAmount() * 1.15f); // +15% degats
        }
    }
    
    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData,
                         Store<EntityStore> store,
                         Ref<EntityStore> attackerRef,
                         Ref<EntityStore> victimRef) {
        // Logique de defense (optionnel)
    }
}
```

### Etape 2 : Ajouter dans l'enum PassiveSkill
Fichier : `skills/skillsInteraction/PassiveSkill.java`
```java
MON_SKILL("MON_SKILL", "Mon Skill", "Description du skill.", new MonSkill()),
// ou avec cout mana :
MON_SKILL("MON_SKILL", "Mon Skill", "Description.", 10, new MonSkill()),
```

### Etape 3 : Ajouter dans le SkillManager
Fichier : `skills/SkillManager.java`
```java
register(new SkillModel("mon_skill", "skill_page_mon_skill", 
    "Mon Skill", "all", 0, 0, 0, 0, 0, 0, 
    List.of("skill_superieur"), List.of("skill_inferieur")));
```

### Etape 4 : Creer le JSON de l'item parchemin
Fichier : `Server/Item/Items/PassiveSkill/skill_page_mon_skill.json`

### Etape 5 : Ajouter dans le fichier de langue
Fichier : `Server/Languages/en-US/server.lang`
```
items.skill_page_mon_skill.name=Mon Skill
items.skill_page_mon_skill.description=Description detaillee
```

### Etape 6 : Ajouter dans les tables de loot
Fichier : `config/configs/LootTableConfig.java`
```java
new LootEntry("skill_page_mon_skill", 1, 1, 1, 5) // 5% de drop
```

### Etape 7 (optionnel) : Ajouter un effet visuel
Fichier : `config/Effects/SkillEffectConfig.java`
```java
VICTIM_EFFECTS.put(PassiveSkill.MON_SKILL, "Red_Flash");
```

## Fichiers a modifier
1. `skills/skills/passives/<Rarete>/<Cat>/MonSkill.java` (creer)
2. `skills/skillsInteraction/PassiveSkill.java` (ajouter enum)
3. `skills/SkillManager.java` (register)
4. `Server/Item/Items/PassiveSkill/skill_page_mon_skill.json` (creer)
5. `Server/Languages/en-US/server.lang` (ajouter)
6. `config/configs/LootTableConfig.java` (ajouter au loot)
7. `config/Effects/SkillEffectConfig.java` (optionnel, effet visuel)

## Liens
- [[Systems/Skills]] - Vue d'ensemble des skills
- [[Config/LootTables]] - Probabilites de drop
- [[Utilitaires/Effets Visuels]] - Effets associes
