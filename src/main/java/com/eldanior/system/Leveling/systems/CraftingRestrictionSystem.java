package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.Map;
import java.util.Set;

/**
 * Restriction d'acces aux benchs d'artisanat.
 *
 * - Bench_Furniture et Bench_Builders : LIBRE pour tous
 * - Autres benchs : necessite la competence specifique OU la competence "Artisanat" (passe-partout)
 */
public class CraftingRestrictionSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    // Benchs accessibles a tous sans competence
    private static final Set<String> FREE_BENCHES = Set.of(
            "bench_furniture", "bench_builders", "bench_campfire"
    );

    // Benchs bloques pour tout le monde (meme avec Artisanat)
    private static final Set<String> BLOCKED_BENCHES = Set.of(
            "bench_arcane", "bench_workbench"
    );

    // Mapping : nom du bench (lowercase) -> competence requise
    private static final Map<String, PassiveSkill> BENCH_SKILLS = Map.of(
            "bench_cooking", PassiveSkill.CRAFT_CUISINE,
            "bench_furnace", PassiveSkill.CRAFT_FONDERIE,
            "bench_armour", PassiveSkill.CRAFT_ARMURERIE,
            "bench_weapon", PassiveSkill.CRAFT_FORGE_ARMES,
            "bench_tannery", PassiveSkill.CRAFT_TANNERIE,
            "bench_alchemy", PassiveSkill.CRAFT_ALCHIMIE,
            "bench_lumbermill", PassiveSkill.CRAFT_SCIERIE,
            "bench_farming", PassiveSkill.CRAFT_AGRICULTURE,
            "bench_salvage", PassiveSkill.CRAFT_RECYCLAGE
    );

    public CraftingRestrictionSystem() {
        super(UseBlockEvent.Pre.class);
    }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl UseBlockEvent.Pre event) {

        Ref<EntityStore> playerEntityRef = event.getContext().getEntity();
        Player player = store.getComponent(playerEntityRef, Player.getComponentType());
        PlayerRef netRef = store.getComponent(playerEntityRef, PlayerRef.getComponentType());

        if (player == null || netRef == null) return;
        if (!event.getInteractionType().toString().equals("Use")) return;

        Vector3i target = event.getTargetBlock();

        assert player.getWorld() != null;
        BenchBlock benchComponent = BlockModule.getComponent(
                BenchBlock.getComponentType(),
                player.getWorld(), target.getX(), target.getY(), target.getZ()
        );

        if (benchComponent == null) return;

        // Recuperer le nom du bench via le BlockType
        String benchName = "";
        try {
            benchName = player.getWorld().getBlockType(target.getX(), target.getY(), target.getZ())
                    .getId().toLowerCase();
        } catch (Exception ignored) {}

        com.eldanior.system.config.EldaniorLogger.debug("[Craft] Bench detecte: '" + benchName + "'");

        // Verifier si c'est un bench bloque pour tout le monde
        for (String blocked : BLOCKED_BENCHES) {
            if (benchName.contains(blocked)) {
                event.setCancelled(true);
                NotificationHelper.sendNotification(netRef,
                        "<color:red>Cet etabli est interdit !</color>",
                        NotificationStyle.Warning);
                return;
            }
        }

        // Verifier si c'est un bench libre
        for (String freeBench : FREE_BENCHES) {
            if (benchName.contains(freeBench)) return; // Libre pour tous
        }

        // Recuperer les donnees du joueur
        PlayerLevelData playerData = store.getComponent(playerEntityRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (playerData == null) {
            event.setCancelled(true);
            NotificationHelper.sendNotification(netRef,
                    "<color:red>Vous devez apprendre un metier pour utiliser ceci !</color>",
                    NotificationStyle.Warning);
            return;
        }

        // Verifier si le joueur a la competence "Artisanat" (passe-partout)
        boolean hasArtisanat = false;
        if (playerData.getActivePassives() != null) {
            for (PassiveSkill skill : playerData.getActivePassives()) {
                if (skill == PassiveSkill.ARTISANAT) {
                    hasArtisanat = true;
                    break;
                }
            }
        }
        if (hasArtisanat) return; // Artisanat = acces a tout

        // Chercher la competence specifique pour ce bench
        PassiveSkill requiredSkill = null;
        String skillName = "";
        for (Map.Entry<String, PassiveSkill> entry : BENCH_SKILLS.entrySet()) {
            if (benchName.contains(entry.getKey())) {
                requiredSkill = entry.getValue();
                skillName = entry.getValue().getDisplayName();
                break;
            }
        }

        if (requiredSkill == null) {
            // Bench inconnu, pas dans la liste → bloquer par defaut (securite)
            event.setCancelled(true);
            NotificationHelper.sendNotification(netRef,
                    "<color:red>Competence requise pour utiliser cet etabli !</color>",
                    NotificationStyle.Warning);
            return;
        }

        // Verifier si le joueur a la competence specifique
        boolean hasSpecificSkill = false;
        if (playerData.getActivePassives() != null) {
            for (PassiveSkill skill : playerData.getActivePassives()) {
                if (skill == requiredSkill) {
                    hasSpecificSkill = true;
                    break;
                }
            }
        }

        // Verifier aussi dans enabledSkills
        if (!hasSpecificSkill) {
            hasSpecificSkill = playerData.isSkillEnabled(requiredSkill.name());
        }

        if (!hasSpecificSkill) {
            event.setCancelled(true);
            NotificationHelper.sendNotification(netRef,
                    "<color:red>Vous devez apprendre </color><color:gold>" + skillName +
                            "</color><color:red> pour utiliser cet etabli !</color>",
                    NotificationStyle.Warning);
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
