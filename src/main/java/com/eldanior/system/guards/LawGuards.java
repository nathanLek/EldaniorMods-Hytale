package com.eldanior.system.guards;

import com.eldanior.system.territory.ParcelData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3d;

import java.util.ArrayList;

/**
 * Gardes au service des lois d'un domaine (2026-11-18, territory/laws) : les gardes postes dans la zone prennent un contrevenant
 * pour cible (frontieres fermees, banni, amende impayee). Meme geste que les gardes Luo envers un etranger sans plaque.
 */
public final class LawGuards {

    private LawGuards() {}

    /** Les gardes postes dans zone, a moins de range blocs de pos, voient target hostile seconds secondes. Nombre de gardes. */
    public static int setOn(Ref<EntityStore> target, Vector3d pos, double range, ParcelData zone, double seconds, Store<EntityStore> store) {
        int n = 0;
        for (Ref<EntityStore> e : new ArrayList<>(TargetUtil.getAllEntitiesInSphere(pos, range, store))) {
            GuardManager.Post p = GuardManager.postOf(e, store);
            if (p == null || !p.type.isGuard()) continue;
            if (zone != null && !zone.contains(p.x, p.y, p.z)) continue;
            LuoNpcs.attack(e, target, seconds, store);
            n++;
        }
        return n;
    }

    /** Ce poste vend-il (commercant, ambulant, marchand) ? Il ferme boutique pendant le couvre-feu. */
    static boolean closesAtCurfew(GuardManager.Post p) {
        return p != null && LuoMerchants.sells(p) && p.type != GuardType.SENSEI && p.type != GuardType.LEGENDAIRE
                && NpcTrades.metier(p) != NpcTrades.Metier.INTENDANT && NpcTrades.metier(p) != NpcTrades.Metier.BANQUIER
                && NpcTrades.metier(p) != NpcTrades.Metier.BAILLI && NpcTrades.metier(p) != NpcTrades.Metier.PASSEUR;
    }
}
