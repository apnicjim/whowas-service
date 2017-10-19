package net.apnic.whowas.entity.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.error.MalformedRequestException;
import net.apnic.whowas.history.*;
import net.apnic.whowas.rdap.controller.RDAPControllerUtil;
import net.apnic.whowas.rdap.TopLevelObject;

import net.apnic.whowas.rdap.controller.RDAPResponseMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@RestController
@RequestMapping("/entities")
public class EntitySearchRouteController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(EntitySearchRouteController.class);

    private final RDAPControllerUtil rdapControllerUtil;
    private final EntitySearchService searchService;

    @Autowired
    public EntitySearchRouteController(EntitySearchService entitySearchService, RDAPResponseMaker rdapResponseMaker)
    {
        this.searchService = entitySearchService;
        this.rdapControllerUtil = new RDAPControllerUtil(rdapResponseMaker);
    }

    @RequestMapping(method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> entitiesGetPath(
        HttpServletRequest request,
        @RequestParam(name="handle", required=false, defaultValue="")
        String handle,
        @RequestParam(name="fn", required=false, defaultValue="")
        String fn)
    {
        if(handle.isEmpty() == false && fn.isEmpty() == true) {
            return rdapControllerUtil.searchResponse(
                    searchService.findByHandle(handle).map(ObjectHistory::mostCurrent)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(Revision::getContents),
                    ObjectClass.ENTITY,
                    request);

        } else if(handle.isEmpty() == true && fn.isEmpty() == false) {
            return rdapControllerUtil.searchResponse(
                    searchService.findByFn(fn).map(ObjectHistory::mostCurrent)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(Revision::getContents),
                    ObjectClass.ENTITY,
                    request);
        } else {
            throw new MalformedRequestException();
        }
    }
}
