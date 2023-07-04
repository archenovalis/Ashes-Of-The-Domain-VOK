package data.plugins;


import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import data.Ids.AoDConditions;
import data.Ids.AoDIndustries;
import data.Ids.AodMemFlags;
import data.scripts.NexerlinColonyStartNerf;
import data.scripts.campaign.econ.listeners.*;
import data.scripts.research.*;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.util.Misc;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AoDCoreModPlugin extends BaseModPlugin {

    public int maxTriTachyonElectronics = 2;
    public static String aodTech = "$Aodtecha";
    public static boolean isInColony = false;
    public static String sophia = "sophia";
    public int configSize = 6;

    public void setIndustryOnPlanet(String SystemName, String Planetname, String industryId, String removeIndustry, String potentialSwitch, boolean toImprove, String aiCore) {
        if (Global.getSector().getStarSystem(SystemName) == null) return;
        List<PlanetAPI> planets = Global.getSector().getStarSystem(SystemName).getPlanets();
        for (PlanetAPI planet : planets) {
            if (planet.getName().equals(Planetname)) {
                if (planet.getMarket() == null) continue;
                if (removeIndustry != null) {
                    planet.getMarket().removeIndustry(removeIndustry, null, false);

                }
                if (industryId != null) {
                    planet.getMarket().addIndustry(industryId);
                    if (industryId.equals("vault_aotd")) {
                        if (Planetname.equals("Chicomoztoc")) {
                            placeSpecialDatabank("hegeheavy_databank", planet);
                        }
                        if (Planetname.equals("Culann")) {
                            placeSpecialDatabank("triheavy_databank", planet);
                        }
                        if (Planetname.equals("Byzantium")) {
                            placeSpecialDatabank("ii_ind_databank", planet);
                        }


                    }
                    planet.getMarket().getIndustry(industryId).setImproved(toImprove);
                    planet.getMarket().getIndustry(industryId).setAICoreId(aiCore);
                }

                if (potentialSwitch != null) {
                    if (planet.getMarket() != null) {
                        if (!planet.getMarket().hasCondition(potentialSwitch)) {
                            planet.getMarket().addCondition(potentialSwitch);
                        }

                    }
                }

            }
        }
    }

    private static void placeSpecialDatabank(String specialDatabank, PlanetAPI planet) {
        SpecialItemData special;
        special = new SpecialItemData(specialDatabank, null);

        planet.getMarket().getIndustry("vault_aotd").setSpecialItem(special);
    }


    private void setListenersIfNeeded() {
        ListenerManagerAPI l = Global.getSector().getListenerManager();

        if (!l.hasListenerOfClass(RescourceCondition.class))
            l.addListener(new RescourceCondition(), true);
        if (!l.hasListenerOfClass(IndUpgradeListener.class))
            l.addListener(new IndUpgradeListener(), true);
        if (!l.hasListenerOfClass(ResearchPanelListener.class))
            l.addListener(new ResearchPanelListener(), true);
        if (!l.hasListenerOfClass(UpgradeOptionsListener.class))
            l.addListener(new UpgradeOptionsListener(), true);
        if (!l.hasListenerOfClass(AgriProdSwitchListener.class))
            l.addListener(new AgriProdSwitchListener(), true);
        if (!l.hasListenerOfClass(AoDFoodDemmandListener.class))
            l.addListener(new AoDFoodDemmandListener(), true);
        if (!l.hasListenerOfClass(AodAdvancedHeavyIndustryApplier.class))
            l.addListener(new AodAdvancedHeavyIndustryApplier(), true);
        if (!l.hasListenerOfClass(AoDIndustrialMightListener.class))
            l.addListener(new AoDIndustrialMightListener(), true);
    }

    private void setVanilaIndustriesDowngrades() {
        for (IndustrySpecAPI allIndustrySpec : Global.getSettings().getAllIndustrySpecs()) {
            if (allIndustrySpec.getId().equals(Industries.FARMING)) {
                allIndustrySpec.setDowngrade(AoDIndustries.MONOCULTURE);
            }
            if (allIndustrySpec.getId().equals(Industries.AQUACULTURE)) {
                allIndustrySpec.setDowngrade(AoDIndustries.FISHING);
                List<String> str = new ArrayList<>();
                for (String tag : allIndustrySpec.getTags()) {
                    if (tag.equals("farming")) continue;
                    str.add(tag);
                }
                allIndustrySpec.getTags().clear();
                str.add(Industries.AQUACULTURE);
                for (String s : str) {
                    allIndustrySpec.addTag(s);
                }
            }
            if (allIndustrySpec.getId().equals(Industries.MINING)) {
                allIndustrySpec.setDowngrade(AoDIndustries.EXTRACTIVE_OPERATION);
            }
            if (allIndustrySpec.getId().equals(Industries.REFINING)) {
                allIndustrySpec.setDowngrade(AoDIndustries.SMELTING);
            }
            if (allIndustrySpec.getId().equals(Industries.LIGHTINDUSTRY)) {
                allIndustrySpec.setDowngrade(AoDIndustries.LIGHT_PRODUCTION);
            }
            if (allIndustrySpec.getId().equals(Industries.HEAVYINDUSTRY)) {
                allIndustrySpec.setDowngrade(AoDIndustries.HEAVY_PRODUCTION);
            }
            if (allIndustrySpec.getId().equals(Industries.WAYSTATION)) {
                allIndustrySpec.addTag("starter");
            }
        }
    }

    private void setAoDTier0UpgradesIfResearched(ResearchAPI researchAPI) {
        for (ResearchOption researchOption : researchAPI.getResearchOptions()) {
            if (!researchOption.isResearched) continue;
            if (!researchOption.hasDowngrade) continue;
            IndustrySpecAPI specAPI = Global.getSettings().getIndustrySpec(researchOption.downgradeId);
            for (String tag : specAPI.getTags()) {
                if (tag.contains("starter")) {
                    specAPI.setUpgrade(researchOption.industryId);
                }
            }
        }
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        super.onNewGameAfterEconomyLoad();
        ResearchAPI researchAPI = new ResearchAPI();
        try {
            researchAPI.loadMergedCSV();
            researchAPI.initializeResearchList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Global.getSector().getPersistentData().remove(aodTech);
        Global.getSector().getPersistentData().remove(AodMemFlags.RESEARCH_SAVED);
        Global.getSector().getPersistentData().put(aodTech, researchAPI);
        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();

        PersonAPI sfckweenPerson = Global.getFactory().createPerson();
        sfckweenPerson.setId(sophia);
        sfckweenPerson.setFaction(Factions.INDEPENDENT);
        sfckweenPerson.setGender(FullName.Gender.FEMALE);
        sfckweenPerson.setRankId(Ranks.POST_SCIENTIST);
        sfckweenPerson.setPostId(Ranks.POST_SCIENTIST);
        sfckweenPerson.setImportance(PersonImportance.HIGH);
        sfckweenPerson.setVoice(Voices.SCIENTIST);
        sfckweenPerson.getName().setFirst("Sophia");
        sfckweenPerson.getName().setLast("Ashley");
        sfckweenPerson.getTags().add("aotd_researcher");
        sfckweenPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sophia"));
        sfckweenPerson.getStats().setSkillLevel("aotd_resourceful", 0);
        ip.addPerson(sfckweenPerson);


    }

    public void RandomSetIndustryOnPlanet(String industryId, int amount, String PlanetType) {
        int count = 0;
        int max_tritachyon = 0;
        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            if (count > amount) {
                break;
            }
            if (faction.isPlayerFaction() || faction.getId().equals("luddic_church") || faction.getId().equals("luddic_path") || faction.getId().equals("pirates") || faction.getId().equals("derelicts")) {
                continue;
            }
            for (MarketAPI aiMarket : Misc.getFactionMarkets(faction.getId())) {
                if (PlanetType != null) {
                    if (aiMarket.getPlanetEntity() != null) {
                        if (aiMarket.getPlanetEntity().getTypeId().contains(PlanetType) && industryId.equals(AoDIndustries.PURIFICATION_CENTER)) {
                            aiMarket.addIndustry(AoDIndustries.PURIFICATION_CENTER);
                            return;
                        }
                    }

                }

                for (Industry industry : aiMarket.getIndustries()) {
                    if (industry.isIndustry()) {
                        if (industry.getId().equals("heavyindustry")
                                || industry.getId().equals("orbitalworks")
                                || industry.getId().equals("militarybase")
                                || industry.getId().equals("highcommand")
                        ) {
                            continue;
                        }
                        aiMarket.removeIndustry(industry.getId(), null, false);
                        aiMarket.addIndustry(industryId);
                        count++;
                        break;
                    }

                }

                if (aiMarket.getFactionId().equals("tritachyon")) {
                    if (max_tritachyon >= maxTriTachyonElectronics) {
                        break;
                    }
                    max_tritachyon++;

                } else {
                    break;
                }

            }
        }
    }

    @Override
    public void afterGameSave() {
        super.afterGameSave();
        ResearchAPI researchAPI = (ResearchAPI) Global.getSector().getPersistentData().get(aodTech);
        Global.getSector().getPersistentData().remove(aodTech);
        Global.getSector().getPersistentData().put(aodTech, researchAPI);
        researchAPI.saveResearch(true);
    }

    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);
        if (Global.getSector().hasScript(NexerlinColonyStartNerf.class)) {
            Global.getSector().removeScriptsOfClass(NexerlinColonyStartNerf.class);

        }
        Global.getSettings().resetCached();
        ResearchAPI researchAPI = (ResearchAPI) Global.getSector().getPersistentData().get(aodTech);
        if (researchAPI == null) {
            researchAPI = new ResearchAPI();
            try {
                researchAPI.loadMergedCSV();
                researchAPI.initializeResearchList();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            Global.getSector().getPersistentData().remove(aodTech);
            Global.getSector().getPersistentData().remove(AodMemFlags.RESEARCH_SAVED);
            Global.getSector().getPersistentData().put(aodTech, researchAPI);
        }

        insertSophia();
        if(!Global.getSector().getMemory().contains("$aotd_sophia")){
            Global.getSector().getMemory().set("$aotd_sophia",false);
        }
        setVanilaIndustriesDowngrades();
        setVanilaSpecialItemNewIndustries(Items.SOIL_NANITES, "subfarming");
        setVanilaSpecialItemNewIndustries(Items.CATALYTIC_CORE, "crystalizator,isotope_separator");
        setVanilaSpecialItemNewIndustries(Items.BIOFACTORY_EMBRYO, "lightproduction,consumerindustry");
        setVanilaSpecialItemNewIndustries(Items.PRISTINE_NANOFORGE, "supplyheavy,weaponheavy");
        setVanilaSpecialItemNewIndustries(Items.PRISTINE_NANOFORGE, "supplyheavy,weaponheavy");
        setVanilaSpecialItemNewIndustries(Items.CORRUPTED_NANOFORGE, "supplyheavy,weaponheavy");
        setVanilaSpecialItemNewIndustries(Items.PRISTINE_NANOFORGE, "supplyheavy,weaponheavy");
        if(!Global.getSector().getMemory().contains("$aotd_can_scientist")){
            Global.getSector().getMemory().set("$aotd_can_scientist",false);
        }

        Global.getSettings().getIndustrySpec(Industries.FUELPROD).addTag("starter");
        Global.getSettings().getIndustrySpec(Industries.WAYSTATION).addTag("starter");
        Global.getSettings().getIndustrySpec(Industries.ORBITALWORKS).addTag("casual_upgrade");
        if (!Global.getSector().getPlayerFaction().getMemory().is(AodMemFlags.AOD_INITALIZED, true)) {
            Global.getSector().getPersistentData().put(AodMemFlags.RESEARCH_SAVED, new HashMap<String, Boolean>());

            researchAPI.handleOtherModsAvailbility();

            researchAPI.saveResearch(true);
            boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
            if (haveNexerelin && Global.getSector().getMemoryWithoutUpdate().getBoolean("$nex_randomSector")) {

                RandomSetIndustryOnPlanet(AoDIndustries.CLEANROOM_MANUFACTORY, 2, null);
                RandomSetIndustryOnPlanet(AoDIndustries.PURIFICATION_CENTER, 1, Planets.PLANET_WATER);
                Global.getSector().getMemoryWithoutUpdate().set("$nexRandAod", true);
                try {
                    researchAPI.loadMergedCSV();
                    researchAPI.updateResearchListFromCSV();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                setIndustriesOnModdedPlanets();
            } else {
                setIndustriesOnVanilaPlanets();
                setIndustriesOnModdedPlanets();
            }

        } else {

            researchAPI = (ResearchAPI) Global.getSector().getPersistentData().get(aodTech);
            if (!Global.getSector().getMemory().contains("$update_1.2.0_aotd")) {
                Global.getSector().getMemory().set("$update_1.2.0_aotd", true);
                researchAPI = (ResearchAPI) Global.getSector().getPersistentData().get(aodTech);
                ResearchAPI updatedApi = new ResearchAPI();
                updatedApi.setCurrentResearching(researchAPI.getCurrentResearching());
                updatedApi.getResearchOptions().addAll(researchAPI.getAllResearchedOptions());
                updatedApi.setResearching(researchAPI.isResearching());
                researchAPI = updatedApi;
                Global.getSector().getPersistentData().remove(aodTech);
                Global.getSector().getPersistentData().put(aodTech, researchAPI);
            }
            researchAPI.loadMergedCSV();
            try {
                researchAPI.updateResearchListFromCSV();

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            researchAPI.handleOtherModsAvailbility();
            researchAPI.clearResearchFromErrors();
            researchAPI.removeCopies();
            setAoDTier0UpgradesIfResearched(researchAPI);

            researchAPI.saveResearch(true);

        }


        setListenersIfNeeded();
        configSize = Misc.MAX_COLONY_SIZE;
        RescourceCondition.applyResourceConditionToAllMarkets();
        IndUpgradeListener.applyIndustyUpgradeCondition();
        Global.getSector().getPlayerFaction().getMemory().set(AodMemFlags.AOD_INITALIZED, true);
        Global.getSector().getMemory().set("$update_1.2.0_aotd", true);
        CampaignEventListener customlistener = new CampaignEventListener() {
            @Override
            public void reportPlayerOpenedMarket(MarketAPI market) {
                isInColony = true;
            }

            @Override
            public void reportPlayerClosedMarket(MarketAPI market) {
                isInColony = false;
            }

            @Override
            public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {

            }

            @Override
            public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {

            }

            @Override
            public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {

            }

            @Override
            public void reportBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle) {

            }

            @Override
            public void reportBattleFinished(CampaignFleetAPI primaryWinner, BattleAPI battle) {

            }

            @Override
            public void reportPlayerEngagement(EngagementResultAPI result) {

            }

            @Override
            public void reportFleetDespawned(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {

            }

            @Override
            public void reportFleetSpawned(CampaignFleetAPI fleet) {

            }

            @Override
            public void reportFleetReachedEntity(CampaignFleetAPI fleet, SectorEntityToken entity) {

            }

            @Override
            public void reportFleetJumped(CampaignFleetAPI fleet, SectorEntityToken from, JumpPointAPI.JumpDestination to) {

            }

            @Override
            public void reportShownInteractionDialog(InteractionDialogAPI dialog) {

            }

            @Override
            public void reportPlayerReputationChange(String faction, float delta) {

            }

            @Override
            public void reportPlayerReputationChange(PersonAPI person, float delta) {

            }

            @Override
            public void reportPlayerActivatedAbility(AbilityPlugin ability, Object param) {

            }

            @Override
            public void reportPlayerDeactivatedAbility(AbilityPlugin ability, Object param) {

            }

            @Override
            public void reportPlayerDumpedCargo(CargoAPI cargo) {

            }


            @Override
            public void reportPlayerDidNotTakeCargo(CargoAPI cargo) {

            }

            @Override
            public void reportEconomyTick(int iterIndex) {

            }

            @Override
            public void reportEconomyMonthEnd() {
                //for testing purpouse

            }
        };

        Global.getSector().addListener(customlistener);
        if (!Global.getSector().getMemory().contains("$update_1.2.0_aotdhot1")) {
            Global.getSector().getMemory().set("$update_1.2.0_aotdhot1", true);
            Global.getSector().removeScriptsOfClass(RemnantSeededFleetManager.class);
        }
        if (!Global.getSector().hasScript(ResearchProgressScript.class)) {
            Global.getSector().addScript(new ResearchProgressScript());

        }

        for (MarketAPI playerMarket : Misc.getPlayerMarkets(false)) {
            if (playerMarket.hasCondition("AodIndUpgrade")) {
                playerMarket.removeCondition("AodIndUpgrade");
                playerMarket.addCondition("AodIndUpgrade");
            }
            if (playerMarket.hasCondition("aotd_industrial_might")) {
                playerMarket.removeCondition("aotd_industrial_might");
                playerMarket.addCondition("aotd_industrial_might");
            }
        }
    }

    private static void insertSophia() {
        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        PersonAPI sophiaAshley = Global.getFactory().createPerson();
        sophiaAshley.setId(sophia);
        sophiaAshley.setFaction(Factions.INDEPENDENT);
        sophiaAshley.setGender(FullName.Gender.FEMALE);
        sophiaAshley.setRankId(Ranks.POST_SCIENTIST);
        sophiaAshley.setPostId(Ranks.POST_SCIENTIST);
        sophiaAshley.setImportance(PersonImportance.HIGH);
        sophiaAshley.setVoice(Voices.SCIENTIST);
        sophiaAshley.getName().setFirst("Sophia");
        sophiaAshley.getName().setLast("Ashley");
        sophiaAshley.getTags().add("aotd_researcher");
        sophiaAshley.getTags().add("aotd_resourceful");
        sophiaAshley.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sophia"));
        sophiaAshley.getStats().setSkillLevel("aotd_resourceful", 1);

        if (!ip.containsPerson(sophiaAshley)) {
            ip.addPerson(sophiaAshley);
        }
    }

    private void setIndustriesOnModdedPlanets() {
        int increased_farming = 3;
        int increased_biotiocs = 0;
        int increased_reci = 0;
        boolean chooseReci = false;
        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            if (faction.getId().equals(Factions.HEGEMONY)) continue;
            if (faction.getId().equals(Factions.TRITACHYON)) continue;
            if (faction.getId().equals(Factions.DIKTAT)) continue;
            if (faction.getId().equals(Factions.PIRATES)) continue;
            if (faction.getId().equals(Factions.LUDDIC_PATH)) continue;
            if (faction.getId().equals(Factions.LUDDIC_CHURCH)) continue;
            if (faction.getId().equals(Factions.INDEPENDENT)) continue;

            for (MarketAPI market : Misc.getFactionMarkets(faction)) {
                if (market.getPlanetEntity() == null) continue;
                if (AoDUtilis.getFoodQuantityBonus(market) >= -1) {
                    if (market.hasIndustry(Industries.FARMING)) {
                        if (increased_biotiocs + increased_reci >= increased_farming) {
                            increased_farming++;

                        } else {
                            if (chooseReci) {
                                setIndustryOnPlanet(market.getStarSystem().getBaseName(), market.getPlanetEntity().getName(), null, null, AoDConditions.SWITCH_RECITIFICATES, false, null);
                                increased_reci++;
                                chooseReci = false;
                            } else {
                                setIndustryOnPlanet(market.getStarSystem().getBaseName(), market.getPlanetEntity().getName(), null, null, AoDConditions.SWITCH_BIOTICS, false, null);
                                increased_biotiocs++;
                                chooseReci = true;
                            }


                        }

                    }

                }
            }

        }
    }

    private void setIndustriesOnVanilaPlanets() {
        setIndustryOnPlanet("Hybrasil", "Culann", AoDIndustries.TRI_TACHYON_HEAVY, Industries.ORBITALWORKS, null, false, Commodities.ALPHA_CORE);
        setIndustryOnPlanet("Aztlan", "Chicomoztoc", AoDIndustries.HEGEMONY_HEAVY, Industries.ORBITALWORKS, null, false, null);
        setIndustryOnPlanet("Hybrasil", "Culann", AoDIndustries.PLANETARY_DEFENCE_FORCE, null, null, true, Commodities.ALPHA_CORE);
        setIndustryOnPlanet("Aztlan", "Chicomoztoc", AoDIndustries.PLANETARY_DEFENCE_FORCE, null, null, false, null);
        setIndustryOnPlanet("Corvus", "Jangala", AoDIndustries.CLEANROOM_MANUFACTORY, null, null, false, null);
        setIndustryOnPlanet("Hybrasil", "Eochu Bres", AoDIndustries.CLEANROOM_MANUFACTORY, null, null, false, null);
        setIndustryOnPlanet("Tyle", "Madeira", AoDIndustries.CLEANROOM_MANUFACTORY, null, null, false, null);
        setIndustryOnPlanet("Valhalla", "Skathi", AoDIndustries.CLEANROOM_MANUFACTORY, null, null, false, null);
        setIndustryOnPlanet("Hybrasil", "Culaan", AoDIndustries.CLEANROOM_MANUFACTORY, null, null, false, null);
        setIndustryOnPlanet("Canaan", "Gilead", AoDIndustries.ARTISANAL_FARMING, Industries.FARMING, null, false, null);
        setIndustryOnPlanet("Hybrasil", "Eouchu Bres", AoDIndustries.ARTISANAL_FARMING, Industries.FARMING, null, false, null);
        setIndustryOnPlanet("Zagan", "Mazalot", AoDIndustries.ARTISANAL_FARMING, Industries.FARMING, AoDConditions.SWITCH_RECITIFICATES, false, null);
        setIndustryOnPlanet("Samarra", "Tartessus", AoDIndustries.ARTISANAL_FARMING, Industries.FARMING, null, false, null);
        setIndustryOnPlanet("Corvus", "Jangala", AoDIndustries.SUBSIDISED_FARMING, Industries.FARMING, null, false, null);
        setIndustryOnPlanet("Naraka", "Yama", AoDIndustries.SUBSIDISED_FARMING, Industries.FARMING, AoDConditions.SWITCH_RECITIFICATES, false, null);
        setIndustryOnPlanet("Westernesse", "Ailmar", AoDIndustries.SUBSIDISED_FARMING, Industries.FARMING, AoDConditions.SWITCH_BIOTICS, false, null);
        setIndustryOnPlanet("Kumari Kandam", "Chalcedon", AoDIndustries.SUBSIDISED_FARMING, Industries.FARMING, AoDConditions.SWITCH_RECITIFICATES, false, null);
        setIndustryOnPlanet("Yma", "Qaras", AoDIndustries.SUBSIDISED_FARMING, Industries.FARMING, AoDConditions.SWITCH_BIOTICS, false, null);
        setIndustryOnPlanet("Galatia", "Ancyra", AoDIndustries.SUBSIDISED_FARMING, Industries.FARMING, null, false, null);
        setIndustryOnPlanet("Mayasura", "Mairaath", AoDIndustries.SUBSIDISED_FARMING, Industries.FARMING, AoDConditions.SWITCH_BIOTICS, false, null);
        setIndustryOnPlanet("Corvus", "Asharu", AoDIndustries.SUBSIDISED_FARMING, Industries.FARMING, AoDConditions.SWITCH_RECITIFICATES, false, null);
        setIndustryOnPlanet("Askonia", "Volturn", AoDIndustries.PURIFICATION_CENTER, null, null, false, null);
        setIndustryOnPlanet("Aztlan", "Chicomoztoc", "vault_aotd", null, null, false, null);
        setIndustryOnPlanet("Hybrasil", "Culann", "vault_aotd", null, null, false, null);
        setIndustryOnPlanet("Eos Exodus", "Baetis", AoDIndustries.SUBLIMATION, null, null, true, null);
        setIndustryOnPlanet("Aztlan", "Coatl", AoDIndustries.POLICRYSTALIZATOR, null, null, true, null);
        setIndustryOnPlanet("Canaan", "Gilead", AoDIndustries.BENEFICATION, null, null, true, null);
        setIndustryOnPlanet("Askonia", "Volturn", AoDIndustries.SUBLIMATION, Industries.MINING, null, true, Commodities.GAMMA_CORE);
        setIndustryOnPlanet("Hybrasil", "Culann", AoDIndustries.CASCADE_REPROCESSOR, Industries.REFINING, null, true, Commodities.ALPHA_CORE);
        setIndustryOnPlanet("Hybrasil", "Culann", Industries.HEAVYBATTERIES, Industries.HEAVYBATTERIES, null, true, Commodities.ALPHA_CORE);
        setIndustryOnPlanet("Westernesse", "Athulf", AoDIndustries.BENEFICATION, Industries.MINING, null, true, Commodities.ALPHA_CORE);
        if (Global.getSettings().getModManager().isModEnabled("Imperium")) {
            setIndustryOnPlanet("Thracia", "Byzantium", "vault_aotd", null, null, false, null);
        }

    }

    private static void setVanilaSpecialItemNewIndustries(String specialItemID, String listOfAdditionalIndustries) {
        SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(specialItemID);
        String prevParams = spec.getParams();
        if (prevParams.contains(listOfAdditionalIndustries)) return;
        spec.setParams(prevParams + "," + listOfAdditionalIndustries);
    }
}
