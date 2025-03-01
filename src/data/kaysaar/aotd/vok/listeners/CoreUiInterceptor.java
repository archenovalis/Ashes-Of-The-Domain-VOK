package data.kaysaar.aotd.vok.listeners;

import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.CoreUITabListener;
import data.kaysaar.aotd.vok.scripts.CoreUITracker;

public class CoreUiInterceptor implements CoreUITabListener {
    @Override
    public void reportAboutToOpenCoreTab(CoreUITabId tab, Object param) {
        if(param instanceof  String){
            String s = (String) param;
            if(s.equals("income_report")){
                CoreUITracker.setMemFlag("income");
            }
        }
        if(param instanceof MarketAPI){
            CoreUITracker.setMemFlag("colonies");
        }
        CoreUITracker.sendSignalToOpenCore = true;
    }
}
