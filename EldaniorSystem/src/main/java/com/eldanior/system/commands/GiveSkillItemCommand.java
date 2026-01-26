package com.eldanior.system.commands;

import com.eldanior.system.rpg.classes.skills.Skills.SkillManager;
import com.eldanior.system.rpg.classes.skills.Skills.SkillModel;
import com.eldanior.system.utils.NotificationHelper;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class GiveSkillItemCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> skillIdArg;

    public GiveSkillItemCommand() {
        super("giveskill", "Donner l'item d'une compétence à un joueur");
        this.playerArg = this.withRequiredArg("joueur", "Nom du joueur", ArgTypes.STRING);
        this.skillIdArg = this.withRequiredArg("skillId", "ID du skill (ex: aura_dragonic_divin)", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return true; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        // Vérification permission
        if (!sender.hasPermission("eldanior.command.giveskill")) {
            sender.sendMessage(Message.raw("§cErreur : Pas de permission."));
            return CompletableFuture.completedFuture(null);
        }

        String targetName = this.playerArg.get(ctx);
        String skillId = this.skillIdArg.get(ctx);

        // Recherche du skill
        SkillModel skill = SkillManager.get(skillId);
        if (skill == null) {
            sender.sendMessage(Message.raw("§cErreur : Skill '" + skillId + "' introuvable."));
            return CompletableFuture.completedFuture(null);
        }

        // Recherche du joueur cible
        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) {
            sender.sendMessage(Message.raw("§cErreur : Joueur '" + targetName + "' introuvable."));
            return CompletableFuture.completedFuture(null);
        }

        // Exécution sur le thread du monde pour manipuler l'inventaire
        assert sender.getWorld() != null;
        return CompletableFuture.runAsync(() -> {
            try {
                // Utilise ton utilitaire pour donner l'item (Slot 8 par défaut dans ton code)
                NotificationHelper.giveSkillItem(targetRef, skill);

                sender.sendMessage(Message.raw("§aSuccès : Item du skill " + skill.getName() + " donné à " + targetName));
                targetRef.sendMessage(Message.raw("§eVous avez reçu l'item du skill : §6" + skill.getName()));
            } catch (Exception e) {
                sender.sendMessage(Message.raw("§cErreur technique lors du don d'item."));
                e.printStackTrace();
            }
        }, sender.getWorld());
    }
}