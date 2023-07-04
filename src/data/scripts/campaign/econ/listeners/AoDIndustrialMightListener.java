package data.scripts.campaign.econ.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.listeners.PlayerColonizationListener;
import data.scripts.campaign.econ.conditions.IcDemmand;
import data.scripts.campaign.econ.conditions.IndustrialMightCondition;
import data.scripts.campaign.econ.conditions.WaterMinningCond;

public class AoDIndustrialMightListener implements PlayerColonizationListener, EconomyTickListener {
    public static void applyResourceConditionToAllMarkets() {
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            IndustrialMightCondition.applyRessourceCond(m);
        }
    }

    @Override
    public void reportPlayerColonizedPlanet(PlanetAPI planetAPI) {
        MarketAPI m = planetAPI.getMarket();
        IndustrialMightCondition.applyRessourceCond(m);
    }

    @Override
    public void reportPlayerAbandonedColony(MarketAPI marketAPI) {

    }

    @Override
    public void reportEconomyTick(int i) {
        applyResourceConditionToAllMarkets();
    }

    @Override
    public void reportEconomyMonthEnd() {

    }

}
