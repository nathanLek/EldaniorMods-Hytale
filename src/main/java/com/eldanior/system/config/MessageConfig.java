package com.eldanior.system.config;

/**
 * Centralise TOUS les messages visibles par les joueurs.
 * Les messages de type NOTIFICATION supportent la syntaxe Hytale : {@code <color:red>texte</color>}
 * Les messages de type CHAT sont en texte brut (pas de couleur supportee dans le chat Hytale).
 */
public enum MessageConfig {

    // ==================== GENERAL ====================
    LOGIN_WELCOME("Bienvenue sur Eldanior, {0} !\nNiveau {1} - {2}\nTapez /es system pour commencer votre aventure !"),
    PERMISSION_DENIED("Vous n'avez pas la permission."),
    PLAYER_NOT_FOUND("Joueur introuvable : {0}"),
    PLAYER_MUST_BE_ONLINE("Le joueur doit etre connecte."),
    NO_DATA("Aucune donnee."),
    ERROR("Erreur : {0}"),
    UNKNOWN_COMMAND("Commande inconnue : {0}"),
    INVENTORY_FULL("Inventaire plein !"),

    // ==================== LEVELING / XP ====================
    XP_GAINED("+{0} XP !"),
    XP_LOST("-{0} XP"),
    LEVEL_SET("Niveau defini a {0}."),
    LEVEL_CHANGED("Votre niveau a ete change a : {0}."),
    LEVEL_REQUIREMENT("Niveau minimum requis : {0} (vous etes niveau {1})"),
    MONEY_GAINED("+{0} Or !"),
    MONEY_LOST("-{0} Or"),
    MONEY_REQUIREMENT("Argent minimum requis : {0} (vous avez {1})"),
    INSUFFICIENT_FUNDS("Pas assez d'Or !"),
    CHARACTER_RESET("Personnage reinitialise (Niveau 1)."),

    // ==================== DUEL ====================
    DUEL_CHALLENGE_SENT("Defi envoye a {0} ! (expire dans 60s)"),
    DUEL_CHALLENGE_RECEIVED("{0} vous defie en duel !"),
    DUEL_CHALLENGE_INSTRUCTIONS("Tapez /es duel accept ou /es duel decline (expire dans 60s)"),
    DUEL_STARTED("DUEL COMMENCE ! Battez-vous !"),
    DUEL_STARTED_OPPONENT("DUEL COMMENCE ! {0} a accepte !"),
    DUEL_WON("Duel gagne ! Vous avez battu {0}"),
    DUEL_LOST("Duel perdu ! {0} vous a vaincu."),
    DUEL_CANCELLED("Duel annule ! Votre adversaire s'est deconnecte."),
    DUEL_EXPIRED("Le defi de duel a expire."),
    DUEL_DECLINED("Defi refuse."),
    DUEL_DECLINED_OPPONENT("{0} a refuse votre defi."),
    DUEL_NO_PENDING("Aucun defi en attente."),
    DUEL_ALREADY_IN("Vous etes deja en duel."),
    DUEL_TARGET_ALREADY_IN("{0} est deja en duel."),
    DUEL_PENDING_EXISTS("Vous avez deja un defi en attente ! Attendez qu'il expire ou soit accepte/refuse."),
    DUEL_CANNOT_SELF("Vous ne pouvez pas vous defier vous-meme."),
    DUEL_XP_REWARD("+{0} XP (mise du duel)"),
    DUEL_XP_PENALTY("-{0} XP (mise du duel)"),

    // ==================== GUILD ====================
    GUILD_USAGE("Usage : /es guild <invite|kick|promote|demote|info|leave|accept|decline|recruitment|join> <arg>"),
    GUILD_NOT_IN("Vous n'etes dans aucune guilde."),
    GUILD_ALREADY_IN("Vous etes deja dans une guilde."),
    GUILD_TARGET_ALREADY_IN("{0} est deja dans une guilde."),
    GUILD_NOBLE_RESTRICTION("{0} fait partie d'une famille noble et ne peut pas rejoindre une guilde."),
    GUILD_NOBLE_SELF("Vous faites partie d'une famille noble et ne pouvez pas rejoindre une guilde."),
    GUILD_NOBLE_CREATE("Vous faites partie d'une famille noble. Les nobles ne peuvent pas creer de guilde."),
    GUILD_CANNOT_JOIN("Vous ne pouvez pas rejoindre de guilde."),
    GUILD_NOT_FOUND("Guilde '{0}' introuvable."),
    GUILD_INVITE_SENT("Invitation envoyee a {0} pour rejoindre {1}"),
    GUILD_INVITE_RECEIVED("{0} vous invite a rejoindre la guilde {1}"),
    GUILD_INVITE_INSTRUCTIONS("Tapez /es guild accept pour accepter ou /es guild decline pour refuser."),
    GUILD_NO_INVITE("Aucune invitation en attente."),
    GUILD_NO_LONGER_EXISTS("La guilde n'existe plus."),
    GUILD_INVITE_DECLINED("Invitation refusee."),
    GUILD_JOINED("Vous avez rejoint la guilde {0} !"),
    GUILD_JOINED_NOTIFY("{0} a rejoint votre guilde via recrutement ouvert."),
    GUILD_LEFT("Vous avez quitte votre guilde."),
    GUILD_DISBANDED("La guilde a ete dissoute."),
    GUILD_KICKED("Vous avez ete exclu de la guilde {0}"),
    GUILD_KICKED_CONFIRM("{0} a ete exclu de la guilde."),
    GUILD_CHEF_ONLY("Seul le Chef peut effectuer cette action."),
    GUILD_INVITE_ONLY("Seul le Chef ou un Officier peut inviter."),
    GUILD_CANNOT_LEAVE_CHEF("Vous devez transferer le role de Chef ou dissoudre la guilde avant de partir."),
    GUILD_LEAVE_INSTRUCTIONS("Utilisez /es guilddisband pour dissoudre."),
    GUILD_PROMOTED("{0} est maintenant {1}"),
    GUILD_PROMOTED_SELF("Vous etes maintenant {0} de votre guilde !"),
    GUILD_DEMOTED("{0} retrogade a {1}"),
    GUILD_DEMOTED_SELF("Vous avez ete retrogade a {0}"),
    GUILD_ALREADY_PROMOTED("Ce joueur est deja Officier ou Chef."),
    GUILD_NOT_OFFICER("Ce joueur n'est pas Officier."),
    GUILD_NOT_IN_YOURS("Ce joueur n'est pas dans votre guilde."),
    GUILD_CREATED("Guilde {0} {1} creee !"),
    GUILD_CREATED_INFO("Vous etes le Chef. -150,000 or."),
    GUILD_CREATED_INSTRUCTIONS("Invitez des membres avec /es guild invite <joueur>"),
    GUILD_TAG_LENGTH("Le tag doit faire entre 2 et 5 caracteres."),
    GUILD_NAME_EXISTS("Une guilde avec ce nom existe deja."),
    GUILD_TAG_EXISTS("Ce tag est deja utilise."),
    GUILD_INVALID_NAME("Nom ou tag invalide (3-24 chars, tag 2-5 lettres, pas de doublon)."),
    GUILD_RECRUITMENT_OPEN("Recrutement ouvert ! Les joueurs peuvent maintenant rejoindre votre guilde librement."),
    GUILD_RECRUITMENT_CLOSED("Recrutement ferme. Les joueurs doivent etre invites pour rejoindre."),
    GUILD_RECRUITMENT_USAGE("Usage : /es guild recruitment <on|off>"),
    GUILD_RECRUITMENT_CHEF_ONLY("Seul le Chef peut changer le mode de recrutement."),
    GUILD_JOIN_USAGE("Usage : /es guild join <nom_guilde>"),
    GUILD_NOT_OPEN("Cette guilde n'est pas en recrutement ouvert. Demandez une invitation."),
    GUILD_TREASURY_DEPOSIT("+{0} Or depose dans la tresorerie !"),
    GUILD_TREASURY_WITHDRAW("-{0} Or retire de la tresorerie !"),
    GUILD_DISBAND_CHEF_ONLY("Seul le Chef peut dissoudre la guilde."),

    // ==================== FAMILY / NOBILITY ====================
    FAMILY_USAGE("Usage : /es family <choose|invite|info> <familyId/joueur>"),
    FAMILY_UNKNOWN("Famille '{0}' inconnue."),
    FAMILY_AVAILABLE("Disponibles : {0}"),
    FAMILY_TAKEN("Cette famille est deja prise."),
    FAMILY_RANK_REQUIRED("Seuls les Marquis et Ducs peuvent choisir une famille."),
    FAMILY_ALREADY_IN("Vous appartenez deja a une famille."),
    FAMILY_RANK_MISMATCH("Cette famille est reservee aux {0}."),
    FAMILY_PATRIARCH("Vous etes Patriarche de la famille {0} !"),
    FAMILY_MOTTO("Devise : {0}"),
    FAMILY_PASSIVE("Competence : {0}"),
    FAMILY_INVITE_ONLY("Seul le Patriarche ou le Vice-Patriarche peut inviter."),
    FAMILY_NOT_IN("Vous n'appartenez a aucune famille."),
    FAMILY_TARGET_ALREADY_IN("{0} appartient deja a une famille."),
    FAMILY_TARGET_NOT_NOBLE("{0} n'est pas noble."),
    FAMILY_JOINED("{0} a rejoint {1} en tant que Membre."),
    FAMILY_JOINED_SELF("Vous avez rejoint la famille {0}"),
    FAMILY_DISBANDED("La famille a ete dissoute par le Patriarche."),
    FAMILY_VICE_SET("{0} est maintenant Vice-Patriarche."),
    FAMILY_VICE_SET_SELF("Vous etes maintenant Vice-Patriarche !"),
    FAMILY_VICE_ONLY("Seul le Patriarche peut nommer un Vice-Patriarche."),
    FAMILY_NOT_IN_YOURS("{0} n'est pas dans votre famille."),

    // ==================== NOBILITY RANKS ====================
    RANK_INVALID("Rang invalide. Utilisez : baron, comte, duc, marquis"),
    RANK_PERMISSION("Seul le Roi ou un Admin peut promouvoir."),
    RANK_NO_SLOTS("Plus de places pour {0}"),
    RANK_PROMOTED("{0} promu au rang de {1}"),
    RANK_CHOOSE_FAMILY("Choisissez votre famille : /es family choose <familyId>"),

    // ==================== CHURCH ====================
    CHURCH_USAGE("Usage : /es church <setpope|demote|ordain|info|status> <joueur>"),
    CHURCH_POPE_SET("{0} est maintenant {1} !"),
    CHURCH_POPE_SET_SELF("Vous etes desormais le {0} !"),
    CHURCH_DEMOTE_ONLY("Seul le Pape ou un Admin peut retrograder."),
    CHURCH_ALREADY_LAIQUE("Ce joueur est deja Laique."),
    CHURCH_DEMOTED("{0} retrogade a {1}"),
    CHURCH_DEMOTED_SELF("Vous avez ete retrogade a {0}"),
    CHURCH_ORDAIN_RANK("Vous devez etre au moins Pretre pour ordonner."),
    CHURCH_ACOLYTE_LIMIT("Limite d'acolytes atteinte ({0} max)."),
    CHURCH_ALREADY_HAS_RANK("Ce joueur a deja un rang d'eglise."),
    CHURCH_ORDAINED("{0} est maintenant {1}"),
    CHURCH_ORDAINED_SELF("Vous avez ete ordonne {0} par {1} !"),

    // ==================== TERRITORY / PARCELS ====================
    ZONE_PROTECTED("Zone protegee ! ({0})"),
    ZONE_ACCESS_DENIED("Acces interdit ! ({0})"),
    ZONE_PVP_DISABLED("PvP desactive dans cette zone !"),
    PARCEL_NOT_IN("Vous n'etes dans aucune parcelle."),
    PARCEL_NOT_OWNER("Pas proprietaire."),
    PARCEL_PRICE_INVALID("Prix invalide."),
    PARCEL_RENT_SET("Prix de location defini : {0} Or/7j"),
    PARCEL_DELETED("{0} supprime !"),
    PARCEL_TAXES_COLLECTED("{0} Or d'impots collectes pour {1}"),
    PARCEL_TREASURY_TRANSFERRED("{0} Or transferes depuis {1}"),

    // ==================== TRADE / SHOP ====================
    TRADE_SENT("Demande d'echange envoyee a {0}"),
    TRADE_RECEIVED("{0} veut echanger avec vous !"),
    TRADE_INSTRUCTIONS("Tapez /es trade accept ou /es trade decline"),
    TRADE_FORCED("Echange force avec {0} !"),
    TRADE_ADMIN_OPENED("Un admin a ouvert un echange avec vous !"),
    SHOP_ALREADY_PURCHASED("Cet objet a deja ete achete !"),
    SHOP_PURCHASED("Objet achete au Marche Noir pour {0} Or !"),
    SHOP_LISTING_REMOVED("Annonce retiree du Marche Noir."),
    SHOP_LISTING_REMOVED_ADMIN("Admin a retire votre annonce du Marche Noir."),
    SHOP_SOLD("{0} a achete votre objet (Marche Noir) pour {1} Or !"),
    SHOP_EARNINGS("+{0} Or recu de ventes au marche !"),
    PARCEL_EARNINGS("+{0} Or recu de ventes de parcelles !"),

    // ==================== QUEST ====================
    QUEST_NPC_NO_QUEST("[PNJ] Je n'ai rien pour vous pour le moment."),
    QUEST_NPC_INFO("[PNJ] Quete : {0}"),
    QUEST_NPC_NEW("[PNJ] Nouvelle quete : {0}"),
    QUEST_NPC_OBJECTIVE("Objectif : {0}"),
    QUEST_NPC_REWARD("Recompense : {0}"),
    QUEST_ACCEPTED("Quete acceptee !"),
    QUEST_COMPLETE("[PNJ] Quete terminee : {0} !"),
    QUEST_IN_PROGRESS("[PNJ] Quete en cours : {0}"),
    QUEST_PROGRESS("Progression : {0} / {1}"),
    QUEST_ABANDONED("Quete abandonnee."),
    QUEST_TITLE_UNLOCKED("Titre debloque : {0}"),
    QUEST_NEW_AVAILABLE("Nouvelle quete disponible : parlez au PNJ !"),

    // ==================== SKILLS ====================
    SKILL_NOT_LEARNED("Vous n'avez pas appris cette competence !"),
    SKILL_ALREADY_OWNED("Vous possedez deja cet item !"),
    SKILL_IN_CHEST("Cet item est deja dans votre coffre personnel !"),
    SKILL_OBTAINED("Item obtenu : {0}"),

    // ==================== TREASURE CHEST ====================
    CHEST_DISCOVERED("Coffre au tresor decouvert !"),

    // ==================== CLASS ====================
    CLASS_SET("Classe : {0}"),

    // ==================== HOLOGRAM ====================
    HOLOGRAM_CREATED("Hologramme cree ! ID: {0}"),
    HOLOGRAM_DELETED("Hologramme {0} supprime ! (restart pour despawn)"),
    HOLOGRAM_NOT_FOUND("Hologramme introuvable: {0}"),
    HOLOGRAM_NONE("Aucun hologramme."),
    HOLOGRAM_POSITION_ERROR("Position introuvable."),

    // ==================== ADMIN ====================
    ADMIN_PLAYER_RESET("Joueur reset !"),
    ADMIN_TITLES_RESET("Titres reinitialises !"),
    ADMIN_GUILDS_RESET("Guildes reinitialises !"),
    ADMIN_FAMILIES_RESET("Familles et Noblesse reinitialises !"),
    ADMIN_PARCELS_RESET("Parcelles reinitialises !"),
    ADMIN_SHOP_RESET("Shop et Marche Noir reinitialises !"),
    ADMIN_LEADERBOARDS_RESET("Classements reinitialises !"),
    ADMIN_FULL_RESET("RESET COMPLET"),
    ADMIN_FULL_RESET_DETAILS("Guildes, Familles, Parcelles, Classements, Shop reinitialises !"),
    ADMIN_RESTART_NEEDED("Redemarrez le serveur pour finaliser."),
    ADMIN_PK_TOGGLE_ON("Vous etes maintenant PK !"),
    ADMIN_PK_TOGGLE_OFF("PK retire."),
    ADMIN_TITLE_GRANTED("Titre accorde : {0}"),
    ADMIN_TITLE_REMOVED("Titre retire : {0}"),
    ADMIN_FAMILY_ASSIGNED("Famille assignee : {0}"),
    ADMIN_PVP_STATUS("PVP {0} pour {1}"),
    ADMIN_PROTECTION_STATUS("Protection {0} pour {1}"),
    ADMIN_RANK_SET("Rang : {0}"),
    ADMIN_CHURCH_SET("Eglise : {0}"),
    ADMIN_INVASION_HEADER("=== INVASION EN COURS ==="),
    ADMIN_INVASION_ALERT("Des creatures hostiles envahissent le monde !"),
    ADMIN_INVASION_PREPARE("Preparez-vous au combat !"),
    ADMIN_BACKUP_COMPLETE("Sauvegarde manuelle terminee en {0}ms."),
    ADMIN_RESTORE_COMPLETE("Restauration terminee en {0}ms."),
    ADMIN_RESTORE_NOTE("Note : les donnees joueurs en memoire restent inchangees jusqu'a reconnexion."),
    ADMIN_BACKUP_USAGE("Usage : /es backup <save|restore>"),

    // ==================== NOTIFICATIONS (avec <color:> Hytale) ====================
    NOTIF_ZONE_PROTECTED("<color:red>Zone protegee !</color> <color:gray>({0})</color>"),
    NOTIF_ZONE_ACCESS_DENIED("<color:red>Acces interdit !</color> <color:gray>({0})</color>"),
    NOTIF_PVP_DISABLED("<color:red>PvP desactive dans cette zone !</color>"),
    NOTIF_FARM_REGEN("<color:gold>Regeneration de {0} dans {1}s !</color>"),
    NOTIF_DUNGEON_REGEN("<color:gold>Le donjon {0} se regenere dans {1}s !</color>"),
    NOTIF_XP_GAINED("<color:green>+{0} XP</color>"),
    NOTIF_XP_LOST("<color:red>-{0} XP</color>"),
    NOTIF_LEVEL_UP("<color:gold>Niveau {0} atteint !</color>"),
    NOTIF_MONEY_GAINED("<color:green>+{0} Or</color>"),
    ;

    private final String template;

    MessageConfig(String template) {
        this.template = template;
    }

    public String get() {
        return template;
    }

    /**
     * Formate le message en remplacant {0}, {1}, {2}... par les arguments.
     */
    public String format(Object... args) {
        String result = template;
        for (int i = 0; i < args.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return result;
    }
}
