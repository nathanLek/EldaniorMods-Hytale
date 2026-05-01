# Quetes Journalieres

#quetes #daily #journaliere #cooldown

## Fonctionnement
- **5 quetes** selectionnees aleatoirement chaque jour
- Tirees parmi **102+ quetes journalieres** definies
- Reset a minuit (`getDayOfYear()` change)
- Cooldown individuel par quete (`cooldownMinutes`)

## Selection quotidienne
```java
DAILY_SELECTION_COUNT = 5;
// Chaque jour, QuestManager selectionne 5 quetes parmi toutes les journalieres
// La selection change a chaque nouveau jour
```

## Distribution par type
| Type | Nombre de quetes definies | Exemples |
|------|--------------------------|----------|
| CHASSE | 28 | "Tuer 10 Loups", "Tuer 5 Dragons" |
| MASSACRE | 11 | "Tuer 50 mobs", "Tuer 200 mobs" |
| COLLECTION | 11 | "Collecter 100 Or", "Collecter 5000 Or" |
| EXPLORATION | 11 | "Trouver 3 coffres", "Trouver 10 coffres" |
| DUEL | 11 | "Gagner 1 duel", "Gagner 5 duels" |
| EXECUTION | 30+ | "Eliminer un joueur PK" |
| **Total** | **102+** | 5 selectionnees par jour |

## Cooldowns
- Chaque quete journaliere a un cooldown apres completion
- Le cooldown est stocke dans `cooldownData` (serialise dans PlayerLevelData)
- Format : `questId=endTimestamp|questId2=endTimestamp2`
- Les cooldowns expires sont filtres au chargement

## Probleme connu
- Les **quetes actives** (en cours, progression partielle) ne sont **pas persistees**
- Si le serveur restart, le joueur perd sa progression sur la quete en cours
- Seuls les cooldowns sont sauvegardes

## Fichiers cles
- `quest/QuestManager.java` — selection daily, cooldowns
- `quest/definitions/daily/` — definitions des quetes journalieres

## Liens
- [[Types de Quetes]] - Les 6 types de quetes
- [[Progression et Recompenses]] - Flow et rewards
- [[../Quetes]] - Vue d'ensemble
