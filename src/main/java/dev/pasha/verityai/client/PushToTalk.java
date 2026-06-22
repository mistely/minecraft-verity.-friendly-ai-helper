package dev.pasha.verityai.client;

import dev.pasha.verityai.VerityAiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicBoolean;

public class PushToTalk {
    public static final KeyBinding VOICE_KEY = new KeyBinding(
        "key.verity_ai.talk",
        KeyConflictContext.IN_GAME,
        InputMappings.Type.KEYSYM,
        GLFW.GLFW_KEY_X,
        "key.categories.verity"
    );

    private final MicRecorder recorder = new MicRecorder();
    private boolean recording = false;
    private final AtomicBoolean processing = new AtomicBoolean(false);

    public static void init() {
        PushToTalk handler = new PushToTalk();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PushToTalk::onClientSetup);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(new Runnable() {
            public void run() {
                ClientRegistry.registerKeyBinding(VOICE_KEY);
            }
        });
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!VerityAiConfig.enabled) return;
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.currentScreen != null) return;

        boolean down = VOICE_KEY.isKeyDown();
        if (down && !recording && !processing.get()) {
            if (recorder.start()) {
                recording = true;
                actionbar(mc, "\u00A7b\uD83C\uDFA4 Verity slushaet...");
            }
        } else if (!down && recording) {
            recording = false;
            byte[] wav = recorder.stop();
            actionbar(mc, "\u00A77Verity dumaet...");
            handle(mc, wav);
        }
    }

    private void handle(final Minecraft mc, final byte[] wav) {
        if (wav == null) {
            actionbar(mc, "\u00A7cNichego ne zapisalos");
            return;
        }
        if (processing.getAndSet(true)) return;
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    String heard = AiClient.transcribe(wav);
                    if (heard == null || heard.trim().isEmpty()) {
                        post(mc, "\u00A77[Verity] ne rasslyshala");
                        return;
                    }
                    post(mc, "\u00A78Ty: " + heard);
                    String reply = AiClient.chat(heard);
                    post(mc, "\u00A7b[Verity] \u00A7r" + reply);
                    if (VerityAiConfig.tts) Tts.speak(reply);
                } catch (Exception ex) {
                    post(mc, "\u00A7c[Verity AI] " + ex.getMessage());
                } finally {
                    processing.set(false);
                }
            }
        }, "verity-ai-pipeline");
        t.setDaemon(true);
        t.start();
    }

    private void actionbar(Minecraft mc, String text) {
        if (mc.player != null) mc.player.sendStatusMessage(new StringTextComponent(text), true);
    }

    private void post(final Minecraft mc, final String text) {
        mc.execute(new Runnable() {
            public void run() {
                if (mc.player != null) mc.player.sendMessage(new StringTextComponent(text), Util.DUMMY_UUID);
            }
        });
    }
}
