package dev.pasha.verityai.client;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Tts {
    private static final boolean WINDOWS =
        System.getProperty("os.name", "").toLowerCase().contains("win");

    public static void speak(String text) {
        if (!WINDOWS) {
            System.out.println("[verity_ai] tts=true works only on Windows (SAPI).");
            return;
        }
        try {
            Path tmp = Files.createTempFile("verity_tts", ".txt");
            Files.write(tmp, text.getBytes(StandardCharsets.UTF_8));
            String safe = tmp.toString().replace("'", "''");
            String cmd = "Add-Type -AssemblyName System.Speech; "
                + "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; "
                + "try { $ru = $s.GetInstalledVoices() | Where-Object { $_.Enabled -and $_.VoiceInfo.Culture.Name -like 'ru*' } | Select-Object -First 1; if ($ru) { $s.SelectVoice($ru.VoiceInfo.Name) } } catch {}; "
                + "$t = Get-Content -Raw -Encoding UTF8 '" + safe + "'; "
                + "$s.Speak($t); "
                + "Remove-Item '" + safe + "'";
            ProcessBuilder pb = new ProcessBuilder(
                "powershell", "-NoProfile", "-NonInteractive", "-Command", cmd);
            pb.redirectErrorStream(true);
            pb.start();
        } catch (Exception e) {
            System.out.println("[verity_ai] TTS error: " + e.getMessage());
        }
    }

    private Tts() {
    }
}
