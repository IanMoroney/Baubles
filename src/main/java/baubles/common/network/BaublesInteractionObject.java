package baubles.common.network;

import baubles.common.Baubles;
import baubles.common.container.ContainerPlayerExpanded;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BaublesInteractionObject implements INamedContainerProvider {
    public static final ResourceLocation ID = new ResourceLocation(Baubles.MODID, "container");

    @Nonnull
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity player) {
        return new ContainerPlayerExpanded(playerInventory, !player.world.isRemote, player);
    }

    @Nonnull
    @Override
    public String getGuiID() {
        return ID.toString();
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(getGuiID());
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return null;
    }
}