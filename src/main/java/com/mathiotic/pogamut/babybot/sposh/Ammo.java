package com.mathiotic.pogamut.babybot.sposh;

import cz.cuni.amis.pogamut.sposh.executor.ParamsSense;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import com.mathiotic.pogamut.babybot.AttackBotContext;

/**
 * How much ammunition does bot have in its current weapon.
 */
@PrimitiveInfo(name="Ammo", description="How much ammunition does current weapon have?")
public class Ammo extends ParamsSense<AttackBotContext, Integer> {

    public Ammo(AttackBotContext ctx) {
        super(ctx, Integer.class);
    }

    public Integer query() {
        int ammo = (int)(100*(ctx.getWeaponry().getCurrentAmmo() * 1.0 / ctx.getWeaponry().getCurrentWeapon().getDescriptor().getPriMaxAmount()));
        return ammo;
    }
}
