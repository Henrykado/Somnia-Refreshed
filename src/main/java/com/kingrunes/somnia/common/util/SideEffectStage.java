package com.kingrunes.somnia.common.util;

import com.kingrunes.somnia.common.SomniaConfig;

import java.util.Arrays;

public class SideEffectStage {
    public static SideEffectStage[] stages;

    public final int minFatigue;
    public final int maxFatigue;
    public final int potionID;
    public final int duration;
    public final int amplifier;

    public SideEffectStage(int minFatigue, int maxFatigue, int potionID, int duration, int amplifier)
    {
        this.minFatigue = minFatigue;
        this.maxFatigue = maxFatigue;
        this.potionID = potionID;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public static void registerSideEffectStages()
    {
        stages = new SideEffectStage[SomniaConfig.FATIGUE.sideEffectStages.length];
        for (int i = 0; i < stages.length; i++)
        {
            stages[i] = parseStage(SomniaConfig.FATIGUE.sideEffectStages[i]);
        }
    }

    private static SideEffectStage parseStage(String stage)
    {
        String[] parts = stage.replace(" ", "").split(",");
        int[] ret = Arrays.stream(parts)
                .mapToInt(Integer::parseInt)
                .toArray();
        return new SideEffectStage(ret[0], ret[1], ret[2], ret[3], ret[4]);
    }

    public static String getSideEffectStageDescription(double fatigue)
    {
        int stage = getSideEffectStage(fatigue);
        float ratio = SomniaConfig.FATIGUE.sideEffectStages.length / 4F;
        int desc = Math.round(stage / ratio);
        return SomniaUtil.translate("somnia.side_effect."+desc);
    }

    public static int getSideEffectStage(double fatigue)
    {
        for (int i = 0; i < SomniaConfig.FATIGUE.sideEffectStages.length; i++)
        {
            SideEffectStage stage = stages[i];
            if (fatigue >= stage.minFatigue && fatigue <= stage.maxFatigue && (!(stage.duration < 0) || i == SomniaConfig.FATIGUE.sideEffectStages.length - 1)) return i + 1;
        }

        return 0;
    }
}
