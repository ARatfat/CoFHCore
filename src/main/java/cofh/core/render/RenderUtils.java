package cofh.core.render;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.lib.vec.uv.UV;
import cofh.lib.render.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class RenderUtils {

    public static class ScaledIconTransformation extends IconTransformation {

        double su = 0.0F;
        double sv = 0.0F;

        public ScaledIconTransformation(TextureAtlasSprite icon) {

            super(icon);
        }

        public ScaledIconTransformation(TextureAtlasSprite icon, double scaleu, double scalev) {

            super(icon);

            su = scaleu;
            sv = scalev;
        }

        @Override
        public void apply(UV texcoord) {

            texcoord.u = icon.getInterpolatedU(texcoord.u % 2 * 16) + su * (icon.getMaxU() - icon.getMinU());
            texcoord.v = icon.getInterpolatedV(texcoord.v % 2 * 16) + sv * (icon.getMaxV() - icon.getMinV());
        }
    }

    //public static final RenderBlocks renderBlocks = new RenderBlocks();

    public static float[][] angleBaseYNeg = new float[6][3];
    public static float[][] angleBaseYPos = new float[6][3];
    public static float[][] angleBaseXPos = new float[6][3];
    public static final float factor = 1F / 16F;

    public static final int[] facingAngle = { 0, 0, 180, 0, 90, -90 };

    public static ScaledIconTransformation[] renderTransformations = new ScaledIconTransformation[4];

    static {
        renderTransformations[0] = new ScaledIconTransformation(TextureUtils.getBlockTexture("stone"));
        renderTransformations[1] = new ScaledIconTransformation(TextureUtils.getBlockTexture("stone"), -1F, 0F);
        renderTransformations[2] = new ScaledIconTransformation(TextureUtils.getBlockTexture("stone"), 0F, -1F);
        renderTransformations[3] = new ScaledIconTransformation(TextureUtils.getBlockTexture("stone"), -1F, -1F);
    }

    public static Vector3 renderVector = new Vector3();

    @Deprecated
    //Due to the static nature of these transforms, we cant use them.
    //Parts of the model pipeline run on other threads causing thread leaks.
    public static ScaledIconTransformation getIconTransformation(TextureAtlasSprite icon) {

        if (icon != null) {
            renderTransformations[0].icon = icon;
        }
        return renderTransformations[0];
    }

    // public static ScaledIconTransformation getIconTransformation(Icon icon, int scaleType) {
    //
    // renderTransformations[scaleType].icon = icon;
    // return renderTransformations[scaleType];
    // }

    @Deprecated
    //Due to the static nature of this, we cant use it.
    //Parts of the model pipeline run on other threads causing thread leaks.
    public static Vector3 getRenderVector(double x, double y, double z) {

        renderVector.x = x;
        renderVector.y = y;
        renderVector.z = z;

        return renderVector;
    }

    static {
        float pi = (float) Math.PI;
        angleBaseYNeg[0][2] = pi;
        angleBaseYNeg[2][0] = -pi / 2;
        angleBaseYNeg[3][0] = pi / 2;
        angleBaseYNeg[4][2] = pi / 2;
        angleBaseYNeg[5][2] = -pi / 2;

        angleBaseYPos[1][2] = pi;
        angleBaseYPos[2][0] = pi / 2;
        angleBaseYPos[3][0] = -pi / 2;
        angleBaseYPos[4][2] = -pi / 2;
        angleBaseYPos[5][2] = pi / 2;

        angleBaseXPos[0][0] = -pi / 2;
        angleBaseXPos[1][0] = pi / 2;
        angleBaseXPos[2][1] = pi;
        angleBaseXPos[4][1] = -pi / 2;
        angleBaseXPos[5][1] = pi / 2;
    }

    public static int getFluidRenderColor(FluidStack fluid) {

        return fluid.getFluid().getColor(fluid);
    }

    public static void setFluidRenderColor(FluidStack fluid) {

        CCRenderState.instance().baseColour = (0xFF | (fluid.getFluid().getColor(fluid) << 8));
    }

    public static void preItemRender() {

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.pullLightmap();
        //CCRenderState.useNormals = true;
    }

    public static void postItemRender() {

        //CCRenderState.useNormals = false;

        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
    }

    public static void preWorldRender(IBlockAccess world, BlockPos pos) {
        CCRenderState ccrs = CCRenderState.instance();

        ccrs.reset();
        ccrs.colour = 0xFFFFFFFF;
        ccrs.setBrightness(world, pos);
    }

	/*public static void renderMask(IIcon maskIcon, IIcon subIcon, Colour maskColor, ItemRenderType type) {

		if (maskIcon == null || subIcon == null) {
			return;
		}
		if (maskColor == null) {
			maskColor = new ColourRGBA(0xFFFFFFFF);
		}
		maskColor.glColour();
		GL11.glEnable(GL11.GL_BLEND);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glDisable(GL11.GL_CULL_FACE);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setNormal(0, 0, 1);
		if (type.equals(ItemRenderType.INVENTORY)) {
			preRenderIconInv(maskIcon, 10);
		} else {
			preRenderIconWorld(maskIcon, 0.001);
		}
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0, 0, -1);
		if (type.equals(ItemRenderType.INVENTORY)) {
			preRenderIconInv(maskIcon, -0.0635);
		} else {
			preRenderIconWorld(maskIcon, -0.0635);
		}
		tessellator.draw();

		RenderHelper.setBlockTextureSheet();

		GL11.glDepthFunc(GL11.GL_EQUAL);
		GL11.glDepthMask(false);

		tessellator.startDrawingQuads();
		tessellator.setNormal(0, 0, 1);
		if (type.equals(ItemRenderType.INVENTORY)) {
			preRenderIconInv(subIcon, 10);
		} else {
			preRenderIconWorld(subIcon, 0.001);
		}
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0, 0, -1);
		if (type.equals(ItemRenderType.INVENTORY)) {
			preRenderIconInv(subIcon, -0.0635);
		} else {
			preRenderIconWorld(subIcon, -0.0635);
		}
		tessellator.draw();

		GL11.glDisable(GL11.GL_BLEND);
		GlStateManager.depthMask(true);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glColor4f(1, 1, 1, 1);
	}*/

	/*public static void preRenderIconWorld(TextureAtlasSprite icon, double z) {
        Tessellator.instance.addVertexWithUV(0, 1, z, icon.getMinU(), icon.getMaxV());
		Tessellator.instance.addVertexWithUV(1, 1, z, icon.getMaxU(), icon.getMaxV());
		Tessellator.instance.addVertexWithUV(1, 0, z, icon.getMaxU(), icon.getMinV());
		Tessellator.instance.addVertexWithUV(0, 0, z, icon.getMinU(), icon.getMinV());
	}

	public static void preRenderIconInv(IIcon icon, double z) {
		Tessellator.instance.addVertexWithUV(0, 16, z, icon.getMinU(), icon.getMaxV());
		Tessellator.instance.addVertexWithUV(16, 16, z, icon.getMaxU(), icon.getMaxV());
		Tessellator.instance.addVertexWithUV(16, 0, z, icon.getMaxU(), icon.getMinV());
		Tessellator.instance.addVertexWithUV(0, 0, z, icon.getMinU(), icon.getMinV());
	}*/

	/*public static void renderItemStack(int xPos, int yPos, float tickPlace, ItemStack stack, Minecraft mc) {

		if (stack != null) {
			float subTickAnimation = stack.animationsToGo - tickPlace;

			if (subTickAnimation > 0.0F) {
				GL11.glPushMatrix();
				float skew = 1.0F + subTickAnimation / 5.0F;
				GL11.glTranslatef(xPos + 8, yPos + 12, 0.0F);
				GL11.glScalef(1.0F / skew, (skew + 1.0F) / 2.0F, 1.0F);
				GL11.glTranslatef(-(xPos + 8), -(yPos + 12), 0.0F);
			}
			renderItem.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, stack, xPos, yPos);
			if (subTickAnimation > 0.0F) {
				GlStateManager.popMatrix();
			}
			renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, stack, xPos, yPos);
		}
	}*/

	/*public static void renderItemStackAtScale(float xPos, float yPos, float tickPlace, ItemStack stack, Minecraft mc, float scale, boolean renderStackSize) {

		if (stack != null) {
			if (!renderStackSize) {
				stack = stack.copy();
				stack.stackSize = 1;
			}
			float subTickAnimation = stack.animationsToGo - tickPlace;
			renderItem.zLevel = 500;
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			if (subTickAnimation > 0.0F) {
				float skew = scale + subTickAnimation / 5.0F;
				GL11.glTranslatef(xPos + 8, yPos + 12, 0.0F);
				GL11.glScalef(scale / skew, (skew + scale) / 2.0F, scale);
				GL11.glTranslatef(-(xPos + 8), -(yPos + 12), 0.0F);
			} else {
				GL11.glScalef(scale, scale, scale);
			}
			RenderUtils.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, stack, xPos, yPos);
			// itemRenderer.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, stack, xPos, yPos);
			renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, stack, (int) xPos, (int) yPos);
			GlStateManager.popMatrix();
			renderItem.zLevel = 0;
		}
	}*/

	/*public static void renderItemAndEffectIntoGUI(FontRenderer font, TextureManager texture, ItemStack stack, float xPos, float yPos) {

		if (stack != null) {
			if (!ForgeHooksClient.renderInventoryItem(renderBlocks, texture, stack, renderItem.renderWithColor, renderItem.zLevel, xPos, yPos)) {
				RenderUtils.renderItemIntoGUI(font, texture, stack, xPos, yPos, true);
			}
		}
	}*/

	/*public static void renderItemIntoGUI(FontRenderer font, TextureManager manager, ItemStack stack, float xPos, float yPos, boolean renderEffect) {

		Item item = stack.getItem();
		int meta = stack.getItemDamage();
		IIcon icon = stack.getIconIndex();
		int color;

		float red;
		float green;
		float blue;

		Block block = Block.getBlockFromItem(item);
		if (stack.getItemSpriteNumber() == 0 && block != null && RenderBlocks.renderItemIn3d(block.getRenderType())) {
			manager.bindTexture(TextureMap.locationBlocksTexture);
			GL11.glPushMatrix();
			GL11.glTranslatef(xPos - 2, yPos + 3, -3.0F + renderItem.zLevel);
			GL11.glScalef(10.0F, 10.0F, 10.0F);
			GL11.glTranslatef(1.0F, 0.5F, 1.0F);
			GL11.glScalef(1.0F, 1.0F, -1.0F); // ??? what's going on with the scale and translation
			GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);

			if (renderItem.renderWithColor) {
				color = item.getColorFromItemStack(stack, 0);
				red = (color >> 16 & 255) / 255.0F;
				green = (color >> 8 & 255) / 255.0F;
				blue = (color & 255) / 255.0F;

				GL11.glColor4f(red, green, blue, 1.0F);
			}

			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			renderBlocks.useInventoryTint = renderItem.renderWithColor;
			renderBlocks.renderBlockAsItem(block, meta, 1.0F);
			renderBlocks.useInventoryTint = true;
			GlStateManager.popMatrix();
		} else if (item.requiresMultipleRenderPasses()) {
			GlStateManager.disableLighting();

			ResourceLocation texture = stack.getItemSpriteNumber() == 0 ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture;
			manager.bindTexture(texture);
			for (int pass = 0, e = item.getRenderPasses(meta); pass < e; ++pass) {
				icon = item.getIcon(stack, pass);

				if (renderItem.renderWithColor) {
					color = item.getColorFromItemStack(stack, pass);
					red = (color >> 16 & 255) / 255.0F;
					green = (color >> 8 & 255) / 255.0F;
					blue = (color & 255) / 255.0F;

					GL11.glColor4f(red, green, blue, 1.0F);
				}

				RenderHelper.renderIcon(xPos, yPos, renderItem.zLevel, icon, 16, 16);

				if (stack.hasEffect(pass)) {
					RenderUtils.renderEffect(manager, xPos, yPos);
					manager.bindTexture(texture);
				}
			}

			GL11.glEnable(GL11.GL_LIGHTING);
		} else {
			GlStateManager.disableLighting();
			ResourceLocation resourcelocation = manager.getResourceLocation(stack.getItemSpriteNumber());
			manager.bindTexture(resourcelocation);

			if (icon == null) {
				icon = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(resourcelocation)).getAtlasSprite("missingno");
			}

			if (renderItem.renderWithColor) {
				color = item.getColorFromItemStack(stack, 0);
				red = (color >> 16 & 255) / 255.0F;
				green = (color >> 8 & 255) / 255.0F;
				blue = (color & 255) / 255.0F;

				GL11.glColor4f(red, green, blue, 1.0F);
			}

			RenderHelper.renderIcon(xPos, yPos, renderItem.zLevel, icon, 16, 16);
			GL11.glEnable(GL11.GL_LIGHTING);

			if (stack.hasEffect(0)) {
				RenderUtils.renderEffect(manager, xPos, yPos);
			}
		}

		GL11.glEnable(GL11.GL_CULL_FACE);
	}*/

	/*public static void renderEffect(TextureManager manager, float x, float y) {

		GL11.glDepthFunc(GL11.GL_GREATER);
		GlStateManager.disableLighting();
		GL11.glDepthMask(false);

		manager.bindTexture(RenderHelper.MC_ITEM_GLINT);
		renderItem.zLevel -= 50.0F;
		GL11.glEnable(GL11.GL_BLEND);
		GlStateManager.blendFunc(GL11.GL_DST_COLOR, GL11.GL_DST_COLOR);
		GL11.glColor4f(0.5F, 0.25F, 0.8F, 1.0F);
		RenderUtils.renderGlint(x - 2, y - 2, 20, 20);
		GL11.glDisable(GL11.GL_BLEND);
		GlStateManager.depthMask(true);
		renderItem.zLevel += 50.0F;

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
	}*/

	/*public static void renderGlint(float x, float y, int u, int v) {

		Tessellator tessellator = RenderHelper.tessellator();
		float uScale = 1 / 256f;
		float vScale = 1 / 256f;
		float s = 4.0F; // skew
		GlStateManager.blendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
		for (int i = 0; i < 2; i++) {
			float uOffset = Minecraft.getSystemTime() % (3000 + i * 1873) / (3000F + i * 1873) * 256F;
			float vOffset = 0.0F;

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(x + 0, y + v, renderItem.zLevel, (uOffset + 0 + v * s) * uScale, (vOffset + v) * vScale);
			tessellator.addVertexWithUV(x + u, y + v, renderItem.zLevel, (uOffset + u + v * s) * uScale, (vOffset + v) * vScale);
			tessellator.addVertexWithUV(x + u, y + 0, renderItem.zLevel, (uOffset + u + 0 * 0) * uScale, (vOffset + 0) * vScale);
			tessellator.addVertexWithUV(x + 0, y + 0, renderItem.zLevel, (uOffset + 0 + 0 * 0) * uScale, (vOffset + 0) * vScale);
			tessellator.draw();
			s = -1.0F;
		}
	}*/

	public static final void renderItemOnBlockSide(TileEntity tile, ItemStack stack, int side, double x, double y, double z) {

		if (stack == null) {
			return;
		}
        GlStateManager.pushMatrix();

		switch (side) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			GlStateManager.translate(x + 0.75, y + 0.875, z + RenderHelper.RENDER_OFFSET * 145);
			break;
		case 3:
			GlStateManager.translate(x + 0.25, y + 0.875, z + 1 - RenderHelper.RENDER_OFFSET * 145);
            GlStateManager.rotate(180, 0, 1, 0);
			break;
		case 4:
			GlStateManager.translate(x + RenderHelper.RENDER_OFFSET * 145, y + 0.875, z + 0.25);
            GlStateManager.rotate(90, 0, 1, 0);
			break;
		case 5:
			GlStateManager.translate(x + 1 - RenderHelper.RENDER_OFFSET * 145, y + 0.875, z + 0.75);
            GlStateManager.rotate(-90, 0, 1, 0);
			break;
		default:
		}
		GlStateManager.scale(0.03125, 0.03125, -RenderHelper.RENDER_OFFSET);
		GlStateManager.rotate(180, 0, 0, 1);

		setupLight(tile, EnumFacing.VALUES[side]);

		RenderHelper.renderItem().renderItemAndEffectIntoGUI(stack, 0,0);

		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GlStateManager.popMatrix();
		net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
	}

    public static void setupLight(TileEntity tile, EnumFacing side) {

        if (tile == null) {
            return;
        }
        BlockPos pos = tile.getPos().offset(side);
        World world = tile.getWorld();

        if (world.getBlockState(pos).isOpaqueCube()) {
            return;
        }
        int br = world.getCombinedLight(pos, 4);
        int brX = br & 65535;
        int brY = br >>> 16;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brX, brY);
    }

}
