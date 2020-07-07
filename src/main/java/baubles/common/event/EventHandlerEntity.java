package baubles.common.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import baubles.api.cap.BaublesCapabilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.TickEvent;
import baubles.api.BaublesApi;
import baubles.api.cap.BaublesContainer;
import baubles.api.cap.BaublesContainerProvider;
import baubles.api.cap.IBaublesItemHandler;
import baubles.common.Baubles;
import baubles.common.network.PacketHandler;
import baubles.common.network.PacketSync;
import net.minecraftforge.fml.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = Baubles.MODID)
public class EventHandlerEntity {

	@SubscribeEvent
	public static void cloneCapabilitiesEvent(PlayerEvent.Clone event)
	{
		try {
			event.getOriginal().getCapability(BaublesCapabilities.CAPABILITY_BAUBLES).ifPresent(bco -> {
				CompoundNBT nbt = ((BaublesContainer) bco).serializeNBT();
				event.getEntityLiving().getCapability(BaublesCapabilities.CAPABILITY_BAUBLES).ifPresent(bcn -> {
					((BaublesContainer) bcn).deserializeNBT(nbt);
				});
			});
		} catch (Exception e) {
			Baubles.log.error("Could not clone player ["+event.getOriginal().getName()+"] baubles when changing dimensions");
		}
	}

	@SubscribeEvent
	public static void attachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof PlayerEntity) {
			event.addCapability(new ResourceLocation(Baubles.MODID,"container"),
					new BaublesContainerProvider((PlayerEntity) event.getObject()));
		}
	}

	@SubscribeEvent
	public static void playerJoin(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) entity;
			syncSlots(player, Collections.singletonList(player));
		}
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		Entity target = event.getTarget();
		if (target instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) target;
			syncSlots(player, Collections.singletonList(player));
		}
	}

	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			PlayerEntity player = event.player;
			player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES).ifPresent(IBaublesItemHandler::tick);
		}
	}

	private static void syncSlots(PlayerEntity player, Collection<? extends PlayerEntity> receivers) {
		player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES).ifPresent(baubles -> {
			for (int i = 0; i < baubles.getSlots(); i++) {
				syncSlot(player, i, baubles.getStackInSlot(i), receivers);
			}
		});
	}

	public static void syncSlot(PlayerEntity player, int slot, ItemStack stack, Collection<? extends PlayerEntity> receivers) {
		PacketSync pkt = new PacketSync(player, slot, stack);
		for (PlayerEntity receiver : receivers) {
			PacketHandler.INSTANCE.sendTo(pkt, ((ServerPlayerEntity) receiver).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
		}
	}

	@SubscribeEvent
	public static void playerDeath(LivingDropsEvent event) {
		if (event.getEntity() instanceof PlayerEntity
				&& !event.getEntity().world.isRemote
				&& !event.getEntity().world.getGameRules().getBoolean("keepInventory")) {
			dropItemsAt(event.getEntityLiving(),event.getDrops());
		}
	}

	private static void dropItemsAt(PlayerEntity player, Collection<ItemEntity> drops) {
		player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES).ifPresent(baubles -> {
			for (int i = 0; i < baubles.getSlots(); ++i) {
				if (!baubles.getStackInSlot(i).isEmpty()) {
					ItemEntity ei = new ItemEntity(player.world,
							player.chasingPosX, player.chasingPosY + player.getEyeHeight(), player.chasingPosZ,
							baubles.getStackInSlot(i).copy());
					ei.setPickupDelay(40);
					drops.add(ei);
					baubles.setStackInSlot(i, ItemStack.EMPTY);
				}
			}
		});
	}
}
