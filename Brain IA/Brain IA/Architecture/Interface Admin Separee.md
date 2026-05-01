# Interface Admin Separee

#architecture #admin #gui #separation

> **IMPLEMENTE** le 2026-05-01

## Architecture
- **Joueur** : `/es system` → SystemScreen (15 tabs, zero logique admin)
- **Admin (OP)** : `/es admin` → AdminScreen (5 tabs admin dedies)

## Fichiers crees
| Fichier | Role |
|---------|------|
| `gui/AdminScreen.java` | Ecran admin (InteractiveCustomUIPage) avec 5 onglets |
| `gui/AdminCommand.java` | Commande `/es admin` avec check OP |
| `Common/UI/Custom/Admin/AdminPage.ui` | Layout UI admin |

## Fichiers modifies
| Fichier | Changement |
|---------|-----------|
| `gui/SystemScreen.java` | ~250 lignes admin supprimees, 16→15 tabs |
| `Common/UI/Custom/System/SystemPage.ui` | 287 lignes admin supprimees (#TabAdmin, #TabBtnAdmin) |
| `ESCommand.java` | Ajout `addSubCommand(new AdminCommand())` |

## 5 Onglets Admin
1. **Dashboard** : Stats serveur, selection joueur, actions (XP, or, classe, noblesse, eglise, PK, titres)
2. **Territoires** : Gestion des territoires avec tous les boutons admin
3. **Proprietes** : Gestion des proprietes avec prix/loyer/protection/delete
4. **Economie** : Gestion economique (price cycling, force trade, shop)
5. **Resets** : Reset guildes, familles, parcelles, shop, classements, reset complet

## Securite
- Double verification : commande + build() verifient `EldaniorLogger.ADMIN_PERMISSION`
- Aucune logique admin dans SystemScreen joueur
- Les boutons admin des territoires/proprietes ne sont plus visibles en mode joueur

## Liens
- [[GUI SystemScreen]] - Interface joueur (nettoyee)
- [[../Commandes/Liste des commandes]] - /es admin ajoute
