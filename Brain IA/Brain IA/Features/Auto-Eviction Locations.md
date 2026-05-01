# Auto-Eviction des Locations

#feature #critique #territoire #location

## Etat actuel
- `ParcelData.isRentExpired()` existe (ligne 300-302) — verifie si le temps est depasse
- `ParcelData.isInGracePeriod()` existe (ligne 303-308) — periode de grace 24h
- `ParcelManager.evict()` existe (ligne 238-244) — expulse un locataire
- **MAIS aucun systeme automatique n'appelle ces methodes**

## Consequences
- Un locataire dont le bail expire **garde l'acces indefiniment**
- La periode de grace n'est jamais verifiee
- Le GUI affiche "EXPIRE" mais le locataire conserve toutes ses permissions
- Seule l'intervention manuelle d'un admin peut expulser

## Implementation proposee

### 1. Systeme de tick d'expiration
```java
// Nouveau : RentalExpirationSystem.java (EntityTickingSystem)
public class RentalExpirationSystem extends EntityTickingSystem {
    private long lastCheck = 0;
    private static final long CHECK_INTERVAL = 60_000; // 1 minute
    
    @Override
    public void tick(float deltaTime) {
        long now = System.currentTimeMillis();
        if (now - lastCheck < CHECK_INTERVAL) return;
        lastCheck = now;
        
        for (ParcelData parcel : ParcelManager.getAllParcels()) {
            if (!parcel.isRented()) continue;
            
            if (parcel.isRentExpired()) {
                if (!parcel.isInGracePeriod()) {
                    // Grace period depassee → eviction
                    ParcelManager.evict(parcel.getId());
                    notifyPlayer(parcel.getRenterUUID(), "Votre bail a expire !");
                    notifyPlayer(parcel.getOwnerUUID(), "Le locataire a ete expulse.");
                } else {
                    // En grace period → avertissement
                    notifyPlayer(parcel.getRenterUUID(), 
                        "Votre bail expire ! Renouvelez dans les 24h.");
                }
            }
        }
    }
}
```

### 2. Avertissements progressifs
| Temps avant expiration | Action |
|----------------------|--------|
| 24h | Notification "Bail expire demain" |
| 1h | Notification urgente "1h restante !" |
| 0 | Debut grace period (24h) |
| -24h | Eviction automatique |

### 3. Verification au login
```java
// Dans EldaniorSystem.onPlayerJoin()
for (ParcelData parcel : ParcelManager.getRentedByPlayer(playerUUID)) {
    if (parcel.isRentExpired() && !parcel.isInGracePeriod()) {
        ParcelManager.evict(parcel.getId());
        // Notifier le joueur
    }
}
```

## Fichiers a modifier
- `territory/systems/` → nouveau `RentalExpirationSystem.java`
- `EldaniorSystem.java` → enregistrer le nouveau system
- `territory/ParcelManager.java` → ajouter `getRentedByPlayer(UUID)`
- `territory/ParcelData.java` → ajouter derniere notification envoyee

## Priorite
**CRITIQUE** — Systeme de location inutilisable sans auto-eviction

## Liens
- [[Systems/Territoires]] - Systeme de parcelles
- [[Systems/Economie]] - Impact economique