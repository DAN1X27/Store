create table Item
(
    id       int generated always as identity primary key,
    username varchar not null unique,
    count    int check ( count >= 0),
    price    double precision
);

create table Orders
(
    id               int generated always as identity primary key,
    owner_id         int references person (id) on delete cascade,
    price            int,
    created_at       timestamp not null,
    order_ready_date date      not null,
    is_ready         boolean
);

create table Item_Order
(
    item_id  int references item (id) on delete cascade,
    order_id int references Orders (id) on delete cascade
);

create table Ordered_Items
(
    id          int generated always as identity primary key,
    order_id    int references orders (id) on delete cascade,
    item_id     int references item (id) on delete cascade,
    items_count int
);

create table Tokens
(
    id      varchar primary key,
    status  varchar                    not null,
    user_id int references person (id) not null
)
