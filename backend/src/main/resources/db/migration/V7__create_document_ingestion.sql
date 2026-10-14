CREATE TABLE file_assets (
    id UUID PRIMARY KEY,
    storage_provider VARCHAR(30) NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    media_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT NOT NULL,
    sha256 VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_file_assets_storage UNIQUE (storage_provider, storage_key),
    CONSTRAINT ck_file_assets_size CHECK (size_bytes > 0),
    CONSTRAINT ck_file_assets_status CHECK (status IN ('STORED', 'ARCHIVED'))
);

CREATE INDEX idx_file_assets_sha256 ON file_assets (sha256);

CREATE TABLE source_documents (
    id UUID PRIMARY KEY,
    file_asset_id UUID NOT NULL UNIQUE REFERENCES file_assets(id) ON DELETE RESTRICT,
    title VARCHAR(255) NOT NULL,
    grade_level INTEGER,
    status VARCHAR(20) NOT NULL,
    extracted_char_count INTEGER NOT NULL,
    chunk_count INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_source_documents_grade CHECK (grade_level IS NULL OR grade_level BETWEEN 1 AND 12),
    CONSTRAINT ck_source_documents_status CHECK (status IN ('READY', 'ARCHIVED')),
    CONSTRAINT ck_source_documents_counts CHECK (extracted_char_count > 0 AND chunk_count > 0)
);

CREATE INDEX idx_source_documents_status_created ON source_documents (status, created_at DESC);

CREATE TABLE document_chunks (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES source_documents(id) ON DELETE RESTRICT,
    chunk_index INTEGER NOT NULL,
    page_number INTEGER,
    source_label VARCHAR(120) NOT NULL,
    content TEXT NOT NULL,
    char_count INTEGER NOT NULL,
    CONSTRAINT uk_document_chunks_order UNIQUE (document_id, chunk_index),
    CONSTRAINT ck_document_chunks_index CHECK (chunk_index >= 0),
    CONSTRAINT ck_document_chunks_page CHECK (page_number IS NULL OR page_number > 0),
    CONSTRAINT ck_document_chunks_chars CHECK (char_count > 0)
);

CREATE INDEX idx_document_chunks_document ON document_chunks (document_id, chunk_index);
