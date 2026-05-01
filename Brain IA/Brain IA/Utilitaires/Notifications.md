# Systeme de Notifications

#notification #message #hud #alerte

## Types de notifications

### 1. Notification en bas a droite
```java
NotificationHelper.sendNotification(playerRef, message, style);
```
Styles : `Default`, `Success`, `Warning`, `Danger`

### 2. Grand titre au centre (Event Title)
```java
NotificationHelper.showEventTitle(playerRef, titre, sousTitre, isMajor);
```
Utilise pour : level up, quete terminee, entree dans un territoire, nouvelle classe

### 3. Level Up
```java
NotificationHelper.showLevelUpTitle(playerRef, level);
```

## Formatage (TinyMsg)
Systeme de tags XML-like pour le texte :
```
<color:red>Texte rouge</color>
<color:#FF0000>Hex couleur</color>
<b>Gras</b>
<i>Italique</i>
<u>Souligne</u>
<reset>Reset format</reset>
```

## Utilisation dans le projet
| Contexte | Type | Exemple |
|----------|------|---------|
| Esquive | Effet visuel | Dodge_Left/Right |
| Skill proc | Notification | "+10% degats" |
| Coffre tresor | Notification Success | "Tresor decouvert !" |
| Zone protegee | Notification Warning | "Zone protegee !" |
| Level up | Event Title | "NIVEAU 50" |
| Quete finie | Event Title | "QUETE TERMINEE" |
| Entree territoire | Event Title | "Eldanior / ROYAUME" |
| Entree parcelle | Notification | "[Parcelle] Maison1" |

## Fichiers cles
- `Leveling/utils/NotificationHelper.java` - Toutes les methodes
- `Leveling/utils/TinyMsg.java` - Formatage des messages

## Liens
- [[Architecture/GUI SystemScreen]] - Interface principale
- [[Systems/Territoires]] - Notifications de zone
