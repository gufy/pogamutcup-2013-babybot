package com.mathiotic.pogamut.babybot.sposh;

import cz.cuni.amis.pogamut.sposh.executor.ParamsSense;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import com.mathiotic.pogamut.babybot.AttackBotContext;

/**
 * How much ammunition does bot have in its current weapon.
 */
@PrimitiveInfo(name="Good to fight", description="Check if the bot is good to fight?")
public class GoodToFight extends ParamsSense<AttackBotContext, Boolean> {

    public GoodToFight(AttackBotContext ctx) {
        super(ctx, Boolean.class);
    }

    public Boolean query() {
        return ctx.isReadyForFight();
    }
}
