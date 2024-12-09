package com.example.cat_service_demo.controllers;

import com.example.cat_service_demo.domain.Cat;
import com.example.cat_service_demo.service.CatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatController {

    private final CatService catService;

    public CatController(CatService catService) {
        this.catService = catService;
    }

    @GetMapping("/cats")
    public ResponseEntity<List<Cat>> getCats() {
        List<Cat> cats = catService.getAll();
        return ResponseEntity.ok(cats);
    }

    @PostMapping("/cat")
    public ResponseEntity<Cat> createCat(Cat cat) {
        Cat createdCat = catService.create(cat);
        return ResponseEntity.ok(createdCat);
    }

    @GetMapping("/cat/{id}")
    public ResponseEntity<Cat> getCatById(@PathVariable("id") Long id) {
        Cat cat = catService.getById(id);
        return ResponseEntity.ok(cat);
    }

    @GetMapping("/cat/from-cache/{id}")
    public ResponseEntity<Cat> getCatByIdFromCache(@PathVariable("id") Long id) {
        Cat cat = catService.getByIdFromCache(id);
        return ResponseEntity.ok(cat);
    }

}
