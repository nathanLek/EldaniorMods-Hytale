package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.system.InvisibilityManager;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rune Sentence Silencieuse (Tenebres, Legendaire), technique ultime d'assassinat de l'Observateur (2026-11-17).
 * La cible visee est figee et marquee (marque vue du seul lanceur) ; l'Observateur se fond dans l'ombre (invisible), surgit
 * dans son dos et frappe une seule fois :
 *  - creature : execution sous 40 % de vie, sinon 35 % de sa vie maximale ;
 *  - joueur : execution sous 30 %, sinon 25 % ;
 *  - boss (Dungeon Boss, role « Boss ») : jamais execute, 15 % ;
 *  - masque par Sans Visage : seuil d'execution +10 points, et le masque tombe au moment du coup.
 * Portee 40 blocs en ligne de vue ; avec Arkies Domini ouverte, la presence visee par l'Oeil (100 blocs, a travers les murs).
 * L'Observateur reste invisible un court instant apres le coup pour disparaitre.
 *
 * Cout de la rarete (1200 mana, 300 s) preleve dans le code SEULEMENT si une cible est trouvee (fiche : overrides).
 * Fiche de conception : scripts/runes/specs/Rune_SentenceSilencieuse.json
 */
public final class SentenceSilencieuse implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    static final double RANGE = 40;
    static final int MANA = 1200;
    static final long COOLDOWN_MS = 300_000;
    static final long STUN_MS = 1_200, BLINK_AT = 300, STRIKE_AT = 550;
    static final float INVIS_S = 2.4f;
    static final double BEHIND = 1.3;
    /** {seuil d'execution, part de vie max sinon} : creature, joueur, boss. */
    static final float NPC_EXEC = 0.40f, NPC_HIT = 0.35f, PLAYER_EXEC = 0.30f, PLAYER_HIT = 0.25f, BOSS_HIT = 0.15f, MASK_BONUS = 0.10f;

    // ===== Assets (verifies : rune_tool assets) =====
    private static final String FX_SMOKE = "Smoke_Black", FX_MARK = "Nyx_Void", FX_STRIKE = "Dungeon_Goblin_Void_Strike",
            FX_DEATH = "Effect_Death_Void", STUN = "Bomb_Explode_Stun",
            SFX_VANISH = "SFX_Crawler_Void_Despawn", SFX_BLINK = "SFX_Daggers_T1_Pounce", SFX_STAB = "SFX_Daggers_T1_Stab_Double_Impact";

    private static final Map<UUID, Long> READY_AT = new ConcurrentHashMap<>();

    @Override public String id() { return "SentenceSilencieuse"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        UUID uuid = SpellFx.uuid(caster, store);
        if (uuid == null) return null;
        long now = System.currentTimeMillis();
        Long ready = READY_AT.get(uuid);
        if (ready != null && ready > now) {
            MartialKi.notify(caster, store, "<color:gray>Sentence Silencieuse : encore " + ((ready - now) / 1000 + 1) + " s.</color>",
                    NotificationStyle.Warning);
            return null;
        }
        Ref<EntityStore> target = ArkiesDomini.lookedAt(uuid);           // l'Oeil voit a travers les murs
        if (target == null || !target.isValid() || !SpellFx.isEnemy(caster, target, store)) target = SpellFx.targetEntity(caster, store, RANGE);
        if (target == null || !target.isValid() || store.getComponent(target, DeathComponent.getComponentType()) != null) {
            MartialKi.notify(caster, store, "<color:gray>Aucune cible : visez la personne à exécuter.</color>", NotificationStyle.Default);
            return null;
        }
        if (MartialKi.stat(caster, DefaultEntityStatTypes.getMana(), store) < MANA) {
            MartialKi.notify(caster, store, "<color:red>Pas assez de mana (" + MANA + ").</color>", NotificationStyle.Warning);
            return null;
        }
        Vector3d me = SpellFx.position(caster, store), tp = SpellFx.position(target, store);
        if (me == null || tp == null) return null;
        MartialKi.addStat(caster, DefaultEntityStatTypes.getMana(), -MANA, store);
        READY_AT.put(uuid, now + COOLDOWN_MS);

        SpellFx.stun(target, STUN, STUN_MS, store, cb);
        InvisibilityManager.applyInvisibility(uuid, caster, InvisibilityManager.InvisType.TOTAL, INVIS_S, store);
        SpellFx.fx(FX_SMOKE, new Vector3d(me).add(0, 0.9, 0), 1.6f, store, cb);
        SpellFx.sound(SFX_VANISH, me, cb);
        SpellFx.fxFor(FX_MARK, new Vector3d(tp).add(0, 2.2, 0), 0.8f, caster, store);
        return new Strike(caster, uuid, target, SansVisage.isDisguised(uuid), now);
    }

    /** Recharge restante (ms), 0 si prete : pour le HUD ou d'autres sorts. */
    public static long remaining(UUID uuid) {
        Long r = uuid == null ? null : READY_AT.get(uuid);
        return r == null ? 0 : Math.max(0, r - System.currentTimeMillis());
    }

    private static final class Strike implements Instance {
        final Ref<EntityStore> caster, target;
        final UUID uuid;
        final boolean masked;
        final long startAt;
        boolean blinked;

        Strike(Ref<EntityStore> caster, UUID uuid, Ref<EntityStore> target, boolean masked, long startAt) {
            this.caster = caster; this.uuid = uuid; this.target = target; this.masked = masked; this.startAt = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!target.isValid() || store.getComponent(target, DeathComponent.getComponentType()) != null) {
                MartialKi.notify(caster, store, "<color:gray>La cible vous a échappé.</color>", NotificationStyle.Default);
                return true;
            }
            long t = now - startAt;
            if (!blinked && t >= BLINK_AT) {
                blinked = true;
                blink(caster, store, cb);
            }
            if (t >= STRIKE_AT) {
                strike(caster, store, cb);
                return true;
            }
            return false;
        }

        /** Dans le dos de la cible, face a elle (a defaut : sur ses cotes, puis devant elle). */
        private void blink(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d tp = SpellFx.position(target, store);
            if (tp == null) return;
            Vector3d fwd = SpellFx.forward(target, store);
            Vector3d side = new Vector3d(-fwd.z, 0, fwd.x);
            World world = SpellFx.world(store);
            Vector3d dest = null;
            for (Vector3d off : new Vector3d[]{new Vector3d(fwd).mul(-BEHIND), new Vector3d(side).mul(BEHIND),
                    new Vector3d(side).mul(-BEHIND), new Vector3d(fwd).mul(BEHIND)}) {
                Vector3d c = new Vector3d(tp).add(off);
                if (free(world, c)) { dest = c; break; }
            }
            if (dest == null) return;                                           // aucune place libre : il frappe d'ou il est
            Vector3d look = new Vector3d(tp).sub(dest);
            look.y = 0;
            if (look.lengthSquared() < 1e-4) look.set(fwd);
            SpellFx.teleport(caster, dest, SpellFx.facing(look.normalize(), false), store, cb);
            SpellFx.fx(FX_SMOKE, new Vector3d(dest).add(0, 0.9, 0), 1.4f, store, cb);
            SpellFx.sound(SFX_BLINK, dest, cb);
        }

        private void strike(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            float[] hp = ArkiesDomini.health(target, store);
            Vector3d tp = SpellFx.position(target, store);
            if (hp == null || hp[1] <= 0 || tp == null) return;
            boolean player = store.getComponent(target, Player.getComponentType()) != null;
            boolean boss = isBoss(target, store);
            float exec = boss ? -1f : (player ? PLAYER_EXEC : NPC_EXEC) + (masked ? MASK_BONUS : 0f);
            float ratio = hp[0] / hp[1];
            boolean execution = ratio <= exec;
            float amount = execution ? hp[0] * 5f + 100f : hp[1] * (boss ? BOSS_HIT : player ? PLAYER_HIT : NPC_HIT);
            SpellFx.damage(caster, target, "Slashing", amount, null, "Impact_Strong", cb);
            SpellFx.fx(FX_STRIKE, new Vector3d(tp).add(0, 1.0, 0), 1.2f, store, cb);
            SpellFx.sound(SFX_STAB, tp, cb);
            if (execution) SpellFx.fx(FX_DEATH, new Vector3d(tp).add(0, 1.0, 0), 1.0f, store, cb);
            if (masked) SansVisage.reveal(caster, uuid, store, cb, "Votre masque tombe au moment du coup.");
            String who = ArkiesDomini.name(target, store);
            MartialKi.notify(caster, store, execution
                    ? "<color:dark_red>Sentence exécutée</color> <color:gray>— " + who + ".</color>"
                    : "<color:gold>Sentence Silencieuse</color> <color:gray>— " + who + " perd " + Math.round(amount / hp[1] * 100)
                      + " % de sa vie" + (boss ? " (un boss ne s'exécute pas)." : " (exécution sous " + Math.round(exec * 100) + " %).") + "</color>",
                    execution ? NotificationStyle.Success : NotificationStyle.Default);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { }
    }

    static boolean isBoss(Ref<EntityStore> e, Store<EntityStore> store) {
        UUID id = SpellFx.uuid(e, store);
        if (id != null && com.eldanior.system.boards.DungeonBosses.isBoss(id)) return true;
        NPCEntity npc = store.getComponent(e, NPCEntity.getComponentType());
        String role = npc != null ? npc.getNPCTypeId() : null;
        return role != null && role.toLowerCase().contains("boss");
    }

    /** Case ou un corps tient : pieds et tete hors d'un bloc solide (chunk non charge : refuse). */
    private static boolean free(World world, Vector3d feet) {
        if (world == null) return false;
        int x = (int) Math.floor(feet.x), y = (int) Math.floor(feet.y), z = (int) Math.floor(feet.z);
        return passable(world, x, y, z) && passable(world, x, y + 1, z);
    }

    private static boolean passable(World world, int x, int y, int z) {
        BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
        return t != null && t.getMaterial() != BlockMaterial.Solid;
    }

    public static void handleDisconnect(UUID uuid) { /* la recharge survit a une reconnexion */ }
}
