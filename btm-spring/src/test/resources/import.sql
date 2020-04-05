create table a (a varchar 200);
create table b (a varchar 200);

create table hibernate_sequences (sequence_name varchar(200), sequence_next_hi_value bigint);
insert into hibernate_sequences (sequence_name, sequence_next_hi_value) values ('test_entity_1', 1);
