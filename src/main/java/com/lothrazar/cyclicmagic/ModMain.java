package com.lothrazar.cyclicmagic;
import com.lothrazar.cyclicmagic.gui.ModGuiHandler;
import com.lothrazar.cyclicmagic.proxy.CommonProxy;
import com.lothrazar.cyclicmagic.registry.*;
import com.lothrazar.cyclicmagic.registry.CapabilityRegistry.IPlayerExtendedProperties;
import com.lothrazar.cyclicmagic.util.Const;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(modid = Const.MODID, useMetadata = true, canBeDeactivated = false, updateJSON = "https://raw.githubusercontent.com/PrinceOfAmber/CyclicMagic/master/update.json", acceptableRemoteVersions = "*", guiFactory = "com.lothrazar." + Const.MODID + ".gui.IngameConfigFactory")
public class ModMain {
  @Instance(value = Const.MODID)
  public static ModMain instance;
  @SidedProxy(clientSide = "com.lothrazar." + Const.MODID + ".proxy.ClientProxy", serverSide = "com.lothrazar." + Const.MODID + ".proxy.CommonProxy")
  public static CommonProxy proxy;
  public static ModLogger logger;
  private static Configuration config;
  private EventRegistry events;
  public static SimpleNetworkWrapper network;
  public final static CreativeTabs TAB = new CreativeTabs(Const.MODID) {
    @Override
    public Item getTabIconItem() {
      Item tab = ItemRegistry.cyclic_wand_build;
      if (tab == null) {
        tab = Items.DIAMOND;
      }
      return tab;
    }
  };
  // thank you for the examples forge. player data storage based on API source code example:
  // https://github.com/MinecraftForge/MinecraftForge/blob/1.9/src/test/java/net/minecraftforge/test/NoBedSleepingTest.java
  @CapabilityInject(IPlayerExtendedProperties.class)
  public static final Capability<IPlayerExtendedProperties> CAPABILITYSTORAGE = null;
  @EventHandler
  public void onPreInit(FMLPreInitializationEvent event) {
    logger = new ModLogger(event.getModLog());
    config = new Configuration(event.getSuggestedConfigurationFile());
    events = new EventRegistry();
    //TODO MAYBE it should be a constructed, not static
    ItemRegistry.construct();
    BlockRegistry.construct();
    StackSizeRegistry.construct();
    config.load();
    syncConfig();
    network = NetworkRegistry.INSTANCE.newSimpleChannel(Const.MODID);
    events.register();
    MinecraftForge.EVENT_BUS.register(instance);
    CapabilityRegistry.register();
    ReflectionRegistry.register();
    PacketRegistry.register(network);
    SoundRegistry.register();
    EnchantRegistry.register();
  }
  @EventHandler
  public void onInit(FMLInitializationEvent event) {
    PotionRegistry.register();
    ItemRegistry.register();
    BlockRegistry.register();
    SpellRegistry.register();
    MobSpawningRegistry.register();
    WorldGenRegistry.register();
    FuelRegistry.register();
    StackSizeRegistry.register();
    RecipeAlterRegistry.register();
    RecipeNewRegistry.register();
    VillageTradeRegistry.register();
    proxy.register();
    TileEntityRegistry.register();
    NetworkRegistry.INSTANCE.registerGuiHandler(this, new ModGuiHandler());
    ProjectileRegistry.register(event);
    //finally, some items have extra forge events to hook into.
    MinecraftForge.EVENT_BUS.register(BlockRegistry.block_storeempty);
    MinecraftForge.EVENT_BUS.register(ItemRegistry.corrupted_chorus);
    MinecraftForge.EVENT_BUS.register(ItemRegistry.heart_food);
    MinecraftForge.EVENT_BUS.register(ItemRegistry.tool_push);
    MinecraftForge.EVENT_BUS.register(EnchantRegistry.launch);
    MinecraftForge.EVENT_BUS.register(EnchantRegistry.magnet);
    MinecraftForge.EVENT_BUS.register(EnchantRegistry.venom);
    MinecraftForge.EVENT_BUS.register(PotionRegistry.slowfall);
    MinecraftForge.EVENT_BUS.register(PotionRegistry.magnet);
    MinecraftForge.EVENT_BUS.register(PotionRegistry.waterwalk);
  }
  @EventHandler
  public void onPostInit(FMLPostInitializationEvent event) {
    // registers all plantable crops. 
    DispenserBehaviorRegistry.register();
  }
  @EventHandler
  public void onServerStarting(FMLServerStartingEvent event) {
    CommandRegistry.register(event);
  }
  @SubscribeEvent
  public void onConfigChanged(OnConfigChangedEvent event) {
    if (event.getModID().equals(Const.MODID)) {
      ModMain.instance.syncConfig();
    }
  }
  public static Configuration getConfig() {
    return config;
  }
  public void syncConfig() {
    // hit on startup and on change event from
    // we cant make this a list/loop because the order does matter
    Configuration c = getConfig();
    EnchantRegistry.syncConfig(c);
    WorldGenRegistry.syncConfig(c);
    PotionRegistry.syncConfig(c);
    events.syncConfig(c);
    BlockRegistry.syncConfig(c);
    ItemRegistry.syncConfig(c);
    FuelRegistry.syncConfig(c);
    MobSpawningRegistry.syncConfig(c);
    RecipeAlterRegistry.syncConfig(c);
    RecipeNewRegistry.syncConfig(c);
    DispenserBehaviorRegistry.syncConfig(c);
    StackSizeRegistry.syncConfig(c);
    SpellRegistry.syncConfig(c);
    CommandRegistry.syncConfig(c);
    VillageTradeRegistry.syncConfig(c);
    c.save();
  }
  /*
   *FOR 1.0.1
   *
   * GARDEN SCYTHE : make it work on netherrack
   * From Spawn :2.8  //// text misaligned
   * INV CRAFTING TABS : NOT SAVED ON RELOADING GAME
   * bb..but wait why are hearts saved and this isnt?
   * UNCRAFTER: client side item stack bugs: goes down by *2 of normal then back up
   *
   * 
   * new Inventory config: completely hide inventory buttons no matter what
   * 
   * 
   * FUTURE PLANS
   * 
   * block breaker//tree harvester?
   * 
   * pets live longer and/or respawn
   * 
   * custom villager zombie textures
   * 
   * add some of my items to loot tables ?
   * https://github.com/MinecraftForge/MinecraftForge/blob/master/src/test/
   * java/net/minecraftforge/debug/LootTablesDebug.java
   * 
   * crafting table hotkeys - numpad?
   *
   * ROTATE: STAIRS: allow switch from top to bottom
   * 
   * Trading Tool // gui
   * 
   * Fix sorting : UtilInventorySort.sort(p, openInventory);
   */
}
