package amymialee.doublejumpattribute;

import me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.Toml;
import me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.TomlWriter;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;

public final class DoubleJumpAttributeConfig {
    private transient File file;
    /// Decides the number of particles from double jumping.
    public int jumpParticleCount = 25;
    /// Base double jump count.
    public int jumpJumpCount = 0;

    private DoubleJumpAttributeConfig() {}

    public static DoubleJumpAttributeConfig load() {
        File file = new File(
                FabricLoader.getInstance().getConfigDir().toString(),
                DoubleJumpAttribute.MODID + ".toml"
        );
        DoubleJumpAttributeConfig config;
        if (file.exists()) {
            Toml configTOML = new Toml().read(file);
            config = configTOML.to(DoubleJumpAttributeConfig.class);
            config.file = file;
        } else {
            config = new DoubleJumpAttributeConfig();
            config.file = file;
            config.save();
        }
        return config;
    }

    public void save() {
        TomlWriter writer = new TomlWriter();
        try {
            writer.write(this, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
