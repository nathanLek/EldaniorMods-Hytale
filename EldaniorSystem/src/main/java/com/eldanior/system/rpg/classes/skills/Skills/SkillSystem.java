package com.eldanior.system.rpg.classes.skills.Skills;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.rpg.classes.ClassManager;
import com.eldanior.system.rpg.classes.ClassModel;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class SkillSystem extends EntityTickingSystem<EntityStore> {

    private float timer = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        timer += dt;
        if (timer < 1.0f) return;
        if (index == 0) timer = 0;

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        PlayerLevelData data = store.getComponent(entityRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null) return;

        String classId = data.getPlayerClassId();
        if (classId == null || classId.equals("none")) return;

        ClassModel classModel = ClassManager.get(classId);
        if (classModel == null) return; // On retire isPassiveActive pour l'instant

        // --- CORRECTION ICI ---
        for (SkillModel skill : classModel.getPassiveSkills()) {
            // On vérifie que c'est bien une compétence "à bascule" (Aura/Passif)
            if (skill instanceof ToggledSkill) {
                ToggledSkill toggledSkill = (ToggledSkill) skill;

                // On vérifie si le joueur l'a activée
                if (data.isSkillEnabled(skill.getId())) {
                    toggledSkill.onTick(entityRef, store, commandBuffer, data);
                    // Note : Pour l'instant on ne gère pas le coût en mana par seconde ici,
                    // on pourra l'ajouter dans data.consumeMana() plus tard.
                }
            }
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}