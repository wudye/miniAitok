package com.mwu.aitiokcoomon.core.context;

import com.mwu.aitiokcoomon.core.exception.CustomException;
import com.mwu.aitok.model.common.enums.HttpCodeEnum;
import com.mwu.aitok.model.member.domain.Member;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Objects;

/**
 * UserContext
 *
 * @AUTHOR: roydon
 * @DATE: 2023/10/30
 **/
public class UserContext {

    // 1. 使用 InheritableThreadLocal 替代 TransmittableThreadLocal
    private final static ThreadLocal<Member> USER_THREAD_LOCAL = new InheritableThreadLocal<>();

    //存入线程中
    public static void setUser(Member user) {
        USER_THREAD_LOCAL.set(user);
    }

    //从线程中获取
    public static Member getUser() {
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 获取当前用户，如果不存在则抛出异常
     * @return Member
     */
    public static Member getRequiredUser() {
        Member member = USER_THREAD_LOCAL.get();
        if (Objects.isNull(member)) {
            throw new CustomException(HttpCodeEnum.NEED_LOGIN);
        }
        return member;
    }

    /**
     * 获取用户ID，如果未登录则返回null
     * @return 用户ID或null
     */
    public static Long getUserId() {
        Member member = getUser();
        // 2. 增加空指针判断，避免在未登录时调用方法出错
        return Objects.nonNull(member) ? member.getUserId() : null;
    }

    /**
     * 获取用户ID，如果不存在则抛出异常
     * @return 用户ID
     */
    public static Long getRequiredUserId() {
        // 3. 先调用 getRequiredUser() 确保用户存在，再获取ID，逻辑更严谨
        return getRequiredUser().getUserId();
    }

    /**
     * 是否登录
     */
    public static boolean hasLogin() {
        // 这里的逻辑依赖 getUserId()，在修复 getUserId 后，这里的逻辑也变得更安全
        return Objects.nonNull(getUserId()) && !getUserId().equals(0L);
    }

    //清理
    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }
}
