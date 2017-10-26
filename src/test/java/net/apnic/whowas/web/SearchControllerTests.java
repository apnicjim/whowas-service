package net.apnic.whowas.web;

import net.apnic.whowas.entity.controller.EntitySearchRouteController;
import net.apnic.whowas.entity.controller.EntitySearchService;
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
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(EntitySearchRouteController.class)
public class SearchControllerTests {

    @TestConfiguration
    @ComponentScan(basePackages = "net.apnic.whowas.rdap.config")
    static class TestRDAPControllerConfiguration {}

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EntitySearchService searchService;

    @Test
    public void searchByHandleExpectedResponse() throws Exception {
        given(searchService.findByHandle("myHandle"))
                .willReturn(Stream.of(testObjectHistory()));

        ResultActions actions = mvc.perform(get("/entities?handle=myHandle"))
                .andExpect(status().isOk())
                .andExpect(isRdap())
                .andExpect(jsonPath("$.entitySearchResults", not(empty())));
    }

    @Test
    public void runtimeExceptionIs500() throws Exception {
        given(searchService.findByHandle("myHandle"))
                .willThrow(new RuntimeException("Test exception"));

        ResultActions actions = mvc.perform(get("/entities?handle=myHandle"))
                .andExpect(status().isInternalServerError())
                .andExpect(isRdap())
                .andExpect(jsonPath("$.errorCode", is("500")));
    }

    @Test
    public void noSearchResultsIsEmptyRDAPResponse() throws Exception {
        given(searchService.findByHandle("myHandle"))
                .willReturn(Stream.empty());

        ResultActions actions = mvc.perform(get("/entities?handle=myHandle"))
                .andExpect(status().isOk())
                .andExpect(isRdap())
                .andExpect(jsonPath("$.entitySearchResults", is(empty())));
    }
}
