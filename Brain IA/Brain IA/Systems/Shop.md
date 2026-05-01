# Systeme de Shop

#shop #marche #vente #blackmarket

## Deux marches

### Shop (joueurs normaux)
- Visible dans l'onglet **Shop** (non-PK)
- Les joueurs peuvent vendre leurs items
- Max 3 listings par joueur
- Pagination dans le GUI

### Marche Noir (PK / Admin)
- Visible dans l'onglet **Black Market**
- Accessible uniquement aux joueurs PK ou admins
- Meme fonctionnement que le shop

## Structure d'une offre
```java
ShopListing {
    sellerUUID, sellerName
    itemId, itemQuantity
    price (en Or)
    timestamp
}
```

## Revenus hors-ligne
- Si le vendeur est deconnecte quand quelqu'un achete, l'argent est stocke dans `pendingEarnings`
- A la prochaine connexion, le joueur recoit ses gains

## Commande
- `/es sell` - Vendre l'item en main

## Fichiers cles
- `shop/ShopManager.java` - Gestion des listings + pendingEarnings
- `shop/SellCommand.java` - Commande de vente
- `gui/tabs/ShopTab.java` - Onglet Shop
- `gui/tabs/BlackMarketTab.java` - Onglet Marche Noir

## Liens
- [[Systems/Economie]] - Monnaie
- [[Architecture/GUI SystemScreen]] - Onglets Shop et Black Market