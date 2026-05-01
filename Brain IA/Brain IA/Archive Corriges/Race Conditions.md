# Race Conditions

#bug #critique #threading #concurrence #CORRIGE

> **CORRIGE** le 2026-05-01 — TRADE_LOCK, DUEL_LOCK, PARTY_LOCK synchronized ajoutes dans TradeManager, DuelManager, PartyManager.

## Probleme (RESOLU)
Plusieurs systemes utilisent des patterns **check-then-act** non atomiques, permettant des etats inconsistants en cas d'acces concurrent.

## Cas identifies

### 1. DuelManager — startDuel()
**Fichier** : `duel/DuelManager.java` lignes 59-63
```java
// Deux operations separees = fenetre de race condition
activeDuels.put(player1, duel);  // Op 1
activeDuels.put(player2, duel);  // Op 2 — entre les deux, un autre thread peut lire un etat partiel
```
**Risque** : Un joueur peut etre dans 2 duels simultanement

### 2. TradeManager — sendInvite()
**Fichier** : `trade/TradeManager.java` lignes 31-37
```java
if (isInTrade(senderUUID) || isInTrade(targetUUID)) return false;  // Check
if (pendingInvites.containsKey(targetUUID)) return false;           // Check
pendingInvites.put(targetUUID, senderUUID);                         // Act
```
**Risque** : Deux invitations simultanées au meme joueur

### 3. PartyManager — joinParty()
**Fichier** : `party/PartyManager.java` lignes 43-48
```java
if (party.isFull() || playerParty.containsKey(playerUUID)) return false; // Check
if (!party.addMember(playerUUID, playerName)) return false;              // Act
playerParty.put(playerUUID, party);                                       // Act
```
**Risque** : Joueur dans 2 groupes simultanement

## Correction proposee
```java
// Option 1 : synchronized block
public static synchronized void startDuel(UUID p1, UUID p2) {
    if (activeDuels.containsKey(p1) || activeDuels.containsKey(p2)) return;
    ActiveDuel duel = new ActiveDuel(p1, p2);
    activeDuels.put(p1, duel);
    activeDuels.put(p2, duel);
}

// Option 2 : ReentrantLock pour granularite
private static final ReentrantLock tradeLock = new ReentrantLock();
public static boolean sendInvite(UUID sender, UUID target) {
    tradeLock.lock();
    try {
        if (isInTrade(sender) || isInTrade(target)) return false;
        pendingInvites.put(target, sender);
        return true;
    } finally {
        tradeLock.unlock();
    }
}
```

## Priorite
**CRITIQUE** — Peut corrompre l'etat du jeu

## Liens
- [[Systems/Echanges]] - Trade system
- [[Systems/Duels]] - Duel system
- [[Systems/Groupe]] - Party system