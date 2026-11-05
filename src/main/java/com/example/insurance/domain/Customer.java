package com.example.insurance.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record Customer(
        String name,
        String email,
        LocalDate dateOfBirth,
        List<Product> products,

        Double height,
        Double weight,
        String profession
) {
    public Customer {
        products = List.copyOf(products);
    }

    public static Customer of(String name, String email, LocalDate dateOfBirth) {
        return new Customer(name, email, dateOfBirth, List.of(), null, null, null);
    }

    public Customer withProduct(Product product) {
        List<Product> updated = new ArrayList<>(products);
        updated.add(product);
        return new Customer(name, email, dateOfBirth, List.copyOf(updated), height, weight, profession);
    }

    public Customer withDetails(double height, double weight, String profession) {
        return new Customer(name, email, dateOfBirth, products, height, weight, profession);
    }
}
