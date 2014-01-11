
CREATE KEYSPACE events WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1 };

USE events;

CREATE TABLE event (
  id UUID,
  title TEXT,
  category TEXT,
  description TEXT,
  startTime TIMESTAMP,
  duration INT,
  PRIMARY KEY (id)
);