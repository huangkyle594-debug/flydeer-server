package com.flydeer.structmind.repository.mysql.option;

import java.util.ArrayList;
import java.util.List;

public class Options<T> {

    private final List<T> options = new ArrayList<>();

    public void add(T option) {
        options.add(option);
    }

    public Boolean contains(T option) {
        return options.contains(option);
    }
}
