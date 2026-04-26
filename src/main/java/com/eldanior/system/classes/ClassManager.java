package com.eldanior.system.classes;

import com.eldanior.system.classes.definitions.*;
import com.eldanior.system.classes.definitions.marchand.*;
import com.eldanior.system.classes.definitions.warrior.*;
import com.eldanior.system.classes.definitions.warrior.BerserkerEnrage;
import com.eldanior.system.classes.definitions.warrior.FureurSanglante;
import com.eldanior.system.classes.definitions.warrior.Devastateur;
import com.eldanior.system.classes.definitions.warrior.LameVirtuose;
import com.eldanior.system.classes.definitions.warrior.MaitreDArmes;
import com.eldanior.system.classes.definitions.warrior.Fleurettiste;
import com.eldanior.system.classes.definitions.warrior.GardienSacre;
import com.eldanior.system.classes.definitions.warrior.RempartVivant;
import com.eldanior.system.classes.definitions.warrior.Sentinelle;
import com.eldanior.system.classes.definitions.warrior.Ecraseur;
import com.eldanior.system.classes.definitions.warrior.BoucherDeGuerre;
import com.eldanior.system.classes.definitions.warrior.ColosseDeFer;
import com.eldanior.system.classes.definitions.warrior.LameCeleste;
import com.eldanior.system.classes.definitions.warrior.Trancheur;
import com.eldanior.system.classes.definitions.warrior.EscrimeurRoyal;
import com.eldanior.system.classes.definitions.warrior.Veteran;
import com.eldanior.system.classes.definitions.warrior.Centurion;
import com.eldanior.system.classes.definitions.warrior.Legionnaire;
import com.eldanior.system.classes.definitions.warrior.CapitaineMercenaire;
import com.eldanior.system.classes.definitions.warrior.ChasseurDePrimes;
import com.eldanior.system.classes.definitions.warrior.LoupDeGuerre;
import com.eldanior.system.classes.definitions.warrior.Marechal;
import com.eldanior.system.classes.definitions.warrior.AvantGardeSupreme;
import com.eldanior.system.classes.definitions.warrior.FerDeLance;
import com.eldanior.system.classes.definitions.warrior.LameFantome;
import com.eldanior.system.classes.definitions.warrior.VirtuoseDuSabre;
import com.eldanior.system.classes.definitions.warrior.DanseurDeLames;
import com.eldanior.system.classes.definitions.warrior.ChampionDeLArene;
import com.eldanior.system.classes.definitions.warrior.RoiGladiateur;
import com.eldanior.system.classes.definitions.warrior.Spartiate;
import com.eldanior.system.classes.definitions.warrior.EnchanteurDeGuerre;
import com.eldanior.system.classes.definitions.warrior.LameArcanique;
import com.eldanior.system.classes.definitions.warrior.ChevalierMystique;
import com.eldanior.system.classes.definitions.warrior.PaladinSacre;
import com.eldanior.system.classes.definitions.warrior.CroiseDivin;
import com.eldanior.system.classes.definitions.warrior.Justicier;
import com.eldanior.system.classes.definitions.warrior.Destructeur;
import com.eldanior.system.classes.definitions.warrior.FleauDeGuerre;
import com.eldanior.system.classes.definitions.warrior.Annihilateur;
import com.eldanior.system.classes.definitions.warrior.Kensei;
import com.eldanior.system.classes.definitions.warrior.Shogun;
import com.eldanior.system.classes.definitions.warrior.RoninLegendaire;
import com.eldanior.system.classes.definitions.warrior.GrandTemplier;
import com.eldanior.system.classes.definitions.warrior.Commandeur;
import com.eldanior.system.classes.definitions.warrior.Inquisiteur;
import com.eldanior.system.classes.definitions.warrior.EluDesBatailles;
import com.eldanior.system.classes.definitions.warrior.Conquerant;
import com.eldanior.system.classes.definitions.warrior.Invaincu;
import com.eldanior.system.classes.definitions.warrior.SeigneurDesTenebres;
import com.eldanior.system.classes.definitions.warrior.CavalierDeLOmbre;
import com.eldanior.system.classes.definitions.warrior.TyranNoir;
import com.eldanior.system.classes.definitions.warrior.TitanDePierre;
import com.eldanior.system.classes.definitions.warrior.GolemVivant;
import com.eldanior.system.classes.definitions.warrior.Forteresse;
import com.eldanior.system.classes.definitions.warrior.CroiseEternel;
import com.eldanior.system.classes.definitions.warrior.SaintGuerrier;
import com.eldanior.system.classes.definitions.warrior.MarteauDivin;
import com.eldanior.system.classes.definitions.warrior.EmpereurDeGuerre;
import com.eldanior.system.classes.definitions.warrior.StrategeSupreme;
import com.eldanior.system.classes.definitions.warrior.ConquerantUltime;
import com.eldanior.system.classes.definitions.warrior.BourrauSupreme;
import com.eldanior.system.classes.definitions.warrior.FaucheseDeGuerre;
import com.eldanior.system.classes.definitions.warrior.JugementFinal;
import com.eldanior.system.classes.definitions.warrior.ArchonRunique;
import com.eldanior.system.classes.definitions.warrior.GardienPrimordial;
import com.eldanior.system.classes.definitions.warrior.SageDeGuerre;
import com.eldanior.system.classes.definitions.warrior.TitanOriginel;
import com.eldanior.system.classes.definitions.warrior.ColosseEternel;
import com.eldanior.system.classes.definitions.warrior.Ascendant;
import com.eldanior.system.classes.definitions.warrior.Apocalypse;
import com.eldanior.system.classes.definitions.warrior.Cataclysme;
import com.eldanior.system.classes.definitions.warrior.Extinction;
import com.eldanior.system.classes.definitions.warrior.HerosMythique;
import com.eldanior.system.classes.definitions.warrior.LegendeVivante;
import com.eldanior.system.classes.definitions.warrior.Paragone;
import com.eldanior.system.classes.definitions.warrior.DemiDieu;
import com.eldanior.system.classes.definitions.warrior.DemiDragon;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;

import java.util.*;

public class ClassManager {

    private static final Map<String, ClassModel> classes = new HashMap<>();
    private static final Set<String> evolutionIds = new HashSet<>();

    public static void init() {
        register(new DragonAncestral());

        register(new Novice());

        register(new Warrior());
        register(new Mage());
        register(new Assassin());
        register(new Archer());
        register(new Merchant());

        register(new Templier());
        register(new Epeiste());
        register(new Fantassin());
        register(new Brute());
        register(new Mercenaire());
        register(new com.eldanior.system.classes.definitions.warrior.Protecteur());
        register(new com.eldanior.system.classes.definitions.warrior.Berserker());
        register(new com.eldanior.system.classes.definitions.warrior.Duelliste());
        register(new com.eldanior.system.classes.definitions.warrior.Paladin());
        register(new com.eldanior.system.classes.definitions.warrior.Gladiateur());
        register(new com.eldanior.system.classes.definitions.warrior.Samourai());
        register(new com.eldanior.system.classes.definitions.warrior.Warlord());
        register(new com.eldanior.system.classes.definitions.warrior.ChevalierNoir());
        register(new com.eldanior.system.classes.definitions.warrior.Croise());

        // Mage evolutions (27)
        register(new com.eldanior.system.classes.definitions.mage.Elementaliste());
        register(new com.eldanior.system.classes.definitions.mage.Enchanteur());
        register(new com.eldanior.system.classes.definitions.mage.Necromancien());
        register(new com.eldanior.system.classes.definitions.mage.Invocateur());
        register(new com.eldanior.system.classes.definitions.mage.Guerisseur());
        register(new com.eldanior.system.classes.definitions.mage.Pyromancien());
        register(new com.eldanior.system.classes.definitions.mage.Cryomancien());
        register(new com.eldanior.system.classes.definitions.mage.Archimage());
        register(new com.eldanior.system.classes.definitions.mage.Sorcier());
        register(new com.eldanior.system.classes.definitions.mage.Druide());
        register(new com.eldanior.system.classes.definitions.mage.Illusionniste());
        register(new com.eldanior.system.classes.definitions.mage.Mystique());
        register(new com.eldanior.system.classes.definitions.mage.Thaumaturge());
        register(new com.eldanior.system.classes.definitions.mage.Alchimiste());
        register(new com.eldanior.system.classes.definitions.mage.Sage());
        register(new com.eldanior.system.classes.definitions.mage.Magus());
        register(new com.eldanior.system.classes.definitions.mage.Liche());
        register(new com.eldanior.system.classes.definitions.mage.Oracle());
        register(new com.eldanior.system.classes.definitions.mage.MaitreElementaire());
        register(new com.eldanior.system.classes.definitions.mage.Chronoturge());
        register(new com.eldanior.system.classes.definitions.mage.Archonte());
        register(new com.eldanior.system.classes.definitions.mage.MageVoid());
        register(new com.eldanior.system.classes.definitions.mage.Primordial());
        register(new com.eldanior.system.classes.definitions.mage.Demiurge());
        register(new com.eldanior.system.classes.definitions.mage.Prophete());
        register(new com.eldanior.system.classes.definitions.mage.AvatarArcanique());
        register(new com.eldanior.system.classes.definitions.mage.DieuDesArcanes());

        // Assassin evolutions (27)
        register(new com.eldanior.system.classes.definitions.assassin.Voleur());
        register(new com.eldanior.system.classes.definitions.assassin.Rodeur());
        register(new com.eldanior.system.classes.definitions.assassin.Sicaire());
        register(new com.eldanior.system.classes.definitions.assassin.Eclaireur());
        register(new com.eldanior.system.classes.definitions.assassin.Empoisonneur());
        register(new com.eldanior.system.classes.definitions.assassin.Saboteur());
        register(new com.eldanior.system.classes.definitions.assassin.Acrobate());
        register(new com.eldanior.system.classes.definitions.assassin.MaitreLame());
        register(new com.eldanior.system.classes.definitions.assassin.OmbreFurtive());
        register(new com.eldanior.system.classes.definitions.assassin.Chasseur());
        register(new com.eldanior.system.classes.definitions.assassin.Ninja());
        register(new com.eldanior.system.classes.definitions.assassin.Espion());
        register(new com.eldanior.system.classes.definitions.assassin.Corsaire());
        register(new com.eldanior.system.classes.definitions.assassin.Traqueur());
        register(new com.eldanior.system.classes.definitions.assassin.LameNoire());
        register(new com.eldanior.system.classes.definitions.assassin.MaitreOmbre());
        register(new com.eldanior.system.classes.definitions.assassin.Faucheur());
        register(new com.eldanior.system.classes.definitions.assassin.PhantomBlade());
        register(new com.eldanior.system.classes.definitions.assassin.GrandMaitrePoison());
        register(new com.eldanior.system.classes.definitions.assassin.AngeDechu());
        register(new com.eldanior.system.classes.definitions.assassin.Spectre());
        register(new com.eldanior.system.classes.definitions.assassin.SeigneurPoison());
        register(new com.eldanior.system.classes.definitions.assassin.LameEternelle());
        register(new com.eldanior.system.classes.definitions.assassin.OmbreSupreme());
        register(new com.eldanior.system.classes.definitions.assassin.EmpereurDesOmbres());
        register(new com.eldanior.system.classes.definitions.assassin.AvatarDuNeant());
        register(new com.eldanior.system.classes.definitions.assassin.DieuDesOmbres());

        // Archer evolutions (15)
        register(new com.eldanior.system.classes.definitions.archer.Tireur());
        register(new com.eldanior.system.classes.definitions.archer.Chasseur());
        register(new com.eldanior.system.classes.definitions.archer.Arbaletrier());
        register(new com.eldanior.system.classes.definitions.archer.Eclaireur());
        register(new com.eldanior.system.classes.definitions.archer.Franc_Tireur());
        register(new com.eldanior.system.classes.definitions.archer.RangerElite());
        register(new com.eldanior.system.classes.definitions.archer.ArcMystique());
        register(new com.eldanior.system.classes.definitions.archer.MaitreChasse());
        register(new com.eldanior.system.classes.definitions.archer.SniperDivin());
        register(new com.eldanior.system.classes.definitions.archer.TireurElementaire());
        register(new com.eldanior.system.classes.definitions.archer.GeneralArcher());
        register(new com.eldanior.system.classes.definitions.archer.OeilDeFaucon());
        register(new com.eldanior.system.classes.definitions.archer.ArcAncien());
        register(new com.eldanior.system.classes.definitions.archer.AvatarDeLArc());
        register(new com.eldanior.system.classes.definitions.archer.DieuDeLArc());
        register(new AvantGarde());
        register(new Ravageur());
        register(new Bretteur());
        register(new LameMage());
        register(new Champion());
        register(new Colosse());
        register(new Executeur());
        register(new GardienRunique());
        register(new Titan());
        register(new Heros());
        register(new Fleau());
        register(new SangDragon());
        register(new DivineApotre());

        // Warrior tier 2 evolutions (77)
        register(new BerserkerEnrage());
        register(new FureurSanglante());
        register(new Devastateur());
        register(new LameVirtuose());
        register(new MaitreDArmes());
        register(new Fleurettiste());
        register(new GardienSacre());
        register(new RempartVivant());
        register(new Sentinelle());
        register(new Ecraseur());
        register(new BoucherDeGuerre());
        register(new ColosseDeFer());
        register(new LameCeleste());
        register(new Trancheur());
        register(new EscrimeurRoyal());
        register(new Veteran());
        register(new Centurion());
        register(new Legionnaire());
        register(new CapitaineMercenaire());
        register(new ChasseurDePrimes());
        register(new LoupDeGuerre());
        register(new Marechal());
        register(new AvantGardeSupreme());
        register(new FerDeLance());
        register(new LameFantome());
        register(new VirtuoseDuSabre());
        register(new DanseurDeLames());
        register(new ChampionDeLArene());
        register(new RoiGladiateur());
        register(new Spartiate());
        register(new EnchanteurDeGuerre());
        register(new LameArcanique());
        register(new ChevalierMystique());
        register(new PaladinSacre());
        register(new CroiseDivin());
        register(new Justicier());
        register(new Destructeur());
        register(new FleauDeGuerre());
        register(new Annihilateur());
        register(new Kensei());
        register(new Shogun());
        register(new RoninLegendaire());
        register(new GrandTemplier());
        register(new Commandeur());
        register(new Inquisiteur());
        register(new EluDesBatailles());
        register(new Conquerant());
        register(new Invaincu());
        register(new SeigneurDesTenebres());
        register(new CavalierDeLOmbre());
        register(new TyranNoir());
        register(new TitanDePierre());
        register(new GolemVivant());
        register(new Forteresse());
        register(new CroiseEternel());
        register(new SaintGuerrier());
        register(new MarteauDivin());
        register(new EmpereurDeGuerre());
        register(new StrategeSupreme());
        register(new ConquerantUltime());
        register(new BourrauSupreme());
        register(new FaucheseDeGuerre());
        register(new JugementFinal());
        register(new ArchonRunique());
        register(new GardienPrimordial());
        register(new SageDeGuerre());
        register(new TitanOriginel());
        register(new ColosseEternel());
        register(new Ascendant());
        register(new Apocalypse());
        register(new Cataclysme());
        register(new Extinction());
        register(new HerosMythique());
        register(new LegendeVivante());
        register(new Paragone());
        register(new DemiDieu());
        register(new DemiDragon());

        register(new BlackMarketPrince());
        register(new GoldBaron());
        register(new GuildMaster());
        register(new MasterArtisan());
        register(new ProsperityAvatar());
        register(new RelicHunter());
        register(new Smuggler());
        register(new WorldForger());
        register(new UnderworldKing());
        register(new com.eldanior.system.classes.definitions.marchand.Negociant());
        register(new com.eldanior.system.classes.definitions.marchand.Caravanier());
        register(new com.eldanior.system.classes.definitions.marchand.Prospecteur());
        register(new com.eldanior.system.classes.definitions.marchand.Banquier());
        register(new com.eldanior.system.classes.definitions.marchand.MarchandDivin());
        register(new com.eldanior.system.classes.definitions.marchand.RoiDuCommerce());

        // Archer tier 2 evolutions (43)
        register(new com.eldanior.system.classes.definitions.archer.ArbaleteLourd());
        register(new com.eldanior.system.classes.definitions.archer.ArbaletrierdElite());
        register(new com.eldanior.system.classes.definitions.archer.ArcDivin());
        register(new com.eldanior.system.classes.definitions.archer.ArcPrecis());
        register(new com.eldanior.system.classes.definitions.archer.ArcPrimordial());
        register(new com.eldanior.system.classes.definitions.archer.ArcRunique());
        register(new com.eldanior.system.classes.definitions.archer.ArcaneArcher());
        register(new com.eldanior.system.classes.definitions.archer.ArcaneSupreme());
        register(new com.eldanior.system.classes.definitions.archer.ArcdesEtoiles());
        register(new com.eldanior.system.classes.definitions.archer.AscendantdelArc());
        register(new com.eldanior.system.classes.definitions.archer.ChasseurSupreme());
        register(new com.eldanior.system.classes.definitions.archer.ChasseurVeteran());
        register(new com.eldanior.system.classes.definitions.archer.CommandantRanger());
        register(new com.eldanior.system.classes.definitions.archer.CommandeurdelElite());
        register(new com.eldanior.system.classes.definitions.archer.CoureurdAurore());
        register(new com.eldanior.system.classes.definitions.archer.DemiDieuArcher());
        register(new com.eldanior.system.classes.definitions.archer.EclaireurFantome());
        register(new com.eldanior.system.classes.definitions.archer.FlecheCosmique());
        register(new com.eldanior.system.classes.definitions.archer.FrancTireurRoyal());
        register(new com.eldanior.system.classes.definitions.archer.GardeForestier());
        register(new com.eldanior.system.classes.definitions.archer.GardiendesArcsAnciens());
        register(new com.eldanior.system.classes.definitions.archer.GeneralSupreme());
        register(new com.eldanior.system.classes.definitions.archer.LegendeArchere());
        register(new com.eldanior.system.classes.definitions.archer.MaitredesTraques());
        register(new com.eldanior.system.classes.definitions.archer.MarechaldesArcs());
        register(new com.eldanior.system.classes.definitions.archer.NemrodLegendaire());
        register(new com.eldanior.system.classes.definitions.archer.OeilAbsolu());
        register(new com.eldanior.system.classes.definitions.archer.OeildeLynx());
        register(new com.eldanior.system.classes.definitions.archer.OeilduDestin());
        register(new com.eldanior.system.classes.definitions.archer.OmbredesVents());
        register(new com.eldanior.system.classes.definitions.archer.PisteurSauvage());
        register(new com.eldanior.system.classes.definitions.archer.RegardPercant());
        register(new com.eldanior.system.classes.definitions.archer.SagittaireArcane());
        register(new com.eldanior.system.classes.definitions.archer.SagittaireElemental());
        register(new com.eldanior.system.classes.definitions.archer.SentinelledElite());
        register(new com.eldanior.system.classes.definitions.archer.SniperCeleste());
        register(new com.eldanior.system.classes.definitions.archer.SniperMystique());
        register(new com.eldanior.system.classes.definitions.archer.TirFatal());
        register(new com.eldanior.system.classes.definitions.archer.TireurLegendaire());
        register(new com.eldanior.system.classes.definitions.archer.TireurdElite());
        register(new com.eldanior.system.classes.definitions.archer.TireurdeSiege());
        register(new com.eldanior.system.classes.definitions.archer.TraqueurdeFauves());
        register(new com.eldanior.system.classes.definitions.archer.VisionAbsolue());

        // Assassin tier 2 evolutions (77)
        register(new com.eldanior.system.classes.definitions.assassin.AcrobateVirtuose());
        register(new com.eldanior.system.classes.definitions.assassin.AgentDouble());
        register(new com.eldanior.system.classes.definitions.assassin.AileBrisee());
        register(new com.eldanior.system.classes.definitions.assassin.AlchimisteMortel());
        register(new com.eldanior.system.classes.definitions.assassin.AngeNoir());
        register(new com.eldanior.system.classes.definitions.assassin.ArchangeNoir());
        register(new com.eldanior.system.classes.definitions.assassin.ArtisteduCombat());
        register(new com.eldanior.system.classes.definitions.assassin.AssassinRoyal());
        register(new com.eldanior.system.classes.definitions.assassin.CapitaineCorsaire());
        register(new com.eldanior.system.classes.definitions.assassin.ChasseurNocturne());
        register(new com.eldanior.system.classes.definitions.assassin.ChasseurdElite());
        register(new com.eldanior.system.classes.definitions.assassin.ChasseurdOmbres());
        register(new com.eldanior.system.classes.definitions.assassin.ChuteFinale());
        register(new com.eldanior.system.classes.definitions.assassin.DanseurMortel());
        register(new com.eldanior.system.classes.definitions.assassin.DemiNeant());
        register(new com.eldanior.system.classes.definitions.assassin.DemiOmbre());
        register(new com.eldanior.system.classes.definitions.assassin.DemolisseurFurtif());
        register(new com.eldanior.system.classes.definitions.assassin.DuellisteSupreme());
        register(new com.eldanior.system.classes.definitions.assassin.EclaireurdElite());
        register(new com.eldanior.system.classes.definitions.assassin.EpeedesOmbres());
        register(new com.eldanior.system.classes.definitions.assassin.EspritVengeur());
        register(new com.eldanior.system.classes.definitions.assassin.FaucheurEternel());
        register(new com.eldanior.system.classes.definitions.assassin.FlibustierNoir());
        register(new com.eldanior.system.classes.definitions.assassin.GrandMaitreLame());
        register(new com.eldanior.system.classes.definitions.assassin.HydreEmpoisonnee());
        register(new com.eldanior.system.classes.definitions.assassin.InfiltreurSupreme());
        register(new com.eldanior.system.classes.definitions.assassin.KageMaitre());
        register(new com.eldanior.system.classes.definitions.assassin.LameAbsolue());
        register(new com.eldanior.system.classes.definitions.assassin.LameAbyssale());
        register(new com.eldanior.system.classes.definitions.assassin.LameDiamant());
        register(new com.eldanior.system.classes.definitions.assassin.LameInfinie());
        register(new com.eldanior.system.classes.definitions.assassin.LameRapide());
        register(new com.eldanior.system.classes.definitions.assassin.LameSpectrale());
        register(new com.eldanior.system.classes.definitions.assassin.LameduVide());
        register(new com.eldanior.system.classes.definitions.assassin.MaindArgent());
        register(new com.eldanior.system.classes.definitions.assassin.MaitreEspion());
        register(new com.eldanior.system.classes.definitions.assassin.MaitredesPoisons());
        register(new com.eldanior.system.classes.definitions.assassin.MoissonneurdAmes());
        register(new com.eldanior.system.classes.definitions.assassin.MonarqueNoir());
        register(new com.eldanior.system.classes.definitions.assassin.NinjaLegendaire());
        register(new com.eldanior.system.classes.definitions.assassin.NuitEternelle());
        register(new com.eldanior.system.classes.definitions.assassin.NuitVivante());
        register(new com.eldanior.system.classes.definitions.assassin.OeilPercant());
        register(new com.eldanior.system.classes.definitions.assassin.OmbreAbsolue());
        register(new com.eldanior.system.classes.definitions.assassin.OmbreErrante());
        register(new com.eldanior.system.classes.definitions.assassin.OmbreEternelle());
        register(new com.eldanior.system.classes.definitions.assassin.OmbreInsaisissable());
        register(new com.eldanior.system.classes.definitions.assassin.PhantomeEternel());
        register(new com.eldanior.system.classes.definitions.assassin.PiegeVivant());
        register(new com.eldanior.system.classes.definitions.assassin.PisteurAbsolu());
        register(new com.eldanior.system.classes.definitions.assassin.PoisonPrimordial());
        register(new com.eldanior.system.classes.definitions.assassin.PredateurSupreme());
        register(new com.eldanior.system.classes.definitions.assassin.PrinceduLarcin());
        register(new com.eldanior.system.classes.definitions.assassin.RegnedelOmbre());
        register(new com.eldanior.system.classes.definitions.assassin.RevenantAbsolu());
        register(new com.eldanior.system.classes.definitions.assassin.RodeurVeteran());
        register(new com.eldanior.system.classes.definitions.assassin.RoidesPirates());
        register(new com.eldanior.system.classes.definitions.assassin.SaboteurExpert());
        register(new com.eldanior.system.classes.definitions.assassin.SeigneurdesVenins());
        register(new com.eldanior.system.classes.definitions.assassin.SeigneurduVenin());
        register(new com.eldanior.system.classes.definitions.assassin.ShinobiSupreme());
        register(new com.eldanior.system.classes.definitions.assassin.SicaireElite());
        register(new com.eldanior.system.classes.definitions.assassin.SouveraindesOmbres());
        register(new com.eldanior.system.classes.definitions.assassin.SpectreOriginel());
        register(new com.eldanior.system.classes.definitions.assassin.SpectreSilencieux());
        register(new com.eldanior.system.classes.definitions.assassin.ToxineEternelle());
        register(new com.eldanior.system.classes.definitions.assassin.TrancheurNoir());
        register(new com.eldanior.system.classes.definitions.assassin.TranscendanceLame());
        register(new com.eldanior.system.classes.definitions.assassin.TraqueurAbsolu());
        register(new com.eldanior.system.classes.definitions.assassin.TraqueurImplacable());
        register(new com.eldanior.system.classes.definitions.assassin.TyrandesOmbres());
        register(new com.eldanior.system.classes.definitions.assassin.VeilleurdelOmbre());
        register(new com.eldanior.system.classes.definitions.assassin.VeninAbsolu());
        register(new com.eldanior.system.classes.definitions.assassin.VipereNoire());
        register(new com.eldanior.system.classes.definitions.assassin.VoidSupreme());
        register(new com.eldanior.system.classes.definitions.assassin.VoidWalker());
        register(new com.eldanior.system.classes.definitions.assassin.VoleurMaitre());

        // Mage tier 2 evolutions (76)
        register(new com.eldanior.system.classes.definitions.mage.ArcheDruide());
        register(new com.eldanior.system.classes.definitions.mage.ArcheLiche());
        register(new com.eldanior.system.classes.definitions.mage.ArchiMagus());
        register(new com.eldanior.system.classes.definitions.mage.ArchimageRoyal());
        register(new com.eldanior.system.classes.definitions.mage.ArchimageSupreme());
        register(new com.eldanior.system.classes.definitions.mage.ArchitecteduMonde());
        register(new com.eldanior.system.classes.definitions.mage.ArchonteSupreme());
        register(new com.eldanior.system.classes.definitions.mage.BrasierVivant());
        register(new com.eldanior.system.classes.definitions.mage.CatalyseurArcane());
        register(new com.eldanior.system.classes.definitions.mage.ChronoSeigneur());
        register(new com.eldanior.system.classes.definitions.mage.ConjurateurMaitre());
        register(new com.eldanior.system.classes.definitions.mage.CreateurCosmique());
        register(new com.eldanior.system.classes.definitions.mage.DemiArcane());
        register(new com.eldanior.system.classes.definitions.mage.DemiMage());
        register(new com.eldanior.system.classes.definitions.mage.DemiurgeSupreme());
        register(new com.eldanior.system.classes.definitions.mage.DevinCosmique());
        register(new com.eldanior.system.classes.definitions.mage.DruidePrimordial());
        register(new com.eldanior.system.classes.definitions.mage.ElementaireSupreme());
        register(new com.eldanior.system.classes.definitions.mage.ElementalisteMaitre());
        register(new com.eldanior.system.classes.definitions.mage.EnchanteurRoyal());
        register(new com.eldanior.system.classes.definitions.mage.EnvouteurMaitre());
        register(new com.eldanior.system.classes.definitions.mage.EruditArcane());
        register(new com.eldanior.system.classes.definitions.mage.ForceNaturelle());
        register(new com.eldanior.system.classes.definitions.mage.ForceOriginelle());
        register(new com.eldanior.system.classes.definitions.mage.GardiendelaNature());
        register(new com.eldanior.system.classes.definitions.mage.GardiendesAmes());
        register(new com.eldanior.system.classes.definitions.mage.GenesesMage());
        register(new com.eldanior.system.classes.definitions.mage.GlacialMage());
        register(new com.eldanior.system.classes.definitions.mage.GrandAlchimiste());
        register(new com.eldanior.system.classes.definitions.mage.GrandArchimage());
        register(new com.eldanior.system.classes.definitions.mage.GrandEnchanteur());
        register(new com.eldanior.system.classes.definitions.mage.GrandGuerisseur());
        register(new com.eldanior.system.classes.definitions.mage.GrandIllusionniste());
        register(new com.eldanior.system.classes.definitions.mage.GrandSage());
        register(new com.eldanior.system.classes.definitions.mage.GrandThaumaturge());
        register(new com.eldanior.system.classes.definitions.mage.InfernalMage());
        register(new com.eldanior.system.classes.definitions.mage.InvocateurSupreme());
        register(new com.eldanior.system.classes.definitions.mage.InvocateurdAmes());
        register(new com.eldanior.system.classes.definitions.mage.LicheEternelle());
        register(new com.eldanior.system.classes.definitions.mage.MageNoir());
        register(new com.eldanior.system.classes.definitions.mage.MageVoidAbsolu());
        register(new com.eldanior.system.classes.definitions.mage.MagusEternel());
        register(new com.eldanior.system.classes.definitions.mage.MagusSupreme());
        register(new com.eldanior.system.classes.definitions.mage.MaitredInvocation());
        register(new com.eldanior.system.classes.definitions.mage.MaitredesFlammes());
        register(new com.eldanior.system.classes.definitions.mage.MaitreduGivre());
        register(new com.eldanior.system.classes.definitions.mage.MaitreduMirage());
        register(new com.eldanior.system.classes.definitions.mage.MaitreduProdige());
        register(new com.eldanior.system.classes.definitions.mage.MaitreduTemps());
        register(new com.eldanior.system.classes.definitions.mage.MentorantSupreme());
        register(new com.eldanior.system.classes.definitions.mage.MystiqueSuperieur());
        register(new com.eldanior.system.classes.definitions.mage.NeantMystique());
        register(new com.eldanior.system.classes.definitions.mage.NecromancienNoir());
        register(new com.eldanior.system.classes.definitions.mage.OracleAbsolu());
        register(new com.eldanior.system.classes.definitions.mage.OracleDivin());
        register(new com.eldanior.system.classes.definitions.mage.PhilosopheMage());
        register(new com.eldanior.system.classes.definitions.mage.PilierdesMondes());
        register(new com.eldanior.system.classes.definitions.mage.PretreArcane());
        register(new com.eldanior.system.classes.definitions.mage.PrimordialAncien());
        register(new com.eldanior.system.classes.definitions.mage.PropheteAbsolu());
        register(new com.eldanior.system.classes.definitions.mage.PropheteduDestin());
        register(new com.eldanior.system.classes.definitions.mage.SeigneurArcanique());
        register(new com.eldanior.system.classes.definitions.mage.SeigneurNonMort());
        register(new com.eldanior.system.classes.definitions.mage.SeigneurdesElements());
        register(new com.eldanior.system.classes.definitions.mage.SeigneurdesMorts());
        register(new com.eldanior.system.classes.definitions.mage.SeigneurduFroid());
        register(new com.eldanior.system.classes.definitions.mage.SorcierSupreme());
        register(new com.eldanior.system.classes.definitions.mage.ThaumaturgeRoyal());
        register(new com.eldanior.system.classes.definitions.mage.TisseurTemporel());
        register(new com.eldanior.system.classes.definitions.mage.TisseurdIllusions());
        register(new com.eldanior.system.classes.definitions.mage.TisseurdeRunes());
        register(new com.eldanior.system.classes.definitions.mage.TransmutateurDivin());
        register(new com.eldanior.system.classes.definitions.mage.VisionnaireSupreme());
        register(new com.eldanior.system.classes.definitions.mage.VoidPrimordial());
        register(new com.eldanior.system.classes.definitions.mage.VoixdelEternel());
        register(new com.eldanior.system.classes.definitions.mage.VoyantArcane());

        // Marchand tier 2 evolutions (15)
        register(new com.eldanior.system.classes.definitions.marchand.ArtisanLegendaire());
        register(new com.eldanior.system.classes.definitions.marchand.AvatardelAbondance());
        register(new com.eldanior.system.classes.definitions.marchand.BanquierImperial());
        register(new com.eldanior.system.classes.definitions.marchand.ChasseurdeReliques());
        register(new com.eldanior.system.classes.definitions.marchand.ContrebandierRoyal());
        register(new com.eldanior.system.classes.definitions.marchand.DemiCommercant());
        register(new com.eldanior.system.classes.definitions.marchand.DemiMarchand());
        register(new com.eldanior.system.classes.definitions.marchand.EmpereurSouterrain());
        register(new com.eldanior.system.classes.definitions.marchand.EmpereurdOr());
        register(new com.eldanior.system.classes.definitions.marchand.ForgeurdesMondes());
        register(new com.eldanior.system.classes.definitions.marchand.GrandCaravanier());
        register(new com.eldanior.system.classes.definitions.marchand.GrandMaitredeGuilde());
        register(new com.eldanior.system.classes.definitions.marchand.MaitreNegociant());
        register(new com.eldanior.system.classes.definitions.marchand.ProspecteurRoyal());
        register(new com.eldanior.system.classes.definitions.marchand.RoiduMarcheNoir());

        for (ClassModel model : classes.values()) {
            if (model.getType() != ClassType.NOVICE) {
                evolutionIds.addAll(model.getNextClassId());
            }
        }

        System.out.println("[Eldanior] Evolution IDs détectés : " + evolutionIds);
        System.out.println("[Eldanior] " + classes.size() + " classes RPG chargees.");
    }

    public static boolean isEvolution(String id) {
        return evolutionIds.contains(id);
    }

    public static void register(ClassModel model) {
        if (model != null) {
            classes.put(model.getId(), model);
        }
    }

    public static Collection<ClassModel> getAll() {
        return classes.values();
    }

    public static ClassModel get(String id) {
        return classes.get(id);
    }

    public static ClassModel getByDisplayName(String name) {
        for (ClassModel model : classes.values()) {
            if (model.getDisplayName().equalsIgnoreCase(name)) {
                return model;
            }
        }
        return null;
    }

    public static String getAvailableIds() {
        if (classes.isEmpty()) return "AUCUNE (Erreur d'init)";
        return String.join(", ", classes.keySet());
    }


}