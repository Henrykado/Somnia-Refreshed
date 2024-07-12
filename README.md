# Somnia Refreshed

## This is a fork of the 1.12.2 version of [Somnia Awoken](https://www.curseforge.com/minecraft/mc-mods/somnia).
### Original mod's 1.16+ code is available at [Su5eD/Somnia-Awoken](https://github.com/Su5eD/Somnia-Awoken).

A Minecraft mod that simulates the world while you sleep, initially released 2011.  

### How it works
Insead of skipping the night, the world is sped up while you sleep.
You can now sleep at any time, as long as you have enough fatigue, which you gain passively over time. 
Be careful - getting too tired gives you negative effects.

![Simulation](src/main/resources/assets/somnia/wiki/simulation.gif)

## Highlighted changes
- You now select wake time by interacting with the bed while sneaking instead of while holding a clock
- Fixed the mod not working properly after using time set commands
- Fatigue system now gives you potion effects to indicate current level of fatigue, instead of introducing a new hud element
- Added new configuration options to disable the fatigue system, accelerate time without messing with ticks, disable certain elements in the sleeping gui (progress bar, remaining time, clock), and more!
- Fixed the camera not facing the right direction and the player not being rendered properly when sleeping