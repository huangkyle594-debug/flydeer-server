package com.flydeer.structmind.repository.mysql.repository;

import com.flydeer.structmind.repository.mysql.mapper.UserInfoMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class UserInfoRepository {

    private final UserInfoMapper userInfoMapper;



}
