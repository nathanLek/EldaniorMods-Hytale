# Systeme d'Effets Visuels

#effets #animation #hytale #visuel

## Entity Effects Hytale disponibles

### Combat / Armes
| ID | Animation | Duree |
|----|-----------|-------|
| Dagger_Dash | Dash rapide | 0.25s |
| Dagger_Pounce | Bond en avant | 0.25s |
| Dagger_Signature | Attaque signature dague | 0.25s |
| Battleaxe_Whirlwind | Tourbillon | 0.5s |
| Battleaxe_Downstrike_Jump | Frappe au sol | 0.5s |
| Sword_Signature_SpinStab | Enchainement epee | 0.5s |
| Mace_Signature | Frappe lourde masse | 0.5s |
| Flame_Staff_Burn | Brulure baton de feu | Variable |
| FlamethrowerSource | Lance-flammes | Variable |

### Mouvement / Esquive
| ID | Animation | Duree |
|----|-----------|-------|
| Dodge_Left | Esquive gauche (DashLeft) | 0.25s |
| Dodge_Right | Esquive droite (DashRight) | 0.25s |
| Dodge_Invulnerability | Invulnerable | 0.25s |
| Intangible_Dark | Traverser (sombre) | Variable |
| Intangible_Smol | Forme reduite | Variable |

### Status / Debuffs
| ID | Effet | Duree |
|----|-------|-------|
| Slow | Ralentissement | Variable |
| Stun | Etourdissement | Variable |
| Root | Immobilisation | Variable |
| Freeze | Gel | Variable |
| Burn | Brulure (DoT) | Variable |
| Poison_T1/T2/T3 | Poison (3 niveaux) | Variable |
| Stamina_Broken | Fatigue | Variable |

### Soins / Buffs
| ID | Effet |
|----|-------|
| Food_Health_Regen_Tiny/Small/Medium/Large | Regen HP |
| Food_Health_Boost_Tiny/Small/Medium/Large | Boost HP max |
| Food_Stamina_Regen_Tiny/Small/Medium/Large | Regen Stamina |
| Mana_Regen / Mana_Regen_High / Mana_Regen_Low | Regen Mana |
| Mana_High / Mana_Low | Boost Mana |
| Mana_Drain | Drain de mana |
| Immune | Immunite temporaire |
| Stoneskin | Peau de pierre (defense) |
| Antidote | Soigne poisons |
| Healing_Totem_Heal | Soin totem |

### Visuels
| ID | Effet |
|----|-------|
| Red_Flash | Flash rouge (degats) |
| Death | Animation de mort |
| Portal_Teleport | Teleportation |
| Bomb_Explode_Stun | Explosion + stun |
| Drop_Uncommon/Rare/Epic/Legendary | Effet de drop rare |

### Transformation
| ID | Effet |
|----|-------|
| Potion_Morph_Dog/Frog/Mouse/Pigeon/Mosshorn | Transformation en animal |

## Integration avec les Skills
214 competences ont un effet visuel associe dans `SkillEffectConfig.java` :
- **ATTACKER_EFFECTS** : effet sur le joueur qui active le skill
- **VICTIM_EFFECTS** : effet sur la cible

## Application
```java
EffectsManager.applyEffect(entityRef, "Dodge_Left", store);
EffectsManager.applyCustomEffect(entityRef, "Burn", 3.0f, OverlapBehavior.OVERWRITE, store);
EffectsManager.applyInfiniteEffect(entityRef, "Stoneskin", store);
EffectsManager.removeEffect(entityRef, "Poison_T1", store);
```

## Fichiers cles
- `config/Effects/EffectsManager.java` - Application des effets
- `config/Effects/SkillEffectConfig.java` - Mapping skill → effet (214 mappings)
- Assets Hytale : `Server/Entity/Effects/` (100+ JSON)

## Liens
- [[Systems/Skills]] - Competences avec effets
- [[Config/StatConfig]] - Stats qui influencent les effets
