package com.eldanior.system.classes.gui;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ClassSelectionScreen extends InteractiveCustomUIPage<ClassSelectionScreen.ClassEventData> {

    private final List<ClassModel> baseClasses;
    private final boolean isEvolution;

    public ClassSelectionScreen(@Nonnull PlayerRef playerRef, @Nonnull List<String> rolledClassIds, boolean isEvolution) {
        super(playerRef, CustomPageLifetime.CanDismiss, ClassEventData.CODEC);
        this.isEvolution = isEvolution;
        this.baseClasses = new ArrayList<>();

        for (String id : rolledClassIds) {
            ClassModel model = ClassManager.get(id);
            if (model != null) {
                this.baseClasses.add(model);
            }
        }

        System.out.println("[DEBUG] Classes pour le choix trouvées : " + baseClasses.size());
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        commands.append("Classes/ClassSelection.ui");

        int index = 0;

        for (ClassModel model : this.baseClasses) {
            if (model.isAdminAccess()) continue;
            if (index >= 6) break;

            String tileId = "#ClassTile" + index;

            // 1. On garde un texte propre sans balises HTML/Couleur
            String displayNameWithRarity = "[" + model.getRarity().name() + "] " + model.getDisplayName();
            commands.set(tileId + " #ClassLabel.Text", displayNameWithRarity);
            commands.set(tileId + " #CategoryIcon.ItemId", getIconForClass(model.getId()));

            // --- 2. NOUVEAU : CHANGEMENT DE LA COULEUR DE FOND ---
            // On récupère le code Hexadécimal
            String hexColor = getRarityHexColor(model.getRarity());

            // On teinte la tuile entière avec cette couleur
            // ⚠️ Note : Si ton UI a un fond spécifique à l'intérieur de la tuile (ex: "#Background"),
            // tu devras peut-être écrire : commands.set(tileId + " #Background.Color", hexColor);
            commands.set(tileId + " #RarityBorderTop.Background",    hexColor);
            commands.set(tileId + " #RarityBorderBottom.Background", hexColor);
            commands.set(tileId + " #RarityBorderLeft.Background",   hexColor);
            commands.set(tileId + " #RarityBorderRight.Background",  hexColor);

            events.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    tileId,
                    EventData.of("Action", "selectClass").append("ClassId", model.getId())
            );

            index++;
        }

        for (int i = index; i < 6; i++) {
            commands.set("#ClassTile" + i + ".Visible", false);
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull ClassEventData data) {
        if (!"selectClass".equals(data.action) || data.classId == null) return;

        ClassModel model = ClassManager.get(data.classId);
        if (model == null) return;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData playerData = store.getComponent(ref, type);

        if (playerData == null) playerData = new PlayerLevelData();

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if (!isEvolution && playerData.getPlayerClassId().equalsIgnoreCase(data.classId)) {
            if (playerRef != null) {
                NotificationHelper.sendNotification(playerRef,
                        "<color:yellow>Vous êtes déjà " + model.getDisplayName() + " !</color>",
                        NotificationStyle.Warning);
            }
            return;
        }

        playerData.setPlayerClass(model.getDisplayName());
        playerData.setPlayerClassId(model.getId());
        playerData.forgetAllSkills();

        StatCalculator.updatePlayerStats(ref, store, playerData);
        store.putComponent(ref, type, playerData);

        if (playerRef != null) {
            // Pour le tchat, on utilise ton ancienne méthode avec les balises HTML <color:...>
            String chatColor = getChatColor(model.getRarity());
            NotificationHelper.sendNotification(playerRef,
                    "<color:green>Évolution : </color>" + chatColor + model.getDisplayName() + "</color>",
                    NotificationStyle.Success);
        }

        this.close();
    }

    // --- NOUVELLE MÉTHODE : Codes couleurs UI (Hexadécimal) ---
    private String getRarityHexColor(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> "#E0E0E0";    // Gris clair / Blanc cassé
            case RARE -> "#3498DB";      // Bleu vif
            case EPIC -> "#9B59B6";      // Violet / Magenta
            case UNIQUE -> "#E74C3C";    // Rouge intense
            case LEGENDARY -> "#F1C40F"; // Or / Jaune éclatant
            case DIVINE -> "#00FFFF";    // Cyan / Aqua brillant
            default -> "#FFFFFF";
        };
    }

    // --- ANCIENNE MÉTHODE : Codes couleurs Tchat (Balises) ---
    private String getChatColor(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> "<color:white>";
            case RARE -> "<color:blue>";
            case EPIC -> "<color:magenta>";
            case UNIQUE -> "<color:red>";
            case LEGENDARY -> "<color:gold>";
            case DIVINE -> "<color:aqua>";
            default -> "<color:gray>";
        };
    }

    private String getIconForClass(String classId) {
        return switch (classId) {
            case "warrior", "epeiste", "fantassin", "brute", "mercenaire" -> "Weapon_Sword_Crude";
            case "mage"     -> "Weapon_Spellbook_Fire";
            case "assassin" -> "Weapon_Daggers_Crude";
            case "archer"   -> "Weapon_Shortbow_Bomb";
            case "merchant" -> "Deco_Treasure";
            case "champion", "heros", "titan", "asura" -> "Weapon_Sword_Steel";
            default         -> "Plant_Fruit_Apple";
        };
    }

    public static class ClassEventData {
        public String action;
        public String classId;

        public static final BuilderCodec<ClassEventData> CODEC =
                BuilderCodec.builder(ClassEventData.class, ClassEventData::new)
                        .addField(new KeyedCodec<>("Action",  Codec.STRING), (d, v) -> d.action  = v, d -> d.action)
                        .addField(new KeyedCodec<>("ClassId", Codec.STRING), (d, v) -> d.classId = v, d -> d.classId)
                        .build();
    }
}