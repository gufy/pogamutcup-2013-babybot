package com.mathiotic.pogamut.babybot;

import com.mathiotic.pogamut.babybot.AttackBotContext;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.event.WorldObjectUpdatedEvent;
import cz.cuni.amis.pogamut.base3d.worldview.object.event.WorldObjectAppearedEvent;
import cz.cuni.amis.pogamut.base3d.worldview.object.event.WorldObjectDisappearedEvent;
import cz.cuni.amis.pogamut.sposh.ut2004.StateSposhLogicController;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.bot.IUT2004BotController;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.*;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * Simple stub of DeathMatch bot.
 *
 * @see IUT2004BotController Controller for various methods called during
 * construction of bot.
 */
public class AttackBotLogic extends StateSposhLogicController<UT2004Bot, AttackBotContext> {

    private String SPOSH_PLAN_RESOURCE = "sposh/plan/attackbot.lap";
    
    @Override
    protected String getPlan() throws IOException {
        return getPlanFromResource(SPOSH_PLAN_RESOURCE);
    }

    /**
     * Create context that can be accessed in every state primitive.
     *
     * @return new context of this bot.
     */
    @Override
    protected AttackBotContext createContext() {
        return new AttackBotContext(bot);
    }

    /**
     * Initialize command of the bot, called during initial handshake, init can
     * set things like name of bot, its skin, skill, team ect.
     *
     * @see Initialize
     * @return
     */
    @Override
    public Initialize getInitializeCommand() {
        return new Initialize().setName("Babybot").setTeam(AgentInfo.TEAM_BLUE).setSkin("HumanMaleA.NightMaleB");
    }

    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
        super.botInitialized(gameInfo, currentConfig, init);
        log.setLevel(Level.WARNING);
        bot.getLogger().getCategory(SPOSH_LOG_CATEGORY).setLevel(Level.INFO);
        
        // setup weapons
        getContext().getWeaponPrefs().addGeneralPref(ItemType.MINIGUN, true);
        getContext().getWeaponPrefs().addGeneralPref(ItemType.LINK_GUN, false);
        getContext().getWeaponPrefs().addGeneralPref(ItemType.FLAK_CANNON, true);
        getContext().getWeaponPrefs().addGeneralPref(ItemType.ROCKET_LAUNCHER, true);
        
        getContext().getWeaponPrefs().newPrefsRange(300)
            .add(ItemType.FLAK_CANNON, true)
            .add(ItemType.MINIGUN, true)
            .add(ItemType.LINK_GUN, true); // 0-to-CLOSE
        getContext().getWeaponPrefs().newPrefsRange(1000)
            .add(ItemType.MINIGUN, true)
            .add(ItemType.ROCKET_LAUNCHER, true); // CLOSE-to-MEDIUM
    }

    @Override
    public void botKilled(BotKilled event) {
    }
    
    /**
     * Create an {@link ExternalBot} with custom made logic and try to connect
     * to Unreal Server at localhost:3000
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws PogamutException {
        new UT2004BotRunner(AttackBotLogic.class, "AttackBot").setMain(true).setLogLevel(Level.WARNING).startAgents(1);
    }
}
