DO
$$
BEGIN
    IF NOT EXISTS(
        SELECT FROM pg_user WHERE usename = 'station_sa'
    )
    THEN
        CREATE USER station_sa
        WITH PASSWORD 'password';
    END IF;
    CREATE SCHEMA IF NOT EXISTS station;
    ALTER SCHEMA station OWNER TO station_sa;
    GRANT ALL PRIVILEGES ON SCHEMA station TO station_sa;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA station TO station_sa;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA station TO station_sa;
END
$$;