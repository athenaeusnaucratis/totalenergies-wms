package com.wms.product;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public List<Company> list(@RequestParam(required = false) String type) {
        if ("supplier".equalsIgnoreCase(type)) return companyService.findSuppliers();
        if ("customer".equalsIgnoreCase(type)) return companyService.findCustomers();
        return companyService.findAll();
    }

    @GetMapping("/{id}")
    public Company get(@PathVariable Long id) {
        return companyService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Company> create(@Valid @RequestBody CompanyService.CompanyDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companyService.create(dto));
    }

    @PutMapping("/{id}")
    public Company update(@PathVariable Long id, @Valid @RequestBody CompanyService.CompanyDto dto) {
        return companyService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
