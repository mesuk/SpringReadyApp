package com.appcoder.springreadyapp.controller;

import com.appcoder.springreadyapp.domain.Customer;
import com.appcoder.springreadyapp.domain.ICustomer;
import com.appcoder.springreadyapp.exception.CustomerNotFoundException;
import com.appcoder.springreadyapp.services.CustomerService;
import io.swagger.annotations.Api;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/customer")
@Api(tags = {"CustomerController"})
public class CustomerController {

    private CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping(value = "/saveCustomer")
    public ResponseEntity<String> saveCustomer(HttpServletRequest requestHeader, @RequestBody Customer request) throws RuntimeException {

        if (customerService.saveUpdateCustomer(request)) {
            return new ResponseEntity<>("Customer Save/update Done", HttpStatus.OK);

        } else {
            return new ResponseEntity<>("Customer Save/update failed", HttpStatus.OK);
        }
    }

    @GetMapping(value = "/fetchAllCustomer")
    public ResponseEntity<List<Customer>> fetchAllCustomer() throws RuntimeException {
        return new ResponseEntity<>(customerService.fetchAllCustomer(), HttpStatus.OK);
    }

    @GetMapping(value = "/fetchCustomerByMobileNumber")
    public ResponseEntity<List<Customer>> fetchCustomerByMobileNumber(@RequestParam String mobileNumber) throws RuntimeException {
        return new ResponseEntity<>(customerService.findCustomerByMobileNumber(mobileNumber), HttpStatus.OK);
    }

    @GetMapping(value = "/fetchCustomerByMobileNumberException")
    public ResponseEntity<List<Customer>> fetchCustomerByMobileNumberException(@RequestParam String mobileNumber)  throws CustomerNotFoundException {

        try{
            return new ResponseEntity<>(customerService.findCustomerByMobileNumberException(mobileNumber), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/fetchCustomerByFirstNameProjection")
    public ResponseEntity<List<ICustomer>> fetchCustomerByFirstNameProjection(@RequestParam String firstName) throws RuntimeException {
        return new ResponseEntity<>(customerService.findCustomerByFirstNameProjection(firstName), HttpStatus.OK);
    }


    @GetMapping(value = "/fetchCustomerByFirstNameCustomQuery")
    public ResponseEntity<List<Customer>> fetchCustomerByFirstNameCustomQuery(@RequestParam String firstName) throws RuntimeException {
        return new ResponseEntity<>(customerService.findCustomerByFirstNameCustomQuery(firstName), HttpStatus.OK);
    }

    @GetMapping(value = "/fetchCustomerByLastNamePagination")
    public ResponseEntity<Page<Customer>> fetchCustomerByLastNamePagination(@RequestParam String lastName, int pageId, int pageSize) throws RuntimeException {
        return new ResponseEntity<>(customerService.findCustomerByLastNamePagination(lastName, pageId, pageSize), HttpStatus.OK);
    }

    @PostMapping(value = "/deleteCustomer")
    public ResponseEntity<String> deleteCustomer(HttpServletRequest requestHeader, @RequestBody Customer request) throws RuntimeException {

        if (customerService.deleteCustomer(request)) {
            return new ResponseEntity<>("Customer delete Done", HttpStatus.OK);

        } else {
            return new ResponseEntity<>("Customer delete failed", HttpStatus.OK);
        }
    }
}
