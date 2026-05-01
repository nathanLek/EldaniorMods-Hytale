# Systeme de Dignite et Aura

#dignite #aura #noblesse #buff

## Dignite
Valeur numerique liee au rang de noblesse. Plus la dignite est haute, plus l'aura est puissante.

| Rang | Dignite de base |
|------|----------------|
| Roturier | 0 |
| Chevalier | 5 |
| Baron | 15 |
| Comte | 30 |
| Duc | 50 |
| Marquis | 75 |
| Roi | 100 |

## Augmentation
- Item **Essence de Dignite** (+1 dignite, rarete Divine)
- Disponible dans les coffres legendaires (0.02%)

## Aura de Dignite (DignityAuraSystem)
Quand la dignite >= 5, le noble emet une **aura** qui affecte les mobs proches :
- Ralentit les mobs dans un rayon proportionnel a la dignite
- 4 niveaux d'effets visuels :

| Dignite | Rayon | Effet |
|---------|-------|-------|
| 5-14 | 4 blocs | Dignity_Aura_Light (leger) |
| 15-29 | 6 blocs | Dignity_Aura_Light |
| 30-49 | 8 blocs | Dignity_Aura_Medium |
| 50-74 | 12 blocs | Dignity_Aura_Medium |
| 75-99 | 16 blocs | Dignity_Aura_Heavy |
| 100+ | 20 blocs | Dignity_Aura_Root (immobilise) |

## Effets JSON Hytale
- `Server/Entity/Effects/Dignity/Dignity_Aura_Light.json`
- `Server/Entity/Effects/Dignity/Dignity_Aura_Medium.json`
- `Server/Entity/Effects/Dignity/Dignity_Aura_Heavy.json`
- `Server/Entity/Effects/Dignity/Dignity_Aura_Root.json`

## Condition d'activation
- Le noble doit avoir une **arme en main** pour que l'aura soit active
- Le systeme verifie l'inventaire a chaque tick

## Fichiers cles
- `titles/nobility/systems/DignityAuraSystem.java` - Systeme ECS
- `config/Player/PlayerPositionTracker.java` - Tracking dignite

## Liens
- [[Systems/Noblesse]] - Rangs et dignite
- [[Utilitaires/Effets Visuels]] - Effets d'aura
- [[Items/Consommables]] - Essence de Dignite