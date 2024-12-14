package com.mamglez.springcloud.msvc.items.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.mamglez.libs.msvc.commons.entities.Product;
import com.mamglez.springcloud.msvc.items.models.Item;

import com.mamglez.springcloud.msvc.items.services.ItemService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@RefreshScope
@RestController
public class ItemController {

    @Value("${configuracion.texto}")
    private String texto;

    @Autowired
    private Environment env;

    /* en lugar de @Autowirede se inyecta mediante el constructor */
    private final ItemService service;
    private final CircuitBreakerFactory cBreakerFactory;
    
    private Logger logger = LoggerFactory.getLogger(ItemController.class);

    // public ItemController(@Qualifier("itemServiceWebClient")ItemService service, CircuitBreakerFactory cBreakerFactory) {
    public ItemController(@Qualifier("itemServiceFeign")ItemService service, CircuitBreakerFactory cBreakerFactory) {
        this.service = service;
        this.cBreakerFactory = cBreakerFactory;
    }

    @GetMapping("/fetch-configs")
    public ResponseEntity<?> fetchConfigs(@Value("${server.port}") String port){
        Map<String, String> json = new HashMap<>();
        json.put("texto", texto);
        json.put("puerto", port);
        logger.info(port);
        logger.info(texto);

        if(env.getActiveProfiles().length>0 && env.getActiveProfiles()[0].equals("dev")){
            json.put("autor.email", env.getProperty("configuracion.autor.nombre"));
            json.put("autor.nombre", env.getProperty("configuracion.autor.email"));
        }
        return ResponseEntity.ok(json);
    }
    
    @GetMapping
    public List<Item> list(@RequestParam(name="name", required = false) String name, 
                @RequestHeader(name="token-request", required = false) String token) {
                    System.out.println(name);
                    System.out.println(token);

        return service.findAll();
    }
    
    @CircuitBreaker(name = "items", fallbackMethod="getFallBackMethodproduct")
    @GetMapping("/details/{id}")
    public ResponseEntity<?> details2(@PathVariable Long id) {
        //Optional<Item> itemOptional = service.findById(id);
        Optional<Item> itemOptional =  service.findById(id);
        if(itemOptional.isPresent()){
            return ResponseEntity.ok().body(itemOptional.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", "no existe el producto"));
    }

    @CircuitBreaker(name="items", fallbackMethod = "getFallBackMethodproduct2")
    @TimeLimiter(name = "items"/*, fallbackMethod = "getFallBackMethodproduct2"*/)
    @GetMapping("/details2/{id}")
    public CompletableFuture<?> details3(@PathVariable Long id) {
        return CompletableFuture.supplyAsync(() -> {
            //Optional<Item> itemOptional = service.findById(id);
            Optional<Item> itemOptional =  service.findById(id);
            if(itemOptional.isPresent()){
                return ResponseEntity.ok().body(itemOptional.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("message", "no existe el producto"));
 
        });
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id) {
        //Optional<Item> itemOptional = service.findById(id);
        Optional<Item> itemOptional = cBreakerFactory.create("items").run(() -> service.findById(id), e -> {
            System.out.println(e.getMessage());
            logger.info(e.getMessage());
            
            Product product = new Product();
            product.setCreateAt(LocalDate.now());
            product.setId(1L);
            product.setName("Camara sony");
            product.setPrice(500.0);
            return Optional.of(new Item(product, 5));
        });
        if(itemOptional.isPresent()){
            return ResponseEntity.ok().body(itemOptional.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", "no existe el producto"));
    }

    public CompletableFuture<?> getFallBackMethodproduct2(Throwable e) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println(e.getMessage());
            logger.info(e.getMessage());
            
            Product product = new Product();
            product.setCreateAt(LocalDate.now());
            product.setId(1L);
            product.setName("Camara sony");
            product.setPrice(500.0);
            return ResponseEntity.ok(new Item(product, 5));
        });
        
    }
    
    public ResponseEntity<?> getFallBackMethodproduct(Throwable e) {
        System.out.println(e.getMessage());
        logger.info(e.getMessage());
        
        Product product = new Product();
        product.setCreateAt(LocalDate.now());
        product.setId(1L);
        product.setName("Camara sony");
        product.setPrice(500.0);
        return ResponseEntity.ok(new Item(product, 5));
    } 

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody Product product){
        return service.save(product);
    } 

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{id}")
    public Product update(@RequestBody Product product, @PathVariable("id") Long id){
        return service.update(product, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id){
        service.delete(id);
    }

}
