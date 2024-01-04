package data.kaysaar.aotd.vok.ui.components;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.impl.HeavyIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.kaysaar.aotd.vok.Ids.AoTDCommodities;
import data.kaysaar.aotd.vok.Ids.AoTDConditions;
import data.kaysaar.aotd.vok.Ids.AoTDIndustries;
import data.kaysaar.aotd.vok.models.ResearchOption;
import data.kaysaar.aotd.vok.models.ResearchRewardType;
import data.kaysaar.aotd.vok.plugins.ReflectionUtilis;
import data.kaysaar.aotd.vok.scripts.research.AoTDFactionResearchManager;
import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TechTreeResearchOptionPanel extends UiPanel {
    public ResearchOption TechToResearch;
    ButtonAPI currentButton;
    PositionAPI coordinates;
    CustomPanelAPI buttonPanel;
    CustomPanelAPI trueProgressionBar;

    public HashMap<String,CustomPanelAPI> getHexagons() {
        return hexagons;
    }

    public HashMap<String,CustomPanelAPI> hexagons = new HashMap<>();
    public float x = 0;
    public float y = 0;

    @Override
    public void createUI(float x, float y) {
        buttonPanel = panel.createCustomPanel(AoTDUiComp.WIDTH_OF_TECH_PANEL - 1, AoTDUiComp.HEIGHT_OF_TECH_PANEL - 1, null);
        TooltipMakerAPI vTT = buttonPanel.createUIElement(AoTDUiComp.WIDTH_OF_TECH_PANEL, AoTDUiComp.HEIGHT_OF_TECH_PANEL, false);
        trueProgressionBar = buttonPanel.createCustomPanel(AoTDUiComp.WIDTH_OF_TECH_PANEL - 10, (AoTDUiComp.HEIGHT_OF_TECH_PANEL * 0.05f) + 10, null);
        if (AoTDMainResearchManager.getInstance().getManagerForPlayer().haveResearched(TechToResearch.Id)) {
            currentButton = vTT.addAreaCheckbox("", TechToResearch.Id, Misc.getBrightPlayerColor(), Misc.getTooltipTitleAndLightHighlightColor(), Misc.getBrightPlayerColor(), AoTDUiComp.WIDTH_OF_TECH_PANEL, AoTDUiComp.HEIGHT_OF_TECH_PANEL, 0);

        } else if (AoTDMainResearchManager.getInstance().getManagerForPlayer().canResearch(TechToResearch.Id, false)) {
            currentButton = vTT.addAreaCheckbox("", TechToResearch.Id, Misc.getPositiveHighlightColor(), Misc.getDarkHighlightColor(), Misc.getTooltipTitleAndLightHighlightColor(), AoTDUiComp.WIDTH_OF_TECH_PANEL, AoTDUiComp.HEIGHT_OF_TECH_PANEL, 0);

        } else {
            currentButton = vTT.addAreaCheckbox("", TechToResearch.Id, Misc.getNegativeHighlightColor(), Misc.getDarkHighlightColor(), Misc.getTooltipTitleAndLightHighlightColor(), AoTDUiComp.WIDTH_OF_TECH_PANEL, AoTDUiComp.HEIGHT_OF_TECH_PANEL, 0);
            currentButton.setEnabled(false);
        }

        LabelAPI title = vTT.addPara(TechToResearch.Name, Color.ORANGE, 10f);
        title.getPosition().inTL(AoTDUiComp.WIDTH_OF_TECH_PANEL - 5 - title.computeTextWidth(title.getText()), 3);
            vTT.addImage(Global.getSettings().getSpriteName("ui_icons_tech_tree", TechToResearch.getSpec().getIconId()), 100, 100, 10f);
            vTT.getPrev().getPosition().inTL(5, 5);


        vTT.addTooltipToPrevious(new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return true;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 300;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addSectionHeading("Technology Name", Alignment.MID, 10f);
                tooltip.addPara(TechToResearch.Name, 10f);
                tooltip.addSectionHeading("Time need to research", Alignment.MID, 10f);
                float days = TechToResearch.TimeToResearch - TechToResearch.daysSpentOnResearching;
                String d = " days";
                if (days <= 1) {
                    d = " day";
                }
                //TODO - commision faction pickup or player faction
                AoTDFactionResearchManager manager = AoTDMainResearchManager.getInstance().getManagerForPlayer();
                if (!TechToResearch.isResearched()) {
                    tooltip.addPara((int) days + d + " to finish research", Misc.getTooltipTitleAndLightHighlightColor(), 10f);
                } else {
                    tooltip.addPara("Researched!", Misc.getPositiveHighlightColor(), 10f);
                }

                tooltip.addSectionHeading("Unlocks", Alignment.MID, 10f);
                HashMap<ResearchRewardType, Boolean> haveThat = new HashMap<>();
                for (Map.Entry<String, ResearchRewardType> entry : TechToResearch.Rewards.entrySet()) {
                    haveThat.put(entry.getValue(), true);
                }
                for (ResearchRewardType researchRewardType : haveThat.keySet()) {
                    pickHeaderByReward(researchRewardType, tooltip);
                    for (Map.Entry<String, ResearchRewardType> entry : TechToResearch.Rewards.entrySet()) {
                        if (entry.getValue() == researchRewardType) {
                            createInfoFromType(entry.getValue(), entry.getKey(), tooltip);
                        }
                    }
                }
                if (!TechToResearch.ReqTechsToResearchFirst.isEmpty() || (TechToResearch.ReqItemsToResearchFirst != null && !TechToResearch.ReqItemsToResearchFirst.isEmpty())) {
                    tooltip.addSectionHeading("Requirements", Alignment.MID, 10f);
                }
                if (!TechToResearch.ReqTechsToResearchFirst.isEmpty()) {
                    tooltip.setParaInsigniaLarge();
                    tooltip.addPara("Research", Color.ORANGE, 10f);
                    tooltip.setParaFontDefault();

                }
                for (String s : TechToResearch.ReqTechsToResearchFirst) {
                    if (s.equals("none")) continue;
                    if (manager.haveResearched(s)) {
                        tooltip.addPara(manager.findNameOfTech(s).Name, Misc.getPositiveHighlightColor(), 10f);
                    } else {
                        tooltip.addPara(manager.findNameOfTech(s).Name, Misc.getNegativeHighlightColor(), 10f);
                    }

                }

                if (TechToResearch.ReqItemsToResearchFirst != null && !TechToResearch.ReqItemsToResearchFirst.isEmpty()) {
                    tooltip.setParaInsigniaLarge();
                    LabelAPI title = tooltip.addPara("Items", Color.ORANGE, 10f);
                    tooltip.setParaFontDefault();
                    for (Map.Entry<String, Integer> entry : TechToResearch.ReqItemsToResearchFirst.entrySet()) {
                        CustomPanelAPI panel = mainPanel.createCustomPanel(300, 60, null);
                        TooltipMakerAPI tooltipMakerAPI = panel.createUIElement(60, 60, false);
                        TooltipMakerAPI labelTooltip = panel.createUIElement(220, 60, false);
                        LabelAPI labelAPI1 = null;
                        if (Global.getSettings().getCommoditySpec(entry.getKey()) != null) {
                            tooltipMakerAPI.addImage(Global.getSettings().getCommoditySpec(entry.getKey()).getIconName(), 60, 60, 10f);
                            labelAPI1 = labelTooltip.addPara(Global.getSettings().getCommoditySpec(entry.getKey()).getName() + " : " + entry.getValue(), 10f);
                        }
                        if (Global.getSettings().getSpecialItemSpec(entry.getKey()) != null) {
                            tooltipMakerAPI.addImage(Global.getSettings().getSpecialItemSpec(entry.getKey()).getIconName(), 60, 60, 10f);
                            labelAPI1 = labelTooltip.addPara(Global.getSettings().getSpecialItemSpec(entry.getKey()).getName() + " : " + entry.getValue(), 10f);
                        }
                        if (manager.haveMetReqForItem(entry.getKey(), entry.getValue()) || manager.getResearchOptionFromRepo(TechToResearch.Id).havePaidForResearch) {
                            labelAPI1.setColor(Misc.getPositiveHighlightColor());
                        } else {
                            labelAPI1.setColor(Misc.getNegativeHighlightColor());
                        }
                        if (TechToResearch.isResearched) {
                            labelAPI1.setColor(Misc.getPositiveHighlightColor());
                        }
                        panel.addUIElement(tooltipMakerAPI).inTL(-10, -20);
                        panel.addUIElement(labelTooltip).inTL(60, 5);
                        tooltip.addCustom(panel, 10f);
                    }

                }
                if (TechToResearch.otherReq != null) {
                    tooltip.setParaInsigniaLarge();
                    tooltip.addPara("Other", Color.ORANGE, 10f);
                    tooltip.setParaFontDefault();
                    if (!TechToResearch.metOtherReq) {
                        tooltip.addPara(TechToResearch.otherReq.two + "\n", Misc.getNegativeHighlightColor(), 10f);
                    } else {
                        tooltip.addPara(TechToResearch.otherReq.two + "\n", Misc.getPositiveHighlightColor(), 10f);
                    }

                }

            }
        }, TooltipMakerAPI.TooltipLocation.RIGHT);
        int beginx = 85;
        int beginy = 36;
        hexagons.clear();
        for (final Map.Entry<String, ResearchRewardType> rewardsEntry : TechToResearch.Rewards.entrySet()) {
            CustomPanelAPI dummyPanel = Global.getSettings().createCustom(65, 65, null);
            TooltipMakerAPI tooltipMakerAPI=  dummyPanel.createUIElement(65,65,false);

            String imagename;
            if(isImageExisting(rewardsEntry.getKey())){
                imagename = Global.getSettings().getSpriteName("ui_icons_tech_tree",rewardsEntry.getKey()+"_sub");

            }
            else{
                imagename = Global.getSettings().getSpriteName("ui_icons_tech_tree","special");

            }
          tooltipMakerAPI.addImage(imagename,65,65,0f);
          dummyPanel.addUIElement(tooltipMakerAPI).inTL(0,0);
            vTT.addCustom(dummyPanel, 0f).getPosition().inTL(beginx, beginy);
            if (rewardsEntry.getValue().equals(ResearchRewardType.INDUSTRY)){
                vTT.addTooltipToPrevious(new TooltipMakerAPI.TooltipCreator() {
                    @Override
                    public boolean isTooltipExpandable(Object tooltipParam) {
                        return true;
                    }

                    @Override
                    public float getTooltipWidth(Object tooltipParam) {
                        return 400;
                    }

                    @Override
                    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                        MarketAPI marketAPI = initalizeMarket();Industry cover = marketAPI.getIndustry("dummy_industry");
                        marketAPI.addIndustry(Industries.POPULATION);
                        marketAPI.addIndustry(rewardsEntry.getKey());
                        marketAPI.reapplyConditions();
                        Industry ind = marketAPI.getIndustry(rewardsEntry.getKey());
                        TooltipMakerAPI tooltipMakerAPI = panel.createUIElement(100,100,true);
                        ind.createTooltip(Industry.IndustryTooltipMode.NORMAL,tooltipMakerAPI,true);
                        marketAPI.reapplyIndustries();
                        marketAPI.reapplyConditions();
                        if(ind.getId().equals(Industries.FARMING)){
                            ind.getSupply(Commodities.FOOD).getQuantity().modifyFlat("test",6);
                            ind.getSupply(AoTDCommodities.BIOTICS).getQuantity().modifyFlat("test",3);
                            ind.getSupply(AoTDCommodities.RECITIFICATES).getQuantity().modifyFlat("test",3);
                        }
                        if(ind.getId().equals(AoTDIndustries.SUBSIDISED_FARMING)){
                            ind.getSupply(AoTDCommodities.BIOTICS).getQuantity().modifyFlat("test",4);
                            ind.getSupply(AoTDCommodities.RECITIFICATES).getQuantity().modifyFlat("test",4);
                        }
                        if(ind.getId().equals(AoTDIndustries.ARTISANAL_FARMING)){
                            ind.getSupply(AoTDCommodities.BIOTICS).getQuantity().modifyFlat("test",2);
                            ind.getSupply(AoTDCommodities.RECITIFICATES).getQuantity().modifyFlat("test",2);
                        }
                        if(ind.getId().equals(Industries.AQUACULTURE)){
                            ind.getSupply(Commodities.FOOD).getQuantity().modifyFlat("test",6);
                        }
                        if(ind.getId().equals(Industries.MINING)){
                            ind.getSupply(Commodities.ORE).getQuantity().modifyFlat("test",6);
                            ind.getSupply(Commodities.RARE_ORE).getQuantity().modifyFlat("test",4);
                            ind.getSupply(Commodities.ORGANICS).getQuantity().modifyFlat("test",6);
                            ind.getSupply(Commodities.VOLATILES).getQuantity().modifyFlat("test",4);
                        }
                        if(ind instanceof HeavyIndustry&&!ind.getId().equals(AoTDIndustries.HEAVY_PRODUCTION)){
                            ind.getDemand(AoTDCommodities.ELECTRONICS).getQuantity().modifyFlat("test",marketAPI.getSize()-3);
                        }
                        ind.createTooltip(Industry.IndustryTooltipMode.NORMAL,tooltip,true);

                        Global.getSector().getEconomy().removeMarket(marketAPI);
                    }
                }, TooltipMakerAPI.TooltipLocation.RIGHT);
            }
            if (rewardsEntry.getValue().equals(ResearchRewardType.MODIFIER)){
                vTT.addTooltipToPrevious(new TooltipMakerAPI.TooltipCreator() {
                    @Override
                    public boolean isTooltipExpandable(Object tooltipParam) {
                        return true;
                    }

                    @Override
                    public float getTooltipWidth(Object tooltipParam) {
                        return 400;
                    }

                    @Override
                    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                        pickNormalHeaderByReward(rewardsEntry.getValue(), tooltip);
                        for (Map.Entry<String, ResearchRewardType> entry : TechToResearch.Rewards.entrySet()) {
                            if (entry.getValue() == rewardsEntry.getValue()) {
                                createInfoFromType(entry.getValue(), entry.getKey(), tooltip);
                            }
                        }
                    }
                }, TooltipMakerAPI.TooltipLocation.RIGHT);
            }
            hexagons.put(rewardsEntry.getKey(),dummyPanel);

            beginx+=58;
        }


        float days = TechToResearch.TimeToResearch - TechToResearch.daysSpentOnResearching;
        String d = " days";
        if (days <= 1) {
            d = " day";
        }
        d += " to finish research";
        if (AoTDMainResearchManager.getInstance().getManagerForPlayer().getCurrentFocus() != null && AoTDMainResearchManager.getInstance().getManagerForPlayer().getCurrentFocus().
                Id.equals(TechToResearch.Id)) {
            LabelAPI labelAPI = vTT.addPara((int) (days) + d, Misc.getBasePlayerColor(), 10f);
            labelAPI.getPosition().inTL((AoTDUiComp.WIDTH_OF_TECH_PANEL - 1) / 2 - (labelAPI.computeTextWidth(labelAPI.getText())) / 2, 103);


        }
        if (TechToResearch.isResearched) {
            LabelAPI labelAPI = vTT.addPara("Researched!", Misc.getBasePlayerColor(), 10f);
            labelAPI.getPosition().inTL((AoTDUiComp.WIDTH_OF_TECH_PANEL - 1) / 2 - (labelAPI.computeTextWidth(labelAPI.getText())) / 2, 103);

        }

        buttonPanel.addUIElement(vTT).inTL(0, -1);
        buttonPanel.addComponent(trueProgressionBar).inTL(10, (AoTDUiComp.HEIGHT_OF_TECH_PANEL * 0.95f) - 13);
        tooltip.addComponent(buttonPanel).inTL(x, y);
        coordinates = buttonPanel.getPosition();
        this.x = x;
        this.y = y;
    }

    public TechTreeResearchOptionPanel(ResearchOption res) {
        TechToResearch = res;
    }

    public void setTechToResearch(ResearchOption researchOption) {
        this.TechToResearch = researchOption;
    }

    public ButtonAPI getCurrentButton() {
        return this.currentButton;
    }

    public PositionAPI getCoordinates() {
        return coordinates;
    }

    public CustomPanelAPI getProgressionBar() {
        return trueProgressionBar;
    }

    public void reset() {
        tooltip.removeComponent(buttonPanel);
        createUI(this.x, this.y);
    }

    public void createInfoFromType(ResearchRewardType type, String object, TooltipMakerAPI tooltip) {
        if (type == ResearchRewardType.INDUSTRY) {
            IndustrySpecAPI specAPI = Global.getSettings().getIndustrySpec(object);
            tooltip.addPara(specAPI.getName(), Misc.getPositiveHighlightColor(), 10f);
        }
        if (type == ResearchRewardType.MODIFIER) {
            tooltip.addPara(object, Misc.getPositiveHighlightColor(), 10f);
        }
    }

    public void pickHeaderByReward(ResearchRewardType type, TooltipMakerAPI tooltip) {
        tooltip.setParaInsigniaLarge();
        if (type == ResearchRewardType.INDUSTRY) tooltip.addPara("Industries", Color.ORANGE, 10f);
        if (type == ResearchRewardType.MODIFIER) tooltip.addPara("Permanent modifiers", Color.orange, 10f);
        tooltip.setParaFontDefault();
    }
    public void pickNormalHeaderByReward(ResearchRewardType type, TooltipMakerAPI tooltip) {
        tooltip.setParaInsigniaLarge();
        if (type == ResearchRewardType.INDUSTRY) tooltip.addSectionHeading("Industries", Alignment.MID, 10f);
        if (type == ResearchRewardType.MODIFIER) tooltip.addSectionHeading("Permanent modifiers", Alignment.MID, 10f);
        tooltip.setParaFontDefault();
    }

    public MarketAPI initalizeMarket(){
        MarketAPI marketToShowTooltip = Global.getFactory().createMarket("to_delete","TEst",6);
        marketToShowTooltip.addCondition(Conditions.FARMLAND_ADEQUATE);
        marketToShowTooltip.addCondition(Conditions.ORE_MODERATE);
        marketToShowTooltip.addCondition(Conditions.RARE_ORE_MODERATE);
        marketToShowTooltip.addCondition(Conditions.ORGANICS_COMMON);
        marketToShowTooltip.addCondition(Conditions.VOLATILES_DIFFUSE);
        marketToShowTooltip.addCondition("IcDemmand");
        marketToShowTooltip.addCondition("AoDFoodDemand");
        marketToShowTooltip.addCondition("AodReci");
        marketToShowTooltip.addCondition("AodSub");
        marketToShowTooltip.addCondition("AodFood");
        marketToShowTooltip.addCondition(Conditions.VOLATILES_DIFFUSE);
        marketToShowTooltip.addIndustry("dummy_industry");
        marketToShowTooltip.setFactionId(Global.getSector().getPlayerFaction().getId());
        marketToShowTooltip.reapplyConditions();
        marketToShowTooltip.setFreePort(true);
        for (CommodityOnMarketAPI allCommodity : marketToShowTooltip.getAllCommodities()) {
            allCommodity.getAvailableStat().addTemporaryModFlat(10000,"src",30);
        }
        marketToShowTooltip.setUseStockpilesForShortages(true);
        return marketToShowTooltip;
    }
    public boolean isImageExisting(String imageName){
        try {
            Global.getSettings().getSpriteName("ui_icons_tech_tree",imageName+"_sub");
        }
        catch (RuntimeException exception){
            return false;
        }
        return true;

    }
}
