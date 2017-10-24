package net.apnic.whowas.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.apnic.whowas.loaders.Loader;
import net.apnic.whowas.loaders.LoaderConfiguration;
import net.apnic.whowas.loaders.RipeDbLoader;
import net.apnic.whowas.rdap.RdapObject;
import net.apnic.whowas.rpsl.rdap.RpslToRdap;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LoadingTest {

    @Test
    public void historyFiltersDuplicateRevisions() {

        List<Revision> revisions = Arrays.asList(
            revision(
                    new ObjectKey(ObjectClass.ENTITY, "example"),
                    "person:  Example Citizen\nhandle:EC44-AP\n".getBytes(),
                    ZonedDateTime.parse("2017-01-01T00:00:00.000+10:00"),
                    ZonedDateTime.parse("2017-02-01T00:00:00.000+10:00")
            ),
            //duplicate
            revision(
                    new ObjectKey(ObjectClass.ENTITY, "example"),
                    "person:  Example Citizen\nhandle:EC44-AP\n".getBytes(),
                    ZonedDateTime.parse("2017-01-01T00:00:00.000+10:00"),
                    ZonedDateTime.parse("2017-02-01T00:00:00.000+10:00")
            ),
            revision(
                    new ObjectKey(ObjectClass.ENTITY, "example"),
                    "person:  Example Citizen\nhandle:EC44-AP\n".getBytes(),
                    ZonedDateTime.parse("2017-02-01T00:00:00.000+10:00"),
                    null
            )
        );

        History history = new History();
        revisions.forEach(revision -> history.addRevision(revision.getContents().getObjectKey(), revision));

        assertThat(
                history.getObjectHistory(new ObjectKey(ObjectClass.ENTITY, "example")).get(),
                is(iterableWithSize(2))
        );
    }

    @Test
    public void exceptionsDuringLoadingArePassedOver() {
        ObjectKey dummyKey = null;
        Revision revision = revision(
                new ObjectKey(ObjectClass.ENTITY, "example"),
                "person:  Example Citizen\nhandle:EC44-AP\n".getBytes(),
                ZonedDateTime.parse("2017-01-01T00:00:00.000+10:00"),
                ZonedDateTime.parse("2017-02-01T00:00:00.000+10:00")
        );
        Revision exceptionInducingRevision = revision(
                new ObjectKey(ObjectClass.ENTITY, "exceptionInducingRevision"),
                "person:  Example Citizen\nhandle:EC44-AP\n".getBytes(),
                ZonedDateTime.parse("2017-01-01T00:00:00.000+10:00"),
                ZonedDateTime.parse("2017-02-01T00:00:00.000+10:00")
        );

        Loader loader = revisionConsumer -> {
            revisionConsumer.accept(dummyKey, revision);
            revisionConsumer.accept(dummyKey, exceptionInducingRevision);
            revisionConsumer.accept(dummyKey, revision);
        };

        List<Revision> loaded = new ArrayList<>();
        LoaderConfiguration.loadFromLoader(loader, Collections.singleton(r -> {
            if (r.equals(exceptionInducingRevision)) throw new RuntimeException("Found exception inducing revision");
            loaded.add(r);
        }));

        assertThat(loaded.size(), is(2));
    }

    @Test
    public void ripeDbLoaderPassesOverObjectsThatCantBeMapped() throws SQLException {

        int entity = 10;
        ResultSet goodResultSet = mock(ResultSet.class);
        when(goodResultSet.getInt("object_type")).thenReturn(entity);
        when(goodResultSet.getBytes("object"))
                .thenReturn("person:  Example Citizen\nhandle:EC44-AP\n".getBytes());
        when(goodResultSet.getString("pkey")).thenReturn("example");
        when(goodResultSet.getLong("timestamp")).thenReturn(0L);

        int badType = 1;
        ResultSet unknownObjectTypeResultSet = mock(ResultSet.class);
        when(unknownObjectTypeResultSet.getInt("object_type")).thenReturn(badType);

        ResultSet canNotParseResultSet = mock(ResultSet.class);
        when(canNotParseResultSet.getInt("object_type")).thenReturn(entity);
        when(canNotParseResultSet.getBytes("object"))
                .thenReturn("can't parse".getBytes());


        JdbcOperations jdbcOperations = mock(JdbcOperations.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                RowCallbackHandler rowCallbackHandler = (RowCallbackHandler)args[1];

                rowCallbackHandler.processRow(goodResultSet);
                rowCallbackHandler.processRow(unknownObjectTypeResultSet);
                rowCallbackHandler.processRow(canNotParseResultSet);
                rowCallbackHandler.processRow(goodResultSet);

                return null;
            }
        }).when(jdbcOperations).query(Mockito.any(PreparedStatementCreator.class), Mockito.any(RowCallbackHandler.class));

        RipeDbLoader ripeDbLoader = new RipeDbLoader(jdbcOperations, 0);

        List<Revision> loaded = new ArrayList<>();
        ripeDbLoader.loadWith((k, r) -> loaded.add(r));

        assertThat(loaded.size(), is(2));
    }

    private String jsonString(RdapObject rdapObject) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return mapper.writeValueAsString(rdapObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Revision revision(
            ObjectKey objectKey,
            byte[] rpsl,
            ZonedDateTime validFrom,
            ZonedDateTime validUntil) {
        RdapObject rdapObject = new RpslToRdap().apply(objectKey, rpsl);
        Revision revision = new Revision(validFrom, validUntil, rdapObject);
        return revision;
    }
}

