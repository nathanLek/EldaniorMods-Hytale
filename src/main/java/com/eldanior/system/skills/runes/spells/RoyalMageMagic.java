package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.guards.GuardType;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Magie du Mage royal (role Eldanior_Guard_Mage), en plus de sa boule de feu (attaque du role). En combat, toutes les
 * {@link #CAST_EVERY_MS} il lance le sort le plus utile :
 *   - Soin royal : un garde a moins de {@link #HEAL_RANGE} blocs sous 70 % de vie -> +{@link #HEAL_RATIO} de sa vie max ;
 *   - Nova de givre : un ennemi a moins de {@link #NOVA_RANGE} blocs -> degats et repousse tous les ennemis proches ;
 *   - Pluie de flammes : sur sa cible (a moins de {@link #CAST_RANGE} blocs) -> degats en zone autour d'elle.
 * Ennemis du mage : sa cible verrouillee et les monstres (hostiles aux joueurs) ; jamais les gardes ni les invocations.
 */
public class RoyalMageMagic extends EntityTickingSystem<EntityStore> {

    private static final long CAST_EVERY_MS = 6_000, CALM_RESET_MS = 5_000;
    private static final double CAST_RANGE = 20.0, FLAME_RADIUS = 3.5, NOVA_RANGE = 5.0, HEAL_RANGE = 12.0;
    private static final float FLAME_DAMAGE = 35f, NOVA_DAMAGE = 20f, HEAL_RATIO = 0.25f;
    private static final long FIRST_CAST_DELAY_MS = 2_500;                       // premier sort : apres la premiere boule de feu

    private static final Map<Ref<EntityStore>, Long> lastCast = new ConcurrentHashMap<>();
    private static final Map<Ref<EntityStore>, Long> lastFight = new ConcurrentHashMap<>();   // dernier instant en combat

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> mage = chunk.getReferenceTo(index);
        if (mage == null || !mage.isValid()) return;
        NPCEntity npc = store.getComponent(mage, NPCEntity.getComponentType());
        if (npc == null || !GuardType.MAGE_ROYAL.getRoleId().equals(npc.getRoleName())) return;
        long now = System.currentTimeMillis();
        Vector3d at = SpellFx.position(mage, store);
        if (at == null) return;
        // En combat : etat de combat (ou alerte / recherche) avec une cible verrouillee a portee. Les courts passages hors de
        // l'etat Combat ne relancent pas l'attente ; seul un vrai retour au calme (CALM_RESET_MS) la remet a zero.
        StateSupport st = StateSupport.get(mage, store);
        String state = st != null ? st.getStateName() : null;
        MarkedEntitySupport marks = MarkedEntitySupport.get(mage, store);
        Ref<EntityStore> target = marks != null ? marks.getMarkedEntityRef(MarkedEntitySupport.DEFAULT_TARGET_SLOT) : null;
        Vector3d tp = target != null && target.isValid() ? SpellFx.position(target, store) : null;
        boolean fighting = state != null && !state.startsWith("Idle") && !state.startsWith("ReturnHome") && tp != null && tp.distance(at) <= CAST_RANGE;
        if (!fighting) {
            Long lf = lastFight.get(mage);
            if (lf != null && now - lf > CALM_RESET_MS) { lastFight.remove(mage); lastCast.remove(mage); }
            return;
        }
        lastFight.put(mage, now);
        Long last = lastCast.get(mage);
        if (last == null) { lastCast.put(mage, now - CAST_EVERY_MS + FIRST_CAST_DELAY_MS); return; }   // entree en combat
        if (now - last < CAST_EVERY_MS) return;
        lastCast.put(mage, now);
        lastCast.keySet().removeIf(r -> !r.isValid());
        lastFight.keySet().removeIf(r -> !r.isValid());
        SpellFx.fx("Bifrost_Divine", new Vector3d(at).add(0, 2.2, 0), 0.6f, store, cb);   // eclat d'incantation au-dessus du mage
        if (heal(mage, at, store, cb)) { EldaniorLogger.info("[Mage royal] Soin royal"); return; }
        List<Ref<EntityStore>> close = enemiesAround(mage, at, NOVA_RANGE, store);
        if (!close.isEmpty()) { nova(mage, at, close, store, cb); EldaniorLogger.info("[Mage royal] Nova de givre (" + close.size() + ")"); return; }
        flames(mage, tp, store, cb);
        EldaniorLogger.info("[Mage royal] Pluie de flammes");
    }

    /** Soin royal : le garde le plus blesse a portee. */
    private static boolean heal(Ref<EntityStore> mage, Vector3d at, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> best = null;
        float bestRatio = 0.7f;
        int health = DefaultEntityStatTypes.getHealth();
        for (Ref<EntityStore> e : new ArrayList<>(TargetUtil.getAllEntitiesInSphere(at, HEAL_RANGE, store))) {
            NPCEntity n = store.getComponent(e, NPCEntity.getComponentType());
            GuardType t = n != null ? GuardType.fromRole(n.getRoleName()) : null;
            if (t == null || !t.isGuard()) continue;
            EntityStatMap stats = store.getComponent(e, EntityStatsModule.get().getEntityStatMapComponentType());
            EntityStatValue hp = stats != null ? stats.get(health) : null;
            if (hp == null || hp.getMax() <= 0) continue;
            float ratio = hp.get() / hp.getMax();
            if (ratio < bestRatio) { bestRatio = ratio; best = e; }
        }
        if (best == null) return false;
        EntityStatMap stats = store.getComponent(best, EntityStatsModule.get().getEntityStatMapComponentType());
        EntityStatValue hp = stats.get(health);
        stats.addStatValue(health, hp.getMax() * HEAL_RATIO);
        Vector3d bp = SpellFx.position(best, store);
        if (bp != null) {
            SpellFx.fx("Apollo_Heal_Pulse", new Vector3d(bp).add(0, 1, 0), 1.4f, store, cb);
            SpellFx.sound("SFX_Deployable_Totem_Heal_Spawn", bp, cb);
        }
        return true;
    }

    /** Nova de givre : degats et repousse tous les ennemis au contact du mage. */
    private static void nova(Ref<EntityStore> mage, Vector3d at, List<Ref<EntityStore>> close, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        SpellFx.fx("Niflheim_Freeze_Burst", new Vector3d(at).add(0, 1, 0), 1.6f, store, cb);
        SpellFx.fx("Niflheim_Ring", new Vector3d(at).add(0, 0.2, 0), 0.6f, store, cb);
        SpellFx.sound("SFX_Golem_Frost_Slam_Impact", at, cb);
        for (Ref<EntityStore> e : close) {
            Vector3d ep = SpellFx.position(e, store);
            if (ep == null) continue;
            Vector3d push = new Vector3d(ep).sub(at);
            push.y = 0;
            if (push.lengthSquared() > 1e-4) push.normalize().mul(8).add(0, 4, 0);
            SpellFx.damage(mage, e, "Physical", NOVA_DAMAGE, push, null, cb);
        }
    }

    /** Pluie de flammes : explosion de feu sur la cible, degats a tous les ennemis autour d'elle. */
    private static void flames(Ref<EntityStore> mage, Vector3d tp, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        SpellFx.fx("Muspelheim_Fire_Pillar", new Vector3d(tp), 0.35f, store, cb);
        SpellFx.fx("Explosion_Medium", new Vector3d(tp).add(0, 0.5, 0), 1.4f, store, cb);
        SpellFx.fx("Impact_Fire", new Vector3d(tp).add(0, 0.2, 0), 2.0f, store, cb);
        SpellFx.sound("SFX_Staff_Flame_Fireball_Impact", tp, cb);
        for (Ref<EntityStore> e : enemiesAround(mage, tp, FLAME_RADIUS, store))
            SpellFx.damage(mage, e, "Physical", FLAME_DAMAGE, null, null, cb);
    }

    /** Ennemis du mage autour d'un point : sa cible verrouillee et les monstres ; ni gardes, ni PNJ de poste, ni invocations. */
    private static List<Ref<EntityStore>> enemiesAround(Ref<EntityStore> mage, Vector3d center, double radius, Store<EntityStore> store) {
        MarkedEntitySupport marks = MarkedEntitySupport.get(mage, store);
        Ref<EntityStore> locked = marks != null ? marks.getMarkedEntityRef(MarkedEntitySupport.DEFAULT_TARGET_SLOT) : null;
        List<Ref<EntityStore>> out = new ArrayList<>();
        for (Ref<EntityStore> e : new ArrayList<>(TargetUtil.getAllEntitiesInSphere(center, radius, store))) {
            if (e == null || !e.isValid() || e.equals(mage) || RuneSpellRuntime.isSummon(e)) continue;
            if (e.equals(locked)) { out.add(e); continue; }
            NPCEntity n = store.getComponent(e, NPCEntity.getComponentType());
            if (n == null || GuardType.isAlly(n.getRoleName())) continue;                       // criminels : ennemis
            WorldSupport ws = WorldSupport.get(e, store);
            if (GuardType.isOutlaw(n.getRoleName()) || (ws != null && ws.getDefaultPlayerAttitude() == Attitude.HOSTILE)) out.add(e);
        }
        return out;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}
