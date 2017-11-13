DROP TABLE IF EXISTS tweets;

CREATE TABLE tweets (
  id      BIGSERIAL PRIMARY KEY NOT NULL,
  message TEXT                  NOT NULL
);