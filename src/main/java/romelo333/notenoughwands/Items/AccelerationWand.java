package romelo333.notenoughwands.Items;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import romelo333.notenoughwands.varia.Tools;

public class AccelerationWand extends GenericWand {

    public static final int MODE_FIRST = 0;
    public static final int MODE_20 = 0;
    public static final int MODE_50 = 1;
    public static final int MODE_100 = 2;
    public static final int MODE_500 = 3;
    public static final int MODE_1000 = 4;
    public static final int MODE_LAST = MODE_1000;

    public static final String[] descriptions = new String[] { "20", "50", "100", "500", "1000" };
    public static final int[] amount = new int[] { 20, 50, 100, 500, 1000 };
    public static final float[] cost = new float[] { 1.0f, 2.0f, 3.0f, 4.0f, 5.0f };

    public AccelerationWand() {
        setup("AccelerationWand", "accelerationWand").xpUsage(5)
            .availability(AVAILABILITY_ADVANCED)
            .loot(2);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean b) {
        super.addInformation(stack, player, list, b);
        list.add(EnumChatFormatting.GREEN + "Mode: " + descriptions[getMode(stack)]);
        list.add("Right click on block to speed up ticks.");
        list.add("Mode key (default '=') to change speed.");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx,
        float sy, float sz) {
        if (!world.isRemote) {
            Block block = world.getBlock(x, y, z);
            int mode = getMode(stack);
            if (!checkUsage(stack, player, cost[mode])) {
                return true;
            }
            Random random = new Random();
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            for (int i = 0; i < amount[mode] / (tileEntity == null ? 5 : 1); i++) {
                if (tileEntity == null) {
                    block.updateTick(world, x, y, z, random);
                } else {
                    tileEntity.updateEntity();
                }
            }
            registerUsage(stack, player, cost[mode]);
        }
        return true;
    }

    @Override
    public void toggleMode(EntityPlayer player, ItemStack stack) {
        int mode = getMode(stack);
        mode++;
        if (mode > MODE_LAST) {
            mode = MODE_FIRST;
        }
        Tools.notify(player, "Switched to " + descriptions[mode] + " mode");
        setMode(stack, mode);
    }

    private void setMode(ItemStack stack, int mode) {
        Tools.getTagCompound(stack)
            .setInteger("mode", mode);
    }

    private int getMode(ItemStack stack) {
        return Tools.getTagCompound(stack)
            .getInteger("mode");
    }

    @Override
    protected void setupCraftingInt(Item wandcore) {
        GameRegistry
            .addRecipe(new ItemStack(this), "gg ", "gw ", "  w", 'g', new ItemStack(Items.dye, 1, 15), 'w', wandcore);
    }

}
