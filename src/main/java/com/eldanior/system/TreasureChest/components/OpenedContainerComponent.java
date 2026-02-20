package com.eldanior.system.TreasureChest.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nullable;

public class OpenedContainerComponent implements Component<EntityStore> {

    public static final BuilderCodec<OpenedContainerComponent> CODEC = BuilderCodec.builder(OpenedContainerComponent.class, OpenedContainerComponent::new)
            .addField(new KeyedCodec<>("X", Codec.INTEGER), (d, v) -> d.x = v, (d) -> d.x)
            .addField(new KeyedCodec<>("Y", Codec.INTEGER), (d, v) -> d.y = v, (d) -> d.y)
            .addField(new KeyedCodec<>("Z", Codec.INTEGER), (d, v) -> d.z = v, (d) -> d.z)
            .build();

    private int x;
    private int y;
    private int z;

    public OpenedContainerComponent() {
    }

    public OpenedContainerComponent(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public OpenedContainerComponent(OpenedContainerComponent other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    @Nullable
    public Component<EntityStore> clone() {
        return new OpenedContainerComponent(this);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }
}