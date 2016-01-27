package com.mathiotic.pogamut.babybot.sposh;

import cz.cuni.amis.pogamut.sposh.engine.VariableContext;
import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import cz.cuni.amis.pogamut.sposh.executor.StateAction;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.visibility.model.BitMatrix;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import com.mathiotic.pogamut.babybot.AttackBotContext;
import cz.cuni.amis.utils.collections.MyCollections;

/**
 * Action for walking randomly around the world and try to pick various items.
 *
 * @author Honza
 */
@PrimitiveInfo(name = "Run and hide", description = "Run and hide.")
public class RunAndHide extends StateAction<AttackBotContext> {

    private NavPoint hiddenPlace = null;

    public RunAndHide(AttackBotContext ctx) {
        super(ctx);
    }

    @Override
    public void init(VariableContext params) {        
        Player enemy = ctx.getPlayers().getNearestVisibleEnemy();
        this.hiddenPlace = ctx.getVisibility().getNearestCoverNavPointFrom(enemy);
        ctx.getNavigation().navigate(this.hiddenPlace);
        //ctx.getConfig().setName("VojtechKopal [RunAndHide]");
    }

    @Override
    public ActionResult run(VariableContext params) {
        Player enemy = ctx.getPlayers().getNearestVisibleEnemy();
        if (ctx.getPlayers().canSeeEnemies()) {
            ctx.getShoot().shoot(enemy);
        } else {
            ctx.getShoot().stopShooting();
            return ActionResult.FINISHED;
        }
        if (!ctx.getPlayers().canSeeEnemies() && !ctx.getVisibility().isVisible(ctx.getInfo().getLocation(), enemy.getLocation())) {
            ctx.getLog().info("I'm hidden now!");
            return ActionResult.FINISHED;
        } else {
            ctx.getLog().info("I want to hide.");
        }
        if (ctx.getNavigation().isNavigating()) {
            if (ctx.getVisibility().getNearestCoverNavPointFrom(enemy) != this.hiddenPlace) {
                this.hiddenPlace = ctx.getVisibility().getNearestCoverNavPointFrom(enemy);
                ctx.getNavigation().navigate(this.hiddenPlace);
            }
            return ActionResult.RUNNING;
        }
        return ActionResult.FINISHED;
    }

    @Override
    public void done(VariableContext params) {
        ctx.getLog().info("Hidden.");
        ctx.getNavigation().stopNavigation();
        ctx.getShoot().stopShooting();
    }
}
