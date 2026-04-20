package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DinoExtinctor extends TitleModel {
    public DinoExtinctor() { super("dino_extinctor", "Extincteur de Dinosaures", "Vous etes la meteorite qui a mis fin a leur ere.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(6, 6, 0, 0, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "raptor", 0.15))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return (data.getMobKillCountContaining("raptor") + data.getMobKillCountContaining("trilobite") + data.getMobKillCountContaining("pterodactyl") + data.getMobKillCountContaining("trillodon") + data.getMobKillCountContaining("archaeopteryx") + data.getMobKillCountContaining("trex")) >= 500; }
}