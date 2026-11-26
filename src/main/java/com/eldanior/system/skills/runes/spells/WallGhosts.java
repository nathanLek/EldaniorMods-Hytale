package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Doubles rouges des presences cachees par un mur (sortis de SensMartial le 2026-11-16 pour la rune Arkies Domini de l'Observateur,
 * classe unique). Le moteur ne dessine rien derriere un mur : pour une cible cachee, un double de son modele, rouge (effet
 * Sens_Fantome, sombre au-dela de NEAR blocs), est place DEVANT le mur sur la ligne des yeux du lanceur a la cible, reduit
 * dans le rapport des distances : a l'ecran il occupe exactement la place de la cible (perspective). Seul le lanceur le voit
 * (PrivateVisibility). Valable en vue a la premiere personne.
 * 2026-11-16 (demande de l'utilisateur) : plus d'etiquette « nom — distance » sur les doubles (« les nameplates qui ne
 * servent pas »).
 */
final class WallGhosts {

    private static final double GHOST_MIN = 0.7, GHOST_MARGIN = 0.35, EYE = 1.6, RAY_STEP = 0.25, NEAR = 15.0;
    private static final String GHOST_EFFECT = "Sens_Fantome", GHOST_FAR = "Sens_Fantome_Loin";

    /** Double d'une cible : entite privee, modele et echelle courants. */
    private static final class Ghost {
        Ref<EntityStore> ref;
        String model, anim;
        double scale;
        boolean dressed;
        Boolean near;
        Vector3d last;
        long lastAt;
    }

    private final Map<Ref<EntityStore>, Ghost> ghosts = new HashMap<>();

    /** true si ce ref est un double de ce suivi (pour ne pas le prendre pour une presence). */
    boolean isGhost(Ref<EntityStore> ref) {
        for (Ghost g : ghosts.values()) if (ref != null && ref.equals(g.ref)) return true;
        return false;
    }

    /**
     * A chaque tick : doubles des cibles cachees (au plus max, dans l'ordre de la liste), retires pour celles revenues en vue,
     * sorties du rayon ou mortes. Un mur entre les yeux et le milieu du corps -> double a d' = distance du mur - marge,
     * echelle d'/d. L'echelle n'est changee (modele recree) que si l'ancienne ne tient plus devant le mur.
     */
    void update(Ref<EntityStore> caster, Vector3d me, List<Ref<EntityStore>> tracked, double radius, int max,
                Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        var world = SpellFx.world(store);
        Vector3d eye = new Vector3d(me).add(0, EYE, 0);
        double r2 = radius * radius;
        Set<Ref<EntityStore>> seen = new HashSet<>();
        for (Ref<EntityStore> e : tracked) {
            if (seen.size() >= max) break;
            Vector3d feet = e != null && e.isValid() && !RuneSpellRuntime.isDying(e) ? SpellFx.position(e, store) : null;
            if (feet == null || feet.distanceSquared(me) > r2) continue;
            var mc = store.getComponent(e, com.hypixel.hytale.server.core.modules.entity.component.ModelComponent.getComponentType());
            var model = mc == null ? null : mc.getModel();
            if (model == null || model.getModelAssetId() == null) continue;
            Vector3d mid = new Vector3d(feet).add(0, Math.max(0.4, model.getEyeHeight() * 0.6), 0);
            double d = eye.distance(mid);
            double wall = firstWall(world, eye, mid, d);
            if (wall < 0) continue;                                         // en vue : la silhouette de particules suffit
            double maxScale = Math.max(GHOST_MIN, wall - GHOST_MARGIN) / d; // echelle maximale qui reste devant le mur
            seen.add(e);
            Ghost g = ghosts.computeIfAbsent(e, k -> new Ghost());
            if (g.ref == null || !g.ref.isValid() && g.dressed) {
                if (g.ref != null) { PrivateVisibility.forget(g.ref); g.ref = null; }
                // porteur = modele simple (un projectile etait supprime par le moteur quand il « touchait » une entite)
                g.scale = maxScale * 0.85;
                g.model = model.getModelAssetId();
                Vector3d at = new Vector3d(eye).add(new Vector3d(feet).sub(eye).mul(g.scale));
                var tr = store.getComponent(e, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
                g.ref = SpellFx.spawnStatic(g.model, (float) (g.scale * Math.max(0.01f, model.getScale())), at,
                        tr == null ? null : new com.hypixel.hytale.math.vector.Rotation3f(tr.getRotation()), store, cb);
                if (g.ref == null) { ghosts.remove(e); continue; }
                PrivateVisibility.show(g.ref, caster);
                g.dressed = false; g.near = null; g.anim = null;
                continue;
            }
            if (!g.ref.isValid()) continue;                                 // le porteur n'existe qu'au tick suivant
            g.dressed = true;
            boolean near = feet.distance(me) <= NEAR;                       // proche : rouge vif ; loin : rouge sombre
            if (g.near == null || g.near != near) {
                if (g.near != null) EffectsManager.removeEffect(g.ref, g.near ? GHOST_EFFECT : GHOST_FAR, cb);
                SpellFx.effect(g.ref, near ? GHOST_EFFECT : GHOST_FAR, cb);
                g.near = near;
            }
            long now = System.currentTimeMillis();
            if (g.last != null && now > g.lastAt) {                         // le double bouge comme la cible
                double speed = SpellFx.horizontalDistance(g.last, feet) / ((now - g.lastAt) / 1000.0);
                String anim = speed > 4.5 ? "Run" : speed > 0.6 ? "Walk" : "Idle";
                if (!anim.equals(g.anim)) {
                    g.anim = anim;
                    try { AnimationUtils.playAnimation(g.ref, AnimationSlot.Movement, anim, true, store); } catch (Exception ignored) { }
                }
            }
            g.last = new Vector3d(feet);
            g.lastAt = now;
            if (!model.getModelAssetId().equals(g.model) || g.scale > maxScale || g.scale < maxScale * 0.4) {
                g.model = model.getModelAssetId();
                g.scale = maxScale * 0.85;
                SpellFx.setModelScale(g.ref, g.model, (float) (g.scale * Math.max(0.01f, model.getScale())), 1.0, cb);
            }
            Vector3d at = new Vector3d(eye).add(new Vector3d(feet).sub(eye).mul(g.scale));
            var t = store.getComponent(e, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
            SpellFx.hold(g.ref, at, t == null ? null : new com.hypixel.hytale.math.vector.Rotation3f(t.getRotation()), store);
        }
        for (var it = ghosts.entrySet().iterator(); it.hasNext(); ) {       // cibles revenues en vue, sorties, mortes
            var en = it.next();
            if (seen.contains(en.getKey())) continue;
            removeGhost(en.getValue(), cb);
            it.remove();
        }
    }

    private static void removeGhost(Ghost g, CommandBuffer<EntityStore> cb) {
        if (g.ref == null) return;
        PrivateVisibility.forget(g.ref);
        SpellFx.remove(g.ref, cb);
        g.ref = null;
    }

    /** Distance des yeux au premier bloc plein et opaque vers la cible ; -1 si rien ne la cache. */
    static double firstWall(com.hypixel.hytale.server.core.universe.world.World world, Vector3d eye, Vector3d target, double d) {
        if (world == null || d < 1.0) return -1;
        Vector3d step = new Vector3d(target).sub(eye).div(d);
        int lx = Integer.MIN_VALUE, ly = 0, lz = 0;
        for (double k = 0.5; k < d - 0.6; k += RAY_STEP) {
            int x = (int) Math.floor(eye.x + step.x * k), y = (int) Math.floor(eye.y + step.y * k), z = (int) Math.floor(eye.z + step.z * k);
            if (x == lx && y == ly && z == lz) continue;
            lx = x; ly = y; lz = z;
            var bt = com.eldanior.system.util.WorldBlockUtil.getBlockType(world, x, y, z);
            if (bt == null) return k;                                       // zone non chargee : on la tient pour cachee
            // seul un bloc plein et opaque cache (herbe, fleurs, feuilles et verre ne comptent pas)
            if (bt.getMaterial() == com.hypixel.hytale.protocol.BlockMaterial.Solid
                    && bt.getOpacity() == com.hypixel.hytale.protocol.Opacity.Solid) return k;
        }
        return -1;
    }

    void cleanup(CommandBuffer<EntityStore> cb) {
        for (Ghost g : ghosts.values()) removeGhost(g, cb);
        ghosts.clear();
    }

    void abort() {
        for (Ghost g : ghosts.values()) { PrivateVisibility.forget(g.ref); RuneSpellRuntime.removeLater(g.ref); }
        ghosts.clear();
    }
}
