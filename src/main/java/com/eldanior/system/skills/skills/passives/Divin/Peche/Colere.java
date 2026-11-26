package com.eldanior.system.skills.skills.passives.Divin.Peche;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skills.passives.Divin.PouvoirsDivins;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Colere (Peche, PK) : +40 % de degats au-dessus de 70 % de vie ; chaque coup recu d'un joueur ajoute une charge de rage
 * (+5 % de degats, 6 charges, 10 s). */
public class Colere implements IPassiveCombatSkill {
    private static final int MAX_CHARGES = 6;
    private static final long DUREE_MS = 10_000L;
    /** Charges de rage : [nombre, fin en ms]. */
    private static final Map<UUID, long[]> RAGE = new ConcurrentHashMap<>();

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (damage.isCancelled()) return false;
        float mult = 1f;
        if (PouvoirsDivins.healthRatio(attackerRef, store) > 0.70f) mult += 0.40f;
        UUID self = PouvoirsDivins.uuid(attackerRef, store);
        long[] rage = self != null ? RAGE.get(self) : null;
        if (rage != null && rage[1] > System.currentTimeMillis()) mult += 0.05f * rage[0];
        if (mult <= 1f) return false;
        damage.setAmount(damage.getAmount() * mult);
        return true;
    }

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (damage.isCancelled() || !PouvoirsDivins.isPlayer(attackerRef, store)) return false;
        UUID self = PouvoirsDivins.uuid(victimRef, store);
        if (self == null) return false;
        long now = System.currentTimeMillis();
        RAGE.compute(self, (k, r) -> {
            int n = r == null || r[1] < now ? 0 : (int) r[0];
            return new long[]{Math.min(MAX_CHARGES, n + 1), now + DUREE_MS};
        });
        return false;
    }

    /** Oublie les charges d'un joueur deconnecte. */
    public static void forget(UUID uuid) { RAGE.remove(uuid); }
}
