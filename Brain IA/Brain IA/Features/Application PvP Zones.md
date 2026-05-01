# Application PvP Zones

#feature #haute #pvp #territoire

## Etat actuel
- `ParcelData.pvpEnabled` existe (ligne 40) — boolean stocke et persiste
- Le toggle PvP fonctionne dans `TerritoiresTab.java` (lignes 314-321)
- Seules les **villes** peuvent toggler le PvP (Royaume/Territoire ne peuvent pas)
- `CombatStatsSystem.java` verifie le PvP **avant** le dodge

## Problemes identifies

### 1. PvP non applique completement
Le check dans `CombatStatsSystem` bloque les degats mais :
- **Pas de feedback visuel** pour le joueur attaquant ("PvP desactive ici")
- **Pas de notification** quand on entre/sort d'une zone PvP
- Les effets visuels d'attaque se declenchent quand meme (confusion)

### 2. Permission PVP non verifiee
`ParcelPermission.PVP` est defini dans l'enum mais **jamais utilise** dans le code de permissions. Le check PvP est en dur dans CombatStatsSystem.

### 3. Toggle PvP sans verification de permission
**Fichier** : `gui/tabs/TerritoiresTab.java` lignes 314-321
- Le bouton toggle est visible pour les admins uniquement
- Mais pas de check si le joueur est **proprietaire de la ville** (Comte)

## Implementation proposee

### 1. Notification d'entree en zone PvP
```java
// Dans ParcelRangeSystem, quand le joueur entre dans une ville
if (parcel.getType() == ParcelType.CITY) {
    if (parcel.isPvpEnabled()) {
        showNotification(player, "§c⚔ Zone PvP ACTIVE", NotificationStyle.Default);
    } else {
        showNotification(player, "§a🛡 Zone PvP desactivee", NotificationStyle.Default);
    }
}
```

### 2. Feedback quand le PvP est bloque
```java
// Dans CombatStatsSystem, quand PvP est desactive
if (!pvpEnabled) {
    // Notification a l'attaquant
    showNotification(attacker, "§cPvP desactive dans cette zone !");
    // Annuler les effets visuels d'attaque
    return; // avant l'application des effets
}
```

### 3. Permission Comte pour toggle
```java
// Dans TerritoiresTab, toggle PvP
boolean isOwner = parcel.getOwnerUUID() != null 
    && parcel.getOwnerUUID().equals(playerUUID);
boolean canToggle = isAdmin || isOwner;
// Afficher le bouton seulement si canToggle
```

## Priorite
**HAUTE** — Le PvP fonctionne partiellement mais manque de polish

## Liens
- [[Systems/Territoires]] - Gestion des villes
- [[Systems/Combat]] - Systeme de combat
- [[Systems/Duels]] - Duels (non affectes par les zones)