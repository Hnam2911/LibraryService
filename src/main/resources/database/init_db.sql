--1.Table Reader
create table reader(
                       id uuid PRIMARY KEY ,
                       name varchar(100) not null,
                       phone varchar(15) not null unique ,
                       email varchar(100)
);
--2.Table Book
create table book(
                     id uuid PRIMARY KEY ,
                     title varchar(255) not null,
                     author varchar(100) not null ,
                     quantity INTEGER not null check ( quantity>=0 )
);
--3.Table BorrowRecord
create table borrow_record(
                              id uuid primary key ,
                              reader_id uuid not null,
                              book_id uuid not null ,
                              borrow_date date not null ,
                              return_date date,
                              status varchar(20) not null check ( status in ('borrowed','overdue','returned') ),
                              constraint fk_borrow_reader foreign key(reader_id) references reader(id),
                              constraint fk_borrow_book foreign key (book_id) references book(id)
);