package com.eldanior.system.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nullable;

public class SkillItemComponent implements Component<EntityStore> {
    private String skillId = "";

    public SkillItemComponent() {}
    public SkillItemComponent(String skillId) { this.skillId = skillId; }

    public String getSkillId() { return skillId; }

    public static final BuilderCodec<SkillItemComponent> CODEC = BuilderCodec.builder(SkillItemComponent.class, SkillItemComponent::new)
            .append(new KeyedCodec<>("SkillId", Codec.STRING), (data, v) -> data.skillId = v, data -> data.skillId).add()
            .build();

    @Nullable
    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Component<EntityStore> clone() {
        return new SkillItemComponent(this.skillId);
    }
}