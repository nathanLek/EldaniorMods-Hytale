package com.eldanior.system;

import com.eldanior.system.Leveling.commands.AddXPCommand;
import com.eldanior.system.Leveling.commands.SetLevelCommand;
import com.eldanior.system.TreasureChest.commands.DeleteTreasureCommand;
import com.eldanior.system.TreasureChest.commands.TreasureConfigCommand;
import com.eldanior.system.classes.commands.SetClassCommand;
import com.eldanior.system.skills.commands.GiveRelicCommand;
import com.eldanior.system.skills.commands.WithdrawCommand;
import com.eldanior.system.titles.commands.TitleTwoArgCommand;
import com.eldanior.system.guild.commands.GuildCommand;
import com.eldanior.system.guild.commands.GuildCreateCommand;
import com.eldanior.system.guild.commands.GuildDisbandCommand;
import com.eldanior.system.titles.nobility.commands.*;
import com.eldanior.system.titles.church.commands.ChurchCommand;
import com.eldanior.system.titles.church.commands.ChurchPromoteCommand;
import com.eldanior.system.gui.SystemCommand;
import com.eldanior.system.duel.DuelCommand;
import com.eldanior.system.shop.SellCommand;
import com.eldanior.system.party.commands.PartyCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class ESCommand extends AbstractCommandCollection {

    public ESCommand() {
        super("es", "Commande principale Eldanior System");
        // /es ouverte a tous : chaque sous-commande garde sa propre permission (generee = admin), sauf celles des
        // joueurs qui appellent requireNoPermission(). canGeneratePermission() n'existe plus dans le serveur (ignoree).
        requireNoPermission();
        this.addSubCommand(new AddXPCommand());
        this.addSubCommand(new SetLevelCommand());
        this.addSubCommand(new com.eldanior.system.Leveling.commands.CombatLogCommand());   // /es combat log : journal de combat (admin, 2026-11-25)
        this.addSubCommand(new com.eldanior.system.guards.GuardCommand());
        this.addSubCommand(new com.eldanior.system.guards.IntendantCommand());   // /es intendant fictifs ajouter|retirer (essai, admin)
        this.addSubCommand(new com.eldanior.system.territory.commands.KingdomCommand());   // /es royaume barriere on|off
        this.addSubCommand(new com.eldanior.system.territory.laws.InvasionCommand());
        this.addSubCommand(new com.eldanior.system.territory.teleport.PortalCommand());
        this.addSubCommand(new com.eldanior.system.guards.CityCommand());   // /es ville : registre de la ville du Comte (joueurs, 2026-11-19)   // /es portail info|retirer (admin, 2026-11-19)   // /es invasion lancer|arreter|liste (admin, 2026-11-18)
        this.addSubCommand(new com.eldanior.system.boards.ContractCommand());   // contrats de monstres du tableau de prime
        this.addSubCommand(new com.eldanior.system.familiers.FamilierCommand());   // /es familier info|relacher (+ tests admin)
        this.addSubCommand(new SetClassCommand());
        this.addSubCommand(new com.eldanior.system.classes.observateur.ObservateurCommand());   // /es observateur : Observateur (Roi, Empereur Luo)
        this.addSubCommand(new GiveRelicCommand());
        this.addSubCommand(new DeleteTreasureCommand());
        this.addSubCommand(new TreasureConfigCommand());
        this.addSubCommand(new WithdrawCommand());

        // Titres
        this.addSubCommand(new TitleTwoArgCommand());

        // Rangs
        this.addSubCommand(new RankCommand());
        this.addSubCommand(new RankPromoteCommand());
        this.addSubCommand(new KingdomCommand());

        // Eglise
        this.addSubCommand(new ChurchCommand());
        this.addSubCommand(new ChurchPromoteCommand());

        // Guilde
        this.addSubCommand(new GuildCommand());
        this.addSubCommand(new GuildCreateCommand());
        this.addSubCommand(new GuildDisbandCommand());

        // Groupe
        this.addSubCommand(new PartyCommand());

        // Famille
        this.addSubCommand(new FamilyCommand());
        this.addSubCommand(new FamilySetCommand());

        // Duel
        this.addSubCommand(new DuelCommand());

        // Shop
        this.addSubCommand(new SellCommand());

        // Echange
        this.addSubCommand(new com.eldanior.system.trade.TradeCommand());

        // Parcelles / Territoires
        this.addSubCommand(new com.eldanior.system.territory.commands.ParcelCommand());
        this.addSubCommand(new com.eldanior.system.territory.commands.ParcelCreateCommand());
        this.addSubCommand(new com.eldanior.system.territory.commands.ParcelDeleteCommand());
        this.addSubCommand(new com.eldanior.system.territory.commands.ParcelInviteCommand());
        this.addSubCommand(new com.eldanior.system.territory.commands.ParcelKickCommand());
        this.addSubCommand(new com.eldanior.system.territory.commands.ParcelSetPermCommand());
        this.addSubCommand(new com.eldanior.system.territory.commands.ParcelSellCommand());
        this.addSubCommand(new com.eldanior.system.territory.commands.ParcelSetPriceCommand());
        this.addSubCommand(new com.eldanior.system.territory.commands.ParcelSetRentCommand());
        this.addSubCommand(new com.eldanior.system.territory.commands.ParcelAssignFamilyCommand());
        this.addSubCommand(new com.eldanior.system.territory.commands.ParcelAssignGuildCommand());
        this.addSubCommand(new com.eldanior.system.territory.commands.ParcelSetRankCommand());
        this.addSubCommand(new com.eldanior.system.territory.commands.ParcelSetRegenCommand());

        // Interface unifiee
        this.addSubCommand(new SystemCommand());
        this.addSubCommand(new com.eldanior.system.accueil.WelcomeCommand());   // /es accueil : apercu de l'accueil (admin, 2026-11-25)
        // /es runes retiree le 2026-11-12 : les runes se gerent dans le menu systeme (onglet Competences, RunesTab)

        // Hologrammes
        this.addSubCommand(new com.eldanior.system.hologram.HologramCommand());

        // Interface admin (OP uniquement)
        this.addSubCommand(new com.eldanior.system.gui.AdminCommand());

        // Transactions (admin)
        this.addSubCommand(new com.eldanior.system.economy.commands.TransactionsCommand());
        this.addSubCommand(new com.eldanior.system.economy.commands.PrixCommand());   // /es prix : editeur des prix (admin)

        // Eviction groupee (admin)
        this.addSubCommand(new com.eldanior.system.territory.commands.EvictAllCommand());

        // Audit des taxes (admin)
        this.addSubCommand(new com.eldanior.system.territory.commands.TaxAuditCommand());

        // Backup / Restore (admin)
        this.addSubCommand(new com.eldanior.system.persistence.commands.BackupCommand());

        // Mode debug (admin)
        this.addSubCommand(new com.eldanior.system.config.commands.DebugCommand());
    }

}