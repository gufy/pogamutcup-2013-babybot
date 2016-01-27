package com.mathiotic.pogamut.babybot.sposh;

import cz.cuni.amis.pogamut.sposh.engine.VariableContext;
import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import cz.cuni.amis.pogamut.sposh.executor.StateAction;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import com.mathiotic.pogamut.babybot.AttackBotContext;
import cz.cuni.amis.utils.collections.MyCollections;

/**
 * Action for walking randomly around the world and try to pick various items.
 *
 * @author Honza
 */
@PrimitiveInfo(name = "Run randomly", description = "Go around the map randomly.")
public class RunRandomly extends StateAction<AttackBotContext> {

    public RunRandomly(AttackBotContext ctx) {
        super(ctx);
    }

    @Override
    public void init(VariableContext params) {        
        NavPoint nav = MyCollections.getRandom(ctx.getWorld().getAll(NavPoint.class).values());
        ctx.getNavigation().navigate(nav);
        //ctx.getConfig().setName(ctx.getName()+" [RunRandomly]");
    }

    @Override
    public ActionResult run(VariableContext params) {
        
        if (ctx.getSensedEnemy() != null) {
            if (ctx.getSensedEnemy().isVisible()) {
                ctx.getShoot().shoot(ctx.getWeaponPrefs(), ctx.getSensedEnemy());
            } else {
                ctx.getNavigation().setFocus(ctx.getSensedEnemy());
                ctx.getShoot().stopShooting();
            }
        } else {
            ctx.getMove().turnHorizontal(70);
            ctx.getShoot().stopShooting();
        }
        
        if (ctx.getNavigation().isNavigating()) {
            return ActionResult.RUNNING;
        }
        return ActionResult.FINISHED;
    }

    @Override
    public void done(VariableContext params) {
        ctx.getNavigation().stopNavigation();
    }
}
