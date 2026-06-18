# Éclat de Glace

#sort #actif #glace #cryomancien #mage #projectile

## Identité
- **ID technique** : `ECLAT_DE_GLACE`
- **SkillManager key** : `skill_page_eclat_de_glace`
- **Type** : Sort actif — Projectile
- **Classe** : [[Cryomancien]] (Mage, Tier 2, Common)
- **Coût** : 80 mana (à la charge)

## Description en jeu
> Projetez un éclat de glace tranchant qui transperce votre cible, infligeant des dégâts de givre et la ralentissant brièvement. Maintenez le clic droit pour charger le sort, puis relâchez votre puissance avec la touche Ability.

## Mécanique
| Étape | Touche | Action |
|-------|--------|--------|
| Charge | Secondary (clic droit) | Dépense 80 mana, `SignatureEnergy = 1`, animation `CastHurlCharging`, particules `IceBall` + `Weapon_Frost_Mist`, son `SFX_Ice_Break` |
| Lancer | Ability1 | Consomme `SignatureEnergy`, tire le projectile `Spell_EclatDeGlace` |

- Si le joueur manque de mana à la charge : animation `Interact` + son `SFX_Bow_No_Ammo`
- Si le joueur tente de lancer sans avoir chargé (`SignatureEnergy = 0`) : même échec sonore

## Projectile
| Paramètre | Valeur |
|-----------|--------|
| Vitesse initiale | 55 |
| Vitesse maximale | 70 |
| Gravité | 0 (trajectoire rectiligne) |
| Rebond | 0 |
| Durée de vie | 3s |
| Rayon de collision | 0.12 |
| Son impact/mort | `SFX_Ice_Break` |
| Particules impact | `Impact_Ice` |

## Effets à l'impact (ProjectileHit)
1. **DamageEntity** : 18 dégâts de type `Ice` + particules `Impact_Ice` et `Effect_Snow`
2. **ApplyEffect** : `Elda_Givre` — ralentissement -50% pendant 6 secondes
3. **RemoveEntity** : destruction du projectile

En cas de `ProjectileMiss` : destruction du projectile uniquement.

## Apparence du Spellbook
- Modèle : `Items/Weapons/Spellbook/Book.blockymodel`
- Texture : `Items/Weapons/Spellbook/Frost_Texture.png`
- Icône : `Icons/ItemsGenerated/Weapon_Spellbook_Frost.png`
- ItemLevel : 30
- Rareté item : Common
- Particules actives (quand chargé) : `Effect_Snow` + `Weapon_Frost_Mist`

## Fichiers JSON
| Fichier | Rôle |
|---------|------|
| `Server/Item/Items/ActiveSkill/Spells/Glace/Projectile/Spell_EclatDeGlace.json` | Item Spellbook |
| `Server/Item/Interactions/Spells/Spell_EclatDeGlace_Charge.json` | Logique de charge |
| `Server/Item/Interactions/Spells/Spell_EclatDeGlace_Launch.json` | Logique de lancer |
| `Server/Item/RootInteractions/Spells/Root_Spell_EclatDeGlace_Charge.json` | Racine charge |
| `Server/Item/RootInteractions/Spells/Root_Spell_EclatDeGlace_Launch.json` | Racine lancer |
| `Server/ProjectileConfigs/Spells/Projectile_Config_EclatDeGlace.json` | Comportement à l'impact |
| `Server/Projectiles/Spells/Spell_EclatDeGlace.json` | Physique du projectile |
| `Server/Models/Projectiles/Spells/Spell_EclatDeGlace.json` | Modèle 3D |

## Enregistrement Java
```java
// SkillManager.java
register("skill_page_eclat_de_glace", new SkillModel(
    "ECLAT_DE_GLACE", "Spell_EclatDeGlace", "Éclat de Glace", "mage, cryomancien",
    80, 0.0f, 1.0f, 18.0f, 30.0f, 6.0f,
    List.of(), List.of()
));
```

## Liens
- [[Cryomancien]] — Classe propriétaire du sort
- [[Sorts Actifs]] — Liste complète des sorts actifs
- [[../../Config/LootTables]] — Tables de loot (le Spellbook peut tomber en coffre)
