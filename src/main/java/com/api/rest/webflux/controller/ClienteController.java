package com.api.rest.webflux.controller;

import com.api.rest.webflux.documents.Cliente;
import com.api.rest.webflux.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping //Va a devolver una sola respuesta, pero dentro lleva un flujo de clientes
    public Mono<ResponseEntity<Flux<Cliente>>> listarClientes() {
        return Mono.just( //Rellenarle evento
                ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(clienteService.findAll()));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Cliente>> verDetallesCliente(@PathVariable String id) {
        return clienteService.findById(id).map(cliente -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(cliente)).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping()
    public Mono<ResponseEntity<Map<String, Object>>> guardarCliente(@Valid @RequestBody Mono<Cliente> monoCliente) {
        Map<String, Object> respuesta = new HashMap<>();
        return monoCliente.flatMap(cliente -> {
            return clienteService.save(cliente).map(c -> {
                respuesta.put("cliente", c);
                respuesta.put("mensaje", "Cliente guardado con exito");
                respuesta.put("timestamp", new Date());
                return ResponseEntity.created(URI.create("/api/clientes/".concat(c.getId())))
                        .contentType(MediaType.APPLICATION_JSON_UTF8).body(respuesta);
            });//Si hay error al guardar
        }).onErrorResume(t -> {
            return Mono.just(t).cast(WebExchangeBindException.class)
                    .flatMap(e -> Mono.just(e.getFieldErrors()))
                            .flatMapMany(Flux::fromIterable)
                            .map(fieldError -> "El campo: " + fieldError.getField() + ""
                                    + fieldError.getDefaultMessage())
                            .collectList()
                            .flatMap(list -> {
                                respuesta.put("errors", list);
                                respuesta.put("timestamp", new Date());
                                respuesta.put("status", HttpStatus.BAD_REQUEST.value());

                                return Mono.just(ResponseEntity.badRequest().body(respuesta));
                            });
        });
    }


    @PutMapping("/{id}")
    public Mono<ResponseEntity<Cliente>> editarCliente(@RequestBody Cliente cliente, @PathVariable String id) {
        return clienteService.findById(id).flatMap(c -> {
            c.setNombre(cliente.getNombre());
            c.setApellido(cliente.getApellido());
            c.setEdad(cliente.getEdad());
            c.setSueldo(cliente.getSueldo());
            return clienteService.save(c);
        }).map(c -> ResponseEntity.created(URI.create("/api/clientes/" .concat(c.getId()))).contentType(MediaType.APPLICATION_JSON_UTF8).body(c)).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminarCliente(@PathVariable String id) {
        return clienteService.findById(id).flatMap(c -> {
            return clienteService.delete(c).then(Mono.just((new ResponseEntity<Void>(HttpStatus.NO_CONTENT))));
        }).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }
}

