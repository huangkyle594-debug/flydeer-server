package com.flydeer.repository.mysql.mapper;

import com.flydeer.contract.atlas.request.AtlasQuery;
import com.flydeer.contract.common.request.PageRequest;
import com.flydeer.repository.mysql.entity.AtlasEntity;
import java.util.List;

public interface AtlasExtMapper {

    List<AtlasEntity> pageQuery(PageRequest<AtlasQuery> request);
}
