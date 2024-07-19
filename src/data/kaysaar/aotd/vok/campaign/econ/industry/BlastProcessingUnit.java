package data.kaysaar.aotd.vok.campaign.econ.industry;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.FuelProduction;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.util.Pair;
import data.kaysaar.aotd.vok.Ids.AoTDCommodities;
import data.kaysaar.aotd.vok.Ids.AoTDTechIds;
import data.kaysaar.aotd.vok.plugins.AoDUtilis;
import data.kaysaar.aotd.vok.plugins.AoTDSpecialItemRepo;
import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;

import static data.kaysaar.aotd.vok.plugins.AoDUtilis.checkForItemBeingInstalled;

public class BlastProcessingUnit extends FuelProduction {
    @Override
    public void apply() {
        super.apply(true);
        int size = market.getSize();
        demand(Commodities.VOLATILES, size-2);
        demand(Commodities.HEAVY_MACHINERY, size);
        supply(Commodities.FUEL, size +4);
        Pair<String, Integer> deficit = getMaxDeficit(Commodities.VOLATILES,Commodities.HEAVY_MACHINERY);

        applyDeficitToProduction(1, deficit, Commodities.FUEL);

        if (!isFunctional()) {
            supply.clear();
        }
    }

    @Override
    public boolean isAvailableToBuild() {
        return AoTDMainResearchManager.getInstance().isAvailableForThisMarket(AoTDTechIds.ANTIMATTER_SYNTHESIS,market);
    }
    @Override
    public String getUnavailableReason() {
        return null;
    }

    @Override
    public boolean showWhenUnavailable() {
        return AoTDMainResearchManager.getInstance().isAvailableForThisMarket(AoTDTechIds.ANTIMATTER_SYNTHESIS,market);
    }
}
