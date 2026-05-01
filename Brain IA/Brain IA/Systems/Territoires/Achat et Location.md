# Achat et Location de Parcelles

#territoire #economie #achat #location

## Achat
1. Le joueur ouvre l'onglet **Proprietes** dans le SystemScreen
2. Voit les parcelles disponibles a proximite (section "Disponibles")
3. Clique sur une parcelle → detail avec prix
4. Bouton **ACHETER** → deduction du prix + taxe 12%
5. Le joueur devient OWNER de la parcelle
6. `purchaseType = "BOUGHT"`

## Location
1. Le proprietaire met sa parcelle en location (prix hebdomadaire)
2. Un joueur voit la parcelle dans "Disponibles"
3. Bouton **LOUER** → deduction du loyer + taxe 12%
4. Le locataire obtient les permissions OWNER
5. Le proprietaire perd ses permissions pendant la location
6. `purchaseType = "RENTED"`, `rentEndTime = now + 7 jours`

## Distribution de la taxe (12%)
Lors de chaque transaction (achat ou location) :
| Destinataire | Part |
|-------------|------|
| Ville | 15% de la taxe |
| Duc | 20% de la taxe |
| Marquis | 30% de la taxe |
| Royaume | 35% de la taxe |

## Renouvellement
- Le locataire peut renouveler via le GUI avant expiration
- Le loyer est deducte + taxe redistribuee
- `rentEndTime` prolonge de 7 jours

## Expiration
- Si le bail expire → periode de grace 24h
- Apres 24h → eviction (non implementee automatiquement)
- GUI affiche "EXPIRE - En attente de paiement"

## Prix configurables (admin)
- Cycle de prix : 0, 500, 1000, 5000, 10000, 50000
- Cycle de loyer : 0, 100, 500, 1000, 5000
- Modifiable via boutons dans le GUI admin

## Fichiers cles
- `gui/tabs/ProprietesTab.java` — GUI achat/location
- `territory/ParcelManager.java` — `buyParcel()`, `rentParcel()`
- `territory/ParcelEconomyManager.java` — `distributeTax()`

## Liens
- [[Types et Hierarchie]] - Types de parcelles
- [[Permissions et Roles]] - Permissions du locataire
- [[../Economie]] - Systeme economique
- [[../Territoires]] - Vue d'ensemble
