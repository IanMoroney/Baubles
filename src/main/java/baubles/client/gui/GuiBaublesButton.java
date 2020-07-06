package baubles.client.gui;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.opengl.GL11;

import baubles.common.network.PacketHandler;
import baubles.common.network.PacketOpenBaublesInventory;
import baubles.common.network.PacketOpenNormalInventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.container.Container;

public class GuiBaublesButton extends Button {

	private final ContainerScreen parentGui;

	public GuiBaublesButton(int buttonId, ContainerScreen parentGui, int x, int y, int width, int height, String buttonText) {
		super(buttonId, x + parentGui.getGuiLeft(), parentGui.getGuiTop() + y, width, height, buttonText);
		this.parentGui = parentGui;
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		if (parentGui instanceof InventoryScreen) {
			PacketHandler.INSTANCE.sendToServer(new PacketOpenBaublesInventory());
		} else {
			PacketHandler.INSTANCE.sendToServer(new PacketOpenNormalInventory());
			this.displayNormalInventory(mouseX, mouseY);
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		if (this.visible)
		{
			int x = this.x + this.parentGui.getGuiLeft();

			FontRenderer fontrenderer = Minecraft.getInstance().fontRenderer;
			Minecraft.getInstance().getTextureManager().bindTexture(GuiPlayerExpanded.background);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= x && mouseY >= this.y && mouseX < x + this.width && mouseY < this.y + this.height;
			int k = this.getHoverState(this.hovered);
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(770, 771, 1, 0);
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			RenderSystem.pushMatrix();
			RenderSystem.translatef(0, 0, 200);
			if (k==1) {
				this.drawTexturedModalRect(x, this.y, 200, 48, 10, 10);
			} else {
				this.drawTexturedModalRect(x, this.y, 210, 48, 10, 10);
				this.drawCenteredString(fontrenderer, I18n.format(this.displayString), x + 5, this.y + this.height, 0xffffff);
			}
			RenderSystem.popMatrix();
		}
	}

	private void displayNormalInventory(double oldMouseX, double oldMouseY) {
		InventoryScreen gui = new InventoryScreen(Minecraft.getInstance().player);
		ObfuscationReflectionHelper.setPrivateValue(InventoryScreen.class, gui, (float) oldMouseX, "field_147048_u");
		ObfuscationReflectionHelper.setPrivateValue(InventoryScreen.class, gui, (float) oldMouseY, "field_147047_v");
		Minecraft.getInstance().displayGuiScreen(gui);
}
