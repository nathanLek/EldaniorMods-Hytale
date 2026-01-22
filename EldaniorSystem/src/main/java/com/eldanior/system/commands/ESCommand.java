package com.eldanior.system.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class ESCommand extends AbstractCommandCollection {

    public ESCommand() {
        super("es", "Commande principale Eldanior System");
        this.addSubCommand(new SetLevelCommand());
        this.addSubCommand(new LevelCommand());
        this.addSubCommand(new StatusCommand());
        this.addSubCommand(new AddXPCommand());
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }
}