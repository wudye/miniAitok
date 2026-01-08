package com.mwu.aitokservice.ai.service.impl;



import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.exception.CustomException;
import com.mwu.aitiokcoomon.core.utils.bean.BeanUtils;
import com.mwu.aitok.model.ai.domain.knowledge.KnowledgeDocumentDO;
import com.mwu.aitok.model.ai.dto.knowledge.web.KnowledgeDocumentCreateDTO;
import com.mwu.aitok.model.ai.dto.knowledge.web.KnowledgeDocumentPageDTO;
import com.mwu.aitok.model.ai.vo.knowledge.web.KnowledgeDocumentVO;
import com.mwu.aitok.model.common.enums.HttpCodeEnum;
import com.mwu.aitok.model.common.enums.StateFlagEnum;
import com.mwu.aitokservice.ai.mapper.KnowledgeDocumentMapper;
import com.mwu.aitokservice.ai.service.IKnowledgeDocumentService;
import com.mwu.aitokservice.ai.service.IKnowledgeSegmentService;
import com.mwu.aitokservice.ai.service.IKnowledgeService;
import com.mwu.aitokstarter.file.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * AI 知识库文档表(KnowledgeDocument)表服务实现类
 *
 * @author roydon
 * @since 2025-06-03 22:03:43
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class KnowledgeDocumentServiceImpl implements IKnowledgeDocumentService {
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final IKnowledgeService knowledgeService;
    private final TokenCountEstimator tokenCountEstimator;
    private final IKnowledgeSegmentService knowledgeSegmentService;
    private final MinioService minioService;

    /**
     * 获取文档列表
     *
     * @param ids 文档编号列表
     * @return 文档列表
     */
    @Override
    public List<KnowledgeDocumentDO> getKnowledgeDocumentList(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<KnowledgeDocumentDO> documents = knowledgeDocumentMapper.findAllById(ids);
        return documents;
    }

    @Override
    public Map<Long, KnowledgeDocumentDO> getKnowledgeDocumentMap(Collection<Long> ids) {

        List<KnowledgeDocumentDO> documents = getKnowledgeDocumentList(ids);
        Map<Long, KnowledgeDocumentDO> documentMap = new HashMap<>(documents.size());
        documents.forEach(document -> documentMap.put(document.getId(), document));


        return documentMap;
    }
    /**
     * 新建文档（单个）
     *
     * @return 文档编号
     */
    @Override
    public Long createKnowledgeDocument(KnowledgeDocumentCreateDTO dto) {
        // 1. 校验参数
        knowledgeService.validateKnowledgeExists(dto.getKnowledgeId());

        // 2. 下载文档
        String content = readUrl(dto.getUrl());

        // 3. 文档记录入库
        KnowledgeDocumentDO documentDO = BeanUtils.toBean(dto, KnowledgeDocumentDO.class)
                .setContent(content)
                .setContentLength(content.length())
                .setTokens(tokenCountEstimator.estimate(content))
                .setStateFlag(StateFlagEnum.ENABLE.getCode());
        knowledgeDocumentMapper.save(documentDO);

        // 4. 文档切片入库（异步）
        knowledgeSegmentService.createKnowledgeSegmentBySplitContentAsync(documentDO.getId(), content);
        return documentDO.getId();
    }

    /**
     * 读取 URL 内容
     *
     * @param url URL
     * @return 内容
     */
    @Override
    public String readUrl(String url) {
        // 下载文件
        ByteArrayResource resource;
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                log.error("[readUrl][url({}) 下载返回非 200 状态: {}]", url, response.statusCode());
                throw new RuntimeException("文件下载失败!");
            }

            byte[] bytes = response.body();
            if (bytes == null || bytes.length == 0) {
                throw new RuntimeException("文档内容为空!");
            }
            resource = new ByteArrayResource(bytes);
        } catch (Exception e) {
            log.error("[readUrl][url({}) 读取失败]", url, e);
            throw new RuntimeException("文件下载失败!");
        }

        // 读取文件
        TikaDocumentReader loader = new TikaDocumentReader(resource);
        List<Document> documents = loader.get();
        Document document = documents.get(0);
        if (document == null || StringUtils.isEmpty(document.getText())) {
            throw new RuntimeException("文档加载失败!");
        }
        return document.getText();
    }

    @Override
    public KnowledgeDocumentDO validateKnowledgeDocumentExists(Long documentId) {
        return null;
    }

    /**
     * 上传文档
     *
     * @param file 文件
     * @return 文档 URL
     */
    @Override
    public String uploadKnowledgeDocument(Long knowledgeId, Integer segmentMaxTokens, MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        // todo 对文件大小进行判断
        // 原始文件名是否符合类型
        if (originalFilename.endsWith(".pdf")
                || originalFilename.endsWith(".doc")
                || originalFilename.endsWith(".docx")) {
            String uploadUrl = minioService.uploadFile(file);
            KnowledgeDocumentCreateDTO knowledgeDocumentCreateDTO = new KnowledgeDocumentCreateDTO();
            knowledgeDocumentCreateDTO.setKnowledgeId(knowledgeId)
                    .setName(originalFilename)
                    .setUrl(uploadUrl)
                    .setSegmentMaxTokens(segmentMaxTokens);
            createKnowledgeDocument(knowledgeDocumentCreateDTO);

            // todo 返回sse id去轮询文档状态
            return uploadUrl;
        }
        throw new CustomException(HttpCodeEnum.DOCUMENT_TYPE_ERROR);
    }

    /**
     * 删除文档
     *
     * @param id 文档编号
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean removeKnowledgeDocumentById(Long id) {
        // 1. 删除文档
        knowledgeDocumentMapper.deleteById(id);
        // 2. 删除文档分段
        knowledgeSegmentService.deleteKnowledgeSegmentByDocumentId(id);
        return true;
    }

    /**
     * 获取文档分页
     *
     * @param dto 分页参数
     * @return 文档分页
     */
    @Override
    public PageData<KnowledgeDocumentVO> getKnowledgeDocumentPage(KnowledgeDocumentPageDTO dto) {
        
        Pageable pageable = PageRequest.of(dto.getPageNum() - 1, dto.getPageSize());
        Page<KnowledgeDocumentDO> page = knowledgeDocumentMapper.findAllByKnowledgeId(dto.getKnowledgeId(), pageable);
        List<KnowledgeDocumentVO> knowledgeDocumentVOS = BeanUtils.toBean(page.getContent(), KnowledgeDocumentVO.class);
        return  PageData.genPageData(knowledgeDocumentVOS, page.getTotalElements());
    }


}
