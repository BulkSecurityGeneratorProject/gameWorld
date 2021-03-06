package com.gameworld.app.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.gameworld.app.domain.Adress;
import com.gameworld.app.service.AdressService;
import com.gameworld.app.web.rest.util.HeaderUtil;
import com.gameworld.app.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Adress.
 */
@RestController
@RequestMapping("/api")
public class AdressResource {

    private final Logger log = LoggerFactory.getLogger(AdressResource.class);

    @Inject
    private AdressService adressService;

    @PostMapping("/adresses")
    @Timed
    public ResponseEntity<Adress> createAdress(@Valid @RequestBody Adress adress) throws URISyntaxException {
        log.debug("REST request to save Adress : {}", adress);
        if (adress.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("adress", "idexists", "A new adress cannot already have an ID")).body(null);
        }
        Adress result = adressService.save(adress);
        return ResponseEntity.created(new URI("/api/adresses/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("adress", result.getId().toString()))
            .body(result);
    }

    @PutMapping("/adresses")
    @Timed
    public ResponseEntity<Adress> updateAdress(@Valid @RequestBody Adress adress) throws URISyntaxException {
        log.debug("REST request to update Adress : {}", adress);
        if (adress.getId() == null) {
            return createAdress(adress);
        }
        Adress result = adressService.save(adress);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("adress", adress.getId().toString()))
            .body(result);
    }

    @GetMapping("/adresses")
    @Timed
    public ResponseEntity<List<Adress>> getAllAdresses(Pageable pageable, @RequestParam(required = false) String filter)
        throws URISyntaxException {
        if ("gamerprofile-is-null".equals(filter)) {
            log.debug("REST request to get all Adresss where gamerProfile is null");
            return new ResponseEntity<>(adressService.findAllWhereGamerProfileIsNull(),
                    HttpStatus.OK);
        }
        log.debug("REST request to get a page of Adresses");
        Page<Adress> page = adressService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/adresses");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/adresses/{id}")
    @Timed
    public ResponseEntity<Adress> getAdress(@PathVariable Long id) {
        log.debug("REST request to get Adress : {}", id);
        Adress adress = adressService.findOne(id);
        return Optional.ofNullable(adress)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/adresses/{id}")
    @Timed
    public ResponseEntity<Void> deleteAdress(@PathVariable Long id) {
        log.debug("REST request to delete Adress : {}", id);
        adressService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("adress", id.toString())).build();
    }

    @GetMapping("/_search/adresses")
    @Timed
    public ResponseEntity<List<Adress>> searchAdresses(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Adresses for query {}", query);
        Page<Adress> page = adressService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/adresses");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/adresses/my")
    @Timed
    public ResponseEntity<Adress> getUserAdress() {
        Adress adress = adressService.getUserAdress();
        return Optional.ofNullable(adress)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


}
