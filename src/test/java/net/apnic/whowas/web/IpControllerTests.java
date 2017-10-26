package net.apnic.whowas.web;

import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static net.apnic.whowas.web.RdapControllerTesting.isRdap;
import static net.apnic.whowas.web.RdapControllerTesting.testObjectHistory;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class IpControllerTests {

    @TestConfiguration
    @ComponentScan(basePackages = "net.apnic.whowas.rdap.config")
    static class TestRDAPControllerConfiguration {}

    @Autowired
    private MockMvc mvc;

    @MockBean
    IntervalTree<IP, ObjectHistory, IpInterval> historyTree;

    IpInterval dummyInterval = null;

    @Test
    public void indexLookupHasResult() throws Exception {
        given(historyTree.containing(any())).willReturn(Stream.of(new Tuple<>(dummyInterval, testObjectHistory())));

        ResultActions actions = mvc.perform(get("/ip/10.0.0.0"))
                .andExpect(status().isOk())
                .andExpect(isRdap());
    }

    @Test
    public void runtimeExceptionIs500() throws Exception {
        given(historyTree.containing(any()))
                .willThrow(new RuntimeException("Test exception"));

        ResultActions actions = mvc.perform(get("/ip/10.0.0.0"))
                .andExpect(status().isInternalServerError())
                .andExpect(isRdap())
                .andExpect(jsonPath("$.errorCode", is("500")));
    }

    @Test
    public void noSearchResultsIsNotFoundRDAPResponse() throws Exception {
        given(historyTree.containing(any())).willReturn(Stream.empty());

        ResultActions actions = mvc.perform(get("/ip/10.0.0.0"))
                .andExpect(status().isNotFound())
                .andExpect(isRdap());
    }
}
