package com.fpt.capstone.tourism.model;

import com.fpt.capstone.tourism.model.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotNull(message = "Username cannot be null")
    @Size(min = 8, max = 30, message = "Username must be between 3 and 30 characters")
    @Column(nullable = false, unique = true)
    private String username;

    @Email(message = "Email should be valid")
    @NotNull(message = "Email cannot be null")
    @Column(nullable = false, unique = true)
    private String email;

    private Gender gender;

    @NotNull(message = "Password cannot be null")
    @Size(min = 8, message = "Password must be at least 6 characters long")
    @ToString.Exclude
    private String password;

    private String phone;

    private String address;

    @Column(name="avatar_img")
    private String avatarImage;

    @Column(name = "email_confirmed")
    private boolean emailConfirmed;

    @Column(name="is_deleted")
    private Boolean deleted;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRoles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRole().getRoleName()))
                .collect(Collectors.toList());
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<UserRole> userRoles;
}
