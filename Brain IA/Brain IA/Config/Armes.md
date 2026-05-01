# Maitrise des Armes

#armes #maitrise #combat #weapon

## 13 Types d'armes

| Type | Label | Classes principales |
|------|-------|-------------------|
| SWORD | Epee | Guerrier, Assassin |
| AXE | Hache | Guerrier |
| BOW | Arc | Archer |
| STAFF | Baton | Mage |
| DAGGER | Dague | Assassin |
| SHIELD | Bouclier | Guerrier |
| SPEAR | Lance | Guerrier |
| SPELLBOOK | Grimoire | Mage |
| MACE | Masse | Paladin |
| GUN | Pistolet | - |
| CLUB | Gourdin | - |
| RIFLE | Fusil | - |
| ANY | Toutes | Marchand |

## Systeme de maitrise
- Chaque classe definit ses `allowedMasteries`
- Le `MasterySystem` (ECS) applique des bonus de degats si l'arme correspond
- Skills de maitrise : `SWORD_MASTERY`, `GREAT_AXE_MASTERY`, `DAGGER_MASTERY`, etc.

## Effets visuels par arme
| Arme | Effet Hytale |
|------|-------------|
| Epee | Sword_Signature_SpinStab |
| Hache | Battleaxe_Whirlwind |
| Dague | Dagger_Dash / Pounce / Signature |
| Arc | Crossbow_Combo_1 / Combo_2 |
| Lance | Mace_Signature |

## Fichier cle
- `config/configs/WeaponMastery.java` - Enum
- `config/configs/system/MasterySystem.java` - Systeme ECS

## Liens
- [[Systems/Skills]] - Skills de maitrise
- [[Systems/Classes]] - Armes autorisees par classe
