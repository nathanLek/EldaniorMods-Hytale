package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.guards.GuardManager;
import com.eldanior.system.guards.GuardType;
import com.eldanior.system.guards.MartialRanks;
import com.eldanior.system.luo.LuoClans;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Combat martial des PNJ de poste (decision du 2026-11-10, docs/plan-clan-luo.md, C) : gardes, patrouilleurs, chevaliers,
 * chevaliers errants, assassins et disciples ont un rang martial (guards.MartialRanks) et se battent avec les competences de
 * leur rang et les techniques de leur clan :
 *   Apprenti et plus     aura de Ki du palier pendant le combat (effet Ki_Visual_<palier>) ;
 *   Disciple et plus     Renforcement corporel permanent : degats subis reduits comme la rune (DivineDamageSystem -> hurtFactor) ;
 *   Expert et plus       Pas leger : ruee vers sa cible quand elle est a 5-14 blocs (toutes les 8 s) ;
 *   Grand Maitre et plus Projection d'epee d'Aura (toutes les 45 s) ;
 *   Saint et plus        lame de Ki vers sa cible a 4-14 blocs (toutes les 2,5 s) ; rafale du combo au corps a corps (12 s) ;
 *   Maitre Supreme       Incarnation de l'Ame (toutes les 90 s) ;
 *   Dieu Martial         Domaine Divin (toutes les 60 s) ;
 *   membre d'un clan     ses techniques de clan dont il a le niveau (LuoClans.TECHNIQUES : Paume de Ki, Frappe des Meridiens,
 *                        Croissant de Ki, Domaine de l'Epee, Fleur de Prunier, Unite du Ciel et de la Terre ; Garde du Fleuve et
 *                        Transformation Hwakyung, liees au joueur, sont exclues), une toutes les 15 s.
 * Les sorts du moteur RuneSpell sont lances par spell.start et avances ici (comme AdventurerMagic) ; leur palier vient de
 * MartialKi.NPC_TIER, leurs cibles de AdventurerMagic.isFoe (SpellFx.isEnemy) : jamais les joueurs, sauf sa cible verrouillee.
 * Combattants enroles (enlist) : PNJ sans poste de combat qui se battent avec un rang donne, comme le combattant du Sensei
 * (duel au Bokken, riposte au katana de fer ; SenseiDuel), quel que soit leur role.
 * Les familles n'ont pas encore de rune (« a venir ») : rien a lancer pour elles.
 */
public class MartialNpcs extends EntityTickingSystem<EntityStore> {

    // ===== Reglages =====
    private static final long THINK_MS = 400, POST_CACHE_MS = 5_000, AURA_MS = 2_000, CALM_MS = 5_000;
    private static final long DASH_CD = 8_000, CLAN_CD = 15_000, PROJECTION_CD = 45_000, DOMAIN_CD = 60_000, FIRST_DELAY_MS = 2_500;
    private static final long BLADE_CD = 2_500, RAFALE_CD = 12_000, INCARNATION_CD = 90_000;
    private static final double FIGHT_RANGE = 25.0, DASH_MIN = 5.0, DASH_MAX = 14.0, DASH_SPEED = 20.0;
    /** Reduction des degats subis par palier (Renforcement corporel). */
    private static final float[] REDUCTION = {0f, 0.10f, 0.12f, 0.15f, 0.18f, 0.22f, 0.25f, 0.30f};
    private static final Set<String> NPC_UNSAFE = Set.of("Rune_GardeFleuve", "Rune_TransformationHwakyung");

    private record Brain(GuardManager.Post post, int tier, int level, LuoClans.Clan clan, long checkedAt) {}

    private static final Map<Ref<EntityStore>, Brain> brains = new ConcurrentHashMap<>();
    /** Combattants enroles (combattant du Sensei...) : rang donne par le code, pas par un poste. */
    private static final Map<Ref<EntityStore>, Brain> enlisted = new ConcurrentHashMap<>();
    /** Lames de Ki lancees par les PNJ (Saint et plus), avancees a chaque passage. */
    private static final Map<Ref<EntityStore>, List<RuneSpell.Instance>> blades = new ConcurrentHashMap<>();

    /** Enrole ce PNJ comme combattant martial de ce niveau (et de ce clan, null sinon) jusqu'a sa disparition. */
    public static void enlist(Ref<EntityStore> npc, int level, LuoClans.Clan clan) {
        if (npc == null) return;
        int tier = MartialRanks.tier(level);
        if (tier <= 0) return;
        enlisted.keySet().removeIf(r -> !r.isValid());
        enlisted.put(npc, new Brain(null, tier, level, clan, Long.MAX_VALUE));
        MartialKi.NPC_TIER.put(npc, tier);
    }
    private static final Map<Ref<EntityStore>, Long> lastThink = new ConcurrentHashMap<>();
    private static final Map<Ref<EntityStore>, Long> lastAura = new ConcurrentHashMap<>();
    private static final Map<Ref<EntityStore>, Long> fightSince = new ConcurrentHashMap<>();
    private static final Map<Ref<EntityStore>, Long> lastFight = new ConcurrentHashMap<>();
    private static final Map<Ref<EntityStore>, Map<String, Long>> ready = new ConcurrentHashMap<>();
    private static final Map<Ref<EntityStore>, List<RuneSpell.Instance>> instances = new ConcurrentHashMap<>();

    /** Facteur des degats subis par un PNJ martial (Renforcement corporel de son palier), 1 sinon. */
    public static float hurtFactor(Ref<EntityStore> target) {
        Integer tier = target != null ? MartialKi.NPC_TIER.get(target) : null;
        return tier == null ? 1f : 1f - REDUCTION[Math.clamp(tier, 1, 8) - 1];
    }

    /** true si ce lanceur est un PNJ martial (ses sorts visent comme ceux d'un aventurier). */
    static boolean isMartialCaster(Ref<EntityStore> ref) {
        return ref != null && MartialKi.NPC_TIER.containsKey(ref);
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> me = chunk.getReferenceTo(index);
        if (!me.isValid()) return;
        tickInstances(me, store, cb);
        long now = System.currentTimeMillis();
        if (now - lastOrphanSweep > ORPHAN_SWEEP_MS) { lastOrphanSweep = now; releaseOrphans(cb); }
        Long lt = lastThink.get(me);
        if (lt != null && now - lt < THINK_MS) return;
        Brain b = enlisted.get(me);
        if (b == null) {
            NPCEntity npc = store.getComponent(me, Objects.requireNonNull(NPCEntity.getComponentType()));
            if (npc == null || !MartialRanks.applies(GuardType.fromRole(npc.getRoleName()))) return;
            b = brain(me, now, store);
        }
        lastThink.put(me, now);
        if (b == null) return;
        try {
            think(me, b, now, store, cb);
        } catch (Exception e) {
            EldaniorLogger.error("[MartialNpcs] combat", e);
        }
    }

    /** Poste, palier et clan du PNJ, relus toutes les 5 s (reglage du poste change, nouveau PNJ). */
    private static Brain brain(Ref<EntityStore> me, long now, Store<EntityStore> store) {
        Brain b = brains.get(me);
        if (b != null && now - b.checkedAt() < POST_CACHE_MS) return b.tier() > 0 ? b : null;
        if (brains.size() > 256) sweep();
        GuardManager.Post p = MartialRanks.martialPost(me, store);
        int level = MartialRanks.level(p);
        b = new Brain(p, p != null ? MartialRanks.tier(level) : 0, level, MartialRanks.clan(p), now);
        brains.put(me, b);
        if (b.tier() > 0) MartialKi.NPC_TIER.put(me, b.tier()); else MartialKi.NPC_TIER.remove(me);
        return b.tier() > 0 ? b : null;
    }

    private static void think(Ref<EntityStore> me, Brain b, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d at = SpellFx.position(me, store);
        if (at == null) return;
        String state = StateSupport.get(me, store).getStateName();
        Ref<EntityStore> target = MarkedEntitySupport.get(me, store).getMarkedEntityRef(MarkedEntitySupport.DEFAULT_TARGET_SLOT);
        Vector3d tp = target != null && target.isValid() ? SpellFx.position(target, store) : null;
        boolean fighting = state != null && !state.startsWith("Idle") && !state.startsWith("ReturnHome") && tp != null
                && tp.distance(at) <= FIGHT_RANGE;
        if (!fighting) {
            Long lf = lastFight.get(me);
            if (lf != null && now - lf > CALM_MS) { lastFight.remove(me); fightSince.remove(me); }
            return;
        }
        lastFight.put(me, now);
        long since = fightSince.computeIfAbsent(me, k -> now);
        Long la = lastAura.get(me);
        if (la == null || now - la >= AURA_MS) {                               // aura de Ki du palier
            lastAura.put(me, now);
            SpellFx.effect(me, KiEpee.VISUAL + b.tier(), cb);
        }
        if (now - since < FIRST_DELAY_MS) return;                              // il jauge d'abord son adversaire
        double dist = tp.distance(at);
        Map<String, Long> cds = ready.computeIfAbsent(me, k -> new ConcurrentHashMap<>());
        if (b.tier() >= 8 && free(cds, "DomaineDivin", now) && !SpellFx.enemiesAround(me, at, 15, store).isEmpty()) {
            if (cast(me, "DomaineDivin", tp, store, cb)) { cds.put("DomaineDivin", now + DOMAIN_CD); return; }
        }
        if (b.tier() >= 7 && free(cds, "IncarnationAme", now) && dist <= 10) {    // Maitre Supreme : l'ame combat a ses cotes
            if (cast(me, "IncarnationAme", tp, store, cb)) { cds.put("IncarnationAme", now + INCARNATION_CD); return; }
        }
        if (b.tier() >= 5 && free(cds, "ProjectionEpeeAura", now) && dist <= 12) {
            if (cast(me, "ProjectionEpeeAura", tp, store, cb)) { cds.put("ProjectionEpeeAura", now + PROJECTION_CD); return; }
        }
        if (b.tier() >= 6 && free(cds, "rafale", now) && dist <= 3.5) {           // Saint : rafale du combo (lame par mouvement)
            if (cast(me, "LuoBokkenRafale", tp, store, cb)) { cds.put("rafale", now + RAFALE_CD); cds.put("lame", now + BLADE_CD); return; }
        }
        if (b.tier() >= 6 && free(cds, "lame", now) && dist >= 4 && dist <= 14) {  // Saint : lame de Ki vers la cible
            Vector3d dir = new Vector3d(tp).add(0, 0.9, 0).sub(new Vector3d(at).add(0, 0.9, 0));
            if (dir.lengthSquared() > 1e-4) {
                CloneFx.faceTowards(me, new Vector3d(tp).add(0, 1.0, 0), store);
                String[] orients = {"H", "G", "D", "V"};
                RuneSpell.Instance bl = KiEpee.launchBlade(me, b.tier(), dir.normalize(), orients[(int) Math.floorMod(now / 700, 4L)], 1f,
                        null, store, cb);
                if (bl != null) blades.computeIfAbsent(me, k -> new CopyOnWriteArrayList<>()).add(bl);
                SpellFx.sound("SFX_Ability_Wind_Strike_Slash", at, cb);
                cds.put("lame", now + BLADE_CD);
            }
        }
        if (b.tier() >= 3 && free(cds, "PasLeger", now) && dist >= DASH_MIN && dist <= DASH_MAX) {   // pas leger vers la cible
            Vector3d dir = new Vector3d(tp).sub(at);
            dir.y = 0;
            if (dir.lengthSquared() > 1e-4) {
                dir.normalize();
                CloneFx.faceTowards(me, new Vector3d(tp).add(0, 1, 0), store);
                SpellFx.push(me, new Vector3d(dir).mul(DASH_SPEED).add(0, 1.2, 0), false, cb);
                SpellFx.fx("Gaia_Dust_Burst", at, 0.5f, store, cb);
                SpellFx.sound("SFX_Light_Melee_T2_Lunge", at, cb);
                cds.put("PasLeger", now + DASH_CD);
                return;
            }
        }
        if (b.clan() != null && free(cds, "clan", now)) {                       // technique du clan
            List<LuoClans.Technique> known = new ArrayList<>();
            for (LuoClans.Technique t : LuoClans.TECHNIQUES)
                if (t.clan() == b.clan() && b.level() >= t.level() && !NPC_UNSAFE.contains(t.rune())) known.add(t);
            if (!known.isEmpty()) {
                LuoClans.Technique t = known.get((int) Math.floorMod(now / 1000, (long) known.size()));
                double reach = t.rune().equals("Rune_CroissantKi") ? 14 : 6;
                if (dist <= reach && cast(me, t.rune().substring(5), tp, store, cb)) cds.put("clan", now + CLAN_CD);
            }
        }
    }

    private static boolean free(Map<String, Long> cds, String key, long now) {
        return cds.getOrDefault(key, 0L) <= now;
    }

    /** Lance un sort du moteur RuneSpell sur ce PNJ, tourne vers sa cible ; true s'il a demarre. */
    private static boolean cast(Ref<EntityStore> me, String spellId, Vector3d tp, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        RuneSpell spell = RuneSpellRuntime.get(spellId);
        if (spell == null) return false;
        CloneFx.faceTowards(me, new Vector3d(tp).add(0, 1.0, 0), store);
        RuneSpell.Instance instance = spell.start(me, store, cb);
        if (instance != null) instances.computeIfAbsent(me, k -> new CopyOnWriteArrayList<>()).add(instance);
        return true;
    }

    private static void tickInstances(Ref<EntityStore> me, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        tickList(me, blades, store, cb);
        tickList(me, instances, store, cb);
    }

    private static void tickList(Ref<EntityStore> me, Map<Ref<EntityStore>, List<RuneSpell.Instance>> instances,
                                 Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<RuneSpell.Instance> list = instances.get(me);
        if (list == null) return;
        long now = System.currentTimeMillis();
        for (RuneSpell.Instance instance : list) {
            boolean done;
            try {
                done = instance.tick(me, now, store, cb);
            } catch (Exception e) {
                EldaniorLogger.error("[MartialNpcs] sort interrompu sur erreur", e);
                done = true;
            }
            if (!done) continue;
            try { instance.cleanup(cb); } catch (Exception e) { EldaniorLogger.error("[MartialNpcs] nettoyage d'un sort", e); }
            list.remove(instance);
        }
        if (list.isEmpty()) instances.remove(me);
    }

    private static final long ORPHAN_SWEEP_MS = 2_000;
    private static volatile long lastOrphanSweep;

    /**
     * Fin des sorts d'un PNJ (retour du 2026-11-11 : epees de Ki et lame d'aura restees apres un duel contre le Sensei) : ses
     * sorts n'avancent que quand le PNJ lui-meme passe dans ce systeme ; s'il disparait, rien ne retirait leurs entites.
     * A appeler avant de retirer le PNJ (SenseiDuel) ; releaseOrphans rattrape les autres cas toutes les 2 s.
     */
    public static void release(Ref<EntityStore> npc, CommandBuffer<EntityStore> cb) {
        if (npc == null) return;
        for (Map<Ref<EntityStore>, List<RuneSpell.Instance>> map : List.of(blades, instances)) {
            List<RuneSpell.Instance> list = map.remove(npc);
            if (list == null) continue;
            for (RuneSpell.Instance i : list) {
                try { i.cleanup(cb); } catch (Exception e) { EldaniorLogger.error("[MartialNpcs] nettoyage d'un sort", e); }
            }
        }
        enlisted.remove(npc);
        MartialKi.NPC_TIER.remove(npc);
    }

    /** Nettoie les sorts des PNJ disparus (mort, retrait, zone dechargee). */
    private static void releaseOrphans(CommandBuffer<EntityStore> cb) {
        for (Ref<EntityStore> r : new java.util.ArrayList<>(instances.keySet())) if (!r.isValid()) release(r, cb);
        for (Ref<EntityStore> r : new java.util.ArrayList<>(blades.keySet())) if (!r.isValid()) release(r, cb);
    }

    /** Oublie les PNJ disparus ; leurs sorts en cours sont abandonnes. */
    private static void sweep() {
        brains.keySet().removeIf(r -> !r.isValid());
        enlisted.keySet().removeIf(r -> !r.isValid());
        blades.keySet().removeIf(r -> !r.isValid());
        MartialKi.NPC_TIER.keySet().removeIf(r -> !r.isValid());
        lastThink.keySet().removeIf(r -> !r.isValid());
        lastAura.keySet().removeIf(r -> !r.isValid());
        fightSince.keySet().removeIf(r -> !r.isValid());
        lastFight.keySet().removeIf(r -> !r.isValid());
        ready.keySet().removeIf(r -> !r.isValid());
        instances.entrySet().removeIf(e -> {
            if (e.getKey().isValid()) return false;
            for (RuneSpell.Instance i : e.getValue()) {
                try { i.abort(); } catch (Exception ex) { EldaniorLogger.error("[MartialNpcs] abandon d'un sort", ex); }
            }
            return true;
        });
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}
