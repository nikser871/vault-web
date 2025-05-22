package meety.models;

import jakarta.persistence.*;
import lombok.*;
import meety.dtos.UserDto;
import meety.models.enums.Role;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "meety_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMember> groupMemberships;

    public User(UserDto userDto) {
        username = userDto.getUsername();
        password = userDto.getPassword();
        role = Role.USER;
        groupMemberships = List.of();
    }
}
