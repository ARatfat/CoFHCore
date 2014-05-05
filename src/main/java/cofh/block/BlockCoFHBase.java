package cofh.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.block.IBlockDebug;
import cofh.api.block.IBlockInfo;
import cofh.api.block.IDismantleable;
import cofh.api.core.IInitializer;
import cofh.api.energy.IEnergyHandler;
import cofh.api.tileentity.IReconfigurableFacing;
import cofh.api.tileentity.ISecureTile;
import cofh.core.CoFHProps;
import cofh.util.CoreUtils;
import cofh.util.MathHelper;
import cofh.util.StringHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class BlockCoFHBase extends BlockContainer implements IBlockDebug, IBlockInfo, IDismantleable, IInitializer {

	public static int renderPass = 0;
	public static byte secureAccess = 0;
	public static String secureOwner = CoFHProps.DEFAULT_OWNER;

	public BlockCoFHBase(Material material) {

		super(material);
		setStepSound(soundTypeStone);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase living, ItemStack stack) {

		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof ISecureTile && living instanceof ICommandSender) {
			((ISecureTile) tile).setOwnerName(((ICommandSender) living).getCommandSenderName());
		}
		if (tile instanceof IReconfigurableFacing) {
			IReconfigurableFacing reconfig = (IReconfigurableFacing) tile;
			int quadrant = MathHelper.floor(living.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

			if (reconfig.allowYAxisFacing()) {
				quadrant = living.rotationPitch > 60 ? 4 : living.rotationPitch < -60 ? 5 : quadrant;
			}
			switch (quadrant) {
			case 0:
				reconfig.setFacing(2);
				return;
			case 1:
				reconfig.setFacing(5);
				return;
			case 2:
				reconfig.setFacing(3);
				return;
			case 3:
				reconfig.setFacing(4);
				return;
			case 4:
				reconfig.setFacing(1);
				return;
			case 5:
				reconfig.setFacing(0);
				return;
			}
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {

		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof TileCoFHBase) {
			((TileCoFHBase) tile).onNeighborBlockChange();
		}
	}

	@Override
	public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {

		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof TileCoFHBase) {
			((TileCoFHBase) tile).onNeighborTileChange(tileX, tileY, tileZ);
		}
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {

		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof TileCoFHBase && tile.getWorldObj() != null) {
			return ((TileCoFHBase) tile).getLightValue();
		}
		return 0;
	}

	@Override
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {

		TileEntity tile = world.getTileEntity(x, y, z);

		return tile instanceof IReconfigurableFacing ? ((IReconfigurableFacing) tile).rotateBlock() : false;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block bId, int bMeta) {

		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof ISecureTile) {
			secureOwner = ((ISecureTile) tile).getOwnerName();
			secureAccess = (byte) ((ISecureTile) tile).getAccess().ordinal();
		}
		if (tile instanceof IInventory) {
			IInventory inv = (IInventory) tile;
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				CoreUtils.dropItemStackIntoWorldWithVelocity(inv.getStackInSlot(i), world, x, y, z);
			}
		}
		if (tile instanceof TileCoFHBase) {
			TileCoFHBase theTile = (TileCoFHBase) tile;
			theTile.blockBroken();
		}
		super.breakBlock(world, x, y, z, bId, bMeta);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {

		Item item = Item.getItemFromBlock(this);

		if (item == null) {
			return null;
		}
		int bMeta = world.getBlockMetadata(x, y, z);
		ItemStack pickBlock = new ItemStack(item, 1, bMeta);
		pickBlock.setTagCompound(getItemStackTag(world, x, y, z));

		return pickBlock;
	}

	@Override
	public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, int x, int y, int z) {

		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof ISecureTile && !((ISecureTile) tile).canPlayerAccess(player.getCommandSenderName())) {
			return -1;
		}
		return ForgeHooks.blockStrength(this, player, world, x, y, z);
	}

	@Override
	public int damageDropped(int bMeta) {

		return bMeta;
	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int side) {

		TileEntity tile = world.getTileEntity(x, y, z);

		return tile instanceof TileCoFHBase ? ((TileCoFHBase) tile).getComparatorInput(side) : 0;
	}

	@Override
	public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {

		return false;
	}

	@Override
	public boolean isOpaqueCube() {

		return false;
	}

	@Override
	public boolean hasComparatorInputOverride() {

		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir) {

	}

	public NBTTagCompound getItemStackTag(World world, int x, int y, int z) {

		return null;
	}

	public abstract ItemStack dismantleBlock(EntityPlayer player, NBTTagCompound nbt, World world, int x, int y, int z, boolean returnBlock, boolean simulate);

	/* IBlockDebug */
	@Override
	public void debugBlock(IBlockAccess world, int x, int y, int z, ForgeDirection side, EntityPlayer player) {

		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof ITileDebug) {
			((ITileDebug) tile).debugTile(side, player);
		}
	}

	/* IBlockInfo */
	@Override
	public void getBlockInfo(IBlockAccess world, int x, int y, int z, ForgeDirection side, EntityPlayer player, List<String> info, boolean debug) {

		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof ITileInfo) {
			((ITileInfo) tile).getTileInfo(info, side, player, debug);
		} else {
			if (tile instanceof IEnergyHandler) {
				IEnergyHandler eHandler = (IEnergyHandler) tile;
				info.add(StringHelper.localize("info.cofh.energy") + ": " + eHandler.getEnergyStored(side) + "/" + eHandler.getMaxEnergyStored(side) + " RF.");
			}
		}
	}

	/* IDismantleable */
	@Override
	public ItemStack dismantleBlock(EntityPlayer player, World world, int x, int y, int z, boolean returnBlock) {

		return dismantleBlock(player, getItemStackTag(world, x, y, z), world, x, y, z, returnBlock, false);
	}

	@Override
	public boolean canDismantle(EntityPlayer player, World world, int x, int y, int z) {

		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof ISecureTile) {
			return ((ISecureTile) tile).canPlayerAccess(player.getCommandSenderName());
		} else if (tile instanceof TileCoFHBase) {
			return ((TileCoFHBase) tile).canPlayerDismantle(player);
		}
		return true;
	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		return true;
	}

}
