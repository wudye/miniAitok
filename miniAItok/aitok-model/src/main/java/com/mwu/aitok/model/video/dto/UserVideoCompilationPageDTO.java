package com.mwu.aitok.model.video.dto;

import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * UserVideoCompilationPageDTO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/11/27
 **/
@NoArgsConstructor

@Data
public class UserVideoCompilationPageDTO  {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private UserVideoCompilation userVideoCompilation;

    public UserVideoCompilationPageDTO(UserVideoCompilation userVideoCompilation) {

        this.userVideoCompilation = userVideoCompilation;
    }


    public static UserVideoCompilationPageDTO build(UserVideoCompilation userVideoCompilation) {
        return new UserVideoCompilationPageDTO(userVideoCompilation);
    }
}
