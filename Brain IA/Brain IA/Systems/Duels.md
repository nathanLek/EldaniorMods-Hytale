# Systeme de Duels

#duel #pvp #combat

## Flow
1. Ouvrir l'onglet **Duel** dans le SystemScreen
2. Voir la liste des joueurs connectes
3. Cliquer **DEFIER** → envoie un defi
4. Le joueur cible tape `/es duel accept` ou `/es duel decline`
5. Si accepte → le duel commence
6. Le joueur qui tombe a 5% HP perd (pas de mort)

## Protection
- `DuelProtectionSystem` empeche la mort pendant un duel
- Si HP <= 5% max → remet a 1 HP et fin du duel
- Gere aussi les **Arenes** (via `ArenaManager`), pas uniquement les duels 1v1

## XP et economie
- Le **perdant** perd **10% de son XP** total
- Le **gagnant** recoit cette XP perdue
- En fin de duel, les deux joueurs sont heal complet (**HP + Mana** via `healFull()`)

## Stats
- Victoires / Defaites
- Streak / Meilleur Streak (note : le streak n'est pas implemente dans DuelManager, seuls `addDuelWin`/`addDuelLoss` existent)
- Historique recent

## Commandes
```
/es duel accept _   → Accepter le defi
/es duel decline _  → Refuser
```

## Fichiers cles
- `duel/DuelManager.java` - Gestion des duels actifs
- `duel/DuelCommand.java` - Commande accept/decline
- `duel/DuelProtectionSystem.java` - Protection anti-mort
- `gui/tabs/DuelTab.java` - Onglet GUI

## Liens
- [[Systems/Territoires]] - PvP desactivable par ville
- [[Systems/Quetes]] - Quetes journalieres de duel
