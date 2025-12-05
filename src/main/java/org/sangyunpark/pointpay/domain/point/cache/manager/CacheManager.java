package org.sangyunpark.pointpay.domain.point.cache.manager;

public interface CacheManager<K, T> {

    T load(K key);

    T loadAndCache(K key);

    T loadFromDb(K key);
}
