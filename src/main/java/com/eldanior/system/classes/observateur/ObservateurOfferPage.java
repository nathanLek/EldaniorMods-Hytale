package com.eldanior.system.classes.observateur;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Proposition du souverain (Classes/Observateur.ui), ouverte chez le joueur vise par /es observateur proposer. Comme le serment de
 * la voie martiale, mais rien n'est perdu : « Accepter » change seulement la classe (ObservateurManager.accept).
 */
public class ObservateurOfferPage extends InteractiveCustomUIPage<ObservateurOfferPage.Data> {

    private final ObservateurManager.Throne throne;
    private final String sovereign;

    public ObservateurOfferPage(@Nonnull PlayerRef playerRef, ObservateurManager.Throne throne, String sovereign) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.throne = throne;
        this.sovereign = sovereign;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Classes/Observateur.ui");
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        ClassModel current = data == null ? null : ClassManager.get(data.getPlayerClassId());
        ui.set("#ArkSubtitle.Text", "Proposition du " + throne.label);
        ui.set("#ArkText.Text", sovereign + ", " + throne.label + ", vous appelle à devenir " + throne.role
                + ". L'Observateur ne répond qu'au souverain : il manie toutes les armes et tous les savoirs du royaume, "
                + "runes de toutes les classes comprises.");
        ui.set("#ArkWarn.Text", "Vous quittez votre classe actuelle (" + (current != null ? current.getDisplayName() : "aucune")
                + "). Le souverain peut vous retirer ce titre : vous la retrouverez alors.");
        ui.set("#ArkKeep.Text", "Vous gardez votre niveau, votre expérience, vos caractéristiques, vos runes et tous vos objets.");
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ArkAccept", EventData.of("Action", "accept"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ArkCancel", EventData.of("Action", "cancel"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        java.util.UUID uuid = playerRef.getUuid();
        try {
            if ("accept".equals(data.action)) {
                ObservateurManager.Offer offer = ObservateurManager.takeOffer(uuid);
                if (offer == null || offer.throne() != throne) {
                    NotificationHelper.sendNotification(playerRef, "<color:red>Cette proposition n'est plus valable.</color>", NotificationStyle.Warning);
                } else {
                    String err = ObservateurManager.accept(ref, store, uuid, playerRef.getUsername(), throne);
                    if (err != null) {
                        NotificationHelper.sendNotification(playerRef, "<color:red>" + err + "</color>", NotificationStyle.Warning);
                    } else {
                        NotificationHelper.sendNotification(playerRef, "<color:gold>Vous êtes désormais Observateur</color> <color:gray>— "
                                + throne.role + ".</color>", NotificationStyle.Success);
                        tell(offer.sovereign(), playerRef.getUsername() + " a accepté : il est désormais votre Observateur.");
                    }
                }
            } else {
                ObservateurManager.Offer offer = ObservateurManager.peekOffer(uuid);
                ObservateurManager.dropOffer(uuid);
                if (offer != null) tell(offer.sovereign(), playerRef.getUsername() + " a décliné votre proposition d'Observateur.");
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Observateur] proposition", e);
        }
        close();
    }

    private static void tell(String name, String text) {
        try {
            for (PlayerRef pr : Universe.get().getPlayers())
                if (pr != null && pr.getUsername().equalsIgnoreCase(name)) pr.sendMessage(Message.raw(text));
        } catch (Exception ignored) { }
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .build();
        public String action;
    }
}
