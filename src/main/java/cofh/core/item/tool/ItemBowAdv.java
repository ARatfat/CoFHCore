package cofh.core.item.tool;

import cofh.core.enchantment.CoFHEnchantment;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.ServerHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityArrow.PickupStatus;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBowAdv extends ItemBow {

    protected ToolMaterial toolMaterial;

    public String repairIngot = "";
    protected boolean showInCreative = true;
    public float arrowSpeedMultiplier = 2.0F;
    public float arrowDamageMultiplier = 1.25F;

    public ItemBowAdv(Item.ToolMaterial toolMaterial) {

        super();
        this.toolMaterial = toolMaterial;
        setMaxDamage(toolMaterial.getMaxUses());
    }

    public int cofh_canEnchantApply(ItemStack stack, Enchantment ench) {

        if (ench.getName() == Enchantments.LOOTING.getName()) {
            return 1;
        }
        if (ench.type == EnumEnchantmentType.BOW) {
            return 1;
        }
        return -1;
    }

    public ItemBowAdv setRepairIngot(String repairIngot) {

        this.repairIngot = repairIngot;
        return this;
    }

    public ItemBowAdv setArrowSpeed(float multiplier) {

        this.arrowSpeedMultiplier = multiplier;
        return this;
    }

    public ItemBowAdv setArrowDamage(float multiplier) {

        arrowDamageMultiplier = multiplier;
        return this;
    }

    public ItemBowAdv setShowInCreative(boolean showInCreative) {

        this.showInCreative = showInCreative;
        return this;
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List list) {

        if (showInCreative) {
            list.add(new ItemStack(item, 1, 0));
        }
    }

    @Override
    public int getItemEnchantability() {

        return toolMaterial.getEnchantability();
    }

    @Override
    public boolean getIsRepairable(ItemStack itemToRepair, ItemStack stack) {

        return ItemHelper.isOreNameEqual(stack, repairIngot);
    }

    @Override
    public boolean isItemTool(ItemStack stack) {

        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {

        boolean hasAmmo = findAmmo(player) != null;

        ActionResult<ItemStack> ret = ForgeEventFactory.onArrowNock(stack, world, player, hand, hasAmmo);
        if (ret != null){
            return ret;
        }

        if (player.capabilities.isCreativeMode || hasAmmo || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0) {
            ret = new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
            player.setActiveHand(hand);
        } else {
            ret = new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }
        return ret;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entityLiving, int itemUse) {


        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLiving;

            boolean creativeOrInfinity = player.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
            ItemStack ammo = findAmmo(player);
            int draw = this.getMaxItemUseDuration(stack) - itemUse;

            ArrowLooseEvent event = new ArrowLooseEvent(player, stack, world, draw, ammo != null || creativeOrInfinity);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                return;
            }
            draw = event.getCharge();



            if (creativeOrInfinity || ammo != null) {
                float drawStrength = getArrowVelocity(draw);
                if (drawStrength < 0.1F) {
                    return;
                }
                int enchantPower = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
                int enchantKnockback = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);
                int enchantFire = EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack);
                int enchantMultishot = EnchantmentHelper.getEnchantmentLevel(CoFHEnchantment.multishot, stack);

                ItemArrow itemArrow = (ItemArrow) (ammo.getItem() instanceof ItemArrow ? ammo.getItem() : Items.ARROW);
                EntityArrow arrow = itemArrow.createArrow(world, ammo, player);
                arrow.setAim(player, player.rotationPitch, player.rotationYaw, 0.0F, drawStrength * arrowSpeedMultiplier, 1.0F);
                double damage = arrow.getDamage() * arrowDamageMultiplier;
                arrow.setDamage(damage);

                if (drawStrength == 1.0F) {
                    arrow.setIsCritical(true);
                }
                if (enchantPower > 0) {
                    arrow.setDamage(damage + enchantPower * 0.5D + 0.5D);
                }
                if (enchantKnockback > 0) {
                    arrow.setKnockbackStrength(enchantKnockback);
                }
                if (enchantFire > 0) {
                    arrow.setFire(100);
                }
                if (creativeOrInfinity) {
                    arrow.pickupStatus = PickupStatus.CREATIVE_ONLY;
                } else {
                    --ammo.stackSize;
                    if (ammo.stackSize == 0){
                        player.inventory.deleteStack(ammo);
                    }
                }
                world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + drawStrength * 0.5F);

                if (ServerHelper.isServerWorld(world)) {
                    world.spawnEntityInWorld(arrow);
                }
                for (int i = 0; i < enchantMultishot; i++) {
                    arrow = itemArrow.createArrow(world, ammo, player);
                    arrow.setAim(player, player.rotationPitch, player.rotationYaw, 0.0F, drawStrength * arrowSpeedMultiplier, 1.0F);
                    arrow.setThrowableHeading(arrow.motionX, arrow.motionY, arrow.motionZ, 1.5f * drawStrength * arrowSpeedMultiplier, 3.0F);

                    arrow.setDamage(damage);

                    if (drawStrength == 1.0F) {
                        arrow.setIsCritical(true);
                    }
                    if (enchantPower > 0) {
                        arrow.setDamage(damage + enchantPower * 0.5D + 0.5D);
                    }
                    if (enchantKnockback > 0) {
                        arrow.setKnockbackStrength(enchantKnockback);
                    }
                    if (enchantFire > 0) {
                        arrow.setFire(100);
                    }
                    arrow.pickupStatus = PickupStatus.CREATIVE_ONLY;

                    if (ServerHelper.isServerWorld(world)) {
                        world.spawnEntityInWorld(arrow);
                    }
                }
                if (!player.capabilities.isCreativeMode) {
                    stack.damageItem(1, player);
                }
            }
        }
    }

    //TODO Do we want other arrow to be fired or just standard.
    private ItemStack findAmmo(EntityPlayer player) {
        if (this.isArrow(player.getHeldItem(EnumHand.OFF_HAND))) {
            return player.getHeldItem(EnumHand.OFF_HAND);
        } else if (this.isArrow(player.getHeldItem(EnumHand.MAIN_HAND))) {
            return player.getHeldItem(EnumHand.MAIN_HAND);
        } else {
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                ItemStack itemstack = player.inventory.getStackInSlot(i);

                if (this.isArrow(itemstack)) {
                    return itemstack;
                }
            }

            return null;
        }
    }

}
