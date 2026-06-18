# Cryomancien

#classes #mage #glace #cryomancien #tier2

## Identité
- **Famille** : Mage
- **Tier** : 2 (évolution de Mage, niveau 400)
- **Rareté** : Common
- **Description** : Le Cryomancien commande le froid absolu. Ses sorts gèrent ses ennemis dans une prison de glace éternelle.
- **Sous-thème** : Glace, ralentissement, contrôle de foule

## Stats bonus hérités (Mage base)
| Stat | Bonus Mage base |
|------|----------------|
| STR | +2 |
| VIT | +4 |
| INT | +20 |
| END | +4 |
| AGL | +4 |
| LCK | +2 |

Bonus Cryomancien (tier 2) : `2 STR, 8 VIT, 24 INT, 6 END, 6 AGL, 4 LCK`

## Compétences passives innées
| Skill | Description |
|-------|-------------|
| `MANA_BARRIER` | Bouclier de mana absorbe les dégâts |
| `STONE_SKIN` | Réduction de dégâts physiques |
| `AWAKENED_MIND` | Bonus régénération de mana |

## Sorts actifs (activeSkillIds)
| Sort | ID | Mana | Mécanisme | Dégâts | Effet |
|------|----|------|-----------|--------|-------|
| **Éclat de Glace** | `ECLAT_DE_GLACE` | 80 | Projectile chargé | 18 (Ice) | `Elda_Givre` -50% vitesse 6s |

## Maîtrises d'armes
- Staff (Bâton)
- Spellbook (Grimoire)

## Titres débloquables
- `maitre_du_givre`
- `glacial_mage`
- `seigneur_du_froid`

## Fichiers clés
- `classes/definitions/mage/Cryomancien.java` — définition Java
- `Server/Item/Items/ActiveSkill/Spells/Glace/Projectile/Spell_EclatDeGlace.json` — Spellbook Éclat de Glace

## Liens
- [[Arbre des classes]] — Position dans l'arbre (Mage > 7 Common)
- [[Systems/Skills/Sorts Actifs]] — Liste complète des sorts actifs
- [[Systems/Classes]] — Système de classes et gacha
