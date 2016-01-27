package com.mathiotic.pogamut.babybot.sposh;

import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.event.WorldObjectUpdatedEvent;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.base3d.worldview.object.event.WorldObjectAppearedEvent;
import cz.cuni.amis.pogamut.base3d.worldview.object.event.WorldObjectDisappearedEvent;
import cz.cuni.amis.pogamut.sposh.engine.VariableContext;
import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import cz.cuni.amis.pogamut.sposh.executor.StateAction;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weapon;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.IncomingProjectile;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import com.mathiotic.pogamut.babybot.AttackBotContext;
import cz.cuni.amis.utils.Cooldown;
import cz.cuni.amis.utils.collections.MyCollections;
import java.util.*;

/**
 * 
 */
@PrimitiveInfo(name = "Chase sensed enemy", description = "Chase sensed enemy and shoot.")
public class ChaseSensedEnemyAndShoot extends StateAction<AttackBotContext> {

    private int counter = 0;
    private boolean strafeLeft = true;
    private Cooldown isEnemyShooting = new Cooldown(2000);
    
    public ChaseSensedEnemyAndShoot(AttackBotContext ctx) {
        super(ctx);
    }
    
    @Override
    public void init(VariableContext params) {      
        //ctx.getConfig().setName("VojtechKopal [RunToPlayerAndShoot]");
    }
    
    /**
     * "AssaultRifle", "ShieldGun", "FlakCannon", "BioRifle", "ShockRifle", "LinkGun", "SniperRifle", "RocketLauncher", "Minigun", "LightingGun", "Translocator"
     * 
     * @param weaponString
     * @return 
     */
    public ItemType getItemTypeFromWeaponString(String weaponString) {
        ItemType type = null;
        
        if ("AssaultRifle".equals(weaponString)) {
            type = ItemType.ASSAULT_RIFLE;
        } else if ("ShieldGun".equals(weaponString)) {
            type = ItemType.SHIELD_GUN;
        } else if ("FlakCannon".equals(weaponString)) {
            type = ItemType.FLAK_CANNON;
        } else if ("BioRifle".equals(weaponString)) {
            type = ItemType.BIO_RIFLE;
        } else if ("ShockRifle".equals(weaponString)) {
            type = ItemType.SHOCK_RIFLE;
        } else if ("LinkGun".equals(weaponString)) {
            type = ItemType.LINK_GUN;
        } else if ("SniperRifle".equals(weaponString)) {
            type = ItemType.SNIPER_RIFLE;
        } else if ("RocketLauncher".equals(weaponString)) {
            type = ItemType.ROCKET_LAUNCHER;
        } else if ("Minigun".equals(weaponString)) {
            type = ItemType.MINIGUN;
        } else if ("LightingGun".equals(weaponString)) {
            type = ItemType.LIGHTNING_GUN;
        } else if ("Translocator".equals(weaponString)) {
            type = ItemType.TRANSLOCATOR;
        }
        
        return type;
    } 
    
    @Override
    public ActionResult run(VariableContext params) {
        this.counter++;
        
        if (ctx.getSensedEnemy() == null) {
            return ActionResult.FINISHED;
        }
        
        if (ctx.getSensedEnemy().getFiring() > 0) {
            isEnemyShooting.use();
        }
        
        if (ctx.getSensedEnemy() != null) {
            ctx.getMove().turnTo(ctx.getSensedEnemy());
            ctx.getNavigation().setFocus(ctx.getSensedEnemy());
        }
        
        if (isEnemyShooting.isCool() && ctx.getSensedEnemy() != null && ctx.getSensedEnemy().isVisible()) {
            NavPoint nearestCover = ctx.getVisibility().getNearestCoverNavPointFrom(ctx.getSensedEnemy());

            if (ctx.getInfo().getDistance(nearestCover) < 1000) {
                ctx.getNavigation().navigate(nearestCover);
                ctx.getNavigation().setContinueTo(ctx.getInfo().getLocation());
            } else {
                this.strafeLeft = !this.strafeLeft;
                int strafeDistance = 300 + (int)Math.floor(100 * Math.random());
                if (this.strafeLeft) {
                    ctx.getMove().strafeLeft(strafeDistance);
                } else {
                    ctx.getMove().strafeRight(strafeDistance);
                }
            }
        }
        
        if (ctx.getSensedEnemy() != null && ctx.getSensedEnemy().isVisible()) {
            ctx.setSensedEnemy(ctx.getSensedEnemy());
            ctx.getShoot().shoot(ctx.getWeaponPrefs(), ctx.getSensedEnemy());
        } else {
            ctx.getShoot().stopShooting();
            if (ctx.getWeaponry().hasLoadedWeapon()) {
                NavPoint nearestUncover = DistanceUtils.getNearest(ctx.getVisibility().getVisibleNavPointsFrom(ctx.getSensedEnemy()), ctx.getInfo().getLocation());
                ctx.getNavigation().navigate(nearestUncover);
            }
        }
        
        if (!ctx.getNavigation().isNavigating() || (ctx.getSensedEnemy() != null && !ctx.getSensedEnemy().isVisible())) {
            ctx.getNavigation().navigate(ctx.getSensedEnemy());
        }
        
        return ActionResult.RUNNING;
    }

    @Override
    public void done(VariableContext params) {
        ctx.getShoot().stopShooting();
        ctx.getNavigation().stopNavigation();
    }
}
