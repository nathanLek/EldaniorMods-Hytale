package com.eldanior.system.skills.skills.passives.Divin.Peche;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skills.passives.Divin.PouvoirsDivins;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Gourmandise (Peche, PK) : chaque joueur tue rend 20 % de la vie max ; 25 % de chance de copier l'un de ses passifs a
 * son rang (recharge d'un jour, propre a la copie : le soin n'en a pas). */
public class Gourmandise implements IPassiveCombatSkill {
    private static final String COPY_COOLDOWN = "PECHE_GOURMANDISE_COPIE";

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (data == null || !PouvoirsDivins.killsPlayer(damage, victimRef, store)) return false;
        PouvoirsDivins.healPercent(attackerRef, store, 0.20f);
        if (!data.canCast(COPY_COOLDOWN) || ThreadLocalRandom.current().nextFloat() >= 0.25f) return true;
        PlayerLevelData victim = PouvoirsDivins.data(victimRef, store);
        if (victim == null) return true;
        List<Map.Entry<String, Integer>> choix = new ArrayList<>();
        for (var e : victim.getSkillRanks().entrySet()) {
            PassiveSkill p = PassiveSkill.fromName(e.getKey());
            if (p == null || p.isDivine()) continue;
            if (data.getLearnedRank(p.name()) < e.getValue()) choix.add(e);
        }
        Player attacker = store.getComponent(attackerRef, Player.getComponentType());
        if (choix.isEmpty()) return true;
        var pick = choix.get(ThreadLocalRandom.current().nextInt(choix.size()));
        PassiveSkill p = PassiveSkill.fromName(pick.getKey());
        var result = data.learnPassive(p, pick.getValue());
        if (result == PlayerLevelData.LearnResult.LIMIT) {
            if (attacker != null) attacker.getPlayerRef().sendMessage(Message.raw("[Gourmandise] Trop de passifs : rien n'a été copié.").color(Color.RED));
            return true;
        }
        data.applyCooldown(COPY_COOLDOWN, 86_400f);
        if (attacker != null) attacker.getPlayerRef().sendMessage(Message.raw("[Gourmandise] Compétence dévorée : " + p.getDisplayName()
                + " (" + PassiveSkill.rankLabel(pick.getValue()) + ").").color(Color.MAGENTA));
        return true;
    }
}
