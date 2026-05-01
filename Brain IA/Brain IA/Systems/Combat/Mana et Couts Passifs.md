# Mana et Couts Passifs

#combat #mana #skills #passifs

## Systeme de Mana
- Mana max = Intelligence × 3.33
- Regeneration = 0.0001852 par seconde (base)
- Les skills passifs consomment du mana a chaque attaque

## Couts par rarete
| Rarete | Cout mana typique |
|--------|-------------------|
| Common | 0-5 mana |
| Uncommon | 5-10 mana |
| Rare | 10-15 mana |
| Epic | 15-25 mana |
| Legendary | 25-35 mana |
| Divine | 35-40 mana |

## Multiplicateurs de regeneration
| Skill | Multiplicateur |
|-------|---------------|
| Mana Font (Mage inne) | 1.5x |
| Mana Heart (Legendary) | 10.5x |

## Flow de consommation
1. Attaque detectee par CombatStatsSystem
2. Pour chaque skill passif actif : verifier `hasEnoughMana()`
3. Si oui → activer le skill + deduire le mana
4. Si non → skill ne se declenche pas (pas de message)
5. Mettre a jour le composant mana via `commandBuffer.putComponent()`

## Probleme connu
Les skills sont evalues **sequentiellement** — si 2 attaques rapides arrivent, le mana peut etre consomme avant la mise a jour du composant.

## Fichier cle
- `Leveling/systems/CombatStatsSystem.java` — lignes 190-211

## Liens
- [[Formules de Degats]] - Formule principale
- [[../Skills]] - Systeme de competences
- [[../Combat]] - Vue d'ensemble
