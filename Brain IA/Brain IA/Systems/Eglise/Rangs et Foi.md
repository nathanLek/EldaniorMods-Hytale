# Rangs et Foi de l'Eglise

#eglise #rangs #foi #hierarchie

## Les 7 rangs ecclesiastiques

| Rang | Couleur | Foi base | Max Acolytes | Max/Eglise |
|------|---------|----------|--------------|------------|
| **Pape** | §6 Or | 100 | 10 | 1 |
| **Saint** | §e Jaune | 80 | 0 | Classe speciale |
| **Cardinal** | §5 Violet | 50 | 3 | 2 |
| **Archeveque** | §9 Bleu | 30 | 2 | 3 |
| **Pretre** | §a Vert | 15 | 1 | 4 |
| **Religieux** | §f Blanc | 5 | 0 | illimite |
| **Laique** | §7 Gris | 0 | 0 | - |

## Chaine de promotion
```
Laique → Religieux → Pretre → Archeveque → Cardinal → Pape
                                                    ↗
                                              Saint (voie speciale)
```

## Systeme de Foi
- **Foi de base** : determinee par le rang (0-100)
- La foi determine la puissance des benedictions
- Accumulee via les activites d'eglise
- Le Pape a la foi la plus elevee (100) pour les benedictions les plus fortes

## Systeme d'Acolytes
| Rang | Peut sponsoriser |
|------|-----------------|
| Pretre | 1 acolyte |
| Archeveque | 2 acolytes |
| Cardinal | 3 acolytes |
| Pape | 10 acolytes |

## Obtention du rang de Saint
- Voie independante : pas de promotion classique
- Obtenu via une **classe speciale** (evolution de classe)
- Le Saint a 80 de foi mais ne peut pas avoir d'acolytes

## Fichiers cles
- `titles/church/ChurchRank.java` — enum des rangs
- `titles/church/ChurchManager.java` — promotions et acolytes

## Liens
- [[Benedictions]] - Items de benediction
- [[../Eglise]] - Vue d'ensemble