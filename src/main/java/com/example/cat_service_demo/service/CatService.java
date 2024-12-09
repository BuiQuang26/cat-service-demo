package com.example.cat_service_demo.service;

import com.example.cat_service_demo.configs.CacheNameConstants;
import com.example.cat_service_demo.domain.Cat;
import com.example.cat_service_demo.repositories.CatRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CatService {

    private final CatRepository catRepository;
    private final CacheManager cacheManager;

    public CatService(CatRepository catRepository, CacheManager cacheManager) {
        this.catRepository = catRepository;
        this.cacheManager = cacheManager;
    }

//    @CachePut(value = CacheNameConstants.CAT_CACHE, key = "#cat.id")
    public Cat create(Cat cat) {
        return catRepository.save(cat);
    }

//    @Cacheable(value = CacheNameConstants.CAT_CACHE, key = "#id")
    public Cat getById(Long id) {
        return catRepository.findById(id).orElseThrow();
    }

    public List<Cat> getAll() {
        return catRepository.findAll();
    }

    public Cat getByIdFromCache(Long id) {
        Cache cache = cacheManager.getCache(CacheNameConstants.CAT_CACHE);
        assert cache != null;
        return cache.get(id, Cat.class);
    }
}
