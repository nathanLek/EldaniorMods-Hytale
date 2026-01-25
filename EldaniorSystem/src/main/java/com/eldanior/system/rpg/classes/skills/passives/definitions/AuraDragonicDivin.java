package com.eldanior.system.rpg.classes.skills.passives.definitions;

import com.eldanior.system.rpg.classes.skills.passives.PassiveSkillModel;
import com.eldanior.system.components.PlayerLevelData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class AuraDragonicDivin extends PassiveSkillModel {

    public AuraDragonicDivin() {
        super(
                "aura_dragonic_divin",
                "Aura Draconique Divine",
                "Une aura ancienne qui regenere le corps et l'esprit.",
                1.0f // 1 seconde
        );
    }

    @Override
    public void onTick(Ref<EntityStore> playerRef, Store<EntityStore> store, PlayerLevelData data) {

        EntityStatMap statMap = store.getComponent(playerRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        // 1. Régénération de VIE
        int hpIndex = DefaultEntityStatTypes.getHealth();
        var hpStat = statMap.get(hpIndex);

        // J'ai ajouté un log pour vérifier que le code s'exécute !
        // Regarde ta console serveur, tu devrais voir ça défiler.
        System.out.println("[Aura] Vérification... HP actuel: " + (hpStat != null ? hpStat.get() : "null"));

        if (hpStat != null && hpStat.get() < hpStat.getMax()) {
            // TEST : J'ai mis 5% (0.05f) au lieu de 1% pour que tu le voies bien !
            float healAmount = hpStat.getMax() * 0.05f;
            statMap.setStatValue(hpIndex, Math.min(hpStat.getMax(), hpStat.get() + healAmount));

            System.out.println("[Aura] + " + healAmount + " PV rendus !");
        }

        // 2. Régénération de MANA
        int manaIndex = DefaultEntityStatTypes.getMana();
        var manaStat = statMap.get(manaIndex);
        if (manaStat != null && manaStat.get() < manaStat.getMax()) {
            // TEST : +10 Mana d'un coup
            statMap.setStatValue(manaIndex, Math.min(manaStat.getMax(), manaStat.get() + 10.0f));
            System.out.println("[Aura] +10 Mana !");
        }
    }
}