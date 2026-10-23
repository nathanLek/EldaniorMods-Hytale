package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rune du Doppelganger (Tenebres, Epic) : un double d'ombre du lanceur, son reflet dans un miroir, pendant {@link #DURATION_MS}.
 *
 * Le double est un PNJ (role Eldanior_Doppel), copie exacte du lanceur : modele, skin, nom affiche, objet en main, sans teinte.
 * Il apparait a {@link #START_OFFSET} blocs a sa droite. Ses deplacements suivent ceux du lanceur en miroir, dans le repere
 * du regard du lanceur : s'il va a gauche, le double va a droite ; devant -> devant, derriere -> derriere. Le sort deplace
 * son point de laisse et c'est le moteur qui le fait marcher ou courir jusque-la (vraies animations, pas de glissade).
 * Il regarde toujours ce que vise le curseur du lanceur (entite ou bloc) : orientation imposee par DoppelPoseSystem, apres
 * l'IA des PNJ. Quand le lanceur ne bouge plus ({@link #IDLE_MS}), le double agit seul : il marche vers l'ennemi vise, ou
 * fait quelques pas autour de lui.
 * Il change d'arme avec le lanceur et rejoue chacune de ses attaques et capacites d'arme (clic gauche, clic droit,
 * competences) avec la meme interaction.
 * Les runes ne sont pas rejouees (pas de sort lance par le double). Le double est intouchable.
 * Si le rejeu est impossible (pas de gestionnaire d'interactions), chaque coup du lanceur est rejoue en degats miroir.
 * Fiche de conception : scripts/runes/specs/Rune_Doppelganger.json
 */
public final class Doppelganger implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 30_000, SPAWN_TIMEOUT_MS = 3_000;
    private static final double START_OFFSET = 3.0, MAX_SEPARATION = 16.0, TELEPORT_JUMP = 8.0, ECHO_RADIUS = 2.5;
    private static final double AIM_RANGE = 40.0, APPROACH_RANGE = 24.0, APPROACH_STOP = 2.0, STROLL_RADIUS = 3.0, SNAP_DISTANCE = 12.0;
    private static final long IDLE_MS = 1_500, STROLL_EVERY_MS = 4_000;
    private static final float ECHO_RATIO = 0.6f;
    private static final Set<InteractionType> REPLAYED = EnumSet.of(InteractionType.Primary, InteractionType.Secondary,
            InteractionType.Ability1, InteractionType.Ability2, InteractionType.Ability3, InteractionType.Ability4);

    // ===== Assets =====
    private static final String ROLE = "Eldanior_Doppel";
    private static final String FX_SMOKE = "Nyx_Void", FX_HIT = "Impact_Sword_Basic";
    private static final String SFX_APPEAR = "SFX_Portal_Void", SFX_END = "SFX_Void_Eviction";

    @Override public String id() { return "Doppelganger"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (pos == null || world == null) return null;
        long now = System.currentTimeMillis();
        Vector3d fwd = SpellFx.forward(caster, store);
        Mirror m = new Mirror(caster, pos, now, world);
        Vector3d at = SpellFx.ground(world, new Vector3d(pos).add(right(fwd).mul(START_OFFSET)));
        m.anchor = new Vector3d(at.x, pos.y, at.z);
        ItemStack held = CloneFx.heldItem(caster, store);
        m.weaponId = held != null ? held.getItemId() : null;
        m.doppel = CloneFx.spawn(world, ROLE, caster, at, SpellFx.facing(fwd, false), CloneFx.Look.of(caster, store),
                held, 0f, null, true, now);
        m.lastPos = at;
        SpellFx.fx(FX_SMOKE, new Vector3d(at).add(0, 1, 0), 1.5f, store, cb);
        SpellFx.sound(SFX_APPEAR, at, cb);
        EldaniorLogger.info("[Doppelganger] double en miroir pour " + DURATION_MS / 1000 + " s");
        return m;
    }

    private record Hit(Ref<EntityStore> target, float amount) {}

    /** Droite du regard (horizontal). */
    private static Vector3d right(Vector3d fwd) { return new Vector3d(-fwd.z, 0, fwd.x); }

    /** Pose du double calculee par le sort, appliquee par DoppelPoseSystem apres l'IA des PNJ. */
    record Pose(Vector3d pos, Rotation3f body, Rotation3f head, MovementStates states) {}
    private static final Map<Ref<EntityStore>, Pose> POSES = new ConcurrentHashMap<>();

    static Pose poseOf(Ref<EntityStore> ref) { return POSES.isEmpty() ? null : POSES.get(ref); }

    private static final class Mirror implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        final World world;
        final Queue<Hit> hits = new ConcurrentLinkedQueue<>();
        final Vector3d lastCaster;
        Vector3d anchor;                                                 // position du double au sol (y : hauteur du lanceur)
        CloneFx.Npc doppel;
        Vector3d lastPos;
        String weaponId;
        boolean ready, replayBroken, autonomous;
        long lastMoveAt, lastStroll;
        Set<Integer> seenChains;                                         // chaines du lanceur deja rejouees (null : premiere lecture)

        Mirror(Ref<EntityStore> caster, Vector3d casterPos, long startAt, World world) {
            this.caster = caster; this.lastCaster = new Vector3d(casterPos); this.startAt = startAt; this.world = world;
            this.lastMoveAt = startAt;
        }

        /** Deplacement en miroir dans le repere du regard : avant/arriere identique, gauche/droite inversee. */
        static Vector3d mirror(Vector3d v, Vector3d fwd) {
            Vector3d r = right(fwd);
            double f = v.x * fwd.x + v.z * fwd.z, l = v.x * r.x + v.z * r.z;
            return new Vector3d(fwd.x * f - r.x * l, 0, fwd.z * f - r.z * l);
        }

        /** Coups du lanceur : seulement utilises si le rejeu des interactions est impossible. */
        @Override public void onOwnerHit(Ref<EntityStore> target, float amount) { if (replayBroken) hits.add(new Hit(target, amount)); }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null || now - startAt >= DURATION_MS) return true;
            Ref<EntityStore> ref = doppel.ref;
            if (ref == null) return doppel.failed || now - doppel.born > SPAWN_TIMEOUT_MS;        // PNJ pas encore cree
            if (!ref.isValid()) return true;
            ready = true;
            Vector3d fwd = SpellFx.forward(caster, store);
            Vector3d here = SpellFx.position(ref, store);
            if (here == null) return true;
            Vector3d step = new Vector3d(pos).sub(lastCaster);
            step.y = 0;
            lastCaster.set(pos);
            Ref<EntityStore> aimed = aimedEntity(store);
            Vector3d aim = aimPoint(aimed, pos, store);
            if (step.length() > TELEPORT_JUMP) {                                                 // lanceur teleporte
                anchor = new Vector3d(pos).add(right(fwd).mul(START_OFFSET));
                lastMoveAt = now;
            } else if (step.length() > 0.02) {                                                   // il bouge : miroir
                if (autonomous) { anchor = new Vector3d(here); autonomous = false; }             // reprend d'ou il est
                anchor.add(mirror(step, fwd));
                lastMoveAt = now;
            } else if (now - lastMoveAt > IDLE_MS) {                                             // immobile : le double agit seul
                autonomous = true;
                Vector3d tp = aimed != null && SpellFx.isEnemy(caster, aimed, store) ? SpellFx.position(aimed, store) : null;
                if (tp != null && tp.distance(here) <= APPROACH_RANGE) {
                    Vector3d back = new Vector3d(here).sub(tp);
                    back.y = 0;
                    if (back.lengthSquared() > 1e-4) back.normalize().mul(APPROACH_STOP);
                    anchor = new Vector3d(tp).add(back);                                         // marche jusqu'a l'ennemi vise
                } else if (now - lastStroll > STROLL_EVERY_MS) {
                    lastStroll = now;
                    double a = Math.random() * Math.PI * 2, r = 1 + Math.random() * (STROLL_RADIUS - 1);
                    anchor = new Vector3d(here).add(Math.cos(a) * r, 0, Math.sin(a) * r);       // quelques pas
                }
            }
            Vector3d sep = new Vector3d(anchor).sub(pos);
            sep.y = 0;
            if (sep.length() > MAX_SEPARATION) anchor = new Vector3d(pos).add(sep.normalize().mul(MAX_SEPARATION));
            anchor.y = pos.y;
            Vector3d q = SpellFx.ground(world, anchor);
            lastPos = here;
            CloneFx.leash(ref, q, store);                                                         // le moteur le fait marcher / courir
            if (here.distance(q) > SNAP_DISTANCE) SpellFx.hold(ref, q, null, store);              // trop loin (obstacle) : il rattrape
            // Regard : toujours vers ce que vise le curseur du lanceur
            Vector3d dir = new Vector3d(aim).sub(here.x, here.y + 1.6, here.z);
            if (dir.lengthSquared() < 1e-4) dir.set(fwd);
            POSES.put(ref, new Pose(null, SpellFx.facing(dir, false), SpellFx.facing(dir, true), null));
            // Meme objet en main que le lanceur
            ItemStack held = CloneFx.heldItem(caster, store);
            String id = held != null ? held.getItemId() : null;
            if (!Objects.equals(id, weaponId)) { weaponId = id; CloneFx.setWeapon(ref, held, cb); }
            replay(ref, store, cb);
            if (replayBroken) echo(pos, q, fwd, store, cb);
            return false;
        }

        /** Entite sous le curseur du lanceur (hors invocations : le double ne se regarde pas lui-meme). */
        private Ref<EntityStore> aimedEntity(Store<EntityStore> store) {
            Ref<EntityStore> e = SpellFx.targetLiving(caster, store, AIM_RANGE);
            return e != null && e.isValid() && !RuneSpellRuntime.isSummon(e) ? e : null;
        }

        /** Point vise par le curseur du lanceur : l'entite (a hauteur du torse), sinon le bloc, sinon droit devant. */
        private Vector3d aimPoint(Ref<EntityStore> aimed, Vector3d pos, Store<EntityStore> store) {
            Vector3d tp = aimed != null ? SpellFx.position(aimed, store) : null;
            if (tp != null) return tp.add(0, 1.2, 0);
            Vector3i block = TargetUtil.getTargetBlock(caster, AIM_RANGE, store);
            if (block != null) return new Vector3d(block.x + 0.5, block.y + 0.5, block.z + 0.5);
            return new Vector3d(pos).add(0, 1.6, 0).add(SpellFx.lookDirection(caster, store).mul(30));
        }

        /** Rejoue sur le double chaque nouvelle interaction du lanceur (attaques, blocage, competences d'arme). */
        private void replay(Ref<EntityStore> ref, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (replayBroken) return;
            try {
                var type = InteractionModule.get().getInteractionManagerComponent();
                InteractionManager mine = store.getComponent(caster, type), theirs = store.getComponent(ref, type);
                if (mine == null || theirs == null) { replayBroken = true; EldaniorLogger.warn("[Doppelganger] rejeu impossible : degats miroir"); return; }
                Set<Integer> current = new HashSet<>();
                for (InteractionChain chain : mine.getChains().values()) {
                    if (chain == null || !REPLAYED.contains(chain.getType())) continue;
                    current.add(chain.getChainId());
                    if (seenChains == null || seenChains.contains(chain.getChainId())) continue;
                    RootInteraction root = chain.getInitialRootInteraction();
                    if (root == null || !replayable(root.getId())) continue;
                    InteractionContext ctx = InteractionContext.forInteraction(theirs, ref, chain.getType(), cb);
                    theirs.tryStartChain(ref, cb, chain.getType(), ctx, root);
                }
                seenChains = current;
            } catch (Exception e) {
                replayBroken = true;
                EldaniorLogger.error("[Doppelganger] rejeu des interactions (degats miroir a la place)", e);
            }
        }

        /** Les runes et grimoires ne sont pas rejoues : le double ne lance pas de sorts. */
        private static boolean replayable(String rootId) {
            return rootId != null && !rootId.startsWith("Root_Ability_") && !rootId.contains("Spell") && !rootId.contains("Rune")
                    && !rootId.contains("Consumable");
        }

        /** Repli : les ennemis places devant le double comme la cible l'est devant le lanceur (en miroir) prennent une part des degats. */
        private void echo(Vector3d pos, Vector3d q, Vector3d fwd, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Hit h; (h = hits.poll()) != null; ) {
                Vector3d tp = SpellFx.position(h.target(), store);
                if (tp == null) continue;
                float dmg = h.amount() * ECHO_RATIO;
                Vector3d spot = new Vector3d(q).add(mirror(new Vector3d(tp).sub(pos), fwd));
                spot.y = tp.y;
                List<Ref<EntityStore>> victims = SpellFx.enemiesAround(caster, spot, ECHO_RADIUS, store);
                for (Ref<EntityStore> e : victims) {
                    if (RuneSpellRuntime.isSummon(e)) continue;
                    Vector3d ep = SpellFx.position(e, store);
                    if (ep == null) continue;
                    Vector3d push = new Vector3d(ep).sub(q);
                    push.y = 0;
                    if (push.lengthSquared() > 1e-4) push.normalize().mul(3).add(0, 2, 0);
                    RuneSpellRuntime.markEcho(e, dmg);                                          // ce coup ne sera pas recopie
                    SpellFx.damage(caster, e, "Physical", dmg, push, "Impact_Light", cb);
                    SpellFx.fx(FX_HIT, new Vector3d(ep).add(0, 1, 0), 1.0f, store, cb);
                }
                SpellFx.fx(FX_SMOKE, new Vector3d(q).add(0, 1.2, 0).add(new Vector3d(fwd).mul(0.8)), 0.6f, store, cb);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            Ref<EntityStore> ref = doppel != null ? doppel.ref : null;
            if (ready && ref != null && ref.isValid() && lastPos != null) {
                SpellFx.fx(FX_SMOKE, new Vector3d(lastPos).add(0, 1, 0), 1.5f, ref.getStore(), cb);
                SpellFx.sound(SFX_END, lastPos, cb);
            }
            if (ref != null) POSES.remove(ref);
            SpellFx.remove(ref, cb);
            RuneSpellRuntime.forgetSummon(ref);
            hits.clear();
        }

        @Override
        public void abort() {
            if (doppel == null) return;
            if (doppel.ref != null) POSES.remove(doppel.ref);
            RuneSpellRuntime.removeLater(doppel.ref);
            RuneSpellRuntime.forgetSummon(doppel.ref);
        }
    }
}
