package com.eldanior.system;

import com.eldanior.system.Leveling.commands.*;
import com.eldanior.system.classes.commands.ClassInfoCommand;
import com.eldanior.system.classes.commands.SetClassCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class ESCommand extends AbstractCommandCollection {

    public ESCommand() {
        super("es", "Commande principale Eldanior System");
        this.addSubCommand(new StatusCommand());
        this.addSubCommand(new AddXPCommand());
        this.addSubCommand(new SetLevelCommand());
        this.addSubCommand(new ClassInfoCommand());
        this.addSubCommand(new SetClassCommand());
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }
}