package com.eldanior.system.skills.skills.passives.Divin.Peche;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skills.passives.Divin.PouvoirsDivins;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.eldanior.system.config.configs.StatConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.awt.Color;

/** Luxure (Peche, PK) : +10 % de vitesse ; en tuant un joueur, lui vole 10 % de son or (recharge de 12 h :
 * PassiveSkill.PECHE_LUXURE). */
public class Luxure implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat, int rank, boolean mastered) {
        return stat == StatConfig.AGILITY_SPEED ? 1.10f : 1f;
    }

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (data == null || !PouvoirsDivins.killsPlayer(damage, victimRef, store)) return false;
        PlayerLevelData victim = PouvoirsDivins.data(victimRef, store);
        if (victim == null) return false;
        long stolen = victim.getMoney() / 10;
        if (stolen <= 0 || !victim.removeMoney(stolen)) return false;
        data.addMoney(stolen);
        Player attacker = store.getComponent(attackerRef, Player.getComponentType());
        if (attacker != null) attacker.getPlayerRef().sendMessage(Message.raw("[Luxure] Vous avez volé " + stolen + " or à votre victime.").color(Color.MAGENTA));
        return true;
    }
}
