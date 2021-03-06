package com.lothrazar.cyclic.block.detectorentity;

import com.lothrazar.cyclic.base.BlockBase;
import com.lothrazar.cyclic.registry.ContainerScreenRegistry;
import com.lothrazar.cyclic.registry.TileRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class BlockDetector extends BlockBase {

  public BlockDetector(Properties properties) {
    super(properties.hardnessAndResistance(1.8F).notSolid());
    this.setHasGui();
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void registerClient() {
    RenderTypeLookup.setRenderLayer(this, RenderType.getCutoutMipped());
    ClientRegistry.bindTileEntityRenderer(TileRegistry.detector_entity, RenderDetector::new);
    ScreenManager.registerFactory(ContainerScreenRegistry.detector_entity, ScreenDetector::new);
  }

  @Override
  @Deprecated
  public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
    TileDetector te = (TileDetector) blockAccess.getTileEntity(pos);
    if (te == null) {
      return 0;
    }
    return te.isPowered() ? 15 : 0;
  }

  @Override
  public boolean canProvidePower(BlockState state) {
    return true;
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new TileDetector();
  }
}
