package com.flydeer.service.graph;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flydeer.common.exception.ErrorCodes;
import com.flydeer.common.exception.business.AtlasForbiddenException;
import com.flydeer.common.exception.business.AtlasNotFoundException;
import com.flydeer.common.exception.business.AtlasNotVisibleException;
import com.flydeer.common.exception.business.GraphNotFoundException;
import com.flydeer.common.exception.request.AtlasNotPublishedException;
import com.flydeer.common.exception.request.BadRequestException;
import com.flydeer.common.exception.request.GraphContentTooLargeException;
import com.flydeer.common.exception.request.GraphIdInvalidException;
import com.flydeer.common.exception.request.GraphParentInvalidException;
import com.flydeer.common.exception.request.GraphRevConflictException;
import com.flydeer.contract.graph.request.GraphBatchItemRequest;
import com.flydeer.contract.graph.request.GraphBatchSaveRequest;
import com.flydeer.contract.graph.request.GraphListRequest;
import com.flydeer.contract.graph.request.GraphMoveRequest;
import com.flydeer.contract.graph.request.GraphRenameRequest;
import com.flydeer.contract.graph.request.GraphSaveRequest;
import com.flydeer.contract.graph.vo.GraphSaveResultVO;
import com.flydeer.repository.postgres.dto.GraphDTO;
import com.flydeer.repository.postgres.dto.GraphParentLink;
import com.flydeer.repository.postgres.mapping.GraphMapping;
import com.flydeer.repository.postgres.repository.GraphRepository;
import com.flydeer.service.atlas.AtlasService;
import com.flydeer.service.graph.config.GraphConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
public class GraphService {

    private static final Pattern GRAPH_ID_PATTERN = Pattern.compile("^gph_[0-9a-f]{12}$");
    private static final ObjectMapper OBJECT_MAPPER = GraphMapping.OBJECT_MAPPER;

    private final GraphRepository graphRepository;
    private final AtlasService atlasService;
    private final GraphConfig graphConfig;
    private final TransactionTemplate postgresTx;

    public GraphService(
        GraphRepository graphRepository,
        AtlasService atlasService,
        GraphConfig graphConfig,
        @Qualifier("postgresTransactionManager") PlatformTransactionManager postgresTransactionManager) {
        this.graphRepository = graphRepository;
        this.atlasService = atlasService;
        this.graphConfig = graphConfig;
        this.postgresTx = new TransactionTemplate(postgresTransactionManager);
    }

    public List<GraphDTO> list(GraphListRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, AtlasNotPublishedException {
        atlasService.requireReadable(request.getAtlasId(), request.getAllUserIds(), false);
        return graphRepository.listMetaByAtlasId(request.getAtlasId(), blankToNull(request.getKeyword()));
    }

    public GraphDTO detail(String graphId, List<Long> userIds)
        throws GraphNotFoundException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException,
        AtlasNotPublishedException {
        GraphDTO graph = requireActiveGraph(graphId);
        atlasService.requireReadable(graph.getAtlasId(), userIds, false);
        return graph;
    }

    @Transactional(transactionManager = "postgresTransactionManager")
    public GraphDTO save(GraphSaveRequest request)
        throws GraphIdInvalidException, GraphContentTooLargeException, GraphParentInvalidException,
        GraphRevConflictException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException,
        BadRequestException {
        atlasService.requireEditable(request.getAtlasId(), request.getAllUserIds());
        validateGraphId(request.getGraphId());
        String contentJson = validateAndSerializeContent(request.getContent());
        enforceSize(contentJson, graphConfig.getMaxContentBytes());

        GraphDTO existing = graphRepository.findByGraphIdIncludingDeleted(request.getGraphId());
        if (existing != null && existing.getDeleted() != null && existing.getDeleted() != 0) {
            throw new GraphIdInvalidException("图 ID 已存在（含已删除）");
        }
        if (existing == null) {
            return createGraph(request, contentJson);
        }
        return updateGraph(request, existing, contentJson);
    }

    public List<GraphSaveResultVO> batchSave(GraphBatchSaveRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, BadRequestException,
        GraphContentTooLargeException {
        atlasService.requireEditable(request.getAtlasId(), request.getAllUserIds());
        List<GraphBatchItemRequest> graphs = request.getGraphs();
        if (graphs.size() > graphConfig.getMaxBatchSize()) {
            throw new BadRequestException("单次最多保存" + graphConfig.getMaxBatchSize() + "张图");
        }

        long totalBytes = 0;
        for (GraphBatchItemRequest item : graphs) {
            if (item.getContent() != null) {
                try {
                    String json = OBJECT_MAPPER.writeValueAsString(item.getContent());
                    totalBytes += json.getBytes(StandardCharsets.UTF_8).length;
                } catch (JsonProcessingException ignored) {
                    // per-item validation will surface the error
                }
            }
        }
        if (totalBytes > graphConfig.getMaxBatchBytes()) {
            throw new GraphContentTooLargeException("批量保存总体积超出限制");
        }

        List<GraphSaveResultVO> results = new ArrayList<>(graphs.size());
        for (GraphBatchItemRequest item : graphs) {
            results.add(saveOneInTx(request, item));
        }
        return results;
    }

    @Transactional(transactionManager = "postgresTransactionManager")
    public GraphDTO rename(GraphRenameRequest request)
        throws GraphNotFoundException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException {
        GraphDTO graph = requireActiveGraph(request.getGraphId());
        atlasService.requireEditable(graph.getAtlasId(), request.getAllUserIds());
        long now = System.currentTimeMillis();
        graphRepository.updateName(graph.getGraphId(), request.getName().trim(), now);
        return graphRepository.findActiveByGraphId(graph.getGraphId());
    }

    @Transactional(transactionManager = "postgresTransactionManager")
    public GraphDTO move(GraphMoveRequest request)
        throws GraphNotFoundException, GraphParentInvalidException, AtlasNotFoundException, AtlasForbiddenException,
        AtlasNotVisibleException {
        GraphDTO graph = requireActiveGraph(request.getGraphId());
        atlasService.requireEditable(graph.getAtlasId(), request.getAllUserIds());
        validateParent(graph.getAtlasId(), graph.getGraphId(), request.getParentGraphId(), true);
        long now = System.currentTimeMillis();
        graphRepository.updateParent(graph.getGraphId(), request.getParentGraphId(), now);
        return graphRepository.findActiveByGraphId(graph.getGraphId());
    }

    @Transactional(transactionManager = "postgresTransactionManager")
    public List<String> delete(String graphId, List<Long> userIds)
        throws GraphNotFoundException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException {
        GraphDTO graph = requireActiveGraph(graphId);
        atlasService.requireEditable(graph.getAtlasId(), userIds);

        List<GraphParentLink> links = graphRepository.listParentLinks(graph.getAtlasId());
        List<String> toDelete = collectDescendants(graphId, links);
        graphRepository.logicalDeleteByIds(toDelete, System.currentTimeMillis());
        return toDelete;
    }

    public List<GraphDTO> listContent(GraphListRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, AtlasNotPublishedException,
        GraphContentTooLargeException {
        atlasService.requireReadable(request.getAtlasId(), request.getAllUserIds(), false);
        List<GraphDTO> rows = graphRepository.listContentByAtlasId(request.getAtlasId());
        long total = 0;
        for (GraphDTO row : rows) {
            if (row.getContent() != null) {
                total += row.getContent().toString().getBytes(StandardCharsets.UTF_8).length;
            }
        }
        if (total > graphConfig.getMaxListContentBytes()) {
            throw new GraphContentTooLargeException("图集内容总体积超出限制，请改用逐图拉取");
        }
        return rows;
    }

    private GraphSaveResultVO saveOneInTx(GraphBatchSaveRequest batch, GraphBatchItemRequest item) {
        try {
            return postgresTx.execute(status -> {
                try {
                    validateGraphId(item.getGraphId());
                    String contentJson = validateAndSerializeContent(item.getContent());
                    enforceSize(contentJson, graphConfig.getMaxContentBytes());

                    GraphSaveRequest single = new GraphSaveRequest(batch);
                    single.setGraphId(item.getGraphId());
                    single.setAtlasId(batch.getAtlasId());
                    single.setName(item.getName());
                    single.setParentGraphId(item.getParentGraphId());
                    single.setRev(item.getRev());
                    single.setContent(item.getContent());
                    GraphDTO saved = saveInternal(single, contentJson);
                    return new GraphSaveResultVO(saved.getGraphId(), true, saved.getRev(), ErrorCodes.SUCCESS);
                } catch (GraphRevConflictException e) {
                    status.setRollbackOnly();
                    return new GraphSaveResultVO(item.getGraphId(), false, e.getRev(), e.getCode());
                } catch (GraphIdInvalidException e) {
                    status.setRollbackOnly();
                    return new GraphSaveResultVO(item.getGraphId(), false, item.getRev(), e.getCode());
                } catch (GraphParentInvalidException e) {
                    status.setRollbackOnly();
                    return new GraphSaveResultVO(item.getGraphId(), false, item.getRev(), e.getCode());
                } catch (GraphContentTooLargeException e) {
                    status.setRollbackOnly();
                    return new GraphSaveResultVO(item.getGraphId(), false, item.getRev(), e.getCode());
                } catch (BadRequestException e) {
                    status.setRollbackOnly();
                    return new GraphSaveResultVO(item.getGraphId(), false, item.getRev(), e.getCode());
                } catch (RuntimeException e) {
                    status.setRollbackOnly();
                    log.warn("batch-save failed for graphId={}", item.getGraphId(), e);
                    return new GraphSaveResultVO(item.getGraphId(), false, item.getRev(), ErrorCodes.UNKNOWN);
                }
            });
        } catch (Exception e) {
            log.warn("batch-save tx failed for graphId={}", item.getGraphId(), e);
            return new GraphSaveResultVO(item.getGraphId(), false, item.getRev(), ErrorCodes.UNKNOWN);
        }
    }

    private GraphDTO saveInternal(GraphSaveRequest request, String contentJson)
        throws GraphIdInvalidException, GraphContentTooLargeException, GraphParentInvalidException,
        GraphRevConflictException, BadRequestException {
        GraphDTO existing = graphRepository.findByGraphIdIncludingDeleted(request.getGraphId());
        if (existing != null && existing.getDeleted() != null && existing.getDeleted() != 0) {
            throw new GraphIdInvalidException("图 ID 已存在（含已删除）");
        }
        if (existing == null) {
            return createGraph(request, contentJson);
        }
        return updateGraph(request, existing, contentJson);
    }

    private GraphDTO createGraph(GraphSaveRequest request, String contentJson)
        throws GraphIdInvalidException, GraphParentInvalidException, BadRequestException {
        if (request.getRev() != null && request.getRev() != 0) {
            throw new BadRequestException("创建图时 rev 必须为 0");
        }
        validateParent(request.getAtlasId(), request.getGraphId(), request.getParentGraphId(), false);

        long now = System.currentTimeMillis();
        GraphDTO dto = new GraphDTO();
        dto.setGraphId(request.getGraphId());
        dto.setAtlasId(request.getAtlasId());
        dto.setName(request.getName().trim());
        dto.setParentGraphId(request.getParentGraphId());
        dto.setContent(request.getContent());
        dto.setRev(1);
        dto.setDeleted(0);
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        try {
            // ensure content string path for insert mapping
            GraphDTO insert = new GraphDTO();
            insert.setGraphId(dto.getGraphId());
            insert.setAtlasId(dto.getAtlasId());
            insert.setName(dto.getName());
            insert.setParentGraphId(dto.getParentGraphId());
            insert.setContent(OBJECT_MAPPER.readTree(contentJson));
            insert.setRev(1);
            insert.setDeleted(0);
            insert.setCreatedAt(now);
            insert.setUpdatedAt(now);
            graphRepository.insert(insert);
        } catch (DuplicateKeyException e) {
            throw new GraphIdInvalidException("图 ID 已存在");
        } catch (JsonProcessingException e) {
            throw new BadRequestException("content 不是合法 JSON");
        }
        return graphRepository.findActiveByGraphId(request.getGraphId());
    }

    private GraphDTO updateGraph(GraphSaveRequest request, GraphDTO existing, String contentJson)
        throws GraphRevConflictException, BadRequestException {
        if (!existing.getAtlasId().equals(request.getAtlasId())) {
            throw new BadRequestException("不允许跨图集修改归属");
        }
        int expected = request.getRev() == null ? -1 : request.getRev();
        int newRev = existing.getRev() + 1;
        long now = System.currentTimeMillis();
        boolean ok = graphRepository.updateContent(
            existing.getGraphId(), request.getName().trim(), contentJson, expected, newRev, now);
        if (!ok) {
            GraphDTO current = graphRepository.findActiveByGraphId(existing.getGraphId());
            int currentRev = current == null ? existing.getRev() : current.getRev();
            throw new GraphRevConflictException(currentRev);
        }
        return graphRepository.findActiveByGraphId(existing.getGraphId());
    }

    private GraphDTO requireActiveGraph(String graphId) throws GraphNotFoundException {
        GraphDTO graph = graphRepository.findActiveByGraphId(graphId);
        if (graph == null) {
            throw new GraphNotFoundException();
        }
        return graph;
    }

    private void validateGraphId(String graphId) throws GraphIdInvalidException {
        if (!StringUtils.hasText(graphId) || !GRAPH_ID_PATTERN.matcher(graphId).matches()) {
            throw new GraphIdInvalidException("图 ID 格式非法");
        }
    }

    private String validateAndSerializeContent(JsonNode content) throws BadRequestException {
        if (content == null || !content.isObject()) {
            throw new BadRequestException("content 必须为 JSON 对象");
        }
        JsonNode nodes = content.get("nodes");
        JsonNode edges = content.get("edges");
        if (nodes == null || !nodes.isArray() || edges == null || !edges.isArray()) {
            throw new BadRequestException("content 顶层必须包含 nodes / edges 数组");
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("content 不是合法 JSON");
        }
    }

    private void enforceSize(String contentJson, int maxBytes) throws GraphContentTooLargeException {
        if (contentJson.getBytes(StandardCharsets.UTF_8).length > maxBytes) {
            throw new GraphContentTooLargeException();
        }
    }

    private void validateParent(Long atlasId, String graphId, String parentGraphId, boolean checkCycle)
        throws GraphParentInvalidException {
        if (parentGraphId == null) {
            return;
        }
        if (!StringUtils.hasText(parentGraphId) || !GRAPH_ID_PATTERN.matcher(parentGraphId).matches()) {
            throw new GraphParentInvalidException();
        }
        if (parentGraphId.equals(graphId)) {
            throw new GraphParentInvalidException();
        }
        GraphDTO parent = graphRepository.findActiveByGraphId(parentGraphId);
        if (parent == null || !atlasId.equals(parent.getAtlasId())) {
            throw new GraphParentInvalidException();
        }
        if (checkCycle) {
            Map<String, String> parentMap = new HashMap<>();
            for (GraphParentLink link : graphRepository.listParentLinks(atlasId)) {
                parentMap.put(link.getGraphId(), link.getParentGraphId());
            }
            String cur = parentGraphId;
            int guard = 0;
            while (cur != null) {
                if (cur.equals(graphId)) {
                    throw new GraphParentInvalidException();
                }
                if (++guard > graphConfig.getMaxParentChainDepth()) {
                    throw new GraphParentInvalidException();
                }
                cur = parentMap.get(cur);
            }
        }
    }

    private List<String> collectDescendants(String rootId, List<GraphParentLink> links) {
        Map<String, List<String>> children = new HashMap<>();
        for (GraphParentLink link : links) {
            if (link.getParentGraphId() == null) {
                continue;
            }
            children.computeIfAbsent(link.getParentGraphId(), k -> new ArrayList<>()).add(link.getGraphId());
        }
        List<String> ordered = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        dfs(rootId, children, ordered, visited);
        return ordered;
    }

    private void dfs(String id, Map<String, List<String>> children, List<String> ordered, Set<String> visited) {
        if (!visited.add(id)) {
            return;
        }
        ordered.add(id);
        for (String child : children.getOrDefault(id, List.of())) {
            dfs(child, children, ordered, visited);
        }
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
