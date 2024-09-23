package romelo333.notenoughwands.Items;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import romelo333.notenoughwands.Config;
import romelo333.notenoughwands.ProtectedBlocks;
import romelo333.notenoughwands.varia.Coordinate;
import romelo333.notenoughwands.varia.Tools;

public class SwappingWand extends GenericWand {

    public static final int MODE_FIRST = 0;
    public static final int MODE_3X3 = 0;
    public static final int MODE_5X5 = 1;
    public static final int MODE_7X7 = 2;
    public static final int MODE_SINGLE = 3;
    public static final int MODE_LAST = MODE_SINGLE;

    private float hardnessDistance = 35.0f;

    public static final String[] descriptions = new String[] { "3x3", "5x5", "7x7", "single" };

    public SwappingWand() {
        setup("SwappingWand", "swappingWand").xpUsage(4)
            .availability(AVAILABILITY_ADVANCED)
            .loot(5);
    }

    @Override
    public void initConfig(Configuration cfg) {
        super.initConfig(cfg);
        hardnessDistance = (float) cfg
            .get(
                Config.CATEGORY_WANDS,
                getUnlocalizedName() + "_hardnessDistance",
                hardnessDistance,
                "How far away the hardness can be to allow swapping (100 means basically everything allowed)")
            .getDouble();
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

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean b) {
        super.addInformation(stack, player, list, b);
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            list.add(EnumChatFormatting.RED + "No selected block");
        } else {
            int id = compound.getInteger("block");
            Block block = (Block) Block.blockRegistry.getObjectById(id);
            int meta = compound.getInteger("meta");
            String name = Tools.getBlockName(block, meta);
            list.add(EnumChatFormatting.GREEN + "Selected block: " + name);
            list.add(EnumChatFormatting.GREEN + "Mode: " + descriptions[compound.getInteger("mode")]);
        }
        list.add("Sneak right click to select a block.");
        list.add("Right click on block to replace.");
        list.add("Mode key (default '=') to switch mode.");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx,
        float sy, float sz) {
        if (!world.isRemote) {
            if (player.isSneaking()) {
                selectBlock(stack, player, world, x, y, z);
            } else {
                placeBlock(stack, player, world, x, y, z, side);
            }
        }
        return true;
    }

    private void placeBlock(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side) {
        if (!checkUsage(stack, player, 1.0f)) {
            return;
        }

        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            Tools.error(player, "First select a block by sneaking");
            return;
        }
        int id = tagCompound.getInteger("block");
        Block block = (Block) Block.blockRegistry.getObjectById(id);
        int meta = tagCompound.getInteger("meta");
        float hardness = tagCompound.getFloat("hardness");

        Block oldblock = world.getBlock(x, y, z);
        int oldmeta = world.getBlockMetadata(x, y, z);
        float blockHardness = oldblock.getBlockHardness(world, x, y, z);

        if (block == oldblock && meta == oldmeta) {
            // The same, nothing happens.
            return;
        }

        if (blockHardness < -0.1f) {
            Tools.error(player, "This block cannot be swapped!");
            return;
        }

        if (Math.abs(hardness - blockHardness) >= hardnessDistance) {
            Tools.error(player, "The hardness of this blocks differs too much to swap!");
            return;
        }

        ProtectedBlocks protectedBlocks = ProtectedBlocks.getProtectedBlocks(world);
        if (protectedBlocks.isProtected(world, x, y, z)) {
            Tools.error(player, "This block is protected. You cannot replace it!");
            return;
        }

        Set<Coordinate> coordinates = findSuitableBlocks(stack, world, side, x, y, z, oldblock, oldmeta);
        boolean notenough = false;
        for (Coordinate coordinate : coordinates) {
            if (!checkUsage(stack, player, 1.0f)) {
                return;
            }
            if (Tools.consumeInventoryItem(Item.getItemFromBlock(block), meta, player.inventory, player)) {
                if (!player.capabilities.isCreativeMode) {
                    Tools.giveItem(world, player, oldblock, oldmeta, 1, x, y, z);
                }
                Tools.playSound(
                    world,
                    block.stepSound.getBreakSound(),
                    coordinate.getX(),
                    coordinate.getY(),
                    coordinate.getZ(),
                    1.0f,
                    1.0f);
                world.setBlock(coordinate.getX(), coordinate.getY(), coordinate.getZ(), block, meta, 2);
                player.openContainer.detectAndSendChanges();
                registerUsage(stack, player, 1.0f);
            } else {
                notenough = true;
            }
        }
        if (notenough) {
            Tools.error(player, "You don't have the right block");
        }
    }

    private void selectBlock(ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        NBTTagCompound tagCompound = Tools.getTagCompound(stack);
        String name = Tools.getBlockName(block, meta);
        if (name == null) {
            Tools.error(player, "You cannot select this block!");
        } else {
            int id = Block.blockRegistry.getIDForObject(block);
            tagCompound.setInteger("block", id);
            tagCompound.setInteger("meta", meta);
            float hardness = block.getBlockHardness(world, x, y, z);
            tagCompound.setFloat("hardness", hardness);
            Tools.notify(player, "Selected block: " + name);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderOverlay(RenderWorldLastEvent evt, EntityClientPlayerMP player, ItemStack wand) {
        MovingObjectPosition mouseOver = Minecraft.getMinecraft().objectMouseOver;
        if (mouseOver != null) {
            Block block = player.worldObj.getBlock(mouseOver.blockX, mouseOver.blockY, mouseOver.blockZ);
            if (block != null && block.getMaterial() != Material.air) {
                int meta = player.worldObj.getBlockMetadata(mouseOver.blockX, mouseOver.blockY, mouseOver.blockZ);

                int wandId = Tools.getTagCompound(wand)
                    .getInteger("block");
                Block wandBlock = (Block) Block.blockRegistry.getObjectById(wandId);
                int wandMeta = Tools.getTagCompound(wand)
                    .getInteger("meta");
                if (wandBlock == block && wandMeta == meta) {
                    return;
                }

                Set<Coordinate> coordinates = findSuitableBlocks(
                    wand,
                    player.worldObj,
                    mouseOver.sideHit,
                    mouseOver.blockX,
                    mouseOver.blockY,
                    mouseOver.blockZ,
                    block,
                    meta);
                renderOutlines(evt, player, coordinates, 200, 230, 180);
            }
        }
    }

    private Set<Coordinate> findSuitableBlocks(ItemStack stack, World world, int sideHit, int x, int y, int z,
        Block centerBlock, int centerMeta) {
        Set<Coordinate> coordinates = new HashSet<Coordinate>();
        int mode = getMode(stack);
        int dim = 0;
        switch (mode) {
            case MODE_SINGLE:
                coordinates.add(new Coordinate(x, y, z));
                return coordinates;
            case MODE_3X3:
                dim = 1;
                break;
            case MODE_5X5:
                dim = 2;
                break;
            case MODE_7X7:
                dim = 3;
                break;
        }
        switch (ForgeDirection.getOrientation(sideHit)) {
            case UP:
            case DOWN:
                for (int dx = x - dim; dx <= x + dim; dx++) {
                    for (int dz = z - dim; dz <= z + dim; dz++) {
                        checkAndAddBlock(world, dx, y, dz, centerBlock, centerMeta, coordinates);
                    }
                }
                break;
            case SOUTH:
            case NORTH:
                for (int dx = x - dim; dx <= x + dim; dx++) {
                    for (int dy = y - dim; dy <= y + dim; dy++) {
                        checkAndAddBlock(world, dx, dy, z, centerBlock, centerMeta, coordinates);
                    }
                }
                break;
            case EAST:
            case WEST:
                for (int dy = y - dim; dy <= y + dim; dy++) {
                    for (int dz = z - dim; dz <= z + dim; dz++) {
                        checkAndAddBlock(world, x, dy, dz, centerBlock, centerMeta, coordinates);
                    }
                }
                break;
        }

        return coordinates;
    }

    private void checkAndAddBlock(World world, int x, int y, int z, Block centerBlock, int centerMeta,
        Set<Coordinate> coordinates) {
        if (world.getBlock(x, y, z) == centerBlock && world.getBlockMetadata(x, y, z) == centerMeta) {
            coordinates.add(new Coordinate(x, y, z));
        }
    }

    private int getMode(ItemStack stack) {
        return Tools.getTagCompound(stack)
            .getInteger("mode");
    }

    @Override
    protected void setupCraftingInt(Item wandcore) {
        GameRegistry.addRecipe(
            new ItemStack(this),
            "rg ",
            "gw ",
            "  w",
            'r',
            Blocks.redstone_block,
            'g',
            Blocks.glowstone,
            'w',
            wandcore);
    }
}
