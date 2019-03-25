-- any sql that is to be pre-loaded before testsuite runs goes in here
-- statements must be a single line without returns
INSERT INTO ADDRESS (ID, CITY, COUNTRY, POSTAL, STATE, STREET, VERSION) VALUES (1, 'Toronto', 'CA', 'K1B 9Y6', 'ON', '1 Main Street', 1)
INSERT INTO ADDRESS (ID, CITY, COUNTRY, POSTAL, STATE, STREET, VERSION) VALUES (2, 'Ottawa', 'CA', 'K2B 4T9', 'ON', '10 Queen Street', 1)
INSERT INTO EMPLOYEE (ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID) VALUES(10, 'Lei', 'Cao', 5000, 1, 1)
INSERT INTO EMPLOYEE (ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID) VALUES(20, 'Chenxiao', 'Cui', 5000, 1, 2)