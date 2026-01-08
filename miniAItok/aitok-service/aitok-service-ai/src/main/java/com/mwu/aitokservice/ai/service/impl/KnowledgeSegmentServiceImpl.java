package com.mwu.aitokservice.ai.service.impl;



import com.mwu.aitiokcoomon.core.utils.bean.BeanUtils;
import com.mwu.aitok.model.ai.bo.KnowledgeSegmentSearchReqBO;
import com.mwu.aitok.model.ai.bo.KnowledgeSegmentSearchRespBO;
import com.mwu.aitok.model.ai.domain.knowledge.KnowledgeDO;
import com.mwu.aitok.model.ai.domain.knowledge.KnowledgeDocumentDO;
import com.mwu.aitok.model.ai.domain.knowledge.KnowledgeSegmentDO;
import com.mwu.aitok.model.ai.vo.chat.ChatMessageRespVO;
import com.mwu.aitok.model.common.enums.StateFlagEnum;
import com.mwu.aitokservice.ai.mapper.KnowledgeSegmentMapper;
import com.mwu.aitokservice.ai.service.IChatModelService;
import com.mwu.aitokservice.ai.service.IKnowledgeDocumentService;
import com.mwu.aitokservice.ai.service.IKnowledgeSegmentService;
import com.mwu.aitokservice.ai.service.IKnowledgeService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.mwu.aitiokcoomon.core.utils.CustomCollectionUtils.convertList;
import static org.apache.hadoop.thirdparty.org.checkerframework.checker.nullness.Opt.orElse;


/**
 * AI 知识库分段表(KnowledgeSegment)表服务实现类
 *
 * @author roydon
 * @since 2025-06-03 22:03:58
 */
@Service
public class KnowledgeSegmentServiceImpl  implements IKnowledgeSegmentService {

    private static final String VECTOR_STORE_METADATA_KNOWLEDGE_ID = "knowledgeId";
    private static final String VECTOR_STORE_METADATA_DOCUMENT_ID = "documentId";
    private static final String VECTOR_STORE_METADATA_SEGMENT_ID = "segmentId";

    private static final Map<String, Class<?>> VECTOR_STORE_METADATA_TYPES = Map.of(
            VECTOR_STORE_METADATA_KNOWLEDGE_ID, String.class,
            VECTOR_STORE_METADATA_DOCUMENT_ID, String.class,
            VECTOR_STORE_METADATA_SEGMENT_ID, String.class);

    @Resource
    private KnowledgeSegmentMapper knowledgeSegmentMapper;
    @Resource
    private IKnowledgeService knowledgeService;
    @Resource
    private IChatModelService chatModelService;
    @Resource
    @Lazy
    private IKnowledgeDocumentService knowledgeDocumentService;
    @Resource
    private TokenCountEstimator tokenCountEstimator;

    /**
     * 搜索知识库分段
     */
    @Override
    public List<KnowledgeSegmentSearchRespBO> searchKnowledgeSegment(KnowledgeSegmentSearchReqBO bo) {
        // 1. 校验
        KnowledgeDO knowledge = knowledgeService.validateKnowledgeExists(bo.getKnowledgeId());

        // 2.1 向量检索
        VectorStore vectorStore = chatModelService.getOrCreateVectorStore(knowledge.getEmbeddingModelId(), VECTOR_STORE_METADATA_TYPES);
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(bo.getContent())
                .topK(Objects.requireNonNullElse(bo.getTopK(), knowledge.getTopK()))
                .similarityThreshold(Objects.requireNonNullElse(bo.getSimilarityThreshold(), knowledge.getSimilarityThreshold().doubleValue()))
                .filterExpression(new FilterExpressionBuilder().eq(VECTOR_STORE_METADATA_KNOWLEDGE_ID, bo.getKnowledgeId().toString()).build())
                .build());
        if (CollectionUtils.isEmpty(documents)) {
            return List.of();
        }
        // 2.2 段落召回
        List<String> convertIds = convertList(documents, Document::getId);

        List<KnowledgeSegmentDO> segments = knowledgeSegmentMapper.findAllByVectorIdOrderByIdDesc(String.valueOf(convertIds));
        if (CollectionUtils.isEmpty(segments)) {
            return Collections.emptyList();
        }

        // 3. 增加召回次数
        knowledgeSegmentMapper.saveAll(segments);

        // 4. 构建结果
        List<KnowledgeSegmentSearchRespBO> result = convertList(segments, segment -> {
            Document document  = documents.stream()
                    .filter(doc -> Objects.equals(doc.getId(), segment.getVectorId()))
                    .findFirst().orElse(null);

            if (document == null) {
                return null;
            }
            return BeanUtils.toBean(segment, KnowledgeSegmentSearchRespBO.class).setScore(document.getScore());
        });
        result.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore())); // 按照分数降序排序
        return result;
    }

    private VectorStore getVectorStoreById(Long knowledgeId) {
        KnowledgeDO knowledge = knowledgeService.validateKnowledgeExists(knowledgeId);
        return chatModelService.getOrCreateVectorStore(knowledge.getEmbeddingModelId(), VECTOR_STORE_METADATA_TYPES);
    }

    /**
     * 基于 content 内容，切片创建多个段落
     *
     * @param documentId 知识库文档编号
     * @param content    文档内容
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createKnowledgeSegmentBySplitContent(Long documentId, String content) {
        // 1. 校验
        KnowledgeDocumentDO documentDO = knowledgeDocumentService.validateKnowledgeDocumentExists(documentId);

        VectorStore vectorStore = getVectorStoreById(documentDO.getKnowledgeId());

        // 2. 文档切片
        List<Document> documentSegments = splitContentByToken(content, documentDO.getSegmentMaxTokens());

        // 3.1 存储切片
        List<KnowledgeSegmentDO> segmentDOs = convertList(documentSegments, segment -> {
            if (StringUtils.isEmpty(segment.getText())) {
                return null;
            }
            return new KnowledgeSegmentDO().setKnowledgeId(documentDO.getKnowledgeId())
                    .setDocumentId(documentId)
                    .setContent(segment.getText()).setContentLength(segment.getText().length())
                    .setVectorId(KnowledgeSegmentDO.VECTOR_ID_EMPTY)
                    .setTokens(tokenCountEstimator.estimate(segment.getText()))
                    .setStateFlag(StateFlagEnum.ENABLE.getCode());
        });

        knowledgeSegmentMapper.saveAll(segmentDOs);
        // 3.2 切片向量化
        for (int i = 0; i < documentSegments.size(); i++) {
            Document segment = documentSegments.get(i);
            KnowledgeSegmentDO segmentDO = segmentDOs.get(i);
            writeVectorStore(vectorStore, segmentDO, segment);
        }
    }

    private static List<Document> splitContentByToken(String content, Integer segmentMaxTokens) {
        TextSplitter textSplitter = buildTokenTextSplitter(segmentMaxTokens);
        return textSplitter.apply(Collections.singletonList(new Document(content)));
    }

    private static TextSplitter buildTokenTextSplitter(Integer segmentMaxTokens) {
        return TokenTextSplitter.builder()
                .withChunkSize(segmentMaxTokens)
                .withMinChunkSizeChars(Integer.MAX_VALUE) // 忽略字符的截断
                .withMinChunkLengthToEmbed(1) // 允许的最小有效分段长度
                .withMaxNumChunks(Integer.MAX_VALUE)
                .withKeepSeparator(true) // 保留分隔符
                .build();
    }

    private void writeVectorStore(VectorStore vectorStore, KnowledgeSegmentDO segmentDO, Document segment) {
        // 1. 向量存储
        // 为什么要 toString 呢？因为部分 VectorStore 实现，不支持 Long 类型，例如说 QdrantVectorStore
        segment.getMetadata().put(VECTOR_STORE_METADATA_KNOWLEDGE_ID, segmentDO.getKnowledgeId().toString());
        segment.getMetadata().put(VECTOR_STORE_METADATA_DOCUMENT_ID, segmentDO.getDocumentId().toString());
        segment.getMetadata().put(VECTOR_STORE_METADATA_SEGMENT_ID, segmentDO.getId().toString());
        vectorStore.add(List.of(segment));

        KnowledgeSegmentDO knowledgeSegment = new KnowledgeSegmentDO().setId(segmentDO.getId()).setVectorId(segment.getId());
        // 2. 更新向量 ID
        knowledgeSegmentMapper.save(knowledgeSegment);
    }

    /**
     * 根据 URL 内容，切片创建多个段落
     *
     * @param url              URL 地址
     * @param segmentMaxTokens 段落最大 Token 数
     * @return 切片后的段落列表
     */
    @Override
    public List<KnowledgeSegmentDO> splitContent(String url, Integer segmentMaxTokens) {
        // 1. 读取 URL 内容
        String content = knowledgeDocumentService.readUrl(url);

        // 2. 文档切片
        List<Document> documentSegments = splitContentByToken(content, segmentMaxTokens);

        // 3. 转换为段落对象
        return convertList(documentSegments, segment -> {
            if (StringUtils.isEmpty(segment.getText())) {
                return null;
            }
            return new KnowledgeSegmentDO()
                    .setContent(segment.getText())
                    .setContentLength(segment.getText().length())
                    .setTokens(tokenCountEstimator.estimate(segment.getText()));
        });
    }

    /**
     * 根据文档编号删除段落
     *
     * @param documentId 文档编号
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteKnowledgeSegmentByDocumentId(Long documentId) {

        List<KnowledgeSegmentDO> segments = knowledgeSegmentMapper.findAllByDocumentId(documentId);
        if (CollectionUtils.isEmpty(segments)) {
            return;
        }
        // 1. 删db

        knowledgeSegmentMapper.deleteAll(segments);
        // 2. 删向量存储
        VectorStore vectorStore = getVectorStoreById(segments.get(0).getKnowledgeId());
        vectorStore.delete(convertList(segments, KnowledgeSegmentDO::getVectorId));
    }
}
