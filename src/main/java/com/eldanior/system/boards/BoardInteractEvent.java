package com.eldanior.system.boards;

import com.eldanior.system.guards.BountyHunter;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3d;
import org.joml.Vector3i;

/**
 * Touche d'interaction sur un tableau : ouvre sa fenetre (BoardScreen). Le bloc declare une interaction Use sans effet
 * ("Simple") : sans elle le moteur n'emet pas UseBlockEvent. Lieu affiche : la ville (ou le territoire, le royaume) du
 * tableau, sinon sa parcelle.
 */
public class BoardInteractEvent extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    public BoardInteractEvent() { super(UseBlockEvent.Pre.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl UseBlockEvent.Pre event) {
        if (event.getInteractionType() != InteractionType.Use || event.getBlockType() == null) return;
        BoardType type = BoardType.of(event.getBlockType().getId());
        if (type == null) return;
        Ref<EntityStore> ref = event.getContext().getEntity();
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        World world = player != null ? player.getWorld() : null;
        if (pr == null || world == null) return;
        Vector3i at = event.getTargetBlock();
        Vector3d pos = new Vector3d(at.x() + 0.5, at.y() + 0.5, at.z() + 0.5);
        ParcelData town = BountyHunter.treasuryAt(world.getName(), pos);
        ParcelData parcel = town != null ? town : ParcelManager.getParcelAt(world.getName(), at.x(), at.y(), at.z());
        String place = parcel != null ? parcel.getName() : null;
        BoardScreen screen = switch (type) {
            case PRIME -> {                                                               // 2026-11-13 : primes et contrats en cartes
                com.eldanior.system.quest.chapitres.QuestChaptersPage.openBountyBoard(ref, store, place, world.getName(), pos,
                        pr.hasPermission(com.eldanior.system.config.EldaniorLogger.ADMIN_PERMISSION));
                yield null;
            }
            case GUILDE -> {                                                              // 2026-11-19 : quetes et missions de guilde en cartes
                com.eldanior.system.quest.chapitres.QuestChaptersPage.openGuildBoard(ref, store, place,
                        pr.hasPermission(com.eldanior.system.config.EldaniorLogger.ADMIN_PERMISSION));
                yield null;
            }
            case MAITRE, INTENDANT, IMMOBILIER -> null;                                   // jamais un bloc
            case QUETES -> {                                                              // 2026-11-13 : quetes en chapitres + primes
                com.eldanior.system.quest.chapitres.QuestChaptersPage.open(ref, store, com.eldanior.system.quest.chapitres.ChapterQuests.TABLEAU,
                        "Tableau des quêtes" + (place != null ? " · " + place : ""), BoardQuests.key(world.getName(), at.x(), at.y(), at.z()),
                        pr.hasPermission(com.eldanior.system.config.EldaniorLogger.ADMIN_PERMISSION));
                yield null;
            }
            case CLASSEMENT -> new RankingBoardScreen(pr, place);
            case DUEL -> new DuelBoardScreen(pr, place);
        };
        if (screen != null) player.getPageManager().openCustomPage(ref, store, screen);
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
