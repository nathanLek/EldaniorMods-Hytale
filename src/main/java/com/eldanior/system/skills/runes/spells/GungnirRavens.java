package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Hugin et Munin, les corbeaux d'Odin : tant qu'un joueur tient Gungnir, deux corbeaux tournent au-dessus de lui
 * (modeles deplaces par le code, Projectile_Config_Gungnir_Raven) et ses coups ont +10 % de chance d'etre critiques
 * (CombatStatsSystem). Ils disparaissent quand il range la lance ou se deconnecte. Jamais sauvegardes avec le monde ; les
 * corbeaux orphelins (deconnexion en solo, anciennes sauvegardes) sont retires autour des joueurs.
 */
public class GungnirRavens extends EntityTickingSystem<EntityStore> {

    private static final String RAVEN = "Projectile_Config_Gungnir_Raven", RAVEN_MODEL = "Gungnir_Raven";
    private static final double ORBIT_RADIUS = 1.5, ORBIT_HEIGHT = 2.3, ORBIT_SPEED = 1.6;   // blocs, blocs, radians par seconde
    private static final float CRIT_CHANCE = 0.10f;
    private static final long RESPAWN_MS = 3_000, SWEEP_MS = 2_000;
    private static final double SWEEP_RADIUS = 16;

    private static final class Pair {
        final Ref<EntityStore>[] ravens;
        final long spawnedAt;
        @SuppressWarnings("unchecked")
        Pair(Ref<EntityStore> a, Ref<EntityStore> b, long at) { ravens = new Ref[]{a, b}; spawnedAt = at; }
    }

    private static final Map<UUID, Pair> ravens = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastSweep = new ConcurrentHashMap<>();

    /** Le joueur tient Gungnir en main. */
    public static boolean holds(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = ref != null && ref.isValid() ? store.getComponent(ref, Player.getComponentType()) : null;
        var inv = player != null ? player.getInventory() : null;
        ItemStack held = inv != null ? inv.getActiveHotbarItem() : null;
        return held != null && !held.isEmpty() && Gungnir.ITEM_ID.equals(held.getItemId());
    }

    /** Coup critique supplementaire de Hugin et Munin (10 %) pour un porteur de Gungnir. */
    public static boolean huginCrit(Ref<EntityStore> attacker, Store<EntityStore> store) {
        return holds(attacker, store) && ThreadLocalRandom.current().nextFloat() < CRIT_CHANCE;
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> me = chunk.getReferenceTo(index);
        UUID uuid = SpellFx.uuid(me, store);
        if (uuid == null) return;
        sweep(uuid, me, store, cb);
        Pair pair = ravens.get(uuid);
        if (!holds(me, store)) {
            if (pair != null) { ravens.remove(uuid); for (Ref<EntityStore> r : pair.ravens) SpellFx.remove(r, cb); }
            return;
        }
        Vector3d at = SpellFx.position(me, store);
        if (at == null) return;
        long now = System.currentTimeMillis();
        if (pair == null || (now - pair.spawnedAt > RESPAWN_MS && (!pair.ravens[0].isValid() || !pair.ravens[1].isValid()))) {
            if (pair != null) for (Ref<EntityStore> r : pair.ravens) SpellFx.remove(r, cb);
            Vector3d above = new Vector3d(at).add(0, ORBIT_HEIGHT, 0);
            ravens.put(uuid, new Pair(spawn(me, above, new Vector3d(1, 0, 0), store, cb), spawn(me, above, new Vector3d(-1, 0, 0), store, cb), now));
            return;
        }
        double base = now / 1000.0 * ORBIT_SPEED;
        for (int i = 0; i < 2; i++) {
            Ref<EntityStore> r = pair.ravens[i];
            if (r == null || !r.isValid()) continue;
            double a = base + i * Math.PI;
            Vector3d p = new Vector3d(at).add(Math.cos(a) * ORBIT_RADIUS, ORBIT_HEIGHT + 0.2 * Math.sin(2 * a + i), Math.sin(a) * ORBIT_RADIUS);
            SpellFx.hold(r, p, SpellFx.facing(new Vector3d(-Math.sin(a), 0, Math.cos(a)), false), store);   // vol tangent au cercle
        }
    }

    /** Un corbeau, jamais sauvegarde avec le monde (sinon il reviendrait en double a chaque reconnexion). */
    private static Ref<EntityStore> spawn(Ref<EntityStore> owner, Vector3d at, Vector3d dir, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> raven = SpellFx.spawnModel(owner, RAVEN, at, dir, cb);
        SpellFx.notSaved(raven, store, cb);
        return raven;
    }

    /** Toutes les 2 s : retire les corbeaux de Gungnir autour du joueur qui n'appartiennent a aucun porteur actif. */
    private static void sweep(UUID uuid, Ref<EntityStore> me, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        long now = System.currentTimeMillis();
        Long last = lastSweep.get(uuid);
        if (last != null && now - last < SWEEP_MS) return;
        lastSweep.put(uuid, now);
        Vector3d at = SpellFx.position(me, store);
        if (at == null) return;
        Set<Ref<EntityStore>> owned = new HashSet<>();
        for (Pair p : ravens.values()) owned.addAll(Arrays.asList(p.ravens));
        for (Ref<EntityStore> e : new ArrayList<>(TargetUtil.getAllEntitiesInSphere(at, SWEEP_RADIUS, store))) {
            if (e == null || !e.isValid() || owned.contains(e)) continue;
            ModelComponent model = store.getComponent(e, ModelComponent.getComponentType());
            if (model != null && model.getModel() != null && RAVEN_MODEL.equals(model.getModel().getModelAssetId())) SpellFx.remove(e, cb);
        }
    }

    /** Deconnexion : les corbeaux seront retires au prochain tick d'un joueur du monde (sinon par sweep a son retour). */
    public static void handleDisconnect(UUID uuid) {
        lastSweep.remove(uuid);
        Pair pair = ravens.remove(uuid);
        if (pair != null) for (Ref<EntityStore> r : pair.ravens) RuneSpellRuntime.removeLater(r);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
