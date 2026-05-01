# Dignite et Aura Noble

#noblesse #dignite #aura #mobs

## Systeme de Dignite
- Chaque rang a une **dignite de base** (0 pour Roturier, 100 pour Roi)
- La dignite peut etre augmentee avec l'item **Essence de Dignite** (+1)
- L'Essence de Dignite est Divine (0.02% en coffre legendaire)

## Aura de Dignite
La dignite active une **aura** qui **ralentit les mobs** proches du joueur.

### 4 Niveaux d'aura
| Dignite | Niveau | Effet |
|---------|--------|-------|
| 0-14 | Aucun | Pas d'aura |
| 15-29 | Faible | Ralentissement leger |
| 30-49 | Moyen | Ralentissement modere |
| 50-74 | Fort | Ralentissement important |
| 75+ | Royal | Ralentissement maximal |

### Fonctionnement technique
1. `DignityAuraSystem` tick regulierement
2. Recupere la dignite du joueur via `PlayerPositionTracker.PLAYER_DIGNITY`
3. Calcule le rayon et l'intensite de l'aura
4. Applique un ralentissement aux mobs dans le rayon
5. Effet visuel sur les mobs ralentis

## Affichage
- La dignite est affichee dans le profil du joueur
- Format nameplate : `[Rang] Nom Von Famille` avec couleur du rang

## Fichiers cles
- `titles/nobility/systems/DignityAuraSystem.java` — systeme ECS
- `config/Player/PlayerPositionTracker.java` — tracking dignite
- `Config/Dignite et Aura.md` — configuration des effets JSON

## Liens
- [[Rangs et Hierarchie]] - Dignite de base par rang
- [[../Noblesse]] - Vue d'ensemble
- [[../../Config/Dignite et Aura]] - Configuration detaillee
