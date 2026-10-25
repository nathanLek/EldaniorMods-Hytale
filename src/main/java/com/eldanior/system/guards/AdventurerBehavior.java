package com.eldanior.system.guards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.configs.Mobs.MobLevelData;
import com.eldanior.system.skills.runes.spells.AdventurerMagic;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.eldanior.system.util.NpcAttitudes;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import org.joml.Vector3d;

import java.util.ArrayList;

/**
 * Comportements « de joueur » d'un aventurier recrute, chaque seconde (AdventurerSystem.follow) :
 *   - repli + potion : la premiere fois sous 30 % de vie, il quitte le combat, rejoint son joueur, boit sa potion (+40 % de
 *     sa vie max) et repart. Une seule potion par mission : ensuite il se bat jusqu'au bout ;
 *   - butin : hors combat, il ramasse les objets au sol autour de lui et les donne a son joueur ;
 *   - paroles : debut de mission, blessure, potion, ennemi redoutable, fin de contrat, chute.
 */
public final class AdventurerBehavior {

    private AdventurerBehavior() {}

    private static final float RETREAT_BELOW = 0.30f, POTION_HEAL = 0.40f;
    private static final long RETREAT_MAX_MS = 6_000, TALK_COOLDOWN_MS = 20_000;
    private static final double LOOT_RANGE = 6.0, NEAR_OWNER = 4.0;
    private static final int STRONG_ENEMY_GAP = 30;                                  // niveaux de plus que lui

    // ===== Paroles =====

    /** Nom de l'aventurier dans ses messages : sa classe, sinon son type. */
    static String speaker(GuardManager.Post p) {
        var cls = AdventurerTier.classOf(p);
        return cls != null ? cls.getDisplayName() : "Aventurier " + AdventurerType.of(p.skinSeed).getLabel();
    }

    /** Message a son joueur ; force : ignore l'anti-spam. */
    static void talk(GuardManager.Post p, String text, boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - p.lastTalk < TALK_COOLDOWN_MS) return;
        p.lastTalk = now;
        PlayerRef pr = p.owner != null ? Universe.get().getPlayer(p.owner) : null;
        if (pr != null) pr.sendMessage(Message.raw("[" + speaker(p) + "] « " + text + " »"));
    }

    /** Debut de mission : potion pleine, et il se presente. */
    static void onRecruited(GuardManager.Post p) {
        p.potionUsed = false;
        p.retreatUntil = 0;
        p.lastTalk = 0;
        talk(p, switch (AdventurerType.of(p.skinSeed)) {
            case GUERRIER -> "Mon épée est à vous. Restez derrière moi !";
            case MAGE -> "Mes runes sont prêtes. Restez à distance de mes sorts !";
            case ARCHER -> "Je couvre vos arrières, à distance.";
            case ASSASSIN -> "Je frapperai depuis l'ombre.";
        }, true);
    }

    /** Ennemi bien plus fort que lui : il previent son joueur. */
    static void warn(GuardManager.Post p, Ref<EntityStore> enemy, Store<EntityStore> store) {
        MobLevelData d = enemy != null && enemy.isValid() ? store.getComponent(enemy, EldaniorSystem.get().getMobLevelDataType()) : null;
        if (d != null && d.getLevel() >= p.level + STRONG_ENEMY_GAP)
            talk(p, "Attention, cet ennemi est redoutable (niveau " + d.getLevel() + ") !", false);
    }

    // ===== Repli et potion =====

    private static final long DRINK_MS = 1_500;
    private static final double CALM_RANGE = 24.0;

    static boolean isRetreating(GuardManager.Post p) { return p.retreatUntil > 0 || p.drinkAt > 0; }

    /** Chaque seconde. Renvoie true s'il est en repli ou boit (pas de combat ni de ramassage pendant ce temps). */
    static boolean tick(GuardManager.Post p, Ref<EntityStore> ref, Vector3d at, Vector3d ownerPos, Ref<EntityStore> owner,
                        Store<EntityStore> store) {
        try {
            long now = System.currentTimeMillis();
            if (p.drinkAt > 0) {                                                             // il boit sa potion
                disengage(ref, at, store);
                if (now >= p.drinkAt) { finishDrink(p, ref, at, store); p.drinkAt = 0; p.retreatUntil = 0; }
                return true;
            }
            if (p.retreatUntil > 0) {                                                        // repli vers son joueur
                disengage(ref, at, store);
                if (at.distance(ownerPos) <= NEAR_OWNER || now >= p.retreatUntil) { startDrink(p, ref, at, store); p.drinkAt = now + DRINK_MS; }
                return true;
            }
            if (!p.potionUsed && hpRatio(ref, store) < RETREAT_BELOW) {                       // premiere grosse blessure
                p.potionUsed = true;
                p.retreatUntil = now + RETREAT_MAX_MS;
                disengage(ref, at, store);
                talk(p, "Je suis blessé, je me replie !", true);
                return true;
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Aventurier] comportement", e);
        }
        return false;
    }

    /**
     * Il quitte le combat : il lache sa cible, repasse au calme et ignore les monstres proches pendant 3 s (renouvele chaque
     * seconde du repli). Sans ca, son groupe d'attitude (Eldanior_Guard, hostile aux monstres) le relance aussitot au combat.
     */
    private static void disengage(Ref<EntityStore> ref, Vector3d at, Store<EntityStore> store) {
        try {
            MarkedEntitySupport marks = MarkedEntitySupport.get(ref, store);
            if (marks != null) marks.setMarkedEntity(MarkedEntitySupport.DEFAULT_TARGET_SLOT, null);
            StateSupport st = StateSupport.get(ref, store);
            if (st != null && st.getStateName() != null && !st.getStateName().startsWith("Idle")) st.setState(ref, "Idle", null, store);
            for (Ref<EntityStore> e : new ArrayList<>(TargetUtil.getAllEntitiesInSphere(at, CALM_RANGE, store))) {
                NPCEntity n = e != null && e.isValid() && !e.equals(ref) ? store.getComponent(e, NPCEntity.getComponentType()) : null;
                if (n == null || GuardType.fromRole(n.getRoleName()) != null) continue;
                NpcAttitudes.force(ref, e, Attitude.IGNORE, 3.0, store);
            }
        } catch (Exception ignored) { }
    }

    /** Il sort sa potion et la boit (le soin arrive a la fin : finishDrink). */
    private static void startDrink(GuardManager.Post p, Ref<EntityStore> ref, Vector3d at, Store<EntityStore> store) {
        hold(ref, "Potion_Health", store);
        AdventurerMagic.drinkSound(at, store);
        talk(p, "*boit une potion de soin*", true);
    }

    /** Fin de la potion : +40 % de sa vie max (le plafond de regeneration en tient compte), il reprend son arme. */
    private static void finishDrink(GuardManager.Post p, Ref<EntityStore> ref, Vector3d at, Store<EntityStore> store) {
        EntityStatMap stats = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        int health = DefaultEntityStatTypes.getHealth();
        var hp = stats != null ? stats.get(health) : null;
        if (hp != null) {
            float value = Math.min(hp.getMax(), hp.get() + hp.getMax() * POTION_HEAL);
            stats.setStatValue(health, value);
            p.lastHp = value;
        }
        AdventurerMagic.potionFx(at, store);
        hold(ref, AdventurerType.of(p.skinSeed).getWeapon(p), store);
        talk(p, "Ça va mieux. Je reprends le combat !", true);
    }

    /** Met un objet dans sa main (emplacement 0 de sa barre). */
    private static void hold(Ref<EntityStore> ref, String itemId, Store<EntityStore> store) {
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        var inv = npc != null ? npc.getInventory() : null;
        if (inv == null || inv.getHotbar() == null) return;
        inv.getHotbar().setItemStackForSlot((short) 0, new ItemStack(itemId, 1));
        inv.setActiveHotbarSlot(ref, (byte) 0, store);
    }

    /** Potion de soin : +40 % de sa vie max (le plafond de regeneration du compagnon en tient compte). */
    private static void drink(GuardManager.Post p, Ref<EntityStore> ref, Vector3d at, Store<EntityStore> store) {
        EntityStatMap stats = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        int health = DefaultEntityStatTypes.getHealth();
        var hp = stats != null ? stats.get(health) : null;
        if (hp == null) return;
        float value = Math.min(hp.getMax(), hp.get() + hp.getMax() * POTION_HEAL);
        stats.setStatValue(health, value);
        p.lastHp = value;
        AdventurerMagic.potionFx(at, store);
        talk(p, "*boit une potion de soin* Je reprends le combat !", true);
    }

    // ===== Butin =====

    // ===== Outils =====

    /** Hors combat : il n'a plus de cible. */
    static boolean calm(Ref<EntityStore> ref, Store<EntityStore> store) {
        MarkedEntitySupport marks = MarkedEntitySupport.get(ref, store);
        Ref<EntityStore> target = marks.getMarkedEntityRef(MarkedEntitySupport.DEFAULT_TARGET_SLOT);
        return target == null || !target.isValid();
    }

    private static float hpRatio(Ref<EntityStore> ref, Store<EntityStore> store) {
        EntityStatMap stats = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        var hp = stats != null ? stats.get(DefaultEntityStatTypes.getHealth()) : null;
        return hp != null && hp.getMax() > 0 ? hp.get() / hp.getMax() : 1f;
    }
}