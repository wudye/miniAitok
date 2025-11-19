
        package com.mwu.aitiokcoomon.core.context;

import com.mwu.aitiokcoomon.core.exception.CustomException;
import com.mwu.aitok.model.common.enums.HttpCodeEnum;
import com.mwu.aitok.model.member.domain.Member;

import java.util.Objects;

public class UserContext {

    // 使用 InheritableThreadLocal 以便在简单场景下子线程继承父线程的值
    private static final InheritableThreadLocal<Member> USER_THREAD_LOCAL = new InheritableThreadLocal<>();

    // 存入线程中
    public static void setUser(Member user) {
        USER_THREAD_LOCAL.set(user);
    }

    // 从线程中获取
    public static Member getUser() {
        return USER_THREAD_LOCAL.get();
    }

    public static Member getRequiredUser() {
        Member member = USER_THREAD_LOCAL.get();
        if (Objects.isNull(member)) {
            throw new CustomException(HttpCodeEnum.NEED_LOGIN);
        }
        return member;
    }

    // 获取用户ID
    public static Long getUserId() {
        Member m = getUser();
        return m == null ? null : m.getUserId();
    }

    public static Long getRequiredUserId() {
        return getRequiredUser().getUserId();
    }

    /**
     * 是否登录
     */
    public static boolean hasLogin() {
        Member m = getUser();
        if (Objects.isNull(m)) {
            return false;
        }
        if (m.getUserId() == null || m.getUserId().equals(0L)) {
            return false;
        }
        return true;
    }

    // 清理
    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }

}
