package net.minecraft.world.level.material;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class Fluid {
   public static final IdMapper<FluidState> f_76104_ = new IdMapper<>();
   protected final StateDefinition<Fluid, FluidState> f_76105_;
   private FluidState f_76103_;
   private final Holder.Reference<Fluid> f_205066_ = BuiltInRegistries.f_257020_.m_203693_(this);

   protected Fluid() {
      StateDefinition.Builder<Fluid, FluidState> builder = new StateDefinition.Builder<>(this);
      this.m_7180_(builder);
      this.f_76105_ = builder.m_61101_(Fluid::m_76145_, FluidState::new);
      this.m_76142_(this.f_76105_.m_61090_());
   }

   protected void m_7180_(StateDefinition.Builder<Fluid, FluidState> p_76121_) {
   }

   public StateDefinition<Fluid, FluidState> m_76144_() {
      return this.f_76105_;
   }

   protected final void m_76142_(FluidState p_76143_) {
      this.f_76103_ = p_76143_;
   }

   public final FluidState m_76145_() {
      return this.f_76103_;
   }

   public abstract Item m_6859_();

   protected void m_213811_(Level p_230550_, BlockPos p_230551_, FluidState p_230552_, RandomSource p_230553_) {
   }

   protected void m_6292_(Level p_76113_, BlockPos p_76114_, FluidState p_76115_) {
   }

   protected void m_213812_(Level p_230554_, BlockPos p_230555_, FluidState p_230556_, RandomSource p_230557_) {
   }

   @Nullable
   protected ParticleOptions m_7792_() {
      return null;
   }

   protected abstract boolean m_5486_(FluidState p_76127_, BlockGetter p_76128_, BlockPos p_76129_, Fluid p_76130_, Direction p_76131_);

   protected abstract Vec3 m_7000_(BlockGetter p_76110_, BlockPos p_76111_, FluidState p_76112_);

   public abstract int m_6718_(LevelReader p_76120_);

   protected boolean m_6685_() {
      return false;
   }

   protected boolean m_6759_() {
      return false;
   }

   protected abstract float m_6752_();

   public abstract float m_6098_(FluidState p_76124_, BlockGetter p_76125_, BlockPos p_76126_);

   public abstract float m_7427_(FluidState p_76123_);

   protected abstract BlockState m_5804_(FluidState p_76136_);

   public abstract boolean m_7444_(FluidState p_76140_);

   public abstract int m_7430_(FluidState p_76141_);

   public boolean m_6212_(Fluid p_76122_) {
      return p_76122_ == this;
   }

   /** @deprecated */
   @Deprecated
   public boolean m_205067_(TagKey<Fluid> p_205068_) {
      return this.f_205066_.m_203656_(p_205068_);
   }

   public abstract VoxelShape m_7999_(FluidState p_76137_, BlockGetter p_76138_, BlockPos p_76139_);

   public Optional<SoundEvent> m_142520_() {
      return Optional.empty();
   }

   /** @deprecated */
   @Deprecated
   public Holder.Reference<Fluid> m_205069_() {
      return this.f_205066_;
   }
}
