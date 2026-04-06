package com.eldanior.system;

import com.eldanior.system.Inventory.commands.InventoryCommand;
import com.eldanior.system.Leveling.commands.*;
import com.eldanior.system.TreasureChest.commands.DeleteTreasureCommand;
import com.eldanior.system.TreasureChest.commands.GenerateTreasureCommand;
import com.eldanior.system.TreasureChest.commands.TreasureConfigCommand;
import com.eldanior.system.classes.commands.ClassInfoCommand;
import com.eldanior.system.classes.commands.SetClassCommand;
import com.eldanior.system.skills.commands.GiveRelicCommand;
import com.eldanior.system.skills.commands.WithdrawCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class ESCommand extends AbstractCommandCollection {

    public ESCommand() {
        super("es", "Commande principale Eldanior System");
        this.addSubCommand(new StatusCommand());
        this.addSubCommand(new AddXPCommand());
        this.addSubCommand(new SetLevelCommand());
        this.addSubCommand(new ClassInfoCommand());
        this.addSubCommand(new SetClassCommand());
        this.addSubCommand(new GiveRelicCommand());
        this.addSubCommand(new InventoryCommand());
        this.addSubCommand(new GenerateTreasureCommand());
        this.addSubCommand(new DeleteTreasureCommand());
        this.addSubCommand(new TreasureConfigCommand());
        this.addSubCommand(new WithdrawCommand());
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }
}