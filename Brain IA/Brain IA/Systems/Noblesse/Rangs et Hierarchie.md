# Rangs et Hierarchie Feodale

#noblesse #rangs #hierarchie #feodal

## Les 7 rangs

| Rang | Couleur | Dignite | Max/Royaume | Chevaliers | Gere |
|------|---------|---------|-------------|------------|------|
| **Roi** | §c Rouge | 100 | 1 | 10 | Royaume + Famille |
| **Marquis** | §6 Or | 75 | 4 | 3 | Territoire + Famille |
| **Duc** | §5 Violet | 50 | 3 | 1 | Territoire + Famille |
| **Comte** | §9 Bleu | 30 | 2 | 2 | Ville + Guilde |
| **Baron** | §a Vert | 15 | 1 | 1 | - |
| **Chevalier** | §f Blanc | 5 | illimite | 0 | - |
| **Roturier** | §7 Gris | 0 | - | 0 | - |

## Chaine feodale
```
Roi
├── Marquis (max 4)
│    ├── 1 Comte
│    └── 2 Barons
├── Duc (max 3)
│    └── 1 Baron
└── Comte (max 2)
     └── 2 Chevaliers
```

## Promotion
- Le **Roi** nomme Marquis, Ducs, Comtes via **Decrets**
- Le **Marquis** peut nommer 1 Comte + 2 Barons
- Le **Duc** peut nommer 1 Baron
- Chaque noble peut adouber des **Chevaliers** (selon sa limite)

## Incompatibilites
- Un noble avec une **famille** ne peut PAS rejoindre une guilde
- Un Comte gere sa ville via sa guilde
- Un Roi ne peut pas etre Pape (cumul interdit ?)

## Fichier cle
- `titles/nobility/NobilityRank.java` — enum des rangs
- `titles/nobility/NobilityManager.java` — promotions et compteurs

## Liens
- [[Decrets Royaux]] - Items de promotion
- [[Dignite et Aura]] - Systeme de dignite
- [[../Noblesse]] - Vue d'ensemble