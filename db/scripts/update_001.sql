create table if not exists post (
    id serial primary key,
    name text,
    link varchar(255) unique,
    text text,
    created timestamp
);