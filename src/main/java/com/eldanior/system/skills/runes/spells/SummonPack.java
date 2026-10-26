package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Invocations de creatures des runes (golem, loups...) : de vrais PNJ (roles Eldanior_Summon_*, gabarit vanilla
 * Template_Summoned_Ally) inscrits comme invocations du lanceur (pas de tir ami, coup fatal = dissipation). Le sort les
 * pilote : chacun prend pour cible un monstre proche (si possible un different), sinon revient pres du lanceur.
 */
final class SummonPack {

    private static final long SPAWN_TIMEOUT_MS = 3_000;
    private static final double HOSTILE_S = 3.0;

    /** Une creature invoquee : ref null tant qu'elle n'est pas creee (au tick suivant). */
    static final class Member {
        final CloneFx.Npc npc;
        final int slot;
        Ref<EntityStore> target;
        Vector3d lastPos;
        boolean gone;

        Member(CloneFx.Npc npc, int slot, Vector3d at) { this.npc = npc; this.slot = slot; this.lastPos = new Vector3d(at); }

        Ref<EntityStore> ref() { return npc.ref; }
    }

    final Ref<EntityStore> owner;
    final List<Member> members = new ArrayList<>();
    final String fxGone;

    SummonPack(Ref<EntityStore> owner, String fxGone) { this.owner = owner; this.fxGone = fxGone; }

    /** Cree une creature (PNJ du role) a pos ; effectId : teinte optionnelle. */
    Member spawn(World world, String roleId, Vector3d pos, Rotation3f rot, float maxHealth, String effectId, long now) {
        CloneFx.Look look = new CloneFx.Look(null, null, null);
        Member m = new Member(CloneFx.spawn(world, roleId, owner, pos, rot, look, null, maxHealth, effectId, false, now), members.size(), pos);
        members.add(m);
        return m;
    }

    /**
     * Avance la meute : creatures mortes ou vaincues dissipees, cible attribuee parmi les monstres a moins de range blocs
     * du point center, sinon retour au lanceur. Renvoie le nombre de creatures encore presentes (ou en cours de creation).
     */
    int tick(Vector3d center, double range, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        int alive = 0;
        Set<Ref<EntityStore>> taken = new HashSet<>();
        List<Ref<EntityStore>> monsters = monstersAround(center, range, store);
        for (Member m : members) {
            if (m.gone) continue;
            Ref<EntityStore> ref = m.ref();
            if (ref == null) {
                if (m.npc.failed || now - m.npc.born > SPAWN_TIMEOUT_MS) m.gone = true; else alive++;
                continue;
            }
            if (!ref.isValid() || RuneSpellRuntime.isDying(ref)) { dissipate(m, store, cb); continue; }
            alive++;
            Vector3d p = SpellFx.position(ref, store);
            if (p != null) m.lastPos = p;
            if (m.target != null && (!m.target.isValid() || store.getComponent(m.target, DeathComponent.getComponentType()) != null
                    || !monsters.contains(m.target))) m.target = null;
            if (m.target == null) m.target = pick(m.lastPos, monsters, taken, store);
            if (m.target != null) {
                taken.add(m.target);
                CloneFx.hostile(ref, m.target, HOSTILE_S, store);
                CloneFx.hostile(m.target, ref, HOSTILE_S, store);
                CloneFx.mark(ref, "LockedTarget", m.target, store);
                CloneFx.setState(ref, "Combat", null, store);
            } else {
                Vector3d home = SpellFx.position(owner, store);
                if (home != null) CloneFx.leash(ref, home, store);
            }
        }
        return alive;
    }

    /** Monstre le plus proche de from, de preference pas deja pris par une autre creature. */
    private static Ref<EntityStore> pick(Vector3d from, List<Ref<EntityStore>> monsters, Set<Ref<EntityStore>> taken, Store<EntityStore> store) {
        Ref<EntityStore> best = null, fallback = null;
        double bestD = Double.MAX_VALUE, fallbackD = Double.MAX_VALUE;
        for (Ref<EntityStore> e : monsters) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null) continue;
            double d = p.distanceSquared(from);
            if (!taken.contains(e) && d < bestD) { bestD = d; best = e; }
            if (d < fallbackD) { fallbackD = d; fallback = e; }
        }
        return best != null ? best : fallback;
    }

    List<Ref<EntityStore>> monstersAround(Vector3d at, double range, Store<EntityStore> store) {
        List<Ref<EntityStore>> out = new ArrayList<>();
        for (Ref<EntityStore> e : SpellFx.enemiesAround(owner, at, range, store))
            if (!RuneSpellRuntime.isSummon(e) && CloneFx.isMonster(e, store)) out.add(e);
        return out;
    }

    /** La creature vaincue se dissipe (retiree avant de mourir : aucun butin). */
    void dissipate(Member m, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        m.gone = true;
        if (fxGone != null) SpellFx.fx(fxGone, new Vector3d(m.lastPos).add(0, 1, 0), 1.0f, store, cb);
        SpellFx.remove(m.ref(), cb);
        RuneSpellRuntime.forgetSummon(m.ref());
    }

    void cleanup(CommandBuffer<EntityStore> cb) {
        for (Member m : members) {
            if (m.gone) continue;
            m.gone = true;
            Ref<EntityStore> ref = m.ref();
            if (ref != null && ref.isValid() && fxGone != null) SpellFx.fx(fxGone, new Vector3d(m.lastPos).add(0, 1, 0), 1.0f, ref.getStore(), cb);
            SpellFx.remove(ref, cb);
            RuneSpellRuntime.forgetSummon(ref);
        }
    }

    void abort() {
        for (Member m : members) {
            RuneSpellRuntime.removeLater(m.ref());
            RuneSpellRuntime.forgetSummon(m.ref());
        }
    }
}
