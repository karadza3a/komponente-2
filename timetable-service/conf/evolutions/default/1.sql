# --- !Ups

create table "lessons" (
  "id" bigint generated by default as identity(start with 1) not null primary key,
  "subject" varchar not null,
  "category" varchar not null,
  "professor" varchar not null,
  "groups" varchar not null,
  "dayOfWeek" varchar not null,
  "timeStart" varchar not null,
  "timeEnd" varchar not null,
  "room" varchar not null,
);

insert into "lessons" ("id","subject","category","professor","groups","dayOfWeek","timeStart","timeEnd","room") values (1,'pred','kat','profa','grupa','FRIDAY','15:15:00.000','17:00:00.000','U3');

# --- !Downs

drop table "lessons" if exists;
