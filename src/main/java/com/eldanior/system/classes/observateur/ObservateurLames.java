package com.eldanior.system.classes.observateur;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Mumei et Kagero, Lames de l'Observateur (2026-11-17) : deux kodachi (objet dague, rendu dans les deux mains) que l'on porte
 * dans le dos, a l'horizontale au creux des reins, poignee de chaque cote (scripts/artefacts/gen_lames_observateur.py).
 *
 * Les fourreaux du dos sont un attachement (ModelAttachment) ajoute au modele du joueur, racine "Belly" collee a l'os du
 * ventre : BACK_FULL (lames rengainees) tant que l'arme est dans l'inventaire sans etre en main, BACK_EMPTY (fourreaux vides)
 * quand elle est en main, rien sinon. Le modele n'est touche que si c'est celui du joueur ("Player") : une transformation
 * (avatar du dragon...) garde le sien, et l'attachement revient quand le modele du joueur est rendu.
 * Reservee a l'Observateur : MasterySystem (ARTIFACT_CLASSES) ; donnee a la nomination (ObservateurManager.accept).
 */
public class ObservateurLames extends DelayedEntitySystem<EntityStore> {

    public static final String ITEM = "Artefact_Daggers_Observateur";
    private static final String DIR = "Items/Weapons/artefacts/";
    static final String BACK_FULL = DIR + "Observateur_Fourreaux_Dos.blockymodel";
    static final String BACK_EMPTY = DIR + "Observateur_Fourreaux_Dos_Vide.blockymodel";
    private static final String BACK_TEXTURE = DIR + "Observateur_Fourreaux_Dos.png";
    private static final String PLAYER_MODEL = "Player";

    public ObservateurLames() { super(0.25f); }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (!ref.isValid()) return;
        Player player = store.getComponent(ref, Player.getComponentType());
        ModelComponent mc = store.getComponent(ref, ModelComponent.getComponentType());
        if (player == null || player.getInventory() == null || mc == null || mc.getModel() == null) return;
        Model model = mc.getModel();
        if (!PLAYER_MODEL.equals(model.getModelAssetId())) return;              // transformation en cours : on n'y touche pas

        String wanted = wanted(player);
        String current = null;
        if (model.getAttachments() != null)
            for (ModelAttachment a : model.getAttachments()) if (isBack(a)) { current = a.getModel(); break; }
        if (wanted == null ? current == null : wanted.equals(current)) return;

        cb.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(withBack(model, wanted)));
        PlayerSkinComponent skin = store.getComponent(ref, PlayerSkinComponent.getComponentType());
        if (skin != null) skin.setNetworkOutdated();                            // comme DragonAvatar : les clients redessinent le skin
    }

    /** Fourreaux a montrer : vides si les lames sont en main, pleins si elles sont dans l'inventaire, sinon aucun. */
    private static String wanted(Player player) {
        ItemStack held = player.getInventory().getActiveHotbarItem();
        if (held != null && !held.isEmpty() && ITEM.equals(held.getItemId())) return BACK_EMPTY;
        ItemContainer bag = player.getInventory().getCombinedBackpackStorageHotbar();
        if (bag == null) return null;
        for (short slot = 0; slot < bag.getCapacity(); slot++) {
            ItemStack stack = bag.getItemStack(slot);
            if (stack != null && !stack.isEmpty() && ITEM.equals(stack.getItemId())) return BACK_FULL;
        }
        return null;
    }

    private static boolean isBack(ModelAttachment a) {
        return a != null && (BACK_FULL.equals(a.getModel()) || BACK_EMPTY.equals(a.getModel()));
    }

    /** Copie du modele (memes champs, comme MonsterContracts.dress), nos fourreaux retires puis remis selon back (null : aucun). */
    private static Model withBack(Model m, String back) {
        List<ModelAttachment> atts = new ArrayList<>();
        if (m.getAttachments() != null) for (ModelAttachment a : m.getAttachments()) if (!isBack(a)) atts.add(a);
        if (back != null) atts.add(new ModelAttachment(back, BACK_TEXTURE, null, null, 1));
        return new Model(m.getModelAssetId(), m.getScale(), m.getRandomAttachmentIds(), atts.toArray(new ModelAttachment[0]),
                m.getBoundingBox(), m.getModel(), m.getTexture(), m.getGradientSet(), m.getGradientId(), m.getEyeHeight(),
                m.getCrouchOffset(), m.getSittingOffset(), m.getSleepingOffset(), m.getAnimationSetMap(), m.getCamera(), m.getLight(),
                m.getParticles(), m.getTrails(), m.getPhysicsValues(), m.getDetailBoxes(), m.getPhobia(), m.getPhobiaModelAssetId());
    }

    /** Donne les lames au joueur s'il ne les a pas deja (nomination). true si elles ont ete ajoutees. */
    static boolean give(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || player.getInventory() == null) return false;
        ItemContainer bag = player.getInventory().getCombinedBackpackStorageHotbar();
        for (short slot = 0; slot < bag.getCapacity(); slot++) {
            ItemStack stack = bag.getItemStack(slot);
            if (stack != null && !stack.isEmpty() && ITEM.equals(stack.getItemId())) return false;
        }
        return bag.addItemStack(new ItemStack(ITEM, 1)).succeeded();
    }

    @Override
    public Query<EntityStore> getQuery() { return Player.getComponentType(); }
}
