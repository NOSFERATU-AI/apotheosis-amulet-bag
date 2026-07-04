package nosferatu.apothbag;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.item.PotionCharmItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import java.util.List;

public class TalismanBagItem extends Item {
    public static final int MAX_SLOTS = 5;
    private static final String TAG_UNLOCKED = "UnlockedSlots";
    private static final String TAG_ITEMS = "Items";

    public TalismanBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                unlockNextSlot(stack, serverPlayer);
            }
            else {
                openBag(serverPlayer, stack);
            }
        }

        return InteractionResult.SUCCESS;
    }


    // Runtime fallback for 1.21.1 NeoForge jars that still call the SRG/obfuscated item-use method.
    // The named use(...) method above is kept too, but this is the one that makes right-click work in your pack.
    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                unlockNextSlot(stack, serverPlayer);
            }
            else {
                openBag(serverPlayer, stack);
            }
        }
        return InteractionResultHolder.m_19092_(stack, level.isClientSide());
    }

    public static void openBag(ServerPlayer player, ItemStack bagStack) {
        Component title = text("Сумка амулетов");
        RegistryAccess registries = player.level().registryAccess();
        MenuProvider provider = new SimpleMenuProvider((int id, Inventory inventory, Player menuPlayer) -> {
            TalismanBagContainer container = new TalismanBagContainer(bagStack, registries);
            return new TalismanBagMenu(id, inventory, container);
        }, title);
        player.openMenu(provider);
    }

    public static boolean unlockNextSlot(ItemStack stack, ServerPlayer player) {
        int unlocked = getUnlockedSlots(stack);
        if (unlocked >= MAX_SLOTS) {
            player.displayClientMessage(text("Все слоты уже открыты"), true);
            return false;
        }

        int cost = (unlocked + 1) * 100;
        if (!player.getAbilities().instabuild && player.experienceLevel < cost) {
            player.displayClientMessage(text("Недостаточно уровней. Нужно: " + cost), true);
            return false;
        }

        if (!player.getAbilities().instabuild) {
            player.giveExperienceLevels(-cost);
        }

        setUnlockedSlots(stack, unlocked + 1);
        player.displayClientMessage(text("Открыт слот " + (unlocked + 1) + " за " + cost + " уровней"), true);
        return true;
    }

    private static CompoundTag getBagData(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.copyTag();
    }

    private static void setBagData(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getUnlockedSlots(ItemStack stack) {
        CompoundTag tag = getBagData(stack);
        int slots = tag.getInt(TAG_UNLOCKED);
        if (slots < 0) return 0;
        return Math.min(slots, MAX_SLOTS);
    }

    public static void setUnlockedSlots(ItemStack stack, int slots) {
        CompoundTag tag = getBagData(stack);
        tag.putInt(TAG_UNLOCKED, Math.max(0, Math.min(slots, MAX_SLOTS)));
        setBagData(stack, tag);
    }

    public static boolean isPotionCharm(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof PotionCharmItem && PotionCharmItem.hasEffect(stack);
    }

    public static void enableCharm(ItemStack stack) {
        if (isPotionCharm(stack)) {
            stack.set(Apoth.Components.CHARM_ENABLED, true);
        }
    }

    public static ItemStack createLockedVisual() {
        return new ItemStack((ItemLike) ApothTalismanBag.LOCKED_SLOT.get());
    }

    public static boolean isLockedVisual(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == ApothTalismanBag.LOCKED_SLOT.get();
    }

    public static void loadItems(ItemStack bag, TalismanBagContainer container, HolderLookup.Provider registries) {
        CompoundTag tag = getBagData(bag);
        ListTag list = tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot >= 0 && slot < MAX_SLOTS && entry.contains("Item", Tag.TAG_COMPOUND)) {
                ItemStack loaded = ItemStack.parseOptional(registries, entry.getCompound("Item"));
                container.setItemSilently(slot, loaded);
            }
        }
    }

    public static void saveItems(ItemStack bag, TalismanBagContainer container, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        int unlocked = getUnlockedSlots(bag);
        for (int slot = 0; slot < Math.min(unlocked, MAX_SLOTS); slot++) {
            ItemStack stack = container.getItem(slot);
            if (isPotionCharm(stack)) {
                CompoundTag entry = new CompoundTag();
                entry.putByte("Slot", (byte) slot);
                // MC 1.21.1 ItemStack#save returns Tag, not CompoundTag. Store that tag directly.
                entry.put("Item", stack.save(registries, new CompoundTag()));
                // ListTag#add(Tag) does not exist in MC 1.21.1; use addTag(index, tag) instead.
                list.addTag(list.size(), entry);
            }
        }
        CompoundTag tag = getBagData(bag);
        tag.put(TAG_ITEMS, list);
        setBagData(bag, tag);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        addTooltip(stack, tooltip);
    }

    // Runtime fallback tooltip method for SRG runtime. Same parameters as 1.21.1 Item#appendHoverText, but obfuscated name.
    public void m_7373_(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        addTooltip(stack, tooltip);
    }

    private static Component text(String value) {
        try {
            Class<?> componentClass = Class.forName("net.minecraft.network.chat.Component");
            Object component = componentClass.getMethod("literal", String.class).invoke(null, value);
            return (Component) component;
        }
        catch (Throwable t) {
            throw new IllegalStateException("Cannot create Minecraft text component", t);
        }
    }

    private static void addTooltip(ItemStack stack, List<Component> tooltip) {
        int unlocked = getUnlockedSlots(stack);
        tooltip.add(text("Открыто слотов: " + unlocked + "/" + MAX_SLOTS));
        if (unlocked < MAX_SLOTS) {
            int cost = (unlocked + 1) * 100;
            tooltip.add(text("Следующий слот: " + cost + " уровней"));
        }
        tooltip.add(text("ПКМ — открыть сумку"));
        tooltip.add(text("Shift + ПКМ — открыть следующий слот"));
    }

}
