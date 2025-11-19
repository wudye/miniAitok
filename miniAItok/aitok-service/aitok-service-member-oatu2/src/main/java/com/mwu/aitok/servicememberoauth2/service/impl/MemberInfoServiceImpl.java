package com.mwu.aitok.servicememberoauth2.service.impl;

import com.mwu.aitiokcoomon.core.exception.CustomException;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.model.common.enums.HttpCodeEnum;
import com.mwu.aitok.model.member.domain.MemberInfo;
import com.mwu.aitok.servicememberoauth2.repository.MemberInfoRepository;
import com.mwu.aitok.servicememberoauth2.service.MemberInfoService;
import com.mwu.aitokstarter.file.service.MinioService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class MemberInfoServiceImpl implements MemberInfoService {
    @Autowired
    private MinioService minioService;

    @Autowired
    private MemberInfoRepository memberInfoRepository;
    @Override
    public String uploadBackImage(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isNull(originalFilename)) {
            throw new CustomException(HttpCodeEnum.IMAGE_TYPE_FOLLOW);
        }
        try
            {
                //对原始文件名进行判断
                if (originalFilename.endsWith(".png")
                        || originalFilename.endsWith(".jpg")
                        || originalFilename.endsWith(".jpeg")
                        || originalFilename.endsWith(".gif")
                        || originalFilename.endsWith(".webp")) {
//            String filePath = PathUtils.generateFilePath(originalFilename);
//            String url = fileStorageService.uploadImgFile(file, QiniuUserOssConstants.PREFIX_URL, filePath);
                    return minioService.uploadFile(file);
                } else {
                    throw new CustomException(HttpCodeEnum.IMAGE_TYPE_FOLLOW);
                }
            }
        catch (Exception e)
            {
                throw new RuntimeException("minio upload error");
            }

    }

    @Override
    public Boolean saveOrUpdate(MemberInfo memberInfo) {
       try  {

           Long userId = memberInfo.getUserId();
           Optional<MemberInfo> optionalMemberInfo = memberInfoRepository.findMemberInfoByUserId(userId);
           if (optionalMemberInfo.isPresent()){
               MemberInfo memberInfo1 = optionalMemberInfo.get();
               BeanUtils.copyProperties(memberInfo1, memberInfo, "infoId", "userId");
               memberInfoRepository.save(memberInfo);
           } else {
               memberInfoRepository.save(memberInfo);
           }
            return true;
        } catch (Exception e) {
            throw new CustomException(null);
        }

    }
}
