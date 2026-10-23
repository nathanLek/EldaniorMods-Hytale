package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Rune de Clonage (Lumiere, Epic) : {@link #CLONES} copies de lumiere du lanceur pendant {@link #DURATION_MS}.
 *
 * Chaque clone est un vrai PNJ, copie indiscernable du lanceur : modele, skin, nom affiche et arme de corps a corps (main nue
 * s'il n'en a pas), sans teinte ; {@link #HP_RATIO} de sa vie maximale. Role Eldanior_Clone_Lumiere (epee : vrais combos
 * d'epee via l'evaluateur de combat) ou Eldanior_Clone_Lumiere_Melee (autre arme, mains nues). Il suit le lanceur ; clic droit
 * sur un ennemi : le clone libre le plus proche part l'attaquer et devient autonome jusqu'a la fin du sort : il combat sa
 * cible puis tout monstre a moins de {@link #AUTO_RANGE} blocs de lui, seul (poursuite, sauts, attaques de l'arme, competence
 * de signature), tant qu'il reste a moins de {@link #LEASH} blocs du lanceur. Les monstres voient les clones et les attaquent.
 * Un coup fatal dissipe le clone. Pas de tir ami entre le lanceur et ses clones.
 * Fiche de conception : scripts/runes/specs/Rune_Clonage.json
 */
public final class Clonage implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final int CLONES = 5;
    private static final long DURATION_MS = 20_000, SPAWN_TIMEOUT_MS = 3_000, DETECT_EVERY_MS = 1_000, SIGNATURE_EVERY_MS = 6_000;
    private static final double HOSTILE_S = 3.0;                                    // attitudes forcees, rafraichies chaque seconde
    private static final float HP_RATIO = 0.5f;
    private static final double TARGET_RANGE = 30.0, LEASH = 40.0, AUTO_RANGE = 15.0;
    private static final double FORMATION_RADIUS = 2.5;
    private static final double[] FORMATION_ANGLES = {-150, -115, 180, 115, 150};   // en arc derriere le lanceur (degres)

    // ===== Assets =====
    private static final String ROLE_SWORD = "Eldanior_Clone_Lumiere", ROLE_MELEE = "Eldanior_Clone_Lumiere_Melee";
    private static final String FX_APPEAR = "Bifrost_Divine", FX_SPARK = "Telekinesie_Aura";
    private static final String SFX_APPEAR = "SFX_Portal_Neutral_Open", SFX_DISPATCH = "SFX_Ability_Wind_Strike_Slash";

    @Override public String id() { return "Clonage"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (pos == null || world == null) return null;
        long now = System.currentTimeMillis();
        Squad s = new Squad(caster, now, world);
        CloneFx.Look look = CloneFx.Look.of(caster, store);
        ItemStack weapon = CloneFx.meleeWeapon(caster, store);
        float hp = CloneFx.maxHealth(caster, store) * HP_RATIO;
        Vector3d fwd = SpellFx.forward(caster, store);
        s.sword = weapon != null && weapon.getItemId().startsWith("Weapon_Sword");
        String role = s.sword ? ROLE_SWORD : ROLE_MELEE;
        for (int i = 0; i < CLONES; i++) {
            Vector3d at = s.slot(pos, fwd, i);
            s.clones.add(new Clone(CloneFx.spawn(world, role, caster, at, SpellFx.facing(fwd, false), look, weapon, hp, null, false, now), i, at));
            SpellFx.fx(FX_APPEAR, new Vector3d(at).add(0, 1, 0), 1.2f, store, cb);
        }
        SpellFx.sound(SFX_APPEAR, pos, cb);
        EldaniorLogger.info("[Clonage] " + CLONES + " clones (" + (int) hp + " PV, arme : " + (weapon != null ? weapon.getItemId() : "main nue") + ")");
        return s;
    }

    private static final class Clone {
        final CloneFx.Npc npc;
        final int slot;                                                                 // place en formation
        Vector3d lastPos;
        Ref<EntityStore> target;
        boolean ready, gone, engaged;                                                   // engaged : lance par un clic droit, autonome
        Clone(CloneFx.Npc npc, int slot, Vector3d pos) { this.npc = npc; this.slot = slot; this.lastPos = pos; }
        boolean free() { return ready && !gone && !engaged; }
    }

    private static final class Squad implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        final World world;
        final List<Clone> clones = new ArrayList<>();
        long lastDetect, lastSignature;
        boolean sword;

        Squad(Ref<EntityStore> caster, long startAt, World world) {
            this.caster = caster; this.startAt = startAt; this.world = world; this.lastSignature = startAt;
        }

        /** Place du clone i dans la formation, en arc derriere le lanceur. */
        Vector3d slot(Vector3d casterPos, Vector3d fwd, int i) {
            double base = Math.atan2(fwd.z, fwd.x), a = base + Math.toRadians(FORMATION_ANGLES[i % FORMATION_ANGLES.length]);
            return SpellFx.ground(world, new Vector3d(casterPos).add(Math.cos(a) * FORMATION_RADIUS, 0, Math.sin(a) * FORMATION_RADIUS));
        }

        @Override public boolean listensClicks() { return true; }

        /** Clic droit sur un ennemi : le clone libre le plus proche de la cible part l'attaquer (a defaut, le plus proche). */
        @Override
        public void onClick(boolean left, Ref<EntityStore> target, Ref<EntityStore> casterRef, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (left) return;
            Ref<EntityStore> t = valid(target, store) ? target : SpellFx.targetEntity(caster, store, TARGET_RANGE);
            if (!valid(t, store)) return;
            Vector3d tp = SpellFx.position(t, store);
            if (tp == null) return;
            Clone best = null, fallback = null;
            double bestD = Double.MAX_VALUE, fallbackD = Double.MAX_VALUE;
            for (Clone c : clones) {
                if (!c.ready || c.gone || t.equals(c.target)) continue;
                double d = c.lastPos.distance(tp);
                if (c.free() && d < bestD) { bestD = d; best = c; }
                if (d < fallbackD) { fallbackD = d; fallback = c; }
            }
            Clone chosen = best != null ? best : fallback;
            if (chosen == null) { EldaniorLogger.info("[Clonage] aucun clone disponible"); return; }
            dispatch(chosen, t, store);
            SpellFx.fx(FX_SPARK, new Vector3d(chosen.lastPos).add(0, 1, 0), 1.5f, store, cb);
            SpellFx.sound(SFX_DISPATCH, chosen.lastPos, cb);
        }

        /** Cible valide pour un clone : ennemi du lanceur, et pas une invocation (ses propres clones compris). */
        private boolean valid(Ref<EntityStore> t, Store<EntityStore> store) {
            return t != null && t.isValid() && SpellFx.isEnemy(caster, t, store) && !RuneSpellRuntime.isSummon(t);
        }

        /** Le clone est lance : il voit la cible comme hostile, la verrouille et passe en combat ; la cible le voit aussi. */
        private void dispatch(Clone c, Ref<EntityStore> t, Store<EntityStore> store) {
            c.target = t;
            c.engaged = true;
            Ref<EntityStore> ref = c.npc.ref;
            CloneFx.hostile(ref, t, HOSTILE_S, store);
            CloneFx.hostile(t, ref, HOSTILE_S, store);
            CloneFx.mark(ref, "LockedTarget", t, store);
            CloneFx.setState(ref, "Combat", sword ? "Default" : null, store);
        }

        /** Le clone revient aupres du lanceur (trop loin) : il n'est plus lance, ses attitudes forcees expirent seules. */
        private void recall(Clone c, Store<EntityStore> store) {
            c.target = null;
            c.engaged = false;
            CloneFx.mark(c.npc.ref, "LockedTarget", null, store);
            CloneFx.setState(c.npc.ref, "Idle", null, store);
        }

        /** Monstres (hostiles aux joueurs) a moins de AUTO_RANGE du point, hors invocations. */
        private List<Ref<EntityStore>> monstersAround(Vector3d at, Store<EntityStore> store) {
            List<Ref<EntityStore>> out = new ArrayList<>();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, AUTO_RANGE, store))
                if (!RuneSpellRuntime.isSummon(e) && CloneFx.isMonster(e, store)) out.add(e);
            return out;
        }

        /** Monstre le plus proche du clone (enchainement autonome apres la mort de sa cible). */
        private Ref<EntityStore> nearestMonster(Vector3d at, Store<EntityStore> store) {
            Ref<EntityStore> best = null;
            double bestD = Double.MAX_VALUE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, AUTO_RANGE, store)) {
                if (RuneSpellRuntime.isSummon(e) || !CloneFx.isMonster(e, store)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                double d = p.distanceSquared(at);
                if (d < bestD) { bestD = d; best = e; }
            }
            return best;
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null || now - startAt >= DURATION_MS) return true;                       // les clones se dissipent (cleanup)
            boolean detect = now - lastDetect >= DETECT_EVERY_MS, signature = now - lastSignature >= SIGNATURE_EVERY_MS;
            if (detect) lastDetect = now;
            if (signature) lastSignature = now;
            int alive = 0;
            Vector3d fwd = SpellFx.forward(caster, store);
            for (Clone c : clones) {
                if (c.gone) continue;
                Ref<EntityStore> ref = c.npc.ref;
                if (ref == null) {                                                              // PNJ pas encore cree
                    if (c.npc.failed || now - c.npc.born > SPAWN_TIMEOUT_MS) c.gone = true; else alive++;
                    continue;
                }
                if (!ref.isValid() || RuneSpellRuntime.isDying(ref)) { dissipate(c, store, cb); continue; }
                alive++;
                Vector3d cp = SpellFx.position(ref, store);
                if (cp != null) c.lastPos = cp;
                if (!c.ready) { c.ready = true; detect = true; }
                // Il marche lui-meme (animations du moteur) vers sa place en formation, (en combat, son role suit la cible)
                CloneFx.leash(ref, slot(pos, fwd, c.slot), store);
                if (signature) CloneFx.fillStat(ref, DefaultEntityStatTypes.getSignatureEnergy(), store);
                if (!c.engaged) {
                    // Pas encore lance : il suit le lanceur ; les monstres proches le voient quand meme
                    if (detect) for (Ref<EntityStore> e : monstersAround(c.lastPos, store)) CloneFx.hostile(e, ref, HOSTILE_S, store);
                    continue;
                }
                // Lance : autonome tant qu'il reste pres du lanceur
                if (c.lastPos.distance(pos) > LEASH) { recall(c, store); continue; }
                if (!detect) continue;
                if (c.target != null && (!c.target.isValid() || SpellFx.position(c.target, store) == null)) c.target = nearestMonster(c.lastPos, store);
                if (c.target != null) { CloneFx.hostile(ref, c.target, HOSTILE_S, store); CloneFx.hostile(c.target, ref, HOSTILE_S, store); }
                for (Ref<EntityStore> e : monstersAround(c.lastPos, store)) {                 // il choisit lui-meme parmi les monstres proches
                    CloneFx.hostile(ref, e, HOSTILE_S, store);
                    CloneFx.hostile(e, ref, HOSTILE_S, store);
                }
            }
            return alive == 0;
        }

        /** Le clone vaincu se dissipe en lumiere (retire avant de mourir : aucun butin, l'arme copiee disparait). */
        private void dissipate(Clone c, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            c.gone = true;
            SpellFx.fx(FX_APPEAR, new Vector3d(c.lastPos).add(0, 1, 0), 1.0f, store, cb);
            SpellFx.remove(c.npc.ref, cb);
            RuneSpellRuntime.forgetSummon(c.npc.ref);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Clone c : clones) {
                if (c.gone) continue;
                c.gone = true;
                Ref<EntityStore> ref = c.npc.ref;
                if (ref != null && ref.isValid()) SpellFx.fx(FX_APPEAR, new Vector3d(c.lastPos).add(0, 1, 0), 1.0f, ref.getStore(), cb);
                SpellFx.remove(ref, cb);
                RuneSpellRuntime.forgetSummon(ref);
            }
        }

        @Override
        public void abort() {
            for (Clone c : clones) {
                RuneSpellRuntime.removeLater(c.npc.ref);
                RuneSpellRuntime.forgetSummon(c.npc.ref);
            }
        }
    }
}
