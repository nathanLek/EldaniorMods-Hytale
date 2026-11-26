package com.eldanior.system.Leveling.commands;

import com.eldanior.system.Leveling.systems.CombatLog;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/** /es combat log (admin) : active ou coupe le journal de combat dans le chat (CombatLog, 2026-11-25). */
public class CombatLogCommand extends AbstractCommandCollection {

    public CombatLogCommand() {
        super("combat", "Combat (admin) : log = journal des coups dans le chat");
        this.addSubCommand(new Log());
    }

    private static final class Log extends AbstractAsyncCommand {
        Log() { super("log", "Active ou coupe le journal de combat (esquive, Force, critique, defense)"); }

        @Nonnull
        @Override
        public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
            Ref<EntityStore> ref = ctx.senderAsPlayerRef();
            if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            if (world == null) return CompletableFuture.completedFuture(null);
            world.execute(() -> {
                try {
                    PlayerRef sender = store.getComponent(ref, PlayerRef.getComponentType());
                    UUIDComponent uuid = store.getComponent(ref, UUIDComponent.getComponentType());
                    if (sender == null || uuid == null) return;
                    if (!sender.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) { sender.sendMessage(Message.raw("Permission refusee.")); return; }
                    boolean on = CombatLog.toggle(uuid.getUuid());
                    sender.sendMessage(Message.raw(on
                            ? "Journal de combat ACTIVÉ : chaque coup donné ou reçu s'affiche ici (avant armure). /es combat log pour couper."
                            : "Journal de combat coupé."));
                } catch (Exception e) {
                    EldaniorLogger.error("CombatLogCommand", e);
                }
            });
            return CompletableFuture.completedFuture(null);
        }
    }
}
