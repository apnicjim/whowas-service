package net.apnic.whowas.web;

import net.apnic.whowas.history.ObjectIndex;
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

import java.util.Arrays;
import java.util.Optional;

import static net.apnic.whowas.web.RdapControllerTesting.isRdap;
import static net.apnic.whowas.web.RdapControllerTesting.testObjectHistory;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class ObjectIndexControllerTests {

    @TestConfiguration
    @ComponentScan(basePackages = "net.apnic.whowas.rdap.config")
    static class TestRDAPControllerConfiguration {}

    @Autowired
    private MockMvc mvc;

    @MockBean
    ObjectIndex objectIndex;

    @Test
    public void indexLookupHasResult() throws Exception {
        for (String url : Arrays.<String>asList(
                "/entity/myHandle",
                "/autnum/1",
                "/domain/apnic.net"
        )) {
            given(objectIndex.historyForObject(any())).willReturn(Optional.of(testObjectHistory()));

            ResultActions actions = mvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andExpect(isRdap())
                    .andExpect(jsonPath("$.objectClassName", is(anything())));
        }
    }

    @Test
    public void runtimeExceptionIs500() throws Exception {
        for (String url : Arrays.<String>asList(
                "/entity/myHandle",
                "/autnum/1",
                "/domain/apnic.net"
        )) {
            try {
                given(objectIndex.historyForObject(any()))
                        .willThrow(new RuntimeException("Test exception"));

                ResultActions actions = mvc.perform(get(url))
                        .andExpect(status().isInternalServerError())
                        .andExpect(isRdap())
                        .andExpect(jsonPath("$.errorCode", is("500")));
            } catch (Exception e) { /* Catch the expected exception for each iteration */ }
        }
    }

    @Test
    public void noSearchResultsIsNotFoundRDAPResponse() throws Exception {
        for (String url : Arrays.<String>asList(
                "/entity/myHandle",
                "/autnum/1",
                "/domain/apnic.net"
        )) {
            given(objectIndex.historyForObject(any()))
                    .willReturn(Optional.empty());

            ResultActions actions = mvc.perform(get(url))
                    .andExpect(status().isNotFound())
                    .andExpect(isRdap());
        }
    }

    @Test
    public void notImplementedPaths() throws Exception {
        for (String url : Arrays.<String>asList(
                "/domains"
        )) {
            ResultActions actions = mvc.perform(get(url))
                    .andExpect(status().isNotImplemented());
                    //.andExpect(isRdap()); //should not implemented still be an rdap response?
        }
    }
}
