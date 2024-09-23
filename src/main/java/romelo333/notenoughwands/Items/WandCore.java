package romelo333.notenoughwands.Items;

import net.minecraft.item.Item;

import cpw.mods.fml.common.registry.GameRegistry;
import romelo333.notenoughwands.NotEnoughWands;

public class WandCore extends Item {

    public WandCore(String name, String texture) {
        setMaxStackSize(64);
        setUnlocalizedName(name);
        setCreativeTab(NotEnoughWands.tabNew);
        setTextureName(NotEnoughWands.MODID + ":" + texture);
        GameRegistry.registerItem(this, name);
    }
}
