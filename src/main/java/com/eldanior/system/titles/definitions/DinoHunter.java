package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DinoHunter extends TitleModel {
    public DinoHunter() { super("dino_hunter", "Chasseur Prehistorique", "Vous chassez les creatures d'un autre age.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(3, 3, 0, 0, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return (data.getMobKillCountContaining("raptor") + data.getMobKillCountContaining("trilobite") + data.getMobKillCountContaining("pterodactyl") + data.getMobKillCountContaining("trillodon") + data.getMobKillCountContaining("archaeopteryx") + data.getMobKillCountContaining("trex")) >= 50; }
}