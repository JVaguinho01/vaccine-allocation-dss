-- VaccDSS database schema (PostgreSQL + PostGIS on Supabase)
-- Run in the Supabase SQL editor of a new project.

create extension if not exists postgis with schema extensions;

create table roles (
    id   serial primary key,
    name varchar not null unique
);

create table menus (
    id   serial primary key,
    name varchar not null,
    slug varchar not null unique
);

create table role_menus (
    role_id int not null references roles (id) on delete cascade,
    menu_id int not null references menus (id) on delete cascade,
    primary key (role_id, menu_id)
);

-- Links a Supabase Auth user to an application role
create table user_profiles (
    id         uuid primary key references auth.users (id) on delete cascade,
    role_id    int not null references roles (id),
    created_at timestamp not null default now()
);

create table regions (
    id         uuid primary key default gen_random_uuid(),
    name       text not null,
    boundary   geometry(MultiPolygon, 4326),
    created_at timestamp not null default now()
);

create table institutions (
    id                     uuid primary key default gen_random_uuid(),
    name                   text not null,
    region_id              uuid not null references regions (id),
    location               geography(Point, 4326),
    storage_capacity       int,
    daily_vaccination_rate int,
    address                text,
    zip_code               text,
    removed                boolean not null default false,
    created_at             timestamp not null default now()
);

create table patients (
    id            uuid primary key default gen_random_uuid(),
    first_name    text not null,
    last_name     text not null,
    birth_date    date,
    gender        text check (gender in ('M', 'F', 'O')),
    region_id     uuid not null references regions (id),
    address       text,
    zip_code      text,
    location      geography(Point, 4326),
    risk_level    smallint not null default 1 check (risk_level between 1 and 5),
    risk_exposure smallint not null default 1 check (risk_exposure between 1 and 5),
    is_active     boolean not null default true,
    created_at    timestamp not null default now(),
    updated_at    timestamp not null default now()
);

create table vaccines (
    id             uuid primary key default gen_random_uuid(),
    name           text not null,
    required_doses int not null default 1,
    cost           numeric(10, 2),
    created_at     timestamp not null default now()
);

create table criteria (
    id   serial primary key,
    name varchar not null unique
);

create index institutions_region_idx   on institutions (region_id);
create index institutions_location_idx on institutions using gist (location);
create index patients_region_idx       on patients (region_id) where is_active;
create index patients_location_idx     on patients using gist (location);
