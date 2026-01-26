package com.eldanior.system.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class ESCommand extends AbstractCommandCollection {

    public ESCommand() {
        super("es", "Commande principale Eldanior System");
        this.addSubCommand(new StatusCommand());
        this.addSubCommand(new AddXPCommand());
        this.addSubCommand(new SetLevelCommand());
        this.addSubCommand(new ClassInfoCommand());
        this.addSubCommand(new SetClassCommand());
        this.addSubCommand(new GiveSkillItemCommand());
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }
}