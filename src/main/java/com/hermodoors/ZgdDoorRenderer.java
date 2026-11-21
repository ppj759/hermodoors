package com.hermodoors;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

public class ZgdDoorRenderer extends TileEntityRenderer<ZgdDoorTile> {
    private static final ResourceLocation LEAF = new ResourceLocation(HermoDoors.MODID, "block/zgd_leaf");
    private static final ResourceLocation FRONT = new ResourceLocation(HermoDoors.MODID, "block/zgd_front");
    private static final ResourceLocation BACK = new ResourceLocation(HermoDoors.MODID, "block/zgd_back");

    // Locked: wing bolts sit 2 pixels deeper in the door (in blocks). Not animated.
    private static final float BOLT_TRAVEL = 2.0F / 16.0F;

    public ZgdDoorRenderer(TileEntityRendererDispatcher d) {
        super(d);
    }

    @Override
    public void render(ZgdDoorTile te, float pt, MatrixStack ms, IRenderTypeBuffer buf, int light, int overlay) {
        BlockState st = te.getBlockState();
        if (!(st.getBlock() instanceof ZgdDoorBlock)) return;

        float p = MathHelper.lerp(pt, te.prev, te.progress);
        float swing = MathHelper.clamp(p, 0.0F, 1.0F);
        swing = swing * swing * (3.0F - 2.0F * swing);

        float bolt = st.get(ZgdDoorBlock.LOCKED) ? 1.0F : 0.0F; // no animation, bolts just switch
        Direction d = st.get(ZgdDoorBlock.FACING);
        Minecraft mc = Minecraft.getInstance();
        BlockModelRenderer r = mc.getBlockRendererDispatcher().getBlockModelRenderer();
        IVertexBuilder vb = buf.getBuffer(RenderType.getCutout());

        ms.push();
        ms.translate(0.5, 0.0, 0.5);
        ms.rotate(Vector3f.YP.rotationDegrees(180.0F - d.getHorizontalAngle()));
        ms.translate(-0.5, 0.0, -0.5);

        // leaf swings around the hinge at x = 16px, z = 3px
        ms.push();
        ms.translate(1.0, 0.0, 3.0 / 16.0);
        ms.rotate(Vector3f.YP.rotationDegrees(90.0F * swing));
        ms.translate(-1.0, 0.0, -3.0 / 16.0);

        draw(r, vb, ms, mc.getModelManager().getModel(LEAF), light, overlay);

        ms.push();
        ms.translate(0.0, 0.0, BOLT_TRAVEL * bolt);
        draw(r, vb, ms, mc.getModelManager().getModel(FRONT), light, overlay);
        ms.pop();

        ms.push();
        ms.translate(0.0, 0.0, -BOLT_TRAVEL * bolt);
        draw(r, vb, ms, mc.getModelManager().getModel(BACK), light, overlay);
        ms.pop();

        ms.pop();
        ms.pop();
    }

    private static void draw(BlockModelRenderer r, IVertexBuilder vb, MatrixStack ms, IBakedModel m, int light, int overlay) {
        r.renderModel(ms.getLast(), vb, null, m, 1.0F, 1.0F, 1.0F, light, overlay, EmptyModelData.INSTANCE);
    }
}
