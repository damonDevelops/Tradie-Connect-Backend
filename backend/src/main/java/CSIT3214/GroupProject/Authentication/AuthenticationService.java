package CSIT3214.GroupProject.Authentication;

import CSIT3214.GroupProject.Config.JwtService;
import CSIT3214.GroupProject.DataAccessLayer.CustomUserDetails;
import CSIT3214.GroupProject.DataAccessLayer.CustomerRepository;
import CSIT3214.GroupProject.DataAccessLayer.ServiceProviderRepository;
import CSIT3214.GroupProject.DataAccessLayer.UserDTO;
import CSIT3214.GroupProject.Model.Customer;
import CSIT3214.GroupProject.Model.Role;
import CSIT3214.GroupProject.Model.ServiceProvider;
import CSIT3214.GroupProject.Model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final CustomerRepository customerRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(UserDTO userDTO) {
        UserDetails userDetails;
        User user;
        if (userDTO.getRole() == Role.ROLE_CUSTOMER) {
            var customer = new Customer();
            // Set customer properties from userDTO
            customer.setFirstName(userDTO.getFirstName());
            customer.setLastName(userDTO.getLastName());
            customer.setEmail(userDTO.getEmail());
            customer.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            customer.setRole(Role.ROLE_CUSTOMER);
            customer.setPhoneNumber(userDTO.getPhoneNumber());
            customer.setStreetAddress(userDTO.getStreetAddress());
            customer.setSuburb(userDTO.getSuburb());
            customer.setPostCode(userDTO.getPostCode());
            //customer.setMembership(userDTO.getMembership());
            customerRepository.save(customer);
            userDetails = new CustomUserDetails(customer, List.of(new SimpleGrantedAuthority(customer.getRole().toString())));
            user = customer;
        } else {
            var serviceProvider = new ServiceProvider();
            // Set serviceProvider properties from userDTO
            serviceProvider.setCompanyName(userDTO.getCompanyName());
            serviceProvider.setAbn(userDTO.getAbn());
            serviceProvider.setEmail(userDTO.getEmail());
            serviceProvider.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            serviceProvider.setRole(Role.ROLE_SERVICE_PROVIDER);
            serviceProvider.setPhoneNumber(userDTO.getPhoneNumber());
            serviceProvider.setStreetAddress(userDTO.getStreetAddress());
            serviceProvider.setPostCode(userDTO.getPostCode());
            serviceProvider.setSuburb(userDTO.getSuburb());
            //serviceProvider.setMembership(userDTO.getMembership());
            serviceProviderRepository.save(serviceProvider);
            userDetails = new CustomUserDetails(serviceProvider, List.of(new SimpleGrantedAuthority(serviceProvider.getRole().toString())));
            user = serviceProvider;
        }

        var jwtToken = jwtService.generateToken(userDetails);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .user(user)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserDetails userDetails;
        var customer = customerRepository.findByEmail(request.getEmail());
        if (customer.isPresent()) {
            userDetails = new CustomUserDetails(customer.get(), List.of(new SimpleGrantedAuthority(customer.get().getRole().toString())));
        } else {
            var serviceProvider = serviceProviderRepository.findByEmail(request.getEmail()).orElseThrow();
            userDetails = new CustomUserDetails(serviceProvider, List.of(new SimpleGrantedAuthority(serviceProvider.getRole().toString())));
        }

        var jwtToken = jwtService.generateToken(userDetails);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

}

