package data.scripts.industry;

import com.fs.starfarer.B;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.Refining;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.util.Pair;
import data.Ids.AodCommodities;
import data.plugins.AoDUtilis;

public class KaysaarSmelting extends BaseIndustry {
    public void apply() {
        super.apply(true);

        int size = market.getSize();


        demand(Commodities.HEAVY_MACHINERY, size - 2); // have to keep it low since it can be circular
        demand(Commodities.ORE, size + 2);
        supply(Commodities.METALS, size-1);

        Pair<String, Integer> deficit = getMaxDeficit(Commodities.HEAVY_MACHINERY, Commodities.ORE);
        int maxDeficit = size - 3; // to allow *some* production so economy doesn't get into an unrecoverable state
        if (deficit.two > maxDeficit) deficit.two = maxDeficit;

        applyDeficitToProduction(2, deficit, Commodities.METALS);


        if (!isFunctional()) {
            supply.clear();
        }
    }
    @Override
    public boolean showWhenUnavailable() {
        return !AoDUtilis.checkForFamilyIndustryInstance(market,Industries.REFINING,Industries.REFINING,this.id);
    }

    @Override
    public boolean isAvailableToBuild() {
        return !AoDUtilis.checkForFamilyIndustryInstance(market,Industries.REFINING,Industries.REFINING,this.id);

    }

    @Override
    public String getUnavailableReason() {
        if(AoDUtilis.checkForFamilyIndustryInstance(market,Industries.REFINING,Industries.REFINING,this.id)){
            return AoDUtilis.reason;
        }
return null;

    }

    @Override
    public void unapply() {
        super.unapply();
    }


    public float getPatherInterest() {
        return 2f + super.getPatherInterest();
    }

    @Override
    protected boolean canImproveToIncreaseProduction() {
        return true;
    }

}
