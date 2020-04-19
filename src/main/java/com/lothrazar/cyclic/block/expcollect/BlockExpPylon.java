package com.lothrazar.cyclic.block.expcollect;

import com.lothrazar.cyclic.base.BlockBase;
import com.lothrazar.cyclic.registry.ItemRegistry;
import com.lothrazar.cyclic.util.UtilChat;
import com.lothrazar.cyclic.util.UtilSound;
import com.lothrazar.cyclic.util.UtilStuff;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockExpPylon extends BlockBase {

  public BlockExpPylon(Properties properties) {
    super(properties.hardnessAndResistance(1.8F).sound(SoundType.GLASS).notSolid());
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void registerClient() {
    RenderTypeLookup.setRenderLayer(this, RenderType.getCutoutMipped());
  }

  public static final int EXP_PER_BOTTLE = 11;

  @Override
  @Deprecated
  public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
    if (!world.isRemote && hand == Hand.MAIN_HAND) {
      ItemStack held = player.getHeldItem(hand);
      if (held.isEmpty() || player.isCrouching()) {
        TileExpPylon tile = (TileExpPylon) world.getTileEntity(pos);
        UtilStuff.messageStatus(player, "" + tile.getStoredXp());
        return ActionResultType.SUCCESS;
      }
      if (held.getItem() == Items.SUGAR) {
        TileExpPylon tile = (TileExpPylon) world.getTileEntity(pos);
        if (tile.drainStoredXp(ExpItemGain.EXP_PER_FOOD)) {
          //do it
          held.shrink(1);
          player.dropItem(new ItemStack(ItemRegistry.experience_food), true);
          UtilSound.playSound(player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
          return ActionResultType.SUCCESS;
        }
        else {
          UtilStuff.messageStatus(player, UtilChat.lang(getTranslationKey() + ".notenough") + " " + tile.getStoredXp() + "/" + ExpItemGain.EXP_PER_FOOD);
        }
      }
      else if (held.getItem() == Items.GLASS_BOTTLE) {
        TileExpPylon tile = (TileExpPylon) world.getTileEntity(pos);
        if (tile.drainStoredXp(EXP_PER_BOTTLE)) {
          //do it
          held.shrink(1);
          player.dropItem(new ItemStack(Items.EXPERIENCE_BOTTLE), true);
          UtilSound.playSound(player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
          return ActionResultType.SUCCESS;
        }
        else {
          UtilStuff.messageStatus(player, UtilChat.lang(getTranslationKey() + ".notenough") + " " + tile.getStoredXp() + "/" + EXP_PER_BOTTLE);
        }
      }
    }
    return ActionResultType.PASS;
  }

  @Override
  public BlockRenderType getRenderType(BlockState bs) {
    return BlockRenderType.MODEL;
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new TileExpPylon();
  }
}