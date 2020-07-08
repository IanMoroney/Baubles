package baubles.client.gui;

import baubles.common.Baubles;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Baubles.MODID, value = Dist.CLIENT)
public class GuiEvents {

	@SubscribeEvent
	public static void guiPostInit(GuiScreenEvent.InitGuiEvent.Post event) {
		if (event.getGui() instanceof InventoryScreen) {
			ContainerScreen gui = (ContainerScreen) event.getGui();
			event.addButton(new GuiBaublesButton(55, gui, 64, 9, 10, 10,
					I18n.format("button.baubles")));
		}
	}
}
