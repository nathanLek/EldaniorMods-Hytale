# Systeme de Classements

#classement #leaderboard #ranking

## 4 Categories de classement

| Categorie | Critere | Donnees |
|-----------|---------|---------|
| **Mobs** | Nombre de mobs tues | mobKills total |
| **PvP** | Nombre de joueurs tues | playerKills |
| **Duel** | Nombre de duels gagnes | duelWins |
| **Guildes/Familles** | Score combine | contribution + (tresorerie / 100) |

## Structure
```java
RankEntry {
    playerName / guildName
    score
}
```

## Onglet GUI
- Onglet **Classements** dans le SystemScreen
- Boutons pour switcher entre categories : MOBS, PVP, GUILDES/FAM, DUEL
- Affichage top 10 par categorie

## Fichiers cles
- `classement/ClassementManager.java` - Calcul et stockage des classements
- `gui/tabs/ClassementsTab.java` - Onglet GUI

## Liens
- [[Systems/Duels]] - Wins comptabilisees
- [[Systems/Guildes]] - Score des guildes
- [[Systems/Familles]] - Score des familles