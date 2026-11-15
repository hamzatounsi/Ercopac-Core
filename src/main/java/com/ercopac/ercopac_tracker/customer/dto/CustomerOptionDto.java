package com.ercopac.ercopac_tracker.customer.dto;

/** Entry of a customer dropdown. {@code active} is false only for the explicitly requested current value. */
public record CustomerOptionDto(Long id, String code, String name, boolean active) {
}
