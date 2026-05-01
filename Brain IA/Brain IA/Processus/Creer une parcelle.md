# Processus : Creer une Parcelle

#processus #parcelle #territoire #guide

## Methode 1 : Selection Tool (recommande)
1. Equipez le **Selection Tool** de Hytale
2. Selectionnez la zone (2 points)
3. Tapez : `/es parcel create <TYPE> <NOM>`
4. La selection est capturee automatiquement via `SelectionManager.getSelectionProvider().computeSelectionCopy()`

## Methode 2 : Commandes pos1/pos2
1. Allez au coin 1 → `/es parcel pos1 _`
2. Allez au coin oppose → `/es parcel pos2 _`
3. `/es parcel create <TYPE> <NOM>`

## Types disponibles
| Type | Cree dans | Par qui |
|------|-----------|---------|
| KINGDOM | Top level | Admin |
| TERRITORY | Kingdom ou Territory | Admin / Roi / Marquis / Duc |
| CITY | Territory | Admin / Comte |
| PLOT | City | Admin uniquement |
| HOUSING | City | Admin uniquement |
| ROOM | Housing | Admin uniquement |
| FARM | City ou Territory | Admin uniquement |

## Detection automatique du parent
La commande teste 3 points (coin1, centre, coin2) pour trouver la zone parente.
Si le parent est invalide (ex: Plot dans un Kingdom), la creation est refusee.

## Apres la creation
1. La parcelle est creee SANS proprietaire
2. Pour les PLOT/HOUSING → le parent (ville) est assigne comme proprio
3. Configurer le prix : `/es parcel setprice <prix> _` et `/es parcel setrent <prix> _`
4. Assigner une famille : `/es parcel assign <familyId> _`
5. Assigner une guilde (villes) : `/es parcel assignguild <guildId> _`

## Ou via le GUI
Dans l'onglet **Proprietes** :
- Cliquer sur la parcelle → panneau de detail
- Boutons : PRIX VENTE, PRIX LOCAT., FAMILLE, GUILDE, PROTECTION

## Liens
- [[Systems/Territoires]] - Documentation complete
- [[Commandes/Liste des commandes]] - Toutes les commandes
