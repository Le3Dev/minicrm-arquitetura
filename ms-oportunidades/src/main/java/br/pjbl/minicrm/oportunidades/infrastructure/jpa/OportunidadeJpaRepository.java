package br.pjbl.minicrm.oportunidades.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OportunidadeJpaRepository extends JpaRepository<OportunidadeEntity, Long> {
}
