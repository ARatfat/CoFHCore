package cofh.core.world.feature;

import org.apache.logging.log4j.Logger;

import cofh.lib.util.WeightedRandomBlock;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.world.feature.FeatureBase;
import cofh.lib.world.feature.FeatureBase.GenRestriction;
import cofh.lib.world.feature.FeatureGenUnderwater;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenerator;

public class UnderwaterParser extends UniformParser {

	@Override
	protected List<WeightedRandomBlock> generateDefaultMaterial() {

		return Arrays.asList(new WeightedRandomBlock(Blocks.dirt, -1), new WeightedRandomBlock(Blocks.grass, -1));
	}

	@Override
	protected FeatureBase getFeature(String featureName, JsonObject genObject, WorldGenerator gen, List<WeightedRandomBlock> matList, int numClusters, GenRestriction biomeRes, boolean retrogen,
			GenRestriction dimRes, Logger log) {

		int chunkChance = MathHelper.clampI(genObject.get("chunkChance").getAsInt(), 1, 1000000);

		return new FeatureGenUnderwater(featureName, gen, matList, numClusters, chunkChance, biomeRes, retrogen, dimRes);
	}

}
