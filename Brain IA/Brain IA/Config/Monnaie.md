# Systeme de Monnaie

#monnaie #coins #or #economie

## 5 Types de pieces

| Piece | Valeur | Rarete coffre Or |
|-------|--------|-----------------|
| Elda Copper Coins | 1 Or | 100% |
| Elda Silver Coins | 10 Or | 20% |
| Elda Gold Coins | 100 Or | 1% |
| Elda Diamond Coins | 1 000 Or | 0.03% |
| Elda Zenith Coins | 10 000 Or | 0.0001% |

## Interaction
- Les pieces utilisent `ConsumableItemMoneyInteraction`
- Clic F sur une piece → ajoute la valeur au solde
- La piece disparait de la hotbar

## Argent de depart
- Nouveau joueur : **0 Or**
- Reset (niveau 1) : **1 000 Or**

## Sources de revenus
| Source | Montant |
|--------|---------|
| Pieces (coffres Or) | 1 - 10 000 |
| Vente d'items (Shop) | Variable |
| Quetes | Variable (recompense) |
| Location immobiliere | Prix du loyer (88%) |
| Consommable | Pillule d'Or (via StatsItem) |

## Depenses
| Depense | Montant |
|---------|---------|
| Achat immobilier | Prix du bien |
| Location immobiliere | Prix du loyer / 7j |
| Achat en boutique | Prix du vendeur |
| Depot guilde/famille | Montant choisi |

## Fichiers cles
- `config/configs/CoinItemRegistry.java` - Mapping piece → valeur
- `skills/interaction/ConsumableItemMoneyInteraction.java` - Interaction
- `Server/Item/Items/Coins/*.json` - JSON des 5 pieces

## Liens
- [[Systems/Economie]] - Systeme economique global
- [[Config/LootTables]] - Pieces dans les coffres Or