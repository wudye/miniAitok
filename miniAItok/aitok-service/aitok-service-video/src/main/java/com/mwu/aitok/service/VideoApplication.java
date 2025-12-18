package com.mwu.aitok.service;

import com.mwu.aitokcommon.cache.annotations.EnableCacheConfig;
import com.mwu.aitolk.feign.config.FeignConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.mwu.aitok.service.video", "com.mwu.aitokcommon", "com.mwu.aitiokcoomon"})
@EnableDiscoveryClient
@EnableCacheConfig
@EnableScheduling
@EnableAsync
@EntityScan({"com.mwu.aitok.model.video.domain", "com.mwu.aitok.model.social.domain"})
@EnableJpaRepositories("com.mwu.aitok.service.video.repository")
@EnableFeignClients(basePackages = "com.mwu.aitolk.feign")
//@ConfigurationPropertiesScan("com.mwu.aitokstarter.file")
@ConfigurationPropertiesScan(basePackages = "com.mwu.aitiokcoomon.core.compont")

public class VideoApplication implements CommandLineRunner {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(VideoApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        System.out.println("                                                                                                                                                            \n" +
                "                                                                                                                                                            \n" +
                "                 ___                                 ___                                                                                                    \n" +
                "               ,--.'|_                             ,--.'|_                                                                                                  \n" +
                "               |  | :,'                  __  ,-.   |  | :,'                                ,--,                                                             \n" +
                "  .--.--.      :  : ' :                ,' ,'/ /|   :  : ' :            .--.--.           ,'_ /|                                     .--.--.      .--.--.    \n" +
                " /  /    '   .;__,'  /      ,--.--.    '  | |' | .;__,'  /            /  /    '     .--. |  | :     ,---.      ,---.      ,---.    /  /    '    /  /    '   \n" +
                "|  :  /`./   |  |   |      /       \\   |  |   ,' |  |   |            |  :  /`./   ,'_ /| :  . |    /     \\    /     \\    /     \\  |  :  /`./   |  :  /`./   \n" +
                "|  :  ;_     :__,'| :     .--.  .-. |  '  :  /   :__,'| :            |  :  ;_     |  ' | |  . .   /    / '   /    / '   /    /  | |  :  ;_     |  :  ;_     \n" +
                " \\  \\    `.    '  : |__    \\__\\/: . .  |  | '      '  : |__           \\  \\    `.  |  | ' |  | |  .    ' /   .    ' /   .    ' / |  \\  \\    `.   \\  \\    `.  \n" +
                "  `----.   \\   |  | '.'|   ,\" .--.; |  ;  : |      |  | '.'|           `----.   \\ :  | : ;  ; |  '   ; :__  '   ; :__  '   ;   /|   `----.   \\   `----.   \\ \n" +
                " /  /`--'  /   ;  :    ;  /  /  ,.  |  |  , ;      ;  :    ;          /  /`--'  / '  :  `--'   \\ '   | '.'| '   | '.'| '   |  / |  /  /`--'  /  /  /`--'  / \n" +
                "'--'.     /    |  ,   /  ;  :   .'   \\  ---'       |  ,   /          '--'.     /  :  ,      .-./ |   :    : |   :    : |   :    | '--'.     /  '--'.     /  \n" +
                "  `--'---'      ---`-'   |  ,     .-./              ---`-'             `--'---'    `--`----'      \\   \\  /   \\   \\  /   \\   \\  /    `--'---'     `--'---'   \n" +
                "                          `--`---'                                                                 `----'     `----'     `----'                             \n" +
                "                                                                                                                                                            ");
    }
}
