package nosferatu.apothbag;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.InteractionResult;

public class BagEvents {


    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof TalismanBagItem)) return;

        if (player.isShiftKeyDown()) {
            TalismanBagItem.unlockNextSlot(stack, player);
        }
        else {
            TalismanBagItem.openBag(player, stack);
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof TalismanBagItem) {
                tickBag(player, stack);
                return;
            }
        }

        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof TalismanBagItem) {
            tickBag(player, offhand);
        }
    }

    private static void tickBag(ServerPlayer player, ItemStack bag) {
        if (!TalismanBagItem.hasStoredItemsFast(bag)) {
            return;
        }

        RegistryAccess registries = player.level().registryAccess();
        TalismanBagContainer container = new TalismanBagContainer(bag, registries);
        int unlocked = TalismanBagItem.getUnlockedSlots(bag);
        boolean dirty = false;

        for (int i = 0; i < Math.min(unlocked, TalismanBagItem.MAX_SLOTS); i++) {
            ItemStack charm = container.getItem(i);
            if (TalismanBagItem.isPotionCharm(charm)) {
                TalismanBagItem.enableCharm(charm);
                charm.getItem().inventoryTick(charm, player.level(), player, i, false);
                dirty = true;
            }
        }

        if (dirty) {
            container.setChanged();
        }
    }
}
