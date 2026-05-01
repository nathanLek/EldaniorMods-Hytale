# Systeme d'Inventaire Personnel

#inventaire #coffre #stockage

## Coffre personnel
- **45 slots** (grille 9×5)
- Accessible via l'onglet **Inventaire** du SystemScreen
- Persistant (sauvegarde dans l'EntityStore)

## Fonctionnement
- Clic sur un slot du coffre → transfere dans la hotbar
- Clic sur un slot de la hotbar → depose dans le coffre
- Les items sont sauvegardes automatiquement

## Onglet Inventaire (SystemScreen)
- Affiche les 45 slots du coffre
- Affiche les 9 slots de la hotbar en dessous
- Tooltips avec nom, rarete, description, durabilite

## Fichiers cles
- `Inventory/components/PlayerPersonalChestData.java` - 45 slots persistants
- `gui/tabs/InventaireTab.java` - Onglet GUI avec tooltips

## Liens
- [[Architecture/GUI SystemScreen]] - Onglet Inventaire
- [[Architecture/Persistence]] - Sauvegarde EntityStore