DROP TABLE IF EXISTS event;
DROP TABLE IF EXISTS tb_user;
create TABLE tb_user(
id SERIAL,
email varchar(100) NOT NULL ,
password varchar(100) NOT NULL ,
primary key (id)
);
CREATE TABLE  event(
id SERIAL,
title varchar(100) NOT NULL,
description varchar(255) NOT NULL ,
price varchar(100) NOT NULL ,
date timestamp not null,
creator_id integer not null,
primary key (id),
constraint fk_created_id foreign key (creator_id) references tb_user(id)
);

