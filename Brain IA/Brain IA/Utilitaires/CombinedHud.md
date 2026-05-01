# CombinedHud

#hud #interface #quete #groupe

## Probleme resolu
Hytale ne supporte qu'UN seul `CustomUIHud` a la fois. Le `CombinedHud` combine :
- **Gauche** : HUD de quete active (objectifs)
- **Droite** : HUD du groupe (membres + HP)

## Fonctionnement
- `CombinedHud` extends le HUD natif Hytale
- `renderPublic()` → affiche les infos de quete
- `renderParty()` → affiche les membres du groupe
- Se reconstruit dynamiquement quand le joueur rejoint/quitte un groupe

## Activation
- Se met en place quand le joueur active une quete dans l'onglet Quetes
- Ou quand le joueur rejoint un groupe

## Fichier cle
- `hud/CombinedHud.java`

## Liens
- [[Systems/Quetes]] - Objectifs affiches
- [[Systems/Groupe]] - Membres affiches