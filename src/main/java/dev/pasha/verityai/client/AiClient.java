package dev.pasha.verityai.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.pasha.verityai.VerityAiConfig;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AiClient {

    public static String transcribe(byte[] wav) throws Exception {
        String boundary = "----verityai" + System.nanoTime();
        HttpURLConnection c = open("/v1/audio/transcriptions");
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        auth(c);
        OutputStream out = c.getOutputStream();
        try {
            writePart(out, boundary, "model", VerityAiConfig.sttModel);
            writePart(out, boundary, "language", VerityAiConfig.language);
            writePart(out, boundary, "response_format", "text");
            out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"audio.wav\"\r\n")
                .getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Type: audio/wav\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(wav);
            out.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
        } finally {
            out.close();
        }
        String body = readResponse(c, "STT");
        String text = body.trim();
        if (text.startsWith("{")) {
            JsonObject o = new JsonParser().parse(text).getAsJsonObject();
            if (o.has("text")) text = o.get("text").getAsString().trim();
        }
        return text;
    }

    public static String chat(String userText) throws Exception {
        JsonObject root = new JsonObject();
        root.addProperty("model", VerityAiConfig.chatModel);
        root.addProperty("temperature", VerityAiConfig.temperature);
        JsonArray messages = new JsonArray();
        messages.add(message("system", VerityAiConfig.persona));
        messages.add(message("user", userText));
        root.add("messages", messages);

        HttpURLConnection c = open("/v1/chat/completions");
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json");
        auth(c);
        byte[] payload = root.toString().getBytes(StandardCharsets.UTF_8);
        OutputStream out = c.getOutputStream();
        try {
            out.write(payload);
            out.flush();
        } finally {
            out.close();
        }
        String body = readResponse(c, "LLM");
        JsonObject o = new JsonParser().parse(body).getAsJsonObject();
        return o.getAsJsonArray("choices").get(0).getAsJsonObject()
            .getAsJsonObject("message").get("content").getAsString().trim();
    }

    private static HttpURLConnection open(String path) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(VerityAiConfig.baseUrl + path).openConnection();
        c.setConnectTimeout(10000);
        c.setReadTimeout(60000);
        return c;
    }

    private static void auth(HttpURLConnection c) {
        if (VerityAiConfig.apiKey != null && !VerityAiConfig.apiKey.isEmpty()) {
            c.setRequestProperty("Authorization", "Bearer " + VerityAiConfig.apiKey);
        }
    }

    private static String readResponse(HttpURLConnection c, String tag) throws Exception {
        int code = c.getResponseCode();
        InputStream is = (code / 100 == 2) ? c.getInputStream() : c.getErrorStream();
        String body = is == null ? "" : readAll(is);
        if (code / 100 != 2) {
            throw new RuntimeException(tag + " HTTP " + code + ": " + shorten(body));
        }
        return body;
    }

    private static String readAll(InputStream is) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int n;
        try {
            while ((n = is.read(chunk)) != -1) buf.write(chunk, 0, n);
        } finally {
            is.close();
        }
        return new String(buf.toByteArray(), StandardCharsets.UTF_8);
    }

    private static JsonObject message(String role, String content) {
        JsonObject m = new JsonObject();
        m.addProperty("role", role);
        m.addProperty("content", content);
        return m;
    }

    private static void writePart(OutputStream out, String boundary, String name, String value) throws Exception {
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n")
            .getBytes(StandardCharsets.UTF_8));
        out.write((value + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    private static String shorten(String s) {
        if (s == null) return "";
        s = s.replaceAll("\\s+", " ");
        return s.length() > 180 ? s.substring(0, 180) + "..." : s;
    }
}
