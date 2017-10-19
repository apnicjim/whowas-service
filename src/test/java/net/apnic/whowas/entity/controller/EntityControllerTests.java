package net.apnic.whowas.entity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.Revision;
import net.apnic.whowas.rdap.RdapObject;
import net.apnic.whowas.rpsl.rdap.RpslToRdap;
import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(EntitySearchRouteController.class)
public class EntityControllerTests {

    @TestConfiguration
    @ComponentScan(basePackages = "net.apnic.whowas.rdap.config")
    static class TestRDAPControllerConfiguration {}

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EntitySearchService searchService;

    @Test
    public void entitySearchByHandleReturnsExpectedResponse() throws Exception {
        given(searchService.findByHandle("myHandle"))
                .willReturn(Stream.of(testObjectHistory()));

        ResultActions actions = mvc.perform(get("/entities?handle=myHandle"))
                .andExpect(status().isOk());
        actions = expectRdap(actions);

        MvcResult result = actions.andReturn();

        result.getResponse().getHeaderNames().forEach(
                headerName -> System.out.println(headerName +": " + result.getResponse().getHeaders(headerName))
        );
        System.out.println(prettyJson(result.getResponse().getContentAsString()));
    }

    private ObjectHistory testObjectHistory() {
        ObjectKey objectKey = new ObjectKey(ObjectClass.ENTITY, "example");
        RdapObject rdapObject = new RpslToRdap()
                .apply(objectKey, "person:  Example Citizen\nhandle:EC44-AP\n".getBytes());
        Revision revision = new Revision(
                ZonedDateTime.parse("2017-10-18T14:47:31.023+10:00"),
                null,
                rdapObject);
        ObjectHistory objectHistory = new ObjectHistory(objectKey).appendRevision(revision);
        return objectHistory;
    }

    private static ResultActions expectRdap(ResultActions actions) throws Exception {
        return actions
                .andExpect(header().string("Content-Type", "application/rdap+json"))
                .andExpect(jsonPath("$.rdapConformance", not(empty())));
    }

    private static String prettyJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Object jsonObject = mapper.readValue(json, Object.class);
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            return prettyJson;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
