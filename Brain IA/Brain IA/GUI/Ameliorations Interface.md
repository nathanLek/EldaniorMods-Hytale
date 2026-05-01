# Ameliorations Interface GUI

#gui #basse #interface #ux

## Problemes identifies

### 1. ProprietesTab — Boutons manquants
**Fichier** : `gui/tabs/ProprietesTab.java`

| Manquant | Description |
|----------|------------|
| Bouton EXPULSER | Expulser un locataire (admin/proprio) — actuellement impossible via GUI |
| Liste des membres | Pas de vue des membres d'une parcelle avec leurs roles |
| Permissions GUI | Impossible de modifier les permissions via GUI (commandes uniquement) |
| Timer expiration | Pas de compte a rebours visible pour les locations |

### 2. TerritoiresTab — Informations incompletes
**Fichier** : `gui/tabs/TerritoiresTab.java`

| Manquant | Description |
|----------|------------|
| Historique taxes | Pas d'historique des taxes percues |
| Membres ville | Pas de liste des residents d'une ville |
| Alertes | Pas d'alerte quand des locations expirent |

### 3. PvP Toggle — Permission
- Le bouton PvP n'est visible que pour les admins
- Les Comtes (proprietaires de ville) devraient aussi pouvoir toggler
- Pas de feedback visuel (couleur du bouton selon l'etat PvP)

### 4. General GUI
| Amelioration | Description |
|-------------|------------|
| Confirmation dialogs | Pas de "Etes-vous sur ?" avant les actions destructives |
| Refresh automatique | Le GUI ne se rafraichit pas apres une action (acheter, louer) |
| Pagination | Listes longues sans pagination (parcelles, quetes) |

## Implementations suggérees

### Bouton EXPULSER
```java
// Dans ProprietesTab, section detail pour le proprietaire
if (iAmOwner && p.isRented()) {
    // Afficher bouton EXPULSER en rouge
    builder.updateText("#BtnEvict", "§c EXPULSER");
    builder.updateVisibility("#BtnEvict", true);
}
```

### Timer d'expiration
```java
// Afficher le temps restant
if (p.isRented() && p.getRentEndTime() > 0) {
    long remaining = p.getRentEndTime() - System.currentTimeMillis();
    String timeStr = formatDuration(remaining); // "2j 14h"
    builder.updateText("#RentTimer", "Expire dans: " + timeStr);
}
```

## Priorite
**BASSE** — Quality of life, pas bloquant

## Liens
- [[Features/Auto-Eviction Locations]] - Systeme d'eviction
- [[Features/Application PvP Zones]] - Toggle PvP
- [[Architecture/GUI SystemScreen]] - Architecture GUI