    package com.TenX.Automobile.repository;

    import com.TenX.Automobile.entity.UserEntity;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import java.util.Optional;
    import java.util.UUID;

    @Repository
    public interface BaseUserRepository extends JpaRepository<UserEntity, UUID> {
        Optional<UserEntity> findByEmail(String email);

    }
