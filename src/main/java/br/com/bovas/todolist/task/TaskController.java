package br.com.bovas.todolist.task;

import br.com.bovas.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        taskModel.setIdUser((UUID) idUser);

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start date or end date must be newer than the current date!");
        }
        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start date must be older than the end date!");
        }
        var task = taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body((task));
    }

    @GetMapping
    public List<TaskModel> findTask(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        return taskRepository.findByIdUser((UUID) idUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateTask(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
        var task = taskRepository.findById(id).orElse(null);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task not found.");
        }

        var idUser = request.getAttribute("idUser");

        if ((!task.getIdUser().equals(idUser))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This user does not have permission to change this task!");
        }

        Utils.copyNonNullProperties(taskModel, task);
        var taskUpdated = taskRepository.save(task);
        return ResponseEntity.ok().body(taskUpdated);
    }
}
