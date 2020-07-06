package baubles.common.container;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
// Mostly based off of ContainerPlayer, without recipe book stuff and with Baubles
public class ContainerPlayerExpanded extends Container {
	// [VanillaCopy] ContainerPlayer
	private static final String[] field_200829_h = new String[]{"item/empty_armor_slot_boots", "item/empty_armor_slot_leggings", "item/empty_armor_slot_chestplate", "item/empty_armor_slot_helmet"};
	private static final EquipmentSlotType[] VALID_EQUIPMENT_SLOTS = new EquipmentSlotType[]{EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET};
	public CraftingInventory craftMatrix = new CraftingInventory(this, 2, 2);
	public CraftResultInventory craftResult = new CraftResultInventory();
	public boolean isLocalWorld;
	private final PlayerEntity player;

	public final IBaublesItemHandler baubles;
	private static final ContainerType<?> Container = null;

	// [VanillaCopy] ContainerPlayer, changes as noted
	public ContainerPlayerExpanded(PlayerInventory InventoryPlayer, boolean localWorld, PlayerEntity player)
	{
		super(Container, getDragEvent(0));
		this.isLocalWorld = localWorld;
		this.player = player;
		baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES).orElseThrow(NullPointerException::new);

		this.addSlot(new CraftingResultSlot(InventoryPlayer.player, this.craftMatrix, this.craftResult, 0, 154, 28));

		for(int i = 0; i < 2; ++i) {
			for(int j = 0; j < 2; ++j) {
				// Baubles: shift crafting grid right
				this.addSlot(new Slot(this.craftMatrix, j + i * 2, 116 + j * 18, 18 + i * 18));
			}
		}

		for(int k = 0; k < 4; ++k) {
			final EquipmentSlotType EquipmentSlotType = VALID_EQUIPMENT_SLOTS[k];
			this.addSlot(new Slot(InventoryPlayer, 39 - k, 8, 8 + k * 18) {
				@Override
				public int getSlotStackLimit() {
					return 1;
				}

				@Override
				public boolean isItemValid(ItemStack stack) {
					return stack.canEquip(EquipmentSlotType, player);
				}

				@Override
				public boolean canTakeStack(PlayerEntity playerIn) {
					ItemStack itemstack = this.getStack();
					return !itemstack.isEmpty() && !playerIn.isCreative()
							&& EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.canTakeStack(playerIn);
				}

				@Nullable
				@OnlyIn(Dist.CLIENT)
				@Override
				public String getSlotTexture() {
					return ContainerPlayerExpanded.field_200829_h[EquipmentSlotType.getIndex()];
				}
			});
		}

		// Baubles: add baubles slots
		this.addSlot(new SlotBauble(baubles,0,77,8 ));
		this.addSlot(new SlotBauble(baubles,1,77,8 + 1 * 18));
		this.addSlot(new SlotBauble(baubles,2,77,8 + 2 * 18));
		this.addSlot(new SlotBauble(baubles,3,77,8 + 3 * 18));
		this.addSlot(new SlotBauble(baubles,4,96,8 ));
		this.addSlot(new SlotBauble(baubles,5,96,8 + 1 * 18));
		this.addSlot(new SlotBauble(baubles,6,96,8 + 2 * 18));

		for(int l = 0; l < 3; ++l) {
			for(int j1 = 0; j1 < 9; ++j1) {
				this.addSlot(new Slot(InventoryPlayer, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
			}
		}

		for(int i1 = 0; i1 < 9; ++i1) {
			this.addSlot(new Slot(InventoryPlayer, i1, 8 + i1 * 18, 142));
		}

		// Baubles: move shield slot right
		this.addSlot(new Slot(InventoryPlayer, 40, 96, 62) {
			@Nullable
			@OnlyIn(Dist.CLIENT)
			public String getSlotTexture() {
				return "item/empty_armor_slot_shield";
			}
		});

	}

	@Override
	public void onCraftMatrixChanged(IInventory par1IInventory) {
		this.slotChangedCraftingGrid(this.player.getEntityWorld(), this.player, this.craftMatrix, this.craftResult);
	}

	@Override
	public void onContainerClosed(PlayerEntity player) {
		super.onContainerClosed(player);
		this.craftResult.clear();

		if (!player.world.isRemote) {
			this.clearContainer(player, player.world, this.craftMatrix);
		}
	}

	@Override
	public boolean canInteractWith(PlayerEntity player) {
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) 
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			EquipmentSlotType EquipmentSlotType = EntityLiving.getSlotForItemStack(itemstack);

			int slotShift = baubles.getSlots();

			if (index == 0) {
				if (!this.mergeItemStack(itemstack1, 9 + slotShift, 45 + slotShift, true)) {
					return ItemStack.EMPTY;
				}

				slot.onSlotChange(itemstack1, itemstack);
			} else if (index >= 1 && index < 5) {
				if (!this.mergeItemStack(itemstack1, 9 + slotShift, 45 + slotShift, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 5 && index < 9) {
				if (!this.mergeItemStack(itemstack1, 9 + slotShift, 45 + slotShift, false)) {
					return ItemStack.EMPTY;
				}
			}

			// baubles -> inv
			else if (index >= 9 && index < 9 + slotShift) {
				if (!this.mergeItemStack(itemstack1, 9 + slotShift, 45 + slotShift, false)) {
					return ItemStack.EMPTY;
				}
			}

			// inv -> armor
			else if (EquipmentSlotType.getSlotType() == EquipmentSlotType.Type.ARMOR
					&& !this.inventorySlots.get(8 - EquipmentSlotType.getIndex()).getHasStack()) {
				int i = 8 - EquipmentSlotType.getIndex();

				if (!this.mergeItemStack(itemstack1, i, i + 1, false)) {
					return ItemStack.EMPTY;
				}
			}

			// inv -> offhand
			else if (EquipmentSlotType == EquipmentSlotType.OFFHAND
					&& !this.inventorySlots.get(45 + slotShift).getHasStack()) {
				if (!this.mergeItemStack(itemstack1, 45 + slotShift, 46 + slotShift, false)) {
					return ItemStack.EMPTY;
				}
			}
			// inv -> bauble
			else if (itemstack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE).isPresent()) {
				IBauble bauble = itemstack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE).orElseThrow(NullPointerException::new);
				for (int baubleSlot : bauble.getBaubleType().getValidSlots()) {
					if ( bauble.canEquip(playerIn) && !this.inventorySlots.get(baubleSlot+9).getHasStack()
							&& !this.mergeItemStack(itemstack1, baubleSlot + 9, baubleSlot + 10, false)) {
						return ItemStack.EMPTY;
					}
					if (itemstack1.getCount() == 0)
						break;
				}
			} else if (index >= 9 + slotShift && index < 36 + slotShift) {
				if (!this.mergeItemStack(itemstack1, 36 + slotShift, 45 + slotShift, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 36 + slotShift && index < 45 + slotShift) {
				if (!this.mergeItemStack(itemstack1, 9 + slotShift, 36 + slotShift, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 9 + slotShift, 45 + slotShift, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty() && !baubles.isEventBlocked() && slot instanceof SlotBauble) {
				itemstack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE)
						.ifPresent(b -> b.onUnequipped(playerIn));
			}

			ItemStack itemstack2 = slot.onTake(playerIn, itemstack1);

			if (index == 0) {
				playerIn.dropItem(itemstack2, false);
			}
		}

		return itemstack;
	}

	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slot) {
		return slot.inventory != this.craftResult && super.canMergeSlot(stack, slot);
	}
}
