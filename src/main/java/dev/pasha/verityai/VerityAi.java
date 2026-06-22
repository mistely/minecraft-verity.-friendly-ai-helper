package dev.pasha.verityai;

import dev.pasha.verityai.client.PushToTalk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(VerityAi.MODID)
public class VerityAi {
    public static final String MODID = "verity_ai";

    public VerityAi() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            VerityAiConfig.load();
            PushToTalk.init();
        }
    }
}
