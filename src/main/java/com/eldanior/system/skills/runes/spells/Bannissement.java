package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Rune du Bannissement (Espace, Unique, rune de predilection du Mage du Vide) : l'ennemi vise (25 blocs) et jusqu'a 4
 * ennemis a 8 blocs de lui sont arraches au monde 6 s : places 60 blocs au-dessus de leur position (meme colonne, donc
 * chunk charge), dissous (Elda_Banni) et figes (lockInPlace ; joueurs replaces toutes les 0,25 s). Puis le vide les rend
 * 8 blocs au-dessus de leur place d'origine : ils retombent et subissent 250 degats.
 * Fiche de conception : scripts/runes/specs/Rune_Bannissement.json
 */
public final class Bannissement implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long BANISH_MS = 6_000, HOLD_PLAYERS_EVERY_MS = 250;
    private static final double RANGE = 25.0, SPREAD = 8.0, POCKET_HEIGHT = 60.0, DROP_HEIGHT = 8.0;
    private static final int TARGETS = 5;
    private static final float RETURN_DAMAGE = 250f;

    // ===== Assets =====
    private static final String BANISHED = "Elda_Banni";
    private static final String FX_VOID = "Nyx_Void", FX_FLASH = "Chaos_Flash";
    private static final String SFX_VOID = "SFX_Portal_Void";

    @Override public String id() { return "Bannissement"; }

    /** Un banni : sa place d'origine et sa poche (au-dessus). */
    private record Exile(Ref<EntityStore> target, Vector3d origin, Vector3d pocket, boolean player) {}

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Ref<EntityStore> first = SpellFx.targetEntity(caster, store, RANGE);
        if (first == null || RuneSpellRuntime.isSummon(first)) first = nearestAhead(caster, pos, store);
        Vector3d fp = first != null ? SpellFx.position(first, store) : null;
        if (fp == null) return null;
        List<Ref<EntityStore>> chosen = new ArrayList<>();
        chosen.add(first);
        List<Ref<EntityStore>> around = SpellFx.enemiesAround(caster, fp, SPREAD, store);
        around.sort((a, b) -> Double.compare(dist(a, fp, store), dist(b, fp, store)));
        for (Ref<EntityStore> e : around) {
            if (chosen.size() >= TARGETS) break;
            if (!chosen.contains(e) && !RuneSpellRuntime.isSummon(e)) chosen.add(e);
        }
        List<Exile> exiles = new ArrayList<>();
        for (Ref<EntityStore> e : chosen) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null) continue;
            Exile x = new Exile(e, new Vector3d(p), new Vector3d(p).add(0, POCKET_HEIGHT, 0), store.getComponent(e, Player.getComponentType()) != null);
            SpellFx.fx(FX_VOID, new Vector3d(p).add(0, 0.5, 0), 1.2f, store, cb);
            SpellFx.fx(FX_FLASH, new Vector3d(p).add(0, 1, 0), 0.6f, store, cb);
            SpellFx.sound(SFX_VOID, p, cb);
            try { EffectsManager.applyCustomEffect(e, BANISHED, BANISH_MS / 1000f, OverlapBehavior.OVERWRITE, cb); }
            catch (Exception ex) { EldaniorLogger.error("Bannissement: effet", ex); }
            SpellFx.teleport(e, x.pocket(), store, cb);
            exiles.add(x);
        }
        if (exiles.isEmpty()) return null;
        return new Exiled(exiles, System.currentTimeMillis());
    }

    private static double dist(Ref<EntityStore> e, Vector3d from, Store<EntityStore> store) {
        Vector3d p = SpellFx.position(e, store);
        return p == null ? Double.MAX_VALUE : p.distance(from);
    }

    private static Ref<EntityStore> nearestAhead(Ref<EntityStore> caster, Vector3d pos, Store<EntityStore> store) {
        Vector3d fwd = SpellFx.forward(caster, store);
        Ref<EntityStore> best = null;
        double bestD = Double.MAX_VALUE;
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, new Vector3d(pos).add(new Vector3d(fwd).mul(RANGE / 2)), RANGE / 2 + 1, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null || RuneSpellRuntime.isSummon(e)) continue;
            Vector3d to = new Vector3d(p).sub(pos);
            double d = to.length();
            if (d > RANGE || to.setComponent(1, 0).normalize().dot(fwd) < 0.5) continue;
            if (d < bestD) { bestD = d; best = e; }
        }
        return best;
    }

    private static final class Exiled implements Instance {
        final List<Exile> exiles;
        final long startAt;
        boolean locked, released;
        long lastHold;

        Exiled(List<Exile> exiles, long startAt) { this.exiles = exiles; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!locked) {                                                        // la teleportation s'applique en fin de tick : figer au suivant
                locked = true;
                for (Exile x : exiles) if (!x.player()) SpellFx.lockInPlace(x.target(), BANISH_MS, store);
                return false;
            }
            if (now - startAt >= BANISH_MS) { release(caster, true, store, cb); return true; }
            if (now - lastHold >= HOLD_PLAYERS_EVERY_MS) {                       // les joueurs bannis restent dans leur poche
                lastHold = now;
                for (Exile x : exiles) if (x.player() && x.target().isValid()) SpellFx.teleport(x.target(), x.pocket(), store, cb);
            }
            return false;
        }

        /** Le vide recrache les bannis au-dessus de leur place. */
        private void release(Ref<EntityStore> caster, boolean hurt, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            released = true;
            for (Exile x : exiles) {
                Ref<EntityStore> e = x.target();
                SpellFx.unlock(e);
                if (!e.isValid() || SpellFx.position(e, store) == null) continue;
                try { EffectsManager.removeEffect(e, BANISHED, cb); } catch (Exception ex) { EldaniorLogger.error("Bannissement: effet", ex); }
                Vector3d drop = new Vector3d(x.origin()).add(0, DROP_HEIGHT, 0);
                SpellFx.teleport(e, drop, store, cb);
                SpellFx.fx(FX_VOID, drop, 1.2f, store, cb);
                SpellFx.sound(SFX_VOID, drop, cb);
                if (hurt && caster != null) SpellFx.damage(caster, e, "Elemental", RETURN_DAMAGE, null, "Impact_Strong", cb);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (released) return;
            for (Exile x : exiles) {                                              // fin anticipee (lanceur mort) : retour a leur place, sans degats
                Ref<EntityStore> e = x.target();
                SpellFx.unlock(e);
                if (!e.isValid()) continue;
                try {
                    EffectsManager.removeEffect(e, BANISHED, cb);
                    SpellFx.teleport(e, x.origin(), e.getStore(), cb);
                } catch (Exception ex) {
                    EldaniorLogger.error("Bannissement: retour anticipe", ex);
                }
            }
        }

        @Override
        public void abort() { for (Exile x : exiles) SpellFx.unlock(x.target()); }   // pas de CommandBuffer : le gel cesse, ils retombent
    }
}
