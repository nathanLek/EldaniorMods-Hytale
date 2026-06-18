# Interface SystemScreen

#gui #interface #onglets

## Acces
Commande `/es system` → ouvre le menu principal

## Onglets (17 total)

| Onglet | Visible pour | Couleur |
|--------|-------------|---------|
| Profil | Tous | Or |
| Inventaire | Tous | Standard |
| Competences | Tous | Standard |
| Guilde | Tous | Standard |
| Groupe | Tous | Standard |
| Famille | Duc+ ou avec famille | Standard |
| Titres | Tous | Standard |
| Quetes | Tous | Standard |
| Classements | Tous | Standard |
| Duel | Tous | Orange |
| Shop | Non-PK | Standard |
| Black Market | PK ou Admin | Standard |
| Territoires | Nobles (Roi/Marquis/Duc/Comte) ou Admin | Marron |
| Proprietes | Tous | Standard |
| Echanges | Marchands, Dragon ou Admin | Or |
| Wiki | Tous | Standard |
| Admin | Permission setlevel | Rouge |

### WikiTab (13 pages)
Onglet encyclopedique accessible a tous, contenant : General, Leveling, Groupe, Duel, Echange, Guilde, Famille, Noblesse, Territoires, Classes, Competences, Titres, Admin.

## Architecture technique
- `SystemScreen.java` extends `InteractiveCustomUIPage`
- `SystemPage.ui` → layout complet (~3800 lignes)
- Chaque onglet = une classe Tab (ex: `ProfilTab.java`, `QuestTab.java`)
- Event bindings : bouton → action string → handleDataEvent → switch

## Pattern de refresh
```java
UICommandBuilder update = new UICommandBuilder();
MonTab.populate(update, ref, store);
this.sendUpdate(update);
```

## Panneau deroulant (detail)
Utilise dans Proprietes et Territoires :
- Clic sur un element → `selectedIndex` + `selectedParcelId`
- Re-clic → deselectionne
- Le panneau `#PropDetail` ou `#TerrDetail` se montre/cache

## Fichiers cles
- `gui/SystemScreen.java` - Controleur principal (~1000 lignes)
- `gui/SystemCommand.java` - Commande /es system
- `gui/tabs/*.java` - 17 onglets (dont WikiTab.java)
- `Common/UI/Custom/System/SystemPage.ui` - Layout

## Liens
- [[Architecture/ECS Systems]] - Systemes qui alimentent les donnees
- [[Architecture/Persistence]] - Sauvegarde des donnees affichees
