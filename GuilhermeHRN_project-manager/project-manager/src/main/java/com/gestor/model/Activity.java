package com.gestor.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Esta classe é como um "Caderno de Registros".
 * Ela guarda o tempo gasto e tudo o que aconteceu no sistema.
 */
public class Activity {
    
    /**
     * Representa um "Cartão de Ponto".
     * Serve para marcar: "Gastei 2 horas fazendo tal coisa".
     */
    public static class TimeRecord {
        private String id;
        private String entityId;    // O código da tarefa ou projeto onde o tempo foi gasto
        private Enums.TimeType type; // Se foi trabalho, reunião ou estudo
        private double hours;        // Quantas horas foram gastas
        private LocalDateTime date;  // Data e hora exata do registro

        public TimeRecord() {
            this.id = UUID.randomUUID().toString();
            this.date = LocalDateTime.now(); // Marca o momento atual
        }

        // Métodos para o sistema ler e salvar os dados do tempo
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getEntityId() { return entityId; }
        public void setEntityId(String entityId) { this.entityId = entityId; }
        public Enums.TimeType getType() { return type; }
        public void setType(Enums.TimeType type) { this.type = type; }
        public double getHours() { return hours; }
        public void setHours(double hours) { this.hours = hours; }
        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }
    }

    /**
     * Representa um "Livro de Auditoria".
     * Guarda um histórico: "Quem fez o quê, quando e onde".
     * Isso é ótimo para segurança e para saber quem alterou um projeto.
     */
    public static class AuditLog {
        private String id;
        private String userId;    // Quem fez a ação
        private String entityId;  // Em qual projeto/tarefa a ação foi feita
        private Enums.LogAction action; // O que foi feito (Criou? Deletou? Alterou?)
        private String details;   // Uma mensagem explicando melhor (ex: "Mudou o prazo para amanhã")
        private LocalDateTime timestamp; // Momento exato que aconteceu

        public AuditLog() {
            this.id = UUID.randomUUID().toString();
            this.timestamp = LocalDateTime.now();
        }

        // Cria um log já com os detalhes preenchidos
        public AuditLog(String userId, String entityId, Enums.LogAction action, String details) {
            this();
            this.userId = userId;
            this.entityId = entityId;
            this.action = action;
            this.details = details;
        }

        // Métodos de acesso aos dados do histórico
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getEntityId() { return entityId; }
        public void setEntityId(String entityId) { this.entityId = entityId; }
        public Enums.LogAction getAction() { return action; }
        public void setAction(Enums.LogAction action) { this.action = action; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}
