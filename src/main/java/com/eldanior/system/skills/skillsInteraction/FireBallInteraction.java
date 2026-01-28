package com.eldanior.system.skills.skillsInteraction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

// LES BONS IMPORTS POUR TON API
import com.hypixel.hytale.math.vector.Vector3f;

public class FireBallInteraction extends SimpleInteraction {

    public FireBallInteraction() {
        super();
    }

    public static final BuilderCodec<FireBallInteraction> CODEC =
            BuilderCodec.builder(FireBallInteraction.class, FireBallInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun,
                         float time,
                         @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context,
                         @NonNullDecl CooldownHandler cooldownHandler) {

        // 1. Correction du type d'interaction
        if (!firstRun || type != InteractionType.Use) return;

        Ref<EntityStore> entityRef = context.getOwningEntity();
        Store<EntityStore> store = entityRef.getStore();

        Player player = store.getComponent(entityRef, Player.getComponentType());

        // 2. Récupération du COMPOSANT Transform (Server side)
        ComponentType<EntityStore, ? extends Component<EntityStore>> Transform = new ComponentType<>();
        Transform transform = (Transform) store.getComponent(entityRef, Transform);

        if (player == null || transform == null) return;

        // 3. Calcul spatial corrigé
        // On utilise getPosition() pour le composant serveur
        Vector3f spawnPos = transform.getPosition().add(0, 1.5f, 0).toVector3f();

        // La rotation (Quaternion) possède la méthode toForwardVector
        Vector3f direction = transform.getRotation().toVector3d().toVector3f();

        launchFireball(player, spawnPos, direction);
    }

    private void launchFireball(Player shooter, Vector3f pos, Vector3f dir) {
        // Utilisation de getDisplayName() ou getName() selon ton API
        System.out.println("🔥 Boule de feu lancée par " + shooter.getDisplayName());
        System.out.println("📍 Pos: " + pos.toString() + " | Dir: " + dir.toString());
    }
}