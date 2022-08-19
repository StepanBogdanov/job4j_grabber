create table if not exist rabbit (
    id serial primary key,
    created_date timestamp default current_timestamp
);