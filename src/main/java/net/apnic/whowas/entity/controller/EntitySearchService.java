package net.apnic.whowas.entity.controller;

import net.apnic.whowas.history.ObjectHistory;

import java.util.stream.Stream;

public interface EntitySearchService {
    Stream<ObjectHistory> findByHandle(String query);
    Stream<ObjectHistory> findByFn(String query);
}
