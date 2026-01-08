package com.mwu.aitokservice.ai.tool;


import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static com.mwu.aitiokcoomon.core.utils.CustomCollectionUtils.convertList;

/**
 * 工具：列出指定目录的文件列表
 */
@Component("directory_list")
public class DirectoryListToolFunction implements Function<DirectoryListToolFunction.Request, DirectoryListToolFunction.Response> {

    @Data
    @JsonClassDescription("列出指定目录的文件列表")
    public static class Request {

        /**
         * 目录路径
         */
        @JsonProperty(required = true, value = "path")
        @JsonPropertyDescription("目录路径，例如说：/Users/yunai")
        private String path;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {

        /**
         * 文件列表
         */
        private List<File> files;

        @Data
        public static class File {

            /**
             * 是否为目录
             */
            private Boolean directory;

            /**
             * 名称
             */
            private String name;

            /**
             * 大小，仅对文件有效
             */
            private String size;

            /**
             * 最后修改时间
             */
            private String lastModified;

        }

    }

    @Override
    public Response apply(Request request) {
        // 使用 Apache Commons / JDK 进行路径与文件处理
        String path = StringUtils.defaultIfBlank(request.getPath(), "/");
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            return new Response(Collections.emptyList());
        }

        File[] files = dir.listFiles();
        if (ArrayUtils.isEmpty(files)) {
            return new Response(Collections.emptyList());
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new Response(convertList(Arrays.asList(files), file -> new Response.File()
                .setDirectory(file.isDirectory())
                .setName(file.getName())
                .setLastModified(Instant.ofEpochMilli(file.lastModified())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(fmt))));
    }

}
