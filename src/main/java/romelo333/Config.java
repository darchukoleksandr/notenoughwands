package romelo333;


import net.minecraftforge.common.config.Configuration;
import romelo333.notenoughwands.ModItems;

public class Config {
    public static String CATEGORY_WANDS = "wands";

    public static void init(Configuration cfg) {
        ModItems.swappingWand.initConfig(cfg);
    }
}
