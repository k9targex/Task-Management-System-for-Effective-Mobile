package com.taskmanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.taskmanagement.dao.TaskRepository;
import com.taskmanagement.dao.UserRepository;
import com.taskmanagement.exception.PerformerNotFound;
import com.taskmanagement.exception.TaskAlreadyExistException;
import com.taskmanagement.exception.TaskNotFoundException;
import com.taskmanagement.model.Comment;
import com.taskmanagement.model.RoleList;
import com.taskmanagement.model.TaskPriority;
import com.taskmanagement.model.TaskStatus;
import com.taskmanagement.model.dto.TaskRequest;
import com.taskmanagement.model.dto.TaskUpdateRequest;
import com.taskmanagement.model.dto.UpdateStatusRequest;
import com.taskmanagement.model.entity.Task;
import com.taskmanagement.model.entity.User;
import com.taskmanagement.security.JwtCore;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


import java.util.*;

@ExtendWith(MockitoExtension.class)
 class UserServiceTest {
    private static final String USER_NOT_FOUND_MESSAGE = "User with name \"%s\" does not exist";
    private static final String TASK_ID_NOT_FOUND_MESSAGE =
            "Task with ID \"%s\" does not exist for this author";
    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private JwtCore jwtCore;


    @Mock
    private HttpServletRequest request;
    @InjectMocks
    private UserService userService;



    @Test
    void testLoadUserByUsername_ExistingUser() {
        String username = "testUser";
        User mockUser = new User();
        mockUser.setUsername(username);
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(mockUser));
        assertEquals(username, userService.loadUserByUsername(username).getUsername());
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        String username = "nonExistentUser";
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(username));
    }
    @Test
    void testGetAllAuthors() {
        User author = User.builder().role(RoleList.AUTHOR).build();
        List<User> authors = Collections.singletonList(author);
        when(userRepository.findAllWithTasksByRole(RoleList.AUTHOR)).thenReturn(authors);

        List<User> result = userService.getAllAuthors();
        assertEquals(authors, result);
    }

    @Test
    void testCreateTask_Success() {
        String token = "dummyToken";
        String username = "testUser";
        String taskTitle = "Test Task";

        TaskRequest taskRequest = TaskRequest.builder()
                .title(taskTitle)
                .description("Task Description")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.HIGH)
                .build();

        User mockUser = new User();
        mockUser.setUsername(username);

        mockUser.setTasks(new ArrayList<>());

        when(jwtCore.getTokenFromRequest(request)).thenReturn(token);
        when(jwtCore.getNameFromJwt(token)).thenReturn(username);
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(mockUser));
        when(taskRepository.existsByTitleAndAuthor(taskTitle, mockUser)).thenReturn(false);

        // Act
        userService.createTask(request, taskRequest);

        // Assert
        verify(taskRepository).save(any(Task.class));
        verify(userRepository).save(mockUser);
    }


    @Test
    void testCreateTask_TaskAlreadyExists() {
        // Arrange
        String token = "dummyToken";
        String username = "testUser";
        String taskTitle = "Test Task";

        TaskRequest taskRequest = TaskRequest.builder()
                .title(taskTitle)
                .description("Task Description")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.HIGH)
                .build();

        User mockUser = new User();
        mockUser.setUsername(username);

        when(jwtCore.getTokenFromRequest(request)).thenReturn(token);
        when(jwtCore.getNameFromJwt(token)).thenReturn(username);
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(mockUser));
        when(taskRepository.existsByTitleAndAuthor(taskTitle, mockUser)).thenReturn(true);

        // Act & Assert
        assertThrows(TaskAlreadyExistException.class, () -> userService.createTask(request, taskRequest));

        // Verify that the task was not saved
        verify(taskRepository, never()).save(any(Task.class));
        verify(userRepository, never()).save(mockUser);
    }
    @Test
    void testCreateTask_WithoutComment() {
        // Arrange
        String token = "dummyToken";
        String username = "testUser";
        String taskTitle = "Test Task";

        TaskRequest taskRequest = TaskRequest.builder()
                .title(taskTitle)
                .description("Task Description")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.HIGH)
                .build();


        User mockUser = User.builder().username(username).tasks(new ArrayList<>()).build();

        when(jwtCore.getTokenFromRequest(request)).thenReturn(token);
        when(jwtCore.getNameFromJwt(token)).thenReturn(username);
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(mockUser));
        when(taskRepository.existsByTitleAndAuthor(taskTitle, mockUser)).thenReturn(false);

        // Act
        userService.createTask(request, taskRequest);

        // Assert
        verify(taskRepository).save(any(Task.class)); // Проверяем сохранение задачи
        verify(userRepository).save(mockUser); // Проверяем сохранение пользователя

    }


    @Test
    void testDeleteTask_Success() {
        Long taskId = 1L;

        // Создаем пользователя и задачу
        User author = User.builder().tasks(new ArrayList<>()).build();
        Task task = Task.builder().id(taskId).author(author).build();

        // Добавляем задачу в список задач автора
        author.getTasks().add(task);

        when(jwtCore.getTokenFromRequest(request)).thenReturn("token");
        when(jwtCore.getNameFromJwt("token")).thenReturn("username");
        when(userRepository.findUserByUsername("username")).thenReturn(Optional.of(author));
        when(taskRepository.findTaskByIdAndAuthor(taskId, author)).thenReturn(Optional.of(task));

        // Выполняем удаление задачи
        userService.deleteTask(request, taskId);

        // Проверяем, что задачи и пользователь сохранены правильно
        verify(taskRepository, times(1)).delete(task);
        verify(userRepository, times(1)).save(author);
    }
    @Test
    void testGetUserTasks() {
        // Arrange
        String username = "testUser";
        User mockUser = User.builder().username(username).build();
        Task task = Task.builder().build();
        List<Task> tasks = Collections.singletonList(task);

        when(jwtCore.getTokenFromRequest(request)).thenReturn("dummyToken");
        when(jwtCore.getNameFromJwt("dummyToken")).thenReturn(username);
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(mockUser));
        when(taskRepository.findAllTasksByUser(mockUser)).thenReturn(tasks);

        // Act
        List<Task> result = userService.getUserTasks(request);

        // Assert
        assertEquals(tasks, result);
        verify(taskRepository).findAllTasksByUser(mockUser);
    }



    @Test
    void testUpdateTask_Success() {
        Long taskId = 1L;
        TaskUpdateRequest updateRequest = TaskUpdateRequest.builder().title("Updated Title").build();
        Task task = Task.builder().id(taskId).title("Old Title").build();
        User author = User.builder().build();

        when(jwtCore.getTokenFromRequest(request)).thenReturn("token");
        when(jwtCore.getNameFromJwt("token")).thenReturn("username");
        when(userRepository.findUserByUsername("username")).thenReturn(Optional.of(author));
        when(taskRepository.findTaskByIdAndAuthor(taskId, author)).thenReturn(Optional.of(task));

        userService.updateTask(taskId, updateRequest, request);

        verify(taskRepository, times(1)).save(task);
        assertEquals("Updated Title", task.getTitle());
    }




    @Test
    void testAddPerformer_Success() {
        Long taskId = 1L;
        Long performerId = 2L;
        Task task = Task.builder().id(taskId).build();
        User performer = User.builder().id(performerId).tasks(new ArrayList<>()).build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndRole(performerId, RoleList.PERFORMER)).thenReturn(Optional.of(performer));

        userService.addPerformer(taskId, performerId);

        verify(taskRepository, times(1)).save(task);
        verify(userRepository, times(1)).save(performer);
    }

    @Test
    void testAddPerformer_ReplaceOldPerformer() {
        // Arrange
        Long taskId = 1L;
        Long newPerformerId = 2L;
        Long oldPerformerId = 3L;

        // Создание задачи
        Task task = Task.builder()
                .id(taskId)
                .build();

        // Создание старого исполнителя с задачей
        User oldPerformer = User.builder()
                .id(oldPerformerId)
                .tasks(new ArrayList<>())
                .build();
        oldPerformer.getTasks().add(task);

        // Создание нового исполнителя без задач
        User newPerformer = User.builder()
                .id(newPerformerId)
                .tasks(new ArrayList<>())
                .build();

        task.setPerformer(oldPerformer);

        // Настройка моков
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndRole(newPerformerId,RoleList.PERFORMER)).thenReturn(Optional.of(newPerformer));
        // Act
        userService.addPerformer(taskId, newPerformerId);

        // Assert
        verify(taskRepository).save(task);
        verify(userRepository).save(newPerformer);
        verify(userRepository).save(oldPerformer);

        assertEquals(newPerformer, task.getPerformer());
        assertTrue(newPerformer.getTasks().contains(task));
        assertFalse(oldPerformer.getTasks().contains(task));
    }





    @Test
    void testAddPerformer_PerformerNotFound() {
        Long taskId = 1L;
        Long performerId = 2L;
        Task task = Task.builder().id(taskId).build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndRole(performerId, RoleList.PERFORMER)).thenReturn(Optional.empty());

        PerformerNotFound thrown = assertThrows(PerformerNotFound.class, () -> {
            userService.addPerformer(taskId, performerId);
        });
        assertEquals(String.format("Performer with ID \"%s\" does not exist", performerId), thrown.getMessage());
    }





    @Test
    void testGetUserTasksByIdWithFilters_Success() {
        Long userId = 1L;
        TaskStatus status = TaskStatus.IN_PROGRESS;
        TaskPriority priority = TaskPriority.HIGH;
        Pageable pageable = Pageable.unpaged();
        User user = User.builder().id(userId).build();
        Page<Task> taskPage = Page.empty();

        when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.findAllTasksByUserAndStatusAndPriority(user, status, priority, pageable)).thenReturn(taskPage);

        Page<Task> result = userService.getUserTasksByIdWithFilters(userId, pageable, status, priority);
        assertEquals(taskPage, result);
    }

    @Test
    void testGetUserTasksByIdWithFilters_UserNotFound() {
        Long userId = 1L;
        TaskStatus status = TaskStatus.IN_PROGRESS;
        TaskPriority priority = TaskPriority.HIGH;
        Pageable pageable = Pageable.unpaged();

        when(userRepository.findUserById(userId)).thenReturn(Optional.empty());

        UsernameNotFoundException thrown = assertThrows(UsernameNotFoundException.class, () -> {
            userService.getUserTasksByIdWithFilters(userId, pageable, status, priority);
        });
        assertEquals(String.format("User with id \"%s\" does not exist", userId), thrown.getMessage());
    }
    @Test
    void testAddCommentToTask_Success() {
        // Arrange
        Long taskId = 1L;
        String commentText = "This is a comment";
        String username = "testUser";
        Task task = Task.builder().id(taskId).comments(new ArrayList<>()).build();
        User user = User.builder().username(username).build();

        when(taskRepository.findTaskByIdAndUser(taskId, user)).thenReturn(Optional.of(task));
        when(jwtCore.getTokenFromRequest(request)).thenReturn("token");
        when(jwtCore.getNameFromJwt("token")).thenReturn(username);
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
        // Act
        userService.addCommentToTask(taskId, commentText, request);

        // Assert
        assertEquals(1, task.getComments().size());
        Comment comment = task.getComments().get(0);
        assertEquals(commentText, comment.getText());
        assertEquals(username, comment.getAuthor());
        verify(taskRepository).save(task);
    }
    @Test
    void testGetUserTasksByIdWithFilters_AllParameters() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();
        TaskStatus status = TaskStatus.PENDING;
        TaskPriority priority = TaskPriority.HIGH;
        User user = new User();
        Task task = new Task();
        Page<Task> taskPage = new PageImpl<>(Collections.singletonList(task));

        when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.findAllTasksByUserAndStatusAndPriority(user, status, priority, pageable))
                .thenReturn(taskPage);

        // Act
        Page<Task> result = userService.getUserTasksByIdWithFilters(userId, pageable, status, priority);

        // Assert
        assertEquals(taskPage, result);
        verify(userRepository, times(1)).findUserById(userId);
        verify(taskRepository, times(1))
                .findAllTasksByUserAndStatusAndPriority(user, status, priority, pageable);
    }
    @Test
    void testGetUserTasksByIdWithFilters_StatusOnly() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();
        TaskStatus status = TaskStatus.PENDING;
        User user = new User();
        Task task = new Task();
        Page<Task> taskPage = new PageImpl<>(Collections.singletonList(task));

        when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.findAllTasksByUserAndStatus(user, status, pageable)).thenReturn(taskPage);

        // Act
        Page<Task> result = userService.getUserTasksByIdWithFilters(userId, pageable, status, null);

        // Assert
        assertEquals(taskPage, result);
        verify(userRepository, times(1)).findUserById(userId);
        verify(taskRepository, times(1)).findAllTasksByUserAndStatus(user, status, pageable);
    }

    @Test
    void testGetUserTasksByIdWithFilters_PriorityOnly() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();
        TaskPriority priority = TaskPriority.HIGH;
        User user = new User();
        Task task = new Task();
        Page<Task> taskPage = new PageImpl<>(Collections.singletonList(task));

        when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.findAllTasksByUserAndPriority(user, priority, pageable)).thenReturn(taskPage);

        // Act
        Page<Task> result = userService.getUserTasksByIdWithFilters(userId, pageable, null, priority);

        // Assert
        assertEquals(taskPage, result);
        verify(userRepository, times(1)).findUserById(userId);
        verify(taskRepository, times(1)).findAllTasksByUserAndPriority(user, priority, pageable);
    }
    @Test
    void testGetUserTasksByIdWithFilters_NoFilters() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();
        User user = new User();
        Task task = new Task();
        Page<Task> taskPage = new PageImpl<>(Collections.singletonList(task));

        when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.findAllTasksByUser(user, pageable)).thenReturn(taskPage);

        // Act
        Page<Task> result = userService.getUserTasksByIdWithFilters(userId, pageable, null, null);

        // Assert
        assertEquals(taskPage, result);
        verify(userRepository, times(1)).findUserById(userId);
        verify(taskRepository, times(1)).findAllTasksByUser(user, pageable);
    }
    @Test
    void testAddCommentToTask_TaskNotFound() {
        // Arrange
        Long taskId = 1L;
        String commentText = "This is a comment";
        String username = "testUser";
        User user = User.builder().username(username).build();

        when(taskRepository.findTaskByIdAndUser(taskId, user)).thenReturn(Optional.empty());
        when(jwtCore.getTokenFromRequest(request)).thenReturn("token");
        when(jwtCore.getNameFromJwt("token")).thenReturn(username);
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () ->
                userService.addCommentToTask(taskId, commentText, request));
    }

    @Test
    void testUpdateStatus_Success() {
        // Arrange
        Long taskId = 1L;
        TaskStatus newStatus = TaskStatus.COMPLETED;
        UpdateStatusRequest updateStatusRequest =  UpdateStatusRequest.builder()
                .status(newStatus)
                .build();

        Task task = new Task();
        task.setStatus(TaskStatus.PENDING);

        User performer = new User();
        performer.setUsername("performer");

        when(taskRepository.findTaskByIdAndPerformer(taskId, performer)).thenReturn(Optional.of(task));
        when(jwtCore.getTokenFromRequest(request)).thenReturn("dummyToken");
        when(jwtCore.getNameFromJwt("dummyToken")).thenReturn("performer");
        when(userRepository.findUserByUsername("performer")).thenReturn(Optional.of(performer));

        // Act
        userService.updateStatus(updateStatusRequest, taskId, request);

        // Assert
        assertEquals(newStatus, task.getStatus());
        verify(taskRepository, times(1)).save(task);
    }
    @Test
    void testUpdateStatus_TaskNotFound() {
        // Arrange
        Long taskId = 1L;
        UpdateStatusRequest updateStatusRequest = UpdateStatusRequest.builder()
                .status(TaskStatus.COMPLETED)
                .build();
    User performer = new User();
        performer.setUsername("performer");

        when(jwtCore.getTokenFromRequest(request)).thenReturn("dummyToken");
        when(jwtCore.getNameFromJwt("dummyToken")).thenReturn("performer");
        when(userRepository.findUserByUsername("performer")).thenReturn(Optional.of(performer));
        when(taskRepository.findTaskByIdAndPerformer(taskId, performer)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () ->
                userService.updateStatus(updateStatusRequest, taskId, request));
    }
    @Test
    void testGetTaskByUser_UserNotFound() {
        // Arrange
        Long taskId = 1L;

        when(jwtCore.getTokenFromRequest(request)).thenReturn("token");
        when(jwtCore.getNameFromJwt("token")).thenReturn("username");
        when(userRepository.findUserByUsername("username")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.addCommentToTask(taskId,"comment",request));

        assertEquals(String.format(USER_NOT_FOUND_MESSAGE, "username"), exception.getMessage());
    }
    @Test
    void testGetCommentsByTaskId_Success() {
        // Arrange
        Long taskId = 1L;
        Task task = new Task();
        Comment comment1 = new Comment();
        Comment comment2 = new Comment();
        List<Comment> comments = Arrays.asList(comment1, comment2);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.findCommentsByTaskId(taskId)).thenReturn(comments);

        // Act
        List<Comment> result = userService.getCommentsByTaskId(taskId);

        // Assert
        assertEquals(comments, result);
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).findCommentsByTaskId(taskId);
    }

    @Test
    void testGetCommentsByTaskId_TaskNotFound() {
        // Arrange
        Long taskId = 1L;

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () ->
                userService.getCommentsByTaskId(taskId));
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).findCommentsByTaskId(taskId);
    }

    @Test
    void testDeleteTask_TaskNotFound() {
        // Arrange
        Long taskId = 1L;
        User mockUser = new User();  // Mock user object
        when(jwtCore.getTokenFromRequest(request)).thenReturn("token");
        when(jwtCore.getNameFromJwt("token")).thenReturn("username");
        when(userRepository.findUserByUsername("username")).thenReturn(Optional.of(mockUser));
        when(taskRepository.findTaskByIdAndAuthor(taskId, mockUser)).thenReturn(Optional.empty());

        // Act & Assert
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> userService.deleteTask(request, taskId)
        );

        assertEquals(String.format(TASK_ID_NOT_FOUND_MESSAGE, taskId), exception.getMessage());
        verify(taskRepository, never()).delete(any(Task.class));
        verify(userRepository, never()).save(any(User.class));
    }


}