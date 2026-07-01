package com.gestor.service;

import com.gestor.model.*;
import com.gestor.util.JsonUtil;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

/**
 * Singleton de Persistencia.
 * Requisito: Salvar e carregar dados de arquivos JSON.
 */
public class Database {
    private static Database instance;
    private List<User> users;
    private List<Project> projects;
    private List<Task> tasks;
    private List<Activity.TimeRecord> timeRecords;
    private List<Activity.AuditLog> auditLogs;

    private Database() {
        loadAll();
    }

    public static synchronized Database getInstance() {
        if (instance == null) instance = new Database();
        return instance;
    }

    public void loadAll() {
        users = JsonUtil.loadList("users.json", new TypeToken<ArrayList<User>>(){}.getType());
        projects = JsonUtil.loadList("projects.json", new TypeToken<ArrayList<Project>>(){}.getType());
        tasks = JsonUtil.loadList("tasks.json", new TypeToken<ArrayList<Task>>(){}.getType());
        timeRecords = JsonUtil.loadList("timeRecords.json", new TypeToken<ArrayList<Activity.TimeRecord>>(){}.getType());
        auditLogs = JsonUtil.loadList("auditLogs.json", new TypeToken<ArrayList<Activity.AuditLog>>(){}.getType());
        
        if (users.isEmpty()) {
            // Requisito: Incluir e-mail no admin padrao
            users.add(new User("admin", "admin", "admin@gestor.com", Enums.UserRole.ADMIN));
            saveUsers();
        }
    }

    public void saveUsers() { JsonUtil.saveList(users, "users.json"); }
    public void saveProjects() { JsonUtil.saveList(projects, "projects.json"); }
    public void saveTasks() { JsonUtil.saveList(tasks, "tasks.json"); }
    public void saveTimeRecords() { JsonUtil.saveList(timeRecords, "timeRecords.json"); }
    public void saveAuditLogs() { JsonUtil.saveList(auditLogs, "auditLogs.json"); }

    /**
     * Registra uma acao no log de auditoria.
     */
    public void log(Enums.LogAction action, String details) {
        Activity.AuditLog entry = new Activity.AuditLog();
        entry.setAction(action);
        entry.setDetails(details);
        entry.setTimestamp(LocalDateTime.now());
        if (AuthService.getCurrentUser() != null) {
            entry.setUserId(AuthService.getCurrentUser().getId());
        }
        auditLogs.add(entry);
        saveAuditLogs();
    }

    public List<User> getUsers() { return users; }
    public List<Project> getProjects() { return projects; }
    public List<Task> getTasks() { return tasks; }
    public List<Activity.TimeRecord> getTimeRecords() { return timeRecords; }
    public List<Activity.AuditLog> getAuditLogs() { return auditLogs; }
}
