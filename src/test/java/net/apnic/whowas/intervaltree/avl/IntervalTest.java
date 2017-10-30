package net.apnic.whowas.intervaltree.avl;

import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Parsing;
import org.junit.Test;

import static net.apnic.whowas.FuncTypeSafeMatchers.encompasses;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

public class IntervalTest {

    @Test
    public void lesserEncompassesGreater() {
        IpInterval lesser = Parsing.parseCIDRInterval("10.0.0.0/23");
        IpInterval greater = Parsing.parseCIDRInterval("10.0.0.0/24");

        assertThat(lesser, lessThan(greater));
        assertThat(lesser, encompasses(greater));
    }
}
