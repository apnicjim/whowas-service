package net.apnic.whowas.entity.controller;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(EntitySearchRouteController.class)
public class EntityControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EntitySearchService searchService;

    @Test
    public void entitySearchByHandleReturnsExpectedResponse() throws Exception {
        given(this.searchService.searchByHandle("myHandle"))
                .willReturn(Stream.of(objectHistory));

        this.mvc.perform(get("/entities?handle=myHandle"))
                .andExpect(status().isOk());
    }
}
