DROP TABLE IF EXISTS currency_conversion;

create table currency_conversion
(
    id bigint  not null constraint currency_conversion_pkey primary key,
    data_cotacao        date,
    data_hora_conversao timestamp,
    moeda_final         varchar(255),
    moeda_origem        varchar(255),
    priority            boolean not null,
    status              integer,
    valor_convertido    double precision,
    valor_desejado      double precision,
    valor_desejado_2      double precision
);
