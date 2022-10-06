CREATE TABLE if not exists weight(
  id BIGSERIAL PRIMARY KEY,
  value real,
  level smallint,
  neuron smallint
);