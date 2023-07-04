package data.scripts.industry;

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.HeavyIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Pair;
import data.plugins.AoDUtilis;


public class KaysaarHeavyProduction extends BaseIndustry {


    @Override
    public void apply() {
        super.apply(true);
        int size = market.getSize();
        demand(Commodities.METALS, size -3 );

        supply(Commodities.HEAVY_MACHINERY, size-1);


        Pair<String, Integer> deficit = getMaxDeficit(Commodities.METALS);
        int maxDeficit =  size - 3; // to allow *some* production so economy doesn't get into an unrecoverable state
        if (deficit.two > maxDeficit) deficit.two = maxDeficit;

        applyDeficitToProduction(2, deficit,
                Commodities.HEAVY_MACHINERY);

//		if (market.getId().equals("chicomoztoc")) {
//			System.out.println("efwefwe");
//		}



        if (!isFunctional()) {
            supply.clear();
            unapply();
        }
    }
    public boolean isDemandLegal(CommodityOnMarketAPI com) {
        return true;
    }

    public boolean isSupplyLegal(CommodityOnMarketAPI com) {
        return true;
    }
    @Override
    public boolean showWhenUnavailable() {
        return !AoDUtilis.checkForFamilyIndustryInstance(market,Industries.HEAVYINDUSTRY,Industries.HEAVYINDUSTRY,this.id);
    }

    @Override
    public boolean isAvailableToBuild() {
        return !AoDUtilis.checkForFamilyIndustryInstance(market,Industries.HEAVYINDUSTRY,Industries.HEAVYINDUSTRY,this.id);

    }


    @Override
    public String getUnavailableReason() {
        if(AoDUtilis.checkForFamilyIndustryInstance(market,Industries.HEAVYINDUSTRY,Industries.HEAVYINDUSTRY,this.id)){
            return AoDUtilis.reason;
        }
        return null;
    }

    @Override
    protected boolean canImproveToIncreaseProduction() {
        return true;
    }

}
