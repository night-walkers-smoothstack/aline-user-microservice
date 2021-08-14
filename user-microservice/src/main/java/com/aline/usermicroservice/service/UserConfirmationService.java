package com.aline.usermicroservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserConfirmationService {

    private final AWSDynamoDBConfig dynamoDBConfig;

}
