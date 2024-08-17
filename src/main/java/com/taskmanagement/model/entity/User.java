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

  @JsonIgnore private String password;

  @Enumerated(EnumType.STRING)
  private RoleList role;

  @ManyToMany(
          cascade = {
                  CascadeType.DETACH,
                  CascadeType.REFRESH,
                  CascadeType.MERGE,
                  CascadeType.PERSIST
          }
  )
  @JoinTable(
          name = "user_tasks",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "task_id")
  )
  @JsonIgnore
  private List<Task> tasks;
}
