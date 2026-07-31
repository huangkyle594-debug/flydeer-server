package com.flydeer.structmind.repository.mysql.repository;

import com.flydeer.structmind.repository.mysql.mapper.UserDelegateMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class UserDelegateRepository {

    private final UserDelegateMapper userDelegateMapper;

}
