package com.mathiotic.pogamut.babybot.sposh;

import cz.cuni.amis.pogamut.sposh.executor.ParamsSense;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import com.mathiotic.pogamut.babybot.AttackBotContext;

/**
 * How much ammunition does bot have in its current weapon.
 */
@PrimitiveInfo(name="Is shooting", description="Am I shooting now?")
public class IsShooting extends ParamsSense<AttackBotContext, Boolean> {

    public IsShooting(AttackBotContext ctx) {
        super(ctx, Boolean.class);
    }

    public Boolean query() {
        return ctx.isShooting();
    }
}
