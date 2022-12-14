CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name  VARCHAR(255)                            NOT NULL,
    email VARCHAR(512)                            NOT NULL UNIQUE,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS requests
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    requester_id BIGINT,
    description  VARCHAR(255)                            NOT NULL,
    created      TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    CONSTRAINT fk_requests_to_items FOREIGN KEY (requester_id) REFERENCES users (id),
    CONSTRAINT pk_requests PRIMARY KEY (id));
CREATE TABLE IF NOT EXISTS items
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name        VARCHAR(255)                            NOT NULL,
    description VARCHAR(255)                            NOT NULL,
    owner_id    BIGINT,
    available   boolean,
    request_id  BIGINT,
    CONSTRAINT fk_items_to_users FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT fk_items_to_requests FOREIGN KEY (request_id) REFERENCES requests(id),
    CONSTRAINT pk_item PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS bookings
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    item_id      BIGINT,
    booking_from TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    booking_to   TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    booker_id    BIGINT,
    status       VARCHAR(50)                             NOT NULL,
    CONSTRAINT fk_bookers_to_items FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT fk_bookers_to_users FOREIGN KEY (booker_id) REFERENCES users (id),
    CONSTRAINT pk_bookers PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS comments
(
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    item_id   BIGINT,
    text      VARCHAR(100)                            NOT NULL,
    created   TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    author_id BIGINT,
    CONSTRAINT fk_comments_to_items FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT fk_comments_to_users FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT pk_comments PRIMARY KEY (id)
);

