package com.aktivingatlan.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.aktivingatlan.domain.Photo;
import com.aktivingatlan.repository.PhotoRepository;
import com.aktivingatlan.repository.search.PhotoSearchRepository;
import com.aktivingatlan.web.rest.util.HeaderUtil;
import com.aktivingatlan.web.rest.util.PaginationUtil;
import com.aktivingatlan.web.rest.dto.PhotoDTO;
import com.aktivingatlan.web.rest.mapper.PhotoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Photo.
 */
@RestController
@RequestMapping("/api")
public class PhotoResource {

    private final Logger log = LoggerFactory.getLogger(PhotoResource.class);

    @Inject
    private PhotoRepository photoRepository;

    @Inject
    private PhotoMapper photoMapper;

    @Inject
    private PhotoSearchRepository photoSearchRepository;

    /**
     * POST  /photos -> Create a new photo.
     */
    @RequestMapping(value = "/photos",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<PhotoDTO> create(@RequestBody PhotoDTO photoDTO) throws URISyntaxException {
        log.debug("REST request to save Photo : {}", photoDTO);
        if (photoDTO.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new photo cannot already have an ID").body(null);
        }
        Photo photo = photoMapper.photoDTOToPhoto(photoDTO);
        Photo result = photoRepository.save(photo);
        photoSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/photos/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert("photo", result.getId().toString()))
                .body(photoMapper.photoToPhotoDTO(result));
    }

    /**
     * PUT  /photos -> Updates an existing photo.
     */
    @RequestMapping(value = "/photos",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<PhotoDTO> update(@RequestBody PhotoDTO photoDTO) throws URISyntaxException {
        log.debug("REST request to update Photo : {}", photoDTO);
        if (photoDTO.getId() == null) {
            return create(photoDTO);
        }
        Photo photo = photoMapper.photoDTOToPhoto(photoDTO);
        Photo result = photoRepository.save(photo);
        photoSearchRepository.save(photo);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert("photo", photoDTO.getId().toString()))
                .body(photoMapper.photoToPhotoDTO(result));
    }

    /**
     * GET  /photos -> get all the photos.
     */
    @RequestMapping(value = "/photos",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<PhotoDTO>> getAll(@RequestParam(value = "page" , required = false) Integer offset,
                                  @RequestParam(value = "per_page", required = false) Integer limit)
        throws URISyntaxException {
        Page<Photo> page = photoRepository.findAll(PaginationUtil.generatePageRequest(offset, limit));
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/photos", offset, limit);
        return new ResponseEntity<>(page.getContent().stream()
            .map(photoMapper::photoToPhotoDTO)
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * GET  /photos/:id -> get the "id" photo.
     */
    @RequestMapping(value = "/photos/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<PhotoDTO> get(@PathVariable Long id) {
        log.debug("REST request to get Photo : {}", id);
        return Optional.ofNullable(photoRepository.findOne(id))
            .map(photoMapper::photoToPhotoDTO)
            .map(photoDTO -> new ResponseEntity<>(
                photoDTO,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /photos/:id -> delete the "id" photo.
     */
    @RequestMapping(value = "/photos/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("REST request to delete Photo : {}", id);
        photoRepository.delete(id);
        photoSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("photo", id.toString())).build();
    }

    /**
     * SEARCH  /_search/photos/:query -> search for the photo corresponding
     * to the query.
     */
    @RequestMapping(value = "/_search/photos/{query}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Photo> search(@PathVariable String query) {
        return StreamSupport
            .stream(photoSearchRepository.search(queryString(query)).spliterator(), false)
            .collect(Collectors.toList());
    }
}