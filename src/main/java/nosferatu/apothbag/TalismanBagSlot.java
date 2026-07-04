package nosferatu.apothbag;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TalismanBagSlot extends Slot {
    private final TalismanBagContainer bagContainer;
    private final int bagSlot;

    public TalismanBagSlot(TalismanBagContainer container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.bagContainer = container;
        this.bagSlot = slot;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.bagContainer.canPlaceItem(this.bagSlot, stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        return this.bagContainer.isSlotUnlocked(this.bagSlot);
    }
}
