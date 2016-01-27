package com.mathiotic.pogamut.babybot.sposh;

import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.sposh.engine.VariableContext;
import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import cz.cuni.amis.pogamut.sposh.executor.StateAction;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPref;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.SendMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ItemPickedUp;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.translator.itemdescriptor.AmmoDescriptor;
import cz.cuni.amis.pogamut.ut2004.communication.translator.itemdescriptor.ItemDescriptor;
import com.mathiotic.pogamut.babybot.AttackBotContext;
import cz.cuni.amis.utils.IFilter;
import cz.cuni.amis.utils.collections.MyCollections;
import cz.cuni.amis.utils.flag.FlagListener;
import java.util.*;

/**
 * Action for walking randomly around the world and try to pick various items.
 *
 * @author Vojta
 */
@PrimitiveInfo(name = "Pick items", description = "Go around the map and try to pick various items.")
public class PickItems extends StateAction<AttackBotContext> {

    private Item selectedItem = null;
    private TabooSet taboo = null;
    private String targetType = null;
    private boolean strafeLeft = true;
    
    private HashMap<UnrealId,Integer> failScore = new HashMap<UnrealId, Integer>();

    public PickItems(AttackBotContext ctx) {
        super(ctx);
        this.taboo = new TabooSet(ctx.getBot());
        final AttackBotContext context = ctx;
        final PickItems self = this;
        this.ctx.getNavigation().addStrongNavigationListener(new FlagListener<NavigationState>() {
            @Override
            public void flagChanged(NavigationState t) {
                switch (t) {
                    case STUCK:
                    case PATH_COMPUTATION_FAILED:
                        // preplanuj trasu!
                        context.getLog().info("I'm stuck!");
                        if (self.selectedItem == null) return;
                        
                        int failScore = 0;
                        if (self.failScore.containsKey(self.selectedItem.getId())) {
                            failScore = self.failScore.get(self.selectedItem.getId());
                        } 
                        
                        if (failScore > 1) {
                            self.taboo.add(self.selectedItem, 15);
                            context.getLog().info("Tabooing item #"+self.selectedItem.getId().toString());
                            List<Item> items = self.getBestPickableItem(self.targetType);
                            if (items.size() > 0) {
                                self.selectedItem = items.get(0);
                            }
                            if (self.selectedItem != null) {
                                self.ctx.getNavigation().navigate(self.selectedItem);
                            } 
                        } else {
                            failScore++;
                            self.failScore.put(self.selectedItem.getId(), failScore);
                            self.selectedItem = null;
                            
                        }
                        
                        self.ctx.getLog().info("Stuck fail score is "+failScore);
                        break;
                    default:
                        
                }
            }
        });
    }
    
    @EventListener(eventClass=ItemPickedUp.class)
    public void pickedUp(ItemPickedUp event) {
       ctx.getAct().act(new SendMessage().setGlobal(true).setText("Picked "+event.getType().getName()));
       ctx.getShoot().changeWeapon(ctx.getWeaponPrefs());
       this.selectedItem = null;
       //ctx.getNavigation().stopNavigation(); 
    }

    public List<Item> getBestPickableItem(String type) {
        final AttackBotContext ctx = this.ctx;
        final String itemType = type;
        
        final List<ItemType> ignoredItems = new ArrayList<ItemType>();
        ignoredItems.add(ItemType.MINI_HEALTH_PACK);
        ignoredItems.add(ItemType.ADRENALINE_PACK);
        
        final NavPoint myLoc = ctx.getInfo().getNearestNavPoint();
        Collection spawnedItemsCollection  = ctx.getItems().getSpawnedItems().values();
        spawnedItemsCollection = this.taboo.filter(spawnedItemsCollection);
        List<Item> spawnedItems = MyCollections.asList(spawnedItemsCollection, new IFilter<Item>() {
            @Override
            public boolean isAccepted(Item t) {
                if (ignoredItems.contains(t.getType())) {
                    return false;
                }
                
                ItemDescriptor descriptor = t.getDescriptor();
                if (descriptor.getItemCategory() == ItemType.Category.AMMO) {
                    AmmoDescriptor ammoDescriptor = (AmmoDescriptor)descriptor;
                    if (ammoDescriptor != null) {
                        if (!ctx.getWeaponry().hasWeapon(ctx.getWeaponry().getWeaponForAmmo(t.getType()))) {
                            return false;
                        }
                    }
                }
                
                return ctx.getFwMap().getPath(myLoc, t.getNavPoint()) != null && ctx.getItems().isPickable(t) && ctx.getItems().isPickupSpawned(t);
            }
        });
        
        final List<ItemType> healthItems = new ArrayList<ItemType>(); 
        healthItems.add(ItemType.HEALTH_PACK);
        healthItems.add(ItemType.MINI_HEALTH_PACK);
        healthItems.add(ItemType.SUPER_HEALTH_PACK);
        
        final List<ItemType> weaponTypes = new ArrayList<ItemType>();
        weaponTypes.add(ItemType.MINIGUN);
        weaponTypes.add(ItemType.LINK_GUN);
        weaponTypes.add(ItemType.FLAK_CANNON);
        weaponTypes.add(ItemType.ROCKET_LAUNCHER);
        
        List<WeaponPref> weaponPrefs = ctx.getWeaponPrefs().getWeaponPreferences(-1).getPrefs();
        for (WeaponPref weaponPref : weaponPrefs) {
            weaponTypes.add(weaponPref.getWeapon());
        }
        
        
        final List<ItemType> ammoTypes = new ArrayList<ItemType>();
        for (WeaponPref weaponPref : weaponPrefs) {
            ammoTypes.add(ctx.getWeaponry().getWeaponDescriptor(weaponPref.getWeapon()).getPriAmmoItemType());
        }
        
        final String preferedType = type;
        Collections.sort(spawnedItems, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                int result = 0;
                result = (int)(ctx.getFwMap().getDistance(myLoc, o1.getNavPoint()) - ctx.getFwMap().getDistance(myLoc, o2.getNavPoint()));
                if (result != 0) result /= Math.abs(result);
                
                if (preferedType == "health") {
                    if (healthItems.contains(o1.getType()) && healthItems.contains(o2.getType())) {
                        result = o1.getAmount() - o2.getAmount();
                        result /= Math.abs(result);
                    } else {
                        if (healthItems.contains(o1.getType())) {
                            result = 1;
                        }
                        if (healthItems.contains(o2.getType())) {
                            result = -1;
                        }
                    }
                } else if (preferedType == "weapon") {
                    if (ammoTypes.contains(o1.getType()) && ammoTypes.contains(o2.getType())) {
                        // distance
                        result = (int)(ctx.getFwMap().getDistance(myLoc, o1.getNavPoint()) - ctx.getFwMap().getDistance(myLoc, o2.getNavPoint()));
                        if (result != 0) result /= Math.abs(result);
                    } else {
                        if (ammoTypes.contains(o1.getType())) {
                            result = 1;
                        }
                        if (ammoTypes.contains(o2.getType())) {
                            result = -1;
                        }
                    }
                } else if (preferedType == "ammo") {
                    if (ammoTypes.contains(o1.getType()) && ammoTypes.contains(o2.getType())) {
                        result = (int)(ctx.getFwMap().getDistance(myLoc, o1.getNavPoint()) - ctx.getFwMap().getDistance(myLoc, o2.getNavPoint()));
                        if (result != 0) result /= Math.abs(result);
                    } else {
                        if (ammoTypes.contains(o1.getType())) {
                            result = 1;
                        }
                        if (ammoTypes.contains(o2.getType())) {
                            result = -1;
                        }
                    }
                } else {
                    if (weaponTypes.contains(o1.getType())) result = 1;
                }
                
                return result;//throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        
        return spawnedItems;
    }
    
    private Location getRandomPoint() {
        return MyCollections.getRandom(ctx.getWorld().getAll(NavPoint.class).values()).getLocation();
    }
    
    private Item getRandomItem() {
        Collection spawnedItemsCollection  = ctx.getItems().getSpawnedItems().values();
        spawnedItemsCollection = taboo.filter(spawnedItemsCollection);
        return (Item)MyCollections.getRandom(spawnedItemsCollection);
    }
    
    @Override
    public void init(VariableContext params) {
        
        String type = null;
        if (params.hasVariable("$type")) { type = (String)params.getValue("$type"); }
        List<Item> bestItems = this.getBestPickableItem(type);
        if (!bestItems.isEmpty()) {
            this.selectedItem = bestItems.get(0);
            ctx.getNavigation().navigate(selectedItem);
        } else {
            this.selectedItem = this.getRandomItem();
            ctx.getNavigation().navigate(selectedItem);
        }
        if (bestItems.size() > 1) {
            ctx.getNavigation().setContinueTo(bestItems.get(1));
        }
    }

    @Override
    public ActionResult run(VariableContext params) {
        String type = null;
        if (params.hasVariable("$type")) { type = (String)params.getValue("$type"); }
        
        if (this.selectedItem != null) {
            //ctx.getConfig().setName("VojtechKopal [PickItems:"+this.selectedItem.getType().getName()+"]");
        }
        
        if (ctx.getSensedEnemy() != null) {
            if (ctx.getSensedEnemy().isVisible()) {
                if (type == "ammo") {
                    ctx.getNavigation().setFocus(ctx.getSensedEnemy());
                    ctx.getShoot().shoot(ctx.getWeaponry().getWeapon(ItemType.SHIELD_GUN), false, ctx.getSensedEnemy());
                } else {
                    ctx.getShoot().shoot(ctx.getWeaponPrefs(), ctx.getSensedEnemy());
                } 
                
                this.strafeLeft = !this.strafeLeft;
                int strafeDistance = 300 + (int)Math.floor(100 * Math.random());
                if (this.strafeLeft) {
                    ctx.getMove().strafeLeft(strafeDistance);
                } else {
                    ctx.getMove().strafeRight(strafeDistance);
                }
            } else {
                ctx.getNavigation().setFocus(ctx.getSensedEnemy());
                ctx.getShoot().stopShooting();
            }
        } else {
            ctx.getMove().turnHorizontal(70);
            ctx.getShoot().stopShooting();
        }
        
        if (this.selectedItem != null && ctx.getNavigation().isNavigating()) {
            return ActionResult.RUNNING;
        }
        return ActionResult.FINISHED;
    }

    @Override
    public void done(VariableContext params) {
        this.selectedItem = null;
        ctx.getShoot().stopShooting();
        ctx.getNavigation().stopNavigation();
    }
}
