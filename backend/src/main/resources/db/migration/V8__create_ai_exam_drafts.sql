ALTER TABLE source_documents
    ADD COLUMN document_kind VARCHAR(30) NOT NULL DEFAULT 'KNOWLEDGE';

ALTER TABLE source_documents
    ADD CONSTRAINT ck_source_documents_kind
        CHECK (document_kind IN ('KNOWLEDGE', 'SAMPLE_EXAM'));

CREATE TABLE ai_exam_drafts (
    id UUID PRIMARY KEY,
    exam_type VARCHAR(30) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    grade_level INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    teacher_instructions VARCHAR(2000),
    status VARCHAR(20) NOT NULL,
    provider VARCHAR(30) NOT NULL,
    model_name VARCHAR(120) NOT NULL,
    content_json JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_ai_exam_drafts_type CHECK (exam_type IN ('FIFTEEN_MINUTE', 'MIDTERM', 'FINAL', 'FULL_YEAR', 'GRADUATION_EXAM')),
    CONSTRAINT ck_ai_exam_drafts_duration CHECK (duration_minutes IN (15, 45, 60)),
    CONSTRAINT ck_ai_exam_drafts_grade CHECK (grade_level BETWEEN 1 AND 12),
    CONSTRAINT ck_ai_exam_drafts_status CHECK (status IN ('DRAFT'))
);

CREATE TABLE ai_exam_draft_sources (
    draft_id UUID NOT NULL REFERENCES ai_exam_drafts(id) ON DELETE RESTRICT,
    document_id UUID NOT NULL REFERENCES source_documents(id) ON DELETE RESTRICT,
    source_role VARCHAR(20) NOT NULL,
    PRIMARY KEY (draft_id, document_id),
    CONSTRAINT ck_ai_exam_draft_sources_role CHECK (source_role IN ('KNOWLEDGE', 'SAMPLE'))
);

CREATE INDEX idx_ai_exam_drafts_created ON ai_exam_drafts (created_at DESC);
CREATE INDEX idx_ai_exam_draft_sources_document ON ai_exam_draft_sources (document_id);
