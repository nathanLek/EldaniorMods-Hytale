package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;

/** Fait avancer, a chaque tick, les sorts divins du joueur : Sanctuaire de Taranis, Gilgamesh Babylon, Porte de Niflheim (+ degel). */
public class DivineSpellsSystem extends EntityTickingSystem<EntityStore> {

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> caster = chunk.getReferenceTo(index);
        if (!caster.isValid()) return;
        UUID uuid = SpellFx.uuid(caster, store);
        if (uuid == null) return;
        TaranisSanctuary.tick(uuid, caster, store, commandBuffer);
        BahamutAdvent.tick(uuid, caster, store, commandBuffer);
        DragonAvatar.tick(uuid, caster, store, commandBuffer);
        ChronosHourglass.tick(uuid, caster, store, commandBuffer);
        ChaosAbyss.tick(uuid, caster, store, commandBuffer);
        BifrostBridge.tick(uuid, caster, store, commandBuffer);
        NyxVeil.tick(uuid, caster, store, commandBuffer);
        ApolloSanctuary.tick(uuid, caster, store, commandBuffer);
        GaiaAwakening.tick(uuid, caster, store, commandBuffer);
        YggdrasilAwakening.tick(uuid, caster, store, commandBuffer);
        GilgameshBabylon.tick(uuid, caster, store, commandBuffer);
        NiflheimGate.tick(uuid, caster, store, commandBuffer);
        RuneSpellRuntime.tick(uuid, caster, store, commandBuffer); // sorts de rune du moteur commun (RuneSpells)
        SpellFx.tickLocks(store); // creatures gelees : position figee
        TerrainRestore.tick(SpellFx.world(store)); // restauration du terrain (givre, terre brulee)
        NiflheimGate.tickPrisons(store, commandBuffer); // piques de glace sur les ennemis geles
        NiflheimGate.tickZones(store, commandBuffer);   // engelure, porte, brume, stalactites, degel
        NiflheimGate.tickIcicles(store, commandBuffer); // stalactites de glace en chute
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
