create table public.persistent_logins
(
    username  varchar(64) not null,
    series    varchar(64) not null
        primary key,
    token     varchar(64) not null,
    last_used timestamp   not null
);

alter table public.persistent_logins
    owner to postgres;

create table public.categories
(
    id                 bytea                       not null
        primary key,
    created_at         timestamp(6) with time zone not null,
    name               varchar(255)                not null,
    updated_at         timestamp(6) with time zone not null,
    parent_category_id bytea
        constraint fk9il7y6fehxwunjeepq0n7g5rd
            references public.categories
);

alter table public.categories
    owner to postgres;

create table public.customers
(
    id           bigint                      not null
        primary key,
    created_at   timestamp(6) with time zone not null,
    email        varchar(255)                not null
        constraint ukrfbvkrffamfql7cjmen8v976v
            unique,
    first_name   varchar(100)                not null,
    last_name    varchar(100)                not null,
    phone_number varchar(50),
    updated_at   timestamp(6) with time zone not null,
    uuid         uuid                        not null
        constraint ukenfyfum1en20int6gevojxlln
            unique
);

alter table public.customers
    owner to postgres;

create table public.orders
(
    id               bigint                      not null
        primary key,
    billing_address  varchar(255),
    created_at       timestamp(6) with time zone not null,
    order_date       timestamp(6) with time zone not null,
    shipping_address varchar(255)                not null,
    status           varchar(255)                not null
        constraint orders_status_check
            check ((status)::text = ANY
        ((ARRAY ['PENDING'::character varying, 'AWAITING_PAYMENT'::character varying, 'CANCELED'::character varying, 'PROCESSING'::character varying, 'SHIPPED'::character varying, 'DELIVERED'::character varying, 'FAILED'::character varying])::text[])),
    total_amount     numeric(38, 2)              not null,
    updated_at       timestamp(6) with time zone not null,
    uuid             uuid                        not null,
    customer_id      bigint
        constraint fkpxtb8awmi0dk6smoh2vp1litg
            references public.customers
);

alter table public.orders
    owner to postgres;

create table public.payments
(
    id             bigint not null
        primary key,
    amount         numeric(38, 2),
    created_at     timestamp(6) with time zone,
    currency       varchar(255)
        constraint payments_currency_check
            check ((currency)::text = ANY
        ((ARRAY ['USD'::character varying, 'EUR'::character varying, 'GBP'::character varying, 'JPY'::character varying])::text[])),
    metadata       jsonb,
    payment_method varchar(255)
        constraint payments_payment_method_check
            check ((payment_method)::text = ANY
                   ((ARRAY ['CASH'::character varying, 'CARD'::character varying, 'BANK_TRANSFER'::character varying])::text[])),
    status         varchar(255)
        constraint payments_status_check
            check ((status)::text = ANY
                   ((ARRAY ['PENDING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'REFUNDED'::character varying])::text[])),
    updated_at     timestamp(6) with time zone,
    uuid           uuid   not null,
    order_id       bigint
        constraint fk81gagumt0r8y3rmudcgpbk42l
            references public.orders
);

alter table public.payments
    owner to postgres;

create table public.users
(
    id            bigint       not null
        primary key,
    password_hash varchar(255) not null,
    role          varchar(255) not null
        constraint users_role_check
            check ((role)::text = ANY
        ((ARRAY ['MANAGER'::character varying, 'STAFF'::character varying, 'INTERN'::character varying, 'ADMIN'::character varying])::text[])),
    username      varchar(255) not null
        constraint ukr43af9ap4edm43mmtq01oddj6
            unique,
    uuid          uuid
);

alter table public.users
    owner to postgres;

create table public.products
(
    id                 bigint                      not null
        primary key,
    created_at         timestamp(6) with time zone not null,
    description        varchar(255),
    is_active          boolean                     not null,
    name               varchar(255)                not null,
    updated_at         timestamp(6) with time zone not null,
    category_id        bytea
        constraint fkog2rp4qthbtt2lfyhfo32lsw9
            references public.categories,
    created_by_user_id bigint                      not null
        constraint fkan9lghvq8mcmqx9uejsxc8nbj
            references public.users
);

alter table public.products
    owner to postgres;

create table public.product_variants
(
    id         bigint                      not null
        primary key,
    attributes varchar(255),
    cost_price numeric(10, 2)              not null,
    created_at timestamp(6) with time zone not null,
    price      numeric(10, 2)              not null,
    sku        varchar(100)                not null
        constraint ukq935p2d1pbjm39n0063ghnfgn
            unique,
    updated_at timestamp(6) with time zone not null,
    weight     numeric(8, 2)               not null,
    product_id bigint                      not null
        constraint fkosqitn4s405cynmhb87lkvuau
            references public.products
);

alter table public.product_variants
    owner to postgres;

create table public.inventory
(
    id                 uuid    not null
        primary key,
    last_restocked_at  timestamp(6) with time zone,
    quantity_committed integer not null,
    quantity_on_hand   integer not null,
    reorder_level      integer not null,
    product_variant_id bigint  not null
        constraint uksmhosiiwyweyooj1okkh3tsqs
            unique
        constraint fkrh3ah6qwgo1pipvxjby2a7208
            references public.product_variants
);

alter table public.inventory
    owner to postgres;

create table public.inventory_transactions
(
    id           uuid                                               not null
        primary key,
    created_at   timestamp with time zone default CURRENT_TIMESTAMP not null,
    notes        varchar(255),
    quantity     integer                                            not null,
    type         varchar(255)                                       not null
        constraint inventory_transactions_type_check
            check ((type)::text = ANY
        ((ARRAY ['sale'::character varying, 'restock'::character varying, 'return'::character varying, 'adjustment'::character varying])::text[])),
    inventory_id uuid                                               not null
        constraint fkr7t3nlrgle6438pr92jfl4n8s
            references public.inventory
);

alter table public.inventory_transactions
    owner to postgres;

create table public.order_items
(
    id                 bigint         not null
        primary key,
    line_total         numeric(38, 2) not null,
    price_per_unit     numeric(38, 2) not null,
    quantity           integer        not null,
    uuid               uuid           not null,
    order_id           bigint         not null
        constraint fkbioxgbv59vetrxe0ejfubep1w
            references public.orders,
    product_variant_id bigint         not null
        constraint fkltmtlue0wixrg1cf0xo7x0l4d
            references public.product_variants
);

alter table public.order_items
    owner to postgres;

create table public.warehouses
(
    id         bytea                       not null
        primary key,
    address    varchar(255),
    created_at timestamp(6) with time zone not null,
    name       varchar(255)                not null,
    updated_at timestamp(6) with time zone not null
);

alter table public.warehouses
    owner to postgres;

