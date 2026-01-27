package com.eldanior.system.rpg.classes.skills.Skills;

import com.eldanior.system.rpg.enums.ClassType;
import com.eldanior.system.rpg.enums.Rarity;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Compétence à effet immédiat (Clic = Action).
 * Exemples : Boule de feu, Soin instantané, Coup d'épée spécial.
 */
public abstract class InstantSkill extends SkillModel {

    public InstantSkill(String id, String name, String description, Rarity rarity, ClassType classType, float cooldown, float manaCost) {
        super(id, name, description, rarity, classType, cooldown, manaCost);
    }

    /**
     * Exécute le sort.
     * @return true si le sort a réussi (pour déclencher le cooldown et le coût en mana).
     */
    public abstract boolean cast(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer);
}