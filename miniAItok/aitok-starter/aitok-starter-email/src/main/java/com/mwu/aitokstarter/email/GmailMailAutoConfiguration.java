package com.mwu.aitokstarter.email;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@AutoConfiguration
@EnableConfigurationProperties(GmailMailProperties.class)
@ConditionalOnClass(JavaMailSender.class)
@ConditionalOnProperty(prefix = "gmail.mail", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GmailMailAutoConfiguration {

    private final GmailMailProperties props;

    public GmailMailAutoConfiguration(GmailMailProperties props) {
        this.props = props;
    }

    @Bean
    @ConditionalOnMissingBean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(props.getHost());
        sender.setPort(props.getPort());
        sender.setProtocol(props.getProtocol());
        sender.setUsername(props.getUsername());
        sender.setPassword(props.getPassword());

        Properties p = sender.getJavaMailProperties();
        p.put("mail.transport.protocol", props.getProtocol());
        p.put("mail.smtp.auth", String.valueOf(props.isAuth()));
        p.put("mail.smtp.starttls.enable", String.valueOf(props.isStarttlsEnable()));
        p.put("mail.debug", "false");
        return sender;
    }

    @Bean
    @ConditionalOnMissingBean
    public GmailMailService gmailMailService(JavaMailSender mailSender) {
        return new GmailMailService(mailSender);
    }
}
