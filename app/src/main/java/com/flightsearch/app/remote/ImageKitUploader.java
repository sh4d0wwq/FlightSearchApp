package com.flightsearch.app.remote;

import android.content.Context;
import android.net.Uri;

import com.flightsearch.app.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Server-side upload to ImageKit (same auth as official Node SDK):
 * POST multipart to upload endpoint + HTTP Basic: username = private API key, password empty.
 * Put {@code imagekit.private.key} in {@code local.properties}. Public key is not used here.
 */
public final class ImageKitUploader {

    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    private ImageKitUploader() {}

    public static String uploadImage(Context context, Uri uri, String fileName) throws IOException {
        String privateKey = BuildConfig.IMAGEKIT_PRIVATE_KEY;
        if (privateKey == null || privateKey.isEmpty()) return null;

        byte[] bytes;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) return null;
            bytes = readAll(in);
        }

        String name = fileName != null ? fileName : "photo.jpg";
        RequestBody fileBody = RequestBody.create(bytes, MediaType.parse("image/jpeg"));
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", name, fileBody)
                .addFormDataPart("fileName", name)
                .build();

        String basic = Credentials.basic(privateKey, "", StandardCharsets.UTF_8);
        Request up = new Request.Builder()
                .url("https://upload.imagekit.io/api/v1/files/upload")
                .header("Authorization", basic)
                .post(body)
                .build();

        try (Response upResp = HTTP.newCall(up).execute()) {
            if (!upResp.isSuccessful() || upResp.body() == null) return null;
            JsonObject res = new Gson().fromJson(upResp.body().string(), JsonObject.class);
            if (res != null && res.has("url")) return res.get("url").getAsString();
        }
        return null;
    }

    private static byte[] readAll(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        byte[] tmp = new byte[8192];
        int n;
        while ((n = in.read(tmp)) != -1) buf.write(tmp, 0, n);
        return buf.toByteArray();
    }
}
