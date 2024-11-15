create table Person
(
    id         integer generated always as identity primary key,
    username   varchar   not null unique,
    email      varchar   not null unique,
    password   varchar   not null,
    created_at timestamp not null,
    role       varchar   not null,
    is_banned  boolean   not null
);

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
);

create table Cart(
     id int generated always as identity primary key ,
     owner_id int references person(id) on delete cascade unique,
     price double precision not null
);

create table Item_Cart(
    cart_id int references cart(id) on delete cascade ,
    item_id int references item(id) on delete cascade
);

create table Cart_Items(
    id int generated always as identity primary key ,
    cart_id int references cart(id) on delete cascade ,
    item_id int references item(id) on delete cascade,
    items_count int not null
);


