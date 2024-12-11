package com.mamglez.springcloud.msvc.products.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.mamglez.springcloud.msvc.products.entities.Product;
import com.mamglez.springcloud.msvc.products.services.ProductService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
//@RequestMapping("/api/products")
public class ProductController {

    final private ProductService service;

    public ProductController(ProductService service){
        this.service = service;
    }

    @GetMapping
    public List<Product> list(){
        return service.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> details(@PathVariable("id") Long id) throws InterruptedException {
        if(id.equals(10L)){
            throw new IllegalStateException("producto no encontrado");
        }

        if(id.equals(7L)){
            TimeUnit.SECONDS.sleep(3L);
        }

        Optional<Product> productOptional = service.findById(id);
        if(productOptional.isPresent()){
            return ResponseEntity.ok(productOptional.orElseThrow());
        }
        return ResponseEntity.notFound().build();
    }
    

}
