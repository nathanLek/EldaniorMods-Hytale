# Permissions GUI Manquantes

#bug #critique #securite #permissions #PARTIEL

> **PARTIELLEMENT CORRIGE** le 2026-05-01 :
> - Constante `EldaniorLogger.ADMIN_PERMISSION` creee et utilisee dans 11 fichiers
> - AdminTab.executePendingCommand() : check admin ajoute avant execution
> - **RESOLU**

## Probleme (PARTIELLEMENT RESOLU)
Plusieurs operations admin dans le GUI n'ont **aucune verification de permission**. Un joueur pourrait potentiellement executer des actions admin si l'interface est accessible.

## AdminTab — 10+ methodes sans verification
**Fichier** : `gui/tabs/AdminTab.java`

| Methode | Ligne | Action | Permission ? |
|---------|-------|--------|-------------|
| handleResetLevel() | 202 | Reset le niveau d'un joueur | NON |
| handleAddXP() | 235 | Ajouter de l'XP | NON |
| handleSetLevel() | 244 | Forcer un niveau | NON |
| handleGiveGold() | 256 | Donner de l'or | NON |
| handleSetPK() | 265 | Forcer le status PK | NON |
| handleResetTitles() | 274 | Reset les titres | NON |
| handleSetClass() | 283 | Forcer une classe | NON |
| handleNobilityPromote() | 295 | Promouvoir noblesse | NON |
| handleChurchPromote() | 307 | Promouvoir eglise | NON |
| executePendingCommand() | 106 | Executer une commande | NON |

## Permission admin hardcodee
```java
boolean isAdmin = player.hasPermission("eldanior.command.setlevel");
```
Utilisee dans 5+ fichiers comme **proxy** pour la detection admin, au lieu d'un role admin propre.

## Risque d'injection de commande (AdminTab)
```java
String[] parts = pendingCommand.split("\\s+");
switch (parts[0]) { ... }
```
Si `pendingCommand` contient des placeholders non remplaces (`<player>`), la commande est executee avec des valeurs brutes.

## Autres tabs avec checks manquants
| Tab | Probleme |
|-----|----------|
| TitresTab | `handleEquip()` — pas de validation titre deverrouille |
| EchangesTab | `handleForceOpen()` — isAdmin non verifie cote serveur |
| GroupeTab | `handleKick()` — pas de check role capitaine |

## Correction proposee
```java
// Constante centralisee
public static final String ADMIN_PERMISSION = "eldanior.admin";

// Verification avant chaque action admin
if (!player.hasPermission(ADMIN_PERMISSION)) {
    NotificationHelper.send(player, "Permission refusee");
    return;
}
```

## Priorite
**CRITIQUE** — Faille de securite majeure

## Liens
- [[../Admin/Outils Admin Manquants]] - Outils admin
- [[../Architecture/Validation Entrees]] - Validation des entrees
