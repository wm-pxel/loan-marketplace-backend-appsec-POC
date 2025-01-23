package com.westmonroe.loansyndication.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AddressFilterService {
    private final List<String> allowedAddresses = Arrays.asList(new String[]{
            "lm.jane.halverson@outlook.com",
            "lm.jimmy.dale@outlook.com",
            "lm.sarah.trask@outlook.com",
            "lm.sai.gao@outlook.com",
            "lmn.pete.kowalski@outlook.com",
            "lm.sally.mcpherson@outlook.com",
            "lm.lucas.harper@outlook.com",
            "lm.claire.meadows@outlook.com",
            "lm.jackson.fields@outlook.com",
            "lm.dana.teller@outlook.com",
    });

    private final List<String> allowedDomains = Arrays.asList(new String[]{
            "westmonroe.com",
            "cobank.com",
            "greenstonefcs.com",
            "fcma.com",
    });

    private String getDomain(String address)
    {
        return  address.substring(address.indexOf("@") + 1);
    }

    public boolean validateAddress(String address) {
        return allowedAddresses.contains(address) || allowedDomains.contains(this.getDomain(address));
    }
}
