package nosferatu.apothbag;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ApothTalismanBag.MODID)
public class ApothTalismanBag {
    public static final String MODID = "apoth_talisman_bag";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<TalismanBagItem> TALISMAN_BAG = ITEMS.registerItem(
            "talisman_bag",
            TalismanBagItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<Item> LOCKED_SLOT = ITEMS.registerSimpleItem(
            "locked_slot",
            new Item.Properties().stacksTo(1)
    );

    public ApothTalismanBag(IEventBus modBus, ModContainer modContainer) {
        ITEMS.register(modBus);
        NeoForge.EVENT_BUS.addListener(BagEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(BagEvents::onRightClickItem);
    }
}
