package org.tib.osl.annotationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tib.osl.annotationservice.domain.Authority;

/**
 * Spring Data JPA repository for the {@link Authority} entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {}
