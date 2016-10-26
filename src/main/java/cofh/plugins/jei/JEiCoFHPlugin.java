package cofh.plugins.jei;

import mezz.jei.api.*;
import mezz.jei.api.ingredients.IModIngredientRegistration;

/**
 * Created by covers1624 on 26/10/2016.
 */
@JEIPlugin
public class JEiCoFHPlugin implements IModPlugin {
    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {

    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {

    }

    @Override
    public void register(IModRegistry registry) {
        registry.addAdvancedGuiHandlers(new SlotMover());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

    }
}
