# Taxes Hebdomadaires

#feature #haute #economie #territoire

## Etat actuel
- `ParcelEconomyManager.collectWeeklyTaxes()` est **vide** (lignes 110-113)
- Le commentaire dit "les taxes sont distribuees automatiquement via distributeTax()"
- `PersistenceManager` appelle cette methode toutes les 5 minutes (ligne 50) — mais elle ne fait rien
- Les taxes sont percues **uniquement** lors des transactions (achat/location)
- Pas de taxe periodique sur les proprietaires

## Probleme
Sans taxe hebdomadaire :
- Un joueur achete une propriete une fois et ne paie plus jamais rien
- La tresorerie des villes/royaumes stagne apres les ventes initiales
- L'economie n'a pas de flux regulier d'or

## Implementation proposee

### 1. Taxe hebdomadaire sur les proprietaires
```java
public static void collectWeeklyTaxes() {
    long now = System.currentTimeMillis();
    
    for (ParcelData parcel : ParcelManager.getAllParcels()) {
        // Seulement les parcelles avec proprietaire
        if (parcel.getOwnerUUID() == null) continue;
        // Seulement les types taxables (PLOT, HOUSING, ROOM)
        if (!isTaxable(parcel.getType())) continue;
        // Verifier le cooldown de 7 jours
        if (!parcel.canCollectTax()) continue;
        
        int taxAmount = calculateWeeklyTax(parcel);
        
        // Deduire du joueur
        PlayerLevelData data = getPlayerData(parcel.getOwnerUUID());
        if (data != null && data.getMoney() >= taxAmount) {
            data.removeMoney(taxAmount);
            distributeTax(parcel, taxAmount); // Distribuer dans la hierarchie
            parcel.setLastTaxCollection(now);
            parcel.setLastTaxAmount(taxAmount);
        } else {
            // Pas assez d'or → avertissement
            notifyPlayer(parcel.getOwnerUUID(), 
                "Vous n'avez pas assez d'or pour payer la taxe de " + parcel.getName());
            // Apres 2 semaines sans paiement → saisie ?
        }
    }
}

private static int calculateWeeklyTax(ParcelData parcel) {
    // Taxe = prix d'achat * taux hebdo
    return (int)(parcel.getPrice() * 0.02); // 2% du prix par semaine
}
```

### 2. Barème de taxe
| Type | Taxe hebdo | Base |
|------|-----------|------|
| PLOT | 2% du prix | Prix d'achat |
| HOUSING | 2% du prix | Prix d'achat |
| ROOM | 1% du prix | Prix plus bas |
| FARM | 3% du prix | Ressource renouvelable |
| CITY/TERRITORY/KINGDOM | 0% | Pas de taxe |

### 3. Consequences du non-paiement
| Semaines impayees | Consequence |
|-------------------|------------|
| 1 | Avertissement |
| 2 | Perte des permissions BUILD/BREAK |
| 3 | Saisie automatique (retour au proprietaire de la ville) |

## Inconsistance a corriger
**ParcelEconomyManager** (15%, 20%, 30%, 35%) vs **TerritoiresTab** (57%, 80%, 87.5%)
→ Deux systemes de pourcentages differents pour la meme chose
→ **Unifier** en un seul systeme de distribution

## Fichiers a modifier
- `territory/ParcelEconomyManager.java` — implementer collectWeeklyTaxes()
- `territory/ParcelData.java` — ajouter compteur semaines impayees
- `territory/ParcelManager.java` — ajouter saisie automatique
- `persistence/PersistenceManager.java` — ajuster le timer (hebdo, pas 5min)

## Priorite
**HAUTE** — L'economie a besoin de flux reguliers

## Liens
- [[Systems/Economie]] - Systeme economique
- [[Systems/Territoires]] - Hierarchie des parcelles
- [[Balance/Economie Taxes]] - Inconsistance des taux