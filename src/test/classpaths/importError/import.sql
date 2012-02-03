--
-- Test data: the column record_id does not exist (the statement is supposed to fail)
--
insert into Record (record_id, name) values (42, 'The answer.');
