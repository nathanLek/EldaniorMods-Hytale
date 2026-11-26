package com.eldanior.system.boards;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.UpdateBossBar;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Barre de boss du jeu (paquet UpdateBossBar, comme les rencontres vanilla ; vue fonctionner en jeu le 2026-11-16) d'un monstre :
 * montree aux joueurs a moins de {@link #RANGE} blocs, retiree a ceux qui s'eloignent. Le client suit lui-meme la vie du monstre
 * (identifiant reseau). Utilisee par les monstres etoiles (MonsterContracts) et les boss de donjon (DungeonBosses).
 */
public final class BossBarView {

    static final double RANGE = 48.0;

    private final Set<UUID> shown = ConcurrentHashMap.newKeySet();
    private volatile int netId = -1;

    /** Thread du monde : montre ou retire la barre selon la distance de chaque joueur. hard : barre « difficile ». */
    public void update(Ref<EntityStore> ref, Store<EntityStore> store, World world, String title, boolean hard) {
        NetworkId net = store.getComponent(ref, NetworkId.getComponentType());
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        if (net == null || t == null) return;
        if (netId != net.getId()) { hide(); netId = net.getId(); }
        Vector3d at = t.getPosition();
        UpdateBossBar show = new UpdateBossBar(netId, Message.raw(title).getFormattedMessage(), false, hard);
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> pref = pr.getReference();
            boolean here = pref != null && pref.isValid() && pref.getStore().getExternalData().getWorld() == world;
            Vector3d pos = here ? com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid()) : null;
            boolean near = pos != null && pos.distanceSquared(at) <= RANGE * RANGE;
            if (near && shown.add(pr.getUuid())) pr.getPacketHandler().writeNoCache(show);
            else if (!near && shown.remove(pr.getUuid())) hideFor(pr, netId);
        }
    }

    /** Retire la barre a tous ceux qui la voient (monstre abattu, disparu, retire). */
    public void hide() {
        if (shown.isEmpty()) return;
        for (PlayerRef pr : Universe.get().getPlayers()) if (shown.contains(pr.getUuid())) hideFor(pr, netId);
        shown.clear();
    }

    private static void hideFor(PlayerRef pr, int netId) {
        try {
            pr.getPacketHandler().writeNoCache(new UpdateBossBar(netId, null, true, false));
        } catch (Exception ignored) { /* joueur deconnecte */ }
    }
}
