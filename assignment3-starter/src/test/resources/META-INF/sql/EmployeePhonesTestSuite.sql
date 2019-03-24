-- any sql that is to be pre-loaded before testsuite runs goes in here
-- statements must be a single line without returns
INSERT INTO ADDRESS (ID, CITY, COUNTRY, POSTAL, STATE, STREET, VERSION) VALUES (5, 'Toronto', 'CA', 'K1B 9Y6', 'ON', '1 Main Street', 1)
INSERT INTO EMPLOYEE (ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID) VALUES(10, 'Lei', 'Cao', 5000, 1, 5)