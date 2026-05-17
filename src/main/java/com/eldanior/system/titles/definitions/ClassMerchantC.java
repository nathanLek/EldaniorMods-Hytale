package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ClassMerchantC extends TitleModel {
    public ClassMerchantC() { super("class_merchant_c", "Voie du Marchand", "Choisir la classe Marchand.", Rarity.COMMON, TitleCategory.CLASSE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return "marchand".equalsIgnoreCase(data.getPlayerClassId()) || "merchant".equalsIgnoreCase(data.getPlayerClassId()); }
}
