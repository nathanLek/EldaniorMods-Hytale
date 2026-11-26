package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Rune du Lancer de Pièces (Combat, Common, rune propre du Marchand) : une poignee de pieces d'or projetees en cone
 * (6 blocs, 60 degres) : 45 degats et recul. Chaque ennemi touche qui meurt dans les 1,5 s rapporte 5 pieces d'or au
 * lanceur (ecriture de PlayerLevelData differee dans le tampon de commandes : relue au moment d'ecrire, elle ne peut
 * pas ecraser un gain du meme tick, comme dans DeathXPSystem).
 * Fiche de conception : scripts/runes/specs/Rune_LancerDePieces.json
 */
public final class LancerDePieces implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RANGE = 6.0, HALF_ANGLE = Math.toRadians(30), MAX_HEIGHT = 3.0;
    private static final float DAMAGE = 45f;
    private static final double PUSH = 6.0, LIFT = 2.0;
    private static final long REWARD_WINDOW_MS = 1_500;
    private static final int GOLD_PER_KILL = 5;

    // ===== Assets =====
    private static final String FX_COINS = "Block_Break_Treasure", FX_GOLD = "Chronos_Gold";
    private static final String SFX_HIT = "SFX_Coins_Hit", SFX_REWARD = "SFX_Coins_Land";

    @Override public String id() { return "LancerDePieces"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d dir = SpellFx.forward(caster, store);
        Vector3d hand = new Vector3d(pos).add(0, 1.3, 0);
        for (double d = 1; d <= RANGE; d += 1.0) {                       // gerbe de pieces le long du cone
            SpellFx.fx(FX_COINS, new Vector3d(hand).add(new Vector3d(dir).mul(d)), 0.5f + (float) (d / RANGE) * 0.5f, store, cb);
        }
        List<Ref<EntityStore>> hit = new ArrayList<>();
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, RANGE + 1, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null || Math.abs(p.y - pos.y) > MAX_HEIGHT) continue;
            Vector3d to = new Vector3d(p.x - pos.x, 0, p.z - pos.z);
            double d = to.length();
            if (d > RANGE || (d > 0.5 && to.div(d).dot(dir) < Math.cos(HALF_ANGLE))) continue;
            Vector3d out = d > 0.5 ? new Vector3d(to).mul(PUSH) : new Vector3d(dir).mul(PUSH);
            SpellFx.damage(caster, e, "Physical", DAMAGE, out.add(0, LIFT, 0), "Impact_Light", cb);
            SpellFx.fx(FX_COINS, new Vector3d(p).add(0, 1, 0), 0.8f, store, cb);
            SpellFx.sound(SFX_HIT, p, cb);
            hit.add(e);
        }
        return hit.isEmpty() ? null : new Reward(hit, System.currentTimeMillis());
    }

    /** Surveille les ennemis touches : chacun qui meurt dans la fenetre rapporte de l'or (une fois). */
    private static final class Reward implements Instance {
        final List<Ref<EntityStore>> watched;
        final long startAt;

        Reward(List<Ref<EntityStore>> watched, long startAt) { this.watched = watched; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= REWARD_WINDOW_MS || watched.isEmpty()) return true;
            for (int i = watched.size() - 1; i >= 0; i--) {
                Ref<EntityStore> e = watched.get(i);
                boolean dead = e.isValid() && store.getComponent(e, DeathComponent.getComponentType()) != null;
                if (!dead && e.isValid()) continue;
                watched.remove(i);
                if (!dead) continue;                                      // retire du monde sans mourir : rien
                Vector3d p = SpellFx.position(e, store);
                if (p != null) { SpellFx.fx(FX_GOLD, new Vector3d(p).add(0, 1, 0), 0.8f, store, cb); SpellFx.sound(SFX_REWARD, p, cb); }
                giveGold(caster, cb);
            }
            return false;
        }

        private static void giveGold(Ref<EntityStore> caster, CommandBuffer<EntityStore> cb) {
            var type = EldaniorSystem.get().getPlayerLevelDataType();
            cb.run(s -> {                                                 // relu et ecrit au vidage du tampon
                PlayerLevelData data = caster.isValid() ? s.getComponent(caster, type) : null;
                if (data == null) return;
                PlayerLevelData copy = (PlayerLevelData) data.clone();
                if (copy == null) return;
                copy.addMoney(GOLD_PER_KILL);
                s.putComponent(caster, type, copy);
            });
        }
    }
}
