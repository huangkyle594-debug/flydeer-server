package com.flydeer.service.graph;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flydeer.common.exception.request.GraphIdInvalidException;
import com.flydeer.common.exception.request.GraphParentInvalidException;
import com.flydeer.common.exception.request.GraphRevConflictException;
import com.flydeer.contract.graph.request.GraphSaveRequest;
import com.flydeer.repository.mysql.dto.AtlasDTO;
import com.flydeer.repository.postgres.dto.GraphDTO;
import com.flydeer.repository.postgres.repository.GraphRepository;
import com.flydeer.service.atlas.AtlasService;
import com.flydeer.service.graph.config.GraphConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private GraphRepository graphRepository;
    @Mock
    private AtlasService atlasService;
    @Mock
    private PlatformTransactionManager postgresTransactionManager;

    private GraphService graphService;

    @BeforeEach
    void setUp() {
        lenient().when(postgresTransactionManager.getTransaction(any(TransactionDefinition.class)))
            .thenReturn(new SimpleTransactionStatus());
        lenient().doAnswer(inv -> null).when(postgresTransactionManager).commit(any());
        lenient().doAnswer(inv -> null).when(postgresTransactionManager).rollback(any());

        GraphConfig config = new GraphConfig();
        graphService = new GraphService(graphRepository, atlasService, config, postgresTransactionManager);
    }

    @Test
    void saveCreatesGraphWhenMissing() throws Exception {
        GraphSaveRequest request = baseSaveRequest("gph_a1b2c3d4e5f6", 0);
        when(atlasService.requireEditable(eq(1L), any())).thenReturn(new AtlasDTO());
        when(graphRepository.findByGraphIdIncludingDeleted("gph_a1b2c3d4e5f6")).thenReturn(null);
        when(graphRepository.findActiveByGraphId("gph_a1b2c3d4e5f6")).thenAnswer(inv -> {
            GraphDTO dto = new GraphDTO();
            dto.setGraphId("gph_a1b2c3d4e5f6");
            dto.setAtlasId(1L);
            dto.setName("总览");
            dto.setRev(1);
            dto.setNodeCount(0);
            dto.setCreatedAt(1L);
            dto.setUpdatedAt(1L);
            return dto;
        });

        GraphDTO saved = graphService.save(request);

        ArgumentCaptor<GraphDTO> captor = ArgumentCaptor.forClass(GraphDTO.class);
        verify(graphRepository).insert(captor.capture());
        assertThat(captor.getValue().getRev()).isEqualTo(1);
        assertThat(saved.getGraphId()).isEqualTo("gph_a1b2c3d4e5f6");
    }

    @Test
    void saveRejectsInvalidGraphId() {
        GraphSaveRequest request = baseSaveRequest("bad_id", 0);
        assertThatThrownBy(() -> graphService.save(request)).isInstanceOf(GraphIdInvalidException.class);
    }

    @Test
    void saveThrowsRevConflictOnStaleUpdate() throws Exception {
        GraphSaveRequest request = baseSaveRequest("gph_a1b2c3d4e5f6", 1);
        when(atlasService.requireEditable(eq(1L), any())).thenReturn(new AtlasDTO());

        GraphDTO existing = new GraphDTO();
        existing.setGraphId("gph_a1b2c3d4e5f6");
        existing.setAtlasId(1L);
        existing.setRev(3);
        existing.setDeleted(0);
        when(graphRepository.findByGraphIdIncludingDeleted("gph_a1b2c3d4e5f6")).thenReturn(existing);
        when(graphRepository.updateContent(anyString(), anyString(), anyString(), anyInt(), anyInt(), anyLong()))
            .thenReturn(false);
        when(graphRepository.findActiveByGraphId("gph_a1b2c3d4e5f6")).thenReturn(existing);

        assertThatThrownBy(() -> graphService.save(request))
            .isInstanceOf(GraphRevConflictException.class)
            .extracting(ex -> ((GraphRevConflictException) ex).getRev())
            .isEqualTo(3);
    }

    @Test
    void moveRejectsSelfParent() throws Exception {
        GraphDTO existing = new GraphDTO();
        existing.setGraphId("gph_a1b2c3d4e5f6");
        existing.setAtlasId(1L);
        existing.setDeleted(0);
        when(graphRepository.findActiveByGraphId("gph_a1b2c3d4e5f6")).thenReturn(existing);
        when(atlasService.requireEditable(eq(1L), any())).thenReturn(new AtlasDTO());

        com.flydeer.contract.graph.request.GraphMoveRequest move =
            new com.flydeer.contract.graph.request.GraphMoveRequest();
        move.setGraphId("gph_a1b2c3d4e5f6");
        move.setParentGraphId("gph_a1b2c3d4e5f6");
        move.setAllUserIds(List.of(9L));

        assertThatThrownBy(() -> graphService.move(move)).isInstanceOf(GraphParentInvalidException.class);
    }

    private GraphSaveRequest baseSaveRequest(String graphId, int rev) {
        ObjectNode content = MAPPER.createObjectNode();
        content.putArray("nodes");
        content.putArray("edges");

        GraphSaveRequest request = new GraphSaveRequest();
        request.setGraphId(graphId);
        request.setAtlasId(1L);
        request.setName("总览");
        request.setParentGraphId(null);
        request.setRev(rev);
        request.setContent(content);
        request.setAllUserIds(List.of(9L));
        return request;
    }
}
