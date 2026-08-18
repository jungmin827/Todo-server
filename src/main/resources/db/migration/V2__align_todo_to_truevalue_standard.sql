-- 트루밸류 백엔드 표준 정렬
--   테이블: todo_entity -> todo (단수, 07_ENTITY_GUIDE + 레퍼런스 구현 기준)
--   PK:     id -> idx (id는 PK가 아니라 비즈니스 식별자 전용)
--   감사:   created_date/modified_date -> register_date/modify_date
-- ddl-auto: validate 고정이므로 스키마는 이 스크립트로만 바꾼다.

ALTER TABLE todo_entity RENAME TO todo;

ALTER TABLE todo RENAME COLUMN id TO idx;
ALTER TABLE todo RENAME COLUMN created_date TO register_date;
ALTER TABLE todo RENAME COLUMN modified_date TO modify_date;

ALTER TABLE todo ALTER COLUMN title TYPE VARCHAR(200);
ALTER TABLE todo ALTER COLUMN body TYPE VARCHAR(2000);
