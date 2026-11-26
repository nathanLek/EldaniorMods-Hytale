package com.eldanior.system.guards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;

/**
 * Sensei (2026-11-10, demande de l'utilisateur) : un par famille, en tenue noble. Maitre d'armes de sa famille (celle du
 * reglage du poste, sinon celle du territoire) :
 *  - Commercer : il enseigne (vend) les manuels secrets des techniques de la famille (boutique Eldanior_Sensei_<Famille>,
 *    scripts/guards/gen_sensei.py), aux seuls membres de la famille (et aux admins) ;
 *  - Duel : duel d'entrainement sans mort (spells.SenseiDuel) ;
 *  - Quetes : a venir (les quetes des PNJ sont desactivees, NpcServices.QUESTS_ENABLED).
 * Tenue noble : clans de l'Empire Luo -> kimono de Doyen du clan ; Clan Luo -> kimono de la Cour ; Runkandel -> tenue de
 * Capitaine ; autres familles -> soie (en attendant leurs tenues nobles).
 */
public final class Sensei {

    private Sensei() {}

    /** Kimono de l'Empire Luo du Sensei (LuoNpcs.outfit) : Doyen du clan, Cour pour le Clan Luo. */
    static String luoOutfit(GuardManager.Post p) {
        String id = GuardManager.familyIdOf(p);
        if (id != null)
            for (com.eldanior.system.luo.LuoClans.Clan c : com.eldanior.system.luo.LuoClans.Clan.values())
                if (c.id.equals(id)) return c.elderOutfit;
        return "Cour";
    }

    /** Tenue hors de l'Empire Luo. */
    static List<String> armor(GuardManager.Post p) {
        String id = GuardManager.familyIdOf(p);
        if ("runkandel".equals(id)) return List.of("Armor_Runkandel_Capitaine_Head", "Armor_Runkandel_Capitaine_Chest",
                "Armor_Runkandel_Capitaine_Hands", "Armor_Runkandel_Capitaine_Legs");
        return List.of("Armor_Cloth_Silk_Head", "Armor_Cloth_Silk_Chest", "Armor_Cloth_Silk_Hands", "Armor_Cloth_Silk_Legs");
    }

    static String greeting(GuardManager.Post p) {
        String fam = p.family != null ? p.family : "notre maison";
        return "La voie de " + fam + " ne s'apprend pas dans les livres seuls. Que veux-tu, élève ?";
    }

    private static final java.util.Random RNG = new java.util.Random();

    /**
     * Replique d'un disciple (2026-11-13, demande de l'utilisateur) : generique, sans lien avec un Sensei, car il y aura des
     * disciples d'autres clans et familles, et des ronins qui s'entrainent seuls dans la nature. Elle depend de son rang, de
     * son clan ou de sa famille, du lieu (en ville ou loin de tout) ; challenger = le joueur vient le defier (duel de quete).
     */
    static String discipleGreeting(GuardManager.Post p, boolean challenger) {
        int tier = MartialRanks.tier(p);
        String rank = MartialRanks.rankName(p);
        com.eldanior.system.luo.LuoClans.Clan clan = MartialRanks.clan(p);
        String house = clan != null ? "le clan " + clan.label : p.family != null ? "la famille " + p.family : null;
        if (challenger) {
            String[] l = {
                    "On m'a dit que tu viendrais. Salue, et tire ta lame : je ne retiendrai pas mes coups.",
                    "Tu cherches un adversaire ? Tu l'as trouvé. Que nos lames parlent pour nous.",
                    "Un duel ? Bien. Un seul de nous deux repartira debout.",
                    house != null ? "Je porte l'honneur de " + house + ". Si tu veux le défier, propose-moi un duel."
                            : "Je n'ai ni maître ni maison, seulement ma lame. Si tu veux l'éprouver, propose-moi un duel."};
            return l[RNG.nextInt(l.length)];
        }
        java.util.List<String> l = new java.util.ArrayList<>(java.util.List.of(
                "Cent coupes à l'aube, cent au crépuscule. La lame ne ment jamais.",
                "Chaque kata est une question. Je cherche encore la réponse.",
                "Ne reste pas dans l'axe de ma lame, voyageur."));
        if (tier <= 2) {
            l.add("Mes bras tremblent encore après mille coupes. Un jour, ils ne trembleront plus.");
            l.add("Je ne suis qu'au début de la voie. Mais je ne m'arrêterai pas.");
        } else if (tier <= 4) {
            l.add("Ma garde est solide. Elle peut l'être davantage.");
            l.add("Le corps apprend vite. L'esprit, beaucoup plus lentement.");
        } else {
            l.add((rank != null ? rank : "Maître") + ", disent-ils. Moi, je ne vois que le chemin qui reste.");
            l.add("Plus je m'élève, plus je vois combien la voie est longue.");
        }
        String place = p.world != null ? MartialDuels.placeName(p.world, p.position()) : "";
        if (place.startsWith("loin") || place.startsWith("près")) {          // seul, loin des villes : a la maniere d'un ronin
            l.add("Ici, pas de maître pour corriger ma garde : seulement le vent et ma lame.");
            l.add("Je m'entraîne loin des villes. Le silence est un bon professeur.");
        }
        if (house != null) l.add("Je m'exerce pour l'honneur de " + house + ".");
        return l.get(RNG.nextInt(l.size()));
    }

    /** « Sensei Haruki », ou le nom complet d'un disciple (« Kenji Sama - Expert Martial »). */
    static String speaker(GuardManager.Post p) {
        return p.type == GuardType.SENSEI ? "Sensei " + GuardManager.givenName(p) : GuardManager.nameplate(p);
    }

    /** Boutique : manuels de la famille pour ses membres (et les admins), equipement du dojo pour tous. */
    static void openShop(GuardManager.Post p, Ref<EntityStore> playerRef, Store<EntityStore> store) {
        Player player = store.getComponent(playerRef, Player.getComponentType());
        PlayerRef pr = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (player == null || pr == null) return;
        String fam = GuardManager.familyIdOf(p);
        PlayerLevelData data = store.getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());
        boolean admin = pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION);
        String mine = data != null && data.getNobleFamilyId() != null ? FamilyManager.migrateId(data.getNobleFamilyId()) : null;
        // 2026-11-13 (demande de l'utilisateur) : le Sensei ne vend plus que l'equipement du dojo (kimono de ronin, Bokken,
        // Parchemin de la Voie Martiale), a tous ; les manuels s'obtiennent par les quetes (Epreuves du Dojo).
        String shop = DOJO_SHOP;
        boolean family = false;
        try {
            // 2026-11-12 : seulement les manuels que le joueur peut apprendre a son rang martial (hors de la voie : Apprenti),
            // ni ceux qu'il connait deja ou porte deja ; vaut aussi pour les admins (pour verifier le filtre)
            int tier = Math.max(1, com.eldanior.system.classes.definitions.luo.VoieMartiale.tier(data));
            player.getPageManager().openCustomPage(playerRef, store, new ShopPage(pr, shop,
                    GuardManager.nameplate(p) + (family ? " — techniques secrètes" : " — équipement du dojo"), "#c9a24a",
                    LegendarySmiths.BUYS_NOTHING).allLevels()
                    .only(id -> com.eldanior.system.skills.runes.spells.LuoManuels.learnable(id, tier, data, playerRef, store)
                            && !(isBook(id) && com.eldanior.system.skills.runes.spells.LuoManuels.hasBook(playerRef, store, id))));
        } catch (Exception e) {
            EldaniorLogger.error("[Sensei] boutique " + p.id, e);
        }
    }

    private static boolean isBook(String id) {
        return id.startsWith(com.eldanior.system.skills.runes.spells.LuoManuels.PREFIX) || id.startsWith("Eldanior_Mouvement_");
    }

    /** Boutique de l'equipement du dojo, ouverte a tous (scripts/guards/gen_sensei.py). */
    static final String DOJO_SHOP = "Eldanior_Sensei_Dojo";

    /** Bokken du Dojo : arme du duel d'entrainement (2026-11-10 : le Bokken est la base des katanas Luo). */
    static final String BOKKEN = "Eldanior_Weapon_Sword_Katana_Bokken";

    /**
     * Duel : contre le Sensei, entrainement au Bokken sans mort ; contre un disciple (2026-11-13, demande de l'utilisateur),
     * katana de fer, sans filet de vie : tuer ou etre tue (le disciple tue tombe vraiment, son poste le fait revenir).
     */
    static void duel(GuardManager.Post p, Ref<EntityStore> npc, Ref<EntityStore> playerRef, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (pr == null || npc == null || !npc.isValid()) return;
        boolean disciple = p.type == GuardType.DISCIPLE;
        boolean ok = com.eldanior.system.skills.runes.spells.SenseiDuel.challenge(playerRef,
                disciple ? request(p, npc, LuoNpcs.IRON_KATANA, true, true) : request(p, npc, BOKKEN, false, false), store);
        if (!ok) pr.sendMessage(Message.raw(speaker(p) + " : « Un duel à la fois. »"));
    }

    /** Duel lance depuis la fenetre des quetes (« Vaincre le Sensei ») ; null si lance, sinon la raison du refus. */
    public static String duelFromQuest(String postId, Ref<EntityStore> playerRef, Store<EntityStore> store) {
        GuardManager.Post p = postId != null ? GuardManager.post(postId) : null;
        com.hypixel.hytale.server.core.universe.world.World world = p != null
                ? com.hypixel.hytale.server.core.universe.Universe.get().getWorld(p.getWorld()) : null;
        Ref<EntityStore> npc = world != null && p.guard != null ? world.getEntityStore().getRefFromUUID(p.guard) : null;
        if (npc == null || !npc.isValid()) return "Le Sensei doit être présent.";
        boolean ok = com.eldanior.system.skills.runes.spells.SenseiDuel.challenge(playerRef, request(p, npc, BOKKEN, false, false), store);
        return ok ? null : speaker(p) + " : « Un duel à la fois. »";
    }

    private static com.eldanior.system.skills.runes.spells.SenseiDuel.Request request(GuardManager.Post p, Ref<EntityStore> npc,
                                                                                       String weapon, boolean lethal, boolean toDeath) {
        return new com.eldanior.system.skills.runes.spells.SenseiDuel.Request(npc, GuardManager.armorOf(p), weapon, speaker(p),
                MartialRanks.level(p), MartialRanks.clan(p), lethal, p.id, toDeath);
    }

    /**
     * Un coup porte au Sensei civil (DivineDamageSystem, avant les degats) : le coup est toujours annule. Si l'agresseur est un
     * joueur et que le Sensei n'est pas deja en combat, il riposte (2026-11-10) : son combattant tire un katana de fer et se bat
     * pour de vrai, avec toutes les techniques de son rang. true si la cible est un Sensei (le coup doit etre annule).
     */
    public static boolean onAttacked(Ref<EntityStore> target, Ref<EntityStore> attacker, Store<EntityStore> store) {
        if (target == null || !target.isValid()) return false;
        com.hypixel.hytale.server.npc.entities.NPCEntity npc = store.getComponent(target,
                com.hypixel.hytale.server.npc.entities.NPCEntity.getComponentType());
        GuardType type = npc != null ? GuardType.fromRole(npc.getRoleName()) : null;
        if (type != GuardType.SENSEI && type != GuardType.DISCIPLE) return false;          // disciples aussi (2026-11-13)
        if (attacker == null || !attacker.isValid() || store.getComponent(attacker, Player.getComponentType()) == null) return true;
        if (com.eldanior.system.skills.runes.spells.SenseiDuel.busy(target)) return true;
        GuardManager.Post p = GuardManager.postOf(target, store);
        if (p == null) return true;
        com.eldanior.system.skills.runes.spells.SenseiDuel.challenge(attacker, request(p, target, LuoNpcs.IRON_KATANA, true, p.type == GuardType.DISCIPLE), store);
        return true;
    }
}
