package moh.gov.zm.lis.iam.repository;

import moh.gov.zm.lis.iam.entity.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findByUserId(UUID userId);
}
