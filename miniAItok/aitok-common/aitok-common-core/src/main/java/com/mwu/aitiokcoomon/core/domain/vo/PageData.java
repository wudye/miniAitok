package com.mwu.aitiokcoomon.core.domain.vo;

import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.video.vo.app.MyVideoVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageData<T>  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 消息状态码
     */
    private int code;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 列表数据
     */
    private List<T> rows;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 是否还有更多数据，作为下拉加载更多使用
     */
    private Boolean hasMore;

    /**
     * 返回分页数据
     *
     * @param page 分页对象
     */

    /**
     * 从 Spring Data 的 Page 对象生成分页数据
     *
     * @param page Spring Data 分页对象
     */
    public static <T> PageData<T> fromSpringPage(Page<T> page) {
        if (page.isEmpty()) {
            return emptyPage();
        }
        // 是否还有更多数据
        Boolean hasMore = page.hasNext();
        return new PageData<>(R.SUCCESS, "OK", page.getContent(), page.getTotalElements(), hasMore);
    }

    /**
     * 返回空数据
     */
    public static <T> PageData<T> emptyPage() {
        return new PageData<>(R.SUCCESS, "OK", null, 0, false);
    }
    public static <T> PageData<T> genPageData(List<T> rows, long total) {
        return new PageData<>(R.SUCCESS, "OK", rows, total, true);
    }


}
