package com.eldanior.system.Leveling.utils;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/**
 * Perte totale d'un personnage (sorti de guards.BankLoans le 2026-11-16 pour etre partage) : comme le serment de la voie
 * martiale, mais sans rien garder (niveau 1, Novice, caracteristiques de depart, compte vide, famille, guilde, titres, quetes,
 * inventaire entier vide, runes), puis le point d'apparition du monde. Utilise par la banque (debiteur en defaut mort) et
 * par l'execution d'un Observateur (classes.observateur). Thread du monde du joueur. Retourne false si rien n'a ete fait.
 */
public final class CharacterWipe {

    private CharacterWipe() {}

    public static boolean wipe(PlayerRef pr, Ref<EntityStore> ref, Store<EntityStore> store, World world) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        UUID uuid = pr.getUuid();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return false;
        if (data.getNobleFamilyId() != null && !data.getNobleFamilyId().isEmpty()) {
            try { com.eldanior.system.gui.tabs.FamilleTab.handleLeave(ref, store); } catch (Exception e) { EldaniorLogger.error("[Remise a zero] famille", e); }
            com.eldanior.system.titles.nobility.family.FamilyManager.removeElderEverywhere(uuid);
        }
        data = store.getComponent(ref, type);
        if (data == null) return false;
        if (data.hasGuild()) {
            try {
                var guild = com.eldanior.system.guild.GuildManager.getPlayerGuild(uuid);
                if (data.isGuildChef() && guild != null && guild.getMemberCount() <= 1) com.eldanior.system.guild.GuildManager.disbandGuild(guild.getId());
                com.eldanior.system.guild.GuildManager.leaveGuild(uuid);
            } catch (Exception e) { EldaniorLogger.error("[Remise a zero] guilde", e); }
        }
        String oldRank = data.getNobilityRank();
        if (oldRank != null && !"ROTURIER".equals(oldRank) && !"CHEVALIER".equals(oldRank) && !"ROI".equals(oldRank)) {
            var rank = com.eldanior.system.titles.nobility.NobilityRank.fromString(oldRank);
            if (rank != null) com.eldanior.system.titles.nobility.NobilityManager.unrecordKingPromotion(rank);
        }
        PlayerLevelData d = (PlayerLevelData) data.clone();
        d.setStrength(1);
        d.setVitality(1);
        d.setIntelligence(1);
        d.setEndurance(1);
        d.setAgility(1);
        d.setLuck(1);
        d.setAttributePoints(0);
        d.setLevel(1);
        d.setExperience(0);
        d.setPlayerClass("Novice");
        d.setPlayerClassId("novice");
        d.forgetAllSkills();
        d.clearSavedEvolutionChoices();
        d.setEvolutionRerolls(0);
        d.setCurrentMana(-1);
        d.setActiveSkillId("");
        d.setDivineSkill("");
        d.clearSkillProcs();
        d.setNobleFamilyId("");
        d.setStatus("");
        d.setNobilityRank("ROTURIER");
        d.setDignity(0);
        d.setChurchRank("LAIQUE");
        d.setFaith(0);
        d.setGuildId("");
        d.setGuildRole("");
        d.setMoney(0);
        d.resetTitles();
        d.setPlayerKills(0);
        d.setPlayerDeaths(0);
        d.setKillStreak(0);
        d.setBestKillStreak(0);
        d.setForcePK(false);
        d.setQuestData("");
        d.setCooldownData("");
        d.clearDuelHistory();
        d.setLastVictimUUID(null);
        d.setHauntingThrustStacks(0);
        d.collectBounty();
        store.putComponent(ref, type, d);
        com.eldanior.system.quest.QuestManager.unregisterBounty(uuid);
        try { com.eldanior.system.quest.QuestManager.getPlayerQuests(uuid).clear(); } catch (Exception e) { EldaniorLogger.error("[Remise a zero] quetes", e); }
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null && player.getInventory() != null) {                         // tout l'inventaire : barre, sac, armure, outils
            var inv = player.getInventory();
            for (ItemContainer c : new ItemContainer[]{inv.getCombinedArmorHotbarUtilityStorage(), inv.getCombinedBackpackStorageHotbar(), inv.getTools()}) {
                if (c == null) continue;
                for (short s = 0; s < c.getCapacity(); s++) {
                    ItemStack st = c.getItemStack(s);
                    if (st != null && !st.isEmpty()) c.removeItemStackFromSlot(s);
                }
            }
        }
        com.eldanior.system.skills.runes.RuneManager.wipeAllRunes(ref, store);
        com.eldanior.system.Leveling.utils.StatCalculator.updatePlayerStats(ref, store, d);
        try {
            Transform[] spawns = world.getWorldConfig().getSpawnProvider().getSpawnPoints();
            if (spawns != null && spawns.length > 0 && spawns[0] != null)
                store.addComponent(ref, Teleport.getComponentType(), Teleport.createForPlayer(spawns[0]));
        } catch (Exception e) { EldaniorLogger.error("[Remise a zero] retour au point d'apparition", e); }
        return true;
    }
}
