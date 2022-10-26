package com.example.stockp.util;

/**
 * @author Mahdi Sharifi
 */

import org.springframework.data.domain.Page;
import org.springframework.hateoas.Link;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * It provides us paginataion links
 */
public  enum PaginationUtil {
    INSTANCE;
    public static <T> List<Link> generatePaginationList(UriComponentsBuilder uriBuilder, Page<T> page) {

        List<Link> linkList = new ArrayList<>();
        int pageNumber = page.getNumber();
        int pageSize = page.getSize();

        if (pageNumber > 0) { // is not the first page
            linkList.add(prepareLink(uriBuilder, 0, pageSize, "first"));
            linkList.add(prepareLink(uriBuilder, pageNumber - 1, pageSize, "prev"));
        }
        if (pageNumber < page.getTotalPages() - 1) { // if is not the last page
            linkList.add(prepareLink(uriBuilder, pageNumber + 1, pageSize, "next"));
            linkList.add(prepareLink(uriBuilder, page.getTotalPages() - 1, pageSize, "last"));
        }

        return linkList;
    }

    private static org.springframework.hateoas.Link prepareLink(UriComponentsBuilder uriBuilder, int pageNumber, int pageSize, String relType) {
        return Link.of(preparePageUri(uriBuilder, pageNumber, pageSize)).withRel(relType);
    }


    private static String preparePageUri(UriComponentsBuilder uriBuilder, int pageNumber, int pageSize) {
        return uriBuilder.replaceQueryParam("page", Integer.toString(pageNumber)).replaceQueryParam("size", Integer.toString(pageSize)).toUriString();
    }


}
