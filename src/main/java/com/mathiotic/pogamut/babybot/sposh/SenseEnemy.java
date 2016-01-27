package com.mathiotic.pogamut.babybot.sposh;

import cz.cuni.amis.pogamut.sposh.engine.VariableContext;
import cz.cuni.amis.pogamut.sposh.executor.ParamsSense;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import com.mathiotic.pogamut.babybot.AttackBotContext;

/**
 * This sense will determine, if the bot can see a player.
 *
 * @author 
 */
@PrimitiveInfo(name = "Can sense enemy", description = "Do I see a player, or remember him?")
public class SenseEnemy extends ParamsSense<AttackBotContext, Boolean> {
    
    public SenseEnemy(AttackBotContext ctx) {
        super(ctx, Boolean.class);
    }

    public Boolean query() {
        if (this.getCtx().getPlayers().canSeePlayers() && ctx.getPlayers().getNearestVisibleEnemy() != null) {      
            ctx.setSensedEnemy(ctx.getPlayers().getNearestVisibleEnemy());
            return true;
        }
        
        if (ctx.getSensedEnemy() != null) {
            return true;
        }
        return false;
    }

    
}
