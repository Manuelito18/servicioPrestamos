package com.fernandez_terrones.prestamoservice.repository;

import com.fernandez_terrones.prestamoservice.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findAllByUsuarioIdOrderByIdDesc(Long usuarioId);

    Optional<Prestamo> findByIdAndUsuarioId(Long id, Long usuarioId);
}
