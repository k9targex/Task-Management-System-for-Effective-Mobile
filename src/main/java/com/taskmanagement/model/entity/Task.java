package com.taskmanagement.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.taskmanagement.model.Comment;
import com.taskmanagement.model.TaskPriority;
import com.taskmanagement.model.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task")
    private String title;

    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_comments", joinColumns = @JoinColumn(name = "task_id"))
    private List<Comment> comments;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="performer_id")
    private User performer;


}
