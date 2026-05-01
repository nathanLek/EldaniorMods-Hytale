# Systeme de Groupe (Party)

#groupe #party #coop

## Creation
- `/es party create _` ou via l'onglet Groupe du SystemScreen
- Le createur devient **Capitaine**
- Max **5 membres** par groupe

## Roles
| Role | Permissions |
|------|-------------|
| Capitaine | Invite, kick, disband |
| Membre | Voir la liste, quitter |

## HUD
- `PartyHud.ui` affiche les membres du groupe avec leurs HP
- `PartyHudUpdateSystem` met a jour toutes les ~20 ticks
- Integre dans le `CombinedHud` (partie droite de l'ecran)

## Commandes
```
/es party create _    → Creer un groupe
/es party invite <j>  → Inviter
/es party kick <j>    → Exclure
/es party leave _     → Quitter
/es party disband _   → Dissoudre
/es party accept _    → Accepter invitation
/es party decline _   → Refuser
/es party list _      → Lister les membres
```

## Deconnexion
- Si un membre se deconnecte → retire du groupe automatiquement
- Si le capitaine se deconnecte → le premier membre devient capitaine

## Fichiers cles
- `party/PartyManager.java` - Gestion des groupes
- `party/Party.java` - Modele (membres, capitaine)
- `party/PartyHud.java` - HUD du groupe
- `party/PartyHudUpdateSystem.java` - Systeme ECS de mise a jour
- `party/commands/PartyCommand.java` - Commandes
- `gui/tabs/GroupeTab.java` - Onglet GUI

## Liens
- [[Utilitaires/CombinedHud]] - Integration HUD
- [[Architecture/GUI SystemScreen]] - Onglet Groupe
