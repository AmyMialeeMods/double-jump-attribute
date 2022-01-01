package amymialee.doublejumpattribute;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = DoubleJumpAttribute.MODID)
public class DoubleJumpAttributeConfig implements ConfigData {
    public int jumpParticleCount = 25;
}
