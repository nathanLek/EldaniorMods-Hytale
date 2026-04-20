package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class RichNoble extends TitleModel {
    public RichNoble() { super("rich_noble", "Noble Fortune", "L or et le sang bleu coulent ensemble.", Rarity.EPIC, TitleCategory.SOCIAL, new TitleBonus(0, 0, 0, 0, 0, 5), List.of(new TitleEffect(TitleEffect.TitleEffectType.MONEY_BONUS_PERCENT, "all", 0.08))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { String r = data.getNobilityRank(); return data.getMoney() >= 1000000 && r != null && !r.equals("ROTURIER") && !r.equals("CHEVALIER"); }
}
