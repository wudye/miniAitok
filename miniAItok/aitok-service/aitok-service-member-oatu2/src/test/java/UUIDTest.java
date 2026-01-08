import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class UUIDTest {

    @Test
    public void test() {

        // 1) 推荐：符合 RFC4122 v4，基于强随机（适合安全性需求）
        /*
        "符合 RFC4122 v4"：生成的是符合 RFC4122 标准的第 4 版 UUID（即基于随机数的 UUID，且正确设置了 version 和 variant 位）。
"基于强随机"：内部使用加密安全的伪随机数生成器（如 SecureRandom），比普通 Random/ThreadLocalRandom 更不可预测。
"(适合安全性需求)"：适用于需要不可预测性的场景（如 token、密钥、nonce 等），但会比非加密随机略慢。
         */
        UUID u1 = UUID.randomUUID();
        System.out.println(u1.toString());

        // 2) 直接用 ThreadLocalRandom（快速），但不保证 version/variant
        UUID u2 = new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong());
        System.out.println("thread local random UUID: " + u2.toString());

        // 3) 用 ThreadLocalRandom 生成并手动设置为 RFC4122 v4（高性能且符合格式，但非加密强）
        long msb = ThreadLocalRandom.current().nextLong();
        long lsb = ThreadLocalRandom.current().nextLong();
        msb = (msb & 0xffffffffffff0fffl) | 0x0000000000004000L; // set version to 4
        lsb = (lsb & 0x3fffffffffffffffL) | 0x8000000000000000L; // set variant to 2
        UUID u3 = new UUID(msb, lsb);
        System.out.println("ThreadLocalRandom RFC4122 v4: " + u3);


        // 4) 用 SecureRandom 明确生成 v4（与 UUID.randomUUID 原理相似）
        SecureRandom sr = new SecureRandom();
        byte[] bytes = new byte[16];
        sr.nextBytes(bytes);
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long msb2 = bb.getLong();
        long lsb2 = bb.getLong();
        msb2 = (msb2 & 0xffffffffffff0fffl) | 0x0000000000004000L;
        lsb2 = (lsb2 & 0x3fffffffffffffffL) | 0x8000000000000000L;
        UUID u4 = new UUID(msb2, lsb2);
        System.out.println("SecureRandom + manual v4: " + u4);


        SecureRandom s2r = new SecureRandom();                    // 自动种子，非阻塞
        byte[] bytes2 = new byte[32];
        sr.nextBytes(bytes2);                                     // 填充随机字节
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes2);
        System.out.println(token);
    }
}
