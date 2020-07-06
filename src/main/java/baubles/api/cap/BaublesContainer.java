package baubles.api.cap;

import baubles.api.IBauble;
import baubles.common.Baubles;
import baubles.common.event.EventHandlerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaublesContainer extends ItemStackHandler implements IBaublesItemHandler {
	private final static int BAUBLE_SLOTS = 7;
	private final ItemStack[] previous = new ItemStack[BAUBLE_SLOTS];
	private final boolean[] changed = new boolean[BAUBLE_SLOTS];
	private boolean blockEvents = false;
	private final LivingEntity holder;

	public BaublesContainer(LivingEntity holder)
	{
		super(BAUBLE_SLOTS);
		this.holder = holder;
		Arrays.fill(previous, ItemStack.EMPTY);
	}

	@Override
	public void setSize(int size)
	{
		if (size != BAUBLE_SLOTS)
			Baubles.log.warn("Cannot resize baubles container");
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		LazyOptional<IBauble> opt = stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE);
		if (stack.isEmpty() || !opt.isPresent())
			return false;
		IBauble bauble = opt.orElseThrow(NullPointerException::new);
		return bauble.canEquip(holder) && bauble.getBaubleType().hasSlot(slot);
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		if (stack.isEmpty() || this.isItemValidForSlot(slot, stack)) {
			super.setStackInSlot(slot, stack);
		}
	}

	@Nonnull
	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (!this.isItemValidForSlot(slot, stack)) return stack;
		return super.insertItem(slot, stack, simulate);
	}

	@Override
	public boolean isEventBlocked() {
		return blockEvents;
	}

	@Override
	public void setEventBlock(boolean blockEvents) {
		this.blockEvents = blockEvents;
	}

	@Override
	protected void onContentsChanged(int slot)
	{
		this.changed[slot] = true;
	}

	@Override
	public void tick() {
		for (int i = 0; i < getSlots(); i++) {
			ItemStack stack = getStackInSlot(i);
			stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE)
					.ifPresent(b -> b.onWornTick(holder));
		}
		sync();
	}

	@Override
	public LivingEntity getHolder() {
		return holder;
	}

	private void sync() {
		if (!(holder instanceof ServerPlayerEntity)) {
			return;
		}

		List<PlayerEntity> receivers = null;
		for (int i = 0; i < getSlots(); i++) {
			ItemStack stack = getStackInSlot(i);
			boolean autosync = stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE).map(b -> b.willAutoSync(holder)).orElse(false);
			if (changed[i] || autosync && !ItemStack.areItemStacksEqual(stack, previous[i])) {
				if (receivers == null) {
					receivers = new ArrayList<>(((ServerWorld) holder.world).getEntityTracker().getTrackingPlayers(holder));
					receivers.add((ServerPlayerEntity) holder);
				}
				EventHandlerEntity.syncSlot((ServerPlayerEntity) holder, i, stack, receivers);
				this.changed[i] = false;
				previous[i] = stack.copy();
			}
		}
	}
}