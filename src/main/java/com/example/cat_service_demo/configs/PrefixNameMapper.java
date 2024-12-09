package com.example.cat_service_demo.configs;

import org.redisson.api.NameMapper;

public class PrefixNameMapper implements NameMapper {
    private final String prefix;

    public PrefixNameMapper(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String map(String name) {
        return prefix + name;
    }

    @Override
    public String unmap(String name) {
        if (name.startsWith(prefix)) {
            return name.substring(prefix.length());
        }
        return name;
    }
}
