package com.eldanior.system.guild.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.guild.Guild;
import com.eldanior.system.guild.GuildManager;
import com.eldanior.system.guild.GuildRole;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GuildCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;
    private final OptionalArg<String> arg1;

    public GuildCommand() {
        super("guild", "Gestion de guilde (invite/kick/promote/demote/info/leave/accept/decline/recruitment/join)");
        this.actionArg = this.withRequiredArg("action", "invite|kick|promote|demote|info|leave|accept|decline|recruitment|join", ArgTypes.STRING);
        this.arg1 = this.withOptionalArg("arg", "Joueur ou nom de guilde", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        Ref<EntityStore> senderEntityRef = ctx.senderAsPlayerRef();
        if (senderEntityRef == null || !senderEntityRef.isValid()) return CompletableFuture.completedFuture(null);

        Store<EntityStore> senderEntityStore = senderEntityRef.getStore();
        World world = ((EntityStore) senderEntityStore.getExternalData()).getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);

        String action = this.actionArg.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = senderEntityStore.getComponent(senderEntityRef, PlayerRef.getComponentType());
                Player sender = senderEntityStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                switch (action.toLowerCase()) {
                    case "invite" -> handleInvite(sender, ctx);
                    case "kick" -> handleKick(sender, ctx);
                    case "promote" -> handlePromote(sender, ctx);
                    case "demote" -> handleDemote(sender, ctx);
                    case "info" -> handleInfo(sender, ctx);
                    case "leave" -> handleLeave(sender);
                    case "accept" -> handleAccept(sender);
                    case "decline" -> handleDecline(sender);
                    case "recruitment" -> handleRecruitment(sender, ctx);
                    case "join" -> handleJoin(sender, ctx);
                    default -> senderRef.sendMessage(Message.raw("Usage : /es guild <invite|kick|promote|demote|info|leave|accept|decline|recruitment|join> <arg>"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }

    // ==================== INVITE ====================
    private void handleInvite(Player sender, CommandContext ctx) {
        String targetName = this.arg1.get(ctx);
        if (targetName == null || targetName.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("Usage : /es guild invite <joueur>"));
            return;
        }

        try {
            var senderRef = sender.getReference();
            if (senderRef == null) return;
            Store<EntityStore> senderStore = senderRef.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData senderData = senderStore.getComponent(senderRef, type);
            if (senderData == null) return;

            if (!senderData.canInviteToGuild()) {
                sender.getPlayerRef().sendMessage(Message.raw("Seul le Chef ou un Officier peut inviter."));
                return;
            }

            UUID senderUUID = getSenderUUID(sender);
            Guild guild = GuildManager.getPlayerGuild(senderUUID);
            if (guild == null) { sender.getPlayerRef().sendMessage(Message.raw("Vous n'etes dans aucune guilde.")); return; }

            PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
            if (targetRef == null) { sender.getPlayerRef().sendMessage(Message.raw("Joueur introuvable.")); return; }

            UUID targetUUID = extractUUID(targetRef);
            PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
            if (targetPlayer == null) { sender.getPlayerRef().sendMessage(Message.raw("Le joueur doit etre connecte.")); return; }

            var ref = targetPlayer.getReference();
            if (ref == null) return;
            Store<EntityStore> store = ref.getStore();
            PlayerLevelData targetData = store.getComponent(ref, type);
            if (targetData == null) targetData = new PlayerLevelData();

            if (!targetData.canJoinGuild()) {
                if (targetData.hasGuild()) {
                    sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " est deja dans une guilde."));
                } else {
                    sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " fait partie d'une famille noble et ne peut pas rejoindre une guilde."));
                }
                return;
            }

            GuildManager.sendInvite(targetUUID, senderUUID);

            sender.getPlayerRef().sendMessage(Message.raw("Invitation envoyee a " + targetName + " pour rejoindre " + guild.getFormattedName()));
            targetPlayer.sendMessage(Message.raw("" + sender.getPlayerRef().getUsername() + " vous invite a rejoindre la guilde " + guild.getFormattedName()));
            targetPlayer.sendMessage(Message.raw("Tapez /es guild accept pour accepter ou /es guild decline pour refuser."));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== ACCEPT ====================
    private void handleAccept(Player sender) {
        try {
            UUID senderUUID = getSenderUUID(sender);
            if (!GuildManager.hasPendingInvite(senderUUID)) {
                sender.getPlayerRef().sendMessage(Message.raw("Aucune invitation en attente.")); return;
            }

            UUID fromUUID = GuildManager.getPendingInvite(senderUUID);
            Guild guild = GuildManager.getPlayerGuild(fromUUID);
            if (guild == null) { sender.getPlayerRef().sendMessage(Message.raw("La guilde n'existe plus.")); GuildManager.clearInvite(senderUUID); return; }

            var ref = sender.getReference();
            if (ref == null) return;
            Store<EntityStore> store = ref.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = store.getComponent(ref, type);
            if (data == null) return;

            if (!data.canJoinGuild()) {
                sender.getPlayerRef().sendMessage(Message.raw("Vous ne pouvez pas rejoindre de guilde.")); return;
            }

            PlayerLevelData copy = (PlayerLevelData) data.clone();
            if (copy == null) return;
            copy.setGuildId(guild.getId());
            copy.setGuildRole("MEMBER");
            store.putComponent(ref, type, copy);

            GuildManager.joinGuild(senderUUID, guild);
            GuildManager.clearInvite(senderUUID);

            // Verifier titres en temps reel apres avoir rejoint une guilde
            com.eldanior.system.titles.TitleManager.checkAndUnlockTitles(ref, store, copy, sender.getPlayerRef());

            sender.getPlayerRef().sendMessage(Message.raw("Vous avez rejoint la guilde " + guild.getFormattedName() + " !"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== DECLINE ====================
    private void handleDecline(Player sender) {
        try {
            UUID senderUUID = getSenderUUID(sender);
            if (!GuildManager.hasPendingInvite(senderUUID)) {
                sender.getPlayerRef().sendMessage(Message.raw("Aucune invitation en attente.")); return;
            }
            GuildManager.clearInvite(senderUUID);
            sender.getPlayerRef().sendMessage(Message.raw("Invitation refusee."));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== KICK ====================
    private void handleKick(Player sender, CommandContext ctx) {
        String targetName = this.arg1.get(ctx);
        if (targetName == null || targetName.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("Usage : /es guild kick <joueur>"));
            return;
        }

        try {
            UUID senderUUID = getSenderUUID(sender);
            var senderRef = sender.getReference();
            if (senderRef == null) return;
            Store<EntityStore> senderStore = senderRef.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData senderData = senderStore.getComponent(senderRef, type);
            if (senderData == null || !senderData.isGuildChef()) {
                sender.getPlayerRef().sendMessage(Message.raw("Seul le Chef peut exclure un membre.")); return;
            }

            Guild guild = GuildManager.getPlayerGuild(senderUUID);
            if (guild == null) return;

            PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
            if (targetRef == null) { sender.getPlayerRef().sendMessage(Message.raw("Joueur introuvable.")); return; }

            UUID targetUUID = extractUUID(targetRef);
            if (!guild.hasMember(targetUUID)) {
                sender.getPlayerRef().sendMessage(Message.raw("Ce joueur n'est pas dans votre guilde.")); return;
            }

            PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
            if (targetPlayer != null) {
                var ref = targetPlayer.getReference();
                if (ref != null) {
                    Store<EntityStore> store = ref.getStore();
                    PlayerLevelData targetData = store.getComponent(ref, type);
                    if (targetData != null) {
                        PlayerLevelData copy = (PlayerLevelData) targetData.clone();
                        if (copy != null) {
                            copy.setGuildId("");
                            copy.setGuildRole("");
                            store.putComponent(ref, type, copy);
                        }
                    }
                }
                targetPlayer.sendMessage(Message.raw("Vous avez ete exclu de la guilde " + guild.getFormattedName()));
            }

            GuildManager.leaveGuild(targetUUID);
            sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " a ete exclu de la guilde."));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== PROMOTE ====================
    private void handlePromote(Player sender, CommandContext ctx) {
        String targetName = this.arg1.get(ctx);
        if (targetName == null || targetName.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("Usage : /es guild promote <joueur>"));
            return;
        }

        try {
            var senderRef = sender.getReference();
            if (senderRef == null) return;
            Store<EntityStore> senderStore = senderRef.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData senderData = senderStore.getComponent(senderRef, type);
            if (senderData == null || !senderData.isGuildChef()) {
                sender.getPlayerRef().sendMessage(Message.raw("Seul le Chef peut promouvoir.")); return;
            }

            PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
            if (targetRef == null) { sender.getPlayerRef().sendMessage(Message.raw("Joueur introuvable.")); return; }

            UUID targetUUID = extractUUID(targetRef);
            PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
            if (targetPlayer == null) { sender.getPlayerRef().sendMessage(Message.raw("Le joueur doit etre connecte.")); return; }

            var ref = targetPlayer.getReference();
            if (ref == null) return;
            Store<EntityStore> store = ref.getStore();
            PlayerLevelData targetData = store.getComponent(ref, type);
            if (targetData == null) return;

            if (!"MEMBER".equals(targetData.getGuildRole())) {
                sender.getPlayerRef().sendMessage(Message.raw("Ce joueur est deja Officier ou Chef.")); return;
            }

            PlayerLevelData copy = (PlayerLevelData) targetData.clone();
            if (copy == null) return;
            copy.setGuildRole("OFFICER");
            store.putComponent(ref, type, copy);

            // Verifier titres en temps reel apres promotion dans la guilde
            com.eldanior.system.titles.TitleManager.checkAndUnlockTitles(ref, store, copy, targetPlayer);

            sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " est maintenant " + GuildRole.OFFICER.getFormattedName()));
            targetPlayer.sendMessage(Message.raw("Vous etes maintenant " + GuildRole.OFFICER.getFormattedName() + " de votre guilde !"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== DEMOTE ====================
    private void handleDemote(Player sender, CommandContext ctx) {
        String targetName = this.arg1.get(ctx);
        if (targetName == null || targetName.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("Usage : /es guild demote <joueur>"));
            return;
        }

        try {
            var senderRef = sender.getReference();
            if (senderRef == null) return;
            Store<EntityStore> senderStore = senderRef.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData senderData = senderStore.getComponent(senderRef, type);
            if (senderData == null || !senderData.isGuildChef()) {
                sender.getPlayerRef().sendMessage(Message.raw("Seul le Chef peut retrograder.")); return;
            }

            PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
            if (targetRef == null) { sender.getPlayerRef().sendMessage(Message.raw("Joueur introuvable.")); return; }

            UUID targetUUID = extractUUID(targetRef);
            PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
            if (targetPlayer == null) { sender.getPlayerRef().sendMessage(Message.raw("Le joueur doit etre connecte.")); return; }

            var ref = targetPlayer.getReference();
            if (ref == null) return;
            Store<EntityStore> store = ref.getStore();
            PlayerLevelData targetData = store.getComponent(ref, type);
            if (targetData == null) return;

            if (!"OFFICER".equals(targetData.getGuildRole())) {
                sender.getPlayerRef().sendMessage(Message.raw("Ce joueur n'est pas Officier.")); return;
            }

            PlayerLevelData copy = (PlayerLevelData) targetData.clone();
            if (copy == null) return;
            copy.setGuildRole("MEMBER");
            store.putComponent(ref, type, copy);

            sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " retrogade a " + GuildRole.MEMBER.getFormattedName()));
            targetPlayer.sendMessage(Message.raw("Vous avez ete retrogade a " + GuildRole.MEMBER.getFormattedName()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== LEAVE ====================
    private void handleLeave(Player sender) {
        try {
            UUID senderUUID = getSenderUUID(sender);
            var ref = sender.getReference();
            if (ref == null) return;
            Store<EntityStore> store = ref.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = store.getComponent(ref, type);
            if (data == null || !data.hasGuild()) {
                sender.getPlayerRef().sendMessage(Message.raw("Vous n'etes dans aucune guilde.")); return;
            }

            Guild guild = GuildManager.getPlayerGuild(senderUUID);

            if (data.isGuildChef()) {
                if (guild != null && guild.getMemberCount() > 1) {
                    sender.getPlayerRef().sendMessage(Message.raw("Vous devez transferer le role de Chef ou dissoudre la guilde avant de partir."));
                    sender.getPlayerRef().sendMessage(Message.raw("Utilisez /es guilddisband pour dissoudre."));
                    return;
                }
                // Seul membre, on dissout
                if (guild != null) GuildManager.disbandGuild(guild.getId());
            }

            PlayerLevelData copy = (PlayerLevelData) data.clone();
            if (copy == null) return;
            copy.setGuildId("");
            copy.setGuildRole("");
            store.putComponent(ref, type, copy);

            GuildManager.leaveGuild(senderUUID);
            sender.getPlayerRef().sendMessage(Message.raw("Vous avez quitte votre guilde."));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== INFO ====================
    private void handleInfo(Player sender, CommandContext ctx) {
        String guildName = this.arg1.get(ctx);
        if (guildName == null || guildName.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("Usage : /es guild info <nom_guilde>"));
            return;
        }

        Guild guild = GuildManager.getByName(guildName);
        if (guild == null) guild = GuildManager.getByTag(guildName);
        if (guild == null) guild = GuildManager.get(guildName.toLowerCase().replace(" ", "_"));

        if (guild == null) {
            sender.getPlayerRef().sendMessage(Message.raw("Guilde '" + guildName + "' introuvable.")); return;
        }

        sender.getPlayerRef().sendMessage(Message.raw("=== " + guild.getFormattedName() + " " + guild.getFormattedTag() + " ==="));
        sender.getPlayerRef().sendMessage(Message.raw("Fondateur : " + guild.getFounderName()));
        sender.getPlayerRef().sendMessage(Message.raw("Membres : " + guild.getMemberCount()));
        sender.getPlayerRef().sendMessage(Message.raw("Mob Kills : " + guild.getTotalMobKills()));
        sender.getPlayerRef().sendMessage(Message.raw("PvP Kills : " + guild.getTotalPlayerKills()));
        sender.getPlayerRef().sendMessage(Message.raw("Morts : " + guild.getTotalDeaths()));
    }

    // ==================== RECRUITMENT ====================
    private void handleRecruitment(Player sender, CommandContext ctx) {
        String mode = this.arg1.get(ctx);
        if (mode == null || (!mode.equalsIgnoreCase("on") && !mode.equalsIgnoreCase("off"))) {
            sender.getPlayerRef().sendMessage(Message.raw("Usage : /es guild recruitment <on|off>"));
            return;
        }

        try {
            var senderRef = sender.getReference();
            if (senderRef == null) return;
            Store<EntityStore> senderStore = senderRef.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData senderData = senderStore.getComponent(senderRef, type);
            if (senderData == null || !senderData.isGuildChef()) {
                sender.getPlayerRef().sendMessage(Message.raw("Seul le Chef peut changer le mode de recrutement."));
                return;
            }

            UUID senderUUID = getSenderUUID(sender);
            Guild guild = GuildManager.getPlayerGuild(senderUUID);
            if (guild == null) { sender.getPlayerRef().sendMessage(Message.raw("Vous n'etes dans aucune guilde.")); return; }

            boolean open = mode.equalsIgnoreCase("on");
            guild.setOpenRecruitment(open);

            if (open) {
                sender.getPlayerRef().sendMessage(Message.raw("Recrutement ouvert ! Les joueurs peuvent maintenant rejoindre votre guilde librement."));
            } else {
                sender.getPlayerRef().sendMessage(Message.raw("Recrutement ferme. Les joueurs doivent etre invites pour rejoindre."));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== JOIN (open recruitment) ====================
    private void handleJoin(Player sender, CommandContext ctx) {
        String guildName = this.arg1.get(ctx);
        if (guildName == null || guildName.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("Usage : /es guild join <nom_guilde>"));
            return;
        }

        try {
            var senderRef = sender.getReference();
            if (senderRef == null) return;
            Store<EntityStore> senderStore = senderRef.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = senderStore.getComponent(senderRef, type);
            if (data == null) return;

            if (!data.canJoinGuild()) {
                if (data.hasGuild()) {
                    sender.getPlayerRef().sendMessage(Message.raw("Vous etes deja dans une guilde."));
                } else {
                    sender.getPlayerRef().sendMessage(Message.raw("Vous faites partie d'une famille noble et ne pouvez pas rejoindre une guilde."));
                }
                return;
            }

            Guild guild = GuildManager.getByName(guildName);
            if (guild == null) guild = GuildManager.getByTag(guildName);
            if (guild == null) guild = GuildManager.get(guildName.toLowerCase().replace(" ", "_"));

            if (guild == null) {
                sender.getPlayerRef().sendMessage(Message.raw("Guilde '" + guildName + "' introuvable.")); return;
            }

            if (!guild.isOpenRecruitment()) {
                sender.getPlayerRef().sendMessage(Message.raw("Cette guilde n'est pas en recrutement ouvert. Demandez une invitation.")); return;
            }

            UUID senderUUID = getSenderUUID(sender);

            PlayerLevelData copy = (PlayerLevelData) data.clone();
            if (copy == null) return;
            copy.setGuildId(guild.getId());
            copy.setGuildRole("MEMBER");
            senderStore.putComponent(senderRef, type, copy);

            GuildManager.joinGuild(senderUUID, guild);

            // Verifier titres en temps reel
            com.eldanior.system.titles.TitleManager.checkAndUnlockTitles(senderRef, senderStore, copy, sender.getPlayerRef());

            sender.getPlayerRef().sendMessage(Message.raw("Vous avez rejoint la guilde " + guild.getFormattedName() + " !"));

            // Notifier le fondateur s'il est en ligne
            PlayerRef founderRef = Universe.get().getPlayer(guild.getFounderUUID());
            if (founderRef != null) {
                founderRef.sendMessage(Message.raw("" + sender.getPlayerRef().getUsername() + " a rejoint votre guilde via recrutement ouvert."));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== UTILS ====================
    private UUID extractUUID(PlayerRef playerRef) throws Exception {
        return UUIDExtractor.getUUID(playerRef);
    }

    private UUID getSenderUUID(Player sender) throws Exception {
        var ref = sender.getReference();
        if (ref == null) return null;
        Store<EntityStore> store = ref.getStore();
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        return extractUUID(pRef);
    }
}
