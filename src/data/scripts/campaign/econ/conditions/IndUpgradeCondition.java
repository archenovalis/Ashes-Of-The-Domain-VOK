package data.scripts.campaign.econ.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.Mining;
import com.fs.starfarer.api.impl.campaign.econ.impl.Refining;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.ui.P;
import data.Ids.AoDIndustries;
import data.plugins.AoDUtilis;
import data.scripts.research.ResearchAPI;
import data.scripts.research.ResearchOption;
import org.magiclib.util.MagicSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static data.plugins.AoDCoreModPlugin.aodTech;

public class IndUpgradeCondition extends BaseMarketConditionPlugin {

    public ResearchAPI researchAPI = (ResearchAPI) Global.getSector().getPersistentData().get(aodTech);
    public static String UpgradeCond = "AodIndUpgrade";
    @Override
    public void apply(String id) {
        super.apply(id);
        if(researchAPI !=null){
            applyUpgrade();

        }

    }


    @Override
    public void unapply(String id) {
        super.unapply(id);
    }
    public void applyUpgrade() {
        for (Industry ind : market.getIndustries()) {
            if(ind.getId().equals(Industries.MINING)){
                ind.getSpec().setDowngrade(AoDIndustries.EXTRACTIVE_OPERATION);
            }
            if(ind.getId().equals(Industries.REFINING)){
                ind.getSpec().setDowngrade(AoDIndustries.SMELTING);
            }
            boolean cont = false;
            for (String s : ind.getSpec().getTags()) {
                if(s.contains("starter")){
                   cont=true;
                   break;
                }
            }
            if(cont){
                continue;
            }
                for (ResearchOption researchOption : researchAPI.getResearchOptions()) {
                    if (!researchOption.hasDowngrade) continue;
                    if (!researchOption.industryId.equals(ind.getId())) continue;
                    if (!ind.isUpgrading()) {
                        ind.getSpec().setUpgrade(null);
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
