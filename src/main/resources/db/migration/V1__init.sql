CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE offices (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    city       VARCHAR(255) NOT NULL,
    address    VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE desks (
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    floor     INTEGER      NOT NULL,
    active    BOOLEAN      NOT NULL DEFAULT TRUE,
    office_id BIGINT       NOT NULL REFERENCES offices (id),
    CONSTRAINT uq_desk_name_in_office UNIQUE (office_id, name)
);

CREATE TABLE bookings (
    id           BIGSERIAL PRIMARY KEY,
    desk_id      BIGINT      NOT NULL REFERENCES desks (id),
    user_id      BIGINT      NOT NULL REFERENCES users (id),
    booking_date DATE        NOT NULL,
    status       VARCHAR(20) NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT now()
);

-- one active booking per desk per day and one per user per day
CREATE UNIQUE INDEX uq_active_booking_desk_date ON bookings (desk_id, booking_date) WHERE status = 'ACTIVE';
CREATE UNIQUE INDEX uq_active_booking_user_date ON bookings (user_id, booking_date) WHERE status = 'ACTIVE';

CREATE INDEX idx_desks_office ON desks (office_id);
CREATE INDEX idx_bookings_date ON bookings (booking_date);
