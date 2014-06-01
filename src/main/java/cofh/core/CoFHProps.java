package cofh.core;

import cofh.CoFHCore;
import cofh.util.StringHelper;

import java.io.File;

public class CoFHProps {

	public static final String FORGE_REQ = "10.0.12.1080";
	public static final String VERSION = CoFHCore.version;
	public static File configDir = null;

	/* Global Constants */
	public static final String DEFAULT_OWNER = "[None]";

	public static final int TIME_CONSTANT = 32;
	public static final int TIME_CONSTANT_HALF = TIME_CONSTANT / 2;
	public static final int TIME_CONSTANT_QUARTER = TIME_CONSTANT / 4;
	public static final int TIME_CONSTANT_EIGHTH = TIME_CONSTANT / 8;
	public static final int RF_PER_MJ = 10;
	public static final int ENTITY_TRACKING_DISTANCE = 64;

	/* Graphics */
	public static final String PATH_GFX = "cofh:textures/";
	public static final String PATH_ENTITY = PATH_GFX + "entity/";

	/* Global Localizations */
	public static String tutorialTabConfiguration = StringHelper.localize("info.cofh.tutorial.tabConfiguration");
	public static String tutorialTabOperation = StringHelper.localize("info.cofh.tutorial.tabConfiguration2");
	public static String tutorialTabRedstone = StringHelper.localize("info.cofh.tutorial.tabRedstone");
	public static String tutorialTabFluxRequired = StringHelper.localize("info.cofh.tutorial.fluxRequired");

	/* Network */
	public static final int NETWORK_UPDATE_RANGE = 192;

	/* Options */
	public static boolean enableInformationTabs = true;
	public static boolean enableTutorialTabs = true;
	public static boolean enableUpdateNotice = true;
	public static boolean enableItemPickupModule = true;

	public static boolean enableOpSecureAccess = false;
	public static boolean enableOpSecureAccessWarning = true;

	private CoFHProps() {

	}

}
