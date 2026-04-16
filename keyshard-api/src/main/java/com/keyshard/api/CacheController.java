package com.keyshard.api;

@RestController
@RequestMapping("/api/v1/cache")
public class CacheController {

    private final CacheService<String, String> cacheService;

    public CacheController(CacheService<String, String> cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/{key}")
    public ResponseEntity<CacheResponse> get(@PathVariable String key) {
        String value = cacheService.get(key);

        if (value == null) {
            throw new KeyNotFoundException(key);
        }

        return ResponseEntity.ok(new CacheResponse(key, value));
    }

    @PutMapping("/{key}")
    public ResponseEntity<CacheResponse> put(
            @PathVariable String key,
            @RequestBody CacheRequest request) {

        cacheService.put(key, request.value(), request.ttl());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CacheResponse(key, request.value()));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        cacheService.delete(key);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public CacheStats stats() {
        return cacheService.getStats();
    }
}