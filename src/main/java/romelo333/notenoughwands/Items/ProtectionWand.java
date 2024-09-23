package romelo333.notenoughwands.Items;

import java.util.List;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import romelo333.notenoughwands.Config;
import romelo333.notenoughwands.ProtectedBlocks;
import romelo333.notenoughwands.network.PacketGetProtectedBlockCount;
import romelo333.notenoughwands.network.PacketGetProtectedBlocks;
import romelo333.notenoughwands.network.PacketHandler;
import romelo333.notenoughwands.network.ReturnProtectedBlockCountHelper;
import romelo333.notenoughwands.network.ReturnProtectedBlocksHelper;
import romelo333.notenoughwands.varia.ContainerToItemRecipe;
import romelo333.notenoughwands.varia.Tools;

public class ProtectionWand extends GenericWand {

    public static final int MODE_FIRST = 0;
    public static final int MODE_PROTECT = 0;
    public static final int MODE_UNPROTECT = 1;
    public static final int MODE_CLEAR = 2;
    public static final int MODE_LAST = MODE_CLEAR;

    public int blockShowRadius = 10;
    public int maximumProtectedBlocks = 16;

    private final boolean master;

    public static final String[] descriptions = new String[] { "protect", "unprotect", "clear all" };

    public ProtectionWand(boolean master) {
        if (master) {
            setup("MasterProtectionWand", "masterProtectionWand").xpUsage(0)
                .availability(AVAILABILITY_CREATIVE)
                .loot(0);
        } else {
            setup("ProtectionWand", "protectionWand").xpUsage(50)
                .availability(AVAILABILITY_ADVANCED)
                .loot(1);
        }
        this.master = master;
    }

    @Override
    public void initConfig(Configuration cfg) {
        super.initConfig(cfg);
        blockShowRadius = cfg
            .get(
                Config.CATEGORY_WANDS,
                getUnlocalizedName() + "_blockShowRadius",
                blockShowRadius,
                "How far around the player protected blocks will be hilighted")
            .getInt();
        maximumProtectedBlocks = cfg
            .get(
                Config.CATEGORY_WANDS,
                getUnlocalizedName() + "_maximumProtectedBlocks",
                master ? 0 : maximumProtectedBlocks,
                "The maximum number of blocks to protect with this wand (set to 0 for no maximum)")
            .getInt();
    }

    private static long tooltipLastTime = 0;

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean b) {
        super.addInformation(stack, player, list, b);
        int mode = getMode(stack);
        int id = getId(stack);
        if ((System.currentTimeMillis() - tooltipLastTime) > 250) {
            tooltipLastTime = System.currentTimeMillis();
            PacketHandler.INSTANCE.sendToServer(new PacketGetProtectedBlockCount(id));
        }
        list.add(EnumChatFormatting.GREEN + "Mode: " + descriptions[mode]);
        if (master) {
            list.add(EnumChatFormatting.YELLOW + "Master wand");
        } else {
            if (id != 0) {
                list.add(EnumChatFormatting.GREEN + "Id: " + id);
            }
        }
        list.add(EnumChatFormatting.GREEN + "Number of protected blocks: " + ReturnProtectedBlockCountHelper.count);
        list.add("Rigth click to protect or unprotect a block.");
        list.add("Mode key (default '=') to switch mode.");
    }

    @Override
    public void toggleMode(EntityPlayer player, ItemStack stack) {
        int mode = getMode(stack);
        mode++;
        if (mode > MODE_LAST) {
            mode = MODE_FIRST;
        }
        Tools.notify(player, "Switched to " + descriptions[mode] + " mode");
        Tools.getTagCompound(stack)
            .setInteger("mode", mode);
    }

    private int getMode(ItemStack stack) {
        return Tools.getTagCompound(stack)
            .getInteger("mode");
    }

    public int getId(ItemStack stack) {
        if (master) {
            return -1;
        }
        return Tools.getTagCompound(stack)
            .getInteger("id");
    }

    private static long lastTime = 0;

    @SideOnly(Side.CLIENT)
    @Override
    public void renderOverlay(RenderWorldLastEvent evt, EntityClientPlayerMP player, ItemStack wand) {
        if ((System.currentTimeMillis() - lastTime) > 250) {
            lastTime = System.currentTimeMillis();
            PacketHandler.INSTANCE.sendToServer(new PacketGetProtectedBlocks());
        }
        if (master) {
            renderOutlines(evt, player, ReturnProtectedBlocksHelper.childBlocks, 30, 30, 200);
        }
        renderOutlines(evt, player, ReturnProtectedBlocksHelper.blocks, 210, 60, 40);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx,
        float sy, float sz) {
        if (!world.isRemote) {
            ProtectedBlocks protectedBlocks = ProtectedBlocks.getProtectedBlocks(world);
            int id = getOrCreateId(stack, world, protectedBlocks);
            int mode = getMode(stack);
            if (mode == MODE_PROTECT) {
                if (!checkUsage(stack, player, 1.0f)) {
                    return true;
                }
                if (!protectedBlocks.protect(player, world, x, y, z, id)) {
                    return true;
                }
                registerUsage(stack, player, 1.0f);
            } else if (mode == MODE_UNPROTECT) {
                if (!protectedBlocks.unprotect(player, world, x, y, z, id)) {
                    return true;
                }
            } else {
                int cnt = protectedBlocks.clearProtections(world, id);
                Tools.notify(player, "Cleared " + cnt + " protected blocks");
            }
        }
        return true;
    }

    private int getOrCreateId(ItemStack stack, World world, ProtectedBlocks protectedBlocks) {
        int id = getId(stack);
        if (id == 0) {
            id = protectedBlocks.getNewId(world);
            Tools.getTagCompound(stack)
                .setInteger("id", id);
        }
        return id;
    }

    @Override
    protected void setupCraftingInt(Item wandcore) {
        if (master) {
            GameRegistry.addRecipe(
                new ItemStack(this),
                "re ",
                "ew ",
                "  w",
                'r',
                Items.comparator,
                'e',
                Items.nether_star,
                'w',
                wandcore);
        } else {
            GameRegistry.addRecipe(
                new ItemStack(this),
                "re ",
                "ew ",
                "  w",
                'r',
                Items.comparator,
                'e',
                Items.ender_eye,
                'w',
                wandcore);
            GameRegistry.addRecipe(
                new ContainerToItemRecipe(
                    new ItemStack[] { new ItemStack(this), new ItemStack(Items.ender_eye), null,
                        new ItemStack(Items.ender_eye), new ItemStack(wandcore), null, null, null,
                        new ItemStack(wandcore) },
                    0,
                    new ItemStack(this)));
        }
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return !master;
    }

    @Override
    public Item getContainerItem() {
        return this;
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        if (hasContainerItem(stack) && stack.hasTagCompound()) {
            ItemStack container = new ItemStack(getContainerItem());
            container.setTagCompound(
                (NBTTagCompound) stack.getTagCompound()
                    .copy());
            return container;
        }
        return null;
    }

}
