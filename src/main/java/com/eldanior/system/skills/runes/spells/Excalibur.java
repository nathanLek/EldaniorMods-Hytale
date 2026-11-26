package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Excalibur, Lame du Createur (artefact divin, Artefact_Sword_Excalibur). Son passif (+50 % contre les morts-vivants et le
 * Neant) est dans ArtefactWeapons ; ce fichier porte sa signature, lancee par l'interaction Excalibur_Signature :
 *   - JugementAvalon : un pilier de lumiere s'abat a quelques blocs devant le porteur. Les ennemis du rayon sont foudroyes
 *     (le passif s'applique aussi : la signature frappe plus fort les tenebres), repousses et eblouis ; le porteur est soigne.
 */
public final class Excalibur {

    private Excalibur() {}

    public static final class JugementAvalon implements RuneSpell {

        // ===== Reglages (equilibrage) =====
        private static final double AHEAD = 4.0, RADIUS = 6.0, MAX_HEIGHT = 4.0;
        private static final float DAMAGE = 220f, HEAL = 0.15f;
        private static final double PUSH = 9.0, LIFT = 5.0;

        // ===== Assets =====
        private static final String FX_PILLAR = "Bifrost_Divine", FX_BURST = "Gilgamesh_Sword_Burst", FX_HEAL = "Apollo_Heal_Pulse";
        private static final String FX_SKY_FLASH = "Taranis_Sky_Flash";
        private static final String SFX_PILLAR = "SFX_Portal_Void", SFX_THUNDER = "SFX_Global_Weather_Thunder";
        private static final String BLIND = "Elda_Aveuglement";

        @Override public String id() { return "JugementAvalon"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return null;
            Vector3d center = new Vector3d(pos).add(SpellFx.forward(caster, store).mul(AHEAD));

            for (double y = 0; y <= 8; y += 1.0)                                     // le pilier de lumiere
                SpellFx.fx(FX_PILLAR, new Vector3d(center).add(0, y, 0), 0.9f, store, cb);
            SpellFx.fx(FX_BURST, new Vector3d(center).add(0, 1, 0), 1.6f, store, cb);
            SpellFx.fx(FX_SKY_FLASH, new Vector3d(center).add(0, 18, 0), 6f, store, cb);
            SpellFx.sound(SFX_PILLAR, center, cb);
            SpellFx.sound2dNear(SFX_THUNDER, center, 40, store);

            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + 1, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || Math.abs(p.y - center.y) > MAX_HEIGHT) continue;
                Vector3d out = new Vector3d(p.x - center.x, 0, p.z - center.z);
                double d = out.length();
                if (d > RADIUS) continue;
                out = d > 0.5 ? out.div(d).mul(PUSH) : SpellFx.forward(caster, store).mul(PUSH);
                SpellFx.damage(caster, e, "Elemental", DAMAGE, out.add(0, LIFT, 0), "Impact_Strong", cb);
                SpellFx.effect(e, BLIND, cb);
                SpellFx.fx(FX_BURST, new Vector3d(p).add(0, 1, 0), 0.6f, store, cb);
            }

            SpellFx.restore(caster, DefaultEntityStatTypes.getHealth(), HEAL, cb);    // la lumiere purifie le porteur
            SpellFx.fx(FX_HEAL, pos, 1.0f, store, cb);
            return null;
        }
    }
}
