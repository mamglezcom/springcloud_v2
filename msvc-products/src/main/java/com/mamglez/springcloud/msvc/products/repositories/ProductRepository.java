package com.mamglez.springcloud.msvc.products.repositories;

import org.springframework.data.repository.CrudRepository;

import com.mamglez.libs.msvc.commons.entities.Product;



public interface ProductRepository extends CrudRepository<Product, Long>{

}