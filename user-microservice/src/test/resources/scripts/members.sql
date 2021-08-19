INSERT INTO bank (id, address, city, state, zipcode, routing_number)
    VALUES (1, '123 Address St', 'Townsville', 'Maine', '12345', '123456789');

INSERT INTO branch (id, name, address, city, state, zipcode, phone, bank_id)
    VALUES (1, 'Main Branch', '123 Address St', 'Townsville', 'Maine', '12345', '(208) 800-8000', 1);

INSERT INTO applicant (id, first_name, last_name, gender, date_of_birth, email, phone, social_security, drivers_license, address, city, state, zipcode, mailing_address, mailing_city, mailing_state, mailing_zipcode, income)
    VALUES (1, 'John', 'Smith', 'MALE', '1995-06-23', 'leandro.yabut@smoothstack.com', '(222) 222-2222', '222-22-2222', 'DL222222', '321 Main St.', 'Townsville', 'Maine', '12345', 'PO Box 1234', 'Townsville', 'Maine', '12345', 7500000);

INSERT INTO member (id, branch_id, applicant_id, membership_id)
    VALUES (1, 1, 1, '12345678');
