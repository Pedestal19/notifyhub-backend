create table if not exists inbound_message (
    id uuid primary key,
    channel varchar(20) not null,
    phone_number varchar(32) not null,
    body text not null,
    status varchar(20) not null,
    received_at timestamptz not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
    );

create index if not exists idx_inbound_message_status on inbound_message(status);
create index if not exists idx_inbound_message_channel on inbound_message(channel);
create index if not exists idx_inbound_message_received_at on inbound_message(received_at desc);
create index if not exists idx_inbound_message_phone_number on inbound_message(phone_number);
