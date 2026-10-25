package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Hugin et Munin, les corbeaux d'Odin : tant qu'un joueur tient Gungnir, deux corbeaux tournent au-dessus de lui
 * (modeles deplaces par le code, Projectile_Config_Gungnir_Raven) et ses coups ont +10 % de chance d'etre critiques
 * (CombatStatsSystem). Ils disparaissent quand il range la lance ou se deconnecte.
 */
public class GungnirRavens extends EntityTickingSystem<EntityStore> {

    private static final String RAVEN = "Projectile_Config_Gungnir_Raven";
    private static final double ORBIT_RADIUS = 1.5, ORBIT_HEIGHT = 2.3, ORBIT_SPEED = 1.6;   // blocs, blocs, radians par seconde
    private static final float CRIT_CHANCE = 0.10f;
    private static final long RESPAWN_MS = 3_000;

    private static final class Pair {
        final Ref<EntityStore>[] ravens;
        final long spawnedAt;
        @SuppressWarnings("unchecked")
        Pair(Ref<EntityStore> a, Ref<EntityStore> b, long at) { ravens = new Ref[]{a, b}; spawnedAt = at; }
    }

    private static final Map<UUID, Pair> ravens = new ConcurrentHashMap<>();

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
            ravens.put(uuid, new Pair(SpellFx.spawnModel(me, RAVEN, above, new Vector3d(1, 0, 0), cb),
                    SpellFx.spawnModel(me, RAVEN, above, new Vector3d(-1, 0, 0), cb), now));
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

    /** Deconnexion : les corbeaux seront retires au prochain tick d'un joueur du monde. */
    public static void handleDisconnect(UUID uuid) {
        Pair pair = ravens.remove(uuid);
        if (pair != null) for (Ref<EntityStore> r : pair.ravens) RuneSpellRuntime.removeLater(r);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
