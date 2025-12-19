package com.mwu.aitok.gatewayserver.config;



import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BucketLookup {
    private static final int BUCKET_COUNT = 1024;

    public static int bucket(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            int h = ((d[0] & 0xFF) << 24) | ((d[1] & 0xFF) << 16) | ((d[2] & 0xFF) << 8) | (d[3] & 0xFF);
            return Math.abs(h) % BUCKET_COUNT;
        } catch (NoSuchAlgorithmException ex) {
            return Math.abs(s.hashCode()) % BUCKET_COUNT;
        }
    }

    public static void main(String[] args) {
        String user = (args != null && args.length > 0) ? args[0] : "user1";
        int b = bucket(user);
        String tokensKey = "request_rate_limiter.{member.user-bucket:" + b + "}.tokens";
        String tsKey = "request_rate_limiter.{member.user-bucket:" + b + "}.timestamp";
        System.out.println("user: " + user);
        System.out.println("bucket: " + b);
        System.out.println("tokens key: " + tokensKey);
        System.out.println("timestamp key: " + tsKey);
    }
}
