CREATE TABLE questions (
    id UUID PRIMARY KEY,
    question_type VARCHAR(30) NOT NULL,
    grade_level SMALLINT NOT NULL,
    topic VARCHAR(150) NOT NULL,
    cognitive_level VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    content_normalized TEXT NOT NULL,
    explanation TEXT,
    suggested_score NUMERIC(7,3),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_questions_type CHECK (question_type IN ('SINGLE_CHOICE', 'TRUE_FALSE', 'SHORT_ANSWER', 'ESSAY')),
    CONSTRAINT ck_questions_grade CHECK (grade_level BETWEEN 1 AND 12),
    CONSTRAINT ck_questions_cognitive CHECK (cognitive_level IN ('RECOGNITION', 'COMPREHENSION', 'APPLICATION', 'HIGH_APPLICATION')),
    CONSTRAINT ck_questions_status CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED')),
    CONSTRAINT ck_questions_score CHECK (suggested_score IS NULL OR suggested_score > 0)
);

CREATE INDEX idx_questions_search ON questions (grade_level, question_type, status);
CREATE INDEX idx_questions_topic ON questions (topic);

CREATE TABLE question_options (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    option_key VARCHAR(10) NOT NULL,
    content TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    position INTEGER NOT NULL,
    CONSTRAINT uk_question_options_key UNIQUE (question_id, option_key),
    CONSTRAINT uk_question_options_position UNIQUE (question_id, position),
    CONSTRAINT ck_question_options_position CHECK (position >= 0)
);

CREATE INDEX idx_question_options_question ON question_options (question_id, position);
