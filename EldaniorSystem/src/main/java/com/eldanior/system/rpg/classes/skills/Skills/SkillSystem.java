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

        // 1. Gestion du Timer (Optimisation)
        timer += dt;
        if (timer < 1.0f) return;
        if (index == 0) timer = 0;

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        // 2. Récupération des données du joueur
        PlayerLevelData data = store.getComponent(entityRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null) return;

        // 3. Vérification de la Classe
        String classId = data.getPlayerClassId();
        if (classId == null || classId.equals("none")) return;

        ClassModel classModel = ClassManager.get(classId);

        if (classModel == null || !classModel.isPassiveActive()) return;

        // 4. Exécution des Compétences (BOUCLE)
        for (SkillModel skill : classModel.getPassiveSkills()) {
            if (skill != null) {
                // MISE A JOUR : On passe le CommandBuffer ici !
                skill.onTick(entityRef, store, commandBuffer, data);
            }
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}