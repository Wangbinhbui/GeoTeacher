CREATE TABLE school_years (
    id UUID PRIMARY KEY,
    code VARCHAR(30) NOT NULL,
    code_normalized VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_school_years_code_normalized UNIQUE (code_normalized),
    CONSTRAINT ck_school_years_dates CHECK (start_date < end_date),
    CONSTRAINT ck_school_years_status CHECK (status IN ('PLANNED', 'ACTIVE', 'ARCHIVED'))
);

CREATE TABLE class_groups (
    id UUID PRIMARY KEY,
    school_year_id UUID NOT NULL REFERENCES school_years(id) ON DELETE RESTRICT,
    class_code VARCHAR(30) NOT NULL,
    class_code_normalized VARCHAR(30) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    grade_level SMALLINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_class_groups_year_code UNIQUE (school_year_id, class_code_normalized),
    CONSTRAINT ck_class_groups_grade_level CHECK (grade_level BETWEEN 1 AND 12),
    CONSTRAINT ck_class_groups_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

CREATE INDEX idx_class_groups_school_year_status
    ON class_groups (school_year_id, status);
