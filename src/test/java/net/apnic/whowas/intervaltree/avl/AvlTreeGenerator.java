package net.apnic.whowas.intervaltree.avl;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.GeneratorConfiguration;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class AvlTreeGenerator extends Generator<AvlTree> {

    @Target({PARAMETER, FIELD, ANNOTATION_TYPE, TYPE_USE})
    @Retention(RUNTIME)
    @GeneratorConfiguration
    public @interface Size {
        int max() default 100;
        int min() default 0;
    }

    private int minSize = 0;
    private int maxSize = 100;
    private int intervalMin = 0;
    private int intervalMax = Integer.MAX_VALUE;

    public AvlTreeGenerator() {
        super(AvlTree.class);
    }

    @Override
    public AvlTree<Integer, IntInterval, IntInterval> generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
        IntIntervalGenerator intIntervalGenerator = gen().make(IntIntervalGenerator.class);
        intIntervalGenerator.configure(intervalMin, intervalMax);

        AvlTree<Integer, IntInterval, IntInterval> tree = new AvlTree<>();
        List<IntInterval> intervals = Stream.generate(() -> intIntervalGenerator.generate(sourceOfRandomness, generationStatus))
                .distinct()
                .limit(sourceOfRandomness.nextInt(minSize, maxSize))
                .collect(Collectors.toList());

        for(IntInterval range : intervals) {
            tree = tree.insert(range, range);
        }

        return tree;
    }

    public void configure(InRange range) {
        this.intervalMin = range.minInt();
        this.intervalMax = range.maxInt();
    }

    public void configure(Size size) {
        this.maxSize = size.min();
        this.maxSize = size.max();
    }
}