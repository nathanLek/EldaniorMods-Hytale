# Systeme de Guildes

#guilde #groupe #social

## Creation
- `/es guildcreate <nom> <tag>` ou via le bouton dans l'onglet Guilde
- Le createur devient **Chef**

## Roles
| Role | Permissions |
|------|-------------|
| CHEF | Tout (invite, kick, promote, disband, tresorerie) |
| OFFICER | Invite, kick |
| MEMBER | Acces basique |

## Commandes
```
/es guild invite <joueur> _    → Inviter
/es guild kick <joueur> _      → Exclure
/es guild promote <joueur> _   → Promouvoir en Officer
/es guild demote <joueur> _    → Retrograder en Member
/es guild info <nom> _         → Info guilde
/es guild leave _              → Quitter
/es guild accept _             → Accepter invitation
/es guild decline _            → Refuser
/es guilddisband               → Dissoudre
```

## Tresorerie
- Depot/retrait via l'onglet Guilde
- Les villes gerees par une guilde alimentent la tresorerie

## Incompatibilites
- Un joueur avec une **famille noble** ne peut PAS rejoindre de guilde
- Un joueur ne peut etre que dans 1 guilde a la fois

## Lien avec les Villes
- Les Comtes gerent les villes via leur guilde
- `/es parcel assignguild <guildId> _` assigne une guilde a une ville
- Les taxes de la ville alimentent la tresorerie de la guilde

## Fichiers cles
- `guild/GuildManager.java` - Registre des guildes
- `guild/Guild.java` - Modele (membres, tresorerie, stats)
- `guild/commands/GuildCommand.java` - Commandes
- `gui/tabs/GuildeTab.java` - Onglet GUI

## Liens
- [[Systems/Territoires]] - Villes gerees par des guildes
- [[Systems/Economie]] - Tresorerie des guildes
- [[Systems/Noblesse]] - Incompatibilite famille/guilde
