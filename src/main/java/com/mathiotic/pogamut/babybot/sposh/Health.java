package com.mathiotic.pogamut.babybot.sposh;

import cz.cuni.amis.pogamut.sposh.executor.ParamsSense;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import com.mathiotic.pogamut.babybot.AttackBotContext;

/**
 * How much ammunition does bot have in its current weapon.
 */
@PrimitiveInfo(name="Health", description="How much health does the bot have?")
public class Health extends ParamsSense<AttackBotContext, Integer> {

    public Health(AttackBotContext ctx) {
        super(ctx, Integer.class);
    }

    public Integer query() {
        return ctx.getInfo().getHealth();
    }
}
