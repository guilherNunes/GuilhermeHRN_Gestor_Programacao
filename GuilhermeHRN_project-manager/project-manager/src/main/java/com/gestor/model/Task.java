package com.gestor.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Esta classe representa uma "Tarefa" (um pequeno pedaço de trabalho).
 * Imagine como um post-it num quadro de avisos.
 */
public class Task {
    private String id;          // O código único da tarefa
    private String title;       // O nome da tarefa (ex: "Criar botão de login")
    private String description; // Explicação detalhada do que deve ser feito
    private Enums.Priority priority; // Se é urgente ou não
    private Enums.Status status;     // Se está pronta, fazendo ou pendente
    private List<String> tags;       // Etiquetas para organizar (ex: "Backend", "Urgente")
    private int progress;            // Quanto por cento já foi feito (0 a 100)
    private String projectId;        // A qual projeto essa tarefa pertence

    // Prepara uma tarefa nova, vazia e pronta para ser preenchida
    public Task() {
        this.id = UUID.randomUUID().toString();
        this.tags = new ArrayList<>(); // Cria uma listinha de etiquetas vazia
        this.progress = 0;             // Começa com 0% feito
        this.status = Enums.Status.PENDENTE; // Começa como "Pendente"
    }

    // Cria uma tarefa já com nome, descrição e prioridade definida
    public Task(String title, String description, Enums.Priority priority, String projectId) {
        this();
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.projectId = projectId;
    }

    // Métodos para ler (get) e escrever (set) as informações da tarefa
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Enums.Priority getPriority() { return priority; }
    public void setPriority(Enums.Priority priority) { this.priority = priority; }
    public Enums.Status getStatus() { return status; }
    public void setStatus(Enums.Status status) { this.status = status; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
}
