package com.mathiotic.pogamut.babybot;

import cz.cuni.amis.pogamut.sposh.context.UT2004Context;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPref;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefsRange;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004PathAutoFixer;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004DistanceStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004PositionStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004TimeStuckDetector;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.IncomingProjectile;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.utils.Cooldown;
import java.util.HashSet;
import java.util.Set;

/**
 * Context for the simple DeathMatch bot.
 */
public class AttackBotContext extends UT2004Context<UT2004Bot> {
    
    private Boolean shooting = false;
    private String name = "VojtechKopal";
    private Player sensedEnemy = null;
    private Cooldown sensedEnemyCooldown = new Cooldown(10000);
    
    /**
     * AutoFixer monitors movement of agent and when it detects faulty
     * connection in the waypoints, it removes them from navigation logic.
     */
    UT2004PathAutoFixer autoFixer;

    public AttackBotContext(UT2004Bot bot) {
        super("AttackBotContext", bot);
        // IMPORTANT: Various modules of context must be initialized.
        initialize();

        // INITIALIZE CUSTOM MODULES

        pathExecutor.addStuckDetector(new UT2004TimeStuckDetector(bot, 3000, 10000)); // if the bot does not move for 3 seconds, consider that it is stuck / if bot waits for more than 10 seconds, consider that is is stuck
        pathExecutor.addStuckDetector(new UT2004PositionStuckDetector(bot));          // watch over the position history of the bot, if the bot does not move sufficiently enough, consider that it is stuck
        pathExecutor.addStuckDetector(new UT2004DistanceStuckDetector(bot));          // watch over distances to target

        // AutoFixer tries to detect faulty connections between waypoints 
        // in the environment and remove them from navigation planners. 
        // AttackBot is used mostly on small maps, e.g. CTF-1on1-Joust, where 
        // every connection is necessary and its loss will break the navigation.

        //autoFixer = new UT2004PathAutoFixer(bot, pathExecutor, fwMap, navBuilder);
    }
   
    public boolean isShooting() { 
        return this.shooting;
    }
    
    public void setIsShooting() {
        this.shooting = true;
    }
    
    public Player getSensedEnemy() {
        if (this.sensedEnemyCooldown.isHot()) {
            return this.sensedEnemy;
        }
        return null;
    }
    
    public void setSensedEnemy(Player enemy) {
        this.sensedEnemyCooldown.use();
        this.sensedEnemy = enemy;
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean hasGoodWeapons() {
        double distance = -1;
        boolean hasGoodWeapon = false;
        
        if (getWeaponry().getWeapons().values().size() > 3) hasGoodWeapon = true;
        
        return hasGoodWeapon;
    }
    
    public boolean isReadyForFight() {
        boolean result = true;
        
        int ammo = (int)(100*(getWeaponry().getCurrentAmmo() * 1.0 / getWeaponry().getCurrentWeapon().getDescriptor().getPriMaxAmount()));
        
        if (ammo <= 20) result = false;
        if (this.getInfo().getHealth() <= 50) result = false;
        if (!this.hasGoodWeapons()) result = false;
        
        return result;
    }
}
