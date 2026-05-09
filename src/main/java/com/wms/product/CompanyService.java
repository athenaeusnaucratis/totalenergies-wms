package com.wms.product;

import com.wms.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepo;

    public CompanyService(CompanyRepository companyRepo) {
        this.companyRepo = companyRepo;
    }

    public List<Company> findAll() {
        return companyRepo.findAll();
    }

    public List<Company> findSuppliers() {
        return companyRepo.findBySupplierTrue();
    }

    public List<Company> findCustomers() {
        return companyRepo.findByCustomerTrue();
    }

    public Company findById(Long id) {
        return companyRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
    }

    public Company create(CompanyDto dto) {
        Company c = new Company();
        applyDto(c, dto);
        return companyRepo.save(c);
    }

    public Company update(Long id, CompanyDto dto) {
        Company c = findById(id);
        applyDto(c, dto);
        return companyRepo.save(c);
    }

    public void delete(Long id) {
        companyRepo.deleteById(id);
    }

    private void applyDto(Company c, CompanyDto dto) {
        c.setName(dto.name());
        c.setSupplier(dto.supplier() != null ? dto.supplier() : false);
        c.setCustomer(dto.customer() != null ? dto.customer() : false);
        c.setEmail(dto.email());
        c.setPhone(dto.phone());
        c.setAddress(dto.address());
    }

    public record CompanyDto(String name, Boolean supplier, Boolean customer,
                             String email, String phone, String address) {}
}
