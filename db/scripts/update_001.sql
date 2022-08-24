create table if not exist post (
    id serial primary key,
    name text,
    text text,
    link text unique,
    created timestamp
);