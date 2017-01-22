package cofh.core.item.tool;

import cofh.api.core.IModelRegister;
import cofh.core.render.CoFHFontRenderer;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.SecurityHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemSwordBase extends ItemSword implements IModelRegister {

	protected TMap<Integer, ToolEntry> itemMap = new THashMap<Integer, ToolEntry>();
	protected ArrayList<Integer> itemList = new ArrayList<Integer>(); // This is actually more memory efficient than a LinkedHashMap

	protected String name;
	protected String modName;
	protected float baseAttackDamage = 3.0F;
	protected float baseAttackSpeed = -2.4F;
	protected boolean showInCreative = true;

	public ItemSwordBase() {

		this("cofh");
	}

	public ItemSwordBase(String modName) {

		super(ToolMaterial.STONE);
		this.modName = modName;
		setMaxStackSize(1);
		setHasSubtypes(true);
	}

	public ItemSwordBase setShowInCreative(boolean showInCreative) {

		this.showInCreative = showInCreative;
		return this;
	}

	protected float getAttackDamage(ItemStack stack) {

		return baseAttackDamage + getToolMaterial(stack).getDamageVsEntity();
	}

	protected float getAttackSpeed(ItemStack stack) {

		return baseAttackSpeed;
	}

	protected String getRepairIngot(ItemStack stack) {

		int i = ItemHelper.getItemDamage(stack);
		if (!itemMap.containsKey(Integer.valueOf(i))) {
			return "cobblestone";
		}
		return itemMap.get(ItemHelper.getItemDamage(stack)).ingot;
	}

	protected Item.ToolMaterial getToolMaterial(ItemStack stack) {

		int i = ItemHelper.getItemDamage(stack);
		if (!itemMap.containsKey(Integer.valueOf(i))) {
			return ToolMaterial.WOOD;
		}
		return itemMap.get(ItemHelper.getItemDamage(stack)).material;
	}

	/* ADD ITEMS */
	public ItemStack addItem(int number, ToolEntry entry) {

		if (itemMap.containsKey(Integer.valueOf(number))) {
			return null;
		}
		itemMap.put(Integer.valueOf(number), entry);
		itemList.add(Integer.valueOf(number));

		ItemStack stack = new ItemStack(this, 1, number);
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("Damage", 0);
		return stack;
	}

	public ItemStack addItem(int number, String name, Item.ToolMaterial material, String ingot, EnumRarity rarity) {

		return addItem(number, new ToolEntry(name, material, ingot, rarity));
	}

	public ItemStack addItem(int number, String name, Item.ToolMaterial material, String ingot) {

		return addItem(number, new ToolEntry(name, material, ingot));
	}

	/* STANDARD METHODS */
	@Override
	@SideOnly (Side.CLIENT)
	public void getSubItems(@Nonnull Item item, CreativeTabs tab, List<ItemStack> list) {

		if (!showInCreative) {
			return;
		}
		for (int i = 0; i < itemList.size(); i++) {
			ItemStack stack = new ItemStack(item, 1, itemList.get(i));
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("Damage", 0);

			list.add(stack);
		}
	}

	@Override
	public void setDamage(ItemStack stack, int damage) {

		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("Damage", 0);
		}
		if (damage < 0) {
			damage = 0;
		}
		stack.getTagCompound().setInteger("Damage", damage);
	}

	@Override
	public boolean getIsRepairable(ItemStack itemToRepair, ItemStack stack) {

		return ItemHelper.isOreNameEqual(stack, getRepairIngot(stack));
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack) {

		return SecurityHelper.isSecure(stack);
	}

	@Override
	public boolean isDamaged(ItemStack stack) {

		return getDamage(stack) > 0;
	}

	@Override
	public boolean isItemTool(ItemStack stack) {

		return true;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {

		return !ItemHelper.itemsEqualWithMetadata(oldStack, newStack);
	}

	@Override
	public int getDamage(ItemStack stack) {

		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("Damage", 0);
		}
		return stack.getTagCompound().getInteger("Damage");
	}

	@Override
	public int getItemEnchantability(ItemStack stack) {

		int i = ItemHelper.getItemDamage(stack);
		if (!itemMap.containsKey(Integer.valueOf(i))) {
			return 0;
		}
		return itemMap.get(ItemHelper.getItemDamage(stack)).material.getEnchantability();
	}

	@Override
	public int getMaxDamage(ItemStack stack) {

		return getToolMaterial(stack).getMaxUses();
	}

	@Override
	public int getMetadata(ItemStack stack) {

		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("Damage", 0);
		}
		return stack.getTagCompound().getInteger("Damage");
	}

	@Override
	public Entity createEntity(World world, Entity location, ItemStack stack) {

		if (SecurityHelper.isSecure(stack)) {
			location.invulnerable = true;
			location.isImmuneToFire = true;
			((EntityItem) location).lifespan = Integer.MAX_VALUE;
		}
		return null;
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {

		Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();

		if (slot == EntityEquipmentSlot.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", getAttackDamage(stack), 0));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", getAttackSpeed(stack), 0));
		}

		return multimap;
	}

	@Override
	@SideOnly (Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack) {

		return CoFHFontRenderer.loadFontRendererStack(stack);
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		int i = ItemHelper.getItemDamage(stack);
		if (!itemMap.containsKey(Integer.valueOf(i))) {
			return EnumRarity.COMMON;
		}
		return itemMap.get(ItemHelper.getItemDamage(stack)).rarity;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {

		int i = ItemHelper.getItemDamage(stack);
		if (!itemMap.containsKey(Integer.valueOf(i))) {
			return "item.invalid";
		}
		ToolEntry item = itemMap.get(i);
		return getUnlocalizedName() + "." + item.name;
	}

	@Override
	public String getToolMaterialName() {

		return "";
	}

	@Override
	public Item setUnlocalizedName(String name) {

		GameRegistry.register(setRegistryName(name));
		this.name = name;
		name = modName + "." + name;
		return super.setUnlocalizedName(name);
	}

	public Item setUnlocalizedName(String name, String registrationName) {

		GameRegistry.register(setRegistryName(registrationName));
		this.name = name;
		name = modName + "." + name;
		return super.setUnlocalizedName(name);
	}

	/* IModelRegister */
	@Override
	@SideOnly (Side.CLIENT)
	public void registerModels() {

		for (Map.Entry<Integer, ToolEntry> entry : itemMap.entrySet()) {
			ModelLoader.setCustomModelResourceLocation(this, entry.getKey(), new ModelResourceLocation(getRegistryName(), "type=" + entry.getValue().name));
		}
	}

	/* ITEM ENTRY */
	public class ToolEntry {

		public String name;
		public Item.ToolMaterial material;
		public String ingot;
		public EnumRarity rarity;

		ToolEntry(String name, Item.ToolMaterial material, String ingot, EnumRarity rarity) {

			this.name = name;
			this.material = material;
			this.ingot = ingot;
			this.rarity = rarity;
		}

		ToolEntry(String name, Item.ToolMaterial material, String ingot) {

			this(name, material, ingot, EnumRarity.COMMON);
		}
	}

}
