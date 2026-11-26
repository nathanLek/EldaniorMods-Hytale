package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.anchors.AnchorChoicePage;
import com.eldanior.system.anchors.AnchorManager;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Rune de la Porte des Mondes (Espace, Legendaire, rune de predilection de l'Architecte du Monde) : porte doree
 * (Gilgamesh_Portal agrandi) 25 s a 2,5 blocs devant le lanceur. Le lanceur et ses allies joueurs qui la franchissent
 * (1,3 bloc du centre) sont teleportes a une ancre du lanceur dans ce monde (Cercle d'Ancrage pose et nomme, voir
 * {@link AnchorManager}) ou, sans ancre, au premier point d'apparition du monde ; protection de chute 5 s, 3 s avant un
 * nouveau passage. Plusieurs ancres dans le monde : une fenetre de choix s'ouvre et la porte n'apparait qu'une fois la
 * destination choisie (fermee sans choix ou 30 s : derniere destination utilisee).
 * Fiche de conception : scripts/runes/specs/Rune_PorteMondes.json
 */
public final class PorteMondes implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 25_000, CHECK_EVERY_MS = 100, GLOW_EVERY_MS = 1_000, PASS_COOLDOWN_MS = 3_000, FALL_GUARD_MS = 5_000;
    private static final long CHOICE_TIMEOUT_MS = 30_000;
    private static final double AHEAD = 2.5, GATE_HEIGHT = 2.0, PASS_RADIUS = 1.3;
    private static final float GATE_SCALE = 2.4f;

    // ===== Assets =====
    private static final String GATE = "Projectile_Config_Gilgamesh_Portal", GATE_MODEL = "Gilgamesh_Portal";
    private static final String FX_GLOW = "Bifrost_Divine", FX_PILLAR = "Bifrost_Pillar";
    private static final String SFX_OPEN = "SFX_Portal_Neutral_Open", SFX_PASS = "SFX_Portal_Neutral";

    @Override public String id() { return "PorteMondes"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (pos == null || world == null) return null;
        UUID uuid = SpellFx.uuid(caster, store);
        List<AnchorManager.Anchor> anchors = new ArrayList<>();
        if (uuid != null)
            for (AnchorManager.Anchor a : AnchorManager.anchorsOf(uuid, world.getName()))
                if (AnchorManager.stillThere(world, a)) anchors.add(a);
        Gate gate = new Gate(caster, uuid, anchors, System.currentTimeMillis());
        if (anchors.size() >= 2) {                                                      // la porte attend le choix
            AnchorManager.clearChoice(uuid);
            Player player = store.getComponent(caster, Player.getComponentType());
            PlayerRef pr = store.getComponent(caster, PlayerRef.getComponentType());
            if (player != null && pr != null) {
                world.execute(() -> {
                    if (caster.isValid()) player.getPageManager().openCustomPage(caster, caster.getStore(), new AnchorChoicePage(pr, anchors, pos));
                });
                return gate;
            }
        }
        AnchorManager.Anchor only = anchors.isEmpty() ? null : anchors.get(0);
        Vector3d dest = destination(only, caster, world, store);
        if (dest == null) return null;
        gate.open(dest, store, cb);
        return gate.opened ? gate : null;
    }

    /** L'ancre choisie, sinon le premier point d'apparition du monde (message au lanceur). */
    private static Vector3d destination(AnchorManager.Anchor anchor, Ref<EntityStore> caster, World world, Store<EntityStore> store) {
        if (anchor != null && AnchorManager.find(anchor.key()) == anchor && AnchorManager.stillThere(world, anchor)) {
            AnchorManager.markUsed(anchor);
            tell(caster, store, "La porte mène à « " + anchor.name + " ».");
            return anchor.arrival();
        }
        try {
            Transform[] spawns = world.getWorldConfig().getSpawnProvider().getSpawnPoints();
            if (spawns != null && spawns.length > 0 && spawns[0] != null) {
                tell(caster, store, anchor != null ? "Cette ancre a disparu : la porte mène au point d'apparition du monde."
                        : "Aucune ancre dans ce monde (posez un Cercle d'Ancrage) : la porte mène au point d'apparition.");
                return new Vector3d(spawns[0].getPosition());
            }
        } catch (Exception e) {
            EldaniorLogger.error("PorteMondes: point d'apparition", e);
        }
        tell(caster, store, "La porte ne trouve aucune destination (ni ancre ni point d'apparition).");
        return null;
    }

    private static void tell(Ref<EntityStore> caster, Store<EntityStore> store, String text) {
        PlayerRef ref = store.getComponent(caster, PlayerRef.getComponentType());
        if (ref != null) ref.sendMessage(Message.raw(text));
    }

    private static final class Gate implements Instance {
        final Ref<EntityStore> caster;
        final UUID uuid;
        final List<AnchorManager.Anchor> choices;
        final long castAt;
        final Map<Ref<EntityStore>, Long> lastPass = new HashMap<>();
        Ref<EntityStore> gate;
        Vector3d base, center, fwd, dest;
        boolean opened, scaled;
        long startAt, lastCheck, lastGlow;

        Gate(Ref<EntityStore> caster, UUID uuid, List<AnchorManager.Anchor> choices, long castAt) {
            this.caster = caster; this.uuid = uuid; this.choices = choices; this.castAt = castAt;
        }

        /** Ouvre la porte devant le lanceur, vers dest. */
        void open(Vector3d dest, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            World world = SpellFx.world(store);
            if (pos == null || world == null) return;
            this.dest = dest;
            fwd = SpellFx.forward(caster, store);
            base = SpellFx.ground(world, new Vector3d(pos).add(new Vector3d(fwd).mul(AHEAD)));
            center = new Vector3d(base).add(0, GATE_HEIGHT, 0);
            gate = SpellFx.spawnModel(caster, GATE, center, fwd, cb);
            SpellFx.notSaved(gate, store, cb);
            SpellFx.fx(FX_PILLAR, base, 0.6f, store, cb);
            SpellFx.fx(FX_GLOW, center, 3.0f, store, cb);
            SpellFx.sound(SFX_OPEN, center, cb);
            startAt = System.currentTimeMillis();
            opened = true;
        }

        /** Avant l'ouverture : attend la reponse de la fenetre de choix (ou 30 s), puis ouvre. true = sort termine. */
        private boolean awaitChoice(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            String key = uuid != null ? AnchorManager.takeChoice(uuid) : "";
            if (key == null && now - castAt < CHOICE_TIMEOUT_MS) return false;
            AnchorManager.Anchor chosen = key != null && !key.isEmpty() ? AnchorManager.find(key) : null;
            if (chosen == null)                                                         // fermee sans choix : derniere destination
                for (AnchorManager.Anchor a : choices) if (AnchorManager.find(a.key()) == a) { chosen = a; break; }
            World world = SpellFx.world(store);
            Vector3d target = world != null ? destination(chosen, caster, world, store) : null;
            if (target == null) return true;
            open(target, store, cb);
            return !opened;
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            if (!opened) return awaitChoice(now, store, cb);
            if (now - startAt >= DURATION_MS) return true;
            if (gate != null && gate.isValid()) {
                if (!scaled) { scaled = true; SpellFx.setModelScale(gate, GATE_MODEL, GATE_SCALE, 1.5, cb); }
                SpellFx.hold(gate, center, SpellFx.facing(fwd, false), store);
            }
            if (now - lastGlow >= GLOW_EVERY_MS) { lastGlow = now; SpellFx.fx(FX_GLOW, center, 1.5f, store, cb); }
            if (now - lastCheck < CHECK_EVERY_MS) return false;
            lastCheck = now;
            for (Ref<EntityStore> a : SpellFx.alliesAround(caster, center, PASS_RADIUS + GATE_HEIGHT, store)) {
                Vector3d p = SpellFx.position(a, store);
                Long last = lastPass.get(a);
                if (p == null || (last != null && now - last < PASS_COOLDOWN_MS)) continue;
                if (SpellFx.horizontalDistance(p, base) > PASS_RADIUS || Math.abs(p.y - base.y) > GATE_HEIGHT + 0.5) continue;
                lastPass.put(a, now);
                SpellFx.fx(FX_GLOW, new Vector3d(p).add(0, 1, 0), 1.2f, store, cb);
                SpellFx.sound(SFX_PASS, p, cb);
                SpellFx.protectFromFall(a, FALL_GUARD_MS, store);
                SpellFx.teleport(a, dest, store, cb);
                SpellFx.fx(FX_PILLAR, dest, 0.5f, store, cb);
            }
            return false;
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { SpellFx.remove(gate, cb); }

        @Override public void abort() { RuneSpellRuntime.removeLater(gate); }
    }
}
