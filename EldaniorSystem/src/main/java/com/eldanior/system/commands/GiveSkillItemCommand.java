package com.eldanior.system.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.rpg.classes.skills.Skills.SkillManager;
import com.eldanior.system.rpg.classes.skills.Skills.SkillModel;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import org.bson.BsonDocument;
import org.bson.BsonString;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GiveSkillItemCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> skillIdArg;

    public GiveSkillItemCommand() {
        super("giveskill", "Donner l'item d'une compétence à un joueur");
        // Arguments requis : Joueur + ID du Skill
        this.playerArg = this.withRequiredArg("joueur", "Nom du joueur", ArgTypes.STRING);
        this.skillIdArg = this.withRequiredArg("skillId", "ID du skill (ex: aura_dragonic_divin)", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return true; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        // 1. Vérification Permission
        if (!sender.hasPermission("eldanior.command.giveskill")) {
            sender.sendMessage(Message.raw("§cErreur : Pas de permission."));
            return CompletableFuture.completedFuture(null);
        }

        // 2. Récupération des arguments
        String targetName = this.playerArg.get(ctx);
        String skillId = this.skillIdArg.get(ctx);

        // 3. Vérification du Skill
        SkillModel skill = SkillManager.get(skillId);
        if (skill == null) {
            sender.sendMessage(Message.raw("§cErreur : Skill '" + skillId + "' introuvable."));
            return CompletableFuture.completedFuture(null);
        }

        // 4. Recherche du joueur (Ticket)
        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) {
            sender.sendMessage(Message.raw("§cErreur : Joueur '" + targetName + "' introuvable."));
            return CompletableFuture.completedFuture(null);
        }

        // 5. Exécution sur le Thread Principal
        assert sender.getWorld() != null;
        return CompletableFuture.runAsync(() -> {
            try {
                // --- REFLEXION POUR UUID (Méthode Robuste) ---
                Field uuidField = PlayerRef.class.getDeclaredField("uuid");
                uuidField.setAccessible(true);
                UUID targetUUID = (UUID) uuidField.get(targetRef);

                // On s'assure d'avoir le joueur connecté
                PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
                if (targetPlayer == null) {
                    sender.sendMessage(Message.raw("§cErreur : Le joueur doit être connecté."));
                    return;
                }

                // Récupération de l'entité ECS réelle pour l'inventaire
                var ref = targetPlayer.getReference();
                if (ref == null) return;
                Player entityPlayer = ref.getStore().getComponent(ref, Player.getComponentType());

                if (entityPlayer == null) return;

                // 1. Définir les métadonnées (Pour que le SkillSystem reconnaisse le skill)
                BsonDocument metadata = new BsonDocument();
                metadata.put(EldaniorSystem.getSkillIdKey().getKey(), new BsonString(skill.getId()));
                // On force le nom pour être sûr
                metadata.put("displayName", new BsonString("§c" + skill.getName()));

                // 2. Créer l'item avec le BON ID (Namespace + ID du JSON)
                // Essaye d'abord avec le namespace
                String itemId = "eldaniorsystem:wand_fireball";

                // SÉCURITÉ : Si l'item custom n'est pas encore chargé, on se rabat sur un bâton
                ItemStack stack;
                try {
                    stack = new ItemStack(itemId, 1, metadata);
                } catch (Exception e) {
                    // Fallback si le JSON est mal lu
                    stack = new ItemStack("Aura_Dragonic", 1, metadata);
                    sender.sendMessage(Message.raw("§eAttention: Item custom non trouvé, fallback sur Bâton."));
                }

                Player p = ref.getStore().getComponent(ref, Player.getComponentType());
                if (p != null) {
                    p.getInventory().getHotbar().addItemStack(stack);
                }

                sender.sendMessage(Message.raw("§aItem donné : " + skill.getName()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, sender.getWorld());
    }
}