package net.apnic.whowas.intervaltree.avl;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

public class IntIntervalGenerator extends Generator<IntInterval> {

    private int min = 0;
    private int max = Integer.MAX_VALUE;

    public IntIntervalGenerator() {
        super(IntInterval.class);
    }

    @Override
    public IntInterval generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
        int a = sourceOfRandomness.nextInt(min, max);
        int b = sourceOfRandomness.nextInt(min, max);
        return a < b ? new IntInterval(a, b) : new IntInterval(b, a);
    }

    public void configure(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public void configure(InRange range) {
        configure(range.minInt(), range.maxInt());
    }
}
