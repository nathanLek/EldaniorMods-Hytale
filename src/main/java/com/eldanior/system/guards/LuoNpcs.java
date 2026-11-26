package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.NpcAttitudes;
import com.eldanior.system.util.NpcFacing;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.eldanior.system.skills.runes.spells.LuoKatanas;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import org.joml.Vector3d;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * PNJ de poste poses sur le territoire du Clan Luo (Post.luo, releve a chaque apparition) : ils prennent la tenue du clan,
 * un prenom du clan pour les civils, s'inclinent devant les plaques et attaquent les etrangers sans plaque (luo.LuoClanSystem).
 *
 * Tenues (decidees le 2026-11-08) : garde de ville, garde royal, patrouilleur = Recrue ; chevalier = Sakura ; assassin = Ombre ;
 * pelerin = Pelerin ; villageois, aventurier, chasseur de prime, criminel = kimonos civils (Rapiece, Comptoir, Pelerin, tires
 * par poste). Mage royal, chevalier errant, maitre de guilde : inchanges.
 *
 * Gestes (jeu d'animations Luo_Gestes, scripts/npcs/gen_luo_anims.py), seulement au repos : les gardes Luo saluent de temps en
 * temps, le pelerin s'arrete pour prier (partout). Pendant un geste, Post.luoBusyUntil fige le PNJ.
 */
public final class LuoNpcs {

    private LuoNpcs() {}

    public static final String GESTURES = "Luo_Gestes";
    /** Variante de role du territoire Luo (scripts/guards/gen_postes_luo.py) : katana du clan, sans bouclier. */
    public static final String LUO_SUFFIX = "_Luo";
    /** Roles qui ont une variante _Luo. */
    static final Set<String> LUO_ROLES = Set.of("Eldanior_Guard_Ville", "Eldanior_Guard_Royal", "Eldanior_Guard_Patrouilleur",
            "Eldanior_Guard_Chevalier", "Eldanior_Guard_Assassin", "Eldanior_Guard_Disciple", "Eldanior_Guard_Mage", "Eldanior_Criminel",
            "Eldanior_Npc_Chasseur_Prime", "Eldanior_Npc_Aventurier_Compagnon");
    /** Katana de fer du clan : arme des guerriers du territoire (aventuriers, chasseurs, criminels). */
    static final String IRON_KATANA = "Eldanior_Weapon_Sword_Katana_Fer_Degainee";
    private static final String[] KATA = {"Kata1", "Kata2", "Kata3", "Kata4"};
    private static final int KATA_SERIES = 8;
    private static final long KATA_MS = 1_100, KATA_REST_MS = 20_000;
    private static final String[] CIVIL = {"Pauvre", "Marchand", "Pelerin", "Ronin"};
    /** Villageois : surtout le Kimono des Rizieres. */
    private static final String[] VILLAGE = {"Rizieres", "Rizieres", "Rizieres", "Pauvre", "Pelerin"};
    private static final String[] GIVEN_NAMES = {"Akane", "Haruki", "Ren", "Sora", "Hana", "Kaito", "Yui", "Daisuke", "Mei", "Takeshi",
            "Rin", "Hikaru", "Sakura", "Jun", "Aoi", "Kenji", "Mirae", "Seojun", "Hayun", "Jiwoo", "Minseo", "Taeyang", "Eun", "Kazuha",
            "Shiro", "Tomoe", "Masaru", "Chiyo", "Isamu", "Natsuki"};
    private static final Random RNG = new Random();

    private static final long APPEL_MS = 1_700;
    private static final long BOW_MS = 1_600, DEEP_BOW_MS = 2_800, SALUTE_MS = 2_000, PRAY_MS = 3_800;

    /** Kimono du clan pour ce poste, null si son type garde sa tenue. */
    static String outfit(GuardManager.Post p) {
        return switch (p.type) {
            case VILLE, PATROUILLEUR -> "Recrue";
            case ROYAL -> "Garde_Royale";                                                    // Garde Royale de l'Empereur (2026-11-10)
            case MAGE_ROYAL -> "Alchimiste";                                                 // pas de magie dans l'empire : alchimiste
            case CHEVALIER -> knightOutfit(p);
            case SENSEI -> Sensei.luoOutfit(p);                                              // tenue noble (Doyen du clan, Cour)                                               // tenue de sa famille (2026-11-10 nuit)
            case ASSASSIN -> "Ombre";
            case DISCIPLE -> discipleOutfit(p);                                              // selon son rang (2026-11-13)
            case AMBULANT -> "Marchand";                                                                   // kimono du Comptoir
            case COMMERCANT -> NpcTrades.metier(p) == NpcTrades.Metier.INTENDANT ? "Lettre" : "Marchand";  // intendant : Kariginu
            case VILLAGEOIS -> VILLAGE[(int) Math.floorMod(p.skinSeed >> 7, (long) VILLAGE.length)];
            case AVENTURIER -> "Ronin";                                                               // Ronin Vagabond
            case CHASSEUR_PRIME, CRIMINEL -> CIVIL[(int) Math.floorMod(p.skinSeed >> 7, (long) CIVIL.length)];
            default -> null;
        };
    }

    /**
     * Kimono d'un chevalier de l'Empire Luo selon sa famille (choisie dans le reglage du poste, sinon celle du territoire) :
     * clan Sama, Hanazakura, Kage ou Setsuka -> tenue du clan (LuoClans.Clan.outfit) ; Clan Luo ou aucune -> kimono de Garde.
     * Retour du 2026-11-10 (nuit) : tous les chevaliers portaient le kimono Sakura.
     */
    static String knightOutfit(GuardManager.Post p) {
        String id = GuardManager.familyIdOf(p);
        if (id != null)
            for (com.eldanior.system.luo.LuoClans.Clan c : com.eldanior.system.luo.LuoClans.Clan.values())
                if (c.id.equals(id)) return c.outfit;
        return "Garde";
    }

    /**
     * Kimono d'un disciple selon son rang martial (2026-11-13, « adapter la tenue en fonction de la classe du disciple ») :
     * debutant et Apprenti en Recrue, Disciple en Ronin, Expert en Garde, Maitre en Noble, Grand Maitre en Seigneur.
     */
    static String discipleOutfit(GuardManager.Post p) {
        return switch (MartialRanks.tier(p)) {
            case 2 -> "Ronin";
            case 3 -> "Garde";
            case 4 -> "Noble";
            case 5, 6, 7, 8 -> "Seigneur";
            default -> "Recrue";
        };
    }

    /** Le poste change-t-il d'apparence (skin de joueur + kimono) ? Sur le territoire Luo, et les disciples partout. */
    static boolean dressed(GuardManager.Post p) {
        return (p.luo || p.type == GuardType.DISCIPLE) && outfit(p) != null;
    }

    static List<String> armor(GuardManager.Post p) {
        String o = outfit(p);
        String prefix = o == null || o.isEmpty() ? "Armor_Kimono_" : "Armor_Kimono_" + o + "_";
        return List.of(prefix + "Head", prefix + "Chest", prefix + "Hands", prefix + "Legs");
    }

    /** Role a faire apparaitre : sa variante _Luo sur le territoire Luo, s'il en a une. */
    static String role(GuardManager.Post p, String roleId) {
        return p.luo && LUO_ROLES.contains(roleId) ? roleId + LUO_SUFFIX : roleId;
    }

    // ===== Fourreau =====
    // Comme le joueur (TenryumaruSystem) : fourreau en main gauche. Barre (variante de role _Luo) : 0 = lame au clair,
    // 1 = au fourreau ; en combat, le gabarit remet la barre 0 ; au repos, sheathTick joue le rengainage puis passe sur la barre 1.
    // Main gauche : on change l'objet de l'emplacement 0 (fourreau plein au repos, avec la poignee ; vide en combat) ; changer
    // d'emplacement actif ne tient pas sur un PNJ (le gabarit garde DefaultOffHandSlot, vu en jeu le 2026-11-08 : poignee absente).

    private static final long SHEATHE_MS = 700;

    /** Apres l'equipement : complete les emplacements du katana du clan (aventurier libre : toujours rengaine). */
    static void equipSheath(GuardManager.Post p, Ref<EntityStore> ref, Inventory inv, Store<EntityStore> store) {
        ItemContainer bar = inv.getHotbar(), util = inv.getUtility();
        // role sans main gauche (role civil) : rien a equiper, sinon « Slot is outside capacity » fait echouer l'apparition
        // (disciple invisible, 2026-11-13)
        if (!p.luo || bar == null || util == null || bar.getCapacity() < 1 || util.getCapacity() < 1) return;
        ItemStack s0 = bar.getItemStack((short) 0);
        LuoKatanas.Katana k = s0 == null || s0.isEmpty() ? null : LuoKatanas.of(s0.getItemId());
        if (k == null) return;
        if (p.type == GuardType.AVENTURIER && p.owner == null) {                              // role interactif : jamais en combat
            bar.setItemStackForSlot((short) 0, new ItemStack(k.id(), 1));
            util.setItemStackForSlot((short) 0, new ItemStack(k.sheath(), 1));
            inv.setActiveUtilitySlot(ref, (byte) 0, store);
            return;
        }
        if (bar.getCapacity() > 1) bar.setItemStackForSlot((short) 1, new ItemStack(k.id(), 1));
        util.setItemStackForSlot((short) 0, new ItemStack(k.sheathEmpty(), 1));
        inv.setActiveUtilitySlot(ref, (byte) 0, store);
    }

    /** Chaque seconde : au repos, rengainage (animation, puis lame et fourreau plein) ; en combat, lame au clair. */
    static void sheathTick(GuardManager.Post p, Ref<EntityStore> ref, NPCEntity npc, Store<EntityStore> store) {
        if (p.type == GuardType.DISCIPLE || (p.type == GuardType.AVENTURIER && p.owner == null)) return;   // kata au bokken ; recrutable
        Inventory inv = npc.getInventory();
        ItemContainer bar = inv != null ? inv.getHotbar() : null, util = inv != null ? inv.getUtility() : null;
        if (bar == null || util == null || bar.getCapacity() < 2 || util.getCapacity() < 1) return;
        ItemStack s0 = bar.getItemStack((short) 0);
        if (s0 == null || s0.isEmpty() || !LuoKatanas.isDrawn(s0.getItemId())) return;
        LuoKatanas.Katana k = LuoKatanas.of(s0.getItemId());
        long now = System.currentTimeMillis();
        byte active = inv.getActiveHotbarSlot();
        if (idle(ref, store)) {
            if (active != 0) { sheath(inv, ref, k, true, store); return; }                    // deja rengaine (ou ronde du patrouilleur)
            if (!p.fought) {                                                                   // retour au repos sans combat : sans animation
                inv.setActiveHotbarSlot(ref, (byte) 1, store);
                sheath(inv, ref, k, true, store);
                return;
            }
            if (p.sheatheAt == 0) {
                try { AnimationUtils.playAnimation(ref, AnimationSlot.Action, "Tenryumaru_Degainee", "Rengainer", true, store); }
                catch (Exception e) { EldaniorLogger.error("[Luo] rengainage", e); }
                p.sheatheAt = now + SHEATHE_MS;
            } else if (now >= p.sheatheAt) {
                inv.setActiveHotbarSlot(ref, (byte) 1, store);
                sheath(inv, ref, k, true, store);
                p.sheatheAt = 0;
                p.fought = false;
            }
            return;
        }
        p.sheatheAt = 0;
        p.fought = true;
        if (active != 0) inv.setActiveHotbarSlot(ref, (byte) 0, store);                        // combat : lame au clair
        sheath(inv, ref, k, false, store);
    }

    /** Fourreau plein (avec la poignee) ou vide dans l'emplacement 0 de la main gauche, s'il n'y est pas deja. */
    private static void sheath(Inventory inv, Ref<EntityStore> ref, LuoKatanas.Katana k, boolean full, Store<EntityStore> store) {
        String wanted = full ? k.sheath() : k.sheathEmpty();
        ItemStack cur = inv.getUtility().getItemStack((short) 0);
        if (cur == null || cur.isEmpty() || !wanted.equals(cur.getItemId()))
            inv.getUtility().setItemStackForSlot((short) 0, new ItemStack(wanted, 1));
        if (inv.getActiveUtilitySlot() != 0) inv.setActiveUtilitySlot(ref, (byte) 0, store);
    }

    /** Arme d'un aventurier, chasseur ou criminel sur le territoire Luo : katana de fer pour les guerriers ; null sinon. */
    static String weapon(GuardManager.Post p, AdventurerType adv) {
        return p.luo && adv == AdventurerType.GUERRIER ? IRON_KATANA : null;
    }

    static String givenName(GuardManager.Post p) {
        return GIVEN_NAMES[(int) Math.floorMod(p.skinSeed, (long) GIVEN_NAMES.length)];
    }

    /** Ce poste a-t-il des gestes (gardes du clan, pelerins) ? */
    static boolean animates(GuardManager.Post p) {
        return p.owner == null && (p.type == GuardType.VILLAGEOIS || p.type == GuardType.DISCIPLE || p.type == GuardType.MARCHAND
                || p.type == GuardType.AMBULANT || p.type == GuardType.COMMERCANT || p.luo && p.type.isGuard());
    }

    /** Chaque seconde, thread du monde : gestes d'ambiance. */
    static void tick(GuardManager.Post p, Ref<EntityStore> ref, Store<EntityStore> store) {
        long now = System.currentTimeMillis();
        if (p.luoBusyUntil > now || !idle(ref, store)) return;
        if (p.type == GuardType.DISCIPLE) {                                                     // series de kata a son poste
            if (DojoTraining.active(p) || now < p.luoNext) return;                             // au mannequin : DojoTraining frappe
            play(p, ref, KATA[p.luoStep % KATA.length], KATA_MS, store);
            if (++p.luoStep >= KATA_SERIES) { p.luoStep = 0; p.luoNext = now + KATA_REST_MS + RNG.nextInt(10_000); }
        } else if (p.type == GuardType.MARCHAND || p.type == GuardType.AMBULANT || p.type == GuardType.COMMERCANT) {                                              // appel des marchands
            if (RNG.nextInt(60) == 0) play(p, ref, "Appel", APPEL_MS, store);
        } else if (p.type == GuardType.VILLAGEOIS) {                                            // habitant : prie parfois
            if (RNG.nextInt(180) == 0) {
                p.pauseUntil = Math.max(p.pauseUntil, now + PRAY_MS);
                play(p, ref, "Priere", PRAY_MS, store);
            }
        } else if (p.type != GuardType.ASSASSIN && RNG.nextInt(90) == 0) {
            play(p, ref, "Salut", SALUTE_MS, store);
        }
    }

    /**
     * Le PNJ du clan s'arrete, se tourne vers eye et s'incline (profondement devant le Patriarche). False s'il ne le peut pas
     * (pas un PNJ du clan, en combat, recrute).
     */
    public static boolean bow(Ref<EntityStore> npc, Vector3d eye, String gesture, Store<EntityStore> store) {
        GuardManager.Post p = GuardManager.postOf(npc, store);
        if (p == null || !(p.luo || p.type == GuardType.MARCHAND) || p.owner != null || p.type == GuardType.CRIMINEL || !calm(npc, store)) return false;
        long ms = "InclinaisonProfonde".equals(gesture) ? DEEP_BOW_MS : "Salut".equals(gesture) ? SALUTE_MS : BOW_MS;
        p.luoStep = 0;                                                                         // interrompt une serie de kata
        p.pauseUntil = Math.max(p.pauseUntil, System.currentTimeMillis() + ms);
        NpcFacing.lookAt(npc, eye, System.currentTimeMillis() + ms);
        return play(p, npc, gesture, ms, store);
    }

    /** PNJ de poste du territoire Luo ? */
    public static boolean isLuo(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        GuardManager.Post p = GuardManager.postOf(ref, store);
        return p != null && (p.luo || p.type == GuardType.MARCHAND);
    }

    /** Garde du territoire Luo (garde, patrouilleur, chevalier, assassin, mage) : il attaque les etrangers sans plaque. */
    public static boolean isFighter(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        GuardManager.Post p = GuardManager.postOf(ref, store);
        return p != null && p.luo && p.type.isGuard();
    }

    /** Le garde voit target comme hostile pendant seconds et, s'il est au repos, accourt. */
    public static void attack(Ref<EntityStore> npc, Ref<EntityStore> target, double seconds, ComponentAccessor<EntityStore> store) {
        if (NpcAttitudes.hostile(npc, target, seconds, store)) GuardManager.call(npc, target, store);
    }

    // =====================================================================================================================

    /** Au repos ou en train de regarder un joueur (roles civils : etat Watching) ; pas en combat ni en discussion. */
    private static boolean calm(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        StateSupport st = StateSupport.get(ref, store);
        String state = st != null ? st.getStateName() : null;
        return state != null && (state.startsWith("Idle") || state.startsWith("Watching"));
    }

    private static boolean idle(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        StateSupport st = StateSupport.get(ref, store);
        String state = st != null ? st.getStateName() : null;
        return state != null && state.startsWith("Idle");
    }

    static boolean play(GuardManager.Post p, Ref<EntityStore> ref, String gesture, long ms, Store<EntityStore> store) {
        try {
            AnimationUtils.playAnimation(ref, AnimationSlot.Action, GESTURES, gesture, true, store);
            p.luoBusyUntil = System.currentTimeMillis() + ms;
            return true;
        } catch (Exception e) {
            EldaniorLogger.error("[Luo] geste " + gesture, e);
            return false;
        }
    }
}
