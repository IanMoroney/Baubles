package baubles.client;

import baubles.api.cap.BaublesCapabilities;
import baubles.common.Baubles;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import baubles.api.BaubleType;
import baubles.common.network.PacketHandler;
import baubles.common.network.PacketOpenBaublesInventory;
import java.util.List;

@Mod.EventBusSubscriber(modid = Baubles.MODID, value = Dist.CLIENT)
public class ClientEventHandler
{
	@SubscribeEvent
	public static void playerTick(TickEvent.ClientTickEvent event) {
		if (event.phase == Phase.START) {
			if (Baubles.ClientInit.KEY_BAUBLES.isPressed() && Minecraft.getInstance().isGameFocused()) {
					PacketHandler.INSTANCE.sendToServer(new PacketOpenBaublesInventory());
			}
		}
	}

	@SubscribeEvent
	public static void tooltipEvent(ItemTooltipEvent event, List<ITextComponent> name) {
		if (!event.getItemStack().isEmpty()) {
			event.getItemStack().getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE).ifPresent(bauble -> {
				BaubleType bt = bauble.getBaubleType();
				ITextComponent text = new TranslationTextComponent("name." + bt);
				name.add(new StringTextComponent("").func_230529_a_(text).func_240699_a_(TextFormatting.GOLD));
				event.getToolTip().add(text);
			});
		}
	}
}
