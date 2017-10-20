package net.apnic.whowas.intervaltree.avl;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import net.apnic.whowas.intervaltree.Interval;
import net.apnic.whowas.types.Tuple;
import org.hamcrest.Matcher;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static net.apnic.whowas.FuncTypeSafeMatchers.*;

import java.util.Collection;
import java.util.stream.Collectors;

@RunWith(JUnitQuickcheck.class)
public class AvlTreePBTest {

    @Property(trials = 100)
    public void resultsOfContainingContainTheQueryArgument(
            @From(IntIntervalGenerator.class) @InRange(minInt = 0, maxInt = 50) IntInterval query,
            @From(AvlTreeGenerator.class) @InRange(minInt = 0, maxInt = 50) @AvlTreeGenerator.Size(min = 0, max = 50) AvlTree t) {
        //Not sure how to make a generic quickcheck generator
        AvlTree<Integer, IntInterval, IntInterval> tree = (AvlTree<Integer, IntInterval, IntInterval>) t;

        Collection<Interval<Integer>> containingKeys =
                tree.containing(query).map(Tuple::first).collect(Collectors.toList());

        Collection<Interval<Integer>> intersectingKeys =
                tree.intersecting(query).map(Tuple::first).collect(Collectors.toList());

        assertThat(containingKeys, everyItem(contains(query)));
        assertThat(
                "The containing keys are a subset of the intersecting keys",
                intersectingKeys,
                (Matcher) hasItems(containingKeys.toArray())
        );
    }
}
