package net.minecraft.world.level.material;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class FlowingFluid extends Fluid {
   public static final BooleanProperty f_75947_ = BlockStateProperties.f_61434_;
   public static final IntegerProperty f_75948_ = BlockStateProperties.f_61420_;
   private static final int f_164507_ = 200;
   private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> f_75949_ = ThreadLocal.withInitial(() -> {
      Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(200) {
         protected void rehash(int p_76102_) {
         }
      };
      object2bytelinkedopenhashmap.defaultReturnValue((byte)127);
      return object2bytelinkedopenhashmap;
   });
   private final Map<FluidState, VoxelShape> f_75950_ = Maps.newIdentityHashMap();

   protected void m_7180_(StateDefinition.Builder<Fluid, FluidState> p_76046_) {
      p_76046_.m_61104_(f_75947_);
   }

   public Vec3 m_7000_(BlockGetter p_75987_, BlockPos p_75988_, FluidState p_75989_) {
      double d0 = 0.0D;
      double d1 = 0.0D;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         blockpos$mutableblockpos.m_122159_(p_75988_, direction);
         FluidState fluidstate = p_75987_.m_6425_(blockpos$mutableblockpos);
         if (this.m_76094_(fluidstate)) {
            float f = fluidstate.m_76182_();
            float f1 = 0.0F;
            if (f == 0.0F) {
               if (!p_75987_.m_8055_(blockpos$mutableblockpos).m_280555_()) {
                  BlockPos blockpos = blockpos$mutableblockpos.m_7495_();
                  FluidState fluidstate1 = p_75987_.m_6425_(blockpos);
                  if (this.m_76094_(fluidstate1)) {
                     f = fluidstate1.m_76182_();
                     if (f > 0.0F) {
                        f1 = p_75989_.m_76182_() - (f - 0.8888889F);
                     }
                  }
               }
            } else if (f > 0.0F) {
               f1 = p_75989_.m_76182_() - f;
            }

            if (f1 != 0.0F) {
               d0 += (double)((float)direction.m_122429_() * f1);
               d1 += (double)((float)direction.m_122431_() * f1);
            }
         }
      }

      Vec3 vec3 = new Vec3(d0, 0.0D, d1);
      if (p_75989_.m_61143_(f_75947_)) {
         for(Direction direction1 : Direction.Plane.HORIZONTAL) {
            blockpos$mutableblockpos.m_122159_(p_75988_, direction1);
            if (this.m_75990_(p_75987_, blockpos$mutableblockpos, direction1) || this.m_75990_(p_75987_, blockpos$mutableblockpos.m_7494_(), direction1)) {
               vec3 = vec3.m_82541_().m_82520_(0.0D, -6.0D, 0.0D);
               break;
            }
         }
      }

      return vec3.m_82541_();
   }

   private boolean m_76094_(FluidState p_76095_) {
      return p_76095_.m_76178_() || p_76095_.m_76152_().m_6212_(this);
   }

   protected boolean m_75990_(BlockGetter p_75991_, BlockPos p_75992_, Direction p_75993_) {
      BlockState blockstate = p_75991_.m_8055_(p_75992_);
      FluidState fluidstate = p_75991_.m_6425_(p_75992_);
      if (fluidstate.m_76152_().m_6212_(this)) {
         return false;
      } else if (p_75993_ == Direction.UP) {
         return true;
      } else {
         return blockstate.m_60734_() instanceof IceBlock ? false : blockstate.m_60783_(p_75991_, p_75992_, p_75993_);
      }
   }

   protected void m_76010_(Level p_255851_, BlockPos p_76012_, FluidState p_76013_) {
      if (!p_76013_.m_76178_()) {
         BlockState blockstate = p_255851_.m_8055_(p_76012_);
         BlockPos blockpos = p_76012_.m_7495_();
         BlockState blockstate1 = p_255851_.m_8055_(blockpos);
         FluidState fluidstate = this.m_76035_(p_255851_, blockpos, blockstate1);
         if (this.m_75977_(p_255851_, p_76012_, blockstate, Direction.DOWN, blockpos, blockstate1, p_255851_.m_6425_(blockpos), fluidstate.m_76152_())) {
            this.m_6364_(p_255851_, blockpos, blockstate1, Direction.DOWN, fluidstate);
            if (this.m_76019_(p_255851_, p_76012_) >= 3) {
               this.m_76014_(p_255851_, p_76012_, p_76013_, blockstate);
            }
         } else if (p_76013_.m_76170_() || !this.m_75956_(p_255851_, fluidstate.m_76152_(), p_76012_, blockstate, blockpos, blockstate1)) {
            this.m_76014_(p_255851_, p_76012_, p_76013_, blockstate);
         }

      }
   }

   private void m_76014_(Level p_256644_, BlockPos p_76016_, FluidState p_76017_, BlockState p_76018_) {
      int i = p_76017_.m_76186_() - this.m_6713_(p_256644_);
      if (p_76017_.m_61143_(f_75947_)) {
         i = 7;
      }

      if (i > 0) {
         Map<Direction, FluidState> map = this.m_76079_(p_256644_, p_76016_, p_76018_);

         for(Map.Entry<Direction, FluidState> entry : map.entrySet()) {
            Direction direction = entry.getKey();
            FluidState fluidstate = entry.getValue();
            BlockPos blockpos = p_76016_.m_121945_(direction);
            BlockState blockstate = p_256644_.m_8055_(blockpos);
            if (this.m_75977_(p_256644_, p_76016_, p_76018_, direction, blockpos, blockstate, p_256644_.m_6425_(blockpos), fluidstate.m_76152_())) {
               this.m_6364_(p_256644_, blockpos, blockstate, direction, fluidstate);
            }
         }

      }
   }

   protected FluidState m_76035_(Level p_256464_, BlockPos p_76037_, BlockState p_76038_) {
      int i = 0;
      int j = 0;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = p_76037_.m_121945_(direction);
         BlockState blockstate = p_256464_.m_8055_(blockpos);
         FluidState fluidstate = blockstate.m_60819_();
         if (fluidstate.m_76152_().m_6212_(this) && this.m_76061_(direction, p_256464_, p_76037_, p_76038_, blockpos, blockstate)) {
            if (fluidstate.m_76170_()) {
               ++j;
            }

            i = Math.max(i, fluidstate.m_76186_());
         }
      }

      if (this.m_6760_(p_256464_) && j >= 2) {
         BlockState blockstate1 = p_256464_.m_8055_(p_76037_.m_7495_());
         FluidState fluidstate1 = blockstate1.m_60819_();
         if (blockstate1.m_280296_() || this.m_76096_(fluidstate1)) {
            return this.m_76068_(false);
         }
      }

      BlockPos blockpos1 = p_76037_.m_7494_();
      BlockState blockstate2 = p_256464_.m_8055_(blockpos1);
      FluidState fluidstate2 = blockstate2.m_60819_();
      if (!fluidstate2.m_76178_() && fluidstate2.m_76152_().m_6212_(this) && this.m_76061_(Direction.UP, p_256464_, p_76037_, p_76038_, blockpos1, blockstate2)) {
         return this.m_75953_(8, true);
      } else {
         int k = i - this.m_6713_(p_256464_);
         return k <= 0 ? Fluids.f_76191_.m_76145_() : this.m_75953_(k, false);
      }
   }

   private boolean m_76061_(Direction p_76062_, BlockGetter p_76063_, BlockPos p_76064_, BlockState p_76065_, BlockPos p_76066_, BlockState p_76067_) {
      Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap;
      if (!p_76065_.m_60734_().m_49967_() && !p_76067_.m_60734_().m_49967_()) {
         object2bytelinkedopenhashmap = f_75949_.get();
      } else {
         object2bytelinkedopenhashmap = null;
      }

      Block.BlockStatePairKey block$blockstatepairkey;
      if (object2bytelinkedopenhashmap != null) {
         block$blockstatepairkey = new Block.BlockStatePairKey(p_76065_, p_76067_, p_76062_);
         byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(block$blockstatepairkey);
         if (b0 != 127) {
            return b0 != 0;
         }
      } else {
         block$blockstatepairkey = null;
      }

      VoxelShape voxelshape1 = p_76065_.m_60812_(p_76063_, p_76064_);
      VoxelShape voxelshape = p_76067_.m_60812_(p_76063_, p_76066_);
      boolean flag = !Shapes.m_83152_(voxelshape1, voxelshape, p_76062_);
      if (object2bytelinkedopenhashmap != null) {
         if (object2bytelinkedopenhashmap.size() == 200) {
            object2bytelinkedopenhashmap.removeLastByte();
         }

         object2bytelinkedopenhashmap.putAndMoveToFirst(block$blockstatepairkey, (byte)(flag ? 1 : 0));
      }

      return flag;
   }

   public abstract Fluid m_5615_();

   public FluidState m_75953_(int p_75954_, boolean p_75955_) {
      return this.m_5615_().m_76145_().m_61124_(f_75948_, Integer.valueOf(p_75954_)).m_61124_(f_75947_, Boolean.valueOf(p_75955_));
   }

   public abstract Fluid m_5613_();

   public FluidState m_76068_(boolean p_76069_) {
      return this.m_5613_().m_76145_().m_61124_(f_75947_, Boolean.valueOf(p_76069_));
   }

   protected abstract boolean m_6760_(Level p_256009_);

   protected void m_6364_(LevelAccessor p_76005_, BlockPos p_76006_, BlockState p_76007_, Direction p_76008_, FluidState p_76009_) {
      if (p_76007_.m_60734_() instanceof LiquidBlockContainer) {
         ((LiquidBlockContainer)p_76007_.m_60734_()).m_7361_(p_76005_, p_76006_, p_76007_, p_76009_);
      } else {
         if (!p_76007_.m_60795_()) {
            this.m_7456_(p_76005_, p_76006_, p_76007_);
         }

         p_76005_.m_7731_(p_76006_, p_76009_.m_76188_(), 3);
      }

   }

   protected abstract void m_7456_(LevelAccessor p_76002_, BlockPos p_76003_, BlockState p_76004_);

   private static short m_76058_(BlockPos p_76059_, BlockPos p_76060_) {
      int i = p_76060_.m_123341_() - p_76059_.m_123341_();
      int j = p_76060_.m_123343_() - p_76059_.m_123343_();
      return (short)((i + 128 & 255) << 8 | j + 128 & 255);
   }

   protected int m_76026_(LevelReader p_76027_, BlockPos p_76028_, int p_76029_, Direction p_76030_, BlockState p_76031_, BlockPos p_76032_, Short2ObjectMap<Pair<BlockState, FluidState>> p_76033_, Short2BooleanMap p_76034_) {
      int i = 1000;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (direction != p_76030_) {
            BlockPos blockpos = p_76028_.m_121945_(direction);
            short short1 = m_76058_(p_76032_, blockpos);
            Pair<BlockState, FluidState> pair = p_76033_.computeIfAbsent(short1, (p_284932_) -> {
               BlockState blockstate1 = p_76027_.m_8055_(blockpos);
               return Pair.of(blockstate1, blockstate1.m_60819_());
            });
            BlockState blockstate = pair.getFirst();
            FluidState fluidstate = pair.getSecond();
            if (this.m_75963_(p_76027_, this.m_5615_(), p_76028_, p_76031_, direction, blockpos, blockstate, fluidstate)) {
               boolean flag = p_76034_.computeIfAbsent(short1, (p_192912_) -> {
                  BlockPos blockpos1 = blockpos.m_7495_();
                  BlockState blockstate1 = p_76027_.m_8055_(blockpos1);
                  return this.m_75956_(p_76027_, this.m_5615_(), blockpos, blockstate, blockpos1, blockstate1);
               });
               if (flag) {
                  return p_76029_;
               }

               if (p_76029_ < this.m_6719_(p_76027_)) {
                  int j = this.m_76026_(p_76027_, blockpos, p_76029_ + 1, direction.m_122424_(), blockstate, p_76032_, p_76033_, p_76034_);
                  if (j < i) {
                     i = j;
                  }
               }
            }
         }
      }

      return i;
   }

   private boolean m_75956_(BlockGetter p_75957_, Fluid p_75958_, BlockPos p_75959_, BlockState p_75960_, BlockPos p_75961_, BlockState p_75962_) {
      if (!this.m_76061_(Direction.DOWN, p_75957_, p_75959_, p_75960_, p_75961_, p_75962_)) {
         return false;
      } else {
         return p_75962_.m_60819_().m_76152_().m_6212_(this) ? true : this.m_75972_(p_75957_, p_75961_, p_75962_, p_75958_);
      }
   }

   private boolean m_75963_(BlockGetter p_75964_, Fluid p_75965_, BlockPos p_75966_, BlockState p_75967_, Direction p_75968_, BlockPos p_75969_, BlockState p_75970_, FluidState p_75971_) {
      return !this.m_76096_(p_75971_) && this.m_76061_(p_75968_, p_75964_, p_75966_, p_75967_, p_75969_, p_75970_) && this.m_75972_(p_75964_, p_75969_, p_75970_, p_75965_);
   }

   private boolean m_76096_(FluidState p_76097_) {
      return p_76097_.m_76152_().m_6212_(this) && p_76097_.m_76170_();
   }

   protected abstract int m_6719_(LevelReader p_76074_);

   private int m_76019_(LevelReader p_76020_, BlockPos p_76021_) {
      int i = 0;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = p_76021_.m_121945_(direction);
         FluidState fluidstate = p_76020_.m_6425_(blockpos);
         if (this.m_76096_(fluidstate)) {
            ++i;
         }
      }

      return i;
   }

   protected Map<Direction, FluidState> m_76079_(Level p_256191_, BlockPos p_76081_, BlockState p_76082_) {
      int i = 1000;
      Map<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
      Short2ObjectMap<Pair<BlockState, FluidState>> short2objectmap = new Short2ObjectOpenHashMap<>();
      Short2BooleanMap short2booleanmap = new Short2BooleanOpenHashMap();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = p_76081_.m_121945_(direction);
         short short1 = m_76058_(p_76081_, blockpos);
         Pair<BlockState, FluidState> pair = short2objectmap.computeIfAbsent(short1, (p_284929_) -> {
            BlockState blockstate1 = p_256191_.m_8055_(blockpos);
            return Pair.of(blockstate1, blockstate1.m_60819_());
         });
         BlockState blockstate = pair.getFirst();
         FluidState fluidstate = pair.getSecond();
         FluidState fluidstate1 = this.m_76035_(p_256191_, blockpos, blockstate);
         if (this.m_75963_(p_256191_, fluidstate1.m_76152_(), p_76081_, p_76082_, direction, blockpos, blockstate, fluidstate)) {
            BlockPos blockpos1 = blockpos.m_7495_();
            boolean flag = short2booleanmap.computeIfAbsent(short1, (p_255612_) -> {
               BlockState blockstate1 = p_256191_.m_8055_(blockpos1);
               return this.m_75956_(p_256191_, this.m_5615_(), blockpos, blockstate, blockpos1, blockstate1);
            });
            int j;
            if (flag) {
               j = 0;
            } else {
               j = this.m_76026_(p_256191_, blockpos, 1, direction.m_122424_(), blockstate, p_76081_, short2objectmap, short2booleanmap);
            }

            if (j < i) {
               map.clear();
            }

            if (j <= i) {
               map.put(direction, fluidstate1);
               i = j;
            }
         }
      }

      return map;
   }

   private boolean m_75972_(BlockGetter p_75973_, BlockPos p_75974_, BlockState p_75975_, Fluid p_75976_) {
      Block block = p_75975_.m_60734_();
      if (block instanceof LiquidBlockContainer) {
         return ((LiquidBlockContainer)block).m_6044_(p_75973_, p_75974_, p_75975_, p_75976_);
      } else if (!(block instanceof DoorBlock) && !p_75975_.m_204336_(BlockTags.f_13068_) && !p_75975_.m_60713_(Blocks.f_50155_) && !p_75975_.m_60713_(Blocks.f_50130_) && !p_75975_.m_60713_(Blocks.f_50628_)) {
         if (!p_75975_.m_60713_(Blocks.f_50142_) && !p_75975_.m_60713_(Blocks.f_50257_) && !p_75975_.m_60713_(Blocks.f_50446_) && !p_75975_.m_60713_(Blocks.f_50454_)) {
            return !p_75975_.m_280555_();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   protected boolean m_75977_(BlockGetter p_75978_, BlockPos p_75979_, BlockState p_75980_, Direction p_75981_, BlockPos p_75982_, BlockState p_75983_, FluidState p_75984_, Fluid p_75985_) {
      return p_75984_.m_76158_(p_75978_, p_75982_, p_75985_, p_75981_) && this.m_76061_(p_75981_, p_75978_, p_75979_, p_75980_, p_75982_, p_75983_) && this.m_75972_(p_75978_, p_75982_, p_75983_, p_75985_);
   }

   protected abstract int m_6713_(LevelReader p_76087_);

   protected int m_6886_(Level p_75998_, BlockPos p_75999_, FluidState p_76000_, FluidState p_76001_) {
      return this.m_6718_(p_75998_);
   }

   public void m_6292_(Level p_75995_, BlockPos p_75996_, FluidState p_75997_) {
      if (!p_75997_.m_76170_()) {
         FluidState fluidstate = this.m_76035_(p_75995_, p_75996_, p_75995_.m_8055_(p_75996_));
         int i = this.m_6886_(p_75995_, p_75996_, p_75997_, fluidstate);
         if (fluidstate.m_76178_()) {
            p_75997_ = fluidstate;
            p_75995_.m_7731_(p_75996_, Blocks.f_50016_.m_49966_(), 3);
         } else if (!fluidstate.equals(p_75997_)) {
            p_75997_ = fluidstate;
            BlockState blockstate = fluidstate.m_76188_();
            p_75995_.m_7731_(p_75996_, blockstate, 2);
            p_75995_.m_186469_(p_75996_, fluidstate.m_76152_(), i);
            p_75995_.m_46672_(p_75996_, blockstate.m_60734_());
         }
      }

      this.m_76010_(p_75995_, p_75996_, p_75997_);
   }

   protected static int m_76092_(FluidState p_76093_) {
      return p_76093_.m_76170_() ? 0 : 8 - Math.min(p_76093_.m_76186_(), 8) + (p_76093_.m_61143_(f_75947_) ? 8 : 0);
   }

   private static boolean m_76088_(FluidState p_76089_, BlockGetter p_76090_, BlockPos p_76091_) {
      return p_76089_.m_76152_().m_6212_(p_76090_.m_6425_(p_76091_.m_7494_()).m_76152_());
   }

   public float m_6098_(FluidState p_76050_, BlockGetter p_76051_, BlockPos p_76052_) {
      return m_76088_(p_76050_, p_76051_, p_76052_) ? 1.0F : p_76050_.m_76182_();
   }

   public float m_7427_(FluidState p_76048_) {
      return (float)p_76048_.m_76186_() / 9.0F;
   }

   public abstract int m_7430_(FluidState p_164509_);

   public VoxelShape m_7999_(FluidState p_76084_, BlockGetter p_76085_, BlockPos p_76086_) {
      return p_76084_.m_76186_() == 9 && m_76088_(p_76084_, p_76085_, p_76086_) ? Shapes.m_83144_() : this.f_75950_.computeIfAbsent(p_76084_, (p_76073_) -> {
         return Shapes.m_83048_(0.0D, 0.0D, 0.0D, 1.0D, (double)p_76073_.m_76155_(p_76085_, p_76086_), 1.0D);
      });
   }
}
