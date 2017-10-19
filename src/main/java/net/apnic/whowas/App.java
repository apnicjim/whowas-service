package net.apnic.whowas;

import net.apnic.whowas.history.History;
import net.apnic.whowas.intervaltree.Interval;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.types.Tuple;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Stream;

@SpringBootApplication
public class App {

    private <K extends Comparable<K>, V, V2, I extends Interval<K>> IntervalTree<K, V2, I> lazyMap(
            IntervalTree<K, V, I> tree, Function<V, V2> mapper) {
        return new IntervalTree<K, V2, I>()
        {
            @Override
            public Stream<Tuple<I, V2>>
            containing(I range) {
                return tree.containing(range)
                        .flatMap(tuple -> Optional.ofNullable(mapper.apply(tuple.second()))
                                .map(Stream::of)
                                .orElse(Stream.empty())
                                .map(v2 -> new Tuple<>(tuple.first(), v2)));
            }

            @Override
            public Optional<V2> exact(I range) {
                return tree.exact(range).map(mapper);
            }

            @Override
            public Stream<Tuple<I, V2>> intersecting(I range) {
                return tree.intersecting(range)
                        .flatMap(tuple -> Optional.ofNullable(mapper.apply(tuple.second()))
                                .map(Stream::of)
                                .orElse(Stream.empty())
                                .map(v2 -> new Tuple<>(tuple.first(), v2)));
            }

            @Override
            public int size() {
                return tree.size();
            }
        };
    }

    @Bean
    public History history()
    {
        return new History();
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(App.class);
        Properties defaultProps = new Properties();

        defaultProps.setProperty(
                "spring.mvc.throw-exception-if-no-handler-found", "true");
        defaultProps.setProperty("spring.resources.add-mappings", "false");
        defaultProps.setProperty("spring.mvc.favicon.enabled", "false");
        defaultProps.setProperty("management.add-application-context-header", "false");
        app.setDefaultProperties(defaultProps);
        app.run(args);
    }
}
