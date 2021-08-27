INSERT INTO applicant (id, first_name, last_name, gender, date_of_birth, email, phone, social_security, drivers_license, address, city, state, zipcode, mailing_address, mailing_city, mailing_state, mailing_zipcode, income)
    VALUES (1, 'John', 'Smith', 'MALE', '1995-06-23', 'johnsmith@email.com', '(222) 222-2222', '222-22-2222', 'DL222222', '321 Main St.', 'Townsville', 'Maine', '12345', 'PO Box 1234', 'Townsville', 'Maine', '12345', 7500000);

INSERT INTO member (id, branch_id, applicant_id, membership_id)
    VALUES (1, 1, 1, '12345678');
