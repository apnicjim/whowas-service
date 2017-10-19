package net.apnic.whowas.rdap.controller;

import net.apnic.whowas.history.*;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.rdap.Error;
import net.apnic.whowas.rdap.*;
import net.apnic.whowas.rdap.http.RdapConstants;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Tuple;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.swing.text.html.Option;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Common RDAP response generation for RDAP controllers.
 */
public class RDAPControllerUtil
{
    private HttpHeaders responseHeaders = null;
    private final RDAPResponseMaker responseMaker;

    public RDAPControllerUtil(RDAPResponseMaker responseMaker)
    {
        setupResponseHeaders();
        this.responseMaker = responseMaker;
    }

    public ResponseEntity<TopLevelObject> errorResponse(
        HttpServletRequest request, Error error, HttpStatus status)
    {
        return new ResponseEntity<TopLevelObject>(
            responseMaker.makeResponse(error, request),
            responseHeaders,
            status);
    }

    public ResponseEntity<TopLevelObject> notImplementedResponse(
        HttpServletRequest request)
    {
        return new ResponseEntity<TopLevelObject>(
            responseMaker.makeResponse(Error.NOT_IMPLEMENTED, request),
            responseHeaders,
            HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<TopLevelObject> historyResponse(
            HttpServletRequest request, ObjectHistory objectHistory)
    {
        return Optional.ofNullable(objectHistory)
                .map(RdapHistory::new)
                .map(history -> responseMaker.makeResponse(history, request))
                .map(response -> new ResponseEntity<TopLevelObject>(
                        response, responseHeaders, HttpStatus.OK))
                .orElse(new ResponseEntity<TopLevelObject>(
                        responseMaker.makeResponse(Error.NOT_FOUND, request),
                        responseHeaders,
                        HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<TopLevelObject> historiesResponse(HttpServletRequest request, IpInterval range, List<ObjectHistory> histories) {
        return Optional.ofNullable(histories.size() > 0 ? histories : null)
                .map(RdapHistory::new)
                .map(history -> responseMaker.makeResponse(history, request))
                .map(response -> new ResponseEntity<TopLevelObject>(
                        response, responseHeaders, HttpStatus.OK))
                .orElse(new ResponseEntity<TopLevelObject>(
                        responseMaker.makeResponse(Error.NOT_FOUND, request),
                        responseHeaders,
                        HttpStatus.NOT_FOUND));
    }

    //Why does this one need the object class? because it is a search response?
    public ResponseEntity<TopLevelObject> searchResponse(
            Stream<RdapObject> rdapObjectStream,
            ObjectClass objectClass,
            HttpServletRequest request) {
        return new ResponseEntity<TopLevelObject>(
                responseMaker.makeResponse(
                        RdapSearch.build(objectClass, rdapObjectStream.collect(Collectors.toList())), request),
                responseHeaders, HttpStatus.OK);
    }

    public ResponseEntity<TopLevelObject> singleObjectResponse(HttpServletRequest request, RdapObject rdapObject) {
        return Optional.ofNullable(rdapObject).map(o -> responseMaker.makeResponse(o, request))
                .map(rdapTLO -> new ResponseEntity<TopLevelObject>(
                        rdapTLO, responseHeaders, HttpStatus.OK))
                .orElse(new ResponseEntity<TopLevelObject>(
                        responseMaker.makeResponse(Error.NOT_FOUND, request),
                        responseHeaders,
                        HttpStatus.NOT_FOUND));
    }

    private void setupResponseHeaders()
    {
        responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(RdapConstants.RDAP_MEDIA_TYPE);
    }
}
