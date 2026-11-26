package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * /es ville (joueurs, 2026-11-19) : ouvre le registre de la ville dont le joueur est le Comte (celle ou il se tient, sinon la
 * premiere), la meme fenetre que la borne de la ville et le Bailli. Admin : la ville ou il se tient.
 */
public class CityCommand extends AbstractAsyncCommand {

    public CityCommand() {
        super("ville", "Registre de votre ville (Comte) : trésor, règles, lois, biens, Bailli");
        requireNoPermission();
    }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        var ref = ctx.senderAsPlayerRef();
        if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);
        var store = ref.getStore();
        var world = store.getExternalData().getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);
        world.execute(() -> {
            try {
                PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
                Player player = store.getComponent(ref, Player.getComponentType());
                if (pr == null || player == null) return;
                boolean admin = pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION);
                TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
                ParcelData here = t != null ? CityStewards.cityAt(world.getName(), t.getPosition().x, t.getPosition().y, t.getPosition().z) : null;
                List<ParcelData> mine = new ArrayList<>();
                for (ParcelData p : ParcelManager.getByType(ParcelType.CITY)) if (p.isOwner(pr.getUuid())) mine.add(p);
                ParcelData city = here != null && (admin || here.isOwner(pr.getUuid())) ? here : mine.isEmpty() ? null : mine.get(0);
                if (city == null) {
                    pr.sendMessage(Message.raw("Vous n'êtes le Comte d'aucune ville. Les villes s'acquièrent chez l'intendant de la maison qui tient "
                            + "le Duché (Domaines › Terres à acquérir).").color("#e8c060"));
                    return;
                }
                player.getPageManager().openCustomPage(ref, store, IntendantPage.forCity(pr, city, null));
            } catch (Exception e) {
                EldaniorLogger.error("CityCommand", e);
            }
        });
        return CompletableFuture.completedFuture(null);
    }
}
