-- Reference only: Flyway is not on the classpath; Hibernate (ddl-auto=update) creates these columns.
-- UI preferences per user. NULL means "not chosen yet": the API returns the defaults ('en', 'light')
-- with languageSaved/themeSaved = false, so the client keeps a cached or browser choice until the first save.
ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_language VARCHAR(5);
ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_theme VARCHAR(10);
