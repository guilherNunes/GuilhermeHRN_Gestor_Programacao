package com.gestor.service;

import com.gestor.model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servico de gerenciamento de Projetos e Tarefas.
 * Requisito: Operacoes basicas de CRUD (Criar, Ler, Atualizar, Deletar) e logs de auditoria.
 */
public class ProjectService {
    private Database db = Database.getInstance();

    /**
     * Adiciona um novo projeto ao sistema.
     * Requisito: CRUD - Criar Projeto.
     */
    public void addProject(Project p) {
        db.getProjects().add(p);
        db.saveProjects();
        db.log(Enums.LogAction.CRIACAO, "Projeto criado: " + p.getName());
    }

    /**
     * Atualiza um projeto existente.
     * Requisito: CRUD - Atualizar Projeto.
     */
    public void updateProject(Project updatedProject) {
        for (int i = 0; i < db.getProjects().size(); i++) {
            if (db.getProjects().get(i).getId().equals(updatedProject.getId())) {
                db.getProjects().set(i, updatedProject);
                db.saveProjects();
                db.log(Enums.LogAction.ALTERACAO, "Projeto atualizado: " + updatedProject.getName());
                return;
            }
        }
    }

    /**
     * Deleta um projeto e suas tarefas associadas.
     * Requisito: CRUD - Deletar Projeto.
     */
    public void deleteProject(String projectId) {
        Project p = findProjectById(projectId);
        if (p != null) {
            db.getProjects().removeIf(project -> project.getId().equals(projectId));
            // Remove todas as tarefas associadas a este projeto
            db.getTasks().removeIf(task -> task.getProjectId().equals(projectId));
            db.saveProjects();
            db.saveTasks();
            db.log(Enums.LogAction.EXCLUSAO, "Projeto excluido: " + p.getName());
        }
    }

    /**
     * Adiciona uma tarefa a um projeto específico.
     * Requisito: CRUD - Criar Tarefa.
     */
    public void addTask(Task t) {
        db.getTasks().add(t);
        db.saveTasks();
        Project p = findProjectById(t.getProjectId());
        if (p != null) {
            p.getTaskIds().add(t.getId());
            db.saveProjects();
        }
        db.log(Enums.LogAction.CRIACAO, "Tarefa criada: " + t.getTitle() + " no projeto " + (p != null ? p.getName() : "[desconhecido]"));
    }

    /**
     * Atualiza uma tarefa existente.
     * Requisito: CRUD - Atualizar Tarefa.
     */
    public void updateTask(Task updatedTask) {
        for (int i = 0; i < db.getTasks().size(); i++) {
            if (db.getTasks().get(i).getId().equals(updatedTask.getId())) {
                db.getTasks().set(i, updatedTask);
                db.saveTasks();
                db.log(Enums.LogAction.ALTERACAO, "Tarefa atualizada: " + updatedTask.getTitle());
                return;
            }
        }
    }

    /**
     * Deleta uma tarefa.
     * Requisito: CRUD - Deletar Tarefa.
     */
    public void deleteTask(String taskId) {
        Task t = findTaskById(taskId);
        if (t != null) {
            db.getTasks().removeIf(task -> task.getId().equals(taskId));
            // Remove a tarefa do projeto associado
            Project p = findProjectById(t.getProjectId());
            if (p != null) {
                p.getTaskIds().remove(taskId);
                db.saveProjects();
            }
            db.saveTasks();
            db.log(Enums.LogAction.EXCLUSAO, "Tarefa excluida: " + t.getTitle());
        }
    }

    public Project findProjectById(String id) {
        return db.getProjects().stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    public Task findTaskById(String id) {
        return db.getTasks().stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Task> getTasksByProject(String projectId) {
        return db.getTasks().stream().filter(t -> t.getProjectId().equals(projectId)).collect(Collectors.toList());
    }

    /**
     * LÓGICA AVANÇADA: Sugestão Automática de Prazo.
     * O sistema olha se você costuma atrasar projetos e sugere um prazo mais realista.
     */
    public LocalDate suggestDeadline(int complexityInDays) {
        List<Project> finished = db.getProjects().stream()
                .filter(p -> p.getStatus() == Enums.Status.CONCLUIDO)
                .collect(Collectors.toList());
        
        double multiplier = 1.2; // Por padrão, damos 20% a mais de tempo por segurança
        
        if (!finished.isEmpty()) {
            long delayedCount = finished.stream().filter(p -> p.getStatus() == Enums.Status.ATRASADO).count();
            if (delayedCount > finished.size() / 2) multiplier = 1.5;
        }
        
        return LocalDate.now().plusDays((long) (complexityInDays * multiplier));
    }

    /**
     * MÉTRICA: Calcula a média de horas que você trabalha por dia.
     */
    public double calculateAverageHoursPerDay() {
        List<Activity.TimeRecord> records = db.getTimeRecords();
        if (records.isEmpty()) return 0;
        
        double totalHours = records.stream().mapToDouble(Activity.TimeRecord::getHours).sum();
        long days = records.stream().map(r -> r.getDate().toLocalDate()).distinct().count();
        
        return days == 0 ? 0 : totalHours / days;
    }
}
