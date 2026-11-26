package com.eldanior.system.skills.runes;

import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Regles des runes (items portant un bloc "Ability", equipables via la fenetre du Bench_Abilities).
 *
 * - Restriction de classe (meme format que SkillModel.requiredClass : "warrior, dragon", "all"...).
 *   Une rune absente de {@link #RUNE_CLASSES} n'est pas restreinte (runes Support, runes debug...).
 * - Parchemins de rune : appris via ConsumableItemSkillInteraction, la rune va dans le sac de runes.
 * - Purge des runes non autorisees au changement de classe, wipe complet au reset niveau 1.
 */
public final class RuneManager {

    private RuneManager() {}

    public static final String AURA_RUNE_ID = "Rune_Aura";
    public static final int AURA_RUNE_MIN_DIGNITY = 5;

    /** Rune -> classes autorisees. A ajuster ici pour equilibrer. */
    private static final Map<String, String> RUNE_CLASSES = Map.ofEntries(
            Map.entry("Rune_Enrage",          "warrior, dragon"),
            Map.entry("Rune_GroundSlam",      "warrior, dragon"),
            Map.entry("Rune_AuraBlade",       "warrior, dragon"),
            Map.entry("Rune_Fireball",        "mage, dragon"),
            Map.entry("Rune_ChargedShot",     "archer, dragon"),
            Map.entry("Rune_ChainHook",       "archer, dragon"),
            Map.entry("Rune_PoisonImbue",     "assassin, dragon"),
            Map.entry("Rune_WindStrike",      "assassin, dragon"),
            Map.entry(AURA_RUNE_ID,           "all"),
            Map.entry("Rune_CourrouxMuspelheim", "mage, dragon"),
            Map.entry("Rune_SanctuaireTaranis", "mage, dragon"),
            Map.entry("Rune_AvenementBahamut", "dragon"),
            Map.entry("Rune_AvatarDragon", "dragon"),
            Map.entry("Rune_SablierChronos", "mage, marchand, dragon"),
            Map.entry("Rune_AbimeChaos", "mage, assassin, dragon"),
            Map.entry("Rune_PontBifrost", "mage, assassin, archer, dragon"),
            Map.entry("Rune_VoileNyx", "assassin, mage, dragon"),
            Map.entry("Rune_SanctuaireApollon", "mage, warrior, dragon"),
            Map.entry("Rune_CourrouxFujin", "archer, assassin, dragon"),
            Map.entry("Rune_ReveilGaia", "warrior, dragon"),
            Map.entry("Rune_EveilYggdrasil", "mage, marchand, dragon"),
            Map.entry("Rune_GilgameshBabylon",      "warrior, dragon"),
            Map.entry("Rune_PorteNiflheim",       "mage, dragon"),
            Map.entry("Rune_Bourrasque", "mage, archer, assassin, dragon"),
            Map.entry("Rune_PuitsGravitationnel", "mage, assassin, dragon"),
            Map.entry("Rune_AutoriteGravitationnelle", "mage, assassin, dragon"),
            Map.entry("Rune_Telekinesie", "mage, assassin, dragon"),
            Map.entry("Rune_Clonage", "mage, dragon, warrior, assassin, archer"),
            Map.entry("Rune_Doppelganger", "mage, dragon, warrior, assassin, archer"),
            Map.entry("Rune_OndeRepulsion", "mage"),
            Map.entry("Rune_Flammeche", "mage"),
            Map.entry("Rune_MurDeFlammes", "mage"),
            Map.entry("Rune_MarqueInfernale", "mage"),
            Map.entry("Rune_AuraDeBraise", "mage"),
            Map.entry("Rune_EclatDeGivre", "mage"),
            Map.entry("Rune_LanceDeCristal", "mage"),
            Map.entry("Rune_PrisonDeGlace", "mage"),
            Map.entry("Rune_SentierDHiver", "mage"),
            Map.entry("Rune_Decharge", "mage"),
            Map.entry("Rune_ChaineDEclairs", "mage"),
            Map.entry("Rune_OrbeStatique", "mage"),
            Map.entry("Rune_Blizzard", "mage"),
            Map.entry("Rune_DomaineElementaire", "mage"),
            Map.entry("Rune_TremblementTectonique", "mage"),
            Map.entry("Rune_DanseDesVents", "mage"),
            Map.entry("Rune_HaloProtecteur", "mage"),
            Map.entry("Rune_ArmeBenie", "mage"),
            Map.entry("Rune_GlyphesFoudroyants", "mage"),
            Map.entry("Rune_EgideRoyale", "mage"),
            Map.entry("Rune_SoinMineur", "mage"),
            Map.entry("Rune_FontaineDeVie", "mage"),
            Map.entry("Rune_LanceDeLumiere", "mage"),
            Map.entry("Rune_LienDAme", "mage"),
            Map.entry("Rune_EspritFollet", "mage"),
            Map.entry("Rune_GolemDePierre", "mage"),
            Map.entry("Rune_SentinelleDeGivre", "mage"),
            Map.entry("Rune_MeuteSpectrale", "mage"),
            Map.entry("Rune_MarqueFuneste", "mage"),
            Map.entry("Rune_PuitsDAmes", "mage"),
            Map.entry("Rune_ReleveDesMorts", "mage"),
            Map.entry("Rune_AmesErrantes", "mage"),
            Map.entry("Rune_Phylactere", "mage"),
            Map.entry("Rune_DomaineDeLaMort", "mage"),
            Map.entry("Rune_FormeSpectrale", "mage"),
            Map.entry("Rune_ArmeeDesTombes", "mage"),
            Map.entry("Rune_MiroirTrompeur", "mage"),
            Map.entry("Rune_GalerieDesGlaces", "mage"),
            Map.entry("Rune_Hallucination", "mage"),
            Map.entry("Rune_ChainesNoires", "mage"),
            Map.entry("Rune_PacteDeSang", "mage"),
            Map.entry("Rune_Marionnette", "mage"),
            Map.entry("Rune_Ruee", "warrior, dragon"),
            Map.entry("Rune_VoileDeFumee", "assassin, dragon"),
            Map.entry("Rune_PluieDeFleches", "archer, dragon"),
            Map.entry("Rune_LancerDePieces", "merchant, dragon"),
            Map.entry("Rune_ProjectionAstrale", "mage"),
            Map.entry("Rune_CeintureAsteroides", "mage"),
            Map.entry("Rune_Prescience", "mage"),
            Map.entry("Rune_RayonCeleste", "mage"),
            Map.entry("Rune_Distorsion", "mage"),
            Map.entry("Rune_RetourEnArriere", "mage"),
            Map.entry("Rune_OeilOmniscient", "mage"),
            Map.entry("Rune_Sentence", "mage"),
            Map.entry("Rune_BulleTemporelle", "mage"),
            Map.entry("Rune_ArretDuTemps", "mage"),
            Map.entry("Rune_Surregime", "mage"),
            Map.entry("Rune_EchosDuPasse", "mage"),
            Map.entry("Rune_Meteore", "mage"),
            Map.entry("Rune_RayonArcanique", "mage"),
            Map.entry("Rune_AvatarOrage", "mage"),
            Map.entry("Rune_Eternite", "mage"),
            Map.entry("Rune_SalveArcanique", "mage"),
            Map.entry("Rune_NuageOrage", "mage"),
            Map.entry("Rune_Reflexion", "mage"),
            Map.entry("Rune_Decret", "mage"),
            Map.entry("Rune_Meditation", "mage"),
            Map.entry("Rune_SanctuaireMineur", "mage"),
            Map.entry("Rune_SavoirAccelere", "mage"),
            Map.entry("Rune_Enseignement", "mage"),
            Map.entry("Rune_Miracle", "mage"),
            Map.entry("Rune_PilierAscendant", "mage"),
            Map.entry("Rune_Inversion", "mage"),
            Map.entry("Rune_Rempart", "mage"),
            Map.entry("Rune_SablesMouvants", "mage"),
            Map.entry("Rune_NuageToxique", "mage"),
            Map.entry("Rune_PierrePhilosophale", "mage"),
            Map.entry("Rune_Petrification", "mage"),
            Map.entry("Rune_Racines", "mage"),
            Map.entry("Rune_Bosquet", "mage"),
            Map.entry("Rune_EspritForet", "mage"),
            Map.entry("Rune_TempeteFeuilles", "mage"),
            Map.entry("Rune_LentilleGravitationnelle", "mage"),
            Map.entry("Rune_CouronneAutorite", "mage"),
            Map.entry("Rune_LoiArcanique", "mage"),
            Map.entry("Rune_PilierDuMonde", "mage"),
            Map.entry("Rune_Bannissement", "mage"),
            Map.entry("Rune_Effacement", "mage"),
            Map.entry("Rune_FissureVide", "mage"),
            Map.entry("Rune_SilenceNeant", "mage"),
            Map.entry("Rune_EruptionPrimordiale", "mage"),
            Map.entry("Rune_SculpteurDeGlace", "mage"),
            Map.entry("Rune_OndeOriginelle", "mage"),
            Map.entry("Rune_Resurrection", "mage"),
            Map.entry("Rune_GolemsCristal", "mage"),
            Map.entry("Rune_Faconnage", "mage"),
            Map.entry("Rune_ColossePierre", "mage"),
            Map.entry("Rune_MatiereVive", "mage"),
            Map.entry("Rune_Reecriture", "mage"),
            Map.entry("Rune_MareeGravitationnelle", "mage"),
            Map.entry("Rune_PluieCometes", "mage"),
            Map.entry("Rune_NaineBlanche", "mage"),
            Map.entry("Rune_Supernova", "mage"),
            Map.entry("Rune_PlanArchitecte", "mage"),
            Map.entry("Rune_Labyrinthe", "mage"),
            Map.entry("Rune_PorteMondes", "mage"),
            Map.entry("Rune_Presage", "mage"),
            Map.entry("Rune_Revelation", "mage"),
            Map.entry("Rune_Prophetie", "mage"),
            Map.entry("Rune_KiEpee", "apprenti_arts_martiaux, disciple_martial, expert_martial, maitre_martial, grand_maitre_martial, saint_martial, maitre_supreme, dieu_martial"),
            Map.entry("Rune_MeditationMartiale", "apprenti_arts_martiaux, disciple_martial, expert_martial, maitre_martial, grand_maitre_martial, saint_martial, maitre_supreme, dieu_martial"),
            Map.entry("Rune_PaumeKi", "clan"),
            Map.entry("Rune_FrappeMeridiens", "clan"),
            Map.entry("Rune_GardeFleuve", "clan"),
            Map.entry("Rune_CroissantKi", "clan"),
            Map.entry("Rune_DomaineEpee", "clan"),
            Map.entry("Rune_FleurPrunier", "clan"),
            Map.entry("Rune_TransformationHwakyung", "clan"),
            Map.entry("Rune_UniteCielTerre", "clan"),
            Map.entry("Rune_RenforcementCorps", "disciple_martial, expert_martial, maitre_martial, grand_maitre_martial, saint_martial, maitre_supreme, dieu_martial"),
            Map.entry("Rune_PasLeger", "expert_martial, maitre_martial, grand_maitre_martial, saint_martial, maitre_supreme, dieu_martial"),
            Map.entry("Rune_SensMartial", "maitre_martial, grand_maitre_martial, saint_martial, maitre_supreme, dieu_martial"),
            Map.entry("Rune_ProjectionEpeeAura", "grand_maitre_martial, saint_martial, maitre_supreme, dieu_martial"),
            Map.entry("Rune_VolEpeeKi", "saint_martial, maitre_supreme, dieu_martial"),
            Map.entry("Rune_IncarnationAme", "maitre_supreme, dieu_martial"),
            Map.entry("Rune_DomaineDivin", "dieu_martial"),
            Map.entry("Rune_ExecutionCeleste", "clan"),
            Map.entry("Rune_TransformationArme", "mousquetaire"),
            Map.entry("Rune_ArkiesDomini", "observateur"),
            Map.entry("Rune_SansVisage", "observateur"),
            Map.entry("Rune_SentenceSilencieuse", "observateur")
    );

    /** Parchemin -> rune enseignee. */
    private static final Map<String, String> PAGE_TO_RUNE = Map.ofEntries(
            Map.entry("rune_page_enrage",           "Rune_Enrage"),
            Map.entry("rune_page_ground_slam",      "Rune_GroundSlam"),
            Map.entry("rune_page_aura_blade",       "Rune_AuraBlade"),
            Map.entry("rune_page_fireball",         "Rune_Fireball"),
            Map.entry("rune_page_charged_shot",     "Rune_ChargedShot"),
            Map.entry("rune_page_chain_hook",       "Rune_ChainHook"),
            Map.entry("rune_page_poison_imbue",     "Rune_PoisonImbue"),
            Map.entry("rune_page_wind_strike",      "Rune_WindStrike"),
            Map.entry("rune_page_courroux_muspelheim", "Rune_CourrouxMuspelheim"),
            Map.entry("rune_page_sanctuaire_taranis", "Rune_SanctuaireTaranis"),
            Map.entry("rune_page_avenement_bahamut", "Rune_AvenementBahamut"),
            Map.entry("rune_page_avatar_dragon", "Rune_AvatarDragon"),
            Map.entry("rune_page_sablier_chronos", "Rune_SablierChronos"),
            Map.entry("rune_page_abime_chaos", "Rune_AbimeChaos"),
            Map.entry("rune_page_pont_bifrost", "Rune_PontBifrost"),
            Map.entry("rune_page_voile_nyx", "Rune_VoileNyx"),
            Map.entry("rune_page_sanctuaire_apollon", "Rune_SanctuaireApollon"),
            Map.entry("rune_page_courroux_fujin", "Rune_CourrouxFujin"),
            Map.entry("rune_page_reveil_gaia", "Rune_ReveilGaia"),
            Map.entry("rune_page_eveil_yggdrasil", "Rune_EveilYggdrasil"),
            Map.entry("rune_page_gilgamesh_babylon",      "Rune_GilgameshBabylon"),
            Map.entry("rune_page_porte_niflheim",       "Rune_PorteNiflheim"),
            Map.entry("rune_page_bourrasque", "Rune_Bourrasque"),
            Map.entry("rune_page_puits_gravitationnel", "Rune_PuitsGravitationnel"),
            Map.entry("rune_page_autorite_gravitationnelle", "Rune_AutoriteGravitationnelle"),
            Map.entry("rune_page_telekinesie", "Rune_Telekinesie"),
            Map.entry("rune_page_clonage", "Rune_Clonage"),
            Map.entry("rune_page_doppelganger", "Rune_Doppelganger"),
            Map.entry("rune_page_onde_repulsion", "Rune_OndeRepulsion"),
            Map.entry("rune_page_flammeche", "Rune_Flammeche"),
            Map.entry("rune_page_mur_de_flammes", "Rune_MurDeFlammes"),
            Map.entry("rune_page_marque_infernale", "Rune_MarqueInfernale"),
            Map.entry("rune_page_aura_de_braise", "Rune_AuraDeBraise"),
            Map.entry("rune_page_eclat_de_givre", "Rune_EclatDeGivre"),
            Map.entry("rune_page_lance_de_cristal", "Rune_LanceDeCristal"),
            Map.entry("rune_page_prison_de_glace", "Rune_PrisonDeGlace"),
            Map.entry("rune_page_sentier_d_hiver", "Rune_SentierDHiver"),
            Map.entry("rune_page_decharge", "Rune_Decharge"),
            Map.entry("rune_page_chaine_d_eclairs", "Rune_ChaineDEclairs"),
            Map.entry("rune_page_orbe_statique", "Rune_OrbeStatique"),
            Map.entry("rune_page_blizzard", "Rune_Blizzard"),
            Map.entry("rune_page_domaine_elementaire", "Rune_DomaineElementaire"),
            Map.entry("rune_page_tremblement_tectonique", "Rune_TremblementTectonique"),
            Map.entry("rune_page_danse_des_vents", "Rune_DanseDesVents"),
            Map.entry("rune_page_halo_protecteur", "Rune_HaloProtecteur"),
            Map.entry("rune_page_arme_benie", "Rune_ArmeBenie"),
            Map.entry("rune_page_glyphes_foudroyants", "Rune_GlyphesFoudroyants"),
            Map.entry("rune_page_egide_royale", "Rune_EgideRoyale"),
            Map.entry("rune_page_soin_mineur", "Rune_SoinMineur"),
            Map.entry("rune_page_fontaine_de_vie", "Rune_FontaineDeVie"),
            Map.entry("rune_page_lance_de_lumiere", "Rune_LanceDeLumiere"),
            Map.entry("rune_page_lien_d_ame", "Rune_LienDAme"),
            Map.entry("rune_page_esprit_follet", "Rune_EspritFollet"),
            Map.entry("rune_page_golem_de_pierre", "Rune_GolemDePierre"),
            Map.entry("rune_page_sentinelle_de_givre", "Rune_SentinelleDeGivre"),
            Map.entry("rune_page_meute_spectrale", "Rune_MeuteSpectrale"),
            Map.entry("rune_page_marque_funeste", "Rune_MarqueFuneste"),
            Map.entry("rune_page_puits_d_ames", "Rune_PuitsDAmes"),
            Map.entry("rune_page_releve_des_morts", "Rune_ReleveDesMorts"),
            Map.entry("rune_page_ames_errantes", "Rune_AmesErrantes"),
            Map.entry("rune_page_phylactere", "Rune_Phylactere"),
            Map.entry("rune_page_domaine_de_la_mort", "Rune_DomaineDeLaMort"),
            Map.entry("rune_page_forme_spectrale", "Rune_FormeSpectrale"),
            Map.entry("rune_page_armee_des_tombes", "Rune_ArmeeDesTombes"),
            Map.entry("rune_page_miroir_trompeur", "Rune_MiroirTrompeur"),
            Map.entry("rune_page_galerie_des_glaces", "Rune_GalerieDesGlaces"),
            Map.entry("rune_page_hallucination", "Rune_Hallucination"),
            Map.entry("rune_page_chaines_noires", "Rune_ChainesNoires"),
            Map.entry("rune_page_pacte_de_sang", "Rune_PacteDeSang"),
            Map.entry("rune_page_marionnette", "Rune_Marionnette"),
            Map.entry("rune_page_ruee", "Rune_Ruee"),
            Map.entry("rune_page_voile_de_fumee", "Rune_VoileDeFumee"),
            Map.entry("rune_page_pluie_de_fleches", "Rune_PluieDeFleches"),
            Map.entry("rune_page_lancer_de_pieces", "Rune_LancerDePieces"),
            Map.entry("rune_page_projection_astrale", "Rune_ProjectionAstrale"),
            Map.entry("rune_page_ceinture_asteroides", "Rune_CeintureAsteroides"),
            Map.entry("rune_page_prescience", "Rune_Prescience"),
            Map.entry("rune_page_rayon_celeste", "Rune_RayonCeleste"),
            Map.entry("rune_page_distorsion", "Rune_Distorsion"),
            Map.entry("rune_page_retour_en_arriere", "Rune_RetourEnArriere"),
            Map.entry("rune_page_oeil_omniscient", "Rune_OeilOmniscient"),
            Map.entry("rune_page_sentence", "Rune_Sentence"),
            Map.entry("rune_page_bulle_temporelle", "Rune_BulleTemporelle"),
            Map.entry("rune_page_arret_du_temps", "Rune_ArretDuTemps"),
            Map.entry("rune_page_surregime", "Rune_Surregime"),
            Map.entry("rune_page_echos_du_passe", "Rune_EchosDuPasse"),
            Map.entry("rune_page_meteore", "Rune_Meteore"),
            Map.entry("rune_page_rayon_arcanique", "Rune_RayonArcanique"),
            Map.entry("rune_page_avatar_orage", "Rune_AvatarOrage"),
            Map.entry("rune_page_eternite", "Rune_Eternite"),
            Map.entry("rune_page_salve_arcanique", "Rune_SalveArcanique"),
            Map.entry("rune_page_nuage_orage", "Rune_NuageOrage"),
            Map.entry("rune_page_reflexion", "Rune_Reflexion"),
            Map.entry("rune_page_decret", "Rune_Decret"),
            Map.entry("rune_page_meditation", "Rune_Meditation"),
            Map.entry("rune_page_sanctuaire_mineur", "Rune_SanctuaireMineur"),
            Map.entry("rune_page_savoir_accelere", "Rune_SavoirAccelere"),
            Map.entry("rune_page_enseignement", "Rune_Enseignement"),
            Map.entry("rune_page_miracle", "Rune_Miracle"),
            Map.entry("rune_page_pilier_ascendant", "Rune_PilierAscendant"),
            Map.entry("rune_page_inversion", "Rune_Inversion"),
            Map.entry("rune_page_rempart", "Rune_Rempart"),
            Map.entry("rune_page_sables_mouvants", "Rune_SablesMouvants"),
            Map.entry("rune_page_nuage_toxique", "Rune_NuageToxique"),
            Map.entry("rune_page_pierre_philosophale", "Rune_PierrePhilosophale"),
            Map.entry("rune_page_petrification", "Rune_Petrification"),
            Map.entry("rune_page_racines", "Rune_Racines"),
            Map.entry("rune_page_bosquet", "Rune_Bosquet"),
            Map.entry("rune_page_esprit_foret", "Rune_EspritForet"),
            Map.entry("rune_page_tempete_feuilles", "Rune_TempeteFeuilles"),
            Map.entry("rune_page_lentille_gravitationnelle", "Rune_LentilleGravitationnelle"),
            Map.entry("rune_page_couronne_autorite", "Rune_CouronneAutorite"),
            Map.entry("rune_page_loi_arcanique", "Rune_LoiArcanique"),
            Map.entry("rune_page_pilier_du_monde", "Rune_PilierDuMonde"),
            Map.entry("rune_page_bannissement", "Rune_Bannissement"),
            Map.entry("rune_page_effacement", "Rune_Effacement"),
            Map.entry("rune_page_fissure_vide", "Rune_FissureVide"),
            Map.entry("rune_page_silence_neant", "Rune_SilenceNeant"),
            Map.entry("rune_page_eruption_primordiale", "Rune_EruptionPrimordiale"),
            Map.entry("rune_page_sculpteur_de_glace", "Rune_SculpteurDeGlace"),
            Map.entry("rune_page_onde_originelle", "Rune_OndeOriginelle"),
            Map.entry("rune_page_resurrection", "Rune_Resurrection"),
            Map.entry("rune_page_golems_cristal", "Rune_GolemsCristal"),
            Map.entry("rune_page_faconnage", "Rune_Faconnage"),
            Map.entry("rune_page_colosse_pierre", "Rune_ColossePierre"),
            Map.entry("rune_page_matiere_vive", "Rune_MatiereVive"),
            Map.entry("rune_page_reecriture", "Rune_Reecriture"),
            Map.entry("rune_page_maree_gravitationnelle", "Rune_MareeGravitationnelle"),
            Map.entry("rune_page_pluie_cometes", "Rune_PluieCometes"),
            Map.entry("rune_page_naine_blanche", "Rune_NaineBlanche"),
            Map.entry("rune_page_supernova", "Rune_Supernova"),
            Map.entry("rune_page_plan_architecte", "Rune_PlanArchitecte"),
            Map.entry("rune_page_labyrinthe", "Rune_Labyrinthe"),
            Map.entry("rune_page_porte_mondes", "Rune_PorteMondes"),
            Map.entry("rune_page_presage", "Rune_Presage"),
            Map.entry("rune_page_revelation", "Rune_Revelation"),
            Map.entry("rune_page_prophetie", "Rune_Prophetie"),
            Map.entry("rune_page_transformation_arme", "Rune_TransformationArme"),
            Map.entry("rune_page_arkiesdomini", "Rune_ArkiesDomini"),
            Map.entry("rune_page_sansvisage", "Rune_SansVisage"),
            Map.entry("rune_page_sentencesilencieuse", "Rune_SentenceSilencieuse")
    );

    /** Joueurs dont on sait deja qu'ils possedent la Rune d'Aura (evite de rescanner l'inventaire). */
    private static final Set<UUID> auraRuneChecked = ConcurrentHashMap.newKeySet();

    // ===================== REGLES =====================

    public static Optional<String> getRuneFromPage(String itemId) {
        return Optional.ofNullable(PAGE_TO_RUNE.get(itemId));
    }

    public static String getRequiredClass(String runeId) {
        return RUNE_CLASSES.getOrDefault(runeId, "all");
    }

    public static boolean isRune(ItemStack stack) {
        return !ItemStack.isEmpty(stack) && stack.getItem() != null && stack.getItem().getAbility() != null;
    }

    /** Meme regle que les parchemins de sorts : la classe du joueur OU sa classe parente doit figurer dans la liste. */
    public static boolean isClassAllowed(String requiredClass, PlayerLevelData data) {
        if (requiredClass == null || requiredClass.equalsIgnoreCase("all")) return true;
        String playerClass = data.getPlayerClassId().toLowerCase();
        ClassModel classModel = ClassManager.get(data.getPlayerClassId());
        String parentClass = classModel != null ? classModel.getType().name().toLowerCase() : "";
        // 2026-11-24 (demande de l'utilisateur) : un artiste martial (famille Guerrier) apprend aussi les runes d'assassin ;
        // l'inverse est refuse (les techniques de la voie ne citent que les 8 classes martiales).
        boolean martial = com.eldanior.system.classes.definitions.luo.VoieMartiale.isMartial(playerClass);
        // Comparaison par jeton (pas de sous-chaine) : "mage" ne doit pas valider "infernal_mage"
        for (String token : requiredClass.toLowerCase().split(",")) {
            String t = token.trim();
            if (t.equals(playerClass) || (!parentClass.isEmpty() && t.equals(parentClass))) return true;
            if (martial && (t.equals("assassin") || t.equals("warrior"))) return true;
        }
        return false;
    }

    /** Une rune declaree par la classe du joueur lui est toujours permise (meme hors de RUNE_CLASSES). */
    public static boolean canUseRune(String runeId, PlayerLevelData data) {
        // 2026-11-24 (demande de l'utilisateur) : les artistes martiaux n'ont pas l'Aura de Dignite, ils y resistent
        if (AURA_RUNE_ID.equals(runeId) && com.eldanior.system.classes.definitions.luo.VoieMartiale.tier(data) > 0) return false;
        if (com.eldanior.system.classes.observateur.ObservateurManager.isObservateur(data)) return true;   // Observateur : toutes les runes
        var technique = com.eldanior.system.luo.LuoClans.technique(runeId);                  // technique d'un clan Luo
        if (technique != null) return com.eldanior.system.luo.LuoClans.knowsClan(technique, data);
        return classRunes(data).contains(runeId) || isClassAllowed(getRequiredClass(runeId), data);
    }

    /** Runes de la classe du joueur (ClassModel.getRuneIds, declarees dans les definitions de classe). */
    public static List<String> classRunes(PlayerLevelData data) {
        String classId = data != null ? data.getPlayerClassId() : null;
        ClassModel model = classId != null && !classId.isEmpty() ? ClassManager.get(classId) : null;
        return model != null ? model.getRuneIds() : List.of();
    }

    // ===================== INVENTAIRE =====================

    private static List<ComponentType<EntityStore, ? extends InventoryComponent>> runeSections() {
        return List.of(
                InventoryComponent.RuneBag.getComponentType(),
                InventoryComponent.AbilitySlots.getComponentType(),
                InventoryComponent.Hotbar.getComponentType(),
                InventoryComponent.Storage.getComponentType(),
                InventoryComponent.Backpack.getComponentType(),
                InventoryComponent.Utility.getComponentType());
    }

    public static boolean ownsRune(Ref<EntityStore> ref, Store<EntityStore> store, String runeId) {
        for (var type : runeSections()) {
            InventoryComponent section = store.getComponent(ref, type);
            if (section != null && section.getInventory()
                    .countItemStacks(stack -> runeId.equals(stack.getItemId())) > 0) {
                return true;
            }
        }
        return false;
    }

    /** Runes possedees (sac, emplacements, barre, inventaire), sans doublon : statistiques admin. */
    public static Set<String> ownedRunes(Ref<EntityStore> ref, Store<EntityStore> store) {
        Set<String> out = new java.util.LinkedHashSet<>();
        for (var type : runeSections()) {
            InventoryComponent section = store.getComponent(ref, type);
            if (section != null) section.getInventory().forEach((slot, stack) -> { if (isRune(stack)) out.add(stack.getItemId()); });
        }
        return out;
    }

    /** Runes equipees dans les emplacements de competence. */
    public static List<String> equippedRunes(Ref<EntityStore> ref, Store<EntityStore> store) {
        List<String> out = new ArrayList<>();
        InventoryComponent.AbilitySlots slots = store.getComponent(ref, InventoryComponent.AbilitySlots.getComponentType());
        if (slots != null) slots.getInventory().forEach((slot, stack) -> { if (isRune(stack)) out.add(stack.getItemId()); });
        return out;
    }

    /** Le joueur possede-t-il une rune de cette categorie (ex. "Eldanior_Runes.Espace") ? */
    public static boolean ownsRuneInCategory(Ref<EntityStore> ref, Store<EntityStore> store, String category) {
        for (var type : runeSections()) {
            InventoryComponent section = store.getComponent(ref, type);
            if (section != null && section.getInventory().countItemStacks(stack -> {
                String[] cats = isRune(stack) ? stack.getItem().getCategories() : null;
                return cats != null && java.util.Arrays.asList(cats).contains(category);
            }) > 0) return true;
        }
        return false;
    }

    /** Ajoute une rune dans le sac de runes. Retourne false si le sac est plein / absent. */
    public static boolean giveRune(Ref<EntityStore> ref, Store<EntityStore> store, String runeId) {
        InventoryComponent.RuneBag runeBag = store.getComponent(ref, InventoryComponent.RuneBag.getComponentType());
        if (runeBag == null) return false;
        if (!runeBag.getInventory().addItemStack(new ItemStack(runeId, 1)).succeeded()) return false;
        runeBag.markDirty();
        return true;
    }

    /** Retire de toutes les sections les runes qui matchent le filtre. Retourne le nombre retire. */
    private static int removeRunes(Ref<EntityStore> ref, Store<EntityStore> store, Predicate<ItemStack> filter) {
        int removed = 0;
        for (var type : runeSections()) {
            InventoryComponent section = store.getComponent(ref, type);
            if (section == null) continue;
            ItemContainer container = section.getInventory();
            List<Short> slots = new ArrayList<>();
            container.forEach((slot, stack) -> {
                if (isRune(stack) && filter.test(stack)) slots.add(slot);
            });
            for (short slot : slots) container.removeItemStackFromSlot(slot);
            if (!slots.isEmpty()) {
                section.markDirty();
                removed += slots.size();
            }
        }
        return removed;
    }

    /** Anciens identifiants des runes divines (avant renommage) -> nouveaux. */
    private static final Map<String, String> LEGACY_IDS = Map.of(
            "Rune_ChatimentCeleste", "Rune_SanctuaireTaranis",
            "Rune_EpeeCeleste", "Rune_GilgameshBabylon",
            "Rune_ZeroAbsolu", "Rune_PorteNiflheim",
            "rune_page_chatiment_celeste", "rune_page_sanctuaire_taranis",
            "rune_page_epee_celeste", "rune_page_gilgamesh_babylon",
            "rune_page_zero_absolu", "rune_page_porte_niflheim",
            "Rune_JugementCeleste", "Rune_CourrouxMuspelheim",
            "rune_page_jugement_celeste", "rune_page_courroux_muspelheim");

    /** Remplace dans l'inventaire les runes / parchemins portant un ancien identifiant. Retourne le nombre converti. */
    public static int migrateLegacyRunes(Ref<EntityStore> ref, Store<EntityStore> store) {
        int migrated = 0;
        for (var type : runeSections()) {
            InventoryComponent section = store.getComponent(ref, type);
            if (section == null) continue;
            ItemContainer container = section.getInventory();
            Map<Short, ItemStack> replace = new java.util.HashMap<>();
            container.forEach((slot, stack) -> {
                String newId = stack == null ? null : LEGACY_IDS.get(stack.getItemId());
                if (newId != null) replace.put(slot, new ItemStack(newId, Math.max(1, stack.getQuantity())));
            });
            replace.forEach(container::setItemStackForSlot);
            if (!replace.isEmpty()) {
                section.markDirty();
                migrated += replace.size();
            }
        }
        return migrated;
    }

    /** Changement de classe : supprime les runes que la nouvelle classe ne peut pas utiliser. */
    public static int purgeDisallowedRunes(Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData data) {
        return removeRunes(ref, store, stack -> !canUseRune(stack.getItemId(), data));
    }

    /** Reset niveau 1 : supprime toutes les runes (la Rune d'Aura sera redonnee avec la dignite). */
    public static int wipeAllRunes(Ref<EntityStore> ref, Store<EntityStore> store) {
        int removed = removeRunes(ref, store, stack -> true);
        UUID uuid = getUuid(ref, store);
        if (uuid != null) { auraRuneChecked.remove(uuid); classRunesGiven.remove(uuid); }
        return removed;
    }

    /**
     * Restriction a l'equipement : une rune non autorisee posee dans les slots de competence
     * est renvoyee dans le sac de runes. Retourne le nombre de runes deplacees.
     */
    public static int enforceAbilitySlots(Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData data) {
        InventoryComponent.AbilitySlots abilitySlots = store.getComponent(ref, InventoryComponent.AbilitySlots.getComponentType());
        InventoryComponent.RuneBag runeBag = store.getComponent(ref, InventoryComponent.RuneBag.getComponentType());
        if (abilitySlots == null || runeBag == null) return 0;

        ItemContainer slotsContainer = abilitySlots.getInventory();
        List<Short> forbidden = new ArrayList<>();
        slotsContainer.forEach((slot, stack) -> {
            if (isRune(stack) && !canUseRune(stack.getItemId(), data)) forbidden.add(slot);
        });

        int moved = 0;
        for (short slot : forbidden) {
            ItemStack stack = slotsContainer.getItemStack(slot);
            if (ItemStack.isEmpty(stack) || !runeBag.getInventory().canAddItemStack(stack)) continue;
            slotsContainer.removeItemStackFromSlot(slot);
            runeBag.getInventory().addItemStack(stack);
            moved++;
        }
        if (moved > 0) {
            abilitySlots.markDirty();
            runeBag.markDirty();
        }
        return moved;
    }

    // ===================== RUNES DE CLASSE =====================

    /** Joueur -> classe dont les runes ont deja ete donnees (evite de rescanner l'inventaire chaque seconde). */
    private static final Map<UUID, String> classRunesGiven = new ConcurrentHashMap<>();

    /**
     * Donne au joueur (sac de runes) les runes de sa classe qu'il ne possede nulle part : a la connexion, et a chaque
     * changement de classe (evolution, /es setclass, admin). Retourne les runes donnees. Sac plein : on reessaiera.
     * Techniques de la voie martiale du Clan Luo : le manuel secret a lire (LuoManuels) au lieu de la rune.
     * 2026-11-24 (demande de l'utilisateur, « le seul moyen d'avoir des runes c'est via les manuels ») : TOUTES les runes de
     * classe sont remises en manuel (MagicManuals) ; une rune sans manuel n'est pas donnee (journal).
     */
    public static List<String> grantClassRunes(Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData data) {
        UUID uuid = getUuid(ref, store);
        String classId = data != null ? data.getPlayerClassId() : null;
        if (uuid == null || classId == null || classId.equals(classRunesGiven.get(uuid))) return List.of();
        List<String> given = new ArrayList<>();
        boolean complete = true;
        for (String runeId : classRunes(data)) {
            if (ownsRune(ref, store, runeId)) continue;
            String book = com.eldanior.system.skills.runes.spells.MagicManuals.anyBookFor(runeId);   // manuel a lire
            // 2026-11-13 (demande de l'utilisateur) : les manuels de palier (Disciple et au-dela) ne sont plus donnes aux
            // promotions ; ils se gagnent dans les Epreuves du Dojo (quetes du Sensei, quest.chapitres). Ki et Meditation
            // (palier 1) restent remis au serment.
            if (book != null && com.eldanior.system.classes.definitions.luo.VoieMartiale.runeTier(runeId) >= 2) continue;
            try {
                if (book != null) {
                    if (com.eldanior.system.skills.runes.spells.LuoManuels.hasBook(ref, store, book)) continue;
                    if (com.eldanior.system.skills.runes.spells.LuoManuels.giveBook(ref, store, book)) given.add(book);
                    else complete = false;                                                  // inventaire plein
                } else {
                    EldaniorLogger.warn("[Runes] rune de classe sans manuel, non donnee : " + runeId);
                }
            } catch (Exception e) {
                EldaniorLogger.error("[Runes] rune de classe introuvable : " + runeId, e);
            }
        }
        if (complete) classRunesGiven.put(uuid, classId);
        return given;
    }

    // ===================== RUNE D'AURA =====================

    /** Donne la Rune d'Aura (sac de runes) si le joueur ne la possede nulle part. Retourne true si donnee. */
    public static boolean grantAuraRuneIfMissing(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID uuid = getUuid(ref, store);
        if (uuid == null || auraRuneChecked.contains(uuid)) return false;

        String book = com.eldanior.system.skills.runes.spells.MagicManuals.bookFor(AURA_RUNE_ID);   // 2026-11-24 : le manuel
        if (ownsRune(ref, store, AURA_RUNE_ID) || book == null
                || com.eldanior.system.skills.runes.spells.LuoManuels.hasBook(ref, store, book)) {
            auraRuneChecked.add(uuid);
            return false;
        }
        if (com.eldanior.system.skills.runes.spells.LuoManuels.giveBook(ref, store, book)) {
            auraRuneChecked.add(uuid);
            return true;
        }
        return false;
    }

    public static void handleDisconnect(UUID uuid) {
        auraRuneChecked.remove(uuid);
        classRunesGiven.remove(uuid);
    }

    private static UUID getUuid(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef netRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (netRef == null) return null;
        try { return UUIDExtractor.getUUID(netRef); }
        catch (Exception e) { return null; }
    }
}
