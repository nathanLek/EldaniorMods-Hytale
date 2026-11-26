package com.eldanior.system.hud;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Barre d'experience en bas a gauche de l'ecran (demande du 2026-11-12) : meme dessin que la barre de vie du jeu, le niveau
 * a la place de l'icone (Hud/XpHud.ui). HUD a part (cle "xp_hud") : il reste affiche quand le CombinedHud (quetes, groupe)
 * est retire. {@link UpdateSystem} le pose et ne renvoie que ce qui a change.
 */
public class XpHud extends CustomUIHud {

    public static final String KEY = "xp_hud";

    private int level = -1, xp = -1, need = -1;

    public XpHud(PlayerRef playerRef) {
        super(playerRef, KEY);
    }

    @Override
    protected void build(UICommandBuilder ui) {
        ui.append("Hud/XpHud.ui");
        if (level >= 0) render(ui);
    }

    /** true si les valeurs ont change (il faut alors renvoyer la barre). */
    boolean set(PlayerLevelData d) {
        int l = d.getLevel(), x = d.getExperience(), n = d.getRequiredExperience();
        if (l == level && x == xp && n == need) return false;
        level = l;
        xp = x;
        need = n;
        return true;
    }

    private void render(UICommandBuilder ui) {
        boolean max = level >= PlayerLevelData.MAX_LEVEL;
        ui.set("#XpLevel.Text", String.valueOf(level));
        ui.set("#XpBar.Value", max ? 1f : Math.max(0f, Math.min(1f, need > 0 ? (float) xp / need : 0f)));
        ui.set("#XpText.Text", max ? "Niveau maximal" : group(xp) + " / " + group(need) + " XP");
    }

    private static String group(int v) {
        return String.format(java.util.Locale.FRANCE, "%,d", v).replace(' ', ' ').replace(' ', ' ');
    }

    /** Pose la barre sur chaque joueur et la tient a jour (toutes les ~0,5 s, seulement si l'XP a bouge). */
    public static class UpdateSystem extends EntityTickingSystem<EntityStore> {
        private int tick;

        @Override
        public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                         @Nonnull CommandBuffer<EntityStore> cb) {
            if (index == 0) tick++;
            if (tick % 10 != 0) return;
            try {
                Ref<EntityStore> ref = chunk.getReferenceTo(index);
                if (!ref.isValid()) return;
                Player player = store.getComponent(ref, Player.getComponentType());
                PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
                PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
                if (player == null || pr == null || data == null) return;
                CustomUIHud current = player.getHudManager().getCustomHud(KEY);
                if (current instanceof XpHud hud) {
                    if (hud.set(data)) {
                        UICommandBuilder ui = new UICommandBuilder();
                        hud.render(ui);
                        hud.update(false, ui);
                    }
                } else {
                    XpHud hud = new XpHud(pr);
                    hud.set(data);
                    player.getHudManager().addCustomHud(pr, hud);
                }
            } catch (Exception e) {
                EldaniorLogger.error("[XpHud]", e);
            }
        }

        @Nonnull
        @Override
        public Query<EntityStore> getQuery() {
            return Player.getComponentType();
        }
    }
}
