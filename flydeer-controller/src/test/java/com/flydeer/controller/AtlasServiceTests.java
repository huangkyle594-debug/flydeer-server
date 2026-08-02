package com.flydeer.controller;

import com.flydeer.common.exception.business.AtlasForbiddenException;
import com.flydeer.common.exception.business.AtlasNotFoundException;
import com.flydeer.common.exception.request.BadRequestException;
import com.flydeer.contract.atlas.enums.AtlasStatus;
import com.flydeer.repository.mysql.dto.AtlasDTO;
import com.flydeer.repository.mysql.dto.AtlasQueryDTO;
import com.flydeer.service.atlas.AtlasService;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class AtlasServiceTests {

    @Autowired
    private AtlasService atlasService;

    @Test
    void createListSubmitAndVisibility() throws Exception {
        AtlasDTO created = atlasService.create("架构笔记", "分层", List.of("架构", "入门"), 10001L, "山泽");
        assertEquals(AtlasStatus.draft.name(), created.getStatus());
        assertEquals(List.of("架构", "入门"), created.getTags());

        AtlasQueryDTO anonQuery = new AtlasQueryDTO();
        PageInfo<AtlasDTO> anonPage = atlasService.list(anonQuery, 1, 10);
        assertTrue(anonPage.getList().stream().noneMatch(a -> a.getId().equals(created.getId())));

        AtlasQueryDTO ownerQuery = new AtlasQueryDTO();
        ownerQuery.setViewerId(10001L);
        PageInfo<AtlasDTO> ownerPage = atlasService.list(ownerQuery, 1, 10);
        assertTrue(ownerPage.getList().stream().anyMatch(a -> a.getId().equals(created.getId())));

        atlasService.submitReview(created.getId(), 10001L);
        AtlasDTO pending = atlasService.getVisible(created.getId(), 10001L);
        assertEquals(AtlasStatus.pending.name(), pending.getStatus());

        AtlasDTO rolled = atlasService.update(created.getId(), 10001L, "架构笔记修订", null, null);
        assertEquals(AtlasStatus.draft.name(), rolled.getStatus());
        assertEquals("架构笔记修订", rolled.getName());

        assertThrows(AtlasForbiddenException.class,
            () -> atlasService.getVisible(created.getId(), 10002L));
        assertThrows(AtlasNotFoundException.class,
            () -> atlasService.requireAuthor(999999L, 10001L));
        assertThrows(BadRequestException.class,
            () -> {
                atlasService.submitReview(created.getId(), 10001L);
                atlasService.submitReview(created.getId(), 10001L);
            });

        atlasService.delete(created.getId(), 10001L);
        assertThrows(AtlasNotFoundException.class,
            () -> atlasService.getVisible(created.getId(), 10001L));
        assertFalse(atlasService.listTags().isEmpty());
    }
}
