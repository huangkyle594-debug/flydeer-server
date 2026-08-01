package com.flydeer.api.user.mapper;

import com.flydeer.contract.user.vo.DelegateVO;
import com.flydeer.repository.mysql.dto.UserDelegateDTO;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserDelegateMapper {

    UserDelegateMapper INSTANCE = Mappers.getMapper(UserDelegateMapper.class);

    DelegateVO toVO(UserDelegateDTO dto);

    List<DelegateVO> toVOList(List<UserDelegateDTO> dtos);

    default Instant map(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
