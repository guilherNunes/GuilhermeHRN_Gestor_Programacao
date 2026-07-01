package com.gestor.model;

import java.util.UUID;

/**
 * Entidade de Usuário do Sistema.
 * Requisito: Organização em classes bem estruturadas e campos claros.
 */
public class User {
    private String id;
    private String username;  // Limite: 20 caracteres
    private String password;  // Limite: 50 caracteres
    private String email;     // Requisito: Validação de e-mail
    private Enums.UserRole role;

    public User() { 
        this.id = UUID.randomUUID().toString(); 
    }

    public User(String username, String password, String email, Enums.UserRole role) {
        this();
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Enums.UserRole getRole() { return role; }
    public void setRole(Enums.UserRole role) { this.role = role; }
}
