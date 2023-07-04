package data.scripts.campaign.econ.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import data.Ids.AodCommodities;

public class WaterMinningCond extends BaseMarketConditionPlugin {
    public static final String UpgradeCond = "watterSupplyMining";
    @Override
    public void apply(String id) {
        super.apply(id);
      ApplyWaterSupply();
    }
    @Override
    public void unapply(String id) {
        super.unapply(id);
        for (Industry ind : market.getIndustries()) {
            unapplyElectronicsDemand((BaseIndustry) ind);
        }

    }
    public void ApplyWaterSupply() {
        int size = market.getSize();
        boolean isCryovolcanicOrFrozen = isCryovolcanicOrFrozen();
        for (Industry ind : market.getIndustries()) {
            if (ind.getId().equals(Industries.MINING)) {
                applyCommoditySupplyToIndustry((BaseIndustry) ind, market.getSize(),isCryovolcanicOrFrozen);

            }


        }

    }

    private boolean isCryovolcanicOrFrozen() {
        boolean isCryovolcanicOrFrozen = false;
        if(market.getPlanetEntity()!=null){
            if(market.getPlanetEntity().getTypeId().equals("frozen") ||market.getPlanetEntity().getTypeId().equals("cryovolcanic")||market.getPlanetEntity().getTypeId().equals("frozen1")){
                isCryovolcanicOrFrozen= true;
            }
        }
        return isCryovolcanicOrFrozen;
    }

    public void unapplyElectronicsDemand(BaseIndustry ind) {
        ind.supply(AodCommodities.WATER, 0, "");

    }
    public void applyCommoditySupplyToIndustry(BaseIndustry ind, int demand,boolean canApply) {
        if (!canApply) return;
        if (ind.getId().equals(Industries.MINING)) {
            ind.supply(AodCommodities.WATER, market.getSize()-2);
            ind.getSupply(AodCommodities.WATER).getQuantity().unmodify(getModId());
            if (ind.getSpecialItem() != null) {
                if (ind.getSpecialItem().getId().equals(Items.MANTLE_BORE)) {
                    ind.getSupply(AodCommodities.WATER).getQuantity().modifyFlat(getModId(), +3, "Mantle Bore");
                }


            }


        }
    }
    public static void applyIndustryUpgradeCondition(MarketAPI marketAPI) {
        if (marketAPI.isInEconomy() && !marketAPI.hasCondition(UpgradeCond)){
            marketAPI.addCondition(UpgradeCond);
        }
    }
    @Override
    public boolean showIcon() {
        return false;
    }

    public String getModId() {
        return condition.getId();
    }

}
