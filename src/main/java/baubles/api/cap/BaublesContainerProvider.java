package baubles.api.cap;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
public class BaublesContainerProvider implements INBTSerializable<CompoundNBT>, ICapabilityProvider {
	private final BaublesContainer inner;
	private final LazyOptional<IBaublesItemHandler> opt;

	public BaublesContainerProvider(PlayerEntity player) {
		this.inner = new BaublesContainer(player);
		this.opt = LazyOptional.of(() -> inner);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
		return BaublesCapabilities.CAPABILITY_BAUBLES.orEmpty(capability, opt);
	}

	@Override
	public CompoundNBT serializeNBT () {
		return this.inner.serializeNBT();
	}

	@Override
	public void deserializeNBT (CompoundNBT nbt) {
		this.inner.deserializeNBT(nbt);
	}
}
