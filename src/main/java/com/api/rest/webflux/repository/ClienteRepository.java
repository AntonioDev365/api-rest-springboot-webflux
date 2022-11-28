package com.api.rest.webflux.repository;

import com.api.rest.webflux.documents.Cliente;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ClienteRepository extends ReactiveMongoRepository<Cliente, String> {
}
