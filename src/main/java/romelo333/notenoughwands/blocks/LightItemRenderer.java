package romelo333.notenoughwands.blocks;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import romelo333.notenoughwands.ModRenderers;
import romelo333.notenoughwands.NotEnoughWands;

@SideOnly(Side.CLIENT)
public class LightItemRenderer implements IItemRenderer {

    ResourceLocation texture = new ResourceLocation(NotEnoughWands.MODID.toLowerCase(), "textures/blocks/light.png");

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        FMLClientHandler.instance()
            .getClient().renderEngine.bindTexture(texture);

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        boolean blending = GL11.glIsEnabled(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_BLEND);

        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
        long t = System.currentTimeMillis() % 6;
        ModRenderers.renderBillboardQuad(0.6f, t * (1.0f / 6.0f), (1.0f / 6.0f));

        GL11.glPopMatrix();

        if (!blending) {
            GL11.glDisable(GL11.GL_BLEND);
        }
    }
}
