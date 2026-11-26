package com.eldanior.system.luo;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.definitions.luo.VoieMartiale;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.runes.RuneManager;
import com.eldanior.system.skills.runes.spells.RuneSpell;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Serment de la Voie Martiale (Voie/VoieMartiale.ui), ouvert par le Rouleau de la Voie Martiale
 * (Eldanior_Parchemin_Voie_Martiale, scripts/familles/gen_voie_martiale_objets.py). Seule entree dans la voie, ouverte a tous
 * (retours de test du 2026-11-09/10) : « Preter serment » fait TOUT perdre sauf 5 % des caracteristiques (au moins 1) :
 * niveau 1, experience, points a repartir, classe (Apprenti en Arts Martiaux), competences et runes (l'Aura revient avec la
 * dignite), famille et rang noble (FamilleTab.handleLeave : succession du Patriarche), guilde, argent (1000), barre d'objets,
 * titres, quetes et statistiques PvP, eglise. Un chef de guilde qui a encore des membres doit d'abord transmettre son role.
 * Le rouleau se consume, puis les manuels de l'Apprenti (Ki de l'Epee, Meditation martiale, Paume de Ki) sont remis.
 */
public class VoieMartialePage extends InteractiveCustomUIPage<VoieMartialePage.Data> {

    public static final String SCROLL = "Eldanior_Parchemin_Voie_Martiale";

    public VoieMartialePage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
    }

    /** Clic droit sur le rouleau : verifications, puis la fenetre de serment. */
    public static final class Parchemin implements RuneSpell {
        @Override public String id() { return "VoieMartialeParchemin"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Player player = store.getComponent(caster, Player.getComponentType());
            PlayerRef pr = store.getComponent(caster, PlayerRef.getComponentType());
            PlayerLevelData data = store.getComponent(caster, EldaniorSystem.get().getPlayerLevelDataType());
            if (player == null || pr == null || data == null) return null;
            if (VoieMartiale.isMartial(data.getPlayerClassId())) {
                NotificationHelper.sendNotification(pr, "<color:gray>Vous suivez déjà la voie martiale.</color>", NotificationStyle.Default);
                return null;
            }
            if (guildBlocks(caster, store, data)) {
                NotificationHelper.sendNotification(pr, "<color:red>Transférez le rôle de chef de votre guilde ou dissolvez-la avant de prêter serment.</color>",
                        NotificationStyle.Warning);
                return null;
            }
            player.getPageManager().openCustomPage(caster, store, new VoieMartialePage(pr));
            return null;
        }
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Voie/VoieMartiale.ui");
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        ClassModel current = data == null ? null : ClassManager.get(data.getPlayerClassId());
        String who = data == null ? "" : "Vous êtes aujourd'hui " + (current != null ? current.getDisplayName() : "sans classe")
                + " de niveau " + data.getLevel() + ". ";
        ui.set("#VoieText.Text", "Le dojo du Clan Luo n'enseigne la voie martiale qu'à ceux qui ont tout laissé derrière eux. " + who
                + "En prêtant serment, vous devenez Apprenti en Arts Martiaux et apprenez la garde, le souffle et le Ki : "
                + "les techniques s'apprennent ensuite dans les manuels secrets du dojo.");
        ui.set("#VoieWarn.Text", "Vous perdrez tout : niveau, expérience, classe, compétences, runes, famille et rang, guilde, "
                + "argent, barre d'objets, titres et quêtes. Ce choix est définitif.");
        ui.set("#VoieKeep.Text", "Vous ne gardez que 5 % de vos caractéristiques, et les objets de votre sac.");
        events.addEventBinding(CustomUIEventBindingType.Activating, "#VoieAccept", EventData.of("Action", "accept"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#VoieCancel", EventData.of("Action", "cancel"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        if ("accept".equals(data.action)) {
            try { swear(ref, store); } catch (Exception e) { EldaniorLogger.error("VoieMartiale: serment", e); }
        }
        close();
    }

    /** Le serment : rouleau consume, personnage remis a zero en Apprenti en Arts Martiaux, manuels de l'Apprenti remis. */
    private void swear(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        Player player = store.getComponent(ref, Player.getComponentType());
        ClassModel apprentice = ClassManager.get(VoieMartiale.FIRST);
        if (data == null || player == null || apprentice == null || VoieMartiale.isMartial(data.getPlayerClassId())) return;
        if (guildBlocks(ref, store, data)) {
            NotificationHelper.sendNotification(playerRef, "<color:red>Transférez le rôle de chef de votre guilde ou dissolvez-la avant de prêter serment.</color>",
                    NotificationStyle.Warning);
            return;
        }
        if (!takeScroll(player)) {
            NotificationHelper.sendNotification(playerRef, "<color:red>Le Rouleau de la Voie Martiale n'est plus entre vos mains.</color>",
                    NotificationStyle.Warning);
            return;
        }
        java.util.UUID uuid = playerRef.getUuid();
        // famille (succession du Patriarche comme « Quitter » de l'onglet Famille) et guilde
        if (data.getNobleFamilyId() != null && !data.getNobleFamilyId().isEmpty()) {
            try { com.eldanior.system.gui.tabs.FamilleTab.handleLeave(ref, store); } catch (Exception e) { EldaniorLogger.error("VoieMartiale: famille", e); }
            com.eldanior.system.titles.nobility.family.FamilyManager.removeElderEverywhere(uuid);
        }
        com.eldanior.system.titles.nobility.family.FamilyRegistry.forgetWelcomes(uuid);   // nouvelle famille = nouveau 1er Mouvement
        data = store.getComponent(ref, type);
        if (data == null) return;
        if (data.hasGuild()) {
            try {
                com.eldanior.system.guild.Guild guild = com.eldanior.system.guild.GuildManager.getPlayerGuild(uuid);
                if (data.isGuildChef() && guild != null) com.eldanior.system.guild.GuildManager.disbandGuild(guild.getId());   // seul membre
                com.eldanior.system.guild.GuildManager.leaveGuild(uuid);
            } catch (Exception e) { EldaniorLogger.error("VoieMartiale: guilde", e); }
        }
        String oldRank = data.getNobilityRank();
        if (oldRank != null && !"ROTURIER".equals(oldRank) && !"CHEVALIER".equals(oldRank) && !"ROI".equals(oldRank)) {
            var rank = com.eldanior.system.titles.nobility.NobilityRank.fromString(oldRank);
            if (rank != null) com.eldanior.system.titles.nobility.NobilityManager.unrecordKingPromotion(rank);
        }
        PlayerLevelData d = (PlayerLevelData) data.clone();
        if (d == null) return;
        // progression : seuls 5 % des caracteristiques restent
        d.setStrength(keep(d.getStrength()));
        d.setVitality(keep(d.getVitality()));
        d.setIntelligence(keep(d.getIntelligence()));
        d.setEndurance(keep(d.getEndurance()));
        d.setAgility(keep(d.getAgility()));
        d.setLuck(keep(d.getLuck()));
        d.setAttributePoints(0);
        d.setLevel(1);
        d.setExperience(0);
        d.setPlayerClass(apprentice.getDisplayName());
        d.setPlayerClassId(apprentice.getId());
        d.forgetAllSkills();
        d.clearSavedEvolutionChoices();
        d.setEvolutionRerolls(0);
        d.setCurrentMana(-1);
        d.setActiveSkillId("");
        d.setDivineSkill("");
        d.clearSkillProcs();
        // famille, noblesse, eglise, guilde, argent
        d.setNobleFamilyId("");
        d.setStatus("");
        d.setNobilityRank("ROTURIER");
        d.setDignity(0);
        d.setChurchRank("LAIQUE");
        d.setFaith(0);
        d.setGuildId("");
        d.setGuildRole("");
        d.setMoney(1000);
        // titres, quetes, statistiques de combat
        d.resetTitles();
        d.setPlayerKills(0);
        d.setPlayerDeaths(0);
        d.setKillStreak(0);
        d.setBestKillStreak(0);
        d.setChestsDiscovered(0);
        d.setForcePK(false);
        d.setQuestData("");
        d.setCooldownData("");
        d.clearDuelHistory();
        d.setLastVictimUUID(null);
        d.setHauntingThrustStacks(0);
        d.collectBounty();
        store.putComponent(ref, type, d);
        data = d;
        try { com.eldanior.system.quest.QuestManager.getPlayerQuests(uuid).clear(); } catch (Exception e) { EldaniorLogger.error("VoieMartiale: quetes", e); }
        clearHotbar(ref, store);
        RuneManager.wipeAllRunes(ref, store);
        StatCalculator.updatePlayerStats(ref, store, data);
        RuneManager.grantClassRunes(ref, store, data);                  // manuels : Ki de l'Epee, Meditation, Paume de Ki
        NotificationHelper.sendNotification(playerRef, "<color:gold>Vous avez prêté serment : Apprenti en Arts Martiaux.</color> "
                + "<color:gray>Lisez vos manuels secrets (clic droit maintenu) pour apprendre leurs techniques.</color>", NotificationStyle.Success);
    }

    /** 5 % d'une caracteristique, au moins 1. */
    private static int keep(int value) { return Math.max(1, Math.round(value * 0.05f)); }

    /** true si le joueur est chef d'une guilde qui a encore d'autres membres (il doit transmettre son role avant). */
    private static boolean guildBlocks(Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData data) {
        if (data == null || !data.hasGuild() || !data.isGuildChef()) return false;
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        var guild = pr == null ? null : com.eldanior.system.guild.GuildManager.getPlayerGuild(pr.getUuid());
        return guild != null && guild.getMemberCount() > 1;
    }

    /** Vide la barre d'objets (comme la remise a zero admin). */
    private static void clearHotbar(Ref<EntityStore> ref, Store<EntityStore> store) {
        var hotbar = store.getComponent(ref, com.hypixel.hytale.server.core.inventory.InventoryComponent.Hotbar.getComponentType());
        if (hotbar == null) return;
        ItemContainer c = hotbar.getInventory();
        for (short s = 0; s < c.getCapacity(); s++) {
            ItemStack st = c.getItemStack(s);
            if (st != null && !st.isEmpty()) c.removeItemStackFromSlot(s);
        }
        hotbar.markDirty();
    }

    /** Retire un rouleau de l'inventaire (barre d'abord). */
    private static boolean takeScroll(Player player) {
        if (player.getInventory() == null) return false;
        ItemContainer all = player.getInventory().getCombinedHotbarFirst();
        for (short s = 0; s < all.getCapacity(); s++) {
            ItemStack st = all.getItemStack(s);
            if (st == null || st.isEmpty() || !SCROLL.equals(st.getItemId())) continue;
            if (st.getQuantity() > 1) all.setItemStackForSlot(s, st.withQuantity(st.getQuantity() - 1));
            else all.removeItemStackFromSlot(s);
            return true;
        }
        return false;
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .build();
        public String action;
    }
}
