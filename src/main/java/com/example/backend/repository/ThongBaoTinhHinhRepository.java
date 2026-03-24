package com.example.backend.repository;

import com.example.backend.entity.ThongBaoTinhHinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ThongBaoTinhHinhRepository extends JpaRepository<ThongBaoTinhHinh, UUID>, JpaSpecificationExecutor<ThongBaoTinhHinh> {
    // JpaSpecificationExecutor allows us to run complex dynamic queries (search, filter, pagination)
}
