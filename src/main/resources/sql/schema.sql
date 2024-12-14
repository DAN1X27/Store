create table Person
(
    id         integer generated always as identity primary key,
    username   varchar   not null unique,
    email      varchar   not null unique,
    password   varchar   not null,
    created_at timestamp not null,
    role       varchar   not null,
    status     varchar   not null
);

create table item
(
    id          integer generated always as identity primary key,
    name        varchar          not null unique,
    count       integer check (count >= 0),
    price       double precision,
    category    varchar          not null,
    description varchar          not null,
    rating      double precision not null
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
    order_id int references Orders (id) on delete cascade,
    primary key (item_id, order_id)
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
    id         varchar primary key,
    status     varchar                    not null,
    user_id    int references person (id) not null,
    expired_at date                       not null
);

create table Cart
(
    id       int generated always as identity primary key,
    owner_id int references person (id) on delete cascade unique,
    price    double precision not null
);

create table Item_Cart
(
    cart_id int references cart (id) on delete cascade,
    item_id int references item (id) on delete cascade,
    primary key (cart_id, item_id)
);

create table Cart_Items
(
    id          int generated always as identity primary key,
    cart_id     int references cart (id) on delete cascade,
    item_id     int references item (id) on delete cascade,
    items_count int not null
);

create table items_reviews
(
    id         integer generated always as identity primary key,
    item_id    integer not null references item (id) on delete cascade,
    owner_id   integer not null references person on delete cascade,
    likes      integer not null,
    comment    varchar not null,
    created_at date    not null,
    grade      integer not null
);

create table liked_reviews
(
    id        integer generated always as identity primary key,
    owner_id  integer references person (id) on delete cascade,
    review_id integer references items_reviews (id) on delete cascade
);

create table items_grades
(
    id       integer generated always as identity primary key,
    item_id  integer references item (id) on delete cascade,
    grade    integer not null,
    owner_id integer references person (id)
);

create table items_images
(
    id         bigint generated always as identity primary key,
    item_id    integer references item,
    image_uuid varchar not null unique
);









