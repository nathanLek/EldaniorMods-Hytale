# Commandes `/es`

Mis à jour le 2026-11-19. Règle : seules les commandes de la première liste sont pour les joueurs ; toutes les autres sont
des commandes d'admin. Permissions : `/es` est ouverte à tous (`requireNoPermission()` dans `ESCommand`) ; chaque
commande joueur appelle aussi `requireNoPermission()` dans son constructeur ; les autres gardent la permission générée par
le serveur (admins seulement), parfois doublée d'un contrôle `EldaniorLogger.ADMIN_PERMISSION`. Attention :
`canGeneratePermission()` n'existe plus dans le serveur, la redéfinir ne fait rien (c'est ce qui bloquait `/es system`).

## Joueurs

| Commande | Rôle |
|---|---|
| `/es system` | Ouvre le menu système (profil, runes, quêtes, guilde, groupe, titres, marché…). |
| `/es party` | Groupe : create, invite, kick, leave, disband, accept, decline, list. |
| `/es duel` | Défier un joueur, accepter ou refuser un duel. |
| `/es trade` | Échange entre joueurs : accept, decline, cancel. |
| `/es guild` | Guilde : invite, kick, promote, demote, info, leave, accept, decline. |
| `/es guilddisband` | Dissoudre sa guilde (chef seulement). |
| `/es kingdom` | Vue d'ensemble du royaume (roi et nobles). |
| `/es sell` | Mettre en vente l'objet en main au marché. |
| `/es familier info` | État de son familier (élément, âge, niveau, XP, œuf) et du malus de deuil. `relacher --confirmer confirmer` : rompre le lien. |
| `/es ville` | Comte : registre de sa ville (celle où il se tient, sinon la première) : trésor, règles, lois, biens, Bailli. Même fenêtre que la borne de la ville et le Bailli. |
| `/incliner` | S'incliner (salut du Clan Luo). |
| `/es observateur proposer --joueur <nom>` · `revoquer` · `renoncer` · `liste` · `nom [--nom <nom d'emprunt>]` · `executer --joueur <nom>` | Observateur (classe unique) : le Roi ou l'Empereur Luo (Patriarche Luo) nomme son assassin privé (une place par trône) ; admins : `--trone roi|empereur`. `nom` : nom d'emprunt au-dessus de la tête. `executer` : le souverain efface le personnage de son Observateur (niveau 1, tout perdu, point d'apparition). |

Ce qui ne passe plus par une commande :
- **supprimées le 2026-11-12** : `/es ancre`, `/es title`, `/es titlelist`, `/es nstatus`, `/es ordre`, `/es classinfo` ;
- **runes** : `/es runes` est supprimée, les runes s'équipent dans le menu système, onglet Compétences (Runes actives) ;
- **biens** : achat et location aux pancartes et affiches de propriété (`/es parcel` réservée aux admins) ;
- **argent** : dépôt, retrait et change chez le banquier (`/es retirer` réservée aux admins) ;
- **famille** : gestion chez l'intendant, ordres de chevalier compris ; **guilde** : fondation chez un Maître de guilde (`/es guildcreate` reste admin).

## Admins

| Domaine | Commandes |
|---|---|
| Progression | `addxp`, `setlevel`, `setclass`, `titleadmin`, `combat log` (journal de combat dans le chat : chaque coup donné ou reçu, esquive et sa raison, Force, critique, défense ; avant armure) |
| Noblesse et Église | `rank`, `rankpromote`, `church`, `churchpromote`, `family`, `familyset`, `intendant fictifs ajouter\|retirer` (5 joueurs fictifs du Clan Luo et un ordre de démonstration, pour essayer l'intendant) |
| Territoires | `parcel`, `pcreate`, `pdelete`, `pinvite`, `pkick`, `psetperm`, `psell`, `psetprice`, `psetrent`, `passign`, `passignguild`, `psetrank`, `psetregen`, `royaume`, `evictall`, `taxaudit`, `invasion lancer\|arreter\|liste [vagues] [niveau] [prime]` (sur le domaine où l'on se tient, jamais de monstre en ville), `portail info\|retirer` (Portail Continental du Marquisat où l'on se tient) |
| PNJ et monde | `garde`, `contrat`, `hologram`, `deletetreasure`, `treasureconfig`, `getrelic` |
| Économie | `prix [recherche]` (éditeur des prix de tous les objets), `retirer`, `transactions`, `guildcreate` (les joueurs passent par le Maître de guilde) |
| Serveur | `admin` (interface d'administration), `backup`, `debug` |
| Familiers (tests) | `familier eclore`, `familier age <jeune\|adolescent\|adulte>`, `familier niveau <n>`, `familier element <nom>`, `familier malus` (retire le sien) |
