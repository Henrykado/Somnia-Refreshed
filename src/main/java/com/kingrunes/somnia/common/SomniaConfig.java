package com.kingrunes.somnia.common;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.api.SomniaAPI;
import com.kingrunes.somnia.common.compat.CompatModule;

import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Ignore;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;

@Config(name = "somnia-refreshed", modid = Somnia.MOD_ID, category = "")
@Config.LangKey("somnia.config.title")
@Mod.EventBusSubscriber(modid = Somnia.MOD_ID)
public class SomniaConfig {
	
	public enum SleepTimingsPresets
	{
		CUSTOM,
		NIGHT,
		DAY
	}
	
	public enum DisplayPosition
	{
		LEFT,
		CENTER,
		RIGHT
	}
	

    @Config.LangKey("somnia.config.fatigue")
    public static final Fatigue FATIGUE = new Fatigue();

    @Config.LangKey("somnia.config.logic")
    public static final Logic LOGIC = new Logic();

    @Config.LangKey("somnia.config.options")
    public static final Options OPTIONS = new Options();

    @Config.LangKey("somnia.config.performance")
    public static final Performance PERFORMANCE = new Performance();

    @Config.LangKey("somnia.config.timings")
    public static final Timings TIMINGS = new Timings();

    public static class Fatigue {
    	
    	@Config.Comment("Enables the fatigue system, every other option in this category requires this to be true")
    	public boolean enableFatigueSystem = true;
    	
        @Config.Comment("Maximum amount of fatigue the player can have (24 fatigue = 1 in-game day)")
        public double maxFatigue = 120d;
        
        @Ignore
        public double fatigueRate = 0.001;
        
        @Config.Comment("Fatigue is decreased by this number while you sleep (every tick)")
        public double fatigueReplenishRate = 0.01;
        
        @Config.Comment("Fatigue necessary for the player to get sleepy and be able to sleep")
        public double fatigueSleepy = 20;
        
        @Config.Comment("Fatigue necessary for the player to get exhausted, set to -1 to disable\nValue should be higher than fatigueSleepy")
        public double fatigueExhausted = 120;
        
        @Config.Comment("Fatigue necessary for the player to get the fading effect, set to -1 to disable\\nValue should be higher than fatigueExhausted")
        public double fatigueFading = -1;
        
        @Config.RequiresMcRestart
        @Config.Comment("Extra effects that are applied when you are sleepy\n"
        		+ "Valid effects: SPEED, SLOWNESS, HASTE, MINING_FATIGUE, STRENGTH, INSTANT_HEALTH, INSTANT_DAMAGE, JUMP_BOOST, NAUSEA, REGENERATION, RESISTANCE, FIRE_RESISTANCE, WATER_BREATHING, INVISIBILITY, BLINDNESS, NIGHT_VISION, HUNGER, WEAKNESS, POISON, WITHER, HEALTH_BOOST, ABSORPTION, SATURATION, GLOWING, LEVITATION, LUCK, UNLUCK,")
        public String[] sleepyEffects = new String[] {};
        
        @Config.RequiresMcRestart
        @Config.Comment("Amplifiers for the sleepyEffects, if empty defaults to 0")
        public int[] sleepyEffectsAmplifiers = new int[] {};
        
        @Config.RequiresMcRestart
        @Config.Comment("Extra effects that are applied when you are exhausted\n"
        		+ "Valid effects: SPEED, SLOWNESS, HASTE, MINING_FATIGUE, STRENGTH, INSTANT_HEALTH, INSTANT_DAMAGE, JUMP_BOOST, NAUSEA, REGENERATION, RESISTANCE, FIRE_RESISTANCE, WATER_BREATHING, INVISIBILITY, BLINDNESS, NIGHT_VISION, HUNGER, WEAKNESS, POISON, WITHER, HEALTH_BOOST, ABSORPTION, SATURATION, GLOWING, LEVITATION, LUCK, UNLUCK,")
        public String[] exhaustedEffects = new String[] {
        		"MINING_FATIGUE",
        		"SLOWNESS"
        };
        
        @Config.RequiresMcRestart
        @Config.Comment("Amplifiers for the exhaustedEffects, if empty defaults to 0")
        public int[] exhaustedEffectsAmplifiers = new int[] {
        		0,
        		1
        };
        
        @Config.RequiresMcRestart
        @Config.Comment("Extra effects that are applied when you are fading\n"
        		+ "Valid effects: SPEED, SLOWNESS, HASTE, MINING_FATIGUE, STRENGTH, INSTANT_HEALTH, INSTANT_DAMAGE, JUMP_BOOST, NAUSEA, REGENERATION, RESISTANCE, FIRE_RESISTANCE, WATER_BREATHING, INVISIBILITY, BLINDNESS, NIGHT_VISION, HUNGER, WEAKNESS, POISON, WITHER, HEALTH_BOOST, ABSORPTION, SATURATION, GLOWING, LEVITATION, LUCK, UNLUCK,")
        public String[] fadingEffects = new String[] {
        		"MINING_FATIGUE",
        		"SLOWNESS",
        		"BLINDNESS",
        		"WITHER"
        };
        
        @Config.RequiresMcRestart
        @Config.Comment("Amplifiers for the fadingEffects, if empty defaults to 0")
        public int[] fadingEffectsAmplifiers = new int[] {
        		0,
        		2
        };

        @Config.RequiresMcRestart
        @Config.LangKey("somnia.config.fatigue.replenishing_items")
        @Config.Comment("Definitions of fatigue replenishing items. Each list consist of an item registry name (and optionally metadata), the amount of fatigue it replenishes, and optionally a fatigue rate modifier. Example registry names: 'fancy_mod:fancy_item' or 'fancy_mod:meta_item@5'")
        public String[] replenishingItems = new String[] {
                "coffeespawner:coffee, 10",
                "coffeespawner:coffee_milk, 10",
                "coffeespawner:coffee_sugar, 15",
                "coffeespawner:coffee_milk_sugar, 15",
                "harvestcraft:coffeeitem, 5",
                "harvestcraft:coffeeconlecheitem, 15",
                "harvestcraft:espressoitem, 15",
                "coffeework:coffee_instant, 10",
                "coffeework:coffee_instant_cup, 10",
                "coffeework:espresso, 15",
                "ic2:mug@1, 5",
                "ic2:mug@2, 15",
                "ic2:mug@3, 10",
                "actuallyadditions:item_coffee, 10",
                "simplytea:cup_tea_black, 10",
                "simplytea:cup_tea_green, 10",
                "simplytea:cup_tea_floral, 10",
                "simplytea:cup_tea_chai, 10",
                "simplytea:cup_tea_chorus, 10",
                "simplytea:cup_cocoa, 10",
        };
    }

    public static class Logic {
        @Config.RangeDouble(min = 1, max = 50)
        @Config.Comment("If the time difference (mc) between multiplied ticking is greater than this, the simulation multiplier is lowered. Otherwise, it's increased. Lowering this number might slow down simulation and improve performance. Don't mess around with it if you don't know what you're doing.")
        public double delta = 50;
        @Config.Comment("Minimum tick speed multiplier, activated during sleep")
        public double baseMultiplier = 1;
        @Config.Comment("Maximum tick speed multiplier, activated during sleep")
        public double multiplierCap = 10;
    }

    public static class Options {
        @Config.Comment("Slightly slower sleep end")
        public boolean fading = true;

        @Config.Comment("Let the player sleep even when there are monsters nearby")
        public boolean ignoreMonsters = false;

        @Config.Comment("Mutes mob sounds while you're asleep. They are confusing with the world sped up")
        public boolean muteSoundWhenSleeping = false;

        @Config.Comment("Allows you to sleep with armor equipped")
        public boolean sleepWithArmor = true;

        @Config.Comment("Provides an enhanced sleeping gui")
        public boolean somniaGui = true;
        
        @Config.Comment("The ETA and multiplier display position in somnia's sleep gui. Accepted values: right, center, left")
        public DisplayPosition displayETASleep = DisplayPosition.LEFT;

        @Config.Comment("Disables the tick speed multiplier number present in the sleeping gui (requires somniaGui)")
        public boolean guiDisableTickMultiplier = false;
        
        @Config.Comment("Disables the remaining time until you wake up present in the sleeping gui (requires somniaGui)")
        public boolean guiDisableETA = false;
        
        @Config.Comment("The display position of the clock in somnia's enhanced sleeping gui")
        public DisplayPosition somniaGuiClockPosition = DisplayPosition.RIGHT;
        
        @Config.Comment("Disables the clock present in the sleeping gui. (requires somniaGui)")
        public boolean guiDisableClock = false;
        
        @Config.Comment("Adds a Sleep Normally button to the wake time select menu")
        public boolean enableSleepNormallyButton = false;
        
        @Config.RequiresWorldRestart
        @Config.Comment("Disables the Awakening and Imsonmnia potions added by the mod, reccomended if you disabled the fatigue system")
        public boolean disablePotions = false;
        
        @Config.Comment("Enables a special sleep menu, accessible by trying to sleep on a bed while sneaking, that lets you choose when exactly you want to wake up")
        public boolean enableWakeTimeSelectMenu = true;
    }

    public static class Performance {
        @Config.Comment("Disables mob spawning while you sleep")
        public boolean disableCreatureSpawning = false;
        @Config.Comment("Disabled chunk light checking from being called every tick while you sleep")
        public boolean disableMoodSoundAndLightCheck = false;
        @Config.Comment("Disable rendering while you're asleep")
        public boolean disableRendering = false;
        
        @Config.Comment("Makes time go by faster when sleeping without altering tick speed")
        public boolean fasterWorldTime = false;
        @Config.Comment("The time speed multiplier, twice as fast by default")
        public double fasterWorldTimeMultiplier = 2;
    }

    public static class Timings {
    	@Config.Comment("Specifies the period in which the player can sleep\n"
    			+ "NIGHT - sets start to 12000, and end to 24000\n"
    			+ "DAY - sets start to 0, and end to 12000\n"
    			+ "CUSTOM - determined by the values of enterSleepStart and enterSleepEnd\n")
    	public SleepTimingsPresets _enterSleepPreset = SleepTimingsPresets.CUSTOM;
    	
        @Config.Comment("Specifies the start of the period in which the player can enter sleep")
        public int enterSleepStart = 0;
        @Config.Comment("Specifies the end of the period in which the player can enter sleep")
        public int enterSleepEnd = 24000;
        
        @Config.Comment("Specifies the start of the valid sleep period\nWhile the enter sleep period determines when you can start sleeping, the valid sleep period determines when you can continue to sleep without being waken up automatically")
        public int validSleepStart = 0;
        @Config.Comment("Specifies the end of the valid sleep period\nWhile the enter sleep period determines when you can start sleeping, the valid sleep period determines when you can continue to sleep without being waken up automatically")
        public int validSleepEnd = 24000;
    }

    @SubscribeEvent
    public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Somnia.MOD_ID)) {
            ConfigManager.sync(Somnia.MOD_ID, Config.Type.INSTANCE);
            Somnia.setSleepPeriods();
        }
    }

    public static void registerReplenishingItems() {
        Arrays.stream(FATIGUE.replenishingItems)
                .forEach(str -> {
                    String[] parts = str.replace(" ", "").split(",");
                    String[] regName = parts[0].split(":");
                    String modid = regName[0];
                    String itemName = regName[1].contains("@") ? regName[1].split("@")[0] : regName[1];
                    int meta = regName[1].contains("@") ? Integer.parseInt(regName[1].split("@")[1]) : 0;
                    ItemStack stack = CompatModule.getModItem(modid, itemName, meta);
                    if (!stack.isEmpty()) SomniaAPI.addReplenishingItem(stack, Double.parseDouble(parts[1]), parts.length > 2 ? Double.parseDouble(parts[2]) : FATIGUE.fatigueRate);
                });
    }
    
    public static ArrayList<Potion> getMobEffectsFromSideEffects(String[] sideEffects)
    {
    	ArrayList<Potion> mobEffects = new ArrayList<Potion>();
    	for (String i : sideEffects)
    	{
    		switch (i)
    		{
	    		case "SPEED": 			mobEffects.add(MobEffects.SPEED); break;
	    		case "SLOWNESS": 		mobEffects.add(MobEffects.SLOWNESS); break;
	    		case "HASTE":			mobEffects.add(MobEffects.HASTE); break;
	    		case "MINING_FATIGUE": 	mobEffects.add(MobEffects.MINING_FATIGUE); break;
	    		case "STRENGTH": 		mobEffects.add(MobEffects.STRENGTH); break;
	    		case "INSTANT_HEALTH": 	mobEffects.add(MobEffects.INSTANT_HEALTH); break;
	    		case "INSTANT_DAMAGE": 	mobEffects.add(MobEffects.INSTANT_DAMAGE); break;
	    		case "JUMP_BOOST": 		mobEffects.add(MobEffects.JUMP_BOOST); break;
	    		case "NAUSEA": 			mobEffects.add(MobEffects.NAUSEA); break;
	    		case "REGENERATION": 	mobEffects.add(MobEffects.REGENERATION); break;
	    		case "RESISTANCE": 		mobEffects.add(MobEffects.RESISTANCE); break;
	    		case "FIRE_RESISTANCE": mobEffects.add(MobEffects.FIRE_RESISTANCE); break;
	    		case "WATER_BREATHING": mobEffects.add(MobEffects.WATER_BREATHING); break;
	    		case "INVISIBILITY": 	mobEffects.add(MobEffects.INVISIBILITY); break;
	    		case "BLINDNESS": 		mobEffects.add(MobEffects.BLINDNESS); break;
	    		case "NIGHT_VISION": 	mobEffects.add(MobEffects.NIGHT_VISION); break;
	    		case "HUNGER": 			mobEffects.add(MobEffects.HUNGER); break;
	    		case "WEAKNESS": 		mobEffects.add(MobEffects.WEAKNESS); break;
	    		case "POISON": 			mobEffects.add(MobEffects.POISON); break;
	    		case "WITHER": 			mobEffects.add(MobEffects.WITHER); break;
	    		case "HEALTH_BOOST": 	mobEffects.add(MobEffects.HEALTH_BOOST); break;
	    		case "ABSORPTION": 		mobEffects.add(MobEffects.ABSORPTION); break;
	    		case "SATURATION": 		mobEffects.add(MobEffects.SATURATION); break;
	    		case "GLOWING": 		mobEffects.add(MobEffects.GLOWING); break;
	    		case "LEVITATION": 		mobEffects.add(MobEffects.LEVITATION); break;
	    		case "LUCK": 			mobEffects.add(MobEffects.LUCK); break;
	    		case "UNLUCK": 			mobEffects.add(MobEffects.UNLUCK); break;
    		}
    	}
    	return mobEffects;
    }
}
