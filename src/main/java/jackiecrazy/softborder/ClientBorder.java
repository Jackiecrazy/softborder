package jackiecrazy.softborder;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.softborder.config.BorderConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SoftBorder.MODID, value = Dist.CLIENT)
public class ClientBorder {
    protected static final ResourceLocation VIGNETTE_TEX_PATH = new ResourceLocation(SoftBorder.MODID, "textures/vignette.png");
    private static float vignetteLevel = 0;
    private static float density = 0;

    @SubscribeEvent
    public static void potionFog(EntityViewRenderEvent.FogDensity e) {
        if (Minecraft.getInstance().player.isPotionActive(SoftBorder.BORDER_ANCHOR.get())) {
            density = MathHelper.clamp((BorderConfig.anchorTime - Minecraft.getInstance().player.getActivePotionEffect(SoftBorder.BORDER_ANCHOR.get()).getDuration()) / (float)BorderConfig.anchorTime, 0, 1);
        } else density = Math.max(0, density - 0.01f);
        if (density != 0) {
            e.setDensity(density);
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void potionFog(EntityViewRenderEvent.FogColors e) {
        if (density != 0) {
            e.setBlue(0);
            e.setRed(0);
            e.setGreen(0);
        }
    }

    @SubscribeEvent
    public static void client(RenderGameOverlayEvent e) {
        if (e.getType() == RenderGameOverlayEvent.ElementType.VIGNETTE && Minecraft.getInstance().player != null && BorderUtils.getClampedOverreachPercentage(Minecraft.getInstance().player) > 0) {
            renderVignette(Minecraft.getInstance().player, e.getWindow().getScaledWidth(), e.getWindow().getScaledHeight());
        }
    }

    protected static void renderVignette(PlayerEntity entityIn, int scaledWidth, int scaledHeight) {
        float to = (float) BorderUtils.getClampedOverreachPercentage(entityIn) / (float) (BorderConfig.max);
        boolean close = true;
        float temp = vignetteLevel;
        if (to > vignetteLevel) {
            vignetteLevel += (to - temp) / 20;
            close = false;
        }
        if (to < vignetteLevel) {
            vignetteLevel += (to - temp) / 20;
            close = !close;
        }
        if (close)
            vignetteLevel = to;

        if (vignetteLevel == 0.0F) return;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.color4f(vignetteLevel, vignetteLevel, vignetteLevel, 1.0F);

        Minecraft.getInstance().getTextureManager().bindTexture(VIGNETTE_TEX_PATH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(0.0D, (double) scaledHeight, -90.0D).tex(0.0F, 1.0F).endVertex();
        bufferbuilder.pos((double) scaledWidth, (double) scaledHeight, -90.0D).tex(1.0F, 1.0F).endVertex();
        bufferbuilder.pos((double) scaledWidth, 0.0D, -90.0D).tex(1.0F, 0.0F).endVertex();
        bufferbuilder.pos(0.0D, 0.0D, -90.0D).tex(0.0F, 0.0F).endVertex();
        tessellator.draw();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
    }
}
