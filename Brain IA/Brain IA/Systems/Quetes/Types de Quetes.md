# Types de Quetes

#quetes #types #principal #secondaire #journaliere

## 3 Categories

### Principales
- Histoire du monde (ex: Naissance du Roi)
- Quetes chainees : `nextQuestId` deverrouille la suivante
- Non repetables
- Donnees par des PNJ specifiques

### Secondaires
- Quetes de PNJ independantes
- Non repetables
- Variees : exploration, combat, dialogue

### Journalieres
- **Repetables** avec cooldown de 24h
- **10 quetes aleatoires** (5 normales + 5 PK, selectionnees separement) chaque jour parmi 102+
- Reset a minuit (getDayOfYear change)

## 6 Types de quetes journalieres

| Type | Objectif | Nombre defini |
|------|----------|--------------|
| CHASSE | Tuer X mobs d'un type specifique | 28 |
| MASSACRE | Tuer X mobs total | 11 |
| COLLECTION | Collecter X or | 11 |
| EXPLORATION | Decouvrir X coffres | 11 |
| DUEL | Gagner X duels | 11 |
| EXECUTION | Tuer X joueurs PK | 30+ |

## Difficulte
| Niveau | Multiplicateur recompense |
|--------|--------------------------|
| EASY | ×0.8 |
| MEDIUM | ×1.0 |
| HARD | ×1.3 |
| EXTREME | ×1.8 |

## Structure des dossiers de definitions
```
quest/definitions/
├── principal/          (quetes principales)
├── secondaire/         (quetes secondaires + info)
└── journaliere/        (quetes journalieres)
    ├── chasse/
    ├── massacre/
    ├── collection/
    ├── exploration/
    ├── duel/
    └── pk/             (sous-series : Assassin 1-10, Pillard 1-5, Terreur 1-8, DuelSombre)
```

## Fichiers cles
- `quest/QuestType.java` — enum des 6 types
- `quest/QuestCategory.java` — enum des 3 categories
- `quest/QuestDifficulty.java` — multiplicateurs
- `quest/definitions/` — definitions des quetes

## Liens
- [[Progression et Recompenses]] - Flow et rewards
- [[Quetes Journalieres]] - Systeme daily
- [[../Quetes]] - Vue d'ensemble