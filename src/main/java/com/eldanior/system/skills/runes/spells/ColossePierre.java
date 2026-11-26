package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.asset.type.model.config.DetailBox;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rune du Colosse de Pierre (Terre, Legendaire, rune de predilection du Demiurge) : 20 s de forme colossale. Le modele du
 * joueur (skin compris) est recopie a l'echelle x2 (memes champs que Model.createScaledModel) ; Elda_Colosse (pierre,
 * -35 % de degats, vitesse x0,8). Transformation : 150 + recul a 6 blocs. Chaque coup porte (3/s max) ouvre un cratere :
 * 80 a 2 blocs de la cible (markEcho) et 5 cases de surface videes ; en mouvement, chaque seconde : 25 a 3 blocs.
 * Garde-fou : un joueur reste a l'echelle x2 sans colosse actif (deconnexion) retrouve son apparence (restoreIfStuck).
 * Fiche de conception : scripts/runes/specs/Rune_ColossePierre.json
 */
public final class ColossePierre implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 20_000, STEP_EVERY_MS = 1_000, CRATER_EVERY_MS = 333;
    private static final float SCALE = 2.0f, STUCK_SCALE = 1.5f;
    private static final double BURST_RADIUS = 6.0, BURST_PUSH = 10.0, CRATER_RADIUS = 2.0, STEP_RADIUS = 3.0, MOVED = 0.5;
    private static final int MAX_CRATERS = 60;
    private static final float BURST = 150f, CRATER = 80f, STEP = 25f;

    // ===== Assets =====
    private static final String FORM = "Elda_Colosse";
    private static final String FX_DUST = "Gaia_Dust_Burst", FX_CRUMBS = "Block_Break_Dust";
    private static final String SFX_WAKE = "SFX_Golem_Earth_Wake", SFX_STEP = "SFX_Golem_Earth_Stomp", SFX_CRATER = "SFX_Golem_Earth_Slam_Impact";

    /** Joueurs actuellement colosses (le garde-fou ignore ceux-la). */
    private static final Set<UUID> ACTIVE = ConcurrentHashMap.newKeySet();

    @Override public String id() { return "ColossePierre"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        UUID uuid = SpellFx.uuid(caster, store);
        ModelComponent current = store.getComponent(caster, ModelComponent.getComponentType());
        if (pos == null || world == null || uuid == null || current == null || current.getModel() == null) return null;
        Model original = current.getModel();
        if (DragonAvatar.MODEL_ASSET.equals(original.getModelAssetId()) || ACTIVE.contains(uuid)) return null;   // deja transforme
        cb.putComponent(caster, ModelComponent.getComponentType(), new ModelComponent(scaled(original, SCALE)));
        EffectsManager.applyCustomEffect(caster, FORM, DURATION_MS / 1000f, OverlapBehavior.OVERWRITE, cb);
        ACTIVE.add(uuid);
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, BURST_RADIUS, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null) continue;
            Vector3d out = new Vector3d(p.x - pos.x, 0, p.z - pos.z);
            if (out.lengthSquared() < 1e-4) out.set(SpellFx.forward(caster, store));
            SpellFx.damage(caster, e, "Earth", BURST, out.normalize().mul(BURST_PUSH).add(0, 4, 0), "Impact_Strong", cb);
        }
        SpellFx.fx(FX_DUST, pos, 3.0f, store, cb);
        SpellFx.sound(SFX_WAKE, pos, cb);
        SpellFx.shake(pos, 25, "Impact_Strong", store);
        long now = System.currentTimeMillis();
        return new Colossus(caster, uuid, original, world, TerrainRestore.open(world, now + DURATION_MS + 2_000), new Vector3d(pos), now);
    }

    /** Copie du modele a l'echelle : memes champs que Model.createScaledModel (boite, yeux, camera, physique, particules). */
    static Model scaled(Model m, float f) {
        Map<String, DetailBox[]> details = null;
        if (m.getDetailBoxes() != null) {
            details = new HashMap<>();
            for (Map.Entry<String, DetailBox[]> en : m.getDetailBoxes().entrySet()) {
                DetailBox[] src = en.getValue(), dst = new DetailBox[src.length];
                for (int i = 0; i < src.length; i++) dst[i] = src[i].scaled(f);
                details.put(en.getKey(), dst);
            }
        }
        ModelParticle[] particles = null;
        if (m.getParticles() != null) {
            particles = new ModelParticle[m.getParticles().length];
            for (int i = 0; i < particles.length; i++) particles[i] = m.getParticles()[i].clone().scale(f);
        }
        PhysicsValues physics = null;
        if (m.getPhysicsValues() != null) { physics = new PhysicsValues(m.getPhysicsValues()); physics.scale(f); }
        return new Model(m.getModelAssetId(), m.getScale() * f, m.getRandomAttachmentIds(), m.getAttachments(),
                m.getBoundingBox() != null ? m.getBoundingBox().clone().scale(f) : null, m.getModel(), m.getTexture(), m.getGradientSet(), m.getGradientId(),
                m.getEyeHeight() * f, m.getCrouchOffset() * f, m.getSittingOffset() * f, m.getSleepingOffset() * f, m.getAnimationSetMap(),
                m.getCamera() != null ? m.getCamera().clone().scale(f) : null, m.getLight(), particles, m.getTrails(), physics, details,
                m.getPhobia(), m.getPhobiaModelAssetId());
    }

    /** Appele a chaque tick de chaque joueur (DivineSpellsSystem) : un joueur reste colosse sans sort actif est rendu a sa taille. */
    static void restoreIfStuck(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (ACTIVE.contains(uuid)) return;
        ModelComponent current = store.getComponent(caster, ModelComponent.getComponentType());
        Model m = current != null ? current.getModel() : null;
        if (m == null || m.getScale() < STUCK_SCALE || !"Player".equals(m.getModelAssetId())) return;
        DragonAvatar.restoreAppearance(caster, null, store, cb);
    }

    private record Hit(Ref<EntityStore> target, float amount) {}

    private static final class Colossus implements Instance {
        final Ref<EntityStore> caster;
        final UUID uuid;
        final Model original;
        final World world;
        final TerrainRestore.Restore batch;
        final Vector3d lastStepPos;
        final long startAt;
        final Queue<Hit> hits = new ConcurrentLinkedQueue<>();
        int craters;
        long lastStep, lastCrater;
        boolean reverted;

        Colossus(Ref<EntityStore> caster, UUID uuid, Model original, World world, TerrainRestore.Restore batch, Vector3d pos, long startAt) {
            this.caster = caster; this.uuid = uuid; this.original = original; this.world = world; this.batch = batch; this.lastStepPos = pos; this.startAt = startAt; this.lastStep = startAt;
        }

        @Override
        public void onOwnerHit(Ref<EntityStore> target, float amount) {
            if (amount > 0 && hits.size() < 8) hits.add(new Hit(target, amount));
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            if (now - startAt >= DURATION_MS) { revert(store, cb); return true; }
            Hit h;
            while ((h = hits.poll()) != null) {
                if (now - lastCrater < CRATER_EVERY_MS) continue;
                Vector3d p = SpellFx.position(h.target(), store);
                if (p == null) continue;
                lastCrater = now;
                crater(p, store, cb);
            }
            if (now - lastStep >= STEP_EVERY_MS) {                               // pas lourds
                lastStep = now;
                if (SpellFx.horizontalDistance(pos, lastStepPos) >= MOVED) {
                    lastStepPos.set(pos);
                    for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, STEP_RADIUS, store))
                        if (!RuneSpellRuntime.isSummon(e)) SpellFx.damage(caster, e, "Earth", STEP, null, null, cb);
                    SpellFx.fx(FX_DUST, pos, 1.2f, store, cb);
                    SpellFx.sound(SFX_STEP, pos, cb);
                    SpellFx.shake(pos, 10, "Impact_Light", store);
                }
            }
            return false;
        }

        /** Le poing du colosse ouvre un cratere sous sa cible. */
        private void crater(Vector3d at, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, CRATER_RADIUS, store)) {
                if (RuneSpellRuntime.isSummon(e)) continue;
                RuneSpellRuntime.markEcho(e, CRATER);
                SpellFx.damage(caster, e, "Earth", CRATER, null, null, cb);
            }
            SpellFx.fx(FX_DUST, at, 1.4f, store, cb);
            SpellFx.fx(FX_CRUMBS, new Vector3d(at).add(0, 0.5, 0), 1.2f, store, cb);
            SpellFx.sound(SFX_CRATER, at, cb);
            if (craters >= MAX_CRATERS) return;
            craters++;
            int cx = (int) Math.floor(at.x), cy = (int) Math.floor(at.y), cz = (int) Math.floor(at.z);
            List<int[]> cross = new ArrayList<>(List.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{-1, 0}, new int[]{0, 1}, new int[]{0, -1}));
            world.execute(() -> {
                try {
                    for (int[] d : cross) {
                        int x = cx + d[0], z = cz + d[1];
                        for (int y = cy; y >= cy - 3; y--) {
                            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z), above = WorldBlockUtil.getBlockType(world, x, y + 1, z);
                            if (t == null || t.getMaterial() != BlockMaterial.Solid || above == null || above.getMaterial() == BlockMaterial.Solid) continue;
                            String id = t.getId();
                            if (!id.startsWith("Plant_") && !TerrainRestore.isKept(world, id, x, y, z) && !TerrainRestore.isProtectedArea(world.getName(), x, y, z)
                                    && WorldBlockUtil.getFluidIndex(world, x, y + 1, z) <= 0)
                                TerrainRestore.place(world, batch, x, y, z, id, TerrainRestore.EMPTY, 0);
                            break;
                        }
                    }
                    TerrainRestore.save();
                } catch (Exception e) {
                    EldaniorLogger.error("ColossePierre: cratere", e);
                }
            });
        }

        /** Retour a la taille normale (skin reconstruit), effet retire, terrain restaure. */
        private void revert(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (reverted) return;
            reverted = true;
            ACTIVE.remove(uuid);
            try {
                if (caster.isValid()) {
                    DragonAvatar.restoreAppearance(caster, original, store, cb);
                    EffectsManager.removeEffect(caster, FORM, cb);
                    Vector3d pos = SpellFx.position(caster, store);
                    if (pos != null) { SpellFx.fx(FX_CRUMBS, new Vector3d(pos).add(0, 1, 0), 2.0f, store, cb); SpellFx.sound(SFX_WAKE, pos, cb); }
                }
            } catch (Exception e) {
                EldaniorLogger.error("ColossePierre: retour", e);
            }
            world.execute(() -> TerrainRestore.restoreNow(world, batch));
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (!reverted && caster.isValid()) revert(caster.getStore(), cb);
            else if (!reverted) { reverted = true; ACTIVE.remove(uuid); world.execute(() -> TerrainRestore.restoreNow(world, batch)); }
        }

        @Override
        public void abort() {
            ACTIVE.remove(uuid);                                                  // le garde-fou rendra sa taille au joueur
            world.execute(() -> TerrainRestore.restoreNow(world, batch));
        }
    }
}
