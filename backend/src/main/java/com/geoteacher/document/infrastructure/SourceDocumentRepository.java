package com.geoteacher.document.infrastructure;

import com.geoteacher.document.domain.SourceDocument;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SourceDocumentRepository extends JpaRepository<SourceDocument, UUID> {

    @EntityGraph(attributePaths = "fileAsset")
    List<SourceDocument> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"fileAsset", "chunks"})
    @Query("select document from SourceDocument document where document.id = :id")
    Optional<SourceDocument> findDetailById(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"fileAsset", "chunks"})
    @Query("select distinct document from SourceDocument document where document.id in :ids")
    List<SourceDocument> findAllDetailByIdIn(@Param("ids") List<UUID> ids);
}
