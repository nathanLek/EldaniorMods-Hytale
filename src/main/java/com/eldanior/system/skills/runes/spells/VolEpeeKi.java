package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.classes.definitions.luo.VoieMartiale;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.FlyMode;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.protocol.packets.player.SetMovementStates;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.UUID;

/**
 * Rune du Vol sur epee de Ki (Luo, Unique, Saint Martial, voie martiale, 2026-11-10) : competence a bascule. Un clic : une
 * epee de Ki geante (modele Ki_Epee_Vol, gen_voie_martiale.py) se forme sous les pieds et le lanceur vole (vol force du moteur,
 * FlyMode.Forced, comme la levitation du Courroux de Muspelheim, mais avec une vitesse : saut pour monter, accroupi pour
 * descendre). Second clic ou mana epuise : atterrissage, protection de chute 6 s.
 * Mana par seconde 8 / 6 / 4 et vitesse 9 / 11 / 13 (Saint, Maitre Supreme, Dieu Martial).
 * Fiche de conception : scripts/runes/specs/Rune_VolEpeeKi.json
 */
public final class VolEpeeKi implements RuneSpell {

    // ===== Reglages (equilibrage), indices = palier - 1 =====
    static final int[] MANA = {8, 8, 8, 8, 8, 8, 6, 4};
    static final float[] SPEED = {9f, 9f, 9f, 9f, 9f, 9f, 11f, 13f};
    static final long SECOND_MS = 1_000, FALL_GUARD_MS = 6_000, TRAIL_MS = 150;
    static final double UNDER = 0.15;
    /** Retours du 2026-11-10 : le joueur etait debout sur la pointe, la lame derriere lui. L'origine du modele est la POINTE
     *  (comme une fleche : Origin_Projectile), la lame s'etend vers l'arriere : pointe devant (orientation du regard) et origine
     *  avancee de la moitie de la longueur (katana -20..+64,7 unites, 64 / bloc, x3,2) pour que le milieu soit sous les pieds. */
    static final double CENTER = (64.7 + 20.0) / 2 / 64.0 * 3.2 + 0.7;   // + 0,7 : epee encore avancee (retour du 2026-11-10, soir)

    // ===== Assets =====
    private static final String CARRIER = "Projectile_Config_Ki_Epee_Vol";
    private static final String FX_START = "Eldanior_Pas_Celeste", FX_TRAIL = "Gilgamesh_Sword_Burst";
    private static final String SFX_START = "SFX_Ability_Wind_Strike_Imbue", SFX_LAND = "SFX_Light_Melee_T2_Lunge";

    @Override public String id() { return "VolEpeeKi"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        UUID uuid = SpellFx.uuid(caster, store);
        Flight running = RuneSpellRuntime.find(uuid, Flight.class);
        if (running != null) {
            running.stop = true;
            return null;
        }
        int tier = Math.max(6, MartialKi.tier(caster, store));
        if (MartialKi.stat(caster, DefaultEntityStatTypes.getMana(), store) < MANA[tier - 1]) {
            MartialKi.notify(caster, store, "<color:red>Pas assez de mana pour vous envoler.</color>", NotificationStyle.Warning);
            return null;
        }
        MeditationMartiale.Calm calm = RuneSpellRuntime.find(uuid, MeditationMartiale.Calm.class);
        if (calm != null) calm.stop = true;
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Flight f = new Flight(caster, tier, System.currentTimeMillis());
        if (!f.fly(caster, store, cb)) return null;
        f.sword = SpellFx.spawnModel(caster, CARRIER, swordAt(pos, SpellFx.forward(caster, store)), SpellFx.forward(caster, store), cb);
        SpellFx.notSaved(f.sword, store, cb);
        SpellFx.fx(FX_START, new Vector3d(pos).add(0, 0.1, 0), 1.5f, store, cb);
        SpellFx.sound(SFX_START, pos, cb);
        MartialKi.notify(caster, store, "<color:aqua>Vol sur épée de Ki</color> <color:gray>— " + VoieMartiale.rank(tier)
                + " (" + MANA[tier - 1] + " mana/s) : saut pour monter, accroupi pour descendre, reclic pour atterrir</color>",
                NotificationStyle.Success);
        return f;
    }

    /** Origine du modele (pointe) : devant les pieds de CENTER, pour que le milieu de la lame soit sous le joueur. */
    static Vector3d swordAt(Vector3d feet, Vector3d fwd) {
        return new Vector3d(feet).add(0, -UNDER, 0).add(new Vector3d(fwd).mul(CENTER));
    }

    static final class Flight implements Instance {
        final Ref<EntityStore> caster;
        volatile boolean stop;
        int tier;
        long lastSecond, lastTrail;
        Ref<EntityStore> sword;
        FlyMode prevFly;
        float prevH, prevV;

        Flight(Ref<EntityStore> caster, int tier, long now) { this.caster = caster; this.tier = tier; this.lastSecond = now; }

        /** Vol force a la vitesse du palier ; garde les reglages d'avant pour les rendre a l'atterrissage. */
        boolean fly(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            MovementManager manager = store.getComponent(caster, MovementManager.getComponentType());
            PlayerRef net = store.getComponent(caster, PlayerRef.getComponentType());
            if (manager == null || net == null || manager.getSettings() == null) return false;
            MovementSettings s = manager.getSettings();
            prevFly = s.fly; prevH = s.horizontalFlySpeed; prevV = s.verticalFlySpeed;
            s.fly = FlyMode.Forced;
            s.horizontalFlySpeed = SPEED[tier - 1];
            s.verticalFlySpeed = SPEED[tier - 1] * 0.7f;
            return apply(caster, manager, net, true, cb);
        }

        void land(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (prevFly == null) return;
            MovementManager manager = store.getComponent(caster, MovementManager.getComponentType());
            PlayerRef net = store.getComponent(caster, PlayerRef.getComponentType());
            if (manager == null || net == null || manager.getSettings() == null) return;
            MovementSettings s = manager.getSettings();
            s.fly = prevFly; s.horizontalFlySpeed = prevH; s.verticalFlySpeed = prevV;
            prevFly = null;
            apply(caster, manager, net, false, cb);
            SpellFx.protectFromFall(caster, FALL_GUARD_MS, store);
        }

        private static boolean apply(Ref<EntityStore> caster, MovementManager manager, PlayerRef net, boolean flying, CommandBuffer<EntityStore> cb) {
            try {
                manager.update(net.getPacketHandler());
                cb.putComponent(caster, MovementManager.getComponentType(), manager);
                net.getPacketHandler().writeNoCache(new SetMovementStates(new SavedMovementStates(flying)));
                return true;
            } catch (Exception e) {
                EldaniorLogger.error("VolEpeeKi: vol", e);
                return false;
            }
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            if (stop) {
                land(caster, store, cb);
                SpellFx.sound(SFX_LAND, pos, cb);
                MartialKi.notify(caster, store, "<color:gray>L'épée de Ki se dissipe.</color>", NotificationStyle.Default);
                return true;
            }
            Vector3d fwd = SpellFx.forward(caster, store);
            SpellFx.hold(sword, swordAt(pos, fwd), SpellFx.facing(fwd, false), store);   // pointe devant
            if (now - lastTrail >= TRAIL_MS) { lastTrail = now; SpellFx.fx(FX_TRAIL, new Vector3d(pos).add(0, -0.2, 0), 0.3f, store, cb); }
            if (now - lastSecond < SECOND_MS) return false;
            lastSecond = now;
            int cost = MANA[tier - 1];
            if (MartialKi.stat(caster, DefaultEntityStatTypes.getMana(), store) < cost) {
                land(caster, store, cb);
                MartialKi.notify(caster, store, "<color:gray>L'épée de Ki se dissipe : mana épuisé.</color>", NotificationStyle.Warning);
                return true;
            }
            MartialKi.addStat(caster, DefaultEntityStatTypes.getMana(), -cost, store);
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            SpellFx.remove(sword, cb);
            if (prevFly != null && caster != null && caster.isValid()) {       // fin imprevue (mort, rune retiree...)
                try { land(caster, caster.getStore(), cb); } catch (Exception e) { EldaniorLogger.error("VolEpeeKi: fin", e); }
            }
        }

        @Override
        public void abort() { RuneSpellRuntime.removeLater(sword); }
    }
}
