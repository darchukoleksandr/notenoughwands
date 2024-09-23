package romelo333.notenoughwands;

import romelo333.notenoughwands.Items.AccelerationWand;
import romelo333.notenoughwands.Items.AdvancedWandCore;
import romelo333.notenoughwands.Items.BuildingWand;
import romelo333.notenoughwands.Items.CapturingWand;
import romelo333.notenoughwands.Items.DisplacementWand;
import romelo333.notenoughwands.Items.IlluminationWand;
import romelo333.notenoughwands.Items.MovingWand;
import romelo333.notenoughwands.Items.ProtectionWand;
import romelo333.notenoughwands.Items.SwappingWand;
import romelo333.notenoughwands.Items.TeleportationWand;
import romelo333.notenoughwands.Items.WandCore;

public class ModItems {

    public static WandCore wandCore;
    public static AdvancedWandCore advancedWandCore;
    public static SwappingWand swappingWand;
    public static TeleportationWand teleportationWand;
    public static CapturingWand capturingWand;
    public static BuildingWand buildingWand;
    public static IlluminationWand illuminationWand;
    public static MovingWand movingWand;
    public static ProtectionWand protectionWand;
    public static ProtectionWand masterProtectionWand;
    public static DisplacementWand displacementWand;
    public static AccelerationWand accelerationWand;

    public static void init() {
        wandCore = new WandCore("WandCore", "wandCore");
        advancedWandCore = new AdvancedWandCore("AdvancedWandCore", "advancedWandCore");
        swappingWand = new SwappingWand();
        teleportationWand = new TeleportationWand();
        capturingWand = new CapturingWand();
        buildingWand = new BuildingWand();
        illuminationWand = new IlluminationWand();
        movingWand = new MovingWand();
        protectionWand = new ProtectionWand(false);
        masterProtectionWand = new ProtectionWand(true);
        displacementWand = new DisplacementWand();
        accelerationWand = new AccelerationWand();
    }
}
