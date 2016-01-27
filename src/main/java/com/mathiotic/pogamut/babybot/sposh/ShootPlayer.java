package com.mathiotic.pogamut.babybot.sposh;

import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
import cz.cuni.amis.pogamut.sposh.executor.Param;
import cz.cuni.amis.pogamut.sposh.executor.ParamsAction;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.FlagInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import com.mathiotic.pogamut.babybot.AttackBotContext;

/**
 * Shoot nearest visible player.
 *
 * @author 
 */
@PrimitiveInfo(name="Shoot the player", description="Shoot the player.")
public class ShootPlayer extends ParamsAction<AttackBotContext> {

    private int jumpCounter = 0;
    
    public ShootPlayer(AttackBotContext ctx) {
        super(ctx);
    }

    public void init() {                
        //ctx.getConfig().setName("VojtechKopal [ShootPlayer]");
    }

    public ActionResult run() {
        ctx.getShoot().setChangeWeaponCooldown(2000);
        
        if (ctx.getPlayers().canSeeEnemies() && ctx.getPlayers().getNearestVisibleEnemy().getFiring() > 0) {
            jumpCounter++;
            if (jumpCounter % 2 == 0) {
                ctx.getMove().strafeLeft(200);
            } else {
                ctx.getMove().strafeRight(200);
            }
        } 
        
        if (ctx.getPlayers().canSeeEnemies()) {
            return ActionResult.FINISHED;
        }
        
        ctx.getShoot().shoot(ctx.getWeaponPrefs(), ctx.getPlayers().getNearestVisiblePlayer());
        
        return ActionResult.RUNNING;
    }

    public void done() {
        ctx.getShoot().stopShooting();
    }
}
