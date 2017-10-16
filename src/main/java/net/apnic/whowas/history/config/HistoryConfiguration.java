package net.apnic.whowas.history.config;

import java.util.Optional;
import java.util.stream.Stream;

import net.apnic.whowas.history.History;
import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.history.ObjectIndex;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.search.SearchEngine;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HistoryConfiguration
{
    @Bean
    public History history()
    {
        return new History();
    }


}
