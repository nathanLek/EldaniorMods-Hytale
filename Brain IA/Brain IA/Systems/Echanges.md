# Systeme d'Echanges

#echange #trade #marchand

## Acces
- Onglet **Echanges** dans le SystemScreen
- Visible uniquement pour les **Marchands** (classe Merchant) et les **admins**

## Flow
1. Le marchand ouvre l'onglet Echanges
2. Voit la liste des joueurs a proximite (15 blocs)
3. Clique **ECHANGER** → envoie une invitation
4. Le joueur cible tape `/es trade accept` ou `/es trade decline`
5. Si accepte → fenetre d'echange s'ouvre pour les deux

## Fenetre d'echange
- **Gauche** : mes items (2x6 = 12 slots)
- **Droite** : ses items (lecture seule)
- **Hotbar** en bas (clic = deposer dans l'echange)
- Clic sur mes slots = reprendre l'item
- Bouton **VALIDER** : chaque joueur valide independamment
- Bouton **ANNULER** : ferme pour les deux, rien echange
- Quand les DEUX ont valide → transfert des items

## Admin
- Bouton **FORCER** : ouvre directement la fenetre sans invitation
- L'admin se voit dans la liste (pour tester seul)

## Commandes
```
/es trade accept _   → Accepter l'invitation
/es trade decline _  → Refuser
/es trade cancel _   → Annuler
```

## Securite
- 1 seul echange a la fois par joueur
- Deconnexion → echange annule, items rendus
- Les bindings de boutons utilisent l'index (pas l'ID classe) pour eviter les bugs de reroll

## Fichiers cles
- `trade/TradeManager.java` - Invitations + sessions
- `trade/TradeSession.java` - Modele d'un echange (12 slots par joueur)
- `trade/TradeCommand.java` - /es trade accept/decline/cancel
- `trade/TradeScreen.java` - Fenetre d'echange interactive
- `Common/UI/Custom/Trade/Trade.ui` - Layout de la fenetre
- `gui/tabs/EchangesTab.java` - Liste des joueurs + invite

## Liens
- [[Systems/Classes]] - Classe Marchand
- [[Architecture/GUI SystemScreen]] - Onglet Echanges
