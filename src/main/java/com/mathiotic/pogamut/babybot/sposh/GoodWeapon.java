package com.mathiotic.pogamut.babybot.sposh;

import cz.cuni.amis.pogamut.sposh.executor.ParamsSense;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPref;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefsRange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import com.mathiotic.pogamut.babybot.AttackBotContext;

/**
 * How much ammunition does bot have in its current weapon.
 */
@PrimitiveInfo(name="Have a good weapon", description="Do I have a good weapon for current situation?")
public class GoodWeapon extends ParamsSense<AttackBotContext, Boolean> {

    public GoodWeapon(AttackBotContext ctx) {
        super(ctx, Boolean.class);
    }

    public Boolean query() {
        return ctx.hasGoodWeapons();
    }
 
}
