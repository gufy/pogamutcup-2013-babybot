package com.mathiotic.pogamut.babybot.sposh;

import cz.cuni.amis.pogamut.sposh.executor.ParamsSense;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import com.mathiotic.pogamut.babybot.AttackBotContext;

/**
 * How much ammunition does bot have in its current weapon.
 */
@PrimitiveInfo(name="Distance to enemy", description="What is the distance to enemy?")
public class Distance extends ParamsSense<AttackBotContext, Integer> {

    public Distance(AttackBotContext ctx) {
        super(ctx, Integer.class);
    }

    public Integer query() {
        double distance = Double.MAX_VALUE;
        Player enemy = ctx.getPlayers().getNearestEnemy(3000);
        
        if (enemy != null) {
            distance = ctx.getInfo().getDistance(enemy);
        }
        
        return (int)distance;
    }
}
