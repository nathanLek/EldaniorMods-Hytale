# Effets Visuels Manquants pour Skills

#feature #moyenne #skills #effets

## Etat actuel
- **214 mappings** skill→effet dans `SkillEffectConfig.java`
- **301 skills** au total dans `PassiveSkill.java`
- **~87 skills sans effet visuel** (29% du total)

## Skills sans mapping (par categorie)

### Vie (0 effets sur ~15 skills)
- Heart of Oak, Perseverance, Constitution, etc.
- **Effet suggere** : `Heal` ou particules vertes

### Resistance (partiel, ~8 skills manquants)
- Tenacity, Hardening, Fortification basse rarete
- **Effet suggere** : `Stoneskin` light ou `Shield_Block`

### Regeneration (partiel, ~6 skills manquants)
- Souffle lent, recuperation passive
- **Effet suggere** : `Regen` ou particules dorées

### Endurance (partiel, ~5 skills manquants)
- Poumons, stamina passifs
- **Effet suggere** : `Speed_Boost` ou aucun (passif invisible)

### Detection (partiel, ~4 skills manquants)
- Vision nocturne, pistage passif
- **Effet suggere** : `Eye_Glow` ou aucun (passif invisible)

## Strategie de completion

### Option A : Effet par categorie (simple)
| Categorie | Effet par defaut |
|-----------|-----------------|
| Vie | `Heal` sur joueur |
| Resistance | `Stoneskin` sur joueur |
| Regeneration | `Heal` sur joueur |
| Endurance | Aucun (passif invisible) |
| Detection | Aucun (passif invisible) |

### Option B : Effet par action (precis)
- Skills qui proc en combat → effet visible
- Skills passifs permanents → pas d'effet (eviter le spam visuel)

## Recommandation
**Option B** — Les skills passifs permanents (bonus de stats) n'ont pas besoin d'effet visuel. Seuls les skills qui **se declenchent** (proc) meritent un feedback visuel.

## Priorite
**MOYENNE** — Esthetique, pas bloquant

## Liens
- [[Optimisations/Validation Effets Visuels]] - Validation des assets existants
- [[Systems/Skills]] - Liste complete des 301 skills
- [[Utilitaires/Effets Visuels]] - Catalogue d'effets Hytale