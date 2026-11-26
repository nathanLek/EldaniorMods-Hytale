package com.eldanior.system.familiers;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.familiers.Espece.Age;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Fenetre de gestion du familier (Familier/Oeuf.ui, generee par scripts/familiers/gen_familiers.py), ouverte par la touche
 * d'interaction sur son oeuf. Maitre : portrait, rarete, element, barres (vie, XP, croissance), fiche, competence et
 * prochaine evolution ; Faire evoluer (adulte, une fois), Nourrir, Me suivre / Rester ici, Relacher (deux clics).
 * Administrateur : en plus, outils de test, aussi sur l'oeuf d'un autre joueur.
 */
public class FamilierPage extends InteractiveCustomUIPage<FamilierPage.Data> {

    private static final String[] BUTTONS = {"Follow", "Feed", "Release", "Close", "EvolveBtn", "AdmHatch", "AdmAge", "AdmElem",
            "AdmSkill", "AdmHeal", "AdmLvlDown", "AdmLvlUp", "AdmLvlMax", "AdmMalus"};

    private final UUID owner;
    private final boolean admin;
    private boolean confirmRelease;
    private String message = "";

    public FamilierPage(@Nonnull PlayerRef playerRef, UUID owner, boolean admin) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.owner = owner;
        this.admin = admin;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Familier/Oeuf.ui");
        for (String b : BUTTONS)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#Fam" + b, EventData.of("Action", b), false);
        fill(ui, ref, store);
    }

    private void fill(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        long now = System.currentTimeMillis();
        Familier f = FamilierManager.of(owner);
        World world = store.getExternalData().getWorld();
        ui.set("#FamAdmin.Visible", admin);
        ui.set("#FamMsg.Text", message);
        if (f == null) {
            ui.set("#FamName.Text", "Lien rompu");
            for (String id : new String[]{"#FamRarity", "#FamSpecies", "#FamElement", "#FamSkill", "#FamSkillDesc", "#FamNext",
                    "#FamHpText", "#FamXpText", "#FamAgeText"}) ui.set(id + ".Text", "");
            ui.set("#FamState.Text", "Ce familier n'est plus lié à personne.");
            for (int i = 0; i < 4; i++) ui.set("#FamVal" + i + ".Text", "—");
            for (String b : new String[]{"#FamFollow", "#FamFeed", "#FamRelease", "#FamEvolve"}) ui.set(b + ".Visible", false);
            return;
        }
        boolean mine = owner.equals(playerRef.getUuid());
        boolean hatched = f.hatched();
        Age age = f.age(now);
        Espece e = f.espece;

        // ---- en-tete ----
        ui.set("#FamPortrait.ItemId", e.eggItem);
        ui.set("#FamName.Text", f.name + (mine ? "" : " — " + f.ownerName));
        ui.set("#FamRarity.Text", e.rarete.label);
        ui.set("#FamRarity.Style.TextColor", e.rarete.color);
        ui.set("#FamSpecies.Text", e.label + (hatched ? " · " + age.label : " · dans l'œuf"));
        ui.set("#FamElement.Text", f.element != null ? "Élément " + f.element.label : hatched ? "Sans élément" : "Élément inconnu");
        ui.set("#FamElement.Style.TextColor", f.element != null ? f.element.color : "#8a9aac");
        ui.set("#FamState.Text", FamilierManager.whereabouts(f, now));

        // ---- barres ----
        float hp = FamilierManager.hp(f, world);
        float maxHp = f.maxHp(age);
        ui.set("#FamHpBar.Value", hatched ? hp : 0f);
        ui.set("#FamHpText.Text", hatched ? Math.round(maxHp * hp) + " / " + Math.round(maxHp) + " PV" : "—");
        boolean max = f.level >= f.maxLevel();
        ui.set("#FamXpBar.Value", max ? 1f : Math.min(1f, f.xp / (float) Math.max(1, f.xpToNext())));
        ui.set("#FamXpText.Text", "Niveau " + f.level + " / " + f.maxLevel() + (max ? " (maximum)" : " · " + f.xp + " / " + f.xpToNext() + " XP"));
        ui.set("#FamAgeBar.Value", FamilierManager.growth(f, now));
        ui.set("#FamAgeText.Text", !hatched ? "Incubation" : age == Age.ADULTE ? "Adulte" : age.label + " → "
                + (age == Age.JEUNE ? Age.ADOLESCENT.label : Age.ADULTE.label));

        // ---- fiche ----
        ui.set("#FamVal0.Text", hatched ? age.label : "Dans l'œuf");
        ui.set("#FamVal1.Text", hatched ? Math.round(maxHp) + " PV · morsure " + Math.round(f.damage(age)) : "—");
        ui.set("#FamVal2.Text", e.mountFrom == null ? "Ne se monte pas" : hatched && e.mountable(age)
                ? "Prêt : touche d'interaction sur lui, puis Chevaucher" : "À partir de l'âge " + e.mountFrom.label.toLowerCase());
        Player player = store.getComponent(ref, Player.getComponentType());
        FamilierManager.Food food = FamilierManager.bestFood(player);
        long fedLeft = Math.max(0, 60_000 - (now - f.fedAt));
        ui.set("#FamVal3.Text", fedLeft > 0 ? "Repu (encore " + FamilierManager.duration(fedLeft) + ")"
                : food != null ? "Dans votre sac : " + food.label().toLowerCase() : "Rien dans votre sac (viande, poisson)");

        // ---- competence et evolution ----
        Competence skill = f.skill;
        ui.set("#FamSkill.Text", FamilierManager.skills(f, now));
        ui.set("#FamSkill.Style.TextColor", skill != null ? skill.color : "#8a9aac");
        ui.set("#FamSkillDesc.Text", skill != null ? skill.description : "Il ne lance aucune compétence. Adulte, faites-le évoluer : "
                + "il en apprendra une, tirée au hasard selon son espèce et son élément.");
        ui.set("#FamNext.Text", FamilierManager.nextEvolution(f, now));
        boolean evolve = FamilierManager.canEvolve(f, now) && (mine || admin);
        ui.set("#FamEvolve.Visible", evolve);
        if (evolve) ui.set("#FamEvolveText.Text", "Une compétence parmi : " + FamilierManager.skillPool(f) + ".");

        // ---- boutons ----
        ui.set("#FamFollow.Visible", hatched && f.hasEgg());
        ui.set("#FamFollow.Text", f.following || f.waiting ? "Rester ici" : "Me suivre");
        ui.set("#FamFeed.Visible", hatched && (mine || admin));
        ui.set("#FamRelease.Visible", mine || admin);
        ui.set("#FamRelease.Text", confirmRelease ? "Confirmer" : "Relâcher");
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        try {
            String action = data.action != null ? data.action : "";
            if (action.equals("Close")) { close(); return; }
            Familier f = FamilierManager.of(owner);
            boolean mine = owner.equals(playerRef.getUuid());
            World world = store.getExternalData().getWorld();
            if (!action.equals("Release")) confirmRelease = false;
            if (action.startsWith("Adm") && !admin) return;
            switch (action) {
                case "Follow" -> message = f != null && (mine || admin) ? FamilierManager.toggleFollow(f, world) : "";
                case "Feed" -> message = f != null && (mine || admin)
                        ? FamilierManager.feed(owner, store.getComponent(ref, Player.getComponentType()), world) : "";
                case "EvolveBtn" -> message = mine || admin ? FamilierManager.evolve(owner, false) : "";
                case "Release" -> {
                    if (f == null || !(mine || admin)) break;
                    if (!confirmRelease) {
                        confirmRelease = true;
                        message = "Relâcher " + f.name + " rompt votre lien pour toujours (sans malus). Cliquez encore pour confirmer.";
                    } else {
                        confirmRelease = false;
                        FamilierManager.release(owner);
                        message = f.name + " retourne à la vie sauvage. Son œuf reste au sol, sans maître.";
                    }
                }
                case "AdmHatch" -> message = FamilierManager.hatchNow(owner) ? "Éclosion dans un instant." : "Pas d'œuf en incubation.";
                case "AdmAge" -> message = FamilierManager.cycleAge(owner);
                case "AdmElem" -> message = FamilierManager.cycleElement(owner);
                case "AdmSkill" -> message = FamilierManager.evolve(owner, true);
                case "AdmHeal" -> message = FamilierManager.healPet(owner, world);
                case "AdmLvlDown" -> message = FamilierManager.addLevels(owner, -10);
                case "AdmLvlUp" -> message = FamilierManager.addLevels(owner, 10);
                case "AdmLvlMax" -> message = FamilierManager.addLevels(owner, Integer.MAX_VALUE);
                case "AdmMalus" -> message = FamilierManager.clearMalus(owner) ? "Malus de deuil retiré." : "Aucun malus.";
                default -> { return; }
            }
            UICommandBuilder ui = new UICommandBuilder();
            fill(ui, ref, store);
            sendUpdate(ui);
        } catch (Exception e) {
            EldaniorLogger.error("[Familiers] fenetre de l'oeuf", e);
        }
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .build();
        public String action;
    }
}
