CREATE TABLE students (
    id UUID PRIMARY KEY,
    student_code VARCHAR(50) NOT NULL,
    student_code_normalized VARCHAR(50) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    full_name_normalized VARCHAR(150) NOT NULL,
    date_of_birth DATE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_students_code_normalized UNIQUE (student_code_normalized),
    CONSTRAINT ck_students_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

CREATE INDEX idx_students_full_name_normalized
    ON students (full_name_normalized);

CREATE TABLE class_enrollments (
    id UUID PRIMARY KEY,
    class_group_id UUID NOT NULL REFERENCES class_groups(id) ON DELETE RESTRICT,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE RESTRICT,
    enrolled_at DATE NOT NULL,
    left_at DATE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_class_enrollments_class_student UNIQUE (class_group_id, student_id),
    CONSTRAINT ck_class_enrollments_dates CHECK (left_at IS NULL OR left_at >= enrolled_at),
    CONSTRAINT ck_class_enrollments_status CHECK (status IN ('ACTIVE', 'LEFT')),
    CONSTRAINT ck_class_enrollments_left_state CHECK (
        (status = 'ACTIVE' AND left_at IS NULL)
        OR (status = 'LEFT' AND left_at IS NOT NULL)
    )
);

CREATE INDEX idx_class_enrollments_class_status
    ON class_enrollments (class_group_id, status);

CREATE INDEX idx_class_enrollments_student_status
    ON class_enrollments (student_id, status);
