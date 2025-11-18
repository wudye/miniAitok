package com.mwu.aitok.servicememberoauth2.entity;
// java
// File: src/main/java/com/mwu/aitok/servicememberoauth2/security/TokenEntity.java

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;



@Entity
@Table(name = "tokens")
public class TokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;


    /*
    @Lob 是 JPA 的注解，用来标记实体属性为“大对象（Large OBject）”。要点：
对于字符类型（String、Character）映射为 CLOB（在 MySQL 通常对应 TEXT/LONGTEXT）；对二进制类型（byte[]、Serializable）映射为 BLOB。
行为由 JPA 提供者和数据库方言决定；在 MySQL 上可能需要显式使用 columnDefinition = "LONGTEXT" 来避免长度截断。
可与 @Basic(fetch = FetchType.LAZY) 一起使用以延迟加载大字段（具体是否生效依实现而定）。
注解不会自动修改已有数据库列类型，若已存在表需执行迁移（ALTER）或启用 Hibernate DDL 更新。
     */
    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String accessToken;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String refreshToken;


    /*

    Instant 是 Java 标准库中的时间类型（java.time.Instant）。简要要点：
表示 UTC 时刻的一个点（瞬间），不可变，纳秒级精度。
常用用途：表示时间戳、序列化为 ISO‑8601（带 Z 的字符串）、与 epoch 毫秒互转。
常用方法：Instant.now()、Instant.parse(...)、toEpochMilli()、ofEpochMilli(...)。
与本地时区互转：LocalDateTime.ofInstant(instant, ZoneId.of("...")) 或 ZonedDateTime.ofInstant(...)。
JPA/Hibernate：可直接作为实体字段映射为 SQL TIMESTAMP/DATETIME（注意数据库时区/精度差异，MySQL 推荐统一使用 UTC 或显式 columnDefinition）。
Jackson：序列化时建议注册 JavaTimeModule 以得到 ISO 字符串输出。
     */
    private Instant accessExpiry;
    private Instant refreshExpiry;
    private Instant createdAt;

    protected TokenEntity() {}

    public TokenEntity(String userId, String accessToken, String refreshToken,
                       Instant accessExpiry, Instant refreshExpiry, Instant createdAt) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessExpiry = accessExpiry;
        this.refreshExpiry = refreshExpiry;
        this.createdAt = createdAt;
    }

    // getters / setters (省略可用 IDE 生成)
    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public Instant getAccessExpiry() { return accessExpiry; }
    public Instant getRefreshExpiry() { return refreshExpiry; }
    public Instant getCreatedAt() { return createdAt; }

    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setAccessExpiry(Instant accessExpiry) { this.accessExpiry = accessExpiry; }
    public void setRefreshExpiry(Instant refreshExpiry) { this.refreshExpiry = refreshExpiry; }
}
