package dev.pasha.verityai.client;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class MicRecorder {
    private static final int SAMPLE_RATE = 16000;
    private static final AudioFormat FORMAT =
        new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

    private TargetDataLine line;
    private Thread thread;
    private volatile boolean running;
    private final ByteArrayOutputStream pcm = new ByteArrayOutputStream();

    public synchronized boolean start() {
        if (running) return true;
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(FORMAT);
            line.start();
            pcm.reset();
            running = true;
            thread = new Thread(new Runnable() {
                public void run() {
                    byte[] buf = new byte[4096];
                    while (running) {
                        int n = line.read(buf, 0, buf.length);
                        if (n > 0) pcm.write(buf, 0, n);
                    }
                }
            }, "verity-ai-mic");
            thread.setDaemon(true);
            thread.start();
            return true;
        } catch (Exception e) {
            running = false;
            System.out.println("[verity_ai] Microphone unavailable: " + e.getMessage());
            return false;
        }
    }

    public synchronized byte[] stop() {
        if (!running) return null;
        running = false;
        try {
            if (thread != null) thread.join(1000);
        } catch (InterruptedException ignored) {
        }
        if (line != null) {
            line.stop();
            line.close();
        }
        byte[] raw = pcm.toByteArray();
        if (raw.length == 0) return null;
        return wrapWav(raw);
    }

    private static byte[] wrapWav(byte[] pcmData) {
        int dataLen = pcmData.length;
        int byteRate = SAMPLE_RATE * 2;
        ByteBuffer bb = ByteBuffer.allocate(44 + dataLen).order(ByteOrder.LITTLE_ENDIAN);
        bb.put("RIFF".getBytes(StandardCharsets.US_ASCII));
        bb.putInt(36 + dataLen);
        bb.put("WAVE".getBytes(StandardCharsets.US_ASCII));
        bb.put("fmt ".getBytes(StandardCharsets.US_ASCII));
        bb.putInt(16);
        bb.putShort((short) 1);
        bb.putShort((short) 1);
        bb.putInt(SAMPLE_RATE);
        bb.putInt(byteRate);
        bb.putShort((short) 2);
        bb.putShort((short) 16);
        bb.put("data".getBytes(StandardCharsets.US_ASCII));
        bb.putInt(dataLen);
        bb.put(pcmData);
        return bb.array();
    }
}
