package ru.yandex.practicum.commerce.warehouse.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.AddressDto;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

@Service
public class WarehouseAddressService {

    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[RandomGenerator.getDefault().nextInt(0, ADDRESSES.length)];

    public AddressDto getAddress() {
        AddressDto dto = new AddressDto();
        dto.setCountry(CURRENT_ADDRESS);
        dto.setCity(CURRENT_ADDRESS);
        dto.setStreet(CURRENT_ADDRESS);
        dto.setHouse(CURRENT_ADDRESS);
        dto.setFlat(CURRENT_ADDRESS);
        return dto;
    }
}