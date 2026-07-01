package com.gestor.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Esta classe representa um "Projeto" inteiro.
 */
public class Project {
    private String id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate deadline;
    private double budget;
    private Enums.Status status;
    private Enums.Priority priority;
    private String ownerId;
    private List<String> taskIds;

    // Datas calculadas para as fases (Requisito: 20/60/20)
    private LocalDate requirementsDeadline;
    private LocalDate developmentDeadline;
    private LocalDate deploymentDeadline;

    // Checkpoints de conclusão das fases
    private boolean requirementsDone = false;
    private boolean developmentDone = false;
    private boolean deploymentDone = false;

    public Project() {
        this.id = UUID.randomUUID().toString();
        this.taskIds = new ArrayList<>();
        this.status = Enums.Status.PENDENTE;
        this.startDate = LocalDate.now();
    }

    public Project(String name, String description, LocalDate deadline, double budget, String ownerId) {
        this();
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.budget = budget;
        this.ownerId = ownerId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }
    public Enums.Status getStatus() { return status; }
    public void setStatus(Enums.Status status) { this.status = status; }
    public Enums.Priority getPriority() { return priority; }
    public void setPriority(Enums.Priority priority) { this.priority = priority; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public List<String> getTaskIds() { return taskIds; }
    public void setTaskIds(List<String> taskIds) { this.taskIds = taskIds; }

    public LocalDate getRequirementsDeadline() { return requirementsDeadline; }
    public void setRequirementsDeadline(LocalDate d) { this.requirementsDeadline = d; }
    public LocalDate getDevelopmentDeadline() { return developmentDeadline; }
    public void setDevelopmentDeadline(LocalDate d) { this.developmentDeadline = d; }
    public LocalDate getDeploymentDeadline() { return deploymentDeadline; }
    public void setDeploymentDeadline(LocalDate d) { this.deploymentDeadline = d; }

    public boolean isRequirementsDone() { return requirementsDone; }
    public void setRequirementsDone(boolean b) { this.requirementsDone = b; }
    public boolean isDevelopmentDone() { return developmentDone; }
    public void setDevelopmentDone(boolean b) { this.developmentDone = b; }
    public boolean isDeploymentDone() { return deploymentDone; }
    public void setDeploymentDone(boolean b) { this.deploymentDone = b; }
}
