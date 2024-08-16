package com.taskmanagement.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.taskmanagement.model.RoleList;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "username")
  private String username;

  private String email;

  private String password;

  @Enumerated(EnumType.STRING)
  private RoleList role;

  @OneToMany(
      mappedBy = "user",
      fetch = FetchType.EAGER,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.PERSIST,CascadeType.REMOVE})
  @JsonIgnore
  private List<Task> tasks;
}
