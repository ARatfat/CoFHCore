package cofh.core.gui;

import cofh.core.init.CoreTextures;
import cofh.lib.gui.GuiBase;
import cofh.lib.gui.GuiProps;
import cofh.lib.util.helpers.StringHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public abstract class GuiCore extends GuiBase {

	public static final String TEX_ARROW_LEFT = GuiProps.PATH_ELEMENTS + "progress_arrow_left.png";
	public static final String TEX_ARROW_RIGHT = GuiProps.PATH_ELEMENTS + "progress_arrow_right.png";
	public static final String TEX_DROP_LEFT = GuiProps.PATH_ELEMENTS + "progress_fluid_left.png";
	public static final String TEX_DROP_RIGHT = GuiProps.PATH_ELEMENTS + "progress_fluid_right.png";

	public static final String TEX_ALCHEMY = GuiProps.PATH_ELEMENTS + "scale_alchemy.png";
	public static final String TEX_BUBBLE = GuiProps.PATH_ELEMENTS + "scale_bubble.png";
	public static final String TEX_CRUSH = GuiProps.PATH_ELEMENTS + "scale_crush.png";
	public static final String TEX_FLAME = GuiProps.PATH_ELEMENTS + "scale_flame.png";
	public static final String TEX_FLUX = GuiProps.PATH_ELEMENTS + "scale_flux.png";
	public static final String TEX_SAW = GuiProps.PATH_ELEMENTS + "scale_saw.png";
	public static final String TEX_SUN = GuiProps.PATH_ELEMENTS + "scale_sun.png";
	public static final String TEX_SNOWFLAKE = GuiProps.PATH_ELEMENTS + "scale_snowflake.png";

	public static final int PROGRESS = 24;
	public static final int SPEED = 16;

	protected String myInfo = "";

	public GuiCore(Container container) {

		super(container);
	}

	public GuiCore(Container container, ResourceLocation texture) {

		super(container, texture);
	}

	protected void generateInfo(String tileString, int lines) {

		myInfo = StringHelper.localize(tileString + "." + 0);
		for (int i = 1; i < lines; i++) {
			myInfo += "\n\n" + StringHelper.localize(tileString + "." + i);
		}
	}

	/* HELPERS */
	@Override
	public void drawButton(TextureAtlasSprite icon, int x, int y, int mode) {

		switch (mode) {
			case 0:
				drawIcon(CoreTextures.ICON_BUTTON, x, y);
				break;
			case 1:
				drawIcon(CoreTextures.ICON_BUTTON_HIGHLIGHT, x, y);
				break;
			default:
				drawIcon(CoreTextures.ICON_BUTTON_INACTIVE, x, y);
				break;
		}
		drawIcon(icon, x, y);
	}

}
