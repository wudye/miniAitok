package com.mwu.aitokservice.social;

import com.mwu.aitokcommon.cache.annotations.EnableCacheConfig;
import com.mwu.aitolk.feign.config.FeignConfig;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SocialApplication
 * 社交服务
 *
 * @AUTHOR: mwu
 * @DATE: 2023/11/2
 **/
@SpringBootApplication(scanBasePackages = {"com.mwu.aitokservice.social", "com.mwu.aitokcommon", "com.mwu.aitiokcoomon"})
@EnableFeignClients(basePackages = "com.mwu.aitolk.feign", defaultConfiguration = {FeignConfig.class})
@EnableDiscoveryClient
@EnableCacheConfig
@EnableAsync
@EnableScheduling
@EntityScan(basePackages = {
        "com.mwu.aitok.model" // 包含 com.mwu.aitok.model.social.domain 下的实体
})
@EnableJpaRepositories(basePackages = {
        "com.mwu.aitokservice.social.repository" // 仓库接口所在包
})

public class SocialApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(SocialApplication.class, args);
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
