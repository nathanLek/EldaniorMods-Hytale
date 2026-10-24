package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.guards.AdventurerType;
import com.eldanior.system.guards.GuardManager;
import com.eldanior.system.guards.GuardType;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import com.eldanior.system.guards.AdventurerTier;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.eldanior.system.gui.tabs.ProfilTab.getAdventureRank;

/**
 * Sorts des aventuriers recrutes, en plus de l'attaque de leur role (sur le modele de RoyalMageMagic). En combat, a
 * intervalle regulier (12 s au rang E, 7 s au rang S), le sort de leur type :
 *   - guerrier : onde de choc (2 ennemis ou plus au contact), sinon frappe heroique sur sa cible ;
 *   - mage : soin de son joueur (sous 60 % de vie), sinon nova de givre (ennemis au contact), sinon pluie de flammes ;
 *   - archer : pluie de fleches autour de sa cible ;
 *   - assassin : frappe de l'ombre (il passe derriere sa cible et frappe).
 * Les degats passent par CombatStatsSystem : passifs, classe et statut de l'aventurier les renforcent. Ennemis : sa cible
 * verrouillee et les monstres ; jamais son joueur, son groupe, les gardes ni les invocations.
 */
public class AdventurerMagic extends EntityTickingSystem<EntityStore> {

    private static final long CALM_RESET_MS = 5_000, FIRST_CAST_DELAY_MS = 3_000;
    private static final double CAST_RANGE = 20.0, CLOSE_RANGE = 5.0, AREA_RADIUS = 4.0, HEAL_RANGE = 12.0;

    private static final Map<Ref<EntityStore>, Long> lastCast = new ConcurrentHashMap<>();
    private static final Map<Ref<EntityStore>, Long> lastFight = new ConcurrentHashMap<>();
    private static final Map<Ref<EntityStore>, Long> lastAura = new ConcurrentHashMap<>();

    /** Noms des sorts d'un type (fenetre de recrutement). */
    public static String spells(AdventurerType type) {
        return switch (type) {
            case GUERRIER -> "Onde de choc, Frappe héroïque";
            case MAGE -> "Soin, Nova de givre, Pluie de flammes";
            case ARCHER -> "Pluie de flèches";
            case ASSASSIN -> "Frappe de l'ombre";
        };
    }

    /** Effet de la potion de soin d'un aventurier (AdventurerBehavior). */
    public static void potionFx(Vector3d at, Store<EntityStore> store) {
        SpellFx.fx("Apollo_Heal_Pulse", new Vector3d(at).add(0, 1, 0), 1.0f, store, store);
        SpellFx.sound("SFX_Deployable_Totem_Heal_Spawn", at, store);
    }

    /** Son de la potion que boit un aventurier. */
    public static void drinkSound(Vector3d at, Store<EntityStore> store) {
        SpellFx.sound("SFX_Health_Potion_High_Drink", at, store);
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> me = chunk.getReferenceTo(index);
        if (!me.isValid()) return;
        NPCEntity npc = store.getComponent(me, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npc == null || GuardType.fromRole(npc.getRoleName()) != GuardType.AVENTURIER) return;
        GuardManager.Post post = GuardManager.adventurerPost(me, store);
        if (post == null) return;
        aura(me, post, store, cb);                                                                  // libre ou recrute
        if (!GuardType.isCompanionRole(npc.getRoleName()) || post.getOwner() == null) return;     // sorts : recrutes seulement
        long now = System.currentTimeMillis();
        Vector3d at = SpellFx.position(me, store);
        if (at == null) return;
        StateSupport st = StateSupport.get(me, store);
        String state = st.getStateName();
        MarkedEntitySupport marks = MarkedEntitySupport.get(me, store);
        Ref<EntityStore> target = marks.getMarkedEntityRef(MarkedEntitySupport.DEFAULT_TARGET_SLOT);
        Vector3d tp = target != null && target.isValid() ? SpellFx.position(target, store) : null;
        boolean fighting = !state.startsWith("Idle") && !state.startsWith("ReturnHome") && tp != null && tp.distance(at) <= CAST_RANGE;
        if (!fighting) {
            Long lf = lastFight.get(me);
            if (lf != null && now - lf > CALM_RESET_MS) { lastFight.remove(me); lastCast.remove(me); }
            return;
        }
        lastFight.put(me, now);
        int rank = Math.max(0, "EDCBAS".indexOf(getAdventureRank(post.getLevel())));
        long every = 12_000 - rank * 1_000L;
        Long last = lastCast.get(me);
        if (last == null) { lastCast.put(me, now - every + FIRST_CAST_DELAY_MS); return; }      // entree en combat
        if (now - last < every) return;
        lastCast.put(me, now);
        lastCast.keySet().removeIf(r -> !r.isValid());
        lastFight.keySet().removeIf(r -> !r.isValid());
        float base = 15f + post.getLevel() * 0.06f;                                               // 16 au niveau 20, 51 au niveau 600
        SpellFx.fx("Bifrost_Divine", new Vector3d(at).add(0, 2.2, 0), 0.5f, store, cb);       // eclat d'incantation
        try {
            switch (AdventurerType.of(post.getSkinSeed())) {
                case GUERRIER -> warrior(me, at, target, tp, base, store, cb);
                case MAGE -> mage(me, post, at, tp, base, store, cb);
                case ARCHER -> arrows(me, tp, base, store, cb);
                case ASSASSIN -> shadow(me, at, target, tp, base, store, cb);
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Aventurier] sort", e);
        }
    }

    // ===== Guerrier =====
    private static void warrior(Ref<EntityStore> me, Vector3d at, Ref<EntityStore> target, Vector3d tp, float base,
                                Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<Ref<EntityStore>> close = enemiesAround(me, at, CLOSE_RANGE, store);
        if (close.size() >= 2) {                                                                    // onde de choc
            SpellFx.fx("Chaos_Shockwave", new Vector3d(at).add(0, 0.3, 0), 0.8f, store, cb);
            SpellFx.sound("SFX_Golem_Frost_Slam_Impact", at, cb);
            for (Ref<EntityStore> e : close) SpellFx.damage(me, e, "Physical", base, pushAway(at, e, store), null, cb);
            return;
        }
        SpellFx.fx("Gilgamesh_Sword_Burst", new Vector3d(tp).add(0, 1, 0), 0.8f, store, cb);      // frappe heroique
        SpellFx.sound("SFX_Ability_Wind_Strike_Slash", tp, cb);
        SpellFx.damage(me, target, "Physical", base * 1.5f, null, null, cb);
    }

    // ===== Mage =====
    private static void mage(Ref<EntityStore> me, GuardManager.Post post, Vector3d at, Vector3d tp, float base,
                             Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (healOwner(post, at, store, cb)) return;
        List<Ref<EntityStore>> close = enemiesAround(me, at, CLOSE_RANGE, store);
        if (!close.isEmpty()) {                                                                     // nova de givre
            SpellFx.fx("Niflheim_Freeze_Burst", new Vector3d(at).add(0, 1, 0), 1.4f, store, cb);
            SpellFx.fx("Niflheim_Ring", new Vector3d(at).add(0, 0.2, 0), 0.5f, store, cb);
            SpellFx.sound("SFX_Golem_Frost_Slam_Impact", at, cb);
            for (Ref<EntityStore> e : close) SpellFx.damage(me, e, "Physical", base * 0.8f, pushAway(at, e, store), null, cb);
            return;
        }
        SpellFx.fx("Muspelheim_Fire_Pillar", new Vector3d(tp), 0.3f, store, cb);                  // pluie de flammes
        SpellFx.fx("Impact_Fire", new Vector3d(tp).add(0, 0.2, 0), 1.6f, store, cb);
        SpellFx.sound("SFX_Staff_Flame_Fireball_Impact", tp, cb);
        for (Ref<EntityStore> e : enemiesAround(me, tp, AREA_RADIUS, store)) SpellFx.damage(me, e, "Physical", base, null, null, cb);
    }

    /** Soin du joueur de l'aventurier : a portee et sous 60 % de vie -> +20 % de sa vie max. */
    private static boolean healOwner(GuardManager.Post post, Vector3d at, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        UUID owner = post.getOwner();
        PlayerRef pr = owner != null ? Universe.get().getPlayer(owner) : null;
        Ref<EntityStore> o = pr != null ? pr.getReference() : null;
        if (o == null || !o.isValid() || o.getStore() != store) return false;
        Vector3d op = SpellFx.position(o, store);
        if (op == null || op.distance(at) > HEAL_RANGE) return false;
        int health = DefaultEntityStatTypes.getHealth();
        EntityStatMap stats = store.getComponent(o, EntityStatsModule.get().getEntityStatMapComponentType());
        EntityStatValue hp = stats != null ? stats.get(health) : null;
        if (hp == null || hp.getMax() <= 0 || hp.get() / hp.getMax() >= 0.6f) return false;
        stats.addStatValue(health, hp.getMax() * 0.2f);
        SpellFx.fx("Apollo_Heal_Pulse", new Vector3d(op).add(0, 1, 0), 1.2f, store, cb);
        SpellFx.sound("SFX_Deployable_Totem_Heal_Spawn", op, cb);
        return true;
    }

    // ===== Archer =====
    private static void arrows(Ref<EntityStore> me, Vector3d tp, float base, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        SpellFx.fx("Apollo_Light_Rain", new Vector3d(tp), 0.6f, store, cb);
        SpellFx.sound("SFX_Ability_Charged_Shot_Impact", tp, cb);
        for (Ref<EntityStore> e : enemiesAround(me, tp, AREA_RADIUS, store)) SpellFx.damage(me, e, "Physical", base, null, null, cb);
    }

    // ===== Assassin =====
    private static void shadow(Ref<EntityStore> me, Vector3d at, Ref<EntityStore> target, Vector3d tp, float base,
                               Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d dir = new Vector3d(tp).sub(at);
        dir.y = 0;
        if (dir.lengthSquared() > 1e-4) dir.normalize();
        Vector3d behind = new Vector3d(tp).add(dir.x * 1.3, 0, dir.z * 1.3);                       // de l'autre cote de sa cible
        SpellFx.fx("Nyx_Mist", new Vector3d(at).add(0, 1, 0), 0.6f, store, cb);
        SpellFx.teleport(me, behind, store, cb);
        SpellFx.fx("Critical_Hit_Blood", new Vector3d(tp).add(0, 1, 0), 1.0f, store, cb);
        SpellFx.sound("SFX_Daggers_T2_Slash_Impact", tp, cb);
        SpellFx.damage(me, target, "Physical", base * 2f, null, null, cb);
    }

    // ===== Outils =====

    /**
     * Aura des exceptionnels et des heros : effet attache a l'entite (particules sur son torse, 8 s), reapplique toutes les
     * 5 s. Il suit ses deplacements et disparait avec lui (ne pas poser de particules dans le monde : elles y restent).
     */
    private static void aura(Ref<EntityStore> me, GuardManager.Post post, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        AdventurerTier tier = AdventurerTier.of(post);
        if (tier == AdventurerTier.ORDINAIRE) return;
        long now = System.currentTimeMillis();
        Long last = lastAura.get(me);
        if (last != null && now - last < 5_000) return;
        lastAura.put(me, now);
        if (lastAura.size() > 64) lastAura.keySet().removeIf(r -> !r.isValid());
        com.eldanior.system.config.Effects.EffectsManager.applyEffect(me,
                tier == AdventurerTier.HEROS ? "Ability_Aura_Visual_6" : "Ability_Aura_Visual_5", cb);
    }

    private static Vector3d pushAway(Vector3d from, Ref<EntityStore> e, Store<EntityStore> store) {
        Vector3d ep = SpellFx.position(e, store);
        if (ep == null) return null;
        Vector3d push = new Vector3d(ep).sub(from);
        push.y = 0;
        if (push.lengthSquared() > 1e-4) push.normalize().mul(7).add(0, 3.5, 0);
        return push;
    }

    /** Ennemis autour d'un point : sa cible verrouillee et les monstres ; ni joueurs, ni allies, ni gardes, ni invocations. */
    private static List<Ref<EntityStore>> enemiesAround(Ref<EntityStore> me, Vector3d center, double radius, Store<EntityStore> store) {
        MarkedEntitySupport marks = MarkedEntitySupport.get(me, store);
        Ref<EntityStore> locked = marks.getMarkedEntityRef(MarkedEntitySupport.DEFAULT_TARGET_SLOT);
        List<Ref<EntityStore>> out = new ArrayList<>();
        for (Ref<EntityStore> e : new ArrayList<>(TargetUtil.getAllEntitiesInSphere(center, radius, store))) {
            if (e == null || !e.isValid() || e.equals(me) || RuneSpellRuntime.isSummon(e)) continue;
            if (GuardManager.blocksFriendlyFire(me, e, store)) continue;                            // son joueur, son groupe
            if (e.equals(locked)) { out.add(e); continue; }
            if (store.getComponent(e, Player.getComponentType()) != null) continue;
            NPCEntity n = store.getComponent(e, Objects.requireNonNull(NPCEntity.getComponentType()));
            if (n == null || GuardType.fromRole(n.getRoleName()) != null) continue;
            WorldSupport ws = WorldSupport.get(e, store);
            if (ws.getDefaultPlayerAttitude() == Attitude.HOSTILE) out.add(e);
        }
        return out;
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}