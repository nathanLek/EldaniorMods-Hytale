# Validation des Entrees

#architecture #moyenne #securite #validation #PARTIEL

> **PARTIELLEMENT CORRIGE** le 2026-05-01 :
> - GuildManager.createGuild() : validation nom (3-24 chars), tag (2-5 chars), regex, doublons
> - ADMIN_PERMISSION centralise dans EldaniorLogger
> - RateLimiter.java cree (cooldown 3s sur trade.invite, extensible)
> - Validation parent parcelles : VALID_CHILDREN map + check dans createParcel
> - **RESOLU**

## Probleme (PARTIELLEMENT RESOLU)
Plusieurs commandes et API acceptent des entrees utilisateur **sans validation**.

## Cas identifies

### 1. Noms de Guilde non valides
`guild/GuildManager.java` — pas de limite de longueur, pas de filtrage caracteres speciaux

### 2. Commandes sans trim()
`quest/QuestManager.java` — espaces dans les donnees → echec silencieux de parsing

### 3. ParcelManager — parentId non valide
`territory/ParcelManager.java` — accepte un parentId inexistant → parcelle orpheline

### 4. Pas de rate limiting
Aucune commande n'a de cooldown → spam possible

### 5. Permission admin hardcodee
```java
boolean isAdmin = player.hasPermission("eldanior.command.setlevel");
```
Utilise dans 5+ fichiers GUI (ShopTab, BlackMarketTab, TerritoiresTab, ProprietesTab, EchangesTab) au lieu d'une constante centralisee.

## Correction proposee
- Validation noms (3-24 char, `[a-zA-Z0-9_ -]+`)
- Validation parent-enfant avec `VALID_CHILDREN` map
- Rate limiter global
- Constante `ADMIN_PERMISSION` centralisee

## Liens
- [[../Bugs/Race Conditions]] - Check-then-act
- [[../Commandes/Liste des commandes]] - Commandes existantes