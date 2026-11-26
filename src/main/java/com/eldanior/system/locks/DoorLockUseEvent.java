package com.eldanior.system.locks;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3i;

/**
 * Touche d'interaction sur une porte verrouillee. Annuler ici laisse la porte fermee ; sinon l'interaction vanilla l'ouvre.
 * Le mode creatif passe sans cle pour construire ; un admin en aventure suit la regle commune, ce qui permet de tester.
 */
public class DoorLockUseEvent extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    public DoorLockUseEvent() { super(UseBlockEvent.Pre.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl UseBlockEvent.Pre event) {
        if (event.getInteractionType() != InteractionType.Use) return;
        DoorLock lock = DoorLock.of(event.getBlockType());
        if (lock == null) return;
        Ref<EntityStore> ref = event.getContext().getEntity();
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        World world = player != null ? player.getWorld() : null;
        if (pr == null || world == null) {
            event.setCancelled(true);
            return;
        }
        if (player.getGameMode() == GameMode.Creative) return;
        Vector3i door = DoorLockManager.anchor(world, event.getTargetBlock());
        boolean open = DoorLockManager.isOpen(event.getBlockType());
        boolean allowed = lock.family()
                ? openFamilyDoor(lock, world, door, open, player, pr, store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType()))
                : openBossDoor(lock, world, door, open, player, pr);
        if (!allowed) event.setCancelled(true);
    }

    /** Il faut etre le Patriarche de cette famille et avoir sa cle sur soi. */
    private static boolean openFamilyDoor(DoorLock lock, World world, Vector3i door, boolean open, Player player, PlayerRef pr, PlayerLevelData data) {
        NobleFamilyModel family = FamilyManager.get(lock.code());
        String name = family != null ? family.getDisplayName() : lock.code();
        if (data == null || !data.isPatriarch() || !lock.code().equals(data.getNobleFamilyId())) {
            DoorLockManager.denied(pr, "Seul le Patriarche de la famille " + name + " détient cette clé.");
            return false;
        }
        if (!DoorLockManager.hasKey(player, lock.keyItem())) {
            DoorLockManager.denied(pr, "Il vous faut la Clé du Trésor de la famille " + name + ".");
            return false;
        }
        if (!open) DoorLockManager.unlocked(world, door, false);
        return true;
    }

    /** Une cle est consommee par ouverture, et la porte reste ouverte pour tout le groupe : personne ne peut la rabattre. */
    private static boolean openBossDoor(DoorLock lock, World world, Vector3i door, boolean open, Player player, PlayerRef pr) {
        if (open) return false;
        if (DoorLockManager.isUnlocked(world, door)) return true;
        // 2026-11-19 : porte d'un donjon a boss. Boss abattu (pas encore revenu) : la porte s'ouvre sans cle (sortir, visiter).
        var gate = com.eldanior.system.boards.DungeonBosses.gatedBy(world.getName(), door);
        if (com.eldanior.system.boards.DungeonBosses.allResting(gate)) {
            DoorLockManager.unlocked(world, door, true);
            NotificationHelper.sendNotification(pr, "Le boss de ce donjon a été vaincu : la porte s'ouvre sans clé. Il revient dans "
                    + Math.max(1, com.eldanior.system.boards.DungeonBosses.restLeftMs(gate) / 60_000 + 1) + " min.");
            return true;
        }
        if (!DoorLockManager.takeKey(player, lock.keyItem())) {
            DoorLockManager.denied(pr, "Il faut une Clé de Boss de rang " + lock.tierLabel() + ".");
            return false;
        }
        DoorLockManager.unlocked(world, door, true);
        NotificationHelper.sendSuccess(pr, "La Clé de Boss se brise dans la serrure.");
        if (!gate.isEmpty()) {                                       // le boss du donjon apparait, fige jusqu'a la fermeture (15 s)
            java.util.UUID opener;
            try { opener = com.eldanior.system.config.UUIDExtractor.getUUID(pr); } catch (Exception e) { opener = null; }
            com.eldanior.system.boards.DungeonBosses.doorOpened(world, gate, opener, pr.getUsername());
        }
        return true;
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
