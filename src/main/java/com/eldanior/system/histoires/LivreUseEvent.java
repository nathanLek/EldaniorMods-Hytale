package com.eldanior.system.histoires;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Livre pose (histoire ou Tome Imperial) : F ou clic droit dessus ouvre la lecture (2026-11-14). Le clic droit passe par
 * UseBlock avec le type Secondary (chaine Block_Secondary ou Eldanior_Livre_Secondaire), d'ou les deux types acceptes ; le bloc
 * declare Use et Secondary = Simple, sans quoi l'evenement n'est pas emis.
 */
public class LivreUseEvent extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    public LivreUseEvent() { super(UseBlockEvent.Pre.class); }

    public static boolean isLivre(BlockType type) {
        return type != null && Livres.isLivre(type.getId());
    }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl UseBlockEvent.Pre event) {
        InteractionType t = event.getInteractionType();
        if ((t != InteractionType.Use && t != InteractionType.Secondary) || !isLivre(event.getBlockType())) return;
        Ref<EntityStore> ref = event.getContext().getEntity();
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        World world = player != null ? player.getWorld() : null;
        if (pr == null || world == null) return;
        String id = event.getBlockType().getId();
        world.execute(() -> {
            if (ref.isValid()) Livres.open(ref, ref.getStore(), player, pr, id);
        });
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
