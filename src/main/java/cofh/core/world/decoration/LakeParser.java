package cofh.core.world.decoration;

import cofh.api.world.IGeneratorParser;
import cofh.core.world.FeatureParser;
import cofh.lib.util.WeightedRandomBlock;
import cofh.lib.world.WorldGenAdvLakes;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LakeParser implements IGeneratorParser {

	@Override
	public WorldGenerator parseGenerator(String generatorName, Config genObject, Logger log, List<WeightedRandomBlock> resList, int clusterSize, List<WeightedRandomBlock> matList) {

		boolean useMaterial = false;
		{
			useMaterial = genObject.hasPath("use-material") ? genObject.getBoolean("use-material") : useMaterial;
		}
		WorldGenAdvLakes r = new WorldGenAdvLakes(resList, useMaterial ? matList : null);
		{
			ArrayList<WeightedRandomBlock> list = new ArrayList<WeightedRandomBlock>();
			if (genObject.hasPath("outline-block")) {
				if (!FeatureParser.parseResList(genObject.root().get("outline-block"), list, true)) {
					log.warn("Entry specifies invalid outline-block for 'lake' generator! Not outlining!");
				} else {
					r.setOutlineBlock(list);
				}
				list = new ArrayList<WeightedRandomBlock>();
			}
			if (genObject.hasPath("gap-block")) {
				if (!FeatureParser.parseResList(genObject.getValue("gap-block"), list, true)) {
					log.warn("Entry specifies invalid gap block for 'lake' generator! Not filling!");
				} else {
					r.setGapBlock(list);
				}
			}
			if (genObject.hasPath("solid-outline")) {
				r.setSolidOutline(genObject.getBoolean("solid-outline"));
			}
			if (genObject.hasPath("total-outline")) {
				r.setTotalOutline(genObject.getBoolean("total-outline"));
			}
		}
		return r;
	}

}
