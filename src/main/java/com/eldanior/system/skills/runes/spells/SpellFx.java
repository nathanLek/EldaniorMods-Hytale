package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.party.Party;
import com.eldanior.system.party.PartyManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Outils communs aux sorts divins (Sanctuaire de Taranis, Gilgamesh Babylon, Porte de Niflheim) :
 * particules envoyees aux joueurs proches, sons, choix des cibles (allies epargnes), degats + knockback.
 */
public final class SpellFx {

    private SpellFx() {}

    /**
     * Rayon commun des grands sorts de zone (Porte de Niflheim, Eveil d'Yggdrasil, Reveil de Gaia, Courroux de
     * Muspelheim, Sanctuaire de Taranis, Abime du Chaos, Sablier de Chronos) : tous couvrent la meme surface.
     */
    static final double DIVINE_ZONE_RADIUS = 35.0;

    /** Rayon d'envoi des particules (ParticleUtil seul se limite a 75 blocs). */
    static final double VIEW_RADIUS = 200.0;

    /** Affiche une particule pour tous les joueurs proches. Retourne le nombre de joueurs servis. */
    static int fx(String id, Vector3d pos, float scale, Store<EntityStore> store, ComponentAccessor<EntityStore> accessor) {
        try {
            List<Ref<EntityStore>> viewers = new ArrayList<>();
            for (Ref<EntityStore> ref : TargetUtil.getAllEntitiesInSphere(pos, VIEW_RADIUS, store)) {
                if (ref != null && ref.isValid() && store.getComponent(ref, Player.getComponentType()) != null) viewers.add(ref);
            }
            if (viewers.isEmpty()) return 0;
            ParticleUtil.spawnParticleEffect(id, pos.x, pos.y, pos.z, 0f, 0f, 0f, scale, null, null, viewers, accessor, 0f);
            return viewers.size();
        } catch (Exception e) {
            EldaniorLogger.error("SpellFx: particule " + id, e);
            return 0;
        }
    }

    /** Particule orientee (lacet / tangage en degres), pour tous les joueurs proches. */
    static void fx(String id, Vector3d pos, float yaw, float pitch, float scale, Store<EntityStore> store, ComponentAccessor<EntityStore> accessor) {
        try {
            List<Ref<EntityStore>> viewers = new ArrayList<>();
            for (Ref<EntityStore> ref : TargetUtil.getAllEntitiesInSphere(pos, VIEW_RADIUS, store)) {
                if (ref != null && ref.isValid() && store.getComponent(ref, Player.getComponentType()) != null) viewers.add(ref);
            }
            if (viewers.isEmpty()) return;
            ParticleUtil.spawnParticleEffect(id, pos.x, pos.y, pos.z, yaw, pitch, 0f, scale, null, null, viewers, accessor, 0f);
        } catch (Exception e) {
            EldaniorLogger.error("SpellFx: particule " + id, e);
        }
    }

    static void sound(String id, Vector3d pos, ComponentAccessor<EntityStore> accessor) {
        int index = SoundEvent.getAssetMap().getIndex(id);
        if (index < 0) return;
        try {
            SoundUtil.playSoundEvent3d(index, SoundCategory.SFX, pos, accessor);
        } catch (Exception e) {
            EldaniorLogger.error("SpellFx: son " + id, e);
        }
    }

    /**
     * Son non positionne (2D) joue a chaque joueur dans le rayon. Pour les sons stereo (ex. tonnerre vanilla),
     * refuses en son positionne.
     */
    static void sound2dNear(String id, Vector3d pos, double radius, Store<EntityStore> store) {
        int index = SoundEvent.getAssetMap().getIndex(id);
        if (index < 0) return;
        try {
            for (Ref<EntityStore> ref : TargetUtil.getAllEntitiesInSphere(pos, radius, store)) {
                if (ref == null || !ref.isValid()) continue;
                PlayerRef player = store.getComponent(ref, PlayerRef.getComponentType());
                if (player != null) SoundUtil.playSoundEvent2dToPlayer(player, index, SoundCategory.SFX);
            }
        } catch (Exception e) {
            EldaniorLogger.error("SpellFx: son 2D " + id, e);
        }
    }

    /** Tremblement de camera (asset CameraEffect) pour les joueurs dans le rayon, plus fort pres du centre. */
    static void shake(Vector3d pos, double radius, String cameraEffectId, Store<EntityStore> store) {
        try {
            var effect = com.hypixel.hytale.server.core.asset.type.camera.CameraEffect.getAssetMap().getAsset(cameraEffectId);
            if (effect == null) return;
            for (Ref<EntityStore> ref : TargetUtil.getAllEntitiesInSphere(pos, radius, store)) {
                if (ref == null || !ref.isValid()) continue;
                PlayerRef player = store.getComponent(ref, PlayerRef.getComponentType());
                Vector3d p = position(ref, store);
                if (player == null || p == null) continue;
                float intensity = (float) Math.max(0.15, 1.0 - p.distance(pos) / radius);
                player.getPacketHandler().writeNoCache(effect.createCameraShakePacket(intensity));
            }
        } catch (Exception e) {
            EldaniorLogger.error("SpellFx: tremblement " + cameraEffectId, e);
        }
    }

    /** Ennemi valide : joueur ou creature, ni le lanceur ni un allie (groupe, guilde, famille). */
    static boolean isEnemy(Ref<EntityStore> caster, Ref<EntityStore> target, Store<EntityStore> store) {
        if (target == null || !target.isValid() || target.equals(caster)) return false;
        boolean isPlayer = store.getComponent(target, Player.getComponentType()) != null;
        boolean isNpc = store.getComponent(target, NPCEntity.getComponentType()) != null;
        if (!isPlayer && !isNpc) return false;
        return !(isPlayer && isAlly(caster, target, store));
    }

    static boolean isAlly(Ref<EntityStore> caster, Ref<EntityStore> target, Store<EntityStore> store) {
        UUID a = uuid(caster, store), b = uuid(target, store);
        if (a != null && b != null) {
            Party pa = PartyManager.getParty(a);
            if (pa != null && pa == PartyManager.getParty(b)) return true;
        }
        var type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData da = store.getComponent(caster, type), db = store.getComponent(target, type);
        if (da == null || db == null) return false;
        return same(da.getGuildId(), db.getGuildId()) || same(da.getNobleFamilyId(), db.getNobleFamilyId());
    }

    private static boolean same(String x, String y) {
        return x != null && !x.isEmpty() && x.equals(y);
    }

    /** Ennemis dans une sphere (copie de la liste : celle de TargetUtil est partagee par thread). */
    static List<Ref<EntityStore>> enemiesAround(Ref<EntityStore> caster, Vector3d center, double radius, Store<EntityStore> store) {
        List<Ref<EntityStore>> out = new ArrayList<>();
        for (Ref<EntityStore> ref : TargetUtil.getAllEntitiesInSphere(center, radius, store)) {
            if (isEnemy(caster, ref, store)) out.add(ref);
        }
        return out;
    }

    /**
     * Degats attribues au lanceur. Knockback optionnel : pose sur la cible (c'est le systeme de knockback qui le lit),
     * comme le fait DamageEntityInteraction.
     */
    static void damage(Ref<EntityStore> caster, Ref<EntityStore> target, String causeId, float amount,
                       Vector3d knockbackVelocity, String cameraEffect, CommandBuffer<EntityStore> cb) {
        try {
            Damage damage = new Damage(new Damage.EntitySource(caster), DamageCause.getAssetMap().getIndex(causeId), amount);
            if (knockbackVelocity != null) {
                KnockbackComponent knockback = new KnockbackComponent();
                knockback.setVelocity(knockbackVelocity);
                knockback.setVelocityType(ChangeVelocityType.Set);
                VelocityConfig velocity = new VelocityConfig();
                velocity.setGroundResistanceMax(0f);
                velocity.setThreshold(0.25f);
                knockback.setVelocityConfig(velocity);
                damage.putMetaObject(Damage.KNOCKBACK_COMPONENT, knockback);
                cb.putComponent(target, KnockbackComponent.getComponentType(), knockback);
            }
            if (cameraEffect != null) {
                int camera = com.hypixel.hytale.server.core.asset.type.camera.CameraEffect.getAssetMap().getIndex(cameraEffect);
                if (camera >= 0) damage.putMetaObject(Damage.CAMERA_EFFECT, new Damage.CameraEffect(camera));
            }
            DamageSystems.executeDamage(target, cb, damage);
        } catch (Exception e) {
            EldaniorLogger.error("SpellFx: degats", e);
        }
    }

    /**
     * Pousse une entite sans la frapper (le systeme de recul du moteur applique la vitesse, joueurs compris).
     * add : la vitesse s'ajoute a la sienne (sinon elle la remplace).
     */
    static void push(Ref<EntityStore> target, Vector3d velocity, boolean add, CommandBuffer<EntityStore> cb) {
        if (target == null || !target.isValid()) return;
        try {
            KnockbackComponent knockback = new KnockbackComponent();
            knockback.setVelocity(velocity);
            knockback.setVelocityType(add ? ChangeVelocityType.Add : ChangeVelocityType.Set);
            VelocityConfig velocity2 = new VelocityConfig();
            velocity2.setGroundResistanceMax(0f);
            velocity2.setThreshold(0.25f);
            knockback.setVelocityConfig(velocity2);
            cb.putComponent(target, KnockbackComponent.getComponentType(), knockback);
        } catch (Exception e) {
            EldaniorLogger.error("SpellFx: poussee", e);
        }
    }

    static void effect(Ref<EntityStore> target, String effectId, ComponentAccessor<EntityStore> cb) {
        try {
            if (!EffectsManager.applyEffect(target, effectId, cb)) {
                EldaniorLogger.warn("[SortsDivins] Effet non applique (" + effectId + " introuvable ?)");
            }
        } catch (Exception e) {
            EldaniorLogger.error("SpellFx: effet " + effectId, e);
        }
    }

    static Vector3d position(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        if (ref == null || !ref.isValid()) return null;                       // entite retiree par le moteur (mort, chunk decharge...)
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        return t != null ? new Vector3d(t.getPosition()) : null;
    }

    static Vector3d lookDirection(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        if (ref == null || !ref.isValid()) return new Vector3d(0, 0, 1);
        HeadRotation head = store.getComponent(ref, HeadRotation.getComponentType());
        return head != null ? new Vector3d(head.getDirection()).normalize() : new Vector3d(0, 0, 1);
    }

    /** Point vise : bloc vise dans la portee, sinon `fallback` blocs devant le joueur, ramene au sol. */
    static Vector3d targetPoint(Ref<EntityStore> caster, Store<EntityStore> store, double range, double fallback) {
        Vector3i block = TargetUtil.getTargetBlock(caster, range, store);
        if (block != null) return new Vector3d(block.x + 0.5, block.y + 1.0, block.z + 0.5);
        Vector3d pos = position(caster, store);
        Vector3d dir = lookDirection(caster, store);
        dir.y = 0;
        if (dir.lengthSquared() < 1e-6) dir.set(0, 0, 1);
        dir.normalize().mul(fallback);
        return ground(world(store), new Vector3d(pos != null ? pos : new Vector3d()).add(dir));
    }

    /**
     * Ennemi sous le curseur : le plus proche le long du regard (a moins de 1.6 bloc de l'axe), jusqu'a range blocs,
     * sans traverser un bloc. null si aucun.
     */
    static Ref<EntityStore> targetEntity(Ref<EntityStore> caster, Store<EntityStore> store, double range) {
        return rayEntity(caster, store, range, true);
    }

    /** Creature ou joueur (allie compris, jamais le lanceur) sous le curseur, ou null. */
    static Ref<EntityStore> targetLiving(Ref<EntityStore> caster, Store<EntityStore> store, double range) {
        return rayEntity(caster, store, range, false);
    }

    private static Ref<EntityStore> rayEntity(Ref<EntityStore> caster, Store<EntityStore> store, double range, boolean enemiesOnly) {
        Vector3d pos = position(caster, store);
        if (pos == null) return null;
        Vector3d eye = new Vector3d(pos).add(0, 1.6, 0), dir = lookDirection(caster, store);
        Vector3i block = TargetUtil.getTargetBlock(caster, range, store);
        double wall = block == null ? range : Math.max(0, new Vector3d(block.x + 0.5, block.y + 0.5, block.z + 0.5).distance(eye) - 0.5);
        Ref<EntityStore> best = null;
        double bestT = Double.MAX_VALUE;
        Vector3d mid = new Vector3d(eye).add(new Vector3d(dir).mul(range / 2));
        List<Ref<EntityStore>> candidates = enemiesOnly ? enemiesAround(caster, mid, range / 2 + 2, store) : TargetUtil.getAllEntitiesInSphere(mid, range / 2 + 2, store);
        for (Ref<EntityStore> e : candidates) {
            if (e == null || !e.isValid() || e.equals(caster)) continue;
            if (!enemiesOnly && store.getComponent(e, Player.getComponentType()) == null && store.getComponent(e, NPCEntity.getComponentType()) == null) continue;
            Vector3d p = position(e, store);
            if (p == null) continue;
            Vector3d to = new Vector3d(p).add(0, 1.0, 0).sub(eye);
            double t = to.dot(dir);
            if (t < 0 || t > range || t > wall + 1.0) continue;
            if (new Vector3d(to).sub(new Vector3d(dir).mul(t)).length() > 1.6) continue;
            if (t < bestT) { bestT = t; best = e; }
        }
        return best;
    }

    /** Ramene le point sur le premier bloc solide en dessous (ou juste au-dessus). */
    static Vector3d ground(World world, Vector3d point) {
        Vector3d hit = TargetUtil.getTargetLocation(world, id -> id != 0, point.x, point.y + 12, point.z, 0, -1, 0, 40);
        return hit != null ? new Vector3d(point.x, hit.y, point.z) : new Vector3d(point);
    }

    // ===================== GEL DES CREATURES =====================
    // Les effets de gel (MovementEffects.DisableAll) ne bloquent que les joueurs (cote client) : l'IA des monstres
    // continue de les deplacer. On fige donc leur position, orientation et elan a chaque tick jusqu'a la fin du gel.

    private record Lock(Vector3d pos, Rotation3f rot, long until) {}
    private static final Map<Ref<EntityStore>, Lock> locks = new ConcurrentHashMap<>();

    /** Fige une creature (non joueur) sur place pendant durationMs. */
    static void lockInPlace(Ref<EntityStore> target, long durationMs, Store<EntityStore> store) {
        if (target == null || !target.isValid() || store.getComponent(target, Player.getComponentType()) != null) return;
        TransformComponent t = store.getComponent(target, TransformComponent.getComponentType());
        if (t == null) return;
        locks.put(target, new Lock(new Vector3d(t.getPosition()), new Rotation3f(t.getRotation()), System.currentTimeMillis() + durationMs));
    }

    /** Libere une creature figee (fin anticipee du gel). */
    static void unlock(Ref<EntityStore> target) {
        locks.remove(target);
    }

    /** Re-applique les gels du monde de ce store (appele a chaque tick). */
    static void tickLocks(Store<EntityStore> store) {
        if (locks.isEmpty()) return;
        long now = System.currentTimeMillis();
        for (Map.Entry<Ref<EntityStore>, Lock> e : locks.entrySet()) {
            Ref<EntityStore> ref = e.getKey();
            Lock lock = e.getValue();
            if (!ref.isValid() || now >= lock.until()) { locks.remove(ref); continue; }
            if (ref.getStore() != store) continue;
            TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
            if (t != null) {
                t.setPosition(lock.pos());
                t.setRotation(new Rotation3f(lock.rot()));
            }
            Velocity v = store.getComponent(ref, Velocity.getComponentType());
            if (v != null) v.setZero();
        }
    }

    // ===================== OUTILS COMMUNS DES SORTS DIVINS =====================

    /** Ajoute a une statistique (vie, mana, endurance...) un pourcentage de son maximum. */
    static void restore(Ref<EntityStore> ref, int statIndex, float percentOfMax, ComponentAccessor<EntityStore> store) {
        try {
            var map = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap.getComponentType());
            if (map == null) return;
            var value = map.get(statIndex);
            if (value == null) return;
            map.addStatValue(statIndex, value.getMax() * percentOfMax);
        } catch (Exception e) {
            EldaniorLogger.error("SpellFx: restauration de statistique", e);
        }
    }

    /** Joueurs allies (le lanceur compris) dans une sphere. */
    static List<Ref<EntityStore>> alliesAround(Ref<EntityStore> caster, Vector3d center, double radius, Store<EntityStore> store) {
        List<Ref<EntityStore>> out = new ArrayList<>();
        for (Ref<EntityStore> ref : TargetUtil.getAllEntitiesInSphere(center, radius, store)) {
            if (ref == null || !ref.isValid() || store.getComponent(ref, Player.getComponentType()) == null) continue;
            if (ref.equals(caster) || isAlly(caster, ref, store)) out.add(ref);
        }
        return out;
    }

    static double horizontalDistance(Vector3d a, Vector3d b) {
        return Math.hypot(a.x - b.x, a.z - b.z);
    }

    /** Fait apparaitre un modele immobile porte par une entite projectile (config "Projectile_Config_..."). */
    static Ref<EntityStore> spawnModel(Ref<EntityStore> caster, String configId, Vector3d pos, Vector3d dir, CommandBuffer<EntityStore> cb) {
        var config = com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig.getAssetMap().getAsset(configId);
        if (config == null || caster == null || !caster.isValid()) {
            if (config == null) EldaniorLogger.error("SpellFx: projectile introuvable " + configId);
            return null;
        }
        try {
            return com.hypixel.hytale.server.core.modules.projectile.ProjectileModule.get()
                    .spawnProjectile(caster, cb, config, new Vector3d(pos), new Vector3d(dir));
        } catch (Exception e) {
            EldaniorLogger.error("SpellFx: " + configId, e);
            return null;
        }
    }

    /** Maintient un modele a une position (et orientation optionnelle), sans elan. */
    static void hold(Ref<EntityStore> ref, Vector3d pos, Rotation3f rot, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) return;
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        if (t == null) return;
        t.setPosition(pos);
        if (rot != null) t.setRotation(rot);
        Velocity v = store.getComponent(ref, Velocity.getComponentType());
        if (v != null) v.setZero();
    }

    /** Orientation (lacet seul, ou lacet + tangage) d'un modele tourne vers dir. */
    static Rotation3f facing(Vector3d dir, boolean withPitch) {
        Rotation3f rot = new Rotation3f();
        rot.setYaw(com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath.normalizeTurnAngle(
                com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath.headingFromDirection(dir.x, dir.z)));
        if (withPitch) rot.setPitch(com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath.pitchFromDirection(dir.x, dir.y, dir.z));
        return rot;
    }

    /**
     * Entite-bloc du moteur : le vrai bloc, rendu par le client, soumis a la gravite (ou deplace par le code via
     * hold). Null si elle n'a pas pu etre creee.
     */
    static Ref<EntityStore> spawnBlock(String blockId, Vector3d pos, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        try {
            var holder = com.hypixel.hytale.server.core.entity.entities.BlockEntity.assembleDefaultBlockEntity(
                    store.getResource(com.hypixel.hytale.server.core.modules.time.TimeResource.getResourceType()), blockId, new Vector3d(pos));
            return cb.addEntity(holder, com.hypixel.hytale.component.AddReason.SPAWN);
        } catch (Exception e) {
            EldaniorLogger.error("SpellFx: entite-bloc " + blockId, e);
            return null;
        }
    }

    /**
     * Remplace le modele d'une entite par une copie a l'echelle voulue (envoyee aux clients avec le modele : c'est ce
     * que fait MinScale/MaxScale). La boite de collision garde une taille fixe de 2*hitboxHalf blocs.
     */
    static void setModelScale(Ref<EntityStore> ref, String modelAssetId, float scale, double hitboxHalf, CommandBuffer<EntityStore> cb) {
        if (ref == null || !ref.isValid() || scale <= 0) return;
        var asset = com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset.getAssetMap().getAsset(modelAssetId);
        if (asset == null) return;
        var box = new com.hypixel.hytale.math.shape.Box(-hitboxHalf / scale, -0.2 / scale, -0.2 / scale, hitboxHalf / scale, 0.2 / scale, 0.2 / scale);
        cb.putComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.ModelComponent.getComponentType(),
                new com.hypixel.hytale.server.core.modules.entity.component.ModelComponent(
                        com.hypixel.hytale.server.core.asset.type.model.config.Model.createScaledModel(asset, scale, asset.generateRandomAttachmentIds(), box)));
    }

    static void remove(Ref<EntityStore> ref, CommandBuffer<EntityStore> cb) {
        if (ref != null && ref.isValid()) cb.tryRemoveEntity(ref, com.hypixel.hytale.component.RemoveReason.REMOVE);
    }

    /** Regard horizontal normalise du lanceur. */
    static Vector3d forward(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        Vector3d look = lookDirection(ref, store);
        Vector3d fwd = new Vector3d(look.x, 0, look.z);
        if (fwd.lengthSquared() < 1e-6) fwd.set(0, 0, 1);
        return fwd.normalize();
    }

    /** Teleporte proprement une entite (composant Teleport du moteur, synchronise avec le client). */
    static void teleport(Ref<EntityStore> ref, Vector3d pos, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        teleport(ref, pos, null, store, cb);
    }

    /** Teleporte en imposant l'orientation (null : garde l'orientation actuelle). */
    static void teleport(Ref<EntityStore> ref, Vector3d pos, Rotation3f facing, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (ref == null || !ref.isValid()) return;
        try {
            TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
            Rotation3f rot = facing != null ? facing : t != null ? new Rotation3f(t.getRotation()) : new Rotation3f();
            var teleport = store.getComponent(ref, Player.getComponentType()) != null
                    ? com.hypixel.hytale.server.core.modules.entity.teleport.Teleport.createForPlayer(new Vector3d(pos), rot)
                    : new com.hypixel.hytale.server.core.modules.entity.teleport.Teleport(new Vector3d(pos), rot);
            cb.putComponent(ref, com.hypixel.hytale.server.core.modules.entity.teleport.Teleport.getComponentType(), teleport);
        } catch (Exception e) {
            EldaniorLogger.error("SpellFx: teleportation", e);
        }
    }

    /** Identifiants des chaines d'interaction actives du type donne (Primary = attaque, Secondary = clic droit). */
    static java.util.Set<Integer> chains(Ref<EntityStore> ref, Store<EntityStore> store, com.hypixel.hytale.protocol.InteractionType type) {
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        try {
            var manager = store.getComponent(ref, com.hypixel.hytale.server.core.modules.interaction.InteractionModule.get().getInteractionManagerComponent());
            if (manager == null) return ids;
            for (var chain : manager.getChains().values()) if (chain != null && chain.getType() == type) ids.add(chain.getChainId());
        } catch (Exception e) {
            EldaniorLogger.error("SpellFx: lecture des interactions", e);
        }
        return ids;
    }

    // Immunite aux degats de chute (sorts qui projettent le lanceur), lue par FallDamageSystem
    private static final Map<UUID, Long> fallProtectedUntil = new ConcurrentHashMap<>();

    static void protectFromFall(Ref<EntityStore> ref, long durationMs, ComponentAccessor<EntityStore> store) {
        UUID uuid = uuid(ref, store);
        if (uuid != null) fallProtectedUntil.put(uuid, System.currentTimeMillis() + durationMs);
    }

    public static boolean isFallProtected(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        if (ref == null || !ref.isValid() || fallProtectedUntil.isEmpty()) return false;
        UUID uuid = uuid(ref, store);
        Long until = uuid != null ? fallProtectedUntil.get(uuid) : null;
        if (until == null) return false;
        if (System.currentTimeMillis() > until) { fallProtectedUntil.remove(uuid); return false; }
        return true;
    }

    static World world(Store<EntityStore> store) {
        return ((EntityStore) store.getExternalData()).getWorld();
    }

    static UUID uuid(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        if (ref == null || !ref.isValid()) return null;
        PlayerRef netRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (netRef == null) return null;
        try { return UUIDExtractor.getUUID(netRef); }
        catch (Exception e) { return null; }
    }
}
