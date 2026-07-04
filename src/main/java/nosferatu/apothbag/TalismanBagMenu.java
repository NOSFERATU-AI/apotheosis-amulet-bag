package nosferatu.apothbag;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TalismanBagMenu extends AbstractContainerMenu {
    private static final int BAG_SLOTS = TalismanBagItem.MAX_SLOTS;
    private static final int PLAYER_INV_START = BAG_SLOTS;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final TalismanBagContainer container;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public TalismanBagMenu(int id, Inventory playerInventory, TalismanBagContainer container) {
        super((MenuType) MenuType.HOPPER, id);
        this.container = container;

        for (int i = 0; i < BAG_SLOTS; i++) {
            this.addSlot(new TalismanBagSlot(container, i, 44 + i * 18, 20));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        // Do not touch AbstractContainerMenu.slots directly here. In the real 1.21.1 runtime
        // that field is NonNullList, while stub builds can easily bake the wrong descriptor.
        // getSlot(int) has a stable runtime method descriptor.
        if (index < 0 || index >= HOTBAR_END) return result;

        Slot slot = this.getSlot(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index < BAG_SLOTS) {
                if (!this.container.isSlotUnlocked(index)) {
                    return ItemStack.EMPTY;
                }
                if (!this.moveItemStackTo(stackInSlot, PLAYER_INV_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            }
            else {
                if (!TalismanBagItem.isPotionCharm(stackInSlot)) {
                    return ItemStack.EMPTY;
                }
                if (!this.moveItemStackTo(stackInSlot, 0, BAG_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }
            else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.setChanged();
    }
}
