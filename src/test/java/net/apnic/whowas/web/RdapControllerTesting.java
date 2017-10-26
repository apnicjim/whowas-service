package net.apnic.whowas.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.Revision;
import net.apnic.whowas.rdap.RdapObject;
import net.apnic.whowas.rpsl.rdap.RpslToRdap;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultMatcher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class RdapControllerTesting {

    public static ObjectHistory testObjectHistory() {
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

    public static ResultMatcher isRdap() {
        return compositeResultMatcher(
                header().string("Content-Type", "application/rdap+json"),
                jsonPath("$.rdapConformance", not(empty()))
        );
    }

    public static ResultMatcher compositeResultMatcher(final ResultMatcher... matchers) {
        return mvcResult -> {
            for(ResultMatcher resultMatcher : matchers) {
                resultMatcher.match(mvcResult);
            }
        };
    }

    public void printResponse(MockHttpServletResponse response) {
        response.getHeaderNames().forEach(headerName ->
                System.out.println(headerName + ": " + response.getHeaders(headerName)));
        try {
            System.out.println(prettyJson(response.getContentAsString()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String prettyJson(String json) {
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
