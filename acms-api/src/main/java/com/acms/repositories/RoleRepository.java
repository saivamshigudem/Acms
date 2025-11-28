package com.acms.repositories;

import com.acms.models.Role;
import com.acms.models.enums.RoleEnum;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends BaseRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);
}
