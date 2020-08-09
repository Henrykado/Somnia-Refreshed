package com.kingrunes.somnia.common;

import com.kingrunes.somnia.Somnia;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Somnia.MOD_ID, category = "")
@Config.LangKey("somnia.config.title")
@Mod.EventBusSubscriber(modid = Somnia.MOD_ID)
public class SomniaConfig {

    @Config.LangKey("somnia.config.fatigue")
    public static final Fatigue FATIGUE = new Fatigue();

    @Config.Comment("Fatigue levels to enter each side effect stage, their potion IDs, amplifiers and duration (ticks)")
    @Config.LangKey("somnia.config.sideEffects")
    public static final SideEffects SIDE_EFFECTS = new SideEffects();

    @Config.LangKey("somnia.config.logic")
    public static final Logic LOGIC = new Logic();

    @Config.LangKey("somnia.config.options")
    public static final Options OPTIONS = new Options();

    @Config.LangKey("somnia.config.performance")
    public static final Performance PERFORMANCE = new Performance();

    @Config.LangKey("somnia.config.timings")
    public static final Timings TIMINGS = new Timings();

    public static class Fatigue {

        @Config.Comment("The fatigue counter's position. Accepted values: tc (top center), tl (top left), tr (top right), bc (bottom center), bl (bottom left), br (bottom right)")
        public String displayFatigue = "br";

        @Config.Comment("The ETA and multiplier display position in somnia's sleep gui. Accepted values: right, center, left")
        public String displayETASleep = "right";

        @Config.Comment("Fatigue is incremented by this number every tick")
        public double fatigueRate = 0.00208;

        @Config.Comment("Fatigue is decreased by this number while you sleep (every tick)")
        public double fatigueReplenishRate = 0.00833;

        @Config.Comment("Enables fatigue side effects")
        public boolean fatigueSideEffects = true;

        @Config.Comment("The required amount of fatigue to sleep")
        public double minimumFatigueToSleep = 20;
    }

    public static class SideEffects {

        @Config.Comment("Amount of fatigue required to enter the first side effect stage")
        public int sideEffectStage1 = 70;
        @Config.Comment("Potion effect ID of the first stage")
        public int sideEffectStage1Potion = 9;
        @Config.Comment("Effect duration of the first stage")
        public int sideEffectStage1Duration = 150;
        @Config.Comment("Potion effect amplifier of the first stage")
        public int sideEffectStage1Amplifier = 0;

        @Config.Comment("Amount of fatigue required to enter the second side effect stage")
        public int sideEffectStage2 = 80;
        @Config.Comment("Potion effect ID of the second stage")
        public int sideEffectStage2Potion = 2;
        @Config.Comment("Effect duration of the second stage")
        public int sideEffectStage2Duration = 300;
        @Config.Comment("Potion effect amplifier of the second stage")
        public int sideEffectStage2Amplifier = 2;

        @Config.Comment("Amount of fatigue required to enter the third side effect stage")
        public int sideEffectStage3 = 90;
        @Config.Comment("Potion effect ID of the third stage")
        public int sideEffectStage3Potion = 19;
        @Config.Comment("Effect duration of the third stage")
        public int sideEffectStage3Duration = 200;
        @Config.Comment("Potion effect amplifier of the third stage")
        public int sideEffectStage3Amplifier = 1;

        @Config.Comment("Amount of fatigue required to enter the fourth side effect stage")
        public int sideEffectStage4 = 95;
        @Config.Comment("Potion effect ID of the fourth stage")
        public int sideEffectStage4Potion = 2;
        @Config.Comment("Potion effect amplifier of the fourth stage")
        public int sideEffectStage4Amplifier = 3;
    }

    public static class Logic {
        @Config.Comment("Minimum tick speed multiplier, activated during sleep")
        public double baseMultiplier = 1;
        @Config.Comment("Maximum tick speed multiplier, activated during sleep")
        public double multiplierCap = 100;
    }

    public static class Options {
        @Config.Comment("Slightly slower sleep end")
        public boolean fading = true;
        @Config.Comment("Let the player sleep even when there are monsters nearby")
        public boolean ignoreMonsters = false;
        @Config.Comment("Deafens you while you're asleep. Mob sounds are confusing with the world sped up")
        public boolean muteSoundWhenSleeping = false;
        @Config.Comment("Allows you to sleep with armor equipped")
        public boolean sleepWithArmor = false;
        @Config.Comment("Provides an enhances sleeping gui")
        public boolean somniaGui = true;
        @Config.Comment("Applies a very small FOV while sleeping, because a vanilla bug makes you face in a wrong direction when your bed doesn't face north")
        public boolean vanillaBugFixes = true;
    }

    /*public static class Profiling { Profiling might come later
        public int secondOnGraph = 30;

        public boolean tpsGraph = false;
    }*/

    public static class Performance {
        @Config.Comment("Disables mob spawning while you sleep")
        public boolean disableCreatureSpawning = false;
        @Config.Comment("Disabled chunk light checking from being called every tick while you sleep")
        public boolean disableMoodSoundAndLightCheck = false;
        @Config.Comment("Disable rendering while you're asleep")
        public boolean disableRendering = false;
    }

    public static class Timings {
        @Config.Comment("Specifies the start of the period in which the player can enter sleep")
        public int enterSleepStart = 0;
        @Config.Comment("Specifies the end of the period in which the player can enter sleep")
        public int enterSleepEnd = 24000;

        @Config.Comment("Specifies the start of the valid sleep period")
        public int validSleepStart = 0;
        @Config.Comment("Specifies the end of the valid sleep period")
        public int validSleepEnd = 24000;
    }

    @SubscribeEvent
    public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Somnia.MOD_ID)) {
            ConfigManager.sync(Somnia.MOD_ID, Config.Type.INSTANCE);
        }
    }
}
